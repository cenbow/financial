package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceUidMapping;
import com.mobanker.framework.service.BaseService;

public interface FinanceUidMappingService extends BaseService<FinanceUidMapping> {
	
	public String getFinanceUid(String paramString);

	public String getYYDUid(String paramString);
}
