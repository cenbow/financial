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
@Table(name="finance_invite_tender_end")
public class FinanceInviteTenderEnd extends BaseEntity{
	
	private static final long serialVersionUID = -3058365758987703424L;

	private String sid;

    private BigDecimal inputAmount;

    private BigDecimal finalPayamount;

    private BigDecimal annualRate;
}