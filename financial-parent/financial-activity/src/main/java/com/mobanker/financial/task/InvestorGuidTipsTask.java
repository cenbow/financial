package com.mobanker.financial.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.service.InvestorGuidTipsService;

/**
 * 发送引导消息
 * 
 * @author yinyafei
 *
 */
@Component
public class InvestorGuidTipsTask {

	@Resource
	private InvestorGuidTipsService investorGuidTipsService;

	/**
	 * 发送昨日绑卡未充值 、发送昨日充值未投标
	 */
	public void sendYesterdayDataMessage() {

		investorGuidTipsService.sendBindNoRechargeUserMsg();
		investorGuidTipsService.sendRechargeNoSubmitUserMsg();
	}

	/**
	 * 发送历史绑卡未充值、发送历史充值未投标
	 */
	public void sendHistoryDateMessage() {

		investorGuidTipsService.sendBindNoRechargeHistoryUserMsg();
		investorGuidTipsService.sendRechargeNoSubmitHistoryUserMsg();
	}
}
