package org.efs.openreports.engine.querycache;

import org.apache.log4j.Logger;
import org.efs.openreports.util.TimeU;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Pass-through to the actual cache, for controlling access.
 * 
 * @author mconner
 * 
 */
public class QueryResultsCache {
    protected static Logger log = Logger.getLogger( QueryResultsCache.class );
    private Ehcache queryResultsEhcache;
    long lastAccessTime = TimeU.I.now();

    public void setEhcache( Ehcache ehcache ) {
        this.queryResultsEhcache = ehcache;
    }

    public QueryResultsBlock get( BlockKey blockKey ) {
        TimeU.Mark mark = TimeU.I.mark();
        Element element = queryResultsEhcache.get( blockKey );
        log.info( "loaded block: " + blockKey + " in " + mark.elapsedInSeconds() + " seconds " );
        if( element == null ) {
            log.warn( "no value for QueryResult: " + blockKey );
            return null;
        } else {
            return (QueryResultsBlock) element.getValue();
        }
    }

    public void put( BlockKey blockKey, QueryResultsBlock currentBlock ) {
        markAccess();
        queryResultsEhcache.put( new Element( blockKey, currentBlock ) );
    }

    public void setQueryResultsEhcache( Ehcache queryResultsEhcache ) {
        this.queryResultsEhcache = queryResultsEhcache;
    }

    protected void markAccess() {
        lastAccessTime = TimeU.I.now();
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void flush() {
        markAccess();
        if ( queryResultsEhcache.getMemoryStoreSize() > 0) {
            queryResultsEhcache.flush();
            log.info( "Flushed the query results cache memory store" );
        }
    }

}
