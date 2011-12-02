package org.efs.openreports.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.delivery.DeliveryMethod;
import org.efs.openreports.engine.JasperVirtualizerHolder;
import org.efs.openreports.engine.ReportEngine;
import org.efs.openreports.engine.ReportEngineProvider;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportChart;
import org.efs.openreports.objects.ReportDeliveryLog;
import org.efs.openreports.objects.ReportLog;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.objects.ReportUserAlert;
import org.efs.openreports.providers.AlertProvider;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportLogProvider;
import org.efs.openreports.util.ReleasableU;
import org.efs.openreports.util.StarkApplicationContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ReportExecutionRunnable implements Runnable, ApplicationContextAware {
    protected static Logger log = Logger.getLogger( ReportExecutionRunnable.class.getName() );

    ReportSchedule reportSchedule;
    private AlertProvider alertProvider;
    private StarkApplicationContext appContext;
    private List<ReportRunCallback> reportRunCallbacks;
    private DirectoryProvider directoryProvider;
    private ParameterProvider parameterProvider;
    private ReportEngineProvider reportEngineProvider;

    private ReportLogProvider reportLogProvider;

    public ReportExecutionRunnable( ) {
    }

    public void run() {
        ReportUser user = reportSchedule.getUser();
        Report report = reportSchedule.getReport();
        ReportLog reportLog = new ReportLog( user, report, new Date() );
        reportLog.setExportType( reportSchedule.getExportType() );
        reportLog.setRequestId( reportSchedule.getRequestId() );

        JasperVirtualizerHolder virtualizerHolder = new JasperVirtualizerHolder();
        ReportEngineOutput reportEngineOutput = null;
        try {
            Map<String, Object> reportParameters = getReportParameters( reportSchedule );
            ReportUserAlert alert = reportSchedule.getAlert();

            if( alert != null ) {
                alert.setReport( report );
                alert = alertProvider.executeAlert( alert, true );

                if( !alert.isTriggered() ) {
                    log.debug( "Alert Not Triggered. Report not run." );
                    return;
                }

                log.debug( "Alert Triggered. Running report." );
            }

            // add standard report parameters
            reportParameters.put( ORStatics.USER_ID, user.getId() );
            reportParameters.put( ORStatics.EXTERNAL_ID, user.getExternalId() );
            reportParameters.put( ORStatics.USER_NAME, user.getName() );
            reportParameters.put( ORStatics.IMAGE_DIR, new File( directoryProvider.getReportImageDirectory() ) );
            reportParameters.put( ORStatics.REPORT_DIR, new File( directoryProvider.getReportDirectory() ) );

            // propagate any book report parameter names
            propagateParameter( ORStatics.BASE_OUTPUT_PATH, reportSchedule.getReportParameters(), reportParameters );
            propagateParameter( ORStatics.REPORT_RUN_REFERERENCE_KEY, reportSchedule.getReportParameters(),
                    reportParameters );

            startReportLog( reportLog );

            ReportEngineInput reportInput = buildReportEngineInput( reportSchedule, report, reportParameters );

            if( report.isJasperReport() ) {
                addChartReport( reportInput );
                virtualizerHolder.setup( reportInput, directoryProvider );
            }

            ReportEngine reportEngine = reportEngineProvider.getReportEngine( report );
            reportEngineOutput = reportEngine.generateReport( reportInput );

            String[] deliveryMethods = reportSchedule.getDeliveryMethods();

            if( deliveryMethods == null || deliveryMethods.length == 0 ) {
                deliveryMethods = new String[] { org.efs.openreports.ReportConstants.DeliveryMethod.EMAIL.getName() };
                log.warn( "DeliveryMethod not set, defaulting to email delivery" );
            }

            deliverReport( reportSchedule, reportLog, reportInput, reportEngineOutput, deliveryMethods );

            reportLogProvider.updateReportLog( reportLog );

            log.debug( "Scheduled Report Finished..." );
        } catch( Exception e ) {
            if( e.getMessage() != null && e.getMessage().indexOf( "Empty" ) > 0 ) {
                reportLog.setStatus( ReportLog.STATUS_EMPTY );
            } else {
                log.error( "ScheduledReport on report: " + reportSchedule.getScheduleDescription() + ", id: "
                        + reportSchedule.getRequestId(), e );
                reportLog.setMessage( e.toString() );
                reportLog.setStatus( ReportLog.STATUS_FAILURE );
            }

            reportLog.setEndTime( new Date() );

            try {
                reportLogProvider.updateReportLog( reportLog );
            } catch( Exception ex ) {
                log.error( "Unable to create ReportLog: " + ex.getMessage() );
            }
        } finally {
            virtualizerHolder.cleanup();
            ReleasableU.I.safeRelease( reportEngineOutput );
        }
        executeCallbacks( reportLog );
    }

    public void setAlertProvider( AlertProvider alertProvider ) {
        this.alertProvider = alertProvider;
    }

    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.appContext = new StarkApplicationContext( applicationContext );
    }

    public void setReportRunCallbacks( List<ReportRunCallback> reportRunCallbacks ) {
        this.reportRunCallbacks = reportRunCallbacks;
    }

    public void setDirectoryProvider( DirectoryProvider directoryProvider ) {
        this.directoryProvider = directoryProvider;
    }

    public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

    public void setReportEngineProvider( ReportEngineProvider reportEngineProvider ) {
        this.reportEngineProvider = reportEngineProvider;
    }

    public void setReportLogProvider( ReportLogProvider reportLogProvider ) {
        this.reportLogProvider = reportLogProvider;
    }

    /** add any charts */
    private void addChartReport( ReportEngineInput reportInput ) throws ProviderException {
        Report report = reportInput.getReport();
        ReportChart reportChart = report.getReportChart();
        if( reportChart != null ) {
            log.debug( "Adding chart: " + reportChart.getName() );
            ReportEngine chartEngine = reportEngineProvider.getChartReportEngine( report );
            ReportEngineOutput reportEngineOutput = chartEngine.generateReport( reportInput );
            try {
                reportInput.getParameters().put( "ChartImage", reportEngineOutput.getContentAsBytes() );
            } finally {
                reportEngineOutput.release();
            }
        }
    }

    private ReportEngineInput buildReportEngineInput( ReportSchedule reportSchedule, Report report,
            Map<String, Object> reportParameters ) {
        ReportEngineInput reportInput = new ReportEngineInput( report, reportParameters );
        reportInput.setExportType( ExportType.findByCode( reportSchedule.getExportType() ) );
        reportInput.setXmlInput( reportSchedule.getXmlInput() );
        reportInput.setLocale( reportSchedule.getLocale() );
        reportInput.setParameters( reportParameters );
        reportInput.setInlineImages( true );
        return reportInput;
    }

    /**
     * Deliver the report, update the report log.
     * 
     * @param reportSchedule
     * @param reportLog
     * @param reportInput
     * @param reportOutput
     * @param deliveryMethods
     * @throws JobExecutionException
     */
    private void deliverReport( ReportSchedule reportSchedule, ReportLog reportLog, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput, String[] deliveryMethods ) throws JobExecutionException {

        // Set status to success. if a DeliveryMethod fails, this is updated to delivery failure
        reportLog.setStatus( ReportLog.STATUS_SUCCESS );

        ArrayList<ReportDeliveryLog> deliveryLogEntries =
                doDelivery( reportSchedule, reportInput, reportOutput, deliveryMethods );
        reportLog.setDeliveryLogs( deliveryLogEntries );
        setReportLogMessageAndStatus( reportLog );
        reportLog.setEndTime( new Date() );
    }

    private ArrayList<ReportDeliveryLog> doDelivery( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput, String[] deliveryMethods ) {
        ArrayList<ReportDeliveryLog> deliveryLogEntries = new ArrayList<ReportDeliveryLog>();

        for( int i = 0; i < deliveryMethods.length; i++ ) {
            DeliveryMethod deliveryMethod = getDeliveryMethod( deliveryMethods[i] );
            List<ReportDeliveryLog> deliveryMethodLogEntries =
                    deliveryMethod.deliverReport( reportSchedule, reportInput, reportOutput );
            deliveryLogEntries.addAll( deliveryMethodLogEntries );
        }
        return deliveryLogEntries;
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

    private DeliveryMethod getDeliveryMethod( String deliveryMethod ) {
        String springBeanId = deliveryMethod + "DeliveryMethod";
        return appContext.getSpringBean( springBeanId, DeliveryMethod.class );
    }

    /**
     * Does some magic for with the report parameters to allow a groovy script to define the report
     * parameter value. Why is this cool? It allows one to do:
     * <code>(new Date() - 1).format("MM-dd-yyyy") </code>This gets effectively gets me "yesterday",
     * which is all I want, but opens up a bunch of other stuff:
     * 
     * TODO: Only supports groovy, for the moment. Oh, and now you can't have a parameter with
     * "#!groovy", but use a groovy script if you really need one. :)
     * 
     * 
     * @param reportSchedule
     * @return
     * @throws ScriptException
     */
    private Map<String, Object> getReportParameters( ReportSchedule reportSchedule ) throws ProviderException,
            ScriptException {

        Report report = reportSchedule.getReport();
        List<ReportParameterMap> rpMap = report.getNonSubReportParameters();
        Map<String, Object> parameters = reportSchedule.getReportParameters();

        Map<String, Object> parsedParameters = parameterProvider.getReportParametersMap( rpMap, parameters, true );
        return parsedParameters;

    }

    /**
     * Push a parameter from a source map to a destination map
     * 
     * @param parameterName
     * @param source
     * @param dest
     */
    private void propagateParameter( String parameterName, Map<String, Object> source, Map<String, Object> dest ) {
        Object value = source.get( parameterName );
        if( value != null ) {
            dest.put( parameterName, value );
        }
    }

    private void setReportLogMessageAndStatus( ReportLog reportLog ) {
        int failCount = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter messageWriter = new PrintWriter( baos );
        messageWriter.println();
        List<ReportDeliveryLog> deliveryLogEntries = reportLog.getDeliveryLogs();
        for( ReportDeliveryLog logEntry : deliveryLogEntries ) {
            if( logEntry.getStatus().equals( ReportLog.STATUS_DELIVERY_FAILURE ) ) {
                failCount++;
                messageWriter.println( logEntry.getMessage() );
            }
        }
        if( failCount > 0 ) {
            messageWriter.close();
            String message = "Failures: " + failCount + baos.toString();
            reportLog.setMessage( message );
            reportLog.setStatus( ReportLog.STATUS_DELIVERY_FAILURE );
        }
    }

    private ReportLog startReportLog( ReportLog reportLog ) throws ProviderException {
        return reportLogProvider.insertReportLog( reportLog );
    }

    public void setSchedule( ReportSchedule reportSchedule ) {
        this.reportSchedule = reportSchedule;
    }

}
