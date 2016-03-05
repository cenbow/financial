package com.mobanker.financial.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mobanker.financial.service.MessageScanService;
import com.mobanker.framework.context.ContextUtils;

public class MessageJobDetail implements Job {
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		MessageScanService messageScanService = (MessageScanService) ContextUtils.getBean(MessageScanService.class);
		messageScanService.scan();
	}
}
