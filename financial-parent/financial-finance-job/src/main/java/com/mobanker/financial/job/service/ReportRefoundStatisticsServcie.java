package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.common.constants.SinaConstants.SinaRate;
import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.financial.entity.FinanceRefundStatistics;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.service.FinanceBankCardService;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceRefundStatisticsService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceTenderCfgService;

/**
 * Description: 还款统计
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Service
public class ReportRefoundStatisticsServcie {

	private final static Logger logger = LoggerFactory.getLogger(ReportRefoundStatisticsServcie.class);
	private final String logPrefix = "[还款统计]------";

	@Resource
	private FinanceRepayPlanDetailService financeRepayPlanDetailService;
	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceRefundStatisticsService financeRefundStatisticsService;
	@Resource
	private FinanceBankCardService financeBankCardService;
	@Resource
	private FinanceCommonCfgService commonCfgService;

	/**
	 * 定时任务批量生成还款统计
	 */
	public void refoundStatistics() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 29);
		Date refundTime = cal.getTime();

		String date = DateUtils.convert(refundTime, DateUtils.DATE_FORMAT);
		deleteDirtyData(date);

		Map<String, FinanceRefundStatistics> repayMap = new ConcurrentHashMap<String, FinanceRefundStatistics>();
		logger.debug("{}执行时间:{}", logPrefix, new Date());
		List<FinanceRepayPlanDetail> repayDetailList = financeRepayPlanDetailService.getRepayDetailListByDate(date);
		logger.debug("{}获取还款数:{}", logPrefix, repayDetailList.size());
		for (FinanceRepayPlanDetail repayPlanDetail : repayDetailList) {
			// 还款统计处
			refoundStatisticsProcess(repayPlanDetail, repayMap);
		}

		for (FinanceRefundStatistics value : repayMap.values()) {
			financeRefundStatisticsService.insert(value);
		}
	}

	/**
	 * 指定日期生成还款统计
	 * 
	 * @param date
	 */
	public void refoundStatisticsByDate(String date) {
		logger.debug("{}执行时间:{}", logPrefix, date);
		Map<String, FinanceRefundStatistics> repayMap = new HashMap<String, FinanceRefundStatistics>();

		deleteDirtyData(date);

		List<FinanceRepayPlanDetail> repayDetailList = financeRepayPlanDetailService.getRepayDetailListByDate(date);
		for (FinanceRepayPlanDetail repayPlanDetail : repayDetailList) {
			// 还款统计处经理
			refoundStatisticsProcess(repayPlanDetail, repayMap);
		}

		for (FinanceRefundStatistics value : repayMap.values()) {
			financeRefundStatisticsService.insert(value);
		}
	}

	/**
	 * 避免生成重复统计数据
	 * 
	 * @param date
	 */
	private void deleteDirtyData(String date) {
		// 避免垃圾数据,删除数据
		FinanceRefundStatistics findParams = new FinanceRefundStatistics();
		findParams.setPlanRefundTime(DateUtils.convert(date, DateUtils.DATE_FORMAT));
		List<FinanceRefundStatistics> refundStatisticsList = financeRefundStatisticsService.getByObj(findParams);
		for (FinanceRefundStatistics value : refundStatisticsList) {

			logger.debug("{}删除的数据:{}", logPrefix, JSONObject.toJSONString(value));
			financeRefundStatisticsService.deleteById(value.getId());
		}
	}

	/**
	 * 还款统计处理
	 * 
	 * @param repayPlanDetail
	 * @param repayMap
	 */
	private void refoundStatisticsProcess(FinanceRepayPlanDetail repayPlanDetail, Map<String, FinanceRefundStatistics> repayMap) {

		Date refoundTime = repayPlanDetail.getRefundTime();
		BigDecimal principal = repayPlanDetail.getPrincipalPayable();
		BigDecimal interest = repayPlanDetail.getInterestPayable();

		String tenderId = repayPlanDetail.getTid(); // 标的id
		FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
		if (tenderCfg != null) {

			String lcUid = tenderCfg.getUid(); // 理财人id
			if (!repayMap.containsKey(lcUid)) {
				FinanceRefundStatistics statistics = new FinanceRefundStatistics();
				UUID uuid = UUID.randomUUID();
				String id = uuid.toString().replace("-", "");
				statistics.setId(id);
				statistics.setPlanRefundTime(refoundTime);
				statistics.setFinancialUser(lcUid);
				statistics.setPrincipal(principal); // 应付本金
				statistics.setInterest(interest); // 应付利息
				statistics.setTotalAmount(principal.add(interest)); // 应付本息和
				statistics.setBankFee(BigDecimal.ZERO); // 银行手续费
				statistics.setBankCardNo(getBandCardNo(lcUid));
				String rechargeRate = commonCfgService.getCommonCfgValueByCode(SinaRate.RECHARGE_RATE);
				if (StringUtils.isEmpty(rechargeRate)) {
					rechargeRate = "0.003";
				}
				BigDecimal cardSinaFeeBg = statistics.getTotalAmount().multiply(new BigDecimal(rechargeRate));
				cardSinaFeeBg = cardSinaFeeBg.setScale(2, BigDecimal.ROUND_HALF_UP);
				statistics.setCardSinaFee(cardSinaFeeBg); // 卡新手续费
				statistics.setCheckStatus("0");
				statistics.setDocStatus("0");
				statistics.setBorrowerRechargeStatus("0");
				statistics.setBorrowerRechargeOrderNo("");
				statistics.setCreateUser("admin");
				statistics.setUpdateUser("admin");
				repayMap.put(lcUid, statistics);
			} else {
				FinanceRefundStatistics statistics = repayMap.get(lcUid);
				statistics.setPrincipal(statistics.getPrincipal().add(principal)); // 应付本金
				statistics.setInterest(statistics.getInterest().add(interest)); // 应付利息
				statistics.setTotalAmount(statistics.getTotalAmount().add(principal).add(interest)); // 应付本息和
				String rechargeRate = commonCfgService.getCommonCfgValueByCode(SinaRate.RECHARGE_RATE);
				if (StringUtils.isEmpty(rechargeRate)) {
					rechargeRate = "0.003";
				}
				BigDecimal cardSinaFeeBg = statistics.getTotalAmount().multiply(new BigDecimal(rechargeRate));
				cardSinaFeeBg = cardSinaFeeBg.setScale(2, BigDecimal.ROUND_HALF_UP);
				statistics.setCardSinaFee(cardSinaFeeBg); // 卡新手续费
				statistics.setCreateUser("admin");
				statistics.setUpdateUser("admin");
			}
		} else {
			logger.error("{}未找到标的:{}", logPrefix, tenderId);
		}
	}

	private String getBandCardNo(String uid) {
		// 获取用户银行卡号
		String bankCardNo = "";
		FinanceBankCard bankCard = financeBankCardService.getBankCardByUid(uid);
		if (bankCard != null) {
			bankCardNo = bankCard.getBankCard();
		}
		return bankCardNo;
	}
}
