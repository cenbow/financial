package com.mobanker.financial.common.enums;

public enum FreezeType {

	/** 冻结 */
	FREEZE("1"),
	/** 解冻 */
	UNFREEZE("2"),
	/** 失败 */
	FAILED("3");

	private String type;

	private FreezeType(String type) {
		this.type = type;
	}

	public String toString() {
		return this.type;
	}

	public static void main(String[] args) {
		System.out.println(BusinessType.BACK_AMOUNT);
	}

}
