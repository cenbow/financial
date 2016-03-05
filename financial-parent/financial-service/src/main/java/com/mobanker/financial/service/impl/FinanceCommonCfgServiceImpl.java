package com.mobanker.financial.service.impl;

import com.mobanker.financial.entity.FinanceCommonCfg;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.framework.service.impl.BaseServiceImpl;
import com.mobanker.framework.spring.property.PropertyPlaceholderConfigurer;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FinanceCommonCfgServiceImpl extends BaseServiceImpl<FinanceCommonCfg> implements FinanceCommonCfgService {
	
	public String getCodeValueById(String id) {
		String value = "";
		FinanceCommonCfg commonCfg = (FinanceCommonCfg) getById(id);
		if ((commonCfg != null) && (commonCfg.getIsDeleted().equals("1"))) {
			value = commonCfg.getValue();
		}
		return value;
	}

	public FinanceCommonCfg getCommonCfgById(String id) {
		FinanceCommonCfg commonCfg = (FinanceCommonCfg) getById(id);
		if ((commonCfg != null) && (commonCfg.getIsDeleted().equals("1"))) {
			return commonCfg;
		}
		return null;
	}

	public FinanceCommonCfg getCommonCfgByCode(String code) {
		FinanceCommonCfg findParams = new FinanceCommonCfg();
		findParams.setCode(code);
		findParams.setIsDeleted("1");
		List<FinanceCommonCfg> commonCfgList = getByObj(findParams);
		if (commonCfgList.size() > 0) {
			return (FinanceCommonCfg) commonCfgList.get(0);
		}
		return null;
	}

	public String getCommonCfgValueByCode(String code) {
		String value = "";
		if (StringUtils.isEmpty(value)) {
			FinanceCommonCfg findParams = new FinanceCommonCfg();
			findParams.setCode(code);
			findParams.setIsDeleted("1");
			List<FinanceCommonCfg> commonCfgList = getByObj(findParams);
			if (commonCfgList.size() > 0) {
				value = ((FinanceCommonCfg) commonCfgList.get(0)).getValue();
			}
		}
		return value;
	}

	public List<FinanceCommonCfg> getCommonCfgByType(String type) {
		FinanceCommonCfg findParams = new FinanceCommonCfg();
		findParams.setType(type);
		findParams.setIsDeleted("1");
		return getByObj(findParams);
	}

	public List<FinanceCommonCfg> getCommonCfgByType(String type, boolean deleteData) {
		FinanceCommonCfg findParams = new FinanceCommonCfg();
		findParams.setType(type);
		if (deleteData)
			findParams.setIsDeleted("0");
		else {
			findParams.setIsDeleted("1");
		}
		return getByObj(findParams);
	}

	@Override
	public String getRequestUrl(String ipCfgCode, String urlSuffix) {

		String ipAddress = getCommonCfgValueByCode(ipCfgCode);
		String value = (String) PropertyPlaceholderConfigurer.getContextProperty(urlSuffix);
		return ipAddress + value;
	}
}
