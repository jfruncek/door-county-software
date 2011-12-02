package org.efs.openreports.scheduler;

import org.efs.openreports.util.ScheduledReportJob;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringAwareJobFactory implements JobFactory,
		ApplicationContextAware {

	private ApplicationContext applicationContext;
	
	public Job newJob(TriggerFiredBundle triggerFiredBundle) throws SchedulerException {
    	JobDetail jobDetail = triggerFiredBundle.getJobDetail();
		String jobClassName = jobDetail.getJobClass().getName();
    	if ( jobClassName.equals( ScheduledReportJob.class.getName() ) ) {
    		return (Job) applicationContext.getBean("scheduledReportJob");
    	} else if ( jobClassName.equals( RunStatusMonitorJob.class.getName() ) ) {
    		return (Job) applicationContext.getBean("runStatusMonitorJob");
    	} else throw new SchedulerException("Found unsupported job class in Quartz scheduler:" + jobClassName);
    }

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
