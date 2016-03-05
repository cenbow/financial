package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.TendeStatusChangeService;

/**
 * Description:开始收益、标的结束
 * 
 * @author yinyafei
 *
 */
@Component
public class TenderStatusChangeTask {

	@Resource
	private TendeStatusChangeService tendeStatusChangeService;

	public void tenderStatusChange() {

		tendeStatusChangeService.tenderStatusChange();
	}
}
