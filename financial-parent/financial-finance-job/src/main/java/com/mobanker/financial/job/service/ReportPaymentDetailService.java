package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceFinancingUser;
import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceReportPay;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.service.FinanceFinancingUserService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceReportPayService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;


/**
 * 实付明细定时任务
 * 
 * @author yinyafei
 * @date 2015.8.8
 *
 */
@Service
public class ReportPaymentDetailService {

	private static final Logger logger = LoggerFactory.getLogger(ReportPaymentDetailService.class);
	private final String logPrefix = "[实付明细]------";

	@Resource
	private FinanceRepayPlanDetailService financeRepayPlanServiceDetail;
	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceInvestUserService financeInvestUserService;
	@Resource
	private FinanceFinancingUserService financeFinancingUserService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceReportPayService financeReportPayService;

	public void actualPayDetail(String date) {

		deleteReportByDate(date);
		
		List<FinanceRepayPlanDetail> repayDetailList = financeRepayPlanServiceDetail.getRepayDetailListBySuccessDate(date);
		logger.debug("{}还款数：{}", logPrefix, repayDetailList.size());
		for (FinanceRepayPlanDetail repayPlanDetail : repayDetailList) {
			String tenderId = repayPlanDetail.getTid();
			String submitTenderId = repayPlanDetail.getSid();
			String uid = repayPlanDetail.getUid(); // 理财人id

			FinanceReportPay financeReportPay = new FinanceReportPay();

			FinanceInvestUser findParams = new FinanceInvestUser();
			findParams.setUid(uid);
			List<FinanceInvestUser> investorList = financeInvestUserService.getByObj(findParams);
			if (investorList.size() > 0) {
				String investorName = investorList.get(0).getName();
				financeReportPay.setInvestorName(investorName); // entity.投资人姓名
				String phone = investorList.get(0).getPhone();
				financeReportPay.setPhone(phone);
			}

			financeReportPay.setActualDate(repayPlanDetail.getRefundSuccessTime());
			FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(tenderId);
			if (tenderCfg != null) {
				financeReportPay.setTenderNo(tenderCfg.getTenderNo()); // entity.标的号
				financeReportPay.setTimeLimit(tenderCfg.getTimeLimit());
				financeReportPay.setFinanceBegin(tenderCfg.getBeginTime()); // entity.理财开始
				financeReportPay.setFinanceEnd(tenderCfg.getEndTime()); // entity.理财结束
				BigDecimal basicYield = tenderCfg.getYield();
				financeReportPay.setBasicYield(basicYield); // entity.基础利率

				String lcUid = tenderCfg.getUid(); // 理财人uid

				FinanceFinancingUser findFinancingParam = new FinanceFinancingUser();
				findFinancingParam.setUid(lcUid);
				List<FinanceFinancingUser> financingUserList = financeFinancingUserService.getByObj(findFinancingParam);
				if (financingUserList.size() > 0) {
					String borrowerName = financingUserList.get(0).getName();
					financeReportPay.setBorrowerName(borrowerName); // entity.借款人
				}

				financeReportPay.setOrderType("新浪支付");

				FinanceSubmitTender submitTender = financeSubmitTenderService.getById(submitTenderId);
				if (submitTender != null) {
					financeReportPay.setAmount(submitTender.getAmount()); // entity.理财金额

					BigDecimal finalYield = submitTender.getFinalYield();
					financeReportPay.setTotalYield(finalYield);
					financeReportPay.setChangeYield(finalYield.subtract(basicYield)); // entity.变动利率
				}

				int period = repayPlanDetail.getPeriod().intValue();
				if (period == 1) {
					financeReportPay.setOnePlanRefundDate(DateUtils.convert(date)); // entity.1月期实际还款日
					financeReportPay.setOnePrincipal(repayPlanDetail.getPrincipalActual()); // entity.1月期本金
					financeReportPay.setOneInterest(repayPlanDetail.getInterestActual()); // entity.1月期利息

					financeReportPay.setTwoInterest(BigDecimal.ZERO);   // entity.2月期实际还款日
					financeReportPay.setTwoPrincipal(BigDecimal.ZERO);  // entity.2月期利息
					financeReportPay.setThreeInterest(BigDecimal.ZERO); // entity.3月期实际还款日
					financeReportPay.setThreePrincipal(BigDecimal.ZERO);// entity.3月期利息

				} else if (period == 2) {
					financeReportPay.setTwoPlanRefundDate(DateUtils.convert(date)); // entity.2月期本金
					financeReportPay.setTwoInterest(repayPlanDetail.getInterestActual()); // entity.2月期实际还款日
					financeReportPay.setTwoPrincipal(repayPlanDetail.getPrincipalActual()); // entity.2月期利息

					financeReportPay.setOnePrincipal(BigDecimal.ZERO);  // entity.1月期本金
					financeReportPay.setOneInterest(BigDecimal.ZERO);   // entity.1月期利息
					financeReportPay.setThreeInterest(BigDecimal.ZERO); // entity.3月期实际还款日
					financeReportPay.setThreePrincipal(BigDecimal.ZERO);// entity.3月期利息
				} else if (period == 3) {
					financeReportPay.setThreePlanRefundDate(DateUtils.convert(date)); // entity.3月期本金
					financeReportPay.setThreeInterest(repayPlanDetail.getInterestActual()); // entity.3月期实际还款日
					financeReportPay.setThreePrincipal(repayPlanDetail.getPrincipalActual()); // entity.3月期利息

					financeReportPay.setOnePrincipal(BigDecimal.ZERO); // entity.1月期本金
					financeReportPay.setOneInterest(BigDecimal.ZERO);  // entity.1月期利息
					financeReportPay.setTwoInterest(BigDecimal.ZERO);  // entity.2月期实际还款日
					financeReportPay.setTwoPrincipal(BigDecimal.ZERO); // entity.2月期利息
				}
			}
			financeReportPayService.insert(financeReportPay);
		}
	}
	
	private void deleteReportByDate(String date) {

		List<FinanceReportPay> reportPayList = financeReportPayService.getFinanceReportPayByAcutalDate(date);
		for (FinanceReportPay reportPay : reportPayList) {
			financeReportPayService.deleteById(reportPay.getId());
		}
	}
}
