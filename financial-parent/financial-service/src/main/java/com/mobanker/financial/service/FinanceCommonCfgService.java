package com.mobanker.financial.service;

import com.mobanker.financial.entity.FinanceCommonCfg;
import com.mobanker.framework.service.BaseService;
import java.util.List;

public interface FinanceCommonCfgService extends BaseService<FinanceCommonCfg> {

	public String getCodeValueById(String paramString);

	public FinanceCommonCfg getCommonCfgById(String paramString);

	public FinanceCommonCfg getCommonCfgByCode(String paramString);

	public String getCommonCfgValueByCode(String paramString);

	public List<FinanceCommonCfg> getCommonCfgByType(String paramString);

	public List<FinanceCommonCfg> getCommonCfgByType(String paramString, boolean paramBoolean);

	public String getRequestUrl(String ipCfgCode, String urlSuffix);
}
