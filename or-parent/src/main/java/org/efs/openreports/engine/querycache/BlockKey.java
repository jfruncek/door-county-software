package org.efs.openreports.engine.querycache;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A key for a QueryResultBlock.
 * 
 * @author mconner
 */
public class BlockKey implements Serializable {
    /** */
    private static final long serialVersionUID = 1L;

    /** the index of the result block */
    int blockIndex;

    /** identifies a specific query result unique to the lifetime of the cache */
    String queryKey;

    /**
     * @param queryKey the query to which this result belongs.
     * @param blockIndex the 0-based index of the block.
     */
    public BlockKey( String queryKey, int blockIndex ) {
        this.queryKey = queryKey;
        this.blockIndex = blockIndex;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true;
        } else if( obj == null ) {
            return false;
        } else if( getClass() != obj.getClass() ) {
            return false;
        }
        BlockKey that = (BlockKey) obj;
        if( this.blockIndex != that.blockIndex ) {
            return false;
        }
        return StringUtils.equals( this.queryKey, that.queryKey );
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public String getQueryKey() {
        return queryKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blockIndex;
        result = prime * result + ObjectUtils.hashCode( queryKey );
        return result;
    }

    public void set( String queryKey, int blockIndex ) {
        this.queryKey = queryKey;
        this.blockIndex = blockIndex;
    }

    public void setBlockIndex( int blockIndex ) {
        this.blockIndex = blockIndex;
    }

    public void setQueryKey( String queryKey ) {
        this.queryKey = queryKey;
    }

    public String toString() {
        return queryKey + ':' + blockIndex;
    }

}
