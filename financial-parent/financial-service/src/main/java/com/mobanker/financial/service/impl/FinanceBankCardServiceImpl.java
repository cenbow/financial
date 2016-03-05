package com.mobanker.financial.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceBankCardDao;
import com.mobanker.financial.entity.FinanceBankCard;
import com.mobanker.financial.service.FinanceBankCardService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceBankCardServiceImpl extends BaseServiceImpl<FinanceBankCard> implements FinanceBankCardService {

	@Resource
	private FinanceBankCardDao bankCardDao;

	@Override
	public FinanceBankCard getBankCardByUid(String uid) {

		FinanceBankCard findParams = new FinanceBankCard();
		findParams.setUid(uid);
		findParams.setIsBinding("1");
		return bankCardDao.getOne(findParams);
	}

	@Override
	public List<FinanceBankCard> getBankCardByDate(Date date) {
		
		return bankCardDao.getBankCardByDate(date);
	}
}