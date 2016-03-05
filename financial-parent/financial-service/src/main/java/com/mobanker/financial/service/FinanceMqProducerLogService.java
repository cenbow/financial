package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceMqProducerLog;
import com.mobanker.framework.service.BaseService;

public interface FinanceMqProducerLogService extends BaseService<FinanceMqProducerLog> {

	/**
	 * 保存消息生产记录
	 * 
	 * @param serialNumber
	 * @param name
	 * @param content
	 * @param consumer
	 */
	public void saveProducerMsgLog(String serialNumber, String name, String content, String consumer);
}
