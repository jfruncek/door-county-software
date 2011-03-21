package org.efs.openreports.scheduler;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RunStatusMonitorJob implements Job {
    protected static Logger log = Logger.getLogger( RunStatusMonitorJob.class.getName() );

    
    public void execute( JobExecutionContext context ) throws JobExecutionException {
        log.info( "RunStatusRegistry Monitor Quartz job still executing. This has been superceded by th RunStatusMonitorTask spring bean"
                + "delete the qrtz_job_details and the qrtz_cron_trogger named 'Run Status Monitor'" );

    }

}
