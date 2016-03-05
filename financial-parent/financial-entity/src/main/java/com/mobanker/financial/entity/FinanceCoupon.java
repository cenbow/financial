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
@Table(name="finance_coupon")
public class FinanceCoupon extends BaseEntity{
	
	private static final long serialVersionUID = -1715051604980071525L;

	private String uid;

    private BigDecimal rate;

    private Date validityBegin;

    private Date validityEnd;

    private String status;

    private String couponCfgId;

    private String source;
}