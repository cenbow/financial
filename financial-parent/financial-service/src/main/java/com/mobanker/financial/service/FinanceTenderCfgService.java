package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.framework.service.BaseService;

public interface FinanceTenderCfgService extends BaseService<FinanceTenderCfg> {
	
	public FinanceTenderCfg getFinanceTenderCfg(String tenderId); 
}
