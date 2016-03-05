package com.mobanker.financial.aop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.activemq.command.ActiveMQTopic;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceRefundStatistics;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.mq.ActiveMQNames;
import com.mobanker.financial.mq.ActiveMQProducer;
import com.mobanker.financial.service.FinanceMqProducerLogService;
import com.mobanker.financial.service.FinanceRefundStatisticsService;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.financial.service.FinanceTenderCfgService;

/**
 * Description: 还款成功后发送MQ主题消息
 * 
 * @author yinyafei 
 * @date 2015.11.24
 */
//@Service
@Aspect
public class RepaymentTopic {

	@Resource
	private FinanceTenderCfgService tenderCfgService;
	@Resource
	private FinanceRepayPlanDetailService repayPlanDetailService;
	@Resource
	private FinanceRefundStatisticsService refundStatisticsService;
	@Resource
	private FinanceMqProducerLogService mqProducerLogService;
	@Resource
	private ActiveMQProducer activeMQProducer;
	@Resource
	private ActiveMQTopic userRefundTopic;

	@Pointcut("execution(* com.mobanker.financial.job.service.RepaymentService.autoRepay(..))")
	public void autoRepay() {
		
	}

	/**
	 * 自动还款发送MQ消息
	 */
	@AfterReturning(value = "autoRepay()")
	public void afterAutoRepay() {

		FinanceRepayPlanDetail planDetailParams = new FinanceRepayPlanDetail();
		planDetailParams.setRefundTime(DateUtils.getStartDatetime(new Date()));
		planDetailParams.setStatus(0);
		List<FinanceRepayPlanDetail> planDetailList = repayPlanDetailService.getByObj(planDetailParams);

		for (FinanceRepayPlanDetail planDetail : planDetailList) {
			sendMQMessage(planDetail);
		}
	}

	@Pointcut("execution(* com.mobanker.financial.job.service.RepaymentService.repayByBatch(String))")
	public void repayByBatch() {
	}

	/**
	 * 批量还款发送MQ消息
	 * 
	 * @param jp
	 * @param tenderNos
	 */
	@AfterReturning("repayByBatch() && args(tenderNos,..)")
	public void afterRepayByBatch(JoinPoint jp, String tenderNos) {

		String[] tenderNoArray = tenderNos.split(",");
		for (String tenderNo : tenderNoArray) {
			List<FinanceRepayPlanDetail> planDetailList = getPlanDetailList(tenderNo);
			if (planDetailList != null) {
				for (FinanceRepayPlanDetail planDetail : planDetailList) {
					sendMQMessage(planDetail);
				}
			}
		}
	}

	@Pointcut("execution(* com.mobanker.financial.job.service.RepaymentService.repayByTender(String))")
	public void repayByTender() {
	}

	/**
	 * 按标的还款发送MQ消息
	 * 
	 * @param jp
	 * @param tenderNo
	 */
	@AfterReturning("repayByTender() && args(tenderNo,..)")
	public void afterRepayByTender(JoinPoint jp, String tenderNo) {

		List<FinanceRepayPlanDetail> planDetailList = getPlanDetailList(tenderNo);
		if (planDetailList != null) {
			for (FinanceRepayPlanDetail planDetail : planDetailList) {
				sendMQMessage(planDetail);
			}
		}
	}

	@Pointcut("execution(* com.mobanker.financial.job.service.RepaymentService.repayByOrderNo(String))")
	public void repayByOrderNo() {
	}

	/**
	 * 按单号还款发送MQ消息
	 * 
	 * @param jp
	 * @param orderNo
	 */
	@AfterReturning("repayByOrderNo() && args(orderNo,..)")
	public void afterRepayByOrderNo(JoinPoint jp, String orderNo) {

		FinanceRepayPlanDetail detailFindParams = new FinanceRepayPlanDetail();
		detailFindParams.setOrderNo(orderNo);
		detailFindParams.setStatus(0); // 未还款记录
		List<FinanceRepayPlanDetail> planDetailList = repayPlanDetailService.getByObj(detailFindParams);

		for (FinanceRepayPlanDetail planDetail : planDetailList) {
			sendMQMessage(planDetail);
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param repayPlanDetail
	 * @param tenderCfg
	 */
	private void sendMQMessage(FinanceRepayPlanDetail repayPlanDetail) {

		Map<String, String> mapMessage = new HashMap<String, String>();
		mapMessage.put("channelId", ActiveMQNames.CHAANEL);
		mapMessage.put("uid", repayPlanDetail.getUid());

		FinanceTenderCfg tenderCfg = tenderCfgService.getById(repayPlanDetail.getTid());
		if (tenderCfg != null) {
			mapMessage.put("tenderNo", tenderCfg.getTenderNo());
		}

		mapMessage.put("refundTime", DateUtils.convert(repayPlanDetail.getRefundTime(), null));
		mapMessage.put("refundSuccessTime", DateUtils.convert(repayPlanDetail.getRefundSuccessTime(), null));

		// 发送回息消息
		BigDecimal interestBg = repayPlanDetail.getInterestPayable();
		if (interestBg.compareTo(BigDecimal.ZERO) > 0) {
			String serialNumber = BillNoUtils.GenerateBillNo();
			mapMessage.put("serialNumber", serialNumber);
			mapMessage.put("orderNo", "LC" + repayPlanDetail.getRefundOrderNo());
			mapMessage.put("amount", interestBg.toString());
			activeMQProducer.sendMapMsg(mapMessage, userRefundTopic);

			String content = JSONObject.toJSONString(mapMessage);
			mqProducerLogService.saveProducerMsgLog(serialNumber, ActiveMQNames.LC_USE_REFUND_TOPIC, content, ActiveMQNames.CW_SYSTEM);
		}

		// 发送回本消息
		BigDecimal principalBg = repayPlanDetail.getPrincipalPayable();
		if (principalBg.compareTo(BigDecimal.ZERO) > 0) {
			String serialNumber = BillNoUtils.GenerateBillNo();
			mapMessage.put("serialNumber", serialNumber);
			mapMessage.put("orderNo", "LC" + repayPlanDetail.getRefundOrderNo());
			mapMessage.put("amount", principalBg.toString());
			activeMQProducer.sendMapMsg(mapMessage, userRefundTopic);
			
			String content = JSONObject.toJSONString(mapMessage);
			mqProducerLogService.saveProducerMsgLog(serialNumber, ActiveMQNames.LC_USE_REFUND_TOPIC, content, ActiveMQNames.CW_SYSTEM);
		}
	}

	/**
	 * 获取还款明细列表
	 * 
	 * @param tenderNo
	 */
	public List<FinanceRepayPlanDetail> getPlanDetailList(String tenderNo) {
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
			return repayPlanDetailList;
		}
		return null;
	}

	/**
	 * 过滤满足条件的还款记录
	 * 
	 * @param repayPlanDetailList
	 * @return
	 */
	public List<FinanceRepayPlanDetail> filterRepayDetailList(List<FinanceRepayPlanDetail> repayPlanDetailList) {

		List<FinanceRepayPlanDetail> filterPlanDetailList = new ArrayList<FinanceRepayPlanDetail>();

		for (FinanceRepayPlanDetail planDetail : repayPlanDetailList) {

			Date refundTimeDate = planDetail.getRefundTime();

			FinanceRefundStatistics findParams = new FinanceRefundStatistics();
			findParams.setPlanRefundTime(refundTimeDate);
			findParams.setDocStatus("1");
			findParams.setCheckStatus("1");
			findParams.setBorrowerRechargeStatus("1");
			List<FinanceRefundStatistics> refundStatisticsList = refundStatisticsService.getByObj(findParams);
			if (refundStatisticsList.size() > 0) {
				filterPlanDetailList.add(planDetail);
			}
		}
		return filterPlanDetailList;
	}
}
