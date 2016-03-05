package com.mobanker.financial.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.mobanker.financial.dao.FinanceMessageDao;
import com.mobanker.financial.entity.FinanceMessage;
import com.mobanker.financial.service.FinanceMessageService;
import com.mobanker.framework.service.impl.BaseServiceImpl;

@Service
public class FinanceMessageServiceImpl extends BaseServiceImpl<FinanceMessage> implements FinanceMessageService{

	@Resource
	private FinanceMessageDao financeMessageDao;
	
	@Override
	public List<FinanceMessage> getAllUnSendMessage() {
		
		return financeMessageDao.getAllUnSendMessage();
	}
}
