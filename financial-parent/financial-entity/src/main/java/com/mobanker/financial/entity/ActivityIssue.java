package com.mobanker.financial.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.mobanker.framework.entity.BaseEntity;

/**
 * 奖品发放记录表
 * @author gaoguoxiang
 * @date Sep 21, 2015 10:38:33 AM
 */

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="activity_issue")
public class ActivityIssue extends BaseEntity {
	
	private static final long serialVersionUID = -4687965780908299363L;
	
	private String takerPhone; //领取人手机号
	private String takerId; //领取人ID；
	private String inviterPhone; //邀请人手机号
	private String inviterId; //邀请人ID
	private String rewardId; //奖品ID
	private String ruleId; //规则ID
	private String reason; //发放原因
	private String status; //状态
	private BigDecimal elementInvestAmount; //投资金额因素
	private Date issueTime; //奖品发放时间
	private String serialNum; //发送流水号
	private BigDecimal elementEarningsAmount; //收益
	private String activity;
	
}
