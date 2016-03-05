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
@Table(name="finance_withdraw_cash")
public class FinanceWithdrawCash extends BaseEntity{
	
	private static final long serialVersionUID = 424622644595527071L;

	private String sfid;

    private String orderNo;

    private String withdrawType;

    private String bankCard;

    private BigDecimal fee;

    private BigDecimal actualAmount;

    private Date withdrawTime;

    private String withdrawStatus;

    private BigDecimal transferFee;

    private String transferStatus;

    private String docMaker;

    private Date docTime;
}