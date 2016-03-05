package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.framework.service.BaseService;

public interface FinanceInviteTenderEndService extends BaseService<FinanceInviteTenderEnd> {

	/**
	 * 获取招标完成
	 * 
	 * @param tenderId
	 * @return
	 */
	public FinanceInviteTenderEnd getTenderEndById(String tenderId);

	/**
	 * 根据自身id获取招标完成
	 * 
	 * @param id
	 * @return
	 */
	public FinanceInviteTenderEnd getBySelfId(String id);
}
