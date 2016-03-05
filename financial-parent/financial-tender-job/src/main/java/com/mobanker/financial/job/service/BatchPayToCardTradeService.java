package com.mobanker.financial.job.service;

import java.io.IOException;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.common.utils.HttpClientUtils;
import com.mobanker.financial.common.constants.FinanceConstants.TradeCodeConstant;
import com.mobanker.financial.common.constants.SinaConstants.SinaRate;
import com.mobanker.financial.common.constants.SinaConstants.SinaUrl;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.enums.TenderStatus;
import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.financial.entity.FinanceDepositWithdraw;
import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.entity.FinanceWithdrawCash;
import com.mobanker.financial.service.FinanceBankCardService;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceDepositWithdrawService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceInviteTenderEndService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.financial.service.FinanceTradeService;
import com.mobanker.financial.service.FinanceWarningService;
import com.mobanker.financial.service.FinanceWithdrawCashService;
import com.mobanker.framework.dto.ResponseEntity;

/**
 * 批量代付到卡
 * 
 * @author yinyafei
 * 
 * 需求变动：
 * 1、以理财人人为维度将将标的所有钱一次提出 ----------> 以标的为单位提出
 * 2、批量代付到卡前增加两个接口的调用--------------> 1、标的录入接口、2、代付接口
 */
@Service
public class BatchPayToCardTradeService {

	private static final Logger logger = LoggerFactory.getLogger(BatchPayToCardTradeService.class);
	private final String logPrefix = "[批量代付到卡]------";

	@Resource
	private FinanceBankCardService financeBankCardService;
	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceWithdrawCashService financeWithdrawCashService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceDepositWithdrawService financeDepositWithdrawService;
	@Resource
	private FinanceInviteTenderEndService financeInviteTenderEndService;
	@Resource
	private FinanceTradeService financeTradeRecordService;
	@Resource
	private IncomeCalculationService investorIncomeUpdateService;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private FinanceWarningService financeWarningService;
	@Resource
	private FinanceInvestUserService investUserService;

	/**
	 * 定时任务批量执行
	 */
	public void bathPayToCardTrade() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		Date date = DateUtils.getStartDatetime(cal.getTime());

		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setBeginTime(date);
		// 统计理财人下所有标的所有投资人,按理财人分组
		List<FinanceTenderCfg> tenderList = financeTenderCfgService.getByObj(findParams);
		logger.debug("{}批量代付到卡标的数:{}", logPrefix, tenderList.size());
		bathPayGroup(tenderList);
	}

	/**
	 * 指定标的执行
	 * 
	 * 支持传参数： tenderId、单个标的的代付到卡 uid ：理财人下的所有到期标的代付到卡
	 * 
	 * @param date
	 */
	public void batchPayToCardTradeById(String param) {

		FinanceTenderCfg findParams = new FinanceTenderCfg();
		findParams.setId(param);
		// 统计理财人下所有标的所有投资人,按理财人分组
		List<FinanceTenderCfg> tenderList = financeTenderCfgService.getByObj(findParams);
		if (tenderList.size() == 0) {
			findParams.setId(null);
			findParams.setUid(param.split(",")[0]);
			findParams.setFinishTime(DateUtils.convert(param.split(",")[1].concat(" 21:00:00")));
			tenderList = financeTenderCfgService.getByObj(findParams);
		}

		if (tenderList.size() > 0) {
			bathPayGroup(tenderList);
		}
	}

	/**
	 * 以理财人维度分组
	 * 
	 * @param date
	 */
	private void bathPayGroup(List<FinanceTenderCfg> tenderList) {

		Map<String, List<FinanceSubmitTender>> map = new HashMap<String, List<FinanceSubmitTender>>();
		Map<String, List<FinanceTenderCfg>> tenderMap = new HashMap<String, List<FinanceTenderCfg>>();

		for (FinanceTenderCfg tenderCfg : tenderList) {

			if (!tenderCfg.getStatus().equals(TenderStatus.TENDER_FULL.toString())) {
				logger.debug("{}标的状态不正确：{} {}", logPrefix, tenderCfg.getId(), tenderCfg.getTenderName());
				//continue;
			}
			if (tenderCfg.getInputAmount().compareTo(BigDecimal.ZERO) == 0) {
				logger.debug("{}无人投标不再代收：{} {}", logPrefix, tenderCfg.getId(), tenderCfg.getTenderName());
				continue;
			}
			String lcUid = tenderCfg.getUid();
			String tenderId = tenderCfg.getId();
			if (map.containsKey(lcUid)) {
				FinanceSubmitTender submitFindparams = new FinanceSubmitTender();
				submitFindparams.setSid(tenderId);
				List<FinanceSubmitTender> subList = financeSubmitTenderService.findByObj(submitFindparams);
				map.get(lcUid).addAll(subList);

				tenderMap.get(lcUid).add(tenderCfg);
			} else {
				FinanceSubmitTender submitFindparams = new FinanceSubmitTender();
				submitFindparams.setSid(tenderId);
				List<FinanceSubmitTender> subList = financeSubmitTenderService.findByObj(submitFindparams);
				map.put(lcUid, subList);

				List<FinanceTenderCfg> subTenderList = new ArrayList<FinanceTenderCfg>();
				subTenderList.add(tenderCfg);
				tenderMap.put(lcUid, subTenderList);
			}
		}

		for (String lcUid : map.keySet()) {
			List<FinanceSubmitTender> submitTenderList = map.get(lcUid);
			logger.debug("{}理财人：{} 投资用户数:{}", logPrefix, lcUid, submitTenderList.size());
			List<FinanceTenderCfg> tenderCfgList = tenderMap.get(lcUid);

			for (FinanceTenderCfg tenderCfg : tenderCfgList) {
				logger.debug("{}理财人：{} 收标:{}", logPrefix, lcUid, tenderCfg.getTenderName());
			}
			boolean flag = checkCollectionSuccess(tenderCfgList);
			if (!flag) {
				continue;
			}

			doBatchPayProcess(lcUid, submitTenderList);
		}
	}

	/**
	 * 校验标的下的钱是否都已付款成功
	 * 
	 * @param uid
	 */
	private boolean checkCollectionSuccess(List<FinanceTenderCfg> tenderCfgList) {

		boolean flag = true;

		for (FinanceTenderCfg tenderCfg : tenderCfgList) {

			String tenderId = tenderCfg.getId();

			FinanceTrade findRecord = new FinanceTrade();
			findRecord.setTenderId(tenderId);
			List<FinanceTrade> tradeRecordList = financeTradeRecordService.getByObj(findRecord);
			for (FinanceTrade tradeRecord : tradeRecordList) {
				String tradeInfo = tradeRecord.getId() + "," + tradeRecord.getStatus() + "," + tradeRecord.getAmount();
				logger.debug("{}资金代收状态：{}", logPrefix, tradeInfo);
				String status = tradeRecord.getStatus();
				if (status.equals("TRADE_FAILED") || status.equals("FAILED")) {
					flag = false;
					logger.error("{}该用户的钱未收到无法代付到卡:{}", logPrefix, tradeInfo);
					financeWarningService.sendWarning("批量代付到卡失败");
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 批量代付到卡
	 * 
	 * @param tenderCfg
	 */
	private void doBatchPayProcess(String lcUid, List<FinanceSubmitTender> submitTenderList) {

		String bindingCardId = "";
		String bankCardNo = "";
		String bankCardName = "";
		FinanceBankCard bankCard = financeBankCardService.getBankCardByUid(lcUid);
		if (bankCard != null) {
			bindingCardId = bankCard.getBindingCardId();
			bankCardNo = bankCard.getBankCard();
			bankCardName = bankCard.getBankName();
		}

		Map<String, BigDecimal> tenderIdAmountMap = new HashMap<String, BigDecimal>();
		Map<String, String> tenderIdInvestorMap = new HashMap<String, String>();
		for (FinanceSubmitTender submitTender : submitTenderList) {
			
			boolean isVip = investUserService.isVipInvestor(submitTender.getUid());
			if (isVip) {
				continue;
			}
			
			String tenderId = submitTender.getSid();
			if (tenderIdAmountMap.containsKey(tenderId)) {
				BigDecimal result = tenderIdAmountMap.get(tenderId).add(submitTender.getAmount());
				tenderIdAmountMap.put(tenderId, result);

				String invetorStrList = tenderIdInvestorMap.get(tenderId) + "$" + submitTender.getUid() + "^UID^" + submitTender.getAmount();
				tenderIdInvestorMap.put(tenderId, invetorStrList);
			} else {
				tenderIdAmountMap.put(tenderId, submitTender.getAmount());
				tenderIdInvestorMap.put(tenderId, submitTender.getUid() + "^UID^" + submitTender.getAmount());
			}
		}
		
		/*
		 * 2015.9.16 批量代付款到卡前先调用标的录入、代付
		 */
		for (String tenderId : tenderIdAmountMap.keySet()) {

			// -----------------------------------------标的录入---------------------------------------

			String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATE_P2P_HOSTINGBORROWINGTARGET);
			
			Map<String, String> params = new HashMap<String, String>();

			FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
			if (tenderCfg != null) {

				params.put("goodsId", tenderCfg.getTenderNo());
				params.put("goodsName", tenderCfg.getTenderName());
				params.put("annualYield", tenderCfg.getYield().toString());
				params.put("totalAmount", tenderCfg.getInputAmount().toString());
				String begineDate = DateUtils.convert(tenderCfg.getInviteTime(), DateUtils.DATE_TIMESTAMP_SHORT_FORMAT);
				params.put("beginDate", begineDate);
				String term = DateUtils.convert(tenderCfg.getEndTime(), DateUtils.DATE_TIMESTAMP_SHORT_FORMAT);
				params.put("term", term);

				params.put("debtorList", lcUid + "^UID^" + tenderCfg.getInputAmount().toString());
				params.put("investorList", tenderIdInvestorMap.get(tenderId));
				params.put("summary", "录入标的");

				String result = "";
				try {
					logger.debug("{}标的录入请求：{}", logPrefix, JSONObject.toJSONString(params));
					result = HttpClientUtils.doPost(url, params);
					logger.debug("{}标的录入结果：{}", logPrefix, result);
					ResponseEntity responseEntity = JSON.parseObject(result, ResponseEntity.class);
					if (responseEntity.getStatus().equals("1")) {
						String data = responseEntity.getData().toString();
						JSONObject jsonObject = JSONObject.parseObject(data);
						String code = jsonObject.get("response_code").toString();
						if (code.equals("APPLY_SUCCESS")) {
							//如果失败说明已经录入
						}
					}
				} catch (IOException e) {
					logger.error("{}代付：{}", logPrefix, e.getMessage());
				}
			}
		}	

		// 批量代付卡到请求参数
		StringBuffer paramSb = new StringBuffer();

		for (String tenderId : tenderIdAmountMap.keySet()) {

			FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
			if (tenderCfg != null) {

				String orderNo = BillNoUtils.GenerateBillNo();
				paramSb.append(orderNo).append("~");
				paramSb.append("binding_card^").append(lcUid);
				paramSb.append(",UID,").append(bindingCardId);
				paramSb.append("~").append(tenderIdAmountMap.get(tenderId));
				paramSb.append("~").append("批量代付到卡").append("~~").append(tenderCfg.getTenderNo());
				paramSb.append("$");

				saveWithdrawCash(tenderId, bankCardNo, tenderIdAmountMap.get(tenderId), "PROCESSING", lcUid, bankCardName, orderNo);
			}
		}
		String tradeList = paramSb.substring(0, paramSb.length() - 1);

		String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATEBATCH_HOSTINGPAYTOCARDTRADE);
		
		Map<String, String> params = new HashMap<String, String>();
		String orderNo = BillNoUtils.GenerateBillNo();
		params.put("outPayNo", orderNo);
		params.put("tradeList", tradeList);
		params.put("outTradeCode", TradeCodeConstant.TRADE_CODE_PAY_BORROWING);

		try {
			logger.debug("{}代付到卡请求：{}", logPrefix, JSONObject.toJSONString(params));
			String result = HttpClientUtils.doPost(url, params);
			logger.debug("{}代付到卡结果：{}", logPrefix, result);

			ResponseEntity responseEntity = JSON.parseObject(result, ResponseEntity.class);
			if (responseEntity.getStatus().equals("1")) {
				String data = responseEntity.getData().toString();
				JSONObject jsonObject = JSONObject.parseObject(data);
				String code = jsonObject.get("response_code").toString();
				if (!code.equals("APPLY_SUCCESS")) {
					logger.error("{}代付到卡失败：{}", logPrefix, result);
					financeWarningService.sendWarning("批量代付到卡失败");
				}
			} else {
				logger.error("{}代付到卡失败：{}", logPrefix, result);
				financeWarningService.sendWarning("批量代付到卡失败");
			}

		} catch (Exception e) {
			logger.error("{}代付到卡失败：{}", logPrefix, e.getMessage());
		}
	}

	/**
	 * 保存提现记录
	 * 
	 * @param tenderId
	 * @param orderNo
	 * @param bankCard
	 * @param amount
	 * @param status
	 * @param uid
	 * @param bankName
	 */
	private void saveWithdrawCash(String tenderId, String bankCardNo, BigDecimal amount, String status, String uid, String bankName, String orderNo) {
		FinanceWithdrawCash withdrawCash = new FinanceWithdrawCash();
		// 获取招标完成id
		String sfid = "";
		
		FinanceInviteTenderEnd tenderEnd = financeInviteTenderEndService.getTenderEndById(tenderId);
		if (tenderEnd != null) {
			sfid = tenderEnd.getId();
		}
		UUID uuid = UUID.randomUUID();
		String id = uuid.toString().replace("-", "");
		withdrawCash.setId(id);
		withdrawCash.setSfid(sfid);
		withdrawCash.setOrderNo(orderNo);
		withdrawCash.setWithdrawType("新浪支付");
		withdrawCash.setBankCard(bankCardNo);
		
		String withdrawRate = commonCfgService.getCommonCfgValueByCode(SinaRate.WITHDRAW_RATE);
		if (StringUtils.isEmpty(withdrawRate)) {
			withdrawRate = "1.5";
		}
		withdrawCash.setFee(new BigDecimal(withdrawRate));
		withdrawCash.setActualAmount(amount);
		withdrawCash.setWithdrawTime(new Date());
		withdrawCash.setWithdrawStatus(status);
		withdrawCash.setTransferFee(BigDecimal.ZERO);
		withdrawCash.setTransferStatus("0");
		withdrawCash.setDocMaker("");
		withdrawCash.setCreateUser("job");
		withdrawCash.setUpdateUser("job");
		withdrawCash.setCreateTime(new Date());
		withdrawCash.setUpdateTime(new Date());
		financeWithdrawCashService.insert(withdrawCash);

		FinanceDepositWithdraw record = new FinanceDepositWithdraw();
		record.setOrderNo(orderNo);
		record.setUid(uid);
		record.setAmount(amount);
		record.setStatus(status);
		record.setRecordType("withdraw");
		record.setUserFee(BigDecimal.ZERO);
		record.setMerchantFee(new BigDecimal(withdrawRate));
		record.setIsFinancial("1");
		record.setBankCard(bankCardNo);
		record.setBankName(bankName);
		record.setCreateUser("job");
		record.setUpdateUser("job");
		record.setCreateTime(new Date());
		record.setUpdateTime(new Date());
		financeDepositWithdrawService.insert(record);
		
		logger.debug("{}生成提现记录orderNo：{}", logPrefix, orderNo);
	}
	
	/**
	 * 保存代付交易记录
	 * @param tenderId
	 * @param orderNo
	 * @param uid
	 * @param amount
	 * @param status
	 * @param response
	 */
	public void saveTradeRecord(String tenderId, String orderNo, String uid, BigDecimal amount, String status, String response) {

		FinanceTrade tradeRecord = new FinanceTrade();
		tradeRecord.setOrderNo(orderNo);
		tradeRecord.setUid(uid);
		tradeRecord.setAmount(amount);
		tradeRecord.setStatus(status);
		tradeRecord.setTradeType("pay");
		tradeRecord.setResponse(response);
		tradeRecord.setCreateUser("job");
		tradeRecord.setUpdateUser("job");
		tradeRecord.setCreateTime(new Date());
		tradeRecord.setUpdateTime(new Date());
		financeTradeRecordService.insert(tradeRecord);
	}
}
