package org.efs.openreports.engine.querycache;

import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.efs.openreports.util.TimeU;

/**
 * Periodic task that examines the QueryResults cache and flushes the cache if its been inactive for
 * some time.<BR>
 * 
 * While Ehcache will flush to the disk if there's too many objects in memory, there's no direct way
 * to make it configure it to flush an element to disk that has sat idle for too long. This
 * effectively does that, but currently for the whole cache.
 * 
 * @author mconner
 * 
 */
public class QueryResultsFlushTask extends TimerTask {
    protected static Logger log = Logger.getLogger( QueryResultsFlushTask.class );
    private int idleTimeInSeconds = 60;
    private QueryResultsCache queryResultsCache;

    public void run() {
        if( TimeU.I.elapsedInSeconds( queryResultsCache.getLastAccessTime(), TimeU.I.now() ) > idleTimeInSeconds ) {
            queryResultsCache.flush();
        }
    }

    public void setidleTimeInSeconds( int idleTimeInSeconds ) {
        this.idleTimeInSeconds = idleTimeInSeconds;
    }

    public void setQueryResultsCache( QueryResultsCache queryResultsCache ) {
        this.queryResultsCache = queryResultsCache;
    }

}
