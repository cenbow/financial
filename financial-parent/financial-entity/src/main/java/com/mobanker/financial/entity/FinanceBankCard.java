package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_bank_card")
public class FinanceBankCard extends BaseEntity{
	
	private static final long serialVersionUID = 2787994491860660148L;

	private String uid;

    private String bankUserName;

    private String bankCode;

    private String bankName;

    private String bankCard;

    private String bankPhone;

    private String cardType;

    private String cardAttribute;

    private String certType;

    private String certNo;

    private String province;

    private String city;

    private String county;

    private String bankBranch;

    private String isVerified;

    private String isBinding;

    private String bindingCardId;

    private String ticket;

    private String isFirstPay;

    private String isSecurityCard;
}