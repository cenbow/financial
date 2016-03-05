package com.mobanker.financial.job.task;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.financial.job.service.RepairBatchPayToCardService;

/**
 * 代付到卡修复
 * 
 * @author yinyafei
 *
 */
@Component
public class RepairBatchPayToCardTask {

	@Resource
	private RepairBatchPayToCardService repairBatchPayToCardService;
	
	public void doRepair(){
		
		repairBatchPayToCardService.doRepair();
	}
}
