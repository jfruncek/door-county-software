package org.efs.openreports.scheduler.notification;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.delivery.DeliverySupport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.TimeU;

public class RunStatusNotificationSupport {
    protected static Logger log = Logger.getLogger( RunStatusNotificationSupport.class.getName() );

    private DeliverySupport deliverySupport;
    private MailProvider mailProvider;

    public void sendRunStatusCompleteNotification( RunStatus runStatus ) {
        String effectiveRecipients = getEffectiveRecipients( runStatus );
        try {
            MailMessage mail = deliverySupport.createMailMessage( effectiveRecipients );
            String subject = runStatus.getRunType().getLabel() + ", " + runStatus.getName() + ", completed ";
            RunStatus.Statistics statistics = runStatus.getSubtreeStatistics();
            if( statistics.hasFailures() ) {
                subject += "with " + statistics.getFailureCount() + " errors";
            } else {
                subject += "successfully";
            }
            mail.setSubject( subject );
            mail.setText( buildMessageText( runStatus, statistics ) );
            mailProvider.sendMail( mail );
            log.info( "Sent completion email to recipients: " + effectiveRecipients );
        } catch( ProviderException pe ) {
            log.error( "Could not send completion report to " + effectiveRecipients );
        }
    }
    
    /**
     * If something is incomplete, we don't send it to the original user, but to the administrators.
     * 
     * @param runStatus
     */
    public void sendRunStatusIncompleteNotification( RunStatus runStatus, boolean withRemove ) {
        String effectiveRecipients = getEffectiveAdminRecipients();
        try {
            MailMessage mail = deliverySupport.createMailMessage( effectiveRecipients );
            
            String subject;
            if( withRemove ) {
                subject =
                        "Status Tracking on " + runStatus.getRunType().getLabel() + ", " + runStatus.getName()
                                + ", remove due to no activity ";
            } else {
                subject =
                        "Warning: No activity on " + runStatus.getRunType().getLabel() + ", " + runStatus.getName();
            }
            RunStatus.Statistics statistics = runStatus.getSubtreeStatistics();
            mail.setSubject( subject );
            mail.setText( buildIncompleteMessageText( runStatus, statistics, withRemove ) );
            mailProvider.sendMail( mail );
            log.info( "Sent inactivity email to recipients: " + effectiveRecipients );
        } catch( ProviderException pe ) {
            log.error( "Could not send inactivity email report to " + effectiveRecipients );
        }
    }
    

    public void setDeliverySupport( DeliverySupport deliverySupport ) {
        this.deliverySupport = deliverySupport;
    }

    public void setMailProvider( MailProvider mailProvider ) {
        this.mailProvider = mailProvider;
    }

    private String buildIncompleteMessageText( RunStatus runStatus, RunStatus.Statistics statistics, boolean withRemove ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( baos );
        writer.println();
        writer.print( " There has been no status update activity on " );
        writer.print( runStatus.getRunType().getLabel() );
        writer.print( ", " );
        writer.print( runStatus.getName() );
        writer.print( " since " );
        writer.print( TimeU.I.formatTimeForMessage( runStatus.getLastActivity() ) );
        writer.print( "." );
        if( withRemove ) {
            writer.print( "  Removing status tracking (it is possible reports are still running)." );
        }
        writer.println();
        writeStatus( runStatus, writer );
        deliverySupport.writeDevelopmentEmailNoticeIfNeeded( writer, runStatus.getNotificationRecipients() );
        writer.flush();
        writer.close();
        return baos.toString();
    }
    
    private String buildMessageText( RunStatus status, RunStatus.Statistics statistics ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( baos );
        writeStatus( status, writer );
        deliverySupport.writeDevelopmentEmailNoticeIfNeeded( writer, status.getNotificationRecipients() );
        writer.flush();
        writer.close();
        return baos.toString();
    }
    

    private String getEffectiveAdminRecipients( ) {
        String productionRecipients = deliverySupport.getAdminEmailRecipients();
        String effectiveRecipients = deliverySupport.getEffectiveEmailRecipients( productionRecipients );
        return effectiveRecipients;
    }

    private String getEffectiveRecipients( RunStatus runStatus ) {
        String productionRecipients = runStatus.getNotificationRecipients();
        String effectiveRecipients = deliverySupport.getEffectiveEmailRecipients( productionRecipients );
        return effectiveRecipients;
    }

    private void indent( PrintWriter writer, int indentLevel ) {
        for( int i = 0; i < indentLevel; i++ ) {
            writer.append( "   " );
        }
    }

    private List<RunStatus> sort( Collection<RunStatus> children ) {
        ArrayList<RunStatus> sortingChildren = new ArrayList<RunStatus>(children);
        Collections.sort( sortingChildren, new Comparator<RunStatus>() {
           public int compare( RunStatus o1, RunStatus o2 ) {
               return o1.getRefererenceKey().compareTo( o2.getRefererenceKey() );
            } 
        });
        return sortingChildren;
    }

    private void writeMessage( String message, PrintWriter writer, int indentLevel ) {
        String[] lines = message.split( "[\\r]?\\n" );
        for( int i = 0; i < lines.length; i++ ) {
            indent( writer, indentLevel + ( ( i == 0 ) ? 0 : 1 ) );
            writer.println( lines[i] );
        }
    }
    
    private void writeStatus( RunStatus runStatus, PrintWriter writer ) {
        writer.print( runStatus.getRunType().getLabel() );
        writer.print( " : " );
        writer.print( runStatus.getName() );
        writer.println();
        writer.println();
        RunStatus.Statistics statistics = runStatus.getSubtreeStatistics();
        if( statistics.hasFailures() ) {
            writer.println( "Failures: " + statistics.getFailureCount() );
            writeSubtreeFailures( runStatus, writer, 1 );
            writer.println();
        }
        if( statistics.hasLeafSuccesses() ) {
            writer.println( "Completed: " + statistics.getLeafSuccessCount() );
            writeSubtreeSuccesses( runStatus, writer, 1 );
            writer.println();
        }
        if (statistics.hasIncompletes()) { // should never happen if complete
            writer.println( "Incomplete: " + statistics.getIncompleteCount() );
            writeSubtreeIncompletes( runStatus, writer, 1 );
            writer.println();
        }
    }
    

    /**
     * Assumption is that there are errors at this node or below.
     * @param runStatus
     * @param writer
     * @param indentLevel
     */
    private void writeSubtreeFailures( RunStatus runStatus, PrintWriter writer, int indentLevel ) {
        if (runStatus.isFailure() || runStatus.isNonLeaf() ) {
            indent( writer, indentLevel );
            writer.print( runStatus.getName() + ": " + runStatus.getDescription() + ": " );
        }
        if( runStatus.isFailure() ) {
            writer.println( runStatus.getStatusMessage() );
            for( String message : runStatus.getdetailMessages() ) {
                writeMessage(message, writer, indentLevel + 2);
            }
        } else {
            writer.println();
        }
        for( RunStatus child : sort(runStatus.getChildren()) ) {
            if( child.getSubtreeStatistics().hasFailures() ) {
                writeSubtreeFailures( child, writer, indentLevel + 1);
            }
        }
    }

    private void writeSubtreeIncompletes( RunStatus runStatus, PrintWriter writer, int indentLevel ) {
        if (!runStatus.isComplete() || runStatus.isNonLeaf() ) {
            indent( writer, indentLevel );
            writer.print( runStatus.getName() + ": " + runStatus.getDescription() + ": " );
        }
        if( !runStatus.isComplete() ) {
            String status = runStatus.getStatusMessage();
            writer.println( (status == null) ? "" : status );
            for( String message : runStatus.getdetailMessages() ) {
                writeMessage(message, writer, indentLevel + 2);
            }
        } else {
            writer.println();
        }
        for( RunStatus child : sort(runStatus.getChildren()) ) {
            if( child.getSubtreeStatistics().hasIncompletes() ) {
                writeSubtreeIncompletes( child, writer, indentLevel + 1);
            }
        }
    }

    /**
     * Only writing the leaf nodes TODO: maybe we want to add something to the status to indicate if
     * it is report worthy, of itself
     * 
     * @param runStatus
     * @param writer
     * @param indentLevel
     */
    private void writeSubtreeSuccesses( RunStatus runStatus, PrintWriter writer, int indentLevel ) {
        if (runStatus.isSuccess() || runStatus.isNonLeaf() ) {
            indent( writer, indentLevel );
            writer.print( runStatus.getName() + ": " + runStatus.getDescription() + ": " );
        }
        if( runStatus.isSuccess() && runStatus.isLeaf() ) { // only show status, detail messages on leafs
            writer.println( runStatus.getStatusMessage() );
            for( String message : runStatus.getdetailMessages() ) {
                writeMessage(message, writer, indentLevel + 2);
            }
        } else {
            writer.println();
        }
        for( RunStatus child : sort(runStatus.getChildren()) ) {
            if( child.getSubtreeStatistics().hasLeafSuccesses() ) {
                writeSubtreeSuccesses( child, writer, indentLevel + 1);
            }
        }
    }

}
