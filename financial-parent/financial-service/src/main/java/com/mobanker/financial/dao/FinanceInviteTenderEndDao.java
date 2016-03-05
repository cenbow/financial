package com.mobanker.financial.dao;

import com.mobanker.financial.entity.FinanceInviteTenderEnd;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceInviteTenderEndDao extends BaseDao<FinanceInviteTenderEnd> {
	
	public FinanceInviteTenderEnd getTenderEndById(String tenderId);

	public FinanceInviteTenderEnd getBySelfId(String id);
}
