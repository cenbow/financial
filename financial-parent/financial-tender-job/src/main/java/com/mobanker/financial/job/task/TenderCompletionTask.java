package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.TenderCompletionService;

/**
 * Description:招标完成
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Component
public class TenderCompletionTask {

	@Resource
	private TenderCompletionService completionService;

	public void investorTenderEnd() {

		completionService.tenderComplete();
	}
}
