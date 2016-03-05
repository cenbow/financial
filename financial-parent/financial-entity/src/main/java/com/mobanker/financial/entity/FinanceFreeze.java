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
@Table(name="finance_freeze")
public class FinanceFreeze extends BaseEntity{
	
	private static final long serialVersionUID = 2535798743067445931L;

	private String uid;

    private String submitTenderId;

    private String freezeOrderNo;

    private String unfreezeOrderNo;

    private BigDecimal amount;

    private String optType;
}