package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mobanker.financial.common.enums.InvestorType;
import com.mobanker.financial.dao.FinanceInvestUserDao;
import com.mobanker.financial.entity.FinanceInvestUser;
import com.mobanker.financial.service.FinanceInvestUserService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

@Service
public class FinanceInvestUserServiceImpl extends BaseServiceImpl<FinanceInvestUser> implements FinanceInvestUserService {

	@Resource
	private  FinanceInvestUserDao financeInvestUserDao;
	
	@Override
	public FinanceInvestUser getInvestUserByUid(String uid) {

		FinanceInvestUser findParams = new FinanceInvestUser();
		findParams.setUid(uid);
		List<FinanceInvestUser> investUserList = getByObj(findParams);
		if (investUserList.size() > 0) {
			return investUserList.get(0);
		}
		return null;
	}

	@Override
	public boolean isVipInvestor(String uid) {

		FinanceInvestUser findParams = new FinanceInvestUser();
		findParams.setUid(uid);
		List<FinanceInvestUser> investUserList = getByObj(findParams);
		if (investUserList.size() > 0) {
			if (investUserList.get(0).getUserType().equals(InvestorType.VIP.toString())) {
				return true;
			}
		}
		return false;
	}
}
