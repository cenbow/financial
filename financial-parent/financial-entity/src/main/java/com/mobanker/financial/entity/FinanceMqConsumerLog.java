package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_mq_consumer_log")
public class FinanceMqConsumerLog extends BaseEntity{
	
	private static final long serialVersionUID = -5230959570548064109L;

	private String name;

    private String isNotify;

    private String notifyStatus;

    private String content;
}