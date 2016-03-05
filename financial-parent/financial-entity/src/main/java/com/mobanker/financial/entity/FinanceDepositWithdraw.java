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
@Table(name="finance_deposit_withdraw")
public class FinanceDepositWithdraw extends BaseEntity{
	
	private static final long serialVersionUID = -5474569226864053792L;

	private String orderNo;

    private String uid;

    private BigDecimal amount;

    private String status;

    private String recordType;

    private BigDecimal userFee;

    private BigDecimal merchantFee;

    private String isFinancial;

    private String bankCard;

    private String bankName;
    
    private String response;

    @Transient
    private String callback;
}