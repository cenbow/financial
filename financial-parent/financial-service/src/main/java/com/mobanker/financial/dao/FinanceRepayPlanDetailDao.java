package com.mobanker.financial.dao;

import java.util.List;

import com.mobanker.financial.entity.FinanceRepayPlanDetail;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceRepayPlanDetailDao extends BaseDao<FinanceRepayPlanDetail> {
	
	public List<FinanceRepayPlanDetail> getRepayDetailListByDate(String repayDate);
	
	public List<FinanceRepayPlanDetail> getRepayDetailListBySuccessDate(String repayDate);
}
