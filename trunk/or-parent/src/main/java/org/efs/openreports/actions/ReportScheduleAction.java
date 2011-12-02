/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.actions;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.DeliveryMethod;
import org.efs.openreports.ReportConstants.ScheduleType;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.delivery.DeliveryMethodGroovyContext;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportAlert;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.objects.ReportUserAlert;
import org.efs.openreports.providers.AlertProvider;
import org.efs.openreports.providers.DateProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.SchedulerProvider;
import org.efs.openreports.providers.UserProvider;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.ParameterAwareHelp;
import org.efs.openreports.util.scripting.ParameterGroovyContext;
import org.quartz.CronExpression;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class ReportScheduleAction extends ActionSupport {
    private static final long serialVersionUID = 4669274401576512976L;

    protected static Logger log = Logger.getLogger( ReportScheduleAction.class );

    private boolean submitScheduledReport;
    private boolean submitValidate;
    private Report report;

    private String scheduleName;
    private String description;
    private int userId = Integer.MIN_VALUE;
    private String userName = "";

    private Date currentStartDate;
    private int scheduleType;
    private Date startDate;
    private String startHour;
    private String startMinute;
    private String startAmPm;
    private String recipients;
    /** a set of output paths */
    private String outputPaths;
    private List<String> parsedOutputPaths;
    private int hours;
    private String cron;

    private int alertId = Integer.MIN_VALUE;
    private int alertLimit;
    private String alertOperator;

    private boolean runToFile;

    private SchedulerProvider schedulerProvider;
    private DateProvider dateProvider;
    private AlertProvider alertProvider;
    private UserProvider userProvider;
    private ParameterProvider parameterProvider;
    private ReportProvider reportProvider;

    private String printCommand;
    private String parsedPrintCommand;
    
    private String emailAttachmentName;
    private String parsedEmailAttachmentName;
    
    private String emailSubjectLine;

    private String parsedEmailSubjectLine;

    // this is read-only, at the moment, not saved when schedule is saved.
    private Map<String, Object> reportParameters;

    private List<ScriptVariable> scriptVariables = new ArrayList<ScriptVariable>();
    

    public String execute() {
        try {
            ReportUser user = getUser();
            if( !hasEmail( user ) ) {
                addActionError( getText( LocalStrings.ERROR_EMAILADDRESS_REQUIRED ) );
                return INPUT;
            }

            if( submitScheduledReport ) {
                validate( user );
                populateReadOnlyElements( user ); 
                if( hasErrors() ) {
                    return INPUT;
                } else {
                    return update( user );
                }
            } else if( submitValidate ) {
                validate( user );
                populateReadOnlyElements( user );
                return INPUT;
            } else {
                return buildForInitialDisplay( user );
            }

        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return INPUT;
        }
    }

    public int getAlertId() {
        return alertId;
    }

    public int getAlertLimit() {
        return alertLimit;
    }

    public String getAlertOperator() {
        return alertOperator;
    }

    public List<ReportAlert> getAlerts() {
        try {
            return alertProvider.getReportAlerts();
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

    public String getCron() {
        return cron;
    }

    public String getDateFormat() {
        return dateProvider.getDateFormatPattern();
    }

    public String getDescription() {
        return description;
    }

    public int getHours() {
        return hours;
    }

    public String[] getOperators() {
        return new String[] { ReportAlert.OPERATOR_EQUAL, ReportAlert.OPERATOR_GREATER, ReportAlert.OPERATOR_LESS };
    }

    public String getOutputPaths() {
        return outputPaths;
    }

    public List<String> getParsedOutputPaths() {
        return parsedOutputPaths;
    }

    public String getParsedPrintCommand() {
        return this.parsedPrintCommand;
    }

    public String getParsedEmailAttachmentName() {
        return this.parsedEmailAttachmentName;
    }

    public String getPrintCommand() {
        return printCommand;
    }

    public String getRecipients() {
        return recipients;
    }

    public Report getReport() {
        return report;
    }

    public Map<String, Object> getReportParameters() {
        return reportParameters;
    }

    public List<ScriptVariable> getScriptVariables() {
        return scriptVariables;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public String getStartAmPm() {
        return startAmPm;
    }

    public Date getStartDate() {
        return startDate;
    }
    
    public Date getCurrentStartDate() {
        return currentStartDate;
    }


    public String getStartHour() {
        return startHour;
    }

    public String getStartMinute() {
        return startMinute;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isRunToFile() {
        return runToFile;
    }

    public void setAlertId( int alertId ) {
        this.alertId = alertId;
    }

    public void setAlertLimit( int alertLimit ) {
        this.alertLimit = alertLimit;
    }

    public void setAlertOperator( String alertOperator ) {
        this.alertOperator = alertOperator;
    }

    public void setAlertProvider( AlertProvider alertProvider ) {
        this.alertProvider = alertProvider;
    }

    public void setCron( String cron ) {
        this.cron = cron;
    }

    public void setDateProvider( DateProvider dateProvider ) {
        this.dateProvider = dateProvider;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setHours( int hours ) {
        this.hours = hours;
    }

    public void setOutputPaths( String outputPaths ) {
        this.outputPaths = outputPaths;
    }

    public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

    public void setReportProvider( ReportProvider reportProvider ) {
        this.reportProvider = reportProvider;
    }

    public void setPrintCommand( String printCommand ) {
        this.printCommand = printCommand;
    }

    public void setRecipients( String recipients ) {
        this.recipients = recipients;
    }

    public void setReport( Report report ) {
        this.report = report;
    }

    public void setRunToFile( boolean runToFile ) {
        this.runToFile = runToFile;
    }

    public void setScheduleName( String scheduleName ) {
        this.scheduleName = scheduleName;
    }

    public void setSchedulerProvider( SchedulerProvider schedulerProvider ) {
        this.schedulerProvider = schedulerProvider;
    }

    public void setScheduleType( int scheduleType ) {
        this.scheduleType = scheduleType;
    }

    public void setStartAmPm( String startAmPm ) {
        this.startAmPm = startAmPm;
    }

    public void setStartDate( Date startDate ) {
        this.startDate = startDate;
    }

    public void setStartHour( String startHour ) {
        this.startHour = startHour;
    }

    public void setStartMinute( String startMinute ) {
        this.startMinute = startMinute;
    }

    public void setSubmitScheduledReport( String submitScheduledReport ) {
        if( submitScheduledReport != null )
            this.submitScheduledReport = true;
    }

    public void setSubmitValidate( String submitValidate ) {
        if( submitValidate != null )
            this.submitValidate = true;
    }

    public void setUserId( int userId ) {
        this.userId = userId;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public void setUserProvider( UserProvider userProvider ) {
        this.userProvider = userProvider;
    }
    
    public String getEmailAttachmentName() {
        return emailAttachmentName;
    }

    public void setEmailAttachmentName( String emailAttachmentName ) {
        this.emailAttachmentName = emailAttachmentName;
    }
    
    public String getEmailSubjectLine() {
        return emailSubjectLine;
    }

    public void setEmailSubjectLine( String emailSubjectLine ) {
        this.emailSubjectLine = emailSubjectLine;
    }

    public String getParsedEmailSubjectLine() {
        return parsedEmailSubjectLine;
    }

    /**
     * Populates the read-only elements of the screen, which includes the scriptable elements.
     * As such this also effectively does some validation. 
     * @param user
     * @throws ProviderException
     */
    protected void populateReadOnlyElements( ReportUser user ) throws ProviderException {
        if( isExistingSchedule() ) {
            ReportSchedule reportSchedule = getScheduledReport( user );
            report = reportSchedule.getReport();
            reportParameters = reportSchedule.getReportParameters();
        } else {
            report = (Report) ActionContext.getContext().getSession().get( ORStatics.REPORT );
            reportParameters = getReportParametersFromSession();
        }

        Date runDateTime = new Date();
        Map<String, Object> evaluatedReportParameters = evaluateReportParameters(reportParameters, runDateTime); 

        Date exampleDateTime = new Date();
        DeliveryMethodGroovyContext groovyContext = new DeliveryMethodGroovyContext( evaluatedReportParameters,
                report.getName(), description , exampleDateTime );
        
        scriptVariables = buildScriptVariables( groovyContext, reportParameters);
        
        parseScriptableElements(groovyContext);
    }

    private ReportSchedule getScheduledReport( ReportUser user ) throws ProviderException {
        ReportSchedule reportSchedule = schedulerProvider.getScheduledReport( user.getId().toString(), scheduleName );
        // TODO: REMOVE: HACK to re-set the user necessary for the boprod01 conversion.
        reportSchedule.setUser( user );
        reportSchedule.setJobGroup( Integer.toString( user.getId() ) );
        
        // re-acquire the report by name (reflects any changes, if any, since originally scheduled).
        reportSchedule.setReport( loadReport( reportSchedule.getReport().getName() ) );
        // TODO: REMOVE: HACK to reset the user id.
        if (reportSchedule.getReportParameters().containsKey( ORStatics.USER_ID )) {
            reportSchedule.getReportParameters().put( ORStatics.USER_ID, user.getId() );
        }
        return reportSchedule;
    }

    private Report loadReport( String reportName ) throws ProviderException {
        return reportProvider.getReport( reportName );
    }

    private Map<String, Object> evaluateReportParameters( Map<String, Object> reportParameters, Date runDateTime ) {
        List<ReportParameterMap> rpMaps = report.getNonSubReportParameters();
        ParameterGroovyContext paramGroovyContext = new ParameterGroovyContext( runDateTime ); 
        Map<String, Object> evaluatedReportParameters = parameterProvider.convertParameters( rpMaps, reportParameters,
                "** Parse Error**",  paramGroovyContext );
        
        
        
        return evaluatedReportParameters;
    }

    private String buildForInitialDisplay( ReportUser user ) throws ProviderException {

        if( isExistingSchedule() ) {
            ReportSchedule reportSchedule = getScheduledReport( user );
            Date newStartDateTime = getNewStartDateTime( reportSchedule );
            
            
            report = reportSchedule.getReport();  // gets the embedded report.
            scheduleType = reportSchedule.getScheduleType();
            //startDate = dateProvider.formatDate( newStartDateTime );
            startDate = newStartDateTime;
            startHour = new SimpleDateFormat("h").format(newStartDateTime);
            startMinute = new SimpleDateFormat("mm").format(newStartDateTime);
            startAmPm = new SimpleDateFormat("a").format(newStartDateTime);
            currentStartDate = reportSchedule.getStartDateTime();
            recipients = reportSchedule.getRecipients();
            outputPaths = reportSchedule.getOutputPaths();
            description = reportSchedule.getScheduleDescription();
            emailAttachmentName = reportSchedule.getEmailAttachmentName();
            emailSubjectLine = reportSchedule.getEmailSubjectLine();
            hours = reportSchedule.getHours();
            cron = reportSchedule.getCronExpression();
            runToFile = reportSchedule.isDeliveryMethodSelected( DeliveryMethod.FILE.getName() );
            printCommand = reportSchedule.getPrintCommand();

            if( reportSchedule.getAlert() != null ) {
                ReportUserAlert userAlert = reportSchedule.getAlert();

                alertId = userAlert.getAlert().getId().intValue();
                alertLimit = userAlert.getLimit();
                alertOperator = userAlert.getOperator();
            }

            // reportParameters = reportSchedule.getReportParameters();
        } else {
            recipients = user.getEmail();
            outputPaths = "";
            printCommand = "";

            report = (Report) ActionContext.getContext().getSession().get( ORStatics.REPORT );
            Calendar calendar = Calendar.getInstance();
            description = report.getName();
            startDate = calendar.getTime();
            int hour = calendar.get( Calendar.HOUR );
            startHour = String.valueOf( hour == 0 ? 12 : hour );
            startMinute = String.valueOf( calendar.get( Calendar.MINUTE ) );

            if( calendar.get( Calendar.AM_PM ) == Calendar.PM ) {
                startAmPm = "PM";
            }
        }

        populateReadOnlyElements( user );

        return INPUT;
    }

    private Date getNewStartDateTime( ReportSchedule reportSchedule ) {
        Date nextFireDate = reportSchedule.getNextFireDate();
        return (nextFireDate == null) ? new Date() : nextFireDate;  
    }

    @SuppressWarnings( "unchecked" )
    private Map<String, Object> getReportParametersFromSession() {
        return (Map) ActionContext.getContext().getSession().get( ORStatics.REPORT_PARAMETERS );
    }

    private ReportUser getUser() throws ProviderException {
        ReportUser user;
        if( !StringUtils.isBlank( userName ) ) {
            // TODO HACK: This section is a bit of a hack to make it possible to load the get the user by name,
            // rather than by id.  It is a temporary measure as part of migrating the back office reports
            // over.
            user = userProvider.getUser( userName );
            userId = user.getId();
        } else if( userId >= 0 ) {
            user = userProvider.getUser( new Integer( userId ) );
        } else {
            user = (ReportUser) ActionContext.getContext().getSession().get( ORStatics.REPORT_USER );
        }
        return user;
    }

    private boolean hasEmail( ReportUser user ) {
        return user.getEmail() != null || user.getEmail().length() > 0;
    }

    /**
     * @return true of the schedule has a name. (New schedules will not have a name).
     */
    private boolean isExistingSchedule() {
        return scheduleName != null && scheduleName.length() > 0;
    }

    private String makeTextValue( Object object ) {
        if( object == null ) {
            return null;
        }
        if( object instanceof Object[] ) {
            return StringUtils.join( (Object[]) object, "," );
        }
        return object.toString();
    }


    private void parseScriptableElements(DeliveryMethodGroovyContext groovyContext) {

        parsedPrintCommand = parseScriptableText( "Print Command", printCommand, groovyContext );
        parsedEmailAttachmentName = parseScriptableText( "Email Attachment Name", emailAttachmentName, groovyContext );
        parsedEmailSubjectLine = parseScriptableText( "Email Subject Line", emailSubjectLine, groovyContext );

        if (outputPaths != null) {
            StringTokenizer st = new StringTokenizer( outputPaths, "\n\r\f" );
            parsedOutputPaths = new ArrayList<String>();
            int line = 1;
            while( st.hasMoreElements() ) {
                String outputPath = st.nextToken().trim();
                if( StringUtils.isNotBlank( outputPath ) ) {
                    parsedOutputPaths.add( parseScriptableText( "Output File[" + line + "]", outputPath, groovyContext ) );
                    line++;
                }
            }
        }
        
    };

    private String parseScriptableText( String name, String text, DeliveryMethodGroovyContext groovyContext ) {
        String parsedText;
        if( groovyContext.isScript( text ) ) {
            Object evalResult;
            try {
                evalResult = groovyContext.eval( text );
                if( evalResult == null ) {
                    addActionError( "script for " + name + " returns null value: " );
                    parsedText = (String) evalResult;
                } else if( !( evalResult instanceof String ) ) {
                    String evalAsString = evalResult.toString();
                    addActionError( "script for " + name + " returns incorrect type: "
                            + evalResult.getClass().getName() );
                    parsedText = evalAsString.substring( 0, Math.min( 50, evalAsString.length() ) );
                } else {
                    parsedText = evalResult.toString();
                }
            } catch( ScriptException e ) {
                parsedText = "error";
                addActionError( "Error parsing " + name + ": " + e.getMessage() );
            }
        } else {
            parsedText = text;
        }
        return parsedText;
    }

    private List<ScriptVariable> buildScriptVariables( DeliveryMethodGroovyContext groovyContext,
            Map<String, Object> reportParameters ) {
        List<ScriptVariable> results = new ArrayList<ScriptVariable>();
        Bindings bindings = groovyContext.getEngine().getBindings( ScriptContext.ENGINE_SCOPE );
        bindings.keySet();
        for( String bindingName : bindings.keySet() ) {

            ScriptVariable var;
            if( DeliveryMethodGroovyContext.isSystemValue( bindingName ) ) {
                var = new ScriptVariable( ScriptVariable.SOURCE_SYSTEM, bindingName, null, bindings.get( bindingName ) );
            } else if( bindingName.startsWith( ORStatics.OPERREPORTS_PARAM_PREFIX ) ) {
                var = new ScriptVariable( ScriptVariable.SOURCE_SYSTEM_PARAM, bindingName, null,
                        bindings.get( bindingName ) );
            } else {
                Object originalValue = reportParameters.get( bindingName );
                // 
                if( isScript( originalValue, bindingName, groovyContext) ) {
                    String scriptText = ParameterAwareHelp.getSingleValue( originalValue, bindingName );
                    var = new ScriptVariable( ScriptVariable.SOURCE_PARAM, bindingName, scriptText,
                            bindings.get( bindingName ) );
                } else {
                    var = new ScriptVariable( ScriptVariable.SOURCE_PARAM, bindingName, bindings.get( bindingName ), null );
                }
                

            }

            results.add( var );
        }
        Collections.sort( results );
        return results;
    }

    private boolean isScript( Object originalValue, String bindingName, DeliveryMethodGroovyContext groovyContext ) {
        if (ParameterAwareHelp.isSingleValue(originalValue)) {
            return groovyContext.isScript( ParameterAwareHelp.getSingleValue( originalValue, bindingName ) );
        } 
        return false;
    }

    /**
     * Actually persist the schedule. TODO: This should be in a transaction. If there's an error on
     * save, the delete will already have blown away the report.
     * 
     * @param user
     * @param reportSchedule
     * @throws ProviderException
     */
    private void saveSchedule( ReportUser user, ReportSchedule reportSchedule ) throws ProviderException {
        boolean isPaused = false;
        String jobGroup = user.getId().toString();
        String jobName = scheduleName; 
        if( isExistingSchedule() ) {
            // in order to update a schedule report, original reportSchedule
            // is deleted and new a one is created.
            isPaused = schedulerProvider.isPaused(jobGroup, jobName);
            schedulerProvider.deleteScheduledReport( jobGroup, jobName );
        }
        schedulerProvider.scheduleReport( reportSchedule );
        if (isPaused) {
            // Not perfect, might still trigger between schedule and pause.
            schedulerProvider.pauseScheduledReport(jobGroup, jobName );  
        }

        addActionMessage( getText( LocalStrings.MESSAGE_SCHEDULE_SUCCESSFUL ) );
    }

    /**
     * 
     * @param user
     * @param validateOnly if true, the report schedule is not persisted.
     * @return
     * @throws ProviderException
     */
    private String update( ReportUser user ) throws ProviderException {
        boolean saved = false;
        saved = updateReportSchedule( user );
        return ( saved ) ? SUCCESS : INPUT;
    }

    private boolean updateReportSchedule( ReportUser user ) throws ProviderException {
        ReportSchedule reportSchedule = new ReportSchedule();

        if( isExistingSchedule() ) {
            reportSchedule = getScheduledReport( user );
        } else {
            report = (Report) ActionContext.getContext().getSession().get( ORStatics.REPORT );

            int exportType = Integer.parseInt( (String) ActionContext.getContext().getSession().get(
                    ORStatics.EXPORT_TYPE ) );

            reportSchedule.setReport( report );
            reportSchedule.setUser( user );
            reportSchedule.setJobGroup( Integer.toString( user.getId() ) );
            reportSchedule.setReportParameters( getReportParametersFromSession() );
            reportSchedule.setExportType( exportType );
            reportSchedule.setScheduleName( report.getId() + "|" + new Date().getTime() );
        }

        reportSchedule.setScheduleType( scheduleType );
        //reportSchedule.setStartDate( dateProvider.parseDate( startDate ) );
        reportSchedule.setStartDate( dateProvider.parseDate( dateProvider.formatDate(startDate)));
        reportSchedule.setStartHour( startHour );
        reportSchedule.setStartMinute( startMinute );
        reportSchedule.setStartAmPm( startAmPm );
        reportSchedule.setRecipients( recipients );
        reportSchedule.setOutputPaths( outputPaths );
        reportSchedule.setScheduleDescription( description );
        reportSchedule.setEmailAttachmentName( emailAttachmentName );
        reportSchedule.setEmailSubjectLine( emailSubjectLine );
        if (user.isAdvancedScheduler()) {
            reportSchedule.setPrintCommand( printCommand );
        }
        reportSchedule.setHours( hours );
        reportSchedule.setCronExpression( cron );

        List<String> deliveryMethods = new ArrayList<String>();
        if( !StringUtils.isBlank( recipients.trim() ) ) {
            if( runToFile ) {
                deliveryMethods.add( DeliveryMethod.FILE.getName() );
            } else {
                deliveryMethods.add( DeliveryMethod.EMAIL.getName() );
            }
        }

        if( !reportSchedule.getOutputPaths().isEmpty() ) {
            deliveryMethods.add( DeliveryMethod.LAN.getName() );
        }

        if( !StringUtils.isEmpty( reportSchedule.getPrintCommand() ) ) {
            deliveryMethods.add( DeliveryMethod.PRINTER.getName() );
        }

        if( deliveryMethods.size() == 0 ) {
            addActionError( getText( LocalStrings.ERROR_NO_DELIVERY_METHOD ) );

            return false;
        }

        reportSchedule.setDeliveryMethods( deliveryMethods.toArray( new String[] {} ) );

        if( alertId != -1 ) {
            ReportAlert alert = alertProvider.getReportAlert( new Integer( alertId ) );

            ReportUserAlert userAlert = new ReportUserAlert();
            userAlert.setAlert( alert );
            userAlert.setUser( user );
            userAlert.setLimit( alertLimit );
            userAlert.setOperator( alertOperator );

            reportSchedule.setAlert( userAlert );
        } else {
            reportSchedule.setAlert( null );
        }

        saveSchedule( user, reportSchedule );
        return true;
    }

    /**
     * TODO: need to look at the framework validate method.
     * 
     * @param user
     * @return
     * @throws ProviderException
     */
    private void validate( ReportUser user ) throws ProviderException {
        String startTimeLabel = getText( "label.startTime" );
        validateScheduleType( scheduleType, getText("label.scheduleType" ) );
        validateInt( startHour, startTimeLabel + " hour ", 1, 12 );
        validateInt( startMinute, startTimeLabel + " minute ", 0, 59 );
        validateAmPm( startAmPm, startTimeLabel + "AM/PM" );
        //validateDate( startDate, getText( "label.startDate" ) );
        validateDate( dateProvider.formatDate(startDate), getText( "label.startDate" ) );
        validateCron( cron, getText( "label.cronExpression" ) );
    }

    private String validateAmPm( String startAmPm, String name ) {
        if( isBlank( startAmPm ) ) {
            addActionError( name + " must be either AM or PM" );
        }
        return startAmPm;
    }

    private void validateCron( String cronString, String name ) {
        if( ScheduleType.CRON.getCode() == scheduleType ) {
            try {
                createACron( cronString );
            } catch( ParseException pe ) {
                addActionError( "Invalid Cron Expression: " + pe.getMessage() );
            }
        }
    }

    /** separate method to avoid unused warning */
    private CronExpression createACron( String cronString ) throws ParseException {
        return new CronExpression( cronString ); 
    }

    private void validateDate( String date, String name ) {
    	try {
            //dateProvider.parseDate( startDate );
            dateProvider.parseDate( date);
        } catch( ProviderException pe ) {
            addActionError( "Invalid " + name + ", " + pe.getMessage() );
        }
    }

    private int validateInt( String string, String name, int minValue, int maxValue ) {
        return validateInt( string, name, minValue, maxValue, Integer.MIN_VALUE );
    }

    private int validateInt( String string, String name, int minValue, int maxValue, int badValue ) {
        if( isBlank( string ) ) {
            addActionError( name + " must be a number between " + minValue + " and " + maxValue + ". (is: " + string
                    + ")" );
        }
        try {
            int result = Integer.parseInt( string );
            if( result < minValue || result > maxValue ) {
                addActionError( name + " must be a number between " + minValue + " and " + maxValue + ". (is: "
                        + string + ")" );
                return badValue;
            } else {
                return result;
            }

        } catch( NumberFormatException nfe ) {
            addActionError( name + " must be  a number between " + minValue + " and " + maxValue + ". (was " + string
                    + ")" );
            return badValue;
        }

    }
    
    private void validateScheduleType( int scheduleCode, String text ) {
        ScheduleType type = ScheduleType.findByCode( scheduleCode );
        if (type == null || type.equals( ScheduleType.NONE )) {
            addActionError("You must select a " + text);
        }
    }

    /**
     * A value that is availble for scripting schedule elements, such as subject line, attachement
     * name, or file output path.  
     */
    public class ScriptVariable implements Comparable<ScriptVariable>{
        public static final String SOURCE_PARAM = "Parameter";
        public static final String SOURCE_SYSTEM = "System";
        public static final String SOURCE_SYSTEM_PARAM = "System Parameter";
        private String name;
        private String source;
        private Object value;
        private Object exampleValue;
        


        /**
         * 
         * @param source  "parameter or system 
         * @param name
         * @param value
         * @param example
         */
        public ScriptVariable( String source, String name, Object value, Object example ) {
            this.name = name;
            this.source = source;
            this.value = value;
            this.exampleValue = example;
        }

        /**
         * @return The value to be used for parsing. 
         */
        public Object getEffectiveValue() {
            return (exampleValue == null) ? value : exampleValue;
        }

        public Object getExampleValueAsText() {
            return  makeTextValue( exampleValue );
        }

        public String getSource() {
            return source;
        }

        public String getName() {
            return name;
        }

        public String getValueAsText() {
            return makeTextValue( value );
        }

        public String getType() {
            if( exampleValue != null ) {
                return ClassUtils.getShortClassName( exampleValue, "-" );
            }
            return ClassUtils.getShortClassName( value, "-" );
        }

        public int compareTo( ScriptVariable that ) {
            int result =  this.source.compareTo( that.source );
            if (result != 0)  return result;
            
            result = this.name.compareTo( that.name );
            if (result != 0) return result;

            result = this.name.compareTo( that.name );
            return result;
            
        }
    }

}