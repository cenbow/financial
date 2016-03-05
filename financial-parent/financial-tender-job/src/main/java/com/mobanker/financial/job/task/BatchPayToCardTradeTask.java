package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.BatchPayToCardTradeService;

/**
 * Description:批量代付到卡 将招标的money提现到理财人的银行卡
 * 
 * @author yinyafei
 *
 */
@Component
public class BatchPayToCardTradeTask {

	@Resource
	private BatchPayToCardTradeService batchPayToCardTradeService;

	public void batchPayToCardTrade() {

		batchPayToCardTradeService.bathPayToCardTrade();
	}
}
