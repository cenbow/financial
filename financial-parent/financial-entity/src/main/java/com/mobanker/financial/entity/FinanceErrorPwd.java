package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_error_pwd")
public class FinanceErrorPwd extends BaseEntity{
	
	private static final long serialVersionUID = -5318334140986834938L;

	private String uid;

    private Integer errorNum;
}