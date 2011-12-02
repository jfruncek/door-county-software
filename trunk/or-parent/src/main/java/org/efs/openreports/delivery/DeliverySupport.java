package org.efs.openreports.delivery;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.providers.PropertiesProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.scripting.GroovyContext;

/**
 * Provides some support for delivery methods, Such as determining environment (prod or dev) and
 * getting development environment recipients, and handles extracting values from properties.
 * 
 * @author mconner
 */
public class DeliverySupport {
    protected static Logger log = Logger.getLogger( DeliverySupport.class );

    private PropertiesProvider propertiesProvider;

    public DeliverySupport( PropertiesProvider propertiesProvider ) {
        this.propertiesProvider = propertiesProvider;
    }

    public void writeDevelopmentEmailNotice( PrintWriter writer, String productionRecipients ) {
        writer.println();
        writer.println();
        writer.println( "===========================================================================" );
        writer.println( "TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST TEST " );
        writer.println( "===========================================================================" );
        writer.println( "This is a test of the OpenReports Development Report Email System. " );
        writer.println( "If this had been an actual production report, it would have been sent " );
        writer.println( "to the following addresses: " );
        writer.println(  );
        writer.println(  );
        writer.println( productionRecipients );
        writer.println(  );
        writer.println(  );
        writer.println( "This concludes this test of the OpenReports Development Report Email System." );
        writer.println(  );
    }

    /** 
     * Writes a test message, if needed.
     * @param writer
     * @param productionRecipients
     */
    public void writeDevelopmentEmailNoticeIfNeeded( PrintWriter writer, String productionRecipientsString ) {
        if( !isProductionServer() ) {
            writeDevelopmentEmailNotice( writer,  productionRecipientsString);
        }
    }

    
    /**
     * Create a standard MailMessage for messages sent from OpenReports.
     * 
     * @param recipients the actual recipients.
     * @return
     */
    public MailMessage createMailMessage( String recipients ) {
        MailMessage mail = new MailMessage();
        mail.setSender( getEmailSender() );
        mail.parseRecipients( recipients );
        mail.parseReplyTos( getReplyTos() );
        mail.setBounceAddress( getReplyTos() );
        return mail;
    }


    public String getBaseURLString() {
        StringBuffer buf = new StringBuffer();
        buf.append( getServletHostName() );
        buf.append( ":" );
        buf.append( getServletPort() );
        buf.append( '/' );
        buf.append( getServletRoot() );
        return buf.toString();
    }

    public boolean getBooleanProperty( String propertyName ) {
        try {
            ORProperty property = propertiesProvider.getProperty( propertyName );
            return Boolean.parseBoolean( property.getValue() );
        } catch( ProviderException pe ) {
            throw new RuntimeException( "Unexpected Exception getting property, " + propertyName );
        }
    }

    public String getDescription( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput ) {
        String description = reportSchedule.getScheduleDescription();
        if( StringUtils.isBlank( description ) ) {
            return reportSchedule.getReport().getName();
        }
        DeliveryMethodGroovyContext groovyContext = new DeliveryMethodGroovyContext( reportInput.getParameters(),
                reportSchedule.getReport().getName(), reportSchedule.getScheduleDescription(), reportInput.getRunDateTime());

        if( groovyContext.isScript( description ) ) {
            try {
                Object evalResult = groovyContext.eval( description );
                if( evalResult instanceof String ) {
                    description = (String) evalResult;
                } else {
                    String className = ( evalResult == null ) ? "null" : evalResult.getClass().getName();
                    log.error( "result of groovy eval is not a String. Ignoring. Value was of type: " + className
                            + " value: " + evalResult.toString() + ". Script was: " + description );
                }
            } catch( ScriptException se ) {
                log.error( "Error trying to evaluate description as groovy, ignoring: " + description, se );
            }
        }
        return description;
    }
    
    public String getEmailSubjectLine( ReportSchedule reportSchedule, ReportEngineInput reportInput, ReportEngineOutput reportOutput) {
        String unparsedSubjectLine = reportSchedule.getEmailSubjectLine();
        if (!StringUtils.isBlank( unparsedSubjectLine )) {
            DeliveryMethodGroovyContext groovyContext = new DeliveryMethodGroovyContext( reportInput.getParameters(),
                    reportSchedule.getReport().getName(), reportSchedule.getScheduleDescription(), reportInput.getRunDateTime() );
            
            return evalToString(unparsedSubjectLine, groovyContext);
        }

        String description = reportSchedule.getScheduleDescription();
        if (!StringUtils.isBlank( description ) ) {
            return description;
        } else {
            return reportSchedule.getReport().getName(); 
        }
    }

    /**
     * Builds the email attachment name. If the email attchement is defined (non-blank), that value
     * is used verbatim, or evaluated as a script. If the email attachment name is not defined, the
     * description is stripped of whitespace and the output extention is used.
     * 
     * @param reportSchedule
     * @param reportInput
     * @param reportOutput
     * @return the email attachment name, if not empty, evaluated as a groovy script, if
     *         appropriate. attachment name is not defined explicitly, fall back to the schedule
     *         description, then to the report name.
     */
    public String getEmailAttachmentName( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput ) {
        String unparsedEmailAttachmentName = reportSchedule.getEmailAttachmentName();
        if( !StringUtils.isBlank( unparsedEmailAttachmentName ) ) {
            DeliveryMethodGroovyContext groovyContext = new DeliveryMethodGroovyContext( reportInput.getParameters(),
                    reportSchedule.getReport().getName(), reportSchedule.getScheduleDescription(), reportInput.getRunDateTime() );
            return evalToString( unparsedEmailAttachmentName, groovyContext ) + reportOutput.getContentExtension();
        }

        String attachmentBaseName = reportSchedule.getScheduleDescription();
        if( StringUtils.isBlank( attachmentBaseName ) ) {
            attachmentBaseName = reportSchedule.getReport().getName();
        }
        return StringUtils.deleteWhitespace( attachmentBaseName ) + reportOutput.getContentExtension();
    }

    String evalToString(String unparsedValue, GroovyContext groovyContext) {
        if( groovyContext.isScript( unparsedValue ) ) {
            try {
                Object evaluatedResult = groovyContext.eval( unparsedValue );
                if( evaluatedResult instanceof String ) {
                    return (String) evaluatedResult;
                } else {
                    String className = ( evaluatedResult == null ) ? "null" : evaluatedResult.getClass().getName();
                    log.error( "result of groovy evaluation is not a String. Ignoring. Value was of type: " + className
                            + " value: " + evaluatedResult.toString() + ". Script was: " + unparsedValue );
                }
            } catch( ScriptException se ) {
                log.error( "Error trying to evaluate description as groovy, ignoring: " + unparsedValue, se );
            }
        }
        return unparsedValue;
    }
    
    
    /**
     * Parses a value, scripting it as necessary.
     * 
     * @param scriptableValue a value which may or may not be scripted
     * @param reportSchedule
     * @param reportInput
     * @return the scripted value, converted to a string, or the original value if it the script is
     *         invalid.
     */
    public String parseScriptableToString( String scriptableValue, GroovyContext groovyContext ) {
        if( !groovyContext.isScript( scriptableValue ) ) {
            return scriptableValue;
        }

        try {
            Object evalResult = groovyContext.eval( scriptableValue );
            if( evalResult instanceof String ) {
                return (String) evalResult;
            } else {
                String actualTypeName = ( evalResult == null ) ? "null" : evalResult.getClass().getName();
                log.warn( "result of groovy eval is not a String. " + "Converting using toString.  Actual type: "
                        + actualTypeName + " value: " + evalResult.toString() + ". Script was: " + scriptableValue );
                return ( evalResult == null ) ? "null" : evalResult.toString();
            }
        } catch( ScriptException se ) {
            log.error( "Error trying to evaluate description as groovy, ignoring: " + scriptableValue, se );
            return scriptableValue;
        }
    }
    

    /**
     * @return the prefix to be used for any file output, for this environment
     */
    public String getDevelopmentFileRoot() {
        return getExpectedProperty( ORProperty.DEVELOPMENT_FILE_ROOT );
    }

    /**
     * @return the recipients defined for development environments
     */
    public String getDevelopmentEmailRecipients() {
        return getExpectedProperty( ORProperty.DEVELOPMENT_EMAIL );
    }
    
    /**
     * @return the email recipients for admin (e.g.: failure email) messages. 
     */
    public String getAdminEmailRecipients() {
        return getDevelopmentEmailRecipients();
    }
    
    /**
     * @param orignialRecipients
     * @return The email recipients based on the environment (DEV, QA, PROD).
     */
    public String getEffectiveEmailRecipients(String orignialRecipients) {
       //Commented for EC-10835
    	/*if (isProductionServer()) {
            return orignialRecipients;    
        } else {
            return getDevelopmentEmailRecipients();
        }*/
    	if (orignialRecipients!=null) {
            return orignialRecipients;    
    	} else {
            return getDevelopmentEmailRecipients();
    	}
    		
    }
    

    public String getEmailSender() {
        // return "it-reporting@" + getServletHostName();
    	return "sbalantrapu@teramedica.com";
    }

    public String getReplyTos() {
        return getProperty( ORProperty.MAIL_REPLY_TO, "sbalantrapu@teramedica.com" );
    }

    public String getServletHostName() { // TODO: figure this out from runtime context.
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch( UnknownHostException e ) {
            return "unknownhost";
        }
    }

    public boolean isCommandDeliveryEnabled() {
        if( isProductionServer() ) {
            return true;
        } else {
            return getBooleanProperty( ORProperty.DEVELOPMENT_ENABLE_COMMAND_DELIVERY );
        }
    }

    public boolean isProductionServer() {
        boolean result = getBooleanProperty( ORProperty.IS_PROD_SERVER );
        return result;
    }

    /**
     * Gets a property that must be defined. It is an error if it is not.
     * 
     * @param propertyName
     * @return the value of the property with the given name.
     * @throws IllegalStateException if the property is not defined.
     * @throws RuntimeException if there is an error getting the property.
     */
    protected String getExpectedProperty( String propertyName ) {
        try {
            ORProperty property = propertiesProvider.getProperty( propertyName );
            if( property == null ) {
                throw new IllegalStateException( "No property, " + propertyName + ", defined" );
            }
            return property.getValue();
        } catch( ProviderException pe ) {
            throw new RuntimeException( "Unexpected Exception getting property, " + propertyName, pe );
        }
    }

    protected String getProperty( String propertyName, String defaultValue ) {
        try {
            ORProperty property = propertiesProvider.getProperty( propertyName );
            if( property == null ) {
                log.info( "No property, " + propertyName + ", defined, using default: " + defaultValue );
                return defaultValue;
            } else if( property.getValue() == null ) {
                log.info( "Null value for property, " + propertyName + ", using default: " + defaultValue );
                return defaultValue;
            } else {
                return property.getValue();
            }
        } catch( ProviderException pe ) {
            throw new RuntimeException( "Unexpected Exception getting property, " + propertyName, pe );
        }
    }

    private Object getServletPort() {
        return 8080; // TODO: figure this out from runtime context.
    }

    private Object getServletRoot() {
        return "openreports"; // TODO: figure this out from runtime context.
    }

}
