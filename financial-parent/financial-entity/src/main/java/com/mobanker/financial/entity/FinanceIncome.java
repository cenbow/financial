package com.mobanker.financial.entity;

import java.math.BigDecimal;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_income")
public class FinanceIncome extends BaseEntity{
	
	private static final long serialVersionUID = -7566194728076094431L;

	private String uid;

    private BigDecimal accumulatedIncome;

    private BigDecimal netWorth;

    private BigDecimal savingpotBalance;

    private BigDecimal receivedBenefits;

    private BigDecimal uncollectedRevenue;

    private BigDecimal savingpotEarnings;

    private BigDecimal financialAssets;

    private BigDecimal savingpotAvailable;

    private BigDecimal savingpotMonthEarnings;

    private BigDecimal savingpotYesterdayEarnings;
}