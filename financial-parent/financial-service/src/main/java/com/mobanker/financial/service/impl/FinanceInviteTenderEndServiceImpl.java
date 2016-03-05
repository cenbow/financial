package com.mobanker.financial.service.impl;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceInviteTenderEndDao;
import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.financial.service.FinanceInviteTenderEndService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceInviteTenderEndServiceImpl extends BaseServiceImpl<FinanceInviteTenderEnd> implements FinanceInviteTenderEndService {

	@Resource
	private FinanceInviteTenderEndDao financeInviteTenderEndDao;

	public FinanceInviteTenderEnd getTenderEndById(String tenderId) {

		return financeInviteTenderEndDao.getTenderEndById(tenderId);
	}

	@Override
	public FinanceInviteTenderEnd getBySelfId(String id) {
		
		return financeInviteTenderEndDao.getBySelfId(id);
	}
}
