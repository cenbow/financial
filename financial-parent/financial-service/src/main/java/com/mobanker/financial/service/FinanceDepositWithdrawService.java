package com.mobanker.financial.service;

import java.util.Date;
import java.util.List;

import com.mobanker.financial.entity.FinanceDepositWithdraw;
import com.mobanker.framework.service.BaseService;

public interface FinanceDepositWithdrawService extends BaseService<FinanceDepositWithdraw> {
	
	public FinanceDepositWithdraw getLatestDepostRecord(String uid);
	
	public List<String> getDepositRecordByDate(Date date);
}
