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
@Table(name="finance_repay_plan_detail")
public class FinanceRepayPlanDetail extends BaseEntity{
	
	private static final long serialVersionUID = -7097022316089318312L;

	private String rpid;

    private BigDecimal changeRate;

    private BigDecimal interestPayable;

    private BigDecimal principalPayable;

    private BigDecimal refundAmount;

    private BigDecimal earnings;

    private BigDecimal fee;

    private Integer status;

    private String description;

    private Integer period;

    private Date refundTime;

    private Date refundSuccessTime;

    private String orderNo;

    private BigDecimal cardSinaFee;

    private String uid;

    private String tid;

    private String sid;

    private String refundOrderNo;

    private BigDecimal interestActual;

    private BigDecimal principalActual;
}