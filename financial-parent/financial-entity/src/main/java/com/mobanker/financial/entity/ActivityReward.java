package com.mobanker.financial.entity;

import java.util.Date;

import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.mobanker.framework.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Table(name = "activity_reward")
public class ActivityReward extends BaseEntity {

	private static final long serialVersionUID = 8823987488321734728L;
	private String type; // 奖品类型
	private String value; // 奖品值
	private String description; // 奖品描述
	private Date validateBegin; // 有效期开始
	private Date validateEnd; // 有效期结束
	private String activity; // 活动
	private String sendObject; // 发放对象
}
