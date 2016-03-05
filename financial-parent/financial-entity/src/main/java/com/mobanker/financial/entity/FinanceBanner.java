package com.mobanker.financial.entity;

import javax.persistence.Table;

import com.mobanker.framework.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
@Table(name="finance_banner")
public class FinanceBanner extends BaseEntity{
	
	private static final long serialVersionUID = -6525186740798477692L;

	private String type;

    private String linkUrl;

    private String needLogin;

    private String needCode;

    private String webview;

    private String appType;

    private String picUrl;

    private Integer orders;
}