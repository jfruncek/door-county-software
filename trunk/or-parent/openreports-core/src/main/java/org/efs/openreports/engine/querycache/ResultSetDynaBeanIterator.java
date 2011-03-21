package org.efs.openreports.engine.querycache;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.sqlsupport.SqlU;

/**
 * 
 * @author mconner
 */
public class ResultSetDynaBeanIterator implements Iterator<DynaBean> {
    protected static Logger log = Logger.getLogger( OrderedQueryResults.class );
    boolean atEnd = false;
    DynaBean currentRow = null;
    DynaClass resultsDescriptor;
    ResultSet resultSet;

    public ResultSetDynaBeanIterator( DynaClass resultsDescriptor, ResultSet resultSet ) {
        this.resultsDescriptor = resultsDescriptor;
        this.resultSet = resultSet;
    }

    public boolean hasNext() {
        if( currentRow != null ) {
            return true;
        } 
        loadNext();
        return currentRow != null;
    }

    public DynaBean next() {
        if( !hasNext() ) {
            throw new NoSuchElementException( "no more rows" );
        } else {
            DynaBean result = currentRow;
            currentRow = null;
            return result;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException( "can't remove from this thing" );
    }


    private DynaBean convertToDynaBean( Serializable[] row ) {
        DynaBean bean = new BasicDynaBean( resultsDescriptor );
        DynaProperty[] properties = resultsDescriptor.getDynaProperties();
        for( int i = 0; i < properties.length; i++ ) {
            bean.set( properties[i].getName(), row[i] );
        }
        return bean;
    }

    private void loadNext() {
        if( atEnd ) {
            return;
        }

        try {
            if( resultSet.next() ) {
                Serializable[] row = SqlU.I.createRow( resultSet, resultsDescriptor );
                currentRow = convertToDynaBean( row );
            } else {
                atEnd = true;
            }
        } catch( SQLException e ) {
            atEnd = true;
            log.error( "error retrieving row from result set" );
        }
    }

}
