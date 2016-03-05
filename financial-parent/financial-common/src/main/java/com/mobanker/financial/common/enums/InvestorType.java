package com.mobanker.financial.common.enums;

public enum InvestorType {

	/** 普通投资人 */
	NOMRAL("1"),
	/** vip投资人 */
	VIP("2");

	private String type;

	private InvestorType(String type) {
		this.type = type;
	}

	public String toString() {
		return this.type;
	}
}
