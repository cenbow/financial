package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.framework.service.BaseService;

public interface FinanceInvestUserService extends BaseService<FinanceInvestUser> {

	/**
	 * 根据uid获取投资人
	 * 
	 * @param uid
	 * @return
	 */
	public FinanceInvestUser getInvestUserByUid(String uid);

	/**
	 * 判断用户是否是vip理财人
	 * 
	 * @param uid
	 * @return
	 */
	public boolean isVipInvestor(String uid);

}
