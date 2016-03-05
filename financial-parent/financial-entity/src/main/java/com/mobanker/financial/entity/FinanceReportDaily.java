package com.mobanker.financial.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_report_daily")
public class FinanceReportDaily extends BaseEntity{
	
	private static final long serialVersionUID = -314486436298994298L;

	private Date dailyDate;

    private BigDecimal actiReg;

    private BigDecimal regDayRatio;

    private BigDecimal regInvest;

    private BigDecimal tenderFinish;

    private BigDecimal tenderFinishDayRatio;

    private BigDecimal bidOldCycle;

    private BigDecimal bidNewCycle;

    private BigDecimal depoDayRatio;

    private BigDecimal deponumDayRatio;

    private BigDecimal depoArpu;

    private BigDecimal depoArpuDayRatio;

    private BigDecimal investArpu;

    private BigDecimal investHoldDepo;

    private BigDecimal tendernumHoldDeponum;

    private BigDecimal tendernumHoldDeponumDayRatio;

    private BigDecimal investnumHoldDeponum;

    private BigDecimal investnumHoldDeponumDayRatio;

    private BigDecimal newActi;

    private BigDecimal newReg;

    private BigDecimal newInvest;

    private BigDecimal newInvestAmount;

    private BigDecimal oldInvest;

    private BigDecimal oldInvestAmount;

    private BigDecimal tenderAmount;

    private BigDecimal tenderFinishAmount;

    private BigDecimal totalInvestCount;

    private BigDecimal totalInvestAmount;

    private BigDecimal totalTenderAmount;

    private BigDecimal depositCount;

    private BigDecimal depositAmount;

    private BigDecimal withdrawCount;

    private BigDecimal withdrawAmount;

    private BigDecimal investCount;

    private BigDecimal investAmount;

    private BigDecimal totalDepositCount;

    private BigDecimal totalDepositAmount;

    private BigDecimal iosActi;

    private BigDecimal iosReg;

    private BigDecimal iosDeposit;

    private BigDecimal iosDepositAmount;

    private BigDecimal iosInvest;

    private BigDecimal iosInvestAmount;

    private BigDecimal iosTotalInvest;

    private BigDecimal iosTotalInvestAmount;

    private BigDecimal iosNewRegDayRatio;

    private BigDecimal iosNewDepoDayRatio;

    private BigDecimal iosNewRegDepo;

    private BigDecimal iosNewRegDepoDayRatio;

    private BigDecimal iosNewDeponumDayRatio;

    private BigDecimal androidActi;

    private BigDecimal androidReg;

    private BigDecimal androidDeposit;

    private BigDecimal androidDepositAmount;

    private BigDecimal androidInvest;

    private BigDecimal androidInvestAmount;

    private BigDecimal androidTotalInvest;

    private BigDecimal androidTotalInvestAmount;

    private BigDecimal androidNewRegDayRatio;

    private BigDecimal androidNewDepoDayRatio;

    private BigDecimal androidNewRegDepo;

    private BigDecimal androidNewRegDepoDayRatio;

    private BigDecimal androidNewDeponumDayRatio;
}