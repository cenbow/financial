package com.mobanker.financial.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceDepositWithdrawDao;
import com.mobanker.financial.entity.FinanceDepositWithdraw;
import com.mobanker.financial.service.FinanceDepositWithdrawService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceDepositWithdrawServiceImpl extends BaseServiceImpl<FinanceDepositWithdraw> implements FinanceDepositWithdrawService {

	@Resource
	private FinanceDepositWithdrawDao financeDepositWithdrawDao;

	@Override
	public FinanceDepositWithdraw getLatestDepostRecord(String uid) {

		return financeDepositWithdrawDao.getLatestDepostRecord(uid);
	}

	@Override
	public List<String> getDepositRecordByDate(Date date) {

		return financeDepositWithdrawDao.getDepositRecordByDate(date);
	}
}
