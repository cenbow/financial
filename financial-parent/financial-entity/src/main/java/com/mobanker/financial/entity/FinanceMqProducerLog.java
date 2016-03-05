package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_mq_producer_log")
public class FinanceMqProducerLog extends BaseEntity{
	
	private static final long serialVersionUID = 2353493610655990722L;

	private String serialNumber;

    private String name;

    private String status;

    private String consumer;

    private String consumerIp;

    private String isRetry;

    private Integer retryNum;

    private Integer hasRetry;

    private String content;
}