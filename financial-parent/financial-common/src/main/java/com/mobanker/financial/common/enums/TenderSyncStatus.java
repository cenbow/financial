package com.mobanker.financial.common.enums;

public enum TenderSyncStatus {

	/**未同步 */
	UNSYNC("0"),
	/**处理中 */
	PROCESSING("1"),
	/**同步完成*/
	SUCCESS("2");
	
	private String status;
	
	private TenderSyncStatus(String status){
		this.status = status;
	}
	
	public String toString(){
		return this.status;
	}
}
