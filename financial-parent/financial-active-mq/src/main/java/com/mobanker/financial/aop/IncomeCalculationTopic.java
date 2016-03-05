package com.mobanker.financial.aop;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.ibatis.session.RowBounds;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.BillNoUtils;
import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.financial.mq.ActiveMQNames;
import com.mobanker.financial.mq.ActiveMQProducer;
import com.mobanker.financial.service.FinanceIncomeService;
import com.mobanker.financial.service.FinanceMqProducerLogService;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.framework.mybatis.SqlInterceptor;

/**
 * Description： 用户收益发送MQ消息通知账户系统
 * 
 * @author yinyafei
 * @date 2015.12.07
 */
//@Service
@Aspect
public class IncomeCalculationTopic {

	@Resource
	private FinanceIncomeService incomeService;
	@Resource
	private FinanceUidMappingService uidMappingService;
	@Resource
	private ActiveMQProducer activeMQProducer;
	@Resource
	private ActiveMQTopic lcUserIncomeTopic;
	@Resource
	private FinanceMqProducerLogService mqProducerLogService;

	@Pointcut("execution(* com.mobanker.financial.job.service.IncomeCalculationService.incomeCalculate(..))")
	public void incomeCalculate() {

	}

	@AfterReturning(value = "incomeCalculate()")
	public void afterIncomeCalculate() {

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

			for (FinanceIncome income : incomeList) { // 收益处理
				sendIncomeMQMessage(income);
			}

			start = i * size;
		}
	}

	/**
	 * 发送MQ消息
	 * 
	 * @param uid
	 * @param incomeAmout
	 */
	private void sendIncomeMQMessage(FinanceIncome income) {

		Map<String, String> messageMap = new HashMap<String, String>();
		String serialNumber = BillNoUtils.GenerateBillNo();
		messageMap.put("serialNumber", serialNumber);
		messageMap.put("channelId", "SINA");
		String uid = uidMappingService.getYYDUid(income.getUid());
		messageMap.put("uid", uid);
		messageMap.put("amount", income.getSavingpotYesterdayEarnings().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		messageMap.put("incomeTime", DateUtils.convert(new Date(), DateUtils.DATE_TIME_FORMAT));
		messageMap.put("orderNo", "LC" + serialNumber);

		activeMQProducer.sendMapMsg(messageMap, lcUserIncomeTopic);

		String content = JSONObject.toJSONString(messageMap);
		String name = ActiveMQNames.LC_USER_INCOME_TOPIC;
		String system = ActiveMQNames.ACCOUNT_SYSTEM;
		mqProducerLogService.saveProducerMsgLog(serialNumber, name, content, system);
	}
}
