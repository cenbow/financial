package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.Calendar;
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
import com.mobanker.financial.common.constants.SinaConstants.SinaUrl;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.common.enums.BusinessType;
import com.mobanker.financial.entity.FinanceFreeze;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceFreezeService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTradeService;
import com.mobanker.framework.dto.ResponseEntity;

/**
 * Desription : 对资金解冻、代收失败的进行重新调用
 * 
 * @author yinyafei 2015.8.17
 */
@Service
public class RepairFailedTradeService {

	private static final Logger logger = LoggerFactory.getLogger(RepairFailedTradeService.class);
	private final String logPrefix = "[交易失败修复]------";

	@Resource
	private FinanceFreezeService financeFreezeService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceTradeService financeTradeService;
	@Resource
	private FinanceCommonCfgService commonCfgService;

	public void doRepair(String date) {
		FinanceFreeze findParams = new FinanceFreeze();
		findParams.setOptType("2");
		List<FinanceFreeze> failedFreezeList = financeFreezeService.getByObj(findParams);

		for (FinanceFreeze record : failedFreezeList) {

			// 针对当天的数据进行修复
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(record.getCreateTime());
			Calendar cal2 = Calendar.getInstance();
			if (!StringUtils.isEmpty(date)) {
				cal2.setTime(DateUtils.convert(date, DateUtils.DATE_FORMAT));
			}
			
			String date1 = DateUtils.convert(cal1.getTime(),DateUtils.DATE_FORMAT);
			String date2 = DateUtils.convert(cal2.getTime(),DateUtils.DATE_FORMAT);
			
			if (date1.equals(date2)) {
				
				logger.debug("{}修复数据:{}", logPrefix, JSONObject.toJSONString(record));
				boolean flag = retryUnFreeze(record);
				
				logger.debug("{}解冻结果:{}", logPrefix, flag);
				if (flag) {
					retryCollect(record);
				}
			}
		}
	}

	/**
	 * 解冻失败修复
	 * 
	 * @param record
	 */
	public boolean retryUnFreeze(FinanceFreeze record) {

		// 解冻是否成功标志
		boolean flag = false;

		String submitId = record.getSubmitTenderId();
		FinanceSubmitTender submitTender = financeSubmitTenderService.getById(submitId);
		if (submitTender != null) {

			String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.BALANCE_UNFREEZE);
			Map<String, String> params = new HashMap<String, String>();
			String uid = submitTender.getUid();
			params.put("identityId", uid);
			params.put("summary", "投资解冻");
			String freezeOrderNo = record.getFreezeOrderNo();
			params.put("outFreezeNo", freezeOrderNo);
			String unfreezeNo = freezeOrderNo + "J";
			params.put("outUnfreezeNo", unfreezeNo);
			params.put("accountType", "SAVING_POT");
			try {

				logger.debug("{}解冻请求:{}", logPrefix, JSONObject.toJSONString(params));
				String result = HttpClientUtils.doPost(url, params);
				logger.debug("{}解冻结果:{}", logPrefix, result);
				ResponseEntity entity = JSONObject.parseObject(result, ResponseEntity.class);
				if (entity != null && entity.getStatus().equals("1")) {
					JSONObject jsonObj = JSONObject.parseObject(entity.getData().toString());
					String code = jsonObj.get("response_code").toString();
					if (code.equals("APPLY_SUCCESS")) {

						record.setOptType("1");
						record.setUnfreezeOrderNo(unfreezeNo);
						financeFreezeService.update(record);
						flag = true;
					}
				}
			} catch (Exception e) {
				logger.error("{}调用解冻出错:{}", logPrefix, e.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 代收修复处理
	 * 
	 * @param uid
	 * @param amount
	 * @param tenderId
	 * @return
	 */
	public void retryCollect(FinanceFreeze record) {

		String uid = record.getUid();
		String amount = record.getAmount().setScale(2, BigDecimal.ROUND_UP).toString();
		String tenderId = "";

		FinanceSubmitTender submitTender = financeSubmitTenderService.getById(record.getSubmitTenderId());
		if (submitTender != null) {
			tenderId = submitTender.getSid();
		}

		String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.CREATE_HOSTING_COLLECT_TRADE);
		Map<String, String> params = new HashMap<String, String>();
		params.put("payerId", uid);
		params.put("amount", amount);
		params.put("summary", "用户投资");
		params.put("balancePayType", "SAVING_POT");
		String orderNo = BillNoUtils.GenerateBillNo();
		params.put("outTradeNo", orderNo);
		params.put("outTradeCode", "1001");
		try {
			logger.debug("{}代收请求:{}", logPrefix, JSONObject.toJSONString(params));
			String result = HttpClientUtils.doPost(url, params);
			logger.debug("{}代收结果:{}", logPrefix, result);
			ResponseEntity entity = JSONObject.parseObject(result, ResponseEntity.class);
			String tradeStatus = "FAILED";
			if (entity != null && entity.getStatus().equals("1")) {
				JSONObject jsonObj = JSONObject.parseObject(entity.getData().toString());
				if (jsonObj.getString("trade_status") != null) {
					tradeStatus = jsonObj.getString("trade_status").toString();
				} else {
					logger.error("{}代收失败:{}", logPrefix, result);
				}
			}
			// 保存交易记录
			saveFinanceTrade(orderNo, uid, amount, "collect", tradeStatus, tenderId, result);

		} catch (Exception e) {
			logger.error("{}资金代收出错:{}", logPrefix, e.getMessage());
		}
	}

	/**
	 * 代收交易记录
	 */
	private void saveFinanceTrade(String orderNo, String uid, String amount, String tradeType, String status, String tenderId, String result) {
		FinanceTrade record = new FinanceTrade();
		record.setOrderNo(orderNo);
		record.setUid(uid);
		record.setAmount(new BigDecimal(amount));
		record.setTradeType(tradeType);
		record.setStatus(status);
		record.setBusinessType(BusinessType.INVEST.toString());
		record.setTenderId(tenderId);
		record.setResponse(result);
		financeTradeService.insert(record);
	}
}
