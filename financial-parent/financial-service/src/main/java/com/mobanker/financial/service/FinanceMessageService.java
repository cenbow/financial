package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceMessage;
import com.mobanker.framework.service.BaseService;

public interface FinanceMessageService extends BaseService<FinanceMessage> {

	/**
	 * 获取所有消息 请使用插件拼接上分页条件
	 */
	public List<FinanceMessage> getAllUnSendMessage();
}
