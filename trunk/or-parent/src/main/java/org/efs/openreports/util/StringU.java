package org.efs.openreports.util;

/**
 * Support for String not covered by StringUtils and the like. Usage:
 * 
 * @author mconner
 */
public class StringU {

    /** Standard instance */
    public static final StringU I = new StringU();

    /**
     * Make a string plural based on a count, by adding an s.
     * 
     * @param string
     * @param count
     * @return the singular if count == 1, else plural (e.g. 0 typically reads as plural (0 reports,
     *         1 report).
     */
    public String plural( String singular, int count ) {
        return ( count == 1 ) ? singular : singular + "s";
    }
    

}
