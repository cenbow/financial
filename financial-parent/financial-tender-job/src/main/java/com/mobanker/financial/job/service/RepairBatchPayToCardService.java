package com.mobanker.financial.job.service;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.financial.entity.FinanceWithdrawCash;
import com.mobanker.financial.service.FinanceInviteTenderEndService;
import com.mobanker.financial.service.FinanceWithdrawCashService;

/**
 * 批量代付到卡修复
 * 
 * @author yinyafei
 * @date 2015.12.09
 */
@Service
public class RepairBatchPayToCardService {

	private static final Logger logger = LoggerFactory.getLogger(RepairFailedTradeService.class);
	private final String logPrefix = "[代付到卡修复]------";

	@Resource
	private FinanceWithdrawCashService financeWithdrawCashService;
	@Resource
	private FinanceInviteTenderEndService financeInviteTenderEndService;
	@Resource
	private BatchPayToCardTradeService batchPayToCardTradeService;

	/**
	 * 批量代付到卡失败修复
	 */
	public void doRepair() {

		FinanceWithdrawCash findParams = new FinanceWithdrawCash();
		findParams.setWithdrawStatus("FAILED");

		List<FinanceWithdrawCash> withdrawList = financeWithdrawCashService.getByObj(findParams);
		for (FinanceWithdrawCash financeWithdrawCash : withdrawList) {

			// 针对当天的数据进行修复
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(financeWithdrawCash.getCreateTime());
			Calendar cal2 = Calendar.getInstance();
			String date1 = DateUtils.convert(cal1.getTime(),DateUtils.DATE_FORMAT);
			String date2 = DateUtils.convert(cal2.getTime(),DateUtils.DATE_FORMAT);
			if (date1.equals(date2)) {

				logger.debug("{}修复数据:{}", logPrefix, JSONObject.toJSONString(financeWithdrawCash));

				String sfId = financeWithdrawCash.getSfid();
				FinanceInviteTenderEnd tenderEnd = financeInviteTenderEndService.getBySelfId(sfId);
				if (tenderEnd != null) {
					String tenderId = tenderEnd.getSid();
					batchPayToCardTradeService.batchPayToCardTradeById(tenderId);
				}
			}
		}
	}
}
