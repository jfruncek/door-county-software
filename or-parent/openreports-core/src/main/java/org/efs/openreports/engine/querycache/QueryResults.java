package org.efs.openreports.engine.querycache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.lang.StringUtils;

public class QueryResults {
    private boolean atMaxRows;

    private QueryResultsCache queryResultsCache;
    private String lastSortProperty = null;
    private OrderedQueryResults queryOrderQueryResults;

    private Map<String, OrderedQueryResults> sortedResults = new HashMap<String, OrderedQueryResults>();

    public QueryResults( QueryResultsCache queryResultsCache, DynaClass resultsDescriptor ) {
        this.queryResultsCache = queryResultsCache;
        queryOrderQueryResults = new OrderedQueryResults( resultsDescriptor );
    }

    public void add( Serializable[] row ) {
        queryOrderQueryResults.add( queryResultsCache, row );
        clearSorted();
    }

    public Serializable[] get( int index ) {
        markLastSortProperty( null );
        return queryOrderQueryResults.get( queryResultsCache, index );
    }

    private void markLastSortProperty( String propertyName ) {
        if( !StringUtils.equals( lastSortProperty, propertyName ) ) {
            clearAllBut( propertyName );
        }
    }

    public DynaBean getAsDynaBean( int index ) {
        markLastSortProperty( null );
        return queryOrderQueryResults.getAsDynaBean( queryResultsCache, index );
    }

    public int getCount() {
        return queryOrderQueryResults.getCount();
    }

    public String getQueryKey() {
        return queryOrderQueryResults.getQueryKey();
    }

    public List<Serializable[]> getResults( int startIndex, int endIndexNonInclusive, String sortPropertyName,
            boolean ascending ) {
        OrderedQueryResults results = getOrderedResults( sortPropertyName );
        return results.getResults( queryResultsCache, startIndex, endIndexNonInclusive, ascending );
    }

    public List<DynaBean> getResultsAsDynaBeans( int startIndex, int endIndexNonInclusive, String sortPropertyName,
            boolean ascending ) {
        OrderedQueryResults results = getOrderedResults( sortPropertyName );
        return results.getResultsAsDynaBeans( queryResultsCache, startIndex, endIndexNonInclusive, ascending );
    }

    public DynaClass getResultsDescriptor() {
        return queryOrderQueryResults.getResultsDescriptor();
    }

    public boolean isAtMaxRows() {
        return atMaxRows;
    }

    /**
     * Note on the results that there are more records, but the report server is only showing the
     * first n.
     * 
     * @param maxRows
     */
    public void setAtMaxRows( boolean atMaxRows ) {
        this.atMaxRows = atMaxRows;
    }

    public void updateWithDynaBean( DynaBean bean, int index ) {
        markLastSortProperty( null );
        queryOrderQueryResults.updateWithDynaBean( queryResultsCache, bean, index );
    }

    /**
     * Unfortunately, this is an in-memory sort, but only hangs onto the sort attribute. for the
     * entire time It also does some gyrations to minimize the thrashing of getting the blocks from
     * the cache.
     * 
     * @param ehcache
     * @param sortProperty
     * @return
     */
    protected synchronized OrderedQueryResults sort( QueryResultsCache queryResultsCache, String sortPropertyName ) {
        int sortPropertyIndex = queryOrderQueryResults.getPropertyIndex( sortPropertyName );
        int count = queryOrderQueryResults.getCount();
        List<SortRow> sortRows = new ArrayList<SortRow>( count );

        for( int i = 0; i < count; i++ ) {
            Serializable[] row = queryOrderQueryResults.get( queryResultsCache, i );
            Object sortValue = row[sortPropertyIndex];
            // Note: to sort entirely in memory, construct with the row already in place
            sortRows.add( new SortRow( sortValue, i ) );
        }
        Collections.sort( sortRows, new SortRow.SortRowComparator() );

        OrderedQueryResults sortedQueryResults =
                new OrderedQueryResults( queryOrderQueryResults.getResultsDescriptor() );

        for( int i = 0; i < sortRows.size(); i++ ) {
            queryOrderQueryResults.preFetch( queryResultsCache, sortRows, i );
            Serializable[] row = sortRows.get( i ).giveUpRow();
            sortedQueryResults.add( queryResultsCache, row );
        }
        return sortedQueryResults;
    }

    public void setQueryResultsCache( QueryResultsCache queryResultsCache ) {
        this.queryResultsCache = queryResultsCache;
    }

    private void clearSorted() {
        if( sortedResults.size() > 0 ) {
            sortedResults.clear();
        }
    }

    private OrderedQueryResults getOrderedQueryResults( String sortPropertyName ) {
        OrderedQueryResults results = sortedResults.get( sortPropertyName );
        if( results == null ) {
            results = sort( queryResultsCache, sortPropertyName );
            sortedResults.put( sortPropertyName, results );
        }
        return results;
    }

    /**
     * To avoid hanging on to a QueryResultsBlock in some OrderedQueryResults we aren't currently
     * using, we'll clear any but that requested.
     * 
     * @param sortPropertyName
     */
    private void clearAllBut( String sortPropertyName ) {
        for( String propertyName : sortedResults.keySet() ) {
            if( !propertyName.equals( sortPropertyName ) ) {
                sortedResults.get( propertyName ).clearCurrent( queryResultsCache );
            }
        }
        if( sortPropertyName != null ) {
            queryOrderQueryResults.clearCurrent( queryResultsCache );
        }
    }

    private OrderedQueryResults getOrderedResults( String sortPropertyName ) {
        markLastSortProperty( sortPropertyName );
        return ( sortPropertyName == null ) ? queryOrderQueryResults : getOrderedQueryResults( sortPropertyName );
    }

}
