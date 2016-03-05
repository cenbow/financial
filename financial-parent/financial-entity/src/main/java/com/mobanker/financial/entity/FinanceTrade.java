package com.mobanker.financial.entity;

import java.math.BigDecimal;

import javax.persistence.Table;
import javax.persistence.Transient;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_trade")
public class FinanceTrade extends BaseEntity{
	
	private static final long serialVersionUID = -860586487386438160L;

	private String tenderId;

    private String orderNo;

    private String uid;

    private BigDecimal amount;

    private String tradeType;

    private String businessType;

    private String status;

    private String batchPayNo;
    
    private String response;

    @Transient
    private String callback;
}