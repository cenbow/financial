package com.mobanker.financial.dao;

import java.math.BigDecimal;
import java.util.List;

import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceIncomeDao extends BaseDao<FinanceIncome> {
	
	public List<FinanceIncome> getAvailableGreaterThanZeroIncome();
	
	public List<FinanceIncome> getYesdayEarnsGreaterThanZeroIncome(String date);
	
	public List<FinanceIncome> getEarnsGreaterThanZeroIncome();
	
	public BigDecimal getSavingpotAvailableBalance();
}
