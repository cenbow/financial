package com.mobanker.financial.dao;

import java.math.BigDecimal;
import java.util.List;

import com.mobanker.financial.entity.FinanceTenderCfg;
import com.mobanker.financial.vo.DailyTenderAmountVo;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceTenderCfgDao extends BaseDao<FinanceTenderCfg> {
	/**
	 * 获取某日所有标的 的金额  放出去的钱
	 * @param date
	 * @return
	 */
	BigDecimal getgetAllTenderAmount(String date);
	
	/**
	 * 获取某日标的一共放了多少钱
	 * @param date
	 * @return
	 */
	List<DailyTenderAmountVo> getDailyTenderTotalAmount(String date);
	
	/**
	 * 获取累计标的金额
	 * @return
	 */
	BigDecimal getTotalTenderAmount();

	/**
	 * 获取标的(无缓存)
	 * @param tenderId
	 * @return
	 */
	FinanceTenderCfg getFinanceTenderCfg(String tenderId);
}
