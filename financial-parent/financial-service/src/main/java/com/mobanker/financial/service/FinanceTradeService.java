package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.framework.service.BaseService;

public interface FinanceTradeService extends BaseService<FinanceTrade> {
	
	/**
	 * 获取指定日期代收交易列表
	 * @param date
	 * @return
	 */
	public List<FinanceTrade> getCollectionListByDate(String date);
}
