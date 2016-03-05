package com.mobanker.financial.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
public class YydDataVo {
	
	private String appVersion;
	private String downloadChannel;
}
