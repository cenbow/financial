package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_sms_verify_log")
public class FinanceSmsVerifyLog extends BaseEntity{
	
	private static final long serialVersionUID = -3890173496717323806L;

	private String uid;

    private String phone;

    private String smsCode;
}