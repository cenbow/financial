package com.mobanker.financial.aop;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.mq.ActiveMQNames;
import com.mobanker.financial.mq.ActiveMQProducer;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.financial.service.FinanceMqProducerLogService;
import com.mobanker.financial.service.FinanceSubmitTenderService;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.financial.service.FinanceUidMappingService;

/**
 * Description: 招标完成,发送MQ消息通知账户系统
 * 
 * @author yinyafei
 * @date 2015.12.7
 */
@Service
@Aspect
public class TenderCompletionTopic {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private FinanceSubmitTenderService financeSubmitTenderService;
	@Resource
	private FinanceInvestUserService financeInvestUserService;
	@Resource
	private FinanceTenderCfgService financeTenderCfgService;
	@Resource
	private FinanceUidMappingService financeUidMappingService;
	@Resource
	private ActiveMQProducer activeMQProducer;
	@Resource
	private FinanceMqProducerLogService mqProducerLogService;
	@Resource
	private ActiveMQTopic lcBidTenderTopic;
	@Resource
	private ActiveMQQueue completionTenderQueue;

	@Pointcut("execution(* com.mobanker.financial.job.service.TenderCompletionService.tenderComplete(..)) || execution(* com.mobanker.financial.job.service.TenderCompletionService.tenderCompteteById(..))")
	public void tenderComplete() {

	}

	@AfterReturning(value = "tenderComplete()", returning = "tenderList")
	public void afterTenderComplete(List<FinanceTenderCfg> tenderList) {

		for (FinanceTenderCfg tenderCfg : tenderList) {

			/*FinanceSubmitTender findParams = new FinanceSubmitTender();
			findParams.setSid(tenderCfg.getId());
			List<FinanceSubmitTender> submitTenderList = financeSubmitTenderService.getByObj(findParams);

			for (FinanceSubmitTender submitTender : submitTenderList) {
				submitTenderSuccess(submitTender);
			}*/
			
			logger.debug("TenderCompletionTopic--:发送MQ消息{}", tenderCfg.getTenderNo());
			
			notifyCWByMQ(tenderCfg);
		}
	}
	
	/**
	 * 结标后通知借款端财务系统
	 * 
	 * @param tenderCfg
	 */
	private void notifyCWByMQ(FinanceTenderCfg tenderCfg) {

		// 如果招标没有完成,发送消息通知借款财务需要补标金额等信息
		if (tenderCfg.getAmount().compareTo(tenderCfg.getInputAmount()) != 0) {
			Map<String, String> tenderMap = new HashMap<String, String>();
			tenderMap.put("id", tenderCfg.getId());
			tenderMap.put("tenderNo", tenderCfg.getTenderNo());
			tenderMap.put("tenderName", tenderCfg.getTenderName());
			BigDecimal needAmountBg = tenderCfg.getAmount().subtract(tenderCfg.getInputAmount());
			tenderMap.put("needAmount", needAmountBg.toString());

			activeMQProducer.sendMapMsg(tenderMap, completionTenderQueue);
			String serialNumber = BillNoUtils.GenerateBillNo();
			String content = JSONObject.toJSONString(tenderMap);
			String name = ActiveMQNames.LC_COMPLETION_TENDER_QUEUE;
			String system = ActiveMQNames.CW_SYSTEM;
			mqProducerLogService.saveProducerMsgLog(serialNumber, name, content, system);
		}
	}

	/**
	 * 投资成功消息队列
	 * 
	 * @param submitTender
	 */
	public void submitTenderSuccess(FinanceSubmitTender submitTender) {

		String uid = submitTender.getUid();
		FinanceInvestUser investor = financeInvestUserService.getInvestUserByUid(uid);
		if (investor != null) {

			FinanceTenderCfg tenderCfg = financeTenderCfgService.getById(submitTender.getSid());
			Map<String, String> messageMap = new HashMap<String, String>();
			String serialNumber = BillNoUtils.GenerateBillNo();
			messageMap.put("serialNumber", serialNumber);
			messageMap.put("channelId", "SINA");
			messageMap.put("uid", financeUidMappingService.getYYDUid(investor.getUid()));
			messageMap.put("name", investor.getName());
			messageMap.put("phone", investor.getName());
			messageMap.put("orderNo", submitTender.getOrderNo());
			messageMap.put("tenderNo", tenderCfg.getTenderNo());
			messageMap.put("tenderAmount", tenderCfg.getAmount().toString());
			messageMap.put("amount", submitTender.getAmount().toString());
			messageMap.put("bidTime", DateUtils.convert(submitTender.getAddTime(), null));
			messageMap.put("yield", tenderCfg.getYield().toString());
			String couponYield = submitTender.getFinalYield().subtract(tenderCfg.getYield()).toString();
			messageMap.put("couponYield", couponYield);
			messageMap.put("timeLimit", tenderCfg.getTimeLimit().toString());
			messageMap.put("expectIncome", submitTender.getExpectIncome().toString());
			messageMap.put("tenderBegin", DateUtils.convert(tenderCfg.getBeginTime(), null));
			messageMap.put("tenderEnd", DateUtils.convert(tenderCfg.getEndTime(), null));

			activeMQProducer.sendMapMsg(messageMap, lcBidTenderTopic);

			String content = JSONObject.toJSONString(messageMap);
			String name = ActiveMQNames.TENDER_SUCCESS_TOPIC;
			String system = ActiveMQNames.ACCOUNT_SYSTEM;
			mqProducerLogService.saveProducerMsgLog(serialNumber, name, content, system);
		}
	}
}
