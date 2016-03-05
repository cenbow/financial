package com.mobanker.financial.job.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceFinancingUser;
import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceReportCollect;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.service.FinanceFinancingUserService;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceInviteTenderEndService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceReportCollectService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.financial.service.FinanceTradeService;

/**
 * 实收明细任务
 * 
 * @author yinyafei
 *
 */
@Service
public class ReportCollectionDetailService {

	private static final Logger logger = LoggerFactory.getLogger(ReportCollectionDetailService.class);
	private final String logPrefix = "[实收明细]------";

	@Resource
	private FinanceRepayPlanDetailService financeRepayPlanServiceDetail;
	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceFinancingUserService financeFinancingUserService;
	@Resource
	private FinanceInvestUserService financeInvestUserService;
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceReportCollectService financeReportCollectService;
	@Resource
	private FinanceTradeService financeTradeService;
	@Resource
	private FinanceInviteTenderEndService financeInviteTenderEndService;

	public void actualCollectionDetail(String date) {

		deleteReportByDate(date);
		
		List<FinanceTrade> tradeRecordList = financeTradeService.getCollectionListByDate(date);
		logger.debug("{}{} 提现数:{}", logPrefix, date, tradeRecordList.size());
		for (FinanceTrade record : tradeRecordList) {
			logger.debug("{}提现信息:{} {} {}", logPrefix, record.getTenderId(), record.getOrderNo(), record.getStatus());
		}

		Set<String> set = new HashSet<String>();
		for (FinanceTrade record : tradeRecordList) {

			// 以标的为维度
			String tenderId = record.getTenderId();
			if (set.contains(tenderId)) {
				continue;
			}
			set.add(tenderId);

			String status = record.getStatus();
			if (status.equals("FAILED") || status.equals("PROCESSING")) {
				logger.error("{}提现未成功:{} {}", logPrefix, record.getId(), status);
				continue;
			}

			FinanceInviteTenderEnd tenderEnd = financeInviteTenderEndService.getTenderEndById(tenderId);
			if (tenderEnd != null) {

				FinanceTenderCfg financeTenderCfg = financeTenderCfgService.getById(tenderId);
				if (financeTenderCfg != null) {

					String lcUid = financeTenderCfg.getUid(); // 借款人uid
					String lcUserName = "";
					FinanceFinancingUser findParams = new FinanceFinancingUser();
					findParams.setUid(lcUid);
					List<FinanceFinancingUser> financeingUserList = financeFinancingUserService.findByObj(findParams);
					if (financeingUserList.size() > 0) {
						FinanceFinancingUser financingUser = financeingUserList.get(0);
						lcUserName = financingUser.getName();
					} else {
						logger.error("{}未找到理财人:{}", logPrefix, lcUid);
					}

					FinanceSubmitTender submitTenderParams = new FinanceSubmitTender();
					submitTenderParams.setSid(tenderId);
					List<FinanceSubmitTender> submitTenderList = financeSubmitTenderService.getByObj(submitTenderParams);
					for (FinanceSubmitTender submitTender : submitTenderList) {

						// 过滤掉vip理财人
						boolean isVip = financeInvestUserService.isVipInvestor(submitTender.getUid());
						if (isVip) {
							continue;
						}
						
						FinanceReportCollect collectDetail = new FinanceReportCollect();

						collectDetail.setTenderNo(financeTenderCfg.getTenderNo()); // entity.标的号
						collectDetail.setTimeLimit(financeTenderCfg.getTimeLimit() * 30); // entity.标的周期
						collectDetail.setFinanceBegin(financeTenderCfg.getBeginTime()); // entity.理财开始
						collectDetail.setFinanceEnd(financeTenderCfg.getEndTime()); // /entity.理财结束
						collectDetail.setBasicYield(financeTenderCfg.getYield()); // entity.基础利率
						collectDetail.setTotalYield(submitTender.getFinalYield()); // entity.合计利率
						collectDetail.setChangeYield(submitTender.getFinalYield().subtract(financeTenderCfg.getYield()));// entity.变动利率

						String uid = submitTender.getUid(); // 投资人id
						FinanceInvestUser investUser = financeInvestUserService.getInvestUserByUid(uid);
						if (investUser != null) {
							collectDetail.setInvestorName(investUser.getName()); // entity.投资人
							collectDetail.setPhone(investUser.getPhone()); // entity.投资人手机号
						} else {
							logger.error("{}未找到投资人:{}", logPrefix, uid);
						}

						collectDetail.setBorrowerName(lcUserName); // entity.借款人
						collectDetail.setAmount(submitTender.getAmount()); // entity.投资金额

						FinanceRepayPlanDetail repayPlanfindParams = new FinanceRepayPlanDetail();
						repayPlanfindParams.setSid(submitTender.getId());
						List<FinanceRepayPlanDetail> planDetailList = financeRepayPlanServiceDetail.getByObj(repayPlanfindParams);
						for (FinanceRepayPlanDetail repayPlanDetail : planDetailList) {

							int period = repayPlanDetail.getPeriod().intValue();
							if (period == 1) {
								collectDetail.setOnePlanRefundDate(repayPlanDetail.getRefundTime()); // entity.一期还款计划日
								collectDetail.setOnePrincipal(repayPlanDetail.getPrincipalPayable()); // entity.一期应还本金
								collectDetail.setOneInterest(repayPlanDetail.getInterestPayable()); // entity.一期应还利息

							} else if (period == 2) {
								collectDetail.setTwoPlanRefundDate(repayPlanDetail.getRefundTime()); // entity.二期还款计划日
								collectDetail.setTwoPrincipal(repayPlanDetail.getPrincipalPayable()); // entity.二期应还本金
								collectDetail.setTwoInterest(repayPlanDetail.getInterestPayable()); // entity.二期应还利息

							} else if (period == 3) {
								collectDetail.setThreePlanRefundDate(repayPlanDetail.getRefundTime()); // entity.三期还款计划日
								collectDetail.setThreePrincipal(repayPlanDetail.getPrincipalPayable()); // entity.三期应还本金
								collectDetail.setThreeInterest(repayPlanDetail.getInterestPayable()); // entity.三期应还利息
							}
						}
						// 设置首月
						setMonthValue(collectDetail, financeTenderCfg.getBeginTime(), "begin", submitTender, 0);

						Calendar calBegin = new GregorianCalendar();
						calBegin.setTime(financeTenderCfg.getBeginTime());
						Calendar calEnd = new GregorianCalendar();
						calEnd.setTime(financeTenderCfg.getEndTime());
						int startMonth = calBegin.get(Calendar.MONTH);
						int endMonth = calEnd.get(Calendar.MONTH);

						// 如果结束月小于开始月,说明已跨年 2015.10.22
						if (endMonth < startMonth) {
							endMonth = endMonth + 12; // 加一年的周期
						}

						// 分布在中间的月
						for (int i = 1; i < endMonth - startMonth; i++) {
							calBegin.add(Calendar.MONTH, 1);
							setMonthValue(collectDetail, calBegin.getTime(), "middle", submitTender, 0);
						}

						int diff = calculateMaxDaydiff(financeTenderCfg.getBeginTime());
						if (diff != 30) {
							diff = 30 - diff;
							if (diff < 0) {
								diff = 0;
							}
						}
						if (endMonth - startMonth > 0) {
							setMonthValue(collectDetail, financeTenderCfg.getEndTime(), "end", submitTender, diff);
						}

						collectDetail.setActualDate(DateUtils.convert(date)); // entity.实收日期
						collectDetail.setOrderType("新浪支付"); // entity.订单类型
						collectDetail.setCreateUser("admin");
						collectDetail.setUpdateUser("admin");
						financeReportCollectService.insert(collectDetail);
					}
				}
			} else {
				logger.error("{}未找到标的:{}", logPrefix, tenderId);
			}
		}
	}
	
