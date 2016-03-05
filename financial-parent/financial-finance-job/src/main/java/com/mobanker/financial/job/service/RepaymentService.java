package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.common.utils.HttpClientUtils;
import com.mobanker.financial.common.constants.FinanceConstants.RepayConstant;
import com.mobanker.financial.common.constants.FinanceConstants.TradeCodeConstant;
import com.mobanker.financial.common.constants.MessageContants.SendMessageCode;
import com.mobanker.financial.common.constants.MessageContants.TepmlateNid;
import com.mobanker.financial.common.constants.SinaConstants.SinaUrl;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.constants.SystemConstants.SysStatus;
import com.mobanker.financial.common.enums.BusinessType;
import com.mobanker.financial.common.exceptions.BusinessException;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.FinanceRefundStatistics;
import com.mobanker.financial.entity.FinanceRepayPlan;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceRefundStatisticsService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceRepayPlanService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.financial.service.FinanceTradeService;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.financial.service.FinanceWarningService;
import com.mobanker.framework.dto.ResponseEntity;

/**
 * Description: 理财财务系统还款功能
 * 
 * Detail:  1、可批量还款
 * 			2、可按标的号还款
 *          3、可按还款明细订单号还款
 * 
 * @author yinyafei
 * 
 * 2015.11.16
 * 
 * vesrion 1.0 重构还款功能，增加定时任务，进行自动还款
 *         1.1 骚年！你已被上天选中，拯救苍生的任务就交给你了！
 */
@Service
public class RepaymentService {
	
	private static final Logger logger = LoggerFactory.getLogger(RepaymentService.class);
	private final String repayLogPrefix = "--------------还款处理：";
	
	@Resource
	private FinanceTenderCfgService tenderCfgService;
	@Resource
	private FinanceRepayPlanDetailService repayPlanDetailService;
	@Resource
	private FinanceTradeService tradeService;
	@Resource
	private FinanceSubmitTenderService submitTenderService;
	@Resource
	private FinanceRepayPlanService financeRepayPlanService;
	@Resource
	private FinanceRefundStatisticsService refundStatisticsService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private MessageCenter messageCenter;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private FinanceWarningService financeWarningService;

	/**
	 * 获取需要还款的数据
	 * 还款统计表中已制单、已审核通过、已充值的数据
	 * 自动还款已订单号方式进行还
	 */
	public void autoRepay() {

		String autoRepayDate = commonCfgService.getCommonCfgValueByCode("AUTO_REFUND_DATE");
		if (StringUtils.isEmpty(autoRepayDate)) {
			autoRepayByDate(new Date());
		} else {
			autoRepayByDate(DateUtils.convert(autoRepayDate, DateUtils.DATE_FORMAT));
		}
	}
	
	/**
	 * 可指定日期执行
	 * @param date
	 */
	public void autoRepayByDate(Date date) {

		logger.debug("{}执行时间{}！", repayLogPrefix, DateUtils.convert(new Date()));
		
		// 将需要还款的明细数据以标的为单位
		Map<String, List<FinanceRepayPlanDetail>> map = new HashMap<String, List<FinanceRepayPlanDetail>>();

		FinanceRepayPlanDetail planDetailParams = new FinanceRepayPlanDetail();
		planDetailParams.setRefundTime(DateUtils.getStartDatetime(date));
		planDetailParams.setStatus(0);
		List<FinanceRepayPlanDetail> planDetailList = repayPlanDetailService.getByObj(planDetailParams);
		planDetailList = filterRepayDetailList(planDetailList);
		
		logger.debug("{}还款明细个{}！", repayLogPrefix, planDetailList.size());
		
		for (FinanceRepayPlanDetail planDetail : planDetailList) {

			String tenderId = planDetail.getTid();
			if (map.containsKey(tenderId)) {
				map.get(tenderId).add(planDetail);
			} else {
				List<FinanceRepayPlanDetail> groupList = new ArrayList<FinanceRepayPlanDetail>();
				groupList.add(planDetail);
				map.put(tenderId, groupList);
			}
			planDetail.setUpdateUser("自动还款");
		}
		
		for (String tenderId : map.keySet()) {

			FinanceTenderCfg tenderCfg = tenderCfgService.getById(tenderId);
			if (tenderCfg != null) {
				List<FinanceRepayPlanDetail> groupList = map.get(tenderId);

				// 开始交易
				beginTrade(groupList, tenderCfg);
			}
		}
	}
	
	
	/**
	 * 批量还款
	 * @param tenderNos 多个标的号已逗号分割
	 */
	public void repayByBatch(String tenderNos) throws BusinessException {

		logger.debug("{}批量还款单号{}！", repayLogPrefix, tenderNos);
		
		String[] tenderNoArray = tenderNos.split(",");
		for (String tenderNo : tenderNoArray) {
			repayByTender(tenderNo);
		}
	}

	/**
	 * 按标的号还款
	 * @param tenderNo 标的号
	 */
	public void repayByTender(String tenderNo) throws BusinessException {

		if (StringUtils.isEmpty(tenderNo)) {
			throw new BusinessException("还款标的号不可为空!");
		}

		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setTenderNo(tenderNo);

		List<FinanceTenderCfg> tenderCfgList = tenderCfgService.getByObj(findParams);
		if (tenderCfgList.size() > 0) {

			FinanceTenderCfg tenderCfg = tenderCfgList.get(0);
			String tenderId = tenderCfg.getId();

			FinanceRepayPlanDetail detailFindParams = new FinanceRepayPlanDetail();
			detailFindParams.setTid(tenderId);
			detailFindParams.setStatus(0); // 未还款记录
			List<FinanceRepayPlanDetail> repayPlanDetailList = repayPlanDetailService.getByObj(detailFindParams);
			repayPlanDetailList = filterRepayDetailList(repayPlanDetailList);

			// 开始交易
			beginTrade(repayPlanDetailList, tenderCfg);

		} else {
			logger.error("{} 无法找到标的信息:{}", repayLogPrefix, tenderNo);
			throw new BusinessException("标的信息不存在!" + tenderNo);
		}
	}

	/**
	 * 按还款计划明细订单号还款
	 * @param orderNo 还款明细订单号
	 */
	public void repayByOrderNo(String orderNo) throws BusinessException {
		FinanceRepayPlanDetail detailFindParams = new FinanceRepayPlanDetail();
		detailFindParams.setOrderNo(orderNo);
		detailFindParams.setStatus(0); // 未还款记录
		List<FinanceRepayPlanDetail> repayPlanDetailList = repayPlanDetailService.getByObj(detailFindParams);
		repayPlanDetailList = filterRepayDetailList(repayPlanDetailList);
		
		FinanceTenderCfg tenderCfg = null;
		if (repayPlanDetailList.size() > 0) {
			String tenderId = repayPlanDetailList.get(0).getTid();
			tenderCfg = tenderCfgService.getById(tenderId);

			if (tenderCfg == null) {
				logger.error("{} 无法找到标的信息:{}", repayLogPrefix, tenderId);
				throw new BusinessException("标的信息不存在!" + orderNo);
			}
		}

		// 开始交易
		beginTrade(repayPlanDetailList, tenderCfg);
	}
	
	/**
	 * 交易处理
	 * 调用新浪代收代付
	 * @param repayDetail
	 * @param tenderCfg
	 */
	private void beginTrade(List<FinanceRepayPlanDetail> repayPlanDetailList, FinanceTenderCfg tenderCfg) throws BusinessException{

		// 校验用户的钱是否都已代收成功
		boolean flag = BeforeBatchPayCheck(tenderCfg);
		if (!flag) {
			logger.error("{} 前校验--调用户新浪代收未完成,无法继续批量代付", repayLogPrefix);
			financeWarningService.sendWarning("还款失败");
			return;
		}
		
		for (FinanceRepayPlanDetail repayPlanDetail : repayPlanDetailList) {
			// 代收交易
			colletionTradeProcess(repayPlanDetail, tenderCfg);
		}

		// 校验用户的钱是否都已代收成功
		flag = BeforeBatchPayCheck(tenderCfg);
		if (!flag) {
			logger.error("{} 调用户新浪代收未完成,无法继续批量代付", repayLogPrefix);
			return;
		}
		
		// 批量代付
		batchPayTradeProcess(repayPlanDetailList, tenderCfg);
		// 更新标的相关信息
		updateSubmitTenderInfo(repayPlanDetailList, tenderCfg);
	}
	
	/**
	 * 还款明细过滤
	 * 过滤掉没有在还款统计中的记录
	 * @param repayPlanDetailList
	 */
	public List<FinanceRepayPlanDetail> filterRepayDetailList(List<FinanceRepayPlanDetail> repayPlanDetailList) {

		List<FinanceRepayPlanDetail> filterPlanDetailList = new ArrayList<FinanceRepayPlanDetail>();

		for (FinanceRepayPlanDetail planDetail : repayPlanDetailList) {

			Date refundTimeDate = planDetail.getRefundTime();

			String tenderId = planDetail.getTid();
			FinanceTenderCfg tenderCfg = tenderCfgService.getFinanceTenderCfg(tenderId);

			FinanceRefundStatistics findParams = new FinanceRefundStatistics();
			findParams.setPlanRefundTime(refundTimeDate);
			findParams.setDocStatus("1");
			findParams.setCheckStatus("1");
			findParams.setBorrowerRechargeStatus("1");
			findParams.setFinancialUser(tenderCfg.getUid());
			List<FinanceRefundStatistics> refundStatisticsList = refundStatisticsService.getByObj(findParams);
			if (refundStatisticsList.size() > 0) {
				filterPlanDetailList.add(planDetail);
			}
		}
		return filterPlanDetailList;
	}
	
