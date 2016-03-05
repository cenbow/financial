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
@Table(name="finance_repay_plan")
public class FinanceRepayPlan extends BaseEntity{
	
	private static final long serialVersionUID = 6454799688962159727L;

	private String sfid;

    private Date nextRefund;

    private BigDecimal interest;

    private BigDecimal principal;

    private String status;

    private String description;
}