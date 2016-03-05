package com.mobanker.financial.dao;

import java.util.List;

import com.mobanker.financial.entity.FinanceReportPay;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceReportPayDao extends BaseDao<FinanceReportPay> {

	public List<FinanceReportPay> getFinanceReportPayByAcutalDate(String date);
	
}
