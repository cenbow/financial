package com.mobanker.financial.common.constants;

/**
 * 财务系统
 * 
 * @author yinyafei
 * 
 *         2015.11.18
 *
 */
public interface FinanceConstants {


	/**
	 * 财务还款常量
	 */
	public interface RepayConstant {

		public static final String COLLECTION_INTEREST = "代收利息";

		public static final String COLLECTION_PRINCIPAL = "代收利息";
	}

	/**
	 * 交易码
	 */
	public interface TradeCodeConstant {
		
		public static final String TRADE_CODE_COLLECT_INVESTMENT = "1001";
		public static final String TRADE_CODE_COLLECT_INVESTMENT_SUMMARY = "代收投资";

		public static final String TRADE_CODE_COLLECT_REFUND = "1002";
		public static final String TRADE_CODE_COLLECT_REFUND_SUMMARY = "代收还款";

		public static final String TRADE_CODE_PAY_BORROWING = "2001";
		public static final String TRADE_CODE_PAY_BORROWING_SUMMARY = "代付借款";

		public static final String TRADE_CODE_PAY_PRINCIPAL_EARNINGS = "2002";
		public static final String TRADE_CODE_PAY_PRINCIPAL_EARNINGS_SUMMARY1 = "代付本金收益";
		
		public static final String TRADE_PAY_PRINCIPAL_SUMMARY = "代付本金";
		public static final String TRADE_PAY_EARNINGS_SUMMARY = "代付收益";
	}
}
