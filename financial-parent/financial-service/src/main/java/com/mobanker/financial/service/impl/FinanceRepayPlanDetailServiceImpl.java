package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceRepayPlanDetailDao;
import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.financial.service.FinanceRepayPlanDetailService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceRepayPlanDetailServiceImpl extends BaseServiceImpl<FinanceRepayPlanDetail> implements FinanceRepayPlanDetailService {

	@Resource
	private FinanceRepayPlanDetailDao financeRepayPlanDetailDao;
	
	@Override
	public List<FinanceRepayPlanDetail> getRepayDetailListByDate(String repayDate) {
		
		return financeRepayPlanDetailDao.getRepayDetailListByDate(repayDate);
	}

	@Override
	public List<FinanceRepayPlanDetail> getRepayDetailListBySuccessDate(String repayDate) {

		return financeRepayPlanDetailDao.getRepayDetailListBySuccessDate(repayDate);
	}
}
