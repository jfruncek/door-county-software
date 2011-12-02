package org.efs.openreports.engine.querycache;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A block of results
 * 
 * @author mconner
 * 
 */
public class QueryResultsBlock implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    ArrayList<Serializable[]> rows = new ArrayList<Serializable[]>();

    public void add( Serializable[] row ) {
        rows.add( row );
    }

    public Serializable[] getRow( int i ) {
        return rows.get( i );
    }

    public void setRow( int index, Serializable[] row ) {
        rows.set( index, row );
    }

    public int size() {
        return rows.size();
    }
}
