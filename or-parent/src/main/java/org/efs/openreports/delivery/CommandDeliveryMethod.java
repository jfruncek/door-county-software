package org.efs.openreports.delivery;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.DeliveredReport;
import org.efs.openreports.objects.ReportDeliveryLog;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.util.IoU;

/**
 * Delivers a reports by way of a command line sub-process. The data is streamed to the command.
 * 
 * @author mconner
 */
public class CommandDeliveryMethod implements DeliveryMethod {
    protected static Logger log = Logger.getLogger( CommandDeliveryMethod.class.getName() );

    private DeliverySupport deliverySupport;

    public List<ReportDeliveryLog> deliverReport( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput ) {
        List<ReportDeliveryLog> result = new ArrayList<ReportDeliveryLog>();
        if( deliverySupport.isCommandDeliveryEnabled() ) {
            ReportDeliveryLog logEntry = writeToPrintCommand( reportSchedule, reportOutput );
            result.add( logEntry );
        } else {
            log.info( "command delivery not enabled. If it were, we would run the command: ["
                    + reportSchedule.getPrintCommand() + "]" );
        }
        return result;
    }

    public byte[] getDeliveredReport( DeliveredReport deliveredReport ) throws DeliveryException {
        throw new DeliveryException( "Method getDeliveredReport not implemented by LanDeliveryMethod" );
    }

    public DeliveredReport[] getDeliveredReports( ReportUser user ) throws DeliveryException {
        throw new DeliveryException( "Method getDeliveredReports not implemented by LanDeliveryMethod" );
    }

    public void setDeliverySupport( DeliverySupport deliverySupport ) {
        this.deliverySupport = deliverySupport;
    }

    private void writeDataToProcess( Process process, ReportSchedule reportSchedule, ReportEngineOutput reportOutput )
            throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream( process.getOutputStream() );
        try {
            reportOutput.getContentManager().copyToStream( bos );
        } finally {
            IoU.I.safeFlushAndClose( bos, "process output stream: " + reportSchedule.getScheduleDescription() );
        }
    }

    private ReportDeliveryLog writeToPrintCommand( ReportSchedule reportSchedule, ReportEngineOutput reportOutput ) {
        String printCommand = reportSchedule.getPrintCommand();
        ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog( ReportConstants.DeliveryMethod.PRINTER.getName(),
                printCommand );
        Process process;
        try {
            process = Runtime.getRuntime().exec( printCommand );
            try {
                writeDataToProcess( process, reportSchedule, reportOutput );
                int result = process.waitFor();
                if( result != 0 ) {
                    log.warn( "Got non-zero result for print command " + printCommand + " on scheduled report:"
                            + reportSchedule.getScheduleDescription() );
                }
                deliveryLogEntry.markSuccess(); // TODO???

                // TODO: should do this in another thread so that we can interrupt it.
            } catch( InterruptedException ie ) {
                deliveryLogEntry.markFailure( "interrupted waiting for print command to complete", ie );
                log.error( printCommand.toString(), ie );
            }
        } catch( IOException ioe ) {
            deliveryLogEntry.markFailure( "IOException running print process", ioe );
            log.error( deliveryLogEntry, ioe );
        }
        return deliveryLogEntry;

    }

}
