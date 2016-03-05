package com.mobanker.financial.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
public class YydRegisterVo {
	
	private String userId;
	private String registerVersion;
}
