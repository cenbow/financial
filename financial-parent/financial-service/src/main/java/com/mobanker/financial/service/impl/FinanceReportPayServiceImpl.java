package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceReportPayDao;
import com.mobanker.financial.entity.FinanceReportPay;
import com.mobanker.financial.service.FinanceReportPayService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceReportPayServiceImpl extends BaseServiceImpl<FinanceReportPay> implements FinanceReportPayService {

	@Resource
	private FinanceReportPayDao financeReportPayDao;
	
	public List<FinanceReportPay> getFinanceReportPayByAcutalDate(String date){
		
		return financeReportPayDao.getFinanceReportPayByAcutalDate(date);
	}
}
