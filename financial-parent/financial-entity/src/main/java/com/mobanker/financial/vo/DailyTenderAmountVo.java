package com.mobanker.financial.vo;


import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper=true)
public class DailyTenderAmountVo {
	
	private String id;
	private BigDecimal totalAmount;
}
