package com.mobanker.financial.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.mobanker.financial.entity.FinanceSubmitTender;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceSubmitTenderDao extends BaseDao<FinanceSubmitTender> {
	Set<String> getDataByDate(String date);
	/**
	 * 根据标的号查询标被投了多少钱
	 * @param sid
	 */
	BigDecimal getTotalAmountBySid(String sid);
	
	List<String> getAllInvestUsers();
	
	/**
	 * 获取累计所有投资金额
	 * @return
	 */
	BigDecimal getTotalInvestAmount();
	
	/**
	 * 获取历史投资记录
	 * @return
	 */
	Integer gethistoryInvestNum(String uid);
	
	/**
	 * 获取某日投资用户数量
	 * @param uid
	 * @param date
	 * @return
	 */
	Integer getUserInvestNum(String uid, String date);
	
	Integer getAllTenderUserNum(String date);
	
	/**
	 * 根据和date获取用户投资额
	 * @param uid
	 * @param date
	 */
	BigDecimal getUserAddTotalAmount(String uid, String date);
	
	BigDecimal getAllUserAddTotalAmount(String date);
	
	/**
	 *  获取某日ios投资用数量
	 * @param uid
	 * @param date
	 * @return
	 */
	Integer getIOSInvestNum(String uid, String date);
	
	/**
	 * 获取某日所有ios用户投资金额
	 * @param uid
	 * @param date
	 * @return
	 */
	BigDecimal getIOSInvestAmount(String uid, String date);
	
	/**
	 * 获取ios用户累计投资金额
	 * @param uid
	 * @param date
	 * @return
	 */
	BigDecimal getIOSTotalInvestAmount(String uid, String date);
	
	/**
	 *  获取ios累计投资用数量
	 * @param uid
	 * @param date
	 * @return
	 */
	String getIOSTotalInvestNum(String uid, String date);
	
	/**
	 * 获取所有投资用户
	 * @return
	 */
	Set<String> getChannelTotalInvestNums(String date);
	
	BigDecimal getChannelTotalInvestAmount(String uid, String date);
	
}
