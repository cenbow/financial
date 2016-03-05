package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.TenderReleaseService;

/**
 * Description:定时发布标的任务
 * 
 * @author yinyafei
 * @date 2015/7/14
 */
@Component
public class TenderReleaseTask {

	@Resource
	private TenderReleaseService tenderReleaseService;

	public void tenderRelease() {
		// 发布标的
		tenderReleaseService.tenderRelease();
	}
}
