package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.ReportRefoundStatisticsServcie;


/**
 * Description: 还款统计
 * 
 * @author yinyafei
 * @date 2015/7/23
 */
@Component
public class RefoundStatisticsTask {
	@Resource
	private ReportRefoundStatisticsServcie refoundStatisticsServcie;
	
	public void refoundStatistics(){
		
		refoundStatisticsServcie.refoundStatistics();
	}
}
