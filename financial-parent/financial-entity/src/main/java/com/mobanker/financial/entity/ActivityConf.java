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
@Table(name = "activity_conf")
public class ActivityConf extends BaseEntity {

	private static final long serialVersionUID = -1216146993769376160L;

	private String code; // 活动的代码
	private String name; // 活动的名称
	private Date beginTime; // 活动开始时间
	private Date endTime; // 活动结束时间
	private String fromApp; // 所属应用
	private String activityUrl; // 活动地址
	private String activityImg; // 活动图片地址
	private String status; // 活动状态
}
