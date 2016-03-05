package com.mobanker.financial.service;

import java.util.List;

import com.mobanker.financial.entity.FinanceReportChannel;
import com.mobanker.framework.dto.ResponseEntity;
import com.mobanker.framework.service.BaseService;

public interface FinanceReportChannelService extends
		BaseService<FinanceReportChannel> {
	
	Integer generatorChannel(List<String> date);
	
	ResponseEntity getDateByParams(String beginTime, String endTime, String version, String channel, String sortField, String sortType);
	
	void deleteByDaily(String date);
	
	ResponseEntity getDataByChannel(String channel, String beginTime, String endTime, String sortField, String sortType);

}
