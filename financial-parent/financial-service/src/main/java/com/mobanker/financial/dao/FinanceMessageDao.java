package com.mobanker.financial.dao;

import java.util.List;

import com.mobanker.financial.entity.FinanceMessage;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceMessageDao extends BaseDao<FinanceMessage>{

	public List<FinanceMessage> getAllUnSendMessage();
}
