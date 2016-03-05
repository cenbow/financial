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
@Table(name="finance_tender_cfg")
public class FinanceTenderCfg extends BaseEntity{
	
	private static final long serialVersionUID = 8707335021238812568L;

	private String tenderNo;

    private String tenderName;

    private Integer timeLimit;

    private BigDecimal amount;

    private String uid;

    private Date inviteTime;

    private Date finishTime;

    private BigDecimal yield;

    private String bearingType;

    private String refundType;

    private Date beginTime;

    private Date endTime;

    private BigDecimal inputAmount;

    private BigDecimal finalPayamount;

    private String status;

    private Date fullTime;

    private String syncStatus;
}