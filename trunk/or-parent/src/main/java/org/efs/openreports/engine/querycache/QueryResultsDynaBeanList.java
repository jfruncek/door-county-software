package org.efs.openreports.engine.querycache;

import java.util.AbstractList;

import org.apache.commons.beanutils.DynaBean;

/**
 * Wraps a queryResults to get the rows for a QueryResult in query order.
 * 
 * @author mconner
 */
public class QueryResultsDynaBeanList extends AbstractList<DynaBean> {

    QueryResults queryResults;

    public QueryResultsDynaBeanList( QueryResults queryResults ) {
        this.queryResults = queryResults;
    }

    @Override
    public DynaBean get( int index ) {
        return queryResults.getAsDynaBean( index );
    }

    @Override
    public int size() {
        return queryResults.getCount();
    }

}
