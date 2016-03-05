package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_common_cfg")
public class FinanceCommonCfg extends BaseEntity{
	
	private static final long serialVersionUID = -921755658934030021L;

	private String type;

    private String typeDesc;

    private String code;

    private String codeDesc;

    private String value;

    private String isDeleted;
}