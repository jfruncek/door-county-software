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

package org.efs.openreports.providers.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.ScheduleType;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.SchedulerProvider;
import org.efs.openreports.util.ScheduledReportJob;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class SchedulerProviderImpl implements SchedulerProvider {
    private static final String STARK_OPEN_REPORTS = "Stark Open Reports";

    private static final String RUN_STATUS_MONITOR = "Run Status Monitor";

    protected static Logger log = Logger.getLogger( SchedulerProviderImpl.class.getName() );

    private Scheduler scheduler;

    public SchedulerProviderImpl( Scheduler scheduler ) {
        log.info( "SchedulerProviderImpl created." );
        this.scheduler = scheduler;
    }

    public void scheduleReport( ReportSchedule reportSchedule ) throws ProviderException {
        String jobGroup = reportSchedule.getJobGroup();
        JobDetail jobDetail = new JobDetail( reportSchedule.getScheduleName(), jobGroup, ScheduledReportJob.class );
        jobDetail.getJobDataMap().put( ORStatics.REPORT_SCHEDULE, reportSchedule );
        jobDetail.setDescription( reportSchedule.getScheduleDescription() );

        ScheduleType scheduleType = ScheduleType.findByCode( reportSchedule.getScheduleType() );

        if( scheduleType == ScheduleType.NONE ) {
            /*
             * Schedule type NONE means just store the job detail information. Durability must be
             * true for any job not associated with a trigger. see
             * http://quartz.sourceforge.net/javadoc/org/quartz/JobDetail.html
             */
            jobDetail.setDurability( true ); // 
            try {
                scheduler.addJob( jobDetail, true );
            } catch( SchedulerException e ) {
                throw new ProviderException( e );
            }
        } else if( scheduleType == ScheduleType.ONCE ) {
            SimpleTrigger trigger =
                    new SimpleTrigger( reportSchedule.getScheduleName(), jobGroup, reportSchedule.getStartDateTime(),
                            null, 0, 0L );
            trigger.setPriority( reportSchedule.getSchedulePriority() );
            trigger.getJobDataMap().put( reportSchedule.getScheduleName(), reportSchedule.getRequestId() );

            try {
                scheduler.scheduleJob( jobDetail, trigger );
            } catch( SchedulerException e ) {
                throw new ProviderException( e );
            }
        } else { // All the rest are actually CRON-BASED Schedule types
            StringBuffer cronExpression = new StringBuffer();

            if( scheduleType == ScheduleType.CRON ) {
                cronExpression.append( reportSchedule.getCronExpression() );
            } else {
                cronExpression.append( "0 " );
                cronExpression.append( reportSchedule.getStartMinute() );
                cronExpression.append( " " );
                cronExpression.append( reportSchedule.getAbsoluteStartHour() );

                if( scheduleType == ScheduleType.HOURLY ) {
                    cronExpression.append( "-" + reportSchedule.getAbsoluteEndHour() );
                }

                if( scheduleType == ScheduleType.WEEKLY ) {
                    cronExpression.append( " ? * " );
                    cronExpression.append( reportSchedule.getDayOfWeek() );
                } else if( scheduleType == ScheduleType.MONTHLY ) {
                    cronExpression.append( " " + reportSchedule.getDayOfMonth() );
                    cronExpression.append( " * ? " );
                } else if( scheduleType == ScheduleType.WEEKDAYS ) {
                    cronExpression.append( " ? * MON-FRI" );
                } else {
                    cronExpression.append( " * * ?" );
                }
            }

            CronTrigger cronTrigger = new CronTrigger( reportSchedule.getScheduleName(), jobGroup );
            try {
                cronTrigger.setCronExpression( cronExpression.toString() );
            } catch( ParseException pe ) {
                throw new ProviderException( pe );
            }

            cronTrigger.setStartTime( reportSchedule.getStartDateTime() );
            cronTrigger.setPriority( reportSchedule.getSchedulePriority() );
            cronTrigger.getJobDataMap().put( reportSchedule.getScheduleName(), reportSchedule.getRequestId() );

            try {
                scheduler.scheduleJob( jobDetail, cronTrigger );
            } catch( SchedulerException e ) {
                throw new ProviderException( e );
            }
        }
    }

    public List<String> getJobGroups() throws ProviderException {
        try {
            String[] jobGroupNames = scheduler.getJobGroupNames();
            return Arrays.asList( jobGroupNames );
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    public List<ReportSchedule> getScheduledReports( String jobGroup ) throws ProviderException {

        List<ReportSchedule> scheduledReports = new ArrayList<ReportSchedule>();

        try {
            String[] jobNames = scheduler.getJobNames( jobGroup );

            for( int i = 0; i < jobNames.length; i++ ) {
                try {
                    JobDetail jobDetail = scheduler.getJobDetail( jobNames[i], jobGroup );

                    if( jobDetail.getJobClass().getName().equals( ScheduledReportJob.class.getName() ) ) {
                        ReportSchedule reportSchedule =
                                (ReportSchedule) jobDetail.getJobDataMap().get( ORStatics.REPORT_SCHEDULE );
                        reportSchedule.setScheduleDescription( jobDetail.getDescription() );
                        reportSchedule.setScheduleState( getTriggerStateName( jobNames[i], jobGroup ) );

                        Trigger trigger = scheduler.getTrigger( jobNames[i], jobGroup );
                        if( trigger != null ) {
                            reportSchedule.setNextFireDate( trigger.getNextFireTime() );
                        }

                        scheduledReports.add( reportSchedule );
                    }
                } catch( ProviderException pe ) {
                    log.error( jobGroup + " | " + jobNames[i] + " | " + pe.toString() );
                }
            }
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }

        return scheduledReports;
    }

    public void deleteScheduledReport( String jobGroup, String name ) throws ProviderException {
        try {
            scheduler.deleteJob( name, jobGroup );
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    public ReportSchedule getScheduledReport( String jobGroup, String name ) throws ProviderException {
        try {
            JobDetail jobDetail = scheduler.getJobDetail( name, jobGroup );
            if( jobDetail == null ) {
                throw new ProviderException( "No Report Schedule for the given name: " + name + " and job group: "
                        + jobGroup );
            }

            ReportSchedule reportSchedule = (ReportSchedule) jobDetail.getJobDataMap().get( ORStatics.REPORT_SCHEDULE );
            reportSchedule.setScheduleDescription( jobDetail.getDescription() );
            reportSchedule.setScheduleState( getTriggerStateName( name, jobGroup ) );

            Trigger trigger = scheduler.getTrigger( name, jobGroup );
            if( trigger != null ) {
                reportSchedule.setNextFireDate( trigger.getNextFireTime() );
            }

            return reportSchedule;
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    public void pauseScheduledReport( String jobGroup, String name ) throws ProviderException {
        try {
            scheduler.pauseJob( name, jobGroup );
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    public boolean isPaused( String jobGroup, String name ) throws ProviderException {
        try {
            Trigger[] triggers = scheduler.getTriggersOfJob( name, jobGroup );
            for( Trigger trigger : triggers ) {
                int triggerState = scheduler.getTriggerState( trigger.getName(), trigger.getGroup() );
                if( triggerState != Trigger.STATE_PAUSED ) {
                    return false;
                }
            }
            return true;
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    public void resumeScheduledReport( String jobGroup, String name ) throws ProviderException {
        try {
            scheduler.resumeJob( name, jobGroup );
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }
    }

    private String getTriggerStateName( String name, String jobGroup ) throws ProviderException {
        int state = -1;

        try {
            state = scheduler.getTriggerState( name, jobGroup );
        } catch( SchedulerException e ) {
            throw new ProviderException( e );
        }

        switch( state ) {
        case Trigger.STATE_BLOCKED:
            return "Blocked";

        case Trigger.STATE_COMPLETE:
            return "Complete";

        case Trigger.STATE_ERROR:
            return "ERROR";

        case Trigger.STATE_NORMAL:
            return "Normal";

        case Trigger.STATE_PAUSED:
            return "Paused";

        default:
            return "";
        }
    }
}