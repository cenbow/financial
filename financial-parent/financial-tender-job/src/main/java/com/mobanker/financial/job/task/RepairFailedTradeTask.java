package com.mobanker.financial.job.task;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.job.service.RepairFailedTradeService;

/**
 * 解冻代收修复
 * 
 * @author yinyafei
 *
 */
@Component
public class RepairFailedTradeTask {

	@Resource
	private RepairFailedTradeService repairFailedTradeService;
	
	public void doRepair() {

		String date = DateUtils.convert(new Date(), DateUtils.DATE_FORMAT);
		repairFailedTradeService.doRepair(date);
	}
}
