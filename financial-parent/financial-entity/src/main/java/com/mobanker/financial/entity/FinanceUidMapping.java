package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_uid_mapping")
public class FinanceUidMapping extends BaseEntity{
	
	private static final long serialVersionUID = 4005460025481256422L;

	private String uid;

    private String customerId;

    private String name;

    private String certNo;

}