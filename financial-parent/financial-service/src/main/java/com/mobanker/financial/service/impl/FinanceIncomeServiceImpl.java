package com.mobanker.financial.service.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceIncomeDao;
import com.mobanker.financial.entity.FinanceIncome;
import com.mobanker.financial.service.FinanceIncomeService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceIncomeServiceImpl extends BaseServiceImpl<FinanceIncome> implements FinanceIncomeService {

	@Resource
	private FinanceIncomeDao incomeDao;

	public FinanceIncome getFinanceIncomeByUid(String uid) {
		FinanceIncome findParams = new FinanceIncome();
		findParams.setUid(uid);
		List<FinanceIncome> incomeList = getByObj(findParams);
		if (incomeList.size() > 0) {
			return incomeList.get(0);
		}
		return null;
	}

	@Override
	public BigDecimal calculateIncome(BigDecimal amount, BigDecimal timeLimit, BigDecimal yield) {
		BigDecimal result = amount.multiply(timeLimit).multiply(yield).divide(new BigDecimal("12"), 2).divide(new BigDecimal("100"), 2);
		result = result.setScale(2, BigDecimal.ROUND_HALF_UP);
		return result;
	}

	@Override
	public List<FinanceIncome> getAvailableGreaterThanZeroIncome() {

		return incomeDao.getAvailableGreaterThanZeroIncome();
	}

	@Override
	public List<FinanceIncome> getYesdayEarnsGreaterThanZeroIncome(String date) {

		return incomeDao.getYesdayEarnsGreaterThanZeroIncome(date);
	}

	@Override
	public List<FinanceIncome> getEarnsGreaterThanZeroIncome() {
		
		return incomeDao.getEarnsGreaterThanZeroIncome();
	}
}
