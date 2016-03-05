package com.mobanker.financial.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Table(name = "finance_report_collect")
public class FinanceReportCollect extends BaseEntity{
	
	private static final long serialVersionUID = -2986868936005318351L;
	//标的号
	private String tenderNo;
	//理财周期
	private Integer timeLimit;
	//投资人
	private String borrowerName;
	//理财人
	private String investorName;
	//投资人手机号
	private String phone;
	//订单类型
	private String orderType;
	//理财金额
	private BigDecimal amount;
	//实收日期
	private Date actualDate;
	//理财开始
	private Date financeBegin;
	//理财结束
	private Date financeEnd;
	//基础利率
	private BigDecimal basicYield;
	//变动利率
	private BigDecimal changeYield;
	//合计利率
	private BigDecimal totalYield;
	//一期计划还款日
	private Date onePlanRefundDate;
	//一期应付本金
	private BigDecimal onePrincipal;
	//一期应付利息
	private BigDecimal oneInterest;
	//二期计划还款日
	private Date twoPlanRefundDate;
	//二期应付本金
	private BigDecimal twoPrincipal;
	//二期应付利息
	private BigDecimal twoInterest;
	//三期计划还款日
	private Date threePlanRefundDate;
	//三期应付本金
	private BigDecimal threePrincipal;
	//三期应付利息
	private BigDecimal threeInterest;
	//一月起始日期
	private Date janBegin;
	//一月周期
	private Integer janCycle;
	//一月应记利息
	private BigDecimal janInterest;
	//二月
	private Date febBegin;
	private Integer febCycle;
	private BigDecimal febInterest;
	//三月
	private Date marBegin;
	private Integer marCycle;
	private BigDecimal marInterest;
	//四月
	private Date aprBegin;
	private Integer aprCycle;
	private BigDecimal aprInterest;
	//五月
	private Date mayBegin;
	private Integer mayCycle;
	private BigDecimal mayInterest;
	//六月
	private Date junBegin;
	private Integer junCycle;
	private BigDecimal junInterest;
	//七月
	private Date julBegin;
	private Integer julCycle;
	private BigDecimal julInterest;
	//八月
	private Date augBegin;
	private Integer augCycle;
	private BigDecimal augInterest;
	//九月
	private Date septBegin;
	private Integer septCycle;
	private BigDecimal septInterest;
	//十月
	private Date octBegin;
	private Integer octCycle;
	private BigDecimal octInterest;
	//十一月
	private Date novBegin;
	private Integer novCycle;
	private BigDecimal novInterest;
	//十二月
	private Date decBegin;
	private Integer decCycle;
	private BigDecimal decInterest;
}
