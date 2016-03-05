package com.mobanker.financial.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.mobanker.framework.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_report_channel")
public class FinanceReportChannel extends BaseEntity {

	private static final long serialVersionUID = 2450765130947197525L;
	
	
	private Date channelDate; //日期
	
	private BigDecimal actiNum; //激活量
	
	private BigDecimal regNum; //所有注册量
	
	private BigDecimal regActiRatio; //注册激活比
	
	private BigDecimal firstDepoNum; //首次充值用户
	
	private BigDecimal regDepoTransform; //注册到充值转化
	
	private BigDecimal newDepoAmount; //新户充值额
	
	private BigDecimal firstInvestNum; //首次投资用户
	
	private BigDecimal regInvestTransform; //注册到投资转化
	
	private BigDecimal newInvestAmount; //新户投资金额
	
	private BigDecimal newInvestArpu; //新户投资arpu
	
	private BigDecimal totalInvestNum; //所有投资用户
	
	private BigDecimal totalInvestAmount; //所有投资金额
	
	private BigDecimal totalInvestArpu; // 投资arpu
	
	private String channel; //渠道
	
	private String appVersion; //版本号
	
	private BigDecimal financeRegNum; //理财注册量
	
	
	

}
