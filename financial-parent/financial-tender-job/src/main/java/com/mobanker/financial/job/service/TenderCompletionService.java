package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.common.utils.HttpClientUtils;
import com.mobanker.financial.common.constants.MessageContants.SendMessageCode;
import com.mobanker.financial.common.constants.MessageContants.TepmlateNid;
import com.mobanker.financial.common.constants.SinaConstants.SinaRate;
import com.mobanker.financial.common.constants.SinaConstants.SinaUrl;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.constants.SystemConstants.SysStatus;
import com.mobanker.financial.common.enums.BusinessType;
import com.mobanker.financial.common.enums.FreezeType;
import com.mobanker.financial.common.enums.TenderStatus;
import com.mobanker.financial.common.message.MessageCenter;
import com.mobanker.financial.entity.FinanceFreeze;
import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.financial.entity.FinancePatchTender;
import com.mobanker.financial.entity.FinanceRepayPlan;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.service.FinanceBankCardService;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceFreezeService;
import com.mobanker.financial.service.FinanceIncomeService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceInviteTenderEndService;
import com.mobanker.financial.service.FinancePatchTenderService;
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
 * Description: 招标完成
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Service
public class TenderCompletionService {

	private static final Logger logger = LoggerFactory.getLogger(TenderCompletionService.class);
	private final String logPrefix = "[招标完成]------";

	@Resource
	private FinanceTenderCfgService tenderCfgService;
	@Resource
	private FinanceSubmitTenderService submitTenderService;
	@Resource
	private FinanceInviteTenderEndService inviteTenderEndService;
	@Resource
	private FinanceRepayPlanService repayPlanService;
	@Resource
	private FinanceRepayPlanDetailService repayPlanServiceDetail;
	@Resource
	private FinanceInvestUserService investUserService;
	@Resource
	private FinanceRefundStatisticsService refundStatisticsService;
	@Resource
	private FinanceTradeService tradeService;
	@Resource
	private FinanceFreezeService financeFreezeService;
	@Resource
	private FinanceBankCardService bankCardService;
	@Resource
	private FinanceIncomeService incomeService;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private MessageCenter messageCenter;
	@Resource
	private FinanceWarningService financeWarningService;
	@Resource
	private FinancePatchTenderService patchTenderService;

	public List<FinanceTenderCfg> tenderComplete() {

		// 整点、半点执行以下招标结束
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		if (minute >= 30) {
			cal.set(Calendar.MINUTE, 30);
		} else {
			cal.set(Calendar.MINUTE, 0);
		}
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setFinishTime(cal.getTime());
		logger.debug("{}招标完成时间:{}", logPrefix, cal.getTime());
		List<FinanceTenderCfg> tenderList = tenderCfgService.getByObj(findParams);
		logger.debug("{}招标完成个数:{}", logPrefix, tenderList.size());

		for (FinanceTenderCfg tenderCfg : tenderList) {
			logger.debug("{}招标完成信息:{} {} 状态{}", logPrefix, tenderCfg.getId(), tenderCfg.getTenderName(), tenderCfg.getStatus());
		}

		for (FinanceTenderCfg tenderCfg : tenderList) {

			logger.debug("{}开始处理标的:{}", logPrefix, tenderCfg.getTenderName() + "-------------------------\r\n");

			tenderCompleteOne(tenderCfg);
		}
		// 为了执行AOP切面,获取返回参数
		return tenderList;
	}

	/**
	 * 根据标的ID完成招标
	 * @param tenderId
	 */
	public List<FinanceTenderCfg> tenderCompteteById(String tenderId) {

		List<FinanceTenderCfg> tenderList = new ArrayList<FinanceTenderCfg>();
		FinanceTenderCfg tenderCfg = tenderCfgService.getById(tenderId);
		if (tenderCfg != null) {
			tenderCompleteOne(tenderCfg);
			tenderList.add(tenderCfg);
		}
		return tenderList;
	}
	

	/**
	 * 手工执行招标完成
	 * 
	 * @param tenderId
	 */
	public void tenderCompleteOne(FinanceTenderCfg tenderCfg) {

		// 判断是否有人投标，若无人投标，不再解冻、代收、生产还款计划
		if (tenderCfg.getInputAmount().compareTo(BigDecimal.ZERO) == 0) {

			endZeroTender(tenderCfg);
			return;
		}

		if (tenderCfg.getStatus().equals(TenderStatus.TENDERING.toString()) || tenderCfg.getStatus().equals(TenderStatus.TENDER_FULL.toString())) {
			// 标的结束处理
			doinvestorTenderEndProcess(tenderCfg);
		} else {
			logger.debug("{}标的状态不正确:{} {}", logPrefix, tenderCfg.getTenderName(), tenderCfg.getTenderNo());
		}
	} 
	
	/**
	 * 流标处理
	 * @param tenderCfg
	 */
	private void endZeroTender(FinanceTenderCfg tenderCfg) {

		logger.debug("{}无人投标:{} {} 状态{}", logPrefix, tenderCfg.getId(), tenderCfg.getTenderName(), tenderCfg.getStatus());
		tenderCfg.setStatus(TenderStatus.TENDER_FULL.toString());
		tenderCfg.setFullTime(new Date());
		tenderCfg.setUpdateTime(new Date());
		tenderCfgService.update(tenderCfg);

		FinanceInviteTenderEnd tenderEnd = new FinanceInviteTenderEnd();

		UUID uuid = UUID.randomUUID();
		String id = uuid.toString().replace("-", "");
		tenderEnd.setId(id);
		tenderEnd.setSid(tenderCfg.getId());
		tenderEnd.setInputAmount(BigDecimal.ZERO);
		tenderEnd.setFinalPayamount(BigDecimal.ZERO);
		tenderEnd.setAnnualRate(BigDecimal.ZERO);
		inviteTenderEndService.insert(tenderEnd);
	}

	/**
	 * 标的结束处理
	 * 
	 * @param tenderCfg
	 */
	@SuppressWarnings("unchecked")
	public void doinvestorTenderEndProcess(FinanceTenderCfg tenderCfg) {

		// 1、解冻代收
		doCollentionProcess(tenderCfg);
		// 2、生成还款记录
		Map<String, Object> rtnMap = calculateTenderFee(tenderCfg);
		BigDecimal inputAmount = (BigDecimal) rtnMap.get("totalAmount");
		BigDecimal finalYield = (BigDecimal) rtnMap.get("finalYield");
		List<FinanceSubmitTender> submitTenderList = (List<FinanceSubmitTender>) rtnMap.get("submitTenderList");
		doRepayPlanProcess(tenderCfg, inputAmount.setScale(2, BigDecimal.ROUND_HALF_UP), finalYield, submitTenderList);
	}

	/**
	 * 资金解冻、代收
	 * 
	 * @param tenderCfg
	 */
	public void doCollentionProcess(FinanceTenderCfg tenderCfg) {

		String tenderId = tenderCfg.getId();

		try {
			FinanceSubmitTender submitTenderParams = new FinanceSubmitTender();
			submitTenderParams.setSid(tenderId);
			List<FinanceSubmitTender> submitTenderList = submitTenderService.getByObj(submitTenderParams);
			Map<String, String> params = new HashMap<String, String>();
			for (FinanceSubmitTender submitTender : submitTenderList) {

				boolean isVip = investUserService.isVipInvestor(submitTender.getUid());
				if (isVip || submitTender.getAmount().compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}

				// 获取资金冻结原纪录
				FinanceFreeze findParams = new FinanceFreeze();
				findParams.setSubmitTenderId(submitTender.getId());
				List<FinanceFreeze> freezeRecordList = financeFreezeService.findByObj(findParams);
				if (freezeRecordList.size() > 0) {

					FinanceFreeze freezeRecord = freezeRecordList.get(0);

					
					String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.BALANCE_UNFREEZE);
					params.put("identityId", submitTender.getUid());
					params.put("summary", "投资解冻");
					String freezeOrderNo = freezeRecord.getFreezeOrderNo();
					params.put("outFreezeNo", freezeOrderNo); // 原冻结单号
					String unfreezeNo = freezeOrderNo + "J";
					params.put("outUnfreezeNo", unfreezeNo);
					params.put("accountType", "SAVING_POT");
					logger.debug("{}解冻请求:{}", logPrefix, JSONObject.toJSONString(params));
					String result = HttpClientUtils.doPost(url, params);
					logger.debug("{}解冻结果:{}", logPrefix, result);
					ResponseEntity entity = JSONObject.parseObject(result, ResponseEntity.class);
					if (entity != null && entity.getStatus().equals(SysStatus.OK)) {
						JSONObject jsonObj = JSONObject.parseObject(entity.getData().toString());
						String code = jsonObj.get("response_code").toString();
						if (code.equals("APPLY_SUCCESS")) {
							freezeRecord.setUnfreezeOrderNo(unfreezeNo);
							freezeRecord.setOptType(FreezeType.FREEZE.toString()); // 0冻结 1解冻 2解冻失败
						} else {
							freezeRecord.setUnfreezeOrderNo(freezeOrderNo + "F");
							freezeRecord.setOptType(FreezeType.FAILED.toString());
							logger.error("{}解冻失败:{}", logPrefix, result);
							financeWarningService.sendWarning("解冻失败");
						}
					} else {
						freezeRecord.setUnfreezeOrderNo(freezeOrderNo + "F");
						freezeRecord.setOptType(FreezeType.FAILED.toString());
						logger.error("{}解冻失败:{}", logPrefix, result);
					}
					String unFreezeId = UUID.randomUUID().toString().replace("-", "");
					freezeRecord.setId(unFreezeId);
					freezeRecord.setCreateTime(new Date());
					freezeRecord.setUpdateTime(new Date());
					financeFreezeService.insert(freezeRecord);

					if (freezeRecord.getOptType().equals(FreezeType.FAILED.toString())) {
						// 解冻失败后，不再待收
						continue;
					}

				} else {
					logger.error("{}未找到冻结记录:{}", logPrefix, submitTender.getId());
					continue;
				}

				String uid = submitTender.getUid(); // 投资人id
				BigDecimal amount = submitTender.getAmount(); // 投资金额

				// 代收
				String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATE_HOSTING_COLLECT_TRADE);
				params.clear();
				params.put("payerId", uid);
				params.put("amount", amount.toString());
				params.put("summary", "用户投资");
				params.put("balancePayType", "SAVING_POT");
				params.put("outTradeCode", "1001");
				String orderNo = BillNoUtils.GenerateBillNo();
				params.put("outTradeNo", orderNo);
				logger.debug("{}代收请求:{}", logPrefix, JSONObject.toJSONString(params));
				String result = HttpClientUtils.doPost(url, params);
				logger.debug("{}代收结果:{}", logPrefix, result);
				ResponseEntity entity = JSONObject.parseObject(result, ResponseEntity.class);
				String tradeStatus = "FAILED";
				if (entity != null && entity.getStatus().equals(SysStatus.OK)) {
					JSONObject jsonObj = JSONObject.parseObject(entity.getData().toString());

					if (jsonObj.getString("trade_status") != null) {
						tradeStatus = jsonObj.getString("trade_status").toString();
						// 代收成功更新收益
						incomeUpdate(submitTender, tenderCfg.getTimeLimit());
						// 发送消息
						sendMsg(tenderCfg, submitTender, uid);
					} else {
						logger.error("{}代收失败:{}", logPrefix, result);
					}

				} else {
					logger.error("{}代收失败:{}", logPrefix, result);
				}
				// 交易记录
				saveFinanceTrade(orderNo, uid, amount, "collect", tradeStatus, submitTender.getSid(), result);
			}

		} catch (Exception e) {
			logger.error("{}招标完成失败:{}", logPrefix, e.getMessage());
		}
	}

	/**
	 * 批量代付后处理
	 * 
	 * @param tenderCfg
	 * @param inputAmount
	 * @param finalYield
	 * @param submitTenderList
	 */
	public void doRepayPlanProcess(FinanceTenderCfg tenderCfg, BigDecimal inputAmount, BigDecimal finalYield, List<FinanceSubmitTender> submitTenderList) {

		String tenderId = tenderCfg.getId();
		int timeLimit = tenderCfg.getTimeLimit();
		Date beginTime = tenderCfg.getBeginTime();
		BigDecimal baseYield = tenderCfg.getYield(); // 基准利率

		BigDecimal totalInterest = incomeService.calculateIncome(inputAmount, new BigDecimal(timeLimit), finalYield);
		BigDecimal finalPayamount = totalInterest.setScale(2, BigDecimal.ROUND_HALF_UP).add(inputAmount);
		tenderCfg.setFinalPayamount(finalPayamount);

		// 将标的状态更改为满标
		tenderCfg.setInputAmount(inputAmount); // 实际投入金额
		tenderCfg.setFinalPayamount(finalPayamount);
		tenderCfg.setStatus(TenderStatus.TENDER_FULL.toString());
		if (tenderCfg.getFullTime() == null) {
			tenderCfg.setFullTime(new Date());
		}
		tenderCfg.setUpdateTime(new Date());
		tenderCfgService.update(tenderCfg);
		logger.debug("{}九点标的结束:{}", logPrefix, tenderCfg.getTenderName());

		// 将结束标的记录在招标完成表
		FinanceInviteTenderEnd tenderEnd = new FinanceInviteTenderEnd();

		UUID uuid = UUID.randomUUID();
		String id = uuid.toString().replace("-", "");
		tenderEnd.setId(id);
		tenderEnd.setSid(tenderId);
		tenderEnd.setInputAmount(inputAmount);
		tenderEnd.setFinalPayamount(finalPayamount);
		tenderEnd.setAnnualRate(finalYield);
		inviteTenderEndService.insert(tenderEnd);

		// 还款计划
		FinanceRepayPlan financeRepayPlan = new FinanceRepayPlan();
		uuid = UUID.randomUUID();
		String repayPlanId = uuid.toString().replace("-", "");
		financeRepayPlan.setId(repayPlanId);
		financeRepayPlan.setSfid(id);
		// 下一还款日
		Calendar cal = Calendar.getInstance();
		cal.setTime(beginTime);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, -1);
		financeRepayPlan.setNextRefund(cal.getTime());
		if (timeLimit > 1) {
			financeRepayPlan.setPrincipal(BigDecimal.ZERO); // 应还本金
		} else {
			financeRepayPlan.setPrincipal(inputAmount); // 应还本金
		}

		BigDecimal interest = incomeService.calculateIncome(inputAmount, BigDecimal.ONE, finalYield);
		financeRepayPlan.setInterest(interest); // 应还利息
		financeRepayPlan.setStatus("0"); // 0:未还款 1:已还款
		financeRepayPlan.setDescription("还款计划");
		financeRepayPlan.setCreateUser("admin");
		financeRepayPlan.setUpdateUser("admin");
		repayPlanService.insert(financeRepayPlan);

		for (FinanceSubmitTender submitTender : submitTenderList) {

			boolean isVip = investUserService.isVipInvestor(submitTender.getUid());
			if (isVip) {
				continue;
			}

			BigDecimal investorYield = submitTender.getFinalYield(); // 投资人利率
			BigDecimal amount = submitTender.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
			String orderNo = submitTender.getOrderNo();
			BigDecimal investorInterest = incomeService.calculateIncome(amount, BigDecimal.ONE, investorYield); // 单个投资人的利息

			for (int i = 0; i < timeLimit; i++) {

				Calendar nextCal = Calendar.getInstance();
				nextCal.setTime(beginTime);
				nextCal.add(Calendar.MONTH, i + 1);
				nextCal.add(Calendar.DATE, -1);

				// 根据周期几条还款明细
				FinanceRepayPlanDetail financeRepayPlanDetail = new FinanceRepayPlanDetail();
				uuid = UUID.randomUUID();
				String repayPlanDetailId = uuid.toString().replace("-", "");
				financeRepayPlanDetail.setId(repayPlanDetailId);
				financeRepayPlanDetail.setRpid(repayPlanId);
				financeRepayPlanDetail.setChangeRate(investorYield.subtract(baseYield)); // 变动利率
				financeRepayPlanDetail.setInterestPayable(investorInterest); // 应付利息
				if (timeLimit == i + 1) {
					financeRepayPlanDetail.setPrincipalPayable(amount); // 应付本金
				} else {
					financeRepayPlanDetail.setPrincipalPayable(BigDecimal.ZERO); // 应付本金
				}
				BigDecimal refundAmount = financeRepayPlanDetail.getPrincipalPayable().add(financeRepayPlanDetail.getInterestPayable());
				financeRepayPlanDetail.setRefundAmount(refundAmount); // 还款金额
				financeRepayPlanDetail.setEarnings(new BigDecimal("0")); // 到期收益
				financeRepayPlanDetail.setFee(new BigDecimal("0")); // 手续费
				financeRepayPlanDetail.setStatus(0);
				financeRepayPlanDetail.setPeriod(i + 1); // 还款期次
				financeRepayPlanDetail.setDescription("还款明细");
				financeRepayPlanDetail.setRefundTime(nextCal.getTime()); // 还款日期
				financeRepayPlanDetail.setUid(submitTender.getUid());
				financeRepayPlanDetail.setTid(tenderId);
				financeRepayPlanDetail.setSid(submitTender.getId());
				financeRepayPlanDetail.setOrderNo(orderNo);
				financeRepayPlanDetail.setRefundOrderNo("");
				String rechargeRate = commonCfgService.getCommonCfgValueByCode(SinaRate.RECHARGE_RATE);
				if (StringUtils.isEmpty(rechargeRate)) {
					rechargeRate = "0.003";
				}
				BigDecimal cardSinaFeeBg = financeRepayPlanDetail.getInterestPayable().add(financeRepayPlanDetail.getPrincipalPayable()).multiply(new BigDecimal(rechargeRate));
				cardSinaFeeBg = cardSinaFeeBg.setScale(2, BigDecimal.ROUND_HALF_UP);
				financeRepayPlanDetail.setCardSinaFee(cardSinaFeeBg);
				financeRepayPlanDetail.setInterestActual(BigDecimal.ZERO);
				financeRepayPlanDetail.setPrincipalActual(BigDecimal.ZERO);
				financeRepayPlanDetail.setCreateUser("admin");
				financeRepayPlanDetail.setUpdateUser("admin");

				repayPlanServiceDetail.insert(financeRepayPlanDetail);
				logger.debug("{}生成还款计划明细:{}", logPrefix, submitTender.getUid());
			}
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param tenderCfg
	 * @param submitTender
	 * @param uid
	 */
	private void sendMsg(FinanceTenderCfg tenderCfg, FinanceSubmitTender submitTender, String uid) {
		// 发送投标成功消息
		Map<String, Object> replaceParam = new HashMap<String, Object>();
		replaceParam.put("#tenderName#", tenderCfg.getTenderName());
		replaceParam.put("#tenderNo#", tenderCfg.getTenderNo());
		replaceParam.put("#amount#", submitTender.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		replaceParam.put("#yield#", tenderCfg.getYield().toString());
		replaceParam.put("#timeLimit#", tenderCfg.getTimeLimit() * 30 + "天");
		replaceParam.put("#expireTime#", DateUtils.convert(tenderCfg.getEndTime(), DateUtils.DATE_FORMAT));
		replaceParam.put("#expectedEarnings#", submitTender.getExpectIncome().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		replaceParam.put("#orderNo#", submitTender.getOrderNo());
		String messageUrl = commonCfgService.getRequestUrl(RequestUrl.MESSAGE_URL, SendMessageCode.SEND_REMIND);
		uid = uidMappingService.getYYDUid(uid);
		messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_SUCCESS, uid, replaceParam);
		// 发送短信
		replaceParam.put("#interestTime#", DateUtils.convert(submitTender.getBearingTime(), DateUtils.DATE_FORMAT));
		messageCenter.sendRemind(messageUrl, TepmlateNid.TENDER_SUCCESS_SMS, uid, replaceParam);
	}

	/**
	 * 保存交易记录
	 * 
	 * @param orderNo
	 * @param uid
	 * @param amount
	 * @param tradeType
	 * @param status
	 */
	private void saveFinanceTrade(String orderNo, String uid, BigDecimal amount, String tradeType, String status, String tenderId, String result) {

		FinanceTrade record = new FinanceTrade();
		record.setOrderNo(orderNo);
		record.setUid(uid);
		record.setAmount(amount);
		record.setTradeType(tradeType);
		record.setStatus(status);
		record.setBusinessType(BusinessType.INVEST.toString()); // 投资
		record.setTenderId(tenderId);
		record.setResponse(result);
		record.setCreateTime(new Date());
		record.setUpdateTime(new Date());
		tradeService.insert(record);
	}

	/**
	 * 计算最终年利率
	 * 
	 * @param tenderCfg
	 * @return
	 */
	private Map<String, Object> calculateTenderFee(FinanceTenderCfg tenderCfg) {

		Map<String, Object> rtnMap = new HashMap<String, Object>();

		String tenderId = tenderCfg.getId();
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal totalInterest = BigDecimal.ZERO;

		FinanceSubmitTender submitTenderParams = new FinanceSubmitTender();
		submitTenderParams.setSid(tenderId);
		List<FinanceSubmitTender> submitTenderList = submitTenderService.getByObj(submitTenderParams);
		for (FinanceSubmitTender submitTender : submitTenderList) {
			boolean isVip = investUserService.isVipInvestor(submitTender.getUid());
			if (isVip || submitTender.getAmount().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}

			BigDecimal amount = submitTender.getAmount();
			totalAmount = totalAmount.add(amount);

			BigDecimal yield = submitTender.getFinalYield();
			totalInterest = totalInterest.add(amount.multiply(yield));
		}
		BigDecimal result = totalInterest.divide(totalAmount, 2);
		result.setScale(2, BigDecimal.ROUND_HALF_UP);

		rtnMap.put("totalAmount", totalAmount);
		rtnMap.put("finalYield", result);
		rtnMap.put("submitTenderList", submitTenderList);
		return rtnMap;
	}

	/**
	 * 代收后更新理财资产、余额
	 * 
	 * @param tenderCfg
	 */
	private void incomeUpdate(FinanceSubmitTender submitTender, Integer timeLimit) {

		String uid = submitTender.getUid();
		FinanceIncome income = incomeService.getFinanceIncomeByUid(uid);
		if (income != null) {
			BigDecimal assets = submitTender.getAmount();
			income.setFinancialAssets(income.getFinancialAssets().add(assets)); // 理财资产增加
			income.setSavingpotBalance(income.getSavingpotBalance().subtract(assets)); // 余额减少

			BigDecimal uncollectedRevenue = incomeService.calculateIncome(assets, new BigDecimal(timeLimit), submitTender.getFinalYield());
			income.setUncollectedRevenue(income.getUncollectedRevenue().add(uncollectedRevenue)); // 待收收益
			income.setAccumulatedIncome(income.getAccumulatedIncome().add(uncollectedRevenue)); // 累计收益

			incomeService.update(income);
		}
	}
	
	/**
	 * vip理财人补标
	 * @param map
	 */
	public void receiveVipSubmitTender(HashMap<String, String> map) {

		logger.debug("{}vip理财人补标:{}", logPrefix, map.toString());
		
		// 为了兼容新老字段userId---uid、needAmount--amount
		String userId = map.get("userId");
		if (userId == null) {
			userId = map.get("uid");
		}
		String tenderId = map.get("id");
		String amount = map.get("needAmount"); // 补标金额
		if (amount == null) {
			amount = map.get("amount");
		}
		
		String patchBidId = map.get("patchBidId");
		String parentPatchBidId = map.get("parentPatchBidId");
		
		if (!StringUtils.isEmpty(parentPatchBidId)) {
			FinancePatchTender parentTender = patchTenderService.getById(parentPatchBidId);
			if (parentTender != null) {
				parentTender.setDeleted("1");
				patchTenderService.update(parentTender);
			}
		}

		FinanceTenderCfg tenderCfg = tenderCfgService.getById(tenderId);
		if (tenderCfg != null) {

			FinancePatchTender patchTender = new FinancePatchTender();
			if (!StringUtils.isEmpty(patchBidId)) {
				patchTender.setId(patchBidId);
			}
			patchTender.setSid(tenderId);
			patchTender.setUid(userId);
			patchTender.setAmount(new BigDecimal(amount));
			patchTender.setCouponId("");
			patchTender.setFinalYield(BigDecimal.ZERO);
			patchTender.setAddTime(new Date());
			patchTender.setBearingTime(tenderCfg.getBeginTime());
			patchTender.setExpireTime(tenderCfg.getEndTime());
			patchTender.setEarnedIncome(BigDecimal.ZERO);
			patchTender.setExpectIncome(BigDecimal.ZERO);
			patchTender.setNextIncome(BigDecimal.ZERO);
			String orderNo = BillNoUtils.GenerateBillNo();
			patchTender.setOrderNo(orderNo);
			patchTenderService.insert(patchTender);

		} else {
			logger.error("{}vip理财人补标无标的{}", logPrefix, tenderId);
		}
	}
}
