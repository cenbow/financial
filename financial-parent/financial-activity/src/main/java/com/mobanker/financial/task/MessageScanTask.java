package com.mobanker.financial.task;

import java.util.Date;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.service.FinanceCommonCfgService;
import com.mobanker.financial.service.MessageScanService;

/**
 * 消息扫描发送
 * 
 * @author yinyafei
 *
 */
@Component
public class MessageScanTask implements ApplicationListener<ContextRefreshedEvent>{

	private static final Logger logger = LoggerFactory.getLogger(MessageScanTask.class);
	
	@Resource
	private MessageScanService messageScanService;
	@Resource
	private FinanceCommonCfgService commonCfgService;
	@Resource
	private SchedulerFactoryBean schedulerFactory;
	
	/**
	 * 构建任务
	 */
	public void buildJob() {
		
		Scheduler scheduler = schedulerFactory.getScheduler();

		JobDetail job = JobBuilder.newJob(MessageJobDetail.class).withIdentity("messageScan").build();
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("messageScanTrigger")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/10 * * * ?")).build();

		try {
			Date date = scheduler.scheduleJob(job, trigger);
			logger.debug("自动还款计划开始执行时间：{}", DateUtils.convert(date, null));
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 暂停消息扫描
	 */
	private void pauseMessageScan() {

		Scheduler scheduler = schedulerFactory.getScheduler();

		JobKey jobKey = JobKey.jobKey("messageScan");
		try {
			scheduler.pauseJob(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 恢复消息扫描
	 */
	private void resumeMessageScan() {

		Scheduler scheduler = schedulerFactory.getScheduler();
		try {

			if (scheduler.isStarted()) {
				JobKey jobKey = JobKey.jobKey("messageScan");
				scheduler.resumeJob(jobKey);
			} else {
				buildJob();
				scheduler.start();
			}

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 消息控制
	 */
	public void controlMessageScan(String flag) {

		if (flag.equals("ON")) {
			// 恢复消息
			resumeMessageScan();
		} else if (flag.equals("OFF")) {
			// 暂停消息
			pauseMessageScan();
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		buildJob();
	}
}
