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
@Table(name="finance_refund_statistics")
public class FinanceRefundStatistics extends BaseEntity{
	
	private static final long serialVersionUID = -3448036191072518950L;

	private Date planRefundTime;

    private String financialUser;

    private BigDecimal principal;

    private BigDecimal interest;

    private BigDecimal totalAmount;

    private BigDecimal bankFee;

    private String docMaker;

    private Date docTime;

    private String checkUser;

    private String checkStatus;

    private String bankCardNo;

    private BigDecimal cardSinaFee;

    private String docStatus;

    private String orderNo;

    private String borrowerRechargeStatus;

    private String borrowerRechargeOrderNo;
}