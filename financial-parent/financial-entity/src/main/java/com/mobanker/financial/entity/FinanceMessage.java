package com.mobanker.financial.entity;

import java.util.Date;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_message")
public class FinanceMessage extends BaseEntity {

	private static final long serialVersionUID = 1622781274478175409L;

	private String messageType;

	private String nid;

	private String replaceContent;

	private String content;

	private String sendStatus;

	private Date realSendTime;

	private String uid;

	private String phone;

	private String isAllUser;

	private String isNowSend;

	private Date settingSendTime;

	private String serialNum;
}
