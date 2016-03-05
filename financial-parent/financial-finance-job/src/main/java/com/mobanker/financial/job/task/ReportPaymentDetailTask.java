package com.mobanker.financial.job.task;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.job.service.ReportPaymentDetailService;

/**
 * 实付明细报表
 * @author yinyafei
 *
 */
@Component
public class ReportPaymentDetailTask {

	@Resource
	private ReportPaymentDetailService reportPaymentDetailService;

	public void reportPaymentDetail() {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -1);
		String date = DateUtils.convert(cal.getTime(), DateUtils.DATE_FORMAT);
		reportPaymentDetailService.actualPayDetail(date);
	}
}
