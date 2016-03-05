package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.framework.service.BaseService;

public interface FinanceRepayPlanDetailService extends BaseService<FinanceRepayPlanDetail> {
	
	public List<FinanceRepayPlanDetail> getRepayDetailListByDate(String repayDate);
	
	public List<FinanceRepayPlanDetail> getRepayDetailListBySuccessDate(String repayDate);
}