	/**
	 * 生成报表前清除该天数据
	 * 
	 * @param date
	 */
	private void deleteReportByDate(String date) {

		Date actualDate = DateUtils.convert(date + " " + DateUtils.DAYTIME_START);
		FinanceReportCollect findParams = new FinanceReportCollect();
		findParams.setActualDate(actualDate);
		List<FinanceReportCollect> collectReportList = financeReportCollectService.getByObj(findParams);
		for (FinanceReportCollect reportCollect : collectReportList) {
			financeReportCollectService.deleteById(reportCollect.getId());
		}
	}

	/**
	 * 设置1-12月份分布统计 值
	 * 
	 * @param paymentDetail
	 * @param date
	 * @param type
	 * @param financeSubmitTender
	 */
	private void setMonthValue(FinanceReportCollect paymentDetail, Date date, String type, FinanceSubmitTender financeSubmitTender, int diff) {

		Calendar calBegin = Calendar.getInstance();
		if (date != null) {
			calBegin.setTime(date);
		}

		int month = calBegin.get(Calendar.MONTH);

		// 一月
		if (month == 0) {
			paymentDetail.setJanBegin(date); // entity.一月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setJanCycle(days); // entity.一月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setJanInterest(interest); // entity.一月利息

		}
		// 二月
		else if (month == 1) {
			paymentDetail.setFebBegin(date); // entity.二月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setFebCycle(days); // entity.二月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setFebInterest(interest); // entity.二月利息
		}
		// 三月
		else if (month == 2) {
			paymentDetail.setMarBegin(date); // entity.三月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setMarCycle(days); // entity.三月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setMarInterest(interest); // entity.三月利息
		}
		// 四月
		else if (month == 3) {
			paymentDetail.setAprBegin(date); // entity.三月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setAprCycle(days); // entity.三月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setAprInterest(interest); // entity.三月利息
		}
		// 五月
		else if (month == 4) {
			paymentDetail.setMayBegin(date); // entity.五月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setMayCycle(days); // entity.五月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setMayInterest(interest); // entity.五月利息
		}
		// 六月
		else if (month == 5) {
			paymentDetail.setJunBegin(date); // entity.六月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setJunCycle(days); // entity.六月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setJunInterest(interest); // entity.六月利息
		}
		// 七月
		else if (month == 6) {
			paymentDetail.setJulBegin(date); // entity.七月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setJulCycle(days); // entity.七月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setJulInterest(interest); // entity.七月利息
		}
		// 八月
		else if (month == 7) {
			paymentDetail.setAugBegin(date); // entity.八月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setAugCycle(days); // entity.八月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setAugInterest(interest); // entity.八月利息
		}
		// 九月
		else if (month == 8) {
			paymentDetail.setSeptBegin(date); // entity.九月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setSeptCycle(days); // entity.九月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setSeptInterest(interest); // entity.九月利息
		}
		// 十月
		else if (month == 9) {
			paymentDetail.setOctBegin(date); // entity.十月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setOctCycle(days); // entity.十月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setOctInterest(interest); // entity.十月利息
		}
		// 十一月
		else if (month == 10) {
			paymentDetail.setNovBegin(date); // entity.十一月时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setNovCycle(days); // entity.十一月周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setNovInterest(interest); // entity.十一月利息
		}
		// 十二月
		else if (month == 11) {
			paymentDetail.setDecBegin(date); // entity.十二时间
			int days = 0;
			if (type.equals("begin")) {
				days = calculateMaxDaydiff(date);
			} else if (type.equals("end")) {
				days = diff;
			} else if (type.equals("middle")) {
				days = 30;
			}
			paymentDetail.setDecCycle(days); // entity.十二周期

			BigDecimal yeild = financeSubmitTender.getFinalYield();
			BigDecimal amount = financeSubmitTender.getAmount();
			BigDecimal interest = calculateInterestByDay(amount, yeild, days);
			paymentDetail.setDecInterest(interest); // entity.十二月利息
		}
	}

	/**
	 * 计算当前与当月最后一天日期差
	 * 
	 * @param amount
	 * @param days
	 * @param beginDate
	 * @return
	 */
	private int calculateMaxDaydiff(Date beginDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(beginDate);
		int maxDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int diffDays = maxDayOfMonth - day + 1;
		return diffDays > 30 ? 30 : diffDays;
	}

	/**
	 * 计算当前与1号日期差
	 * 
	 * @param endDate
	 * @return
	 */
	@SuppressWarnings("unused")
	private int calculateMinDaydiff(Date endDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(endDate);
		int maxDayOfMonth = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return day - maxDayOfMonth + 1;
	}

	/**
	 * 日收益计算
	 * 
	 * @param amount
	 * @param yearRate
	 * @param days
	 * @return
	 */
	private BigDecimal calculateInterestByDay(BigDecimal amount, BigDecimal yearRate, int days) {
		BigDecimal resultBg = amount.multiply(yearRate).multiply(new BigDecimal(days)).divide(new BigDecimal("360"), 2).divide(new BigDecimal("100"), 2);
		return resultBg.setScale(2, BigDecimal.ROUND_HALF_UP);
	}
}
