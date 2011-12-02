package org.efs.openreports.engine;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.querycache.OrderedQueryResults;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.engine.querycache.QueryResultsCache;
import org.efs.openreports.engine.sqlsupport.SqlU;

public class QueryResultsBuilder {
    protected static Logger log = Logger.getLogger( OrderedQueryResults.class );

    /** */
    private static final long serialVersionUID = 1L;

    private QueryResultsCache queryResultsCache;

    public QueryResultsBuilder() {
    }

    /**
     * <p>
     * Return the name of this DynaClass (analogous to the <code>getName()</code> method of
     * <code>java.lang.Class</code), which
     * allows the same <code>DynaClass</code> implementation class to support different dynamic
     * classes, with different sets of properties.
     * </p>
     */
    public String getName() {

        return( this.getClass().getName() );

    }

    /**
     * <p>
     * Instantiate and return a new DynaBean instance, associated with this DynaClass.
     * <strong>NOTE</strong> - This operation is not supported, and throws an exception.
     * </p>
     * 
     * @throws IllegalAccessException if the Class or the appropriate constructor is not accessible
     * @throws InstantiationException if this Class represents an abstract class, an array class, a
     *             primitive type, or void; or if instantiation fails for some other reason
     */
    public DynaBean newInstance() throws IllegalAccessException, InstantiationException {

        throw new UnsupportedOperationException( "newInstance() not supported" );

    }

    public void setQueryResultsCache( QueryResultsCache  queryResultsCache  ) {
        this.queryResultsCache = queryResultsCache ;
    }

    /**
     * <p>
     * Copy the column values for each row in the specified <code>ResultSet</code> into a newly
     * created {@link DynaBean}, and add this bean to the list of {@link DynaBean}s that will later
     * by returned by a call to <code>getRows()</code>.
     * </p>
     * 
     * @param resultSet The <code>ResultSet</code> whose data is to be copied
     * @throws SQLException if an error is encountered copying the data
     */
    protected void copy( ResultSet resultSet, QueryResults results, Integer maxRows ) throws SQLException {
        while( underRowLimit( results.getCount(), maxRows ) && resultSet.next() ) {
            results.add( createRow( resultSet, results.getResultsDescriptor() ) );
        }
    }

    /**
     * <p>
     * Introspect the metadata associated with our result set, and populate the
     * <code>properties</code> and <code>propertiesMap</code> instance variables.
     * </p>
     * 
     * @param resultSet The <code>resultSet</code> whose metadata is to be introspected
     * @throws SQLException if an error is encountered processing the result set metadata
     */
    protected QueryResults introspect( ResultSet resultSet ) throws SQLException {
        DynaClass dynaClass = SqlU.I.buildDynaClassFromMetaData( resultSet );
        return new QueryResults( queryResultsCache, dynaClass );
    }

    /**
     * <p>
     * Construct a new {@link RowSetDynaClass} for the specified <code>ResultSet</code>. The
     * property names corresponding to the column names in the result set will be lower cased or
     * not, depending on the specified <code>lowerCase</code> value.
     * </p>
     * <p/>
     * 
     * @param resultSet The result set to be wrapped
     * @throws NullPointerException if <code>resultSet</code> is <code>null</code>
     * @throws SQLException if the metadata for this result set cannot be introspected
     */
    QueryResults build( ResultSet resultSet, Integer maxRows ) throws SQLException {
        QueryResults results = introspect( resultSet );
        copy( resultSet, results, maxRows );
        if( atRowLimit( results.getCount(), maxRows ) ) {
            if( resultSet.next() ) {
                results.setAtMaxRows( true );
            }

        }
        return results;
    }

    private boolean atRowLimit( int size, Integer maxRows ) {
        return hasMaxRows( maxRows ) && ( size == maxRows );
    }

    private Serializable[] createRow( ResultSet resultSet, DynaClass dynaClass ) throws SQLException {
        return SqlU.I.createRow( resultSet, dynaClass );
    }

    private boolean hasMaxRows( Integer maxRows ) {
        return maxRows != null;
    }

    private boolean underRowLimit( int size, Integer maxRows ) {
        return !hasMaxRows( maxRows ) || size < maxRows;
    }
}
