package com.mobanker.financial.job.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.service.FinanceDailyService;

@Component
public class ReportDailyTask {
	private static final Logger logger = LoggerFactory.getLogger(ReportDailyTask.class);
	@Resource
	private FinanceDailyService financeDailyService;
	
	public void execu() {
		logger.debug("=========>> FinanceDailyTask execu");
		Date convert = new Date();
		Calendar calendar = new GregorianCalendar(); 
		calendar.setTime(convert);
		calendar.add(Calendar.DATE,-1);
		convert = calendar.getTime();
		String string = DateUtils.convert(convert);
		String date2 = string.substring(0,10);
		
		List<String> list = new ArrayList<String>();
		list.add(date2);
		financeDailyService.generatorDaily(list);
	}
}
