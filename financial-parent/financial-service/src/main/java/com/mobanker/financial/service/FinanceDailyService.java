package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceDaily;
import com.mobanker.framework.service.BaseService;

public interface FinanceDailyService extends BaseService<FinanceDaily> {
	
	/**
	 * 生成日报
	 * @param dateList 日期格式yyyyMMdd
	 * @return 生成日报的条数
	 */
	Integer generatorDaily(List<String> dateList);
	
	void deleteByDaily(String date);

}
