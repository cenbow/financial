package com.mobanker.financial.service;

import java.math.BigDecimal;
import java.util.List;

import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.framework.service.BaseService;

public interface FinanceIncomeService extends BaseService<FinanceIncome> {
	
	/**
	 * 通过uid获取收益信息
	 * @param uid
	 * @return
	 */
	public FinanceIncome getFinanceIncomeByUid(String uid);
	
	/**
	 * 收益计算
	 * @param amount
	 * @param timeLimit
	 * @param yield
	 * @return
	 */
	public BigDecimal calculateIncome(BigDecimal amount, BigDecimal timeLimit, BigDecimal yield);
	
	/**
	 * 获取用于余额大于0的收益列表
	 * @return
	 */
	public List<FinanceIncome> getAvailableGreaterThanZeroIncome();
	
	/**
	 * 获取昨日收益大于0的收益列表
	 * @return
	 */
	public List<FinanceIncome> getYesdayEarnsGreaterThanZeroIncome(String date);
	
	/**
	 * 获取理财收益大于0的收益列表
	 * @return
	 */
	public List<FinanceIncome> getEarnsGreaterThanZeroIncome();
}
