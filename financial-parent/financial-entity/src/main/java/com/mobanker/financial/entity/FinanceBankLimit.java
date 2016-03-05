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
@Table(name="finance_bank_limit")
public class FinanceBankLimit extends BaseEntity{
	
	private static final long serialVersionUID = 2049960583732899314L;

	private String bankName;

    private String bankCode;

    private BigDecimal bindingFirst;

    private BigDecimal bindingSingle;

    private BigDecimal bindingDaily;

    private BigDecimal bindingMinimum;

    private Integer isValidateCard;
}