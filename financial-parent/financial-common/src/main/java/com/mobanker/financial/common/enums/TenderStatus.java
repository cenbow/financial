package com.mobanker.financial.common.enums;

/**
 * Description: 标的状态
 * @author yinyafei
 * @date 2015/7/8
 */
public enum TenderStatus {

	/**未投标 */
	UNTENDER("1"),
	/**投标中 */
	TENDERING("2"),
	/**已满标*/
	TENDER_FULL("3"),
	/**收益中 */
	TENDER_INCOMING("4"),
	/**标的完成 */
	TENDER_END("5");
	
	private String status;
	
	private TenderStatus(String status){
		this.status = status;
	}
	
	public String toString(){
		return this.status;
	}
}