	/**
	 * 从理财人代收资金
	 * 
	 * @param repayDetail 还款计划明细
	 * 
	 * 资金流： 理财人充值 ----> 代收本金、利息 ----> 新浪中间账户 ----> 批量代付到投资人账户
	 */
	public void colletionTradeProcess(FinanceRepayPlanDetail repayDetail, FinanceTenderCfg tenderCfg) {

		// 代收利息
		String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATE_HOSTING_COLLECT_TRADE);
		if (!StringUtils.isEmpty(url)) {

			String uid = tenderCfg.getUid();
			// 调用新浪接口请求参数
			Map<String, String> params = new HashMap<String, String>();

			// 代收利息
			BigDecimal interestBg = repayDetail.getInterestPayable();
			if (interestBg.compareTo(BigDecimal.ZERO) > 0) {
				params.put("payerId", uid);
				params.put("amount", interestBg.toString());
				params.put("summary", RepayConstant.COLLECTION_INTEREST);
				params.put("balancePayType", "SAVING_POT");
				String outTradeNo = BillNoUtils.GenerateBillNo();
				params.put("outTradeNo", outTradeNo);
				params.put("outTradeCode", TradeCodeConstant.TRADE_CODE_COLLECT_REFUND);

				String status = "FAILED";
				String result = "";
				try {
					
					logger.debug("{} 代收请求:{}", repayLogPrefix, params);
					result = HttpClientUtils.doPost(url, params);
					logger.debug("{} 代收结果:{}", repayLogPrefix, result);
					ResponseEntity responseEntity = JSONObject.parseObject(result, ResponseEntity.class);
					if (responseEntity.getStatus().equals(SysStatus.OK)) {
						if (responseEntity.getData() != null) {
							JSONObject jsonObj = JSONObject.parseObject(responseEntity.getData().toString());
							if (jsonObj.get("trade_status") != null) {
								status = jsonObj.get("trade_status").toString();
							}
						}
					}
				} catch (Exception e) {
					logger.error("{} 调用户新浪代收接口出错:{}", repayLogPrefix, e.getMessage());
				}

				// 保存代收交易记录
				saveTradeRecord(uid, interestBg.toString(), outTradeNo, tenderCfg.getId(), "collect", "", result, status);
			}

			// 代收本金
			BigDecimal principalBg = repayDetail.getPrincipalPayable();
			if (principalBg.compareTo(BigDecimal.ZERO) > 0) {

				// 其他请求参数不变，替换掉金额、单号
				params.put("amount", principalBg.toString());
				String outTradeNo = BillNoUtils.GenerateBillNo();
				params.put("outTradeNo", outTradeNo);

				String status = "FAILED";
				String result = "";
				try {
					result = HttpClientUtils.doPost(url, params);
					ResponseEntity responseEntity = JSONObject.parseObject(result, ResponseEntity.class);
					if (responseEntity.getStatus().equals(SysStatus.OK)) {
						if (responseEntity.getData() != null) {
							JSONObject jsonObj = JSONObject.parseObject(responseEntity.getData().toString());
							if (jsonObj.get("trade_status") != null) {
								status = jsonObj.get("trade_status").toString();
							}
						}
					}
				} catch (Exception e) {
					logger.error("{} 调用户新浪代收接口出错:{}", repayLogPrefix, e.getMessage());
				}

				// 保存代收交易记录
				saveTradeRecord(uid, principalBg.toString(), outTradeNo, tenderCfg.getId(), "collect", "", result, status);
			}

		} else {
			logger.error("{}代收新浪地址为空:{}", repayLogPrefix, url);
			throw new BusinessException("代收新浪地址为空!");
		}
	}
	
	/**
	 * 代付前的校验
	 * 校验用户的钱是否已经代收成功、成功后再进行批量代付
	 * 
	 * @param tenderCfg
	 */
	private boolean BeforeBatchPayCheck(FinanceTenderCfg tenderCfg) {

		FinanceTrade findParams = new FinanceTrade();
		findParams.setTenderId(tenderCfg.getId());
		findParams.setUid(tenderCfg.getUid());
		findParams.setTradeType("collect");
		List<FinanceTrade> tradeRecordList = tradeService.getByObj(findParams);
		for (FinanceTrade tradeRecord : tradeRecordList) {

			logger.debug("{}代收状态{},{}！", repayLogPrefix, tradeRecord.getOrderNo(), tradeRecord.getStatus());

			String status = tradeRecord.getStatus();
			if (status.equals("FAILED") || status.equals("TRADE_FAILED")) {
				return false;
			}
		}
		return true;
	}
	
	
	
	/**
	 * 将钱代付到投资人账户
	 * @param repayPlanDetailList 还款计划明细列表
	 * @param tenderCfg 标的信息
	 */
	private void batchPayTradeProcess(List<FinanceRepayPlanDetail> repayPlanDetailList, FinanceTenderCfg tenderCfg) {

		String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATE_BATCH_HOSTING_PAYTRADE);
		if (!StringUtils.isEmpty(url)) {

			Map<String, String> orderNoMap = new HashMap<String, String>();

			// 代付利息明细参数
			List<Map<String, String>> tradeList = new ArrayList<Map<String, String>>();
			for (FinanceRepayPlanDetail repayPlanDetail : repayPlanDetailList) {

				BigDecimal interestBg = repayPlanDetail.getInterestPayable();
				if (interestBg.compareTo(BigDecimal.ZERO) > 0) {

					Map<String, String> interestDetailMap = new HashMap<String, String>();
					interestDetailMap.put("amount", interestBg.toString());
					interestDetailMap.put("fee", "0");
					interestDetailMap.put("payeeAccountType", "SAVING_POT");
					interestDetailMap.put("payeeIdentityId", repayPlanDetail.getUid());
					String outTradeNo = BillNoUtils.GenerateBillNo();
					interestDetailMap.put("outTradeNo", outTradeNo);
					interestDetailMap.put("summary", TradeCodeConstant.TRADE_PAY_EARNINGS_SUMMARY);
					tradeList.add(interestDetailMap);

					// 记录单号
					orderNoMap.put(repayPlanDetail.getId() + "LX", outTradeNo);
				}

				BigDecimal principalBg = repayPlanDetail.getPrincipalPayable();
				if (principalBg.compareTo(BigDecimal.ZERO) > 0) {

					Map<String, String> principalDetailMap = new HashMap<String, String>();
					principalDetailMap.put("amount", principalBg.toString());
					principalDetailMap.put("fee", "0");
					principalDetailMap.put("payeeAccountType", "SAVING_POT");
					principalDetailMap.put("payeeIdentityId", repayPlanDetail.getUid());
					String outTradeNo = BillNoUtils.GenerateBillNo();
					principalDetailMap.put("outTradeNo", outTradeNo);
					principalDetailMap.put("summary", TradeCodeConstant.TRADE_PAY_PRINCIPAL_SUMMARY);
					tradeList.add(principalDetailMap);

					// 记录单号
					orderNoMap.put(repayPlanDetail.getId() + "BJ", outTradeNo);
				}
			}

			if (tradeList.size() > 0) {
				// 批量代付请求参数
				Map<String, Object> map = new HashMap<String, Object>();
				String orderNo = BillNoUtils.GenerateBillNo();
				map.put("outPayNo", orderNo);
				map.put("outTradeCode", TradeCodeConstant.TRADE_CODE_PAY_PRINCIPAL_EARNINGS);
				map.put("tradeList", tradeList);

				Map<String, String> params = new HashMap<String, String>();
				params.put("batchRequest", JSONObject.toJSONString(map));

				String responseCode = "";
				String status = "";
				String result = "";
				try {
					logger.debug("{} 批量代付请求:{}", repayLogPrefix, params);
					result = HttpClientUtils.doPost(url, params);
					logger.debug("{} 批量代付结果:{}", repayLogPrefix, result);
					ResponseEntity responseEntity = JSONObject.parseObject(result, ResponseEntity.class);
					if (responseEntity.getStatus().equals(SysStatus.OK)) {
						if (responseEntity.getData() != null) {
							JSONObject jsonObj = JSONObject.parseObject(responseEntity.getData().toString());
							responseCode = jsonObj.get("response_code").toString();
							if (responseCode.equals("APPLY_SUCCESS")) {
								status = "PROCESSING";
							} else {
								status = "FAILED";
							}
						}
					}
				} catch (Exception e) {
					logger.error("{} 调用户新浪代付接口出错:{}", repayLogPrefix, e.getMessage());
				}

				// 保存批量交易记录
				for (FinanceRepayPlanDetail planDetail : repayPlanDetailList) {

					String uid = planDetail.getUid();
					String amount = "";
					String businessType = "";

					// 批量还款外部订单号
					planDetail.setRefundOrderNo(orderNo);

					BigDecimal interestBg = planDetail.getInterestPayable();
					if (interestBg.compareTo(BigDecimal.ZERO) > 0) {
						amount = planDetail.getInterestPayable().toString();
						businessType = BusinessType.BACK_INTEREST.toString();
						// 回息交易记录
						saveTradeRecord(uid, amount, orderNoMap.get(planDetail.getId() + "LX"), tenderCfg.getId(), "batchpay", businessType, result, status);
					}

					BigDecimal principalBg = planDetail.getPrincipalPayable();
					if (principalBg.compareTo(BigDecimal.ZERO) > 0) {
						amount = planDetail.getPrincipalPayable().toString();
						businessType = BusinessType.BACK_AMOUNT.toString();
						// 回本交易记录
						saveTradeRecord(uid, amount, orderNoMap.get(planDetail.getId() + "BJ"), tenderCfg.getId(), "batchpay", businessType, result, status);
					}
					
					// 批量代付成功后发送消息
					if (responseCode.equals("APPLY_SUCCESS")) {
						sendMessageCenter(planDetail, tenderCfg);
					}
				}

				// 在这里判断，保证上面成功失败的交易记录都把存下来
				if (!responseCode.equals("APPLY_SUCCESS")) {
					throw new BusinessException("批量代付失败!");
				}

			} else {
				logger.error("{} 无还款明细:{}", repayLogPrefix);
			}

		} else {
			logger.error("{} 批量代付新浪地址为空:{}", repayLogPrefix, url);
		}
	}
	
	/**
	 * 保存交易记录
	 */
	private void saveTradeRecord(String uid, String amount, String orderNo, String tenderId, String tradeType, String businessType, String response, String status) {

		FinanceTrade tradeRecord = new FinanceTrade();
		tradeRecord.setUid(uid);
		tradeRecord.setAmount(new BigDecimal(amount));
		tradeRecord.setOrderNo(orderNo);
		tradeRecord.setTenderId(tenderId);
		tradeRecord.setTradeType(tradeType);
		tradeRecord.setBusinessType(businessType);
		tradeRecord.setResponse(response);
		tradeRecord.setStatus(status);
		tradeRecord.setCreateUser("自动还款");
		tradeRecord.setUpdateUser("自动还款");
		tradeService.insert(tradeRecord);
	}
	
	/**
	 * 更新投标记录、还款计划信息 
	 * 1、投标记录表
	 * 2、还款计划表
	 * 3、还款计划明细表
	 */
	private void updateSubmitTenderInfo(List<FinanceRepayPlanDetail> repayPlanDetailList, FinanceTenderCfg tenderCfg) {

		for (FinanceRepayPlanDetail repayDetail : repayPlanDetailList) {

			// 注: 一个时间点内只会有一条明细 此时： 还款计划 、还款计划明细、投标记录 为 1:1:1
			FinanceSubmitTender submitTender = submitTenderService.getById(repayDetail.getSid());
			if (submitTender != null) {

				submitTender.setEarnedIncome(submitTender.getEarnedIncome().add(repayDetail.getInterestPayable()));

				// 下一笔收益
				if (tenderCfg.getTimeLimit().equals(repayDetail.getPeriod())) {
					submitTender.setNextIncome(BigDecimal.ZERO);
				} else {
					submitTender.setNextIncome(repayDetail.getInterestPayable());
				}
				submitTenderService.update(submitTender);
			}

			// 更新还款计划表
			String repayPlanId = repayDetail.getRpid();
			FinanceRepayPlan financeRepayPlan = financeRepayPlanService.getById(repayPlanId);
			if (financeRepayPlan != null) {
				if (tenderCfg.getTimeLimit().equals(repayDetail.getPeriod())) {
					financeRepayPlan.setStatus("4");
				} else {
					financeRepayPlan.setStatus(String.valueOf(repayDetail.getPeriod()));
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(repayDetail.getRefundTime());
					calendar.add(Calendar.MONTH, 1);
					financeRepayPlan.setNextRefund(calendar.getTime());
				}
				financeRepayPlanService.update(financeRepayPlan);
			}

			// 更新还款计划明细表
			repayDetail.setStatus(1);
			repayDetail.setRefundSuccessTime(new Date());
			repayDetail.setInterestActual(repayDetail.getInterestPayable());
			repayDetail.setPrincipalActual(repayDetail.getPrincipalPayable());
			repayPlanDetailService.update(repayDetail);
		}
	}
	
	/**
	 * 发送消息中心
	 * Detail: 发送回本、回息APP消息及短信
	 * 
	 * @param repayPlanDetailList
	 */
	private void sendMessageCenter(FinanceRepayPlanDetail repayPlanDetail, FinanceTenderCfg tenderCfg) {

		String uid = repayPlanDetail.getUid();

		// uid转换
		uid = uidMappingService.getYYDUid(uid);

		Map<String, Object> msgMap = new HashMap<>();
		msgMap.put("#tenderName#", tenderCfg.getTenderName());
		msgMap.put("#tenderNo#", tenderCfg.getTenderNo());

		String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);
		BigDecimal interestBg = repayPlanDetail.getInterestPayable();
		BigDecimal principalBg = repayPlanDetail.getPrincipalPayable();
		// 还利息
		if (interestBg.compareTo(BigDecimal.ZERO) > 0 && principalBg.compareTo(BigDecimal.ZERO) == 0) {
			msgMap.put("#incomeAmount#", interestBg.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_INTEREST, uid, msgMap);
		}

		// 标的到期,还利息和本金
		if (interestBg.compareTo(BigDecimal.ZERO) > 0 && principalBg.compareTo(BigDecimal.ZERO) > 0) {
			msgMap.put("#amount#", principalBg.add(interestBg).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_PRINCIPAL, uid, msgMap);
			messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_EXPIRED_SMS, uid, msgMap);
		}
	}
}
