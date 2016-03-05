package com.mobanker.financial.job.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobanker.financial.job.service.RepaySwitchService;
import com.mobanker.financial.job.service.RepaymentService;
import com.mobanker.framework.context.ContextUtils;

/**
 * Description: 自动还款执行内容
 * 
 * @author yinyafei
 * 2015-12-17
 */
public class RepayJobDetail implements Job {

	private static final Logger logger = LoggerFactory.getLogger(RepayJobDetail.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		RepaymentService repaymentService = (RepaymentService) ContextUtils.getBean(RepaymentService.class);

		RepaySwitchService repaySwitchService = (RepaySwitchService) ContextUtils.getBean(RepaySwitchService.class);
		boolean flag = repaySwitchService.isAutoRepay();
		if (flag) {
			
			logger.debug("----------已开启自动还款,扫描ing~");
			repaymentService.autoRepay();
		}
	}
}
