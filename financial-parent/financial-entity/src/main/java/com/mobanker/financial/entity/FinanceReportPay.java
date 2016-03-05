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
@Table(name="finance_report_pay")
public class FinanceReportPay extends BaseEntity{
	
	private static final long serialVersionUID = 228343203960267055L;

	private String tenderNo;

    private Integer timeLimit;

    private String borrowerName;

    private String investorName;

    private String phone;

    private String orderType;

    private BigDecimal amount;

    private Date actualDate;

    private Date financeBegin;

    private Date financeEnd;

    private BigDecimal basicYield;

    private BigDecimal changeYield;

    private BigDecimal totalYield;

    private Date onePlanRefundDate;

    private BigDecimal onePrincipal;

    private BigDecimal oneInterest;

    private Date twoPlanRefundDate;

    private BigDecimal twoPrincipal;

    private BigDecimal twoInterest;

    private Date threePlanRefundDate;

    private BigDecimal threePrincipal;

    private BigDecimal threeInterest;
}