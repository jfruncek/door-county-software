package org.efs.openreports.util;

import org.apache.log4j.Logger;

/**
 * A very simple util class for Releasables.
 */
public class ReleasableU {
    protected static final Logger log = Logger.getLogger( ReleasableU.class.getName() );

    public static final ReleasableU I = new ReleasableU();

    /**
     * Convenience to call release with null check.
     * 
     * @param releasable
     */
    public void safeRelease( Releasable releasable ) {
        if( releasable != null ) {
            releasable.release();
        }
    }

}
