/*
 * Copyright (C) 2003 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.efs.openreports.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.concurrent.BlockingThreadPoolTaskExecutor;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportLog;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.UserProvider;
import org.efs.openreports.scheduler.ReportExecutionRunnable;
import org.efs.openreports.scheduler.ReportRunCallback;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ScheduledReportJob implements Job, ApplicationContextAware {
    protected static Logger log = Logger.getLogger( ScheduledReportJob.class.getName() );

    private StarkApplicationContext appContext;
    private List<ReportRunCallback> reportRunCallbacks;
    private ReportProvider reportProvider;
    private UserProvider userProvider;
    private BlockingThreadPoolTaskExecutor reportExecutor;
    
    public ScheduledReportJob() {
    }

    public void execute( JobExecutionContext context ) throws JobExecutionException {
        log.debug( "Scheduled Report Executing...." );

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        ReportSchedule reportSchedule = (ReportSchedule) jobDataMap.get( ORStatics.REPORT_SCHEDULE );
        reportSchedule.setScheduleDescription( context.getJobDetail().getDescription() );

        List<String> messages = new ArrayList<String>();
        boolean reloadsOK = reloadReport( reportSchedule, messages ) & loadUser( reportSchedule, messages );
        if (! reloadsOK ) {
            doCallbacksForUnrunnableReport( reportSchedule, messages );
        } else {
            ReportExecutionRunnable runnable = appContext.getSpringBean( "reportExecutionRunnable", ReportExecutionRunnable.class );
            runnable.setSchedule(reportSchedule );
            reportExecutor.execute( runnable );
        }
    }

    private void doCallbacksForUnrunnableReport( ReportSchedule reportSchedule, List<String> messages ) {
        ReportUser user = reportSchedule.getUser();
        Report report = reportSchedule.getReport();
        ReportLog reportLog = new ReportLog( user, report, new Date() );
        reportLog.setStatus( ReportLog.STATUS_FAILURE );
        reportLog.setMessage( StringUtils.join( messages.iterator(), ", " ));
        reportLog.setEndTime( new Date() );
        reportLog.setExportType( reportSchedule.getExportType() );
        reportLog.setRequestId( reportSchedule.getRequestId() );
        executeCallbacks( reportLog );
    }


    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.appContext = new StarkApplicationContext( applicationContext );
    }

    public void setReportProvider( ReportProvider reportProvider ) {
        this.reportProvider = reportProvider;
    }

    public void setReportExecutor( BlockingThreadPoolTaskExecutor reportExecutor ) {
        this.reportExecutor = reportExecutor;
    }

    public void setUserProvider( UserProvider userProvider ) {
        this.userProvider = userProvider;
    }

    public void setReportRunCallbacks(List<ReportRunCallback> reportRunCallbacks) {
        this.reportRunCallbacks = reportRunCallbacks;
    }

    /*
     * Execute all ScheduledReportCallbacks registered for this job. Callbacks are configured in the
     * Spring bean scheduledReportCallbacks
     */
    private void executeCallbacks( ReportLog reportLog ) {
        if( reportRunCallbacks != null ) {
            for( ReportRunCallback callback : reportRunCallbacks ) {
                callback.callback( reportLog );
            }
        }
    }

    /**
     * Reload the report in the schedule so that we acquire any report configuration changes since
     * the schedule was saved. referenceReport is potentially out of date and is used only as a
     * reference, by name, to the current report.
     * 
     * @param reportSchedule the schedule to be updated
     * @param errorMessages will be added if to iff the report could not be updated.
     * @return true if the report could be loaded, false otherwise. 
     */
    private boolean reloadReport( ReportSchedule reportSchedule, List<String> errorMessages ) {
        if( reportSchedule.getReport() == null ) {
            errorMessages.add( "No report provided" );
            return false;
        }
        
        String reportName = reportSchedule.getReport().getName();
        if( reportName == null ) {
            errorMessages.add( "No report name provided" );
            return false;
        }

        try {
            Report report = reportProvider.getReport( reportName );
            if( report == null ) {
                errorMessages.add( "No report found with name: " + reportName );
                return false;
            }
            reportSchedule.setReport( report );
            return true;
        } catch( ProviderException pe ) {
            errorMessages.add( "Cant load report with name: " + reportName + ": " + pe.getMessage() );
            return false;
        }
    }


    /**
     * Reload the user so that we acquire the user correctly. referenceUser is potentially out of
     * date and is effectively a reference, by name, to the current user.
     * 
     * @return the ReportUser whose name matchers that in refrenceUser.
     * @throws JobExecutionException if it can't load a report with the same name.
     */
    private boolean loadUser( ReportSchedule reportSchedule, List<String> messages ) {
        ReportUser referenceUser = reportSchedule.getUser();
        if( referenceUser == null ) {
            messages.add( "No user provided" );
            return false;
        }

        String reportUserName = referenceUser.getName();
        if( reportUserName == null ) {
            messages.add( "No user name provided" );
            return false;
        }

        try {
            ReportUser reportUser = userProvider.getUser( reportUserName );
            if( reportUser == null ) {
                messages.add( "No user found with name: " + reportUserName );
                return false;
            }
            reportSchedule.setUser( reportUser );
            return true;
        } catch( ProviderException pe ) {
            messages.add( "Cant load user with name: " + reportUserName + ": " + pe.getMessage() );
            return false;
        }
    }


}