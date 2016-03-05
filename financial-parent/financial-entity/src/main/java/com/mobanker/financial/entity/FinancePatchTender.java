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
@Table(name="finance_patch_tender")
public class FinancePatchTender extends BaseEntity{
	
	private static final long serialVersionUID = 1714657593450561219L;

	private String uid;

    private String sid;

    private BigDecimal amount;

    private String couponId;

    private BigDecimal finalYield;

    private Date addTime;

    private Date bearingTime;

    private Date expireTime;

    private BigDecimal earnedIncome;

    private BigDecimal nextIncome;

    private BigDecimal expectIncome;

    private String orderNo;
    
    private String deleted;
}