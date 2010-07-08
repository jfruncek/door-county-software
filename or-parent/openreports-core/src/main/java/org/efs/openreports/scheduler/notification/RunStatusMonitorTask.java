package org.efs.openreports.scheduler.notification;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.providers.PropertiesProvider;
import org.efs.openreports.providers.PropertiesSupport;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.TimeU;

public class RunStatusMonitorTask extends TimerTask {

    protected static Logger log = Logger.getLogger( RunStatusMonitorTask.class.getName() );
    private PropertiesSupport propertiesSupport;
    private RunStatusRegistry runStatusRegistry;

    @Override
    public void run() {

        try {
            // the threshold for each of these is different
            logInactiveRunStatuses( );
            warnOfInactiveRunStatuses( );
            removeInactiveRunStatuses();
        } catch( Exception e ) {
            log.error( "Exception running RunStatusMonitorTask: " + e.getMessage(), e );
        }
    }

    private void removeInactiveRunStatuses() throws ProviderException {
        int minutesInactivity  =  propertiesSupport.getIntValue( ORProperty.RUN_STATUS_INACTIVITY_TIMEOUT, TimeU.MINUTES_IN_DAY);
        List<RunStatus> currentRunStatuses = getInactiveRunStatuses( TimeU.I.shiftTimeByMinutes( -minutesInactivity ) );
        for( RunStatus rootStatus :  currentRunStatuses ) {
            runStatusRegistry.removeRootStatusWithNotification( rootStatus );
        }
    }
    
    private void warnOfInactiveRunStatuses() throws ProviderException {
        int minutesInactivity  =  propertiesSupport.getIntValue( ORProperty.RUN_STATUS_INACTIVITY_WARNING, 60 );
        List<RunStatus> currentRunStatuses = getInactiveRunStatuses( TimeU.I.shiftTimeByMinutes( -minutesInactivity ) );
        for( RunStatus rootStatus :  currentRunStatuses ) {
            runStatusRegistry.sendInactivityWarning( rootStatus );
        }
    }

    

    private void logInactiveRunStatuses() throws ProviderException {
        int minutesInactivity = propertiesSupport.getIntValue( ORProperty.RUN_STATUS_INACTIVITY_LOGGING, 10 );
        List<RunStatus> currentRunStatuses = getInactiveRunStatuses( TimeU.I.shiftTimeByMinutes( -minutesInactivity ) );

        log.info( "RunStatusMonitorTask executing: There are currently " + currentRunStatuses.size()
                + " root level reports inactive for " + minutesInactivity + " or more minutes." );

        if( currentRunStatuses.size() > 0 ) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter( baos );
            writer.println();
            writer.println( "-----------------------------------------------------------------------" );
            writer.println( "RunStatuses:" );
            writer.println( "-----------------------------------------------------------------------" );
            writer.println();
            for( RunStatus rootStatus : currentRunStatuses ) {
                writeStatus( writer, rootStatus, 1 );
                writer.println();
            }
            writer.println();
            writer.flush();
            log.info( baos.toString() );
        }
    }

    public void setPropertiesProvider( PropertiesProvider propertiesProvider ) {
        propertiesSupport = new PropertiesSupport(propertiesProvider);
    }

    public void setRunStatusRegistry( RunStatusRegistry runStatusRegistry ) {
        this.runStatusRegistry = runStatusRegistry;
    }

    private List<RunStatus> getInactiveRunStatuses( long sinceTimeInMillesconds ) {
        List<RunStatus> inactiveRunStatuses = new ArrayList<RunStatus>();
        for( RunStatus runStatus : runStatusRegistry.getRootLevelStatuses() ) {
            if( runStatus.getLastActivity() < sinceTimeInMillesconds ) {
                inactiveRunStatuses.add( runStatus );
            }
        }
        return inactiveRunStatuses;
    }

    private void writeStatus( PrintWriter writer, RunStatus runStatus, int indentLevel ) {
        String indent = StringUtils.rightPad( "", 3 * indentLevel );
        boolean isComplete = runStatus.isComplete();
        writer.append( indent ).append(isComplete ? " " : "*");
        writer.append( runStatus.getName() ).append( ": " ).append( runStatus.getDescription() )
                .append( ": " ).append( isComplete ? "complete" : "incomplete" );
        writer.println();
        for( RunStatus child : runStatus.getChildren() ) {
            writeStatus( writer, child, indentLevel + 1 );
        }
    }

}
