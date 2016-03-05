package com.mobanker.financial.mq;

/**
 * 各个队列主题名字
 * 
 * @author yinyafei
 *
 */
public class ActiveMQNames {

	/**
	 * 系统名称
	 */
	public static final String CW_SYSTEM = "财务系统";

	public static final String ACTIVITY_SYSTEM = "活动平台";

	public static final String ACCOUNT_SYSTEM = "账户平台";

	/**
	 * 渠道
	 */
	public static final String CHAANEL = "SINA";

	/**
	 * 用户还款主题
	 */
	public static final String LC_USE_REFUND_TOPIC = "LC_Use_Refund_Topic";
	/**
	 * 放标队列
	 */
	public static final String TENDER_RELEASE_QUEUE = "LCTenderReleaseQueue";
	/**
	 * 投标成功主题
	 */
	public static final String TENDER_SUCCESS_TOPIC = "LC_Bid_Tender_Topic";
	
	/**
	 * 用户收益主题
	 */
	public static final String LC_USER_INCOME_TOPIC = "LC_USER_INCOME_TOPIC";
	/**
	 * 补标
	 */
	public static final String LC_COMPLETION_TENDER_QUEUE = "LC_Completion_Tender_Queue";
}
