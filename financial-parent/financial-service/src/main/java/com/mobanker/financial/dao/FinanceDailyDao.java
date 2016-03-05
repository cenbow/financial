package com.mobanker.financial.dao;

import java.math.BigDecimal;

import com.mobanker.financial.entity.FinanceDaily;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceDailyDao extends BaseDao<FinanceDaily> {
	/**
	 * 获取ios用户昨日投资人数
	 * @param date
	 * @return
	 */
	BigDecimal getYesterdayIOSTotalInvestNum(String date);
	
	/**
	 * 获取ios昨日投资金额
	 * @param date
	 * @return
	 */
	BigDecimal getYesterdayIOSTotalInvestAmount(String date);
	
	/**
	 * 获取Android用户昨日投资人数
	 * @param date
	 * @return
	 */
	BigDecimal getYesterdayAndroidTotalInvestNum(String date);
	
	/**
	 * 获取昨日Android投资金额 
	 * @param date2
	 * @return
	 */
	BigDecimal getYesterdayAndroidTotalInvestAmount(String date2);
	
	
	void deleteByDaily(String date);
}
