package com.mobanker.financial.dao;

import java.util.Date;
import java.util.List;

import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceBankCardDao extends BaseDao<FinanceBankCard> {
	
	public List<FinanceBankCard> getBankCardByDate(Date date);
}
