package com.mobanker.financial.common.constants;

/**
 * 消息中心
 * 
 * @author yinyafei
 *
 */
public interface MessageContants {

	public interface SendMessageCode {

		/**
		 * 发送系统消息:走消息模板的方式
		 */
		public static final String SEND_SYS_NOCODE = "sendSysNoCode";
		/**
		 * 发送系统消息:不走消息模板的方式
		 */
		public static final String SEND_REMIND = "sendRemind";
		
		
		public static final String SEND_NO_CODE = "sendNoCode";
	}
	
	public interface TepmlateNid {
		/**
		 * 实名认证
		 */
		public static final String REALNAME_CERT_SUCCESS = "lc_realname_certification_success";
		/**
		 * 充值中?
		 */
		public static final String RECHARGE_PROCESSING = "lc_recharge_processing";
		/**
		 * 充值成功
		 */
		public static final String RECHARGE_SUCCESS = "lc_recharge_success";
		/**
		 * 充值失败
		 */
		public static final String RECHARGE_FAIL = "lc_recharge_fail";
		/**
		 * 投标冻结
		 */
		public static final String TENDER_FREEZE = "lc_tender_freeze";
		/**
		 * 投标成功
		 */
		public static final String TENDER_SUCCESS = "lc_tender_success";
		
		/**
		 * 投标成功短信
		 */
		public static final String TENDER_SUCCESS_SMS = "lc_tender_success_sms";
		/**
		 * 回本
		 */
		public static final String TENDER_INTEREST = "lc_tender_interest";
		/**
		 * 回息
		 */
		public static final String TENDER_PRINCIPAL = "lc_tender_principal";
		/**
		 * 提现中
		 */
		public static final String WITHDRAW_PROCESSING = "lc_withdraw_processing";
		/**
		 * 提现成功
		 */
		public static final String WITHDRAW_SUCCESS = "lc_withdraw_success";
		/**
		 * 提现失败
		 */
		public static final String WITHDRAW_FAIL = "lc_withdraw_fail";
		/**
		 * 标的到期短信
		 */
		public static final String TENDER_EXPIRED_SMS = "lc_tender_expired_sms";
		/**
		 * 提现成功短信
		 */
		public static final String WITHDRAW_SUCCESS_SMS = "lc_withdraw_success_sms";
		/**
		 * 首次登陆
		 */
		public static final String FIRST_LOGIN = "lc_first_login";
		
		//理财活动002-1510-01放款成功短信
		public static final String LC_ACTIVITY_LOAN_SUCCESS_SMS = "lc_activity_loan_success_sms";
		//理财活动002-1510-01放款成功
		public static final String LC_ACTIVITY_LOAN_SUCCESS = "lc_activity_loan_success";
	}
}
