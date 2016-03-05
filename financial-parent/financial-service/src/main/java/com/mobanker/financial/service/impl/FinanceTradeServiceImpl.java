package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceTradeDao;
import com.mobanker.financial.entity.FinanceTrade;
import com.mobanker.financial.service.FinanceTradeService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceTradeServiceImpl extends BaseServiceImpl<FinanceTrade> implements FinanceTradeService {

	@Resource
	private FinanceTradeDao financeTradeDao;
	
	public List<FinanceTrade> getCollectionListByDate(String date){
		
		return financeTradeDao.getCollectionListByDate(date);
	}
}
