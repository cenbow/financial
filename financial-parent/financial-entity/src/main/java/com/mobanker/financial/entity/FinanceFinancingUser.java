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
@Table(name="finance_financing_user")
public class FinanceFinancingUser extends BaseEntity{
	
	private static final long serialVersionUID = 1717735785045555289L;

	private String uid;

    private String password;

    private Boolean isCertification;

    private Boolean isSetpwd;

    private Date certificationTime;

    private Date setpwdTime;

    private Boolean isReadsina;

    private String name;

    private String phone;

    private String certNo;
}