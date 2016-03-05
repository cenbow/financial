package com.mobanker.financial.aop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.activemq.command.ActiveMQQueue;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.mq.ActiveMQNames;
import com.mobanker.financial.mq.ActiveMQProducer;
import com.mobanker.financial.service.FinanceMqProducerLogService;
import com.mobanker.financial.service.FinanceUidMappingService;

/**
 * Description: 标的发布后发送MQ消息
 * 
 * @author yinyafei
 * @date 2015.12.3
 */
@Service
@Aspect
public class TenderReleasedQueue {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private ActiveMQQueue tenderReleaseQueue;
	@Resource
	private ActiveMQProducer activeMQProducer;
	@Resource
	private FinanceMqProducerLogService mqProducerLogService;

	@Pointcut("execution(* com.mobanker.financial.job.service.TenderReleaseService.tenderRelease(..))")
	public void tenderRelease() {

	}

	@AfterReturning(value = "tenderRelease()", returning = "tenderList")
	public void afterTenderRelease(List<FinanceTenderCfg> tenderList) {
		for (FinanceTenderCfg tenderCfg : tenderList) {

			logger.debug("TenderReleasedQueue:发送MQ消息{}", tenderCfg.getTenderNo());
			sendMQMessage(tenderCfg);
		}
	}

	/**
	 * 发送MQ消息
	 * 
	 * @param tenderCfg
	 */
	private void sendMQMessage(FinanceTenderCfg tenderCfg) {

		Map<String, String> tenderMap = new HashMap<String, String>();
		tenderMap.put("id", tenderCfg.getId());
		tenderMap.put("tenderNo", tenderCfg.getTenderNo());
		tenderMap.put("tenderName", tenderCfg.getTenderName());
		tenderMap.put("timeLimit", tenderCfg.getTimeLimit().toString());
		tenderMap.put("amount", tenderCfg.getAmount().toString());
		tenderMap.put("yield", tenderCfg.getYield().toString());
		String uid = uidMappingService.getYYDUid(tenderCfg.getUid());
		tenderMap.put("uid", uid);
		tenderMap.put("inviteTime", DateUtils.convert(tenderCfg.getInviteTime()));
		tenderMap.put("finishTime", DateUtils.convert(tenderCfg.getFinishTime()));
		tenderMap.put("beginTime", DateUtils.convert(tenderCfg.getBeginTime()));
		tenderMap.put("endTime", DateUtils.convert(tenderCfg.getEndTime()));

		activeMQProducer.sendMapMsg(tenderMap, tenderReleaseQueue);

		String content = JSONObject.toJSONString(tenderMap);
		String name = ActiveMQNames.TENDER_RELEASE_QUEUE;
		String system = ActiveMQNames.CW_SYSTEM;
		mqProducerLogService.saveProducerMsgLog(BillNoUtils.GenerateBillNo(), name, content, system);
	}
}
