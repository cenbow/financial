package com.mobanker.financial.service.impl;

import javax.annotation.Resource;

import com.mobanker.financial.dao.FinanceTenderCfgDao;
import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.service.FinanceTenderCfgService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class FinanceTenderCfgServiceImpl extends BaseServiceImpl<FinanceTenderCfg> implements FinanceTenderCfgService {

	@Resource
	private FinanceTenderCfgDao financeTenderCfgDao;
	
	@Override
	public FinanceTenderCfg getFinanceTenderCfg(String tenderId) {
		
		return financeTenderCfgDao.getFinanceTenderCfg(tenderId);
	}
}
