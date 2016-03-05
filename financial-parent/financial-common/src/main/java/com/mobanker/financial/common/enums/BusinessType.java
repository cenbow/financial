package com.mobanker.financial.common.enums;

public enum BusinessType {

	/**投资 */
	INVEST("1"),
	/**回本 */
	BACK_AMOUNT("2"),
	/**回息*/
	BACK_INTEREST("3");
	
	private String type;
	
	private BusinessType(String type){
		this.type = type;
	}
	
	public String toString(){
		return this.type;
	}
	
	public static void main(String[] args) {
		System.out.println(BusinessType.BACK_AMOUNT);
	}
}
