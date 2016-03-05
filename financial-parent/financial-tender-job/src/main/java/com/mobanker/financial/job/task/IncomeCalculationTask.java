package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.IncomeCalculationService;

/**
 * Description:用户收益计算
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Component
public class IncomeCalculationTask {

	@Resource
	private IncomeCalculationService calculationService;

	public void incomeCalculate() {

		calculationService.incomeCalculate();
	}
}
