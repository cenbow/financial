package com.mobanker.financial.common.constants;

/**
 * 通用常量类
 * 
 * @author yinyafei
 *
 *         2015.11.18
 */
public interface SinaConstants {

	/**
	 * 新浪费率
	 *
	 */
	public interface SinaRate{
		/**
		 * 充值
		 */
		public static final String RECHARGE_RATE = "RECHARGE_RATE";
		/**
		 * 提现
		 */
		public static final String WITHDRAW_RATE = "WITHDRAW_RATE";
	}
	
	public interface SinaUrl {
		/**
		 * 用户实名认证接口
		 * 
		 **/
		public static final String REAL_NAME_CERTIFICATION = "realNameCertification";
		/**
		 * 用户实名认证解绑
		 * */
		public static final String UNBINDING_VERIFY = "unbindingVerify";
		/**
		 * 用户认证信息查询
		 * */
		public static final String QUERY_VERIFY = "queryVerify";
		/**
		 * 绑定银行卡
		 * */
		public static final String BINDING_BANK_CARD = "bindingBankCard";
		/**
		 * 绑定银行卡推进接口
		 * */
		public static final String BINDING_BANKCARD_ADVANCE = "bindingBankCardAdvance";
		/**
		 * 银行卡解绑
		 * */
		public static final String UNBINDING_BANK_CARD = "unbindingBankCard";
		/**
		 * 查询银行卡
		 * */
		public static final String QUERY_BANK_CARD = "queryBankCard";
		/**
		 * 用户余额查询接口
		 */
		public static final String QUERY_BALANCE = "queryBalance";
		/**
		 * 创建托管代收交易
		 */
		public static final String CREATE_HOSTING_COLLECT_TRADE = "createHostingCollectTrade";
		/**
		 * 创建托管代付交易
		 */
		public static final String CREATE_SINGLE_HOSTING_PAYTRADE = "createSingleHostingPayTrade";
		/**
		 * 创建批量托管交易代付
		 */
		public static final String CREATE_BATCH_HOSTING_PAYTRADE = "createBatchHostingPayTrade";
		/**
		 * 用户充值
		 */
		public static final String CREATE_HOSTING_DEPOSIT = "createHostingDeposit";
		/**
		 * 支付推进接口
		 */
		public static final String ADVANCE_HOSTING_PAY = "advanceHostingPay";
		/** 托管提现接口 */
		public static final String CREATE_HOSTING_WITHDRAW = "createHostingWithdraw";
		/**
		 * 托管充值记录查询
		 */
		public static final String QUERY_HOSTING_DEPOSIT = "queryHostingDeposit";
		/**
		 * 托管提现记录查询
		 */
		public static final String QUERY_HOSTING_WITHDRAW = "queryHostingWithdraw";
		/**
		 * 托管交易支付
		 */
		public static final String PAY_HOSTING_TRADE = "payHostingTrade";
		/**
		 * 支付结果查询
		 */
		public static final String QUERY_PAY_RESULT = "queryPayResult";
		/**
		 * 托管交易查询
		 */
		public static final String QUERY_HOSTING_TRADE = "queryHostingTrade";
		/**
		 * 查询收支明细
		 */
		public static final String QUERY_ACCOUNT_DETAILS = "queryAccountDetails";
		/**
		 * 资金冻结
		 */
		public static final String BALANCE_FREEZE = "balanceFreeze";
		/**
		 * 资金解冻
		 */
		public static final String BALANCE_UNFREEZE = "balanceUnfreeze";
		/**
		 * 存钱罐收益率查询
		 */
		public static final String QUERY_FUND_YIELD = "queryFundYield";
		/**
		 * 批量代付到卡
		 */
		public static final String CREATEBATCH_HOSTINGPAYTOCARDTRADE = "createBatchHostingPayToCardTrade";
		/**
		 * 从支付平台获取用户基本信息
		 */
		public static final String GET_AUTHCUSTOMER_BY_UID = "getAuthCustomerByUid";
		/**
		 * 绑定认证信息
		 */
		public static final String BINDING_VERIFY = "bindingVerify";
		/**
		 * 标的录入
		 */
		public static final String CREATE_P2P_HOSTINGBORROWINGTARGET = "createp2pHostingBorrowingTarget";
	}
}
