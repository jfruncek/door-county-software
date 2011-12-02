package org.efs.openreports.scheduler.notification;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Builds reference keys to be used with the RunStatus and RunStatusRegistry for reportSchedules.
 * Basically, we are looking for a unique key within the registry.
 * 
 * @author mconner
 */
public class ReferenceKeyBuilder {
    private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd.HHmmss.SSS" );

    private final String PREFIX = "RUNSTATUS:";
    private final String INSTANCE_PREFIX = PREFIX + dateFormat.format( new Date() ) + ":";

    /** Unique for a server instance (good enough for our purposes) */
    private int runSequence = 0;

    public boolean isCorrectFormat( String referenceKeyCandidate ) {
        return referenceKeyCandidate != null && referenceKeyCandidate.startsWith( PREFIX );
    }

    /**
     * While we could use the RunStatus to provide some meaning to the key, we really only need
     * something unique.
     * 
     * @param runStatus currently unused.
     * @return A reasonably unique value. Uses the millisecond the builder was initialized(loaded),
     *         the ms the key was made, plus a sequence number unique to the instance.
     */
    public synchronized String makeReferenceKey( RunStatus runStatus ) {
        return INSTANCE_PREFIX + dateFormat.format( new Date() ) + ':' + ( ++runSequence );
    }

    synchronized int getNextSequence() {
        return ++runSequence;
    }

}
