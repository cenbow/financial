package com.mobanker.financial.service;

import java.util.Date;
import java.util.List;

import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.framework.service.BaseService;

public interface FinanceBankCardService extends BaseService<FinanceBankCard> {
	
	/**
	 * 根据uid获取用户绑定的银行卡
	 * @param uid
	 * @return
	 */
	public FinanceBankCard getBankCardByUid(String uid);
	
	/**
	 * 根据日期获取绑卡用户
	 * @param date
	 * @return
	 */
	public List<FinanceBankCard> getBankCardByDate(Date date);
}
