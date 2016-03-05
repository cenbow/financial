package com.mobanker.financial.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
public class DownloadChannelUserCountVo {
	
	private String downloadChannel;
	
	private Integer counts;
	
	private String version;
}
