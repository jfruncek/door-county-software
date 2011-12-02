package org.efs.openreports.engine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.DeliveryMethod;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.ReportConstants.ScheduleType;
import org.efs.openreports.concurrent.BlockingThreadPoolTaskExecutor;
import org.efs.openreports.delivery.DeliverySupport;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.UserProvider;
import org.efs.openreports.scheduler.ReportExecutionRunnable;
import org.efs.openreports.scheduler.ReportRunStatusInit;
import org.efs.openreports.scheduler.notification.RunStatus;
import org.efs.openreports.scheduler.notification.RunStatusRegistry;
import org.efs.openreports.scheduler.notification.RunType;
import org.efs.openreports.util.StarkApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BatchReportProcessor implements ReportProcessor, ApplicationContextAware {
    protected static Logger LOG = Logger.getLogger( BatchReportProcessor.class );

    private static final String BATCH_STATUS = "batch_status";
    private static final String BATCH_STATUS_MESSAGE = "batch_status_message";
    private static final String PARAM_PREFIX = "rp_";

    private StarkApplicationContext appContext;
    private ReportProvider reportProvider;
    private RunStatusRegistry runStatusRegistry;
    private UserProvider userProvider;
    private DeliverySupport deliverySupport;
    private BlockingThreadPoolTaskExecutor reportExecutor;

    public void handleResults( QueryResults queryResults, ReportEngineInput reportInput ) {
        LOG.info( "BatchReportProcessor.handleResults for report: " + reportInput.getReport().getName() );
        RunStatusAcquisition runStatusAcquisition = acquireRunStatus( reportInput );
        RunStatus batchRunStatus = runStatusAcquisition.runStatus;
        List<DynaBeanRunStatusInit> initDatas = createRunStatusInitDatas( queryResults );
        setNotificationRecipients( batchRunStatus, reportInput );

        for( DynaBeanRunStatusInit initData : initDatas ) {
            RunStatus reportRunStatus =
                    runStatusRegistry.addRunStatus( RunType.REPORT, initData.getReportName(), initData
                            .getScheduleDescription(), batchRunStatus );
            initData.setRunStatus( reportRunStatus );
        }
        LOG.info( "BatchReportProcessor.handleResults just before queuing the reports: " + reportInput.getReport().getName() );
        queueReports( initDatas );
        markFailedReportsAsComplete( initDatas );
        markBatchStatusAsCompleteIfNecessary( runStatusAcquisition, initDatas );
        for( DynaBeanRunStatusInit initData : initDatas ) {
            initData.getBeanHelper().updateIfNecessary();
        }
        LOG.info( "leaving BatchReportProcessor.handleResults for report: " + reportInput.getReport().getName() );
    }

    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.appContext = new StarkApplicationContext( applicationContext );
    }

    public void setReportExecutor( BlockingThreadPoolTaskExecutor reportExecutor ) {
        this.reportExecutor = reportExecutor;
    }
    
    public void setReportProvider( ReportProvider reportProvider ) {
        this.reportProvider = reportProvider;
    }

    public void setRunStatusRegistry( RunStatusRegistry registry ) {
        this.runStatusRegistry = registry;
    }

    public void setUserProvider( UserProvider userProvider ) {
        this.userProvider = userProvider;
    }

    /**
     * If the batch executes as part of a larger run (e.g.: a BOOK), we use the RunStatus identified 
     * in the report parameters. Otherwise we just create a new one.
     * 
     * @param reportInput
     * @return a RunStatusAquisition containing the RunStatus
     */
    private RunStatusAcquisition acquireRunStatus( ReportEngineInput reportInput ) {
        String referencyKey = (String) reportInput.getParameters().get( ORStatics.REPORT_RUN_REFERERENCE_KEY );
        RunStatus runStatus = runStatusRegistry.getRunStatus( referencyKey );
        boolean preexistingStatus = false;
        if( runStatus == null ) {
            Report report = reportInput.getReport();
            runStatus = runStatusRegistry.addRunStatus( RunType.BATCH, report.getName(), report.getDescription() );
        } else {
            runStatus.setRunType( RunType.BATCH );
            preexistingStatus = true;
        }

        return new RunStatusAcquisition( runStatus, preexistingStatus );
    }

    private DynaBeanRunStatusInit createRunStatusInitData( QueryResults queryResults, int index, Map<String, ReportUser> userMap) {
        DynaBeanHelper beanHelper = new DynaBeanHelper( queryResults, index );
        String scheduleDescription = beanHelper.getString( "schedule_description" );
        String reportName = beanHelper.getString( "report_name" );
        String userName = beanHelper.getString( "user_name" );
        String exportTypeString = beanHelper.getString( "report_export_type" );
        Date scheduleStartDatetime = beanHelper.getDate( "schedule_start_datetime" );
        String scheduleTypeString = beanHelper.getString( "schedule_type" );
        String scheduleCronExpression = beanHelper.getString( "schedule_cron_expression" );
        String scheduleEmailSubject = beanHelper.getString( "schedule_email_subject" );
        String scheduleEmailAttachmentName = beanHelper.getString( "schedule_email_attachment_name" );
        String scheduleEmailRecipients = beanHelper.getString( "schedule_email_recipients" );
        String scheduleOutputPath = beanHelper.getString( "schedule_output_path" );
        String schedulePrintCommand = beanHelper.getString( "schedule_print_command" );

        Report report = getReport( beanHelper, reportName );

        ReportUser reportUser = getUser( beanHelper, userName, userMap );

        ExportType exportType = getExportType( beanHelper, exportTypeString );

        ScheduleType scheduleType = getScheduleType( beanHelper, scheduleTypeString );

        if( report == null || reportUser == null || exportType == null || scheduleType == null ) {
            String itemReportName = ( reportName == null ) ? "No Report Name Provided" : reportName;
            DynaBeanRunStatusInit badBRI = new DynaBeanRunStatusInit( itemReportName, beanHelper );
            if( reportName == null ) {
                badBRI.addError( "No Report Name Provided" );
            } else if( report == null ) {
                badBRI.addError( "No Report with name, " + reportName + ", could be found" );
            }

            if( exportTypeString == null ) {
                badBRI.addError( "No export type provided" );
            } else if( exportType == null ) {
                badBRI.addError( "Invalid export type: " + exportTypeString );
            }
            if( scheduleTypeString == null ) {
                badBRI.addError( "No schedule type provided" );
            } else if( scheduleType == null ) {
                badBRI.addError( "Invalid schedule type: " + scheduleTypeString );
            }
            return badBRI;
        }

        Map<String, Object> reportParameters = new HashMap<String, Object>();
        for( ReportParameterMap rpMap : report.getParameters() ) {
            String parameterName = rpMap.getReportParameter().getName();
            reportParameters.put( parameterName, beanHelper.getString( PARAM_PREFIX + parameterName.toLowerCase() ) );
        }

        ReportSchedule reportSchedule = new ReportSchedule();

        reportSchedule.setReport( report );
        reportSchedule.setUser( reportUser );
        reportSchedule.setJobGroup( Integer.toString( reportUser.getId() ) );
        reportSchedule.setReportParameters( reportParameters );
        reportSchedule.setExportType( exportType.getCode() );
        reportSchedule.setScheduleType( scheduleType.getCode() );
        reportSchedule.setScheduleName( report.getId() + "|" + new Date().getTime() );
        reportSchedule.setScheduleDescription( scheduleDescription );

        reportSchedule.setStartDate( scheduleStartDatetime );
        Calendar cal = Calendar.getInstance();
        cal.setTime( scheduleStartDatetime );
        reportSchedule.setStartHour( Integer.toString( cal.get( Calendar.HOUR ) ) );
        reportSchedule.setStartMinute( Integer.toString( cal.get( Calendar.MINUTE ) ) );
        reportSchedule.setStartAmPm( cal.get( ( Calendar.AM_PM ) ) == Calendar.AM ? "AM" : "PM" );

        reportSchedule.setRecipients( scheduleEmailRecipients );
        reportSchedule.setOutputPaths( scheduleOutputPath );
        reportSchedule.setEmailAttachmentName( scheduleEmailAttachmentName );
        reportSchedule.setEmailSubjectLine( scheduleEmailSubject );
        reportSchedule.setPrintCommand( schedulePrintCommand );
        reportSchedule.setCronExpression( scheduleCronExpression );

        List<String> deliveryMethods = new ArrayList<String>();
        if( !StringUtils.isBlank( reportSchedule.getRecipients() ) ) {
            deliveryMethods.add( DeliveryMethod.EMAIL.getName() );
        }

        if( !StringUtils.isEmpty( reportSchedule.getOutputPaths() ) ) {
            deliveryMethods.add( DeliveryMethod.LAN.getName() );
        }

        if( !StringUtils.isEmpty( reportSchedule.getPrintCommand() ) ) {
            deliveryMethods.add( DeliveryMethod.PRINTER.getName() );
        }

        if( deliveryMethods.size() == 0 ) {
            LOG.warn( "No delivery method defined for report: " + reportName );
        }

        reportSchedule.setDeliveryMethods( deliveryMethods.toArray( new String[] {} ) );

        return new DynaBeanRunStatusInit( reportSchedule, beanHelper );
    }

    private List<DynaBeanRunStatusInit> createRunStatusInitDatas( QueryResults queryResults ) {
        ArrayList<DynaBeanRunStatusInit> runStatusInits = new ArrayList<DynaBeanRunStatusInit>();
        Map<String, ReportUser> userMap = new HashMap<String, ReportUser>();  
        for(int i = 0, count = queryResults.getCount(); i < count; i++) {
            DynaBeanRunStatusInit runStatusInit = createRunStatusInitData( queryResults, i, userMap);
            runStatusInits.add( runStatusInit );
        }
        return runStatusInits;
    }

    private ExportType getExportType( DynaBeanHelper beanHelper, String exportTypeString ) {
        ExportType exportType = ExportType.findByNameIgnoreCase( exportTypeString );
        if( exportType == null ) {
            beanHelper.setStatus( "error", "Invalid export type: " + exportTypeString );
        }
        return exportType;
    }

    private Report getReport( DynaBeanHelper beanHelper, String reportName ) {
        try {
            Report report = reportProvider.getReport( reportName );
            if( report == null ) {
                beanHelper.setStatus( "error", "No report named: [" + reportName + "]" );
            }
            return report;
        } catch( ProviderException pe ) {
            beanHelper.setStatus( "error", "Exception attempting to get report with name: [" + reportName + "]: "
                    + pe.getMessage() );
            return null;
        }
    }

    private ScheduleType getScheduleType( DynaBeanHelper beanHelper, String scheduleTypeString ) {
        ScheduleType scheduleType = ScheduleType.findByNameIgnoreCase( scheduleTypeString );
        if( scheduleType == null ) {
            beanHelper.setStatus( "error", "Invalid schedule type: " + scheduleTypeString );
        }
        return scheduleType;

    }

    /**
     * 
     * @param beanHelper
     * @param userName
     * @param userMap just using it to avoid repeated lookups of the user
     * @return
     */
    private ReportUser getUser( DynaBeanHelper beanHelper, String userName, Map<String, ReportUser> userMap ) {
        ReportUser reportUser = userMap.get( userName );
        if (reportUser != null) {
            return reportUser;
        }
        try {
            reportUser = userProvider.getUser( userName );
            if( reportUser == null ) {
                beanHelper.setStatus( "error", "No report user named: [" + userName + "]" );
            } else {
                userMap.put( userName, reportUser );
            }
            return reportUser;

        } catch( ProviderException pe ) {
            beanHelper.setStatus( "error", "Exception attempting to get user witn name: [" + userName + "]: "
                    + pe.getMessage() );
            return null;
        }
    }

    private boolean hasSchedulingErrors( List<? extends ReportRunStatusInit> runStatusInitDatas ) {
        for( ReportRunStatusInit reportRunStatusInit : runStatusInitDatas ) {
            if( reportRunStatusInit.hasErrors() ) {
                return true;
            }
        }
        return false;
    }

    private ReportUser loadReportUser( ReportEngineInput reportInput )  {
        String userName = (String) reportInput.getParameters().get( ORStatics.USER_NAME );
        try {
            return userProvider.getUser( userName );
        } catch( ProviderException e ) {
            LOG.warn( "could not load user, " + userName );
            return null;
        }
    }
    
    

    /**
     * If this BatchReportProcessor created the RunStatus for the batch, then we also need to mark
     * it as complete, because it cannot be tied back to the registry. This warrants a bit of
     * explanation (as it took me a while to puzzle through):
     * <p>
     * When a report is run interactively, there's no callback when finished. However, when
     * scheduled, there's a NotifyingScheduleReportCallback that is called by the schedule job, and
     * it uses ReportLog.requestId to tie back to the RunStatus in the RunStatusRegistry. However,
     * the ReportLog is not available to the ReportEngine, and therefore not available to the
     * ReportProcessor. However, this isn't a problem, we simply mark the batch as complete when we
     * are done scheduling.
     * </p>
     * </p> In the scenario where a batch is part of a Book, the Batch report <i>is</i> represented
     * by a referenceKey registered in the RunStatusRegistry. As such, we don't mark it as complete
     * yet, as it will be marked as complete in the callback.
     * 
     * This is a bit messy, and could probably be cleaned up if we simply registered every report so
     * there's always a Report. TODO: consider implementing this.
     * 
     * <ul>
     * <li>
     * 
     * @param runStatusAcquisition
     * @param initDatas
     */
    private void markBatchStatusAsCompleteIfNecessary( RunStatusAcquisition runStatusAcquisition,
            List<? extends ReportRunStatusInit> initDatas ) {
        if( !runStatusAcquisition.isPreexisting() ) {
            boolean hasErrors = hasSchedulingErrors( initDatas );
            String status = hasErrors ? "Completed, but errors scheduling" : "success";
            runStatusRegistry.markRunCompleteWithNotification( runStatusAcquisition.getRunStatus().getRefererenceKey(),
                    status, true, null );
        }
    }

    private void markFailedReportsAsComplete( List<? extends ReportRunStatusInit> runStatusInitDatas ) {
        for( ReportRunStatusInit reportRunStatusInit : runStatusInitDatas ) {
            if( reportRunStatusInit.hasErrors() ) {
                runStatusRegistry.markRunCompleteWithNotification( reportRunStatusInit.getRequestId(), "error", false,
                        reportRunStatusInit.getErrorMessages() );
            }
        }
    }

    private void queueReports( List<DynaBeanRunStatusInit> runStatusInits ) {
        for( DynaBeanRunStatusInit runStatusInit : runStatusInits ) {
            if( runStatusInit.hasSchedule() ) {
                ReportExecutionRunnable runnable = appContext.getSpringBean( "reportExecutionRunnable", ReportExecutionRunnable.class );
                runnable.setSchedule(runStatusInit.getSchedule());
                reportExecutor.execute( runnable );
                runStatusInit.getBeanHelper().setStatus( "success", "Report Successfully scheduled" );
            }
        }
    }

    private void setNotificationRecipients( RunStatus runStatus, ReportEngineInput reportInput ) {
        String notificationEmailAddresses = (String) reportInput.getParameters().get( ORStatics.NOTIFICATION_EMAIL_ADDRESSES );
        if (StringUtils.isBlank( notificationEmailAddresses ) ) {
            ReportUser reportUser = loadReportUser( reportInput ); 
            notificationEmailAddresses  = ( reportUser == null ) ? null : reportUser.getEmail();
        }
        if (StringUtils.isBlank( notificationEmailAddresses ) ) {
            LOG.warn( "Could not get RunStatus recipients on Batch Report, " + reportInput.getReport().getName() + "using admin email recipients" );
            notificationEmailAddresses = deliverySupport.getAdminEmailRecipients();
        }
        runStatus.setNotificationRecipients( notificationEmailAddresses );
    }

    /**
     * wrapper to provide some simple support.
     * 
     * @author mconner
     * 
     */
    static class DynaBeanHelper {
        DynaBean bean;
        private QueryResults queryResults;
        private boolean updateRequired = false;
        private int index;

        public DynaBeanHelper( QueryResults queryResults, int index ) {
            this.bean = queryResults.getAsDynaBean( index );
            this.queryResults = queryResults;
            this.index = index;
        }
        
        void updateIfNecessary() {
            if( updateRequired ) {
                queryResults.updateWithDynaBean( bean, index );
            }
        }

        public Date getDate( String propertyName ) {
            return getValue( propertyName, Date.class );
        }

        public String getString( String propertyName ) {
            return getValue( propertyName, String.class );
        }

        public Object getValue( String propertyName ) {
            try {
                Object result = bean.get( propertyName );
                return result;
            } catch( IllegalArgumentException iae ) {
                throw new BatchReportConfigException( "Batch Reporting Processing: Required report column, "
                        + propertyName + ", is missing", iae );
            }
        }

        public <T> T getValue( String propertyName, Class<T> expectedType ) {
            Object value = getValue( propertyName );
            if( value == null || expectedType.isInstance( value ) ) {
                return expectedType.cast( value );
            } else {
                String message =
                        "Incorrect Datatype for property: " + propertyName + ". Expected: " + expectedType.getName()
                                + ", was: " + value.getClass().getName();
                throw new BatchReportConfigException( message );
            }
        }

        public void setStatus( String status, String statusMessage ) {
            setValue( BATCH_STATUS, status );
            setValue( BATCH_STATUS_MESSAGE, statusMessage );
        }

        public void setValue( String propertyName, Object value ) {
            DynaProperty dynaProperty = bean.getDynaClass().getDynaProperty( propertyName );
            if( dynaProperty == null ) {
                throw new BatchReportConfigException( "No column, " + propertyName
                        + " defined for update of batch report" );
            }
            if( dynaProperty.getType().isInstance( value ) ) {
                bean.set( propertyName, value );
                updateRequired  = true;
            } else {
                throw new BatchReportConfigException( "Column, " + propertyName
                        + " not expected type for batch report.  Expected: " + value.getClass().getName()
                        + ", actual: " + dynaProperty.getType() );
            }
        }

    }

    /**
     * Just ties the RunStatusInitData to a DynaBeanHelper so we can push status back into the
     * result set of the batch report.
     */
    static class DynaBeanRunStatusInit extends ReportRunStatusInit {
        DynaBeanHelper beanHelper;

        DynaBeanRunStatusInit( ReportSchedule reportSchedule, DynaBeanHelper beanHelper ) {
            super( reportSchedule );
            this.beanHelper = beanHelper;
        }

        DynaBeanRunStatusInit( String reportName, DynaBeanHelper beanHelper ) {
            super( reportName );
            this.beanHelper = beanHelper;
        }

        public DynaBeanHelper getBeanHelper() {
            return beanHelper;
        }

    }

    /** 
     * A little class to tie some info about how we got the status to the status, itself. 
     */
    private static class RunStatusAcquisition {
        boolean preexisting;
        RunStatus runStatus;

        RunStatusAcquisition( RunStatus runStatus, boolean preexisting ) {
            this.runStatus = runStatus;
            this.preexisting = preexisting;
        }

        public RunStatus getRunStatus() {
            return runStatus;
        }

        public boolean isPreexisting() {
            return preexisting;
        }
    }

}
