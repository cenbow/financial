package com.mobanker.financial.job.task;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.job.service.ReportCollectionDetailService;

/**
 * 实收明细定时任务
 * @author yinyafei
 * @date 2015.8.4
 */
@Component
public class ReportColletionDetailTask {

	@Resource
	private ReportCollectionDetailService daliyActualPaymentDetailService;
	
	public void doActualPaymentDetail() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		String date = DateUtils.convert(cal.getTime(), DateUtils.DATE_FORMAT);
		daliyActualPaymentDetailService.actualCollectionDetail(date);
	}
}
