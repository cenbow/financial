package com.mobanker.financial.job.task;

import java.util.Date;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.mobanker.common.utils.DateUtils;
import com.mobanker.financial.job.service.RepaySwitchService;


/**
 * Description:自动还款实现
 * Detail: 8:00-22:00点 每隔5分钟执行
 * 
 * @author yinyafei
 * 
 * 2015-12-17
 */
@Service
public class AutoRepayJob implements ApplicationListener<ContextRefreshedEvent>{
	
	private static final Logger logger = LoggerFactory.getLogger(AutoRepayJob.class);

	@Resource
	private SchedulerFactoryBean schedulerFactory;
	@Resource
	private RepaySwitchService repaySwitchService;

	/**
	 * 构建任务
	 */
	public void buildJob() {
		
		Scheduler scheduler = schedulerFactory.getScheduler();

		JobDetail job = JobBuilder.newJob(RepayJobDetail.class).withIdentity("autoRepayJob").build();
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("autoRepayTrigger")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 8-22 * * ?")).build();

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
	 * 开关控制
	 */
	public void controlAutoRepay(String flag) {
		
		repaySwitchService.turnOffAutoRepay(flag);
	}


	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		// 容器启动后自动执行
		buildJob();
	}
}
