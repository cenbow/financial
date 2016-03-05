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
@Table(name="finance_invest_user")
public class FinanceInvestUser extends BaseEntity{
	
	private static final long serialVersionUID = -3003444326809284442L;

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

    private String userType;
}