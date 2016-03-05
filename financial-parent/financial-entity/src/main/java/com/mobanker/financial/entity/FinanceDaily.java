package com.mobanker.financial.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 理财日报实体类
 * @author zhanghaifeng
 */

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_report_daily")
public class FinanceDaily extends BaseEntity{
	
	private static final long serialVersionUID = 6460145254726614730L;
	
	private Date dailyDate;
	
	private BigDecimal actiReg;						//激活到注册转化率  ACTI_TO_REG
	
	private BigDecimal regDayRatio;						//注册日环比增长
	
	private BigDecimal regInvest;						//注册到投资转化
	
	private BigDecimal tenderFinish;					//标的完成比例
	
	private BigDecimal tenderFinishDayRatio;			//标的完成比例日环比
	
	private BigDecimal bidOldCycle;						//投标老用户占比
	
	private BigDecimal bidNewCycle;						//投标新用户占比
	
	private BigDecimal depoDayRatio;					//充值用户日环比
	
	private BigDecimal deponumDayRatio;					//充值额日环比
	
	private BigDecimal depoArpu;						//充值arpu
	
	private BigDecimal depoArpuDayRatio;				//充值arpu日环比
	
	private BigDecimal investArpu;						//投资arpu
	
	private BigDecimal investHoldDepo;					//投资用户占充值用户比
	
	private BigDecimal tendernumHoldDeponum;			//标的额占充值额比   TENDER_NUM_HOLD_DEPO_NUM
	
	private BigDecimal tendernumHoldDeponumDayRatio;	//标的额占充值额比环比
	
	private BigDecimal investnumHoldDeponum;			//投资额占充值额比
	
	private BigDecimal investnumHoldDeponumDayRatio;	//投资额占充值额比环比
	
	private BigDecimal newActi;                         //新增激活
	
	private BigDecimal newReg;                          //新增所有注册
	
	private BigDecimal newFinanceReg;                   //新增理财注册
	
	private BigDecimal newInvest;                       //新增投资用户
	
	private BigDecimal newInvestAmount;                 //新增投资金额
	
	private BigDecimal oldInvest;                       //老投资用户
	
	private BigDecimal oldInvestAmount;                 //老投资金额
	
	private BigDecimal tenderAmount;                    //当日标的额
	
	private BigDecimal tenderFinishAmount;              //完成标的额
	
	private BigDecimal totalInvestCount;                //累计投资用户
	
	private BigDecimal totalInvestAmount;               //累计投资金额
	
	private BigDecimal totalTenderAmount;               //累计标的额
	
	private BigDecimal depositCount;                    //充值用户
	
	private BigDecimal depositAmount;                   //充值额
	
	private BigDecimal withdrawCount;                   //提现用户数
	
	private BigDecimal withdrawAmount;                  //用户提现金额
	
	private BigDecimal investCount;                     //投资用户
	
	private BigDecimal investAmount;                    //投资金额
	
	private BigDecimal totalDepositCount;               //累计充值用户
	
	private BigDecimal totalDepositAmount;              //累计充值额
	
	private BigDecimal iosActi;                         //ios激活量
	
	private BigDecimal iosReg;                          //ios注册量
	
	private BigDecimal iosDeposit;                      //ios充值用户
	
	private BigDecimal iosDepositAmount;                //ios充值额
	
	private BigDecimal iosInvest;                       //ios投资用户
	
	private BigDecimal iosInvestAmount;                 //ios投资金额
	
	private BigDecimal iosTotalInvest;                  //iOS累计投资用户
	
	private BigDecimal iosTotalInvestAmount;            //ios累计投资金额
	
	private BigDecimal iosNewRegDayRatio;			    //ios新增注册日环比增长
	
	private BigDecimal iosNewDepoDayRatio;				//ios新增充值用户日环比  IOS_NEW_DEPO_NUM_DAY_RATIO
	
	private BigDecimal iosNewRegDepo;					//ios新增注册到充值转化
	
	private BigDecimal iosNewRegDepoDayRatio;			//ios新增注册到充值转化环比
	
	private BigDecimal iosNewDeponumDayRatio;			//ios新增充值额日环比
	
	private BigDecimal androidActi;                         //ios激活量
	
	private BigDecimal androidReg;                          //ios注册量
	
	private BigDecimal androidDeposit;                      //ios充值用户
	
	private BigDecimal androidDepositAmount;                //ios充值额
	
	private BigDecimal androidInvest;                       //ios投资用户
	
	private BigDecimal androidInvestAmount;                 //ios投资金额
	
	private BigDecimal androidTotalInvest;                  //iOS累计投资用户
	
	private BigDecimal androidTotalInvestAmount;            //ios累计投资金额
	
	private BigDecimal androidNewRegDayRatio;			    //ios新增注册日环比增长
	
	private BigDecimal androidNewDepoDayRatio;				//ios新增充值用户日环比
	
	private BigDecimal androidNewRegDepo;					//ios新增注册到充值转化
	
	private BigDecimal androidNewRegDepoDayRatio;			//ios新增注册到充值转化环比
	
	private BigDecimal androidNewDeponumDayRatio;			//ios新增充值额日环比
	
	private BigDecimal savingpotAvailableBalance;           //存钱罐累计可以余额
	
	private BigDecimal iosFinanceReg;                       //ios理财注册量
	
	private BigDecimal androidFinanceReg;                   //android理财注册


}
