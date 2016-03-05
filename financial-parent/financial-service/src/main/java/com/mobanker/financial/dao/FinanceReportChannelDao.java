package com.mobanker.financial.dao;

import java.util.List;
import java.util.Map;

import com.mobanker.financial.entity.FinanceReportChannel;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceReportChannelDao extends BaseDao<FinanceReportChannel> {
	/**
	 * 查询渠道数据
	 * @param map
	 * @return
	 */
	List<FinanceReportChannel> getDateByParams(Map<String, Object> map);
	
	void deleteByDaily(String date);
	
	List<FinanceReportChannel> getDataByChannel(Map<String, Object> map);
}
