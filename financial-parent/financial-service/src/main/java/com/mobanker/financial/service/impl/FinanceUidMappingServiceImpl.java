package com.mobanker.financial.service.impl;

import com.mobanker.financial.entity.FinanceUidMapping;
import com.mobanker.financial.service.FinanceUidMappingService;
import com.mobanker.framework.service.impl.BaseServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FinanceUidMappingServiceImpl extends BaseServiceImpl<FinanceUidMapping> implements FinanceUidMappingService {

	public String getFinanceUid(String userId) {

		FinanceUidMapping findParams = new FinanceUidMapping();
		findParams.setUid(userId);
		List<FinanceUidMapping> FinanceUidMappingList = getByObj(findParams);
		if (FinanceUidMappingList.size() > 0) {
			return ((FinanceUidMapping) FinanceUidMappingList.get(0)).getCustomerId();
		}
		return userId;
	}

	public String getYYDUid(String customerId) {

		FinanceUidMapping findParams = new FinanceUidMapping();
		findParams.setCustomerId(customerId);
		List<FinanceUidMapping> FinanceUidMappingList = getByObj(findParams);
		if (FinanceUidMappingList.size() > 0) {
			return ((FinanceUidMapping) FinanceUidMappingList.get(0)).getUid();
		}
		return customerId;
	}
}
