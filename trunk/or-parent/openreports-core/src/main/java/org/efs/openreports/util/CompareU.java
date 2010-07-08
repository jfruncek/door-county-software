package org.efs.openreports.util;

/**
 * Compare utilities.
 * 
 * @author mconner
 * 
 */
public class CompareU {
    public static final CompareU I = new CompareU();

    public int compareNullLow( Object o1, Object o2 ) {
        if( o1 == o2 )
            return 0;
        if( o1 == null )
            return -1;
        if( o2 == null )
            return 1;
        if( o1 instanceof Comparable ) {
            return compareWithSuppressUnchecked( o1, o2 );
        } else {
            return 0;
        }
    }

    @SuppressWarnings( "unchecked" )
    private int compareWithSuppressUnchecked( Object v1, Object v2 ) {
        return ( (Comparable<Object>) v1 ).compareTo( v2 );
    }

}
