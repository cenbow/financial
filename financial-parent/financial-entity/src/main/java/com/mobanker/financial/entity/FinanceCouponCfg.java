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
@Table(name="finance_coupon_cfg")
public class FinanceCouponCfg extends BaseEntity{
	
	private static final long serialVersionUID = -3497273034840700294L;

	private BigDecimal rate;

    private String activity;

    private Date validityBegin;

    private Date validityEnd;

    private Integer couponDays;

    private Date couponBegin;

    private Date couponEnd;
}