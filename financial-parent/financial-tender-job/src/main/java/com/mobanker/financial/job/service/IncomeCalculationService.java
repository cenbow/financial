package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.HttpClientUtils;
import com.mobanker.financial.common.constants.SinaConstants.SinaUrl;
import com.mobanker.financial.common.constants.SystemConstants.RequestUrl;
import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.FinanceIncomeService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.framework.dto.ResponseEntity;
import com.mobanker.framework.mybatis.SqlInterceptor;

/**
 * Description: 更新收益
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Service
public class IncomeCalculationService {

	private final Logger logger = LoggerFactory.getLogger(IncomeCalculationService.class);
	private final String logPrefix = "[收益计算]------";

	@Resource
	private FinanceInvestUserService investUserService;
	@Resource
	private FinanceIncomeService incomeService;
	@Resource
	private FinanceSubmitTenderService submitTenderService;
	@Resource
	private FinanceTenderCfgService tenderCfgService;
	@Resource
	private FinanceRepayPlanDetailService repayPlanServiceDetail;
	@Resource
	private FinanceCommonCfgService commonCfgService;

	/**
	 * 定时任务收益计算
	 */
	public void incomeCalculate() {

		long beginTime = System.currentTimeMillis();
		int size = 3000;
		// 总用户数
		int totalCount = incomeService.count(new FinanceIncome());
		// 分几批次处理
		int batchCount = totalCount / size;
		if (totalCount % size > 0) {
			batchCount = batchCount + 1;
		}

		int start = 0;
		for (int i = 1; i <= batchCount; i++) {
			SqlInterceptor.setRowBounds(new RowBounds(start, size));
			List<FinanceIncome> incomeList = incomeService.getAvailableGreaterThanZeroIncome();
			logger.debug("{}用户收益更新数{}:", logPrefix, incomeList.size());

			for (FinanceIncome income : incomeList) { // 收益处理

				// 收益更新
				incomeUpdateProcess(income);
			}

			start = i * size;
		}

		long endTime = System.currentTimeMillis();
		long useTime = (endTime - beginTime) / 1000;
		logger.debug("{}用户收益刷新耗时:{}", logPrefix, useTime);
	}

	/**
	 * 单个用户收益更新
	 * 
	 * @param uid
	 */
	public void incomeCalculateById(String uid) {

		FinanceIncome financeIncome = incomeService.getFinanceIncomeByUid(uid);
		if (financeIncome != null) {
			// 收益处理
			incomeUpdateProcess(financeIncome);
		} else {
			logger.error("{}用户不存在:{}", logPrefix, uid);
		}
	}

	/**
	 * 收益更新处理
	 * 
	 * @param investor
	 */
	public void incomeUpdateProcess(FinanceIncome financeIncome) {

		String balance = "0"; // 余额
		String availableBalance = "0"; // 可用余额
		String savingPotBonus = "0"; // 存钱罐收益
		String yesterdayBonus = "0"; // 昨日收益
		String nearMonthBonus = "0"; // 近一月收益

		String uid = financeIncome.getUid();
		try {
			String url = commonCfgService.getRequestUrl(RequestUrl.PAY_URL, SinaUrl.QUERY_BALANCE);
			if (!StringUtils.isEmpty(url)) {

				Map<String, String> param = new HashMap<String, String>();
				param.put("identityId", uid);
				param.put("accountType", "SAVING_POT");

				String result = HttpClientUtils.doPost(url, param);
				ResponseEntity responseEntity = JSON.parseObject(result, ResponseEntity.class);
				if (responseEntity != null && responseEntity.getStatus().equals("1")) {
					if (!StringUtils.isEmpty(responseEntity.getData())) {
						String data = responseEntity.getData().toString();
						JSONObject jsonObject = JSONObject.parseObject(data);
						if (jsonObject.get("response_code").toString().equals("APPLY_SUCCESS")) {
							if (jsonObject.get("available_balance") != null) {
								availableBalance = jsonObject.get("available_balance").toString();
								balance = jsonObject.get("balance").toString();
								String[] bonusArray = jsonObject.get("bonus").toString().split("\\^");
								if (bonusArray.length > 1) {
									yesterdayBonus = bonusArray[0]; // 昨日收益
									nearMonthBonus = bonusArray[1]; // 近一月收益
									savingPotBonus = bonusArray[2]; // 存钱罐收益
								}
							}

							financeIncome.setSavingpotBalance(new BigDecimal(balance));
							financeIncome.setSavingpotAvailable(new BigDecimal(availableBalance));
							financeIncome.setSavingpotEarnings(new BigDecimal(savingPotBonus)); // 存钱罐收益
							financeIncome.setSavingpotMonthEarnings(new BigDecimal(nearMonthBonus)); // 近一月收益
							financeIncome.setSavingpotYesterdayEarnings(new BigDecimal(yesterdayBonus)); // 昨日收益

							Map<String, BigDecimal> incomeMap = computeReceivedBenefits(uid);
							BigDecimal receivedIncome = incomeMap.get("receivedIncome");
							BigDecimal unreceiveIncome = incomeMap.get("unreceiveIncome");
							BigDecimal financialAssets = incomeMap.get("financialAssets");

							// 更新已收收益、待收收益
							financeIncome.setReceivedBenefits(receivedIncome);
							financeIncome.setUncollectedRevenue(unreceiveIncome);

							// 累计收益 = 存钱罐收益 + 已收收益 + 代收收益
							BigDecimal accumulated = new BigDecimal(savingPotBonus).add(receivedIncome).add(unreceiveIncome);
							financeIncome.setAccumulatedIncome(accumulated); // 累计收益
							financeIncome.setFinancialAssets(financialAssets);
							financeIncome.setNetWorth(financeIncome.getSavingpotBalance().add(financeIncome.getFinancialAssets())); // 净资产
							financeIncome.setCreateUser("admin");
							financeIncome.setUpdateUser("admin");
							incomeService.update(financeIncome);
						}
					}
				} else {
					logger.debug("{}新浪余额接口失败:{}", logPrefix, result);
				}
			}
		} catch (Exception e) {
			logger.error("{}收益计算出错:{}" + logPrefix, e.getMessage());
		}
	}
	
	/**
	 * 根据还款记录明细计算各种收益情况
	 * @param uid
	 * @return
	 */
	private Map<String, BigDecimal> computeReceivedBenefits(String uid) {

		BigDecimal receivedIncome = BigDecimal.ZERO; // 已收收益
		BigDecimal unreceiveIncome = BigDecimal.ZERO; // 待收收益
		BigDecimal financialAssets = BigDecimal.ZERO; // 理财资产
		FinanceRepayPlanDetail findParams = new FinanceRepayPlanDetail();
		findParams.setUid(uid);
		List<FinanceRepayPlanDetail> planDetailList = repayPlanServiceDetail.getByObj(findParams);
		for (FinanceRepayPlanDetail planDetail : planDetailList) {
			if (planDetail.getStatus() == 1) { // 已还款
				receivedIncome = receivedIncome.add(planDetail.getInterestPayable());
			} else if (planDetail.getStatus() == 0) { // 未还款
				unreceiveIncome = unreceiveIncome.add(planDetail.getInterestPayable());
				financialAssets = financialAssets.add(planDetail.getPrincipalPayable());
			}
		}

		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		map.put("receivedIncome", receivedIncome);
		map.put("unreceiveIncome", unreceiveIncome);
		map.put("financialAssets", financialAssets);

		return map;
	}
}
