package com.mobanker.financial.service.impl;

import java.util.Date;

import com.mobanker.financial.entity.FinanceMqProducerLog;
import com.mobanker.financial.service.FinanceMqProducerLogService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceMqProducerLogServiceImpl extends BaseServiceImpl<FinanceMqProducerLog> implements FinanceMqProducerLogService {
	
	@Override
	public void saveProducerMsgLog(String serialNumber, String name, String content, String consumer) {

		FinanceMqProducerLog producerLog = new FinanceMqProducerLog();
		producerLog.setCreateUser("admin");
		producerLog.setUpdateUser("admin");
		producerLog.setUpdateTime(new Date());
		producerLog.setSerialNumber(serialNumber);
		producerLog.setName(name);
		producerLog.setContent(content);
		producerLog.setConsumer(consumer);
		producerLog.setConsumerIp("");
		producerLog.setStatus("0");
		producerLog.setRetryNum(3);
		producerLog.setHasRetry(0);
		this.insert(producerLog);
	}
}
