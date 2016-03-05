package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceReportPay;
import com.mobanker.framework.service.BaseService;

public interface FinanceReportPayService extends BaseService<FinanceReportPay> {

	/**
	 * 根据日期获取实付报表
	 * 
	 * @param date
	 * @return
	 */
	public List<FinanceReportPay> getFinanceReportPayByAcutalDate(String date);
}
