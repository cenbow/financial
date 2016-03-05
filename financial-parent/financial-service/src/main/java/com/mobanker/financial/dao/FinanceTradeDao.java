package com.mobanker.financial.dao;

import java.util.List;

import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceTradeDao extends BaseDao<FinanceTrade> {
	
	public List<FinanceTrade> getCollectionListByDate(String date);
}
