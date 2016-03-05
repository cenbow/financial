package com.mobanker.financial.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.mobanker.financial.entity.FinanceDepositWithdraw;
import com.mobanker.framework.dao.BaseDao;

public interface FinanceDepositWithdrawDao extends BaseDao<FinanceDepositWithdraw> {

	public FinanceDepositWithdraw getLatestDepostRecord(String uid);
	
	public List<String> getDepositRecordByDate(Date date);
	
	Set<String> getDataByDate(String date);
	
	/**
	 * 获取累计充值用户数量
	 * @return
	 */
	List<String> getTotalDepositNum();
	
	/**
	 * 获取累计用户充值金额
	 * @return
	 */
	BigDecimal getTotalDepositAmount();
	
	/**
	 * 获取某日所有用户充值总金额
	 * @param date
	 * @return
	 */
	BigDecimal getgetRechargeAmount(String uid, String date);
	
	/**
	 * 获取某日提现用户数量
	 * @param date
	 * @return
	 */
	Set<String> getWithdrawNum(String date);
	
	/**
	 * 获取某日提现用户金额
	 * @param date
	 * @return
	 */
	BigDecimal getWithdrawAmount(String uid, String date);
	
	/**
	 * 获取历史充值记录
	 * @param userId
	 * @return
	 */
	Integer getRechargeDateByUidNODate(String userId);
	
	/**
	 * 获取某日ios的充值用户
	 * @param uid
	 * @param date
	 */
	Integer getIOSDepositNum(String uid, String date);
	
	/**
	 * 获取某日 ios用户的充值金额
	 * @param uid
	 * @param date
	 * @return
	 */
	BigDecimal getIOSDepositAmount(String uid, String date);
	
	/**
	 * 
	 * @param uid
	 * @return
	 */
	Integer getIOSNewAndOldDepositNum(String uid);
	
	/**
	 * 获取某日ios新充值用户数量
	 * @param uid
	 * @param date
	 * @return
	 */
	Integer getIOSNewDepositNum(String uid, String date);
	
	/**
	 * 根据uid和date获取用户充值金额
	 * @param uid
	 * @param date
	 * @return
	 */
	BigDecimal getUserRechargeAmount(String uid, String date);
	
}
