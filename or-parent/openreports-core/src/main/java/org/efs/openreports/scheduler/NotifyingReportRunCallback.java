package org.efs.openreports.scheduler;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.delivery.DeliverySupport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.scheduler.notification.RunStatusRegistry;


public class NotifyingReportRunCallback implements ReportRunCallback {

    protected static Logger log = Logger.getLogger( NotifyingReportRunCallback.class.getName() );

    private DeliverySupport deliverySupport;
    private MailProvider mailProvider;
    private RunStatusRegistry runStatusRegistry;

    public void callback( ReportRunData reportRunData ) {
        log.info("in NotifyingScheduledReportCallback : [" + reportRunData.getReport().getName() + "]");
        if( isFailure( reportRunData ) ) {
            sendReportFailureNotification( reportRunData );
        }
        if( runStatusRegistry.isValidReferenceKeyFormat( reportRunData.getRequestId() ) ) {
            log.info( "Scheduled report completed: " + reportRunData.getReport() + " executed: " + reportRunData.getStatus() );
            markReportCompleted( reportRunData );
        }
    }

    public void setRunStatusRegistry( RunStatusRegistry runStatusRegistry ) {
        this.runStatusRegistry = runStatusRegistry;
    }

    public void setDeliverySupport( DeliverySupport deliverySupport ) {
        this.deliverySupport = deliverySupport;
    }

    public void setMailProvider( MailProvider mailProvider ) {
        this.mailProvider = mailProvider;
    }

    private String buildFailureNotificationText( ReportRunData reportLog, String productionRecipientsString,
            String effectiveRecipientsString ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( baos );
        writeFailureMessage( writer, reportLog, effectiveRecipientsString );
        deliverySupport.writeDevelopmentEmailNoticeIfNeeded( writer, productionRecipientsString );
        writer.flush();
        writer.close();
        return baos.toString();
    }

    private String buildFailureRecipients(ReportRunData reportLog) {
        String productionRecipients = reportLog.getUser().getEmail() + "," + deliverySupport.getAdminEmailRecipients();
        return productionRecipients ;
    }
    
    private boolean isFailure( ReportRunData reportRunData ) {
        return !reportRunData.isSuccess();
    }

    private void markReportCompleted( ReportRunData reportLog ) {
        List<String> detailMessages = makeMessageList( reportLog.getMessage() );
        if( runStatusRegistry.isValidReferenceKeyFormat(reportLog.getRequestId()) ) {
            runStatusRegistry.markRunCompleteWithNotification( reportLog.getRequestId(), reportLog.getStatus(),
                    reportLog.isSuccess(), detailMessages );
        }
    }

    private List<String> makeMessageList( String message ) {
        List<String> messages = new ArrayList<String>();
        if (message != null) {
            messages.add( message );    
        }
        return messages;
    }

    private void sendReportFailureNotification( ReportRunData reportLog ) {
        String productionRecipientsString = buildFailureRecipients( reportLog );
        String effectiveRecipentsString = deliverySupport.getEffectiveEmailRecipients( productionRecipientsString );
        List<String> effectiveRecipients = MailMessage.parseAddressList( effectiveRecipentsString );
        for( String recipient : effectiveRecipients ) {
            MailMessage mail = deliverySupport.createMailMessage( recipient );
            mail.setSubject( "Report Failed: " + reportLog.getReport().getName() + ": " + reportLog.getStatus() );
            mail.setText( buildFailureNotificationText( reportLog, productionRecipientsString, effectiveRecipentsString) );
            try {
                mailProvider.sendMail( mail );
                log.info( "Sent report execution failure email to recipient: " + recipient );
            } catch( ProviderException pe ) {
                log.error( "Could not send abnormal completion report to " + recipient );
            }
        }
    }

    private void writeFailureMessage(PrintWriter writer, ReportRunData reportLog, String recipientsString) {
        writer.println();
        writer.println();
        writer.println( "Report: " + reportLog.getReport().getName() );
        writer.println( "Started: " + reportLog.getStartTime() );
        writer.println( "Finished: " + reportLog.getEndTime() );
        writer.println( "\nStatus: " + reportLog.getStatus() );
        writer.println( "\nMessage: " + reportLog.getMessage() == null ? "(none)" : reportLog.getMessage() );
        writer.println(  );
        writer.println(  );
        if( reportLog.getDeliveryDatas() != null ) {
            for( ReportDeliveryData deliveryLog : reportLog.getDeliveryDatas() ) {
                writer.println( "\nDelivery Method: " + deliveryLog.getDeliveryMethod() );
                writer.println( "\nStarted: " + deliveryLog.getStartTime() );
                writer.println( "\nFinished: " + deliveryLog.getEndTime() );
                writer.println( "\nStatus: " + deliveryLog.getStatus() );
                writer.println( "\nMessage: " + 
                        (deliveryLog.getMessage() == null ? "(none)" : deliveryLog.getMessage()) );
                writer.println( "\n" );
            }
        }
        List<String> recipients = MailMessage.parseAddressList( recipientsString );
        
        if( recipients.size() > 1 ) {
            writer.println( "\n" );
            writer.println( "This message was sent to the following:" );
            for( String recipient : recipients ) {
                writer.println( "   " + recipient );
            }
        }
    }

}
