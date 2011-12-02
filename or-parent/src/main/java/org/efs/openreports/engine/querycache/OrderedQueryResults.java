package org.efs.openreports.engine.querycache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.util.TimeU;

/**
 * Represents the results of a query, in some order. That order may be the original query order, or
 * some sort order. The Results are backed by a cache of QueryResultBlocks. The items are stored in
 * an queryResultsCache, and QueryResults knows how to get them, given a reference to the cache.
 * 
 * @author mconner
 * 
 */
public class OrderedQueryResults {
    protected static Logger log = Logger.getLogger( OrderedQueryResults.class );
    private static final int BLOCK_SIZE = 8192;

    protected String queryKey;
    protected DynaClass resultsDescriptor;

    private int count = 0;
    private QueryResultsBlock currentBlock;
    private int currentBlockIndex = -1;
    private boolean currentBlockIsDirty;

    public OrderedQueryResults( DynaClass resultsDescriptor ) {
        this.queryKey = KeyBuilder.makeQueryKey();
        this.resultsDescriptor = resultsDescriptor;
    }

    /**
     * @param queryResultsCache
     * @param bean
     */
    public void add( QueryResultsCache queryResultsCache, Serializable[] row ) {
        QueryResultsBlock block = getBlock( queryResultsCache, count );
        block.add( row );
        currentBlockIsDirty = true;
        count++;
    }

    /**
     * force a flush of the current block (probably unnecessary).
     * 
     * @param queryResultsCache
     */
    public void flushCurrentBlock( QueryResultsCache queryResultsCache ) {
        if( currentBlockIsDirty ) {
            saveCurrentBlock( queryResultsCache );
        }
    }

    public Serializable[] get( QueryResultsCache queryResultsCache, int resultIndex ) {
        QueryResultsBlock block = getBlock( queryResultsCache, resultIndex );
        return block.getRow( resultIndex % BLOCK_SIZE );
    }

    public DynaBean getAsDynaBean( QueryResultsCache queryResultsCache, int resultIndex ) {
        Serializable[] row = get( queryResultsCache, resultIndex );
        DynaBean bean = convertToDynaBean( row );
        return bean;
    }

    /**
     * Loose any reference to the current block (memory savings)
     * 
     * @param queryResultsCache
     */
    public void clearCurrent( QueryResultsCache queryResultsCache ) {
        flushCurrentBlock( queryResultsCache );
        currentBlock = null;
        currentBlockIndex = -1;

    }

    public int getCount() {
        return count;
    }

    public int getPropertyIndex( String sortPropertyName ) {
        DynaProperty[] properties = resultsDescriptor.getDynaProperties();
        for( int i = 0; i < properties.length; i++ ) {
            if( properties[i].getName().equals( sortPropertyName ) ) {
                return i;
            }
        }
        return -1;
    }

    public String getQueryKey() {
        return queryKey;
    }

    public List<Serializable[]> getResults( QueryResultsCache queryResultsCache, int startIndex,
            int endIndexNonInclusive, boolean ascending ) {
        List<Serializable[]> results = new ArrayList<Serializable[]>( count );
        if( startIndex >= endIndexNonInclusive ) {
            return results;
        }

        int startOffset = getOffset( startIndex, count, ascending );
        int endOffsetNonInclusive = getOffset( endIndexNonInclusive, count, ascending );
        for( int i = startOffset; i != endOffsetNonInclusive; i += ( ascending ) ? 1 : -1 ) {
            CollectionUtils.addIgnoreNull( results, get( queryResultsCache, i ) );
        }
        return results;
    }

    public List<DynaBean> getResultsAsDynaBeans( QueryResultsCache queryResultsCache, int startIndex,
            int endIndexNonInclusive, boolean ascending ) {
        List<Serializable[]> results = getResults( queryResultsCache, startIndex, endIndexNonInclusive, ascending );
        List<DynaBean> beans = new ArrayList<DynaBean>();
        for( Serializable[] row : results ) {
            beans.add( convertToDynaBean( row ) );
        }
        return beans;
    }

    public DynaClass getResultsDescriptor() {
        return resultsDescriptor;
    }

    public void updateWithDynaBean( QueryResultsCache queryResultsCache, DynaBean bean, int resultIndex ) {
        Serializable[] row = makeRowFromDynaBean( bean, resultIndex );
        QueryResultsBlock block = getBlock( queryResultsCache, resultIndex );
        block.setRow( resultIndex % BLOCK_SIZE, row );
        currentBlockIsDirty = true;
    }

    /**
     * get the row values for any rows in the same block so we aren't bouncing getting blocks quite
     * so much.
     * 
     * @param sortRows
     * @param index
     */
    protected void preFetch( QueryResultsCache queryResultsCache, List<SortRow> sortRows, int startRowIndex ) {
        SortRow startSortRow = sortRows.get( startRowIndex );
        if( startSortRow.getRow() != null ) {
            return; // already done for this block.
        }
        int startSortRowBlockIndex = blockIndexFromResultIndex( startSortRow.getUnsortedIndex() );
        for( int i = startRowIndex, endNonInclusive = endOfBlockNonInclusive( startRowIndex ); i < endNonInclusive; i++ ) {
            SortRow sortRow = sortRows.get( i );
            if( blockIndexFromResultIndex( sortRow.getUnsortedIndex() ) == startSortRowBlockIndex ) {
                Serializable[] row = get( queryResultsCache, sortRow.getUnsortedIndex() );
                sortRow.setRow( row );
            }
        }

    }

    private void addNewBlock() {
        currentBlock = new QueryResultsBlock();
        currentBlockIndex = blockIndexFromResultIndex( count );
        currentBlockIsDirty = false;
    }

    private int blockIndexFromResultIndex( int resultIndex ) {
        return resultIndex / BLOCK_SIZE;
    }

    private DynaBean convertToDynaBean( Serializable[] row ) {
        DynaBean bean = new BasicDynaBean( resultsDescriptor );
        DynaProperty[] properties = resultsDescriptor.getDynaProperties();
        for( int i = 0; i < properties.length; i++ ) {
            bean.set( properties[i].getName(), row[i] );
        }
        return bean;
    }

    private int endOfBlockNonInclusive( int index ) {
        int startOfNextBlock = ( index / BLOCK_SIZE + 1 ) * BLOCK_SIZE;
        return Math.min( startOfNextBlock, count );
    }

    private QueryResultsBlock getBlock( QueryResultsCache queryResultsCache, int resultIndex ) {
        int requestedBlockIndex = blockIndexFromResultIndex( resultIndex );

        if( currentBlockIndex == requestedBlockIndex ) {
            return currentBlock;
        }
        flushCurrentBlock( queryResultsCache );

        if( resultIndex == count ) {
            addNewBlock();
        } else {
            loadBlock( queryResultsCache, requestedBlockIndex );
        }
        return currentBlock;
    }

    private int getColumnCount() {
        return resultsDescriptor.getDynaProperties().length;
    }

    private void loadBlock( QueryResultsCache queryResultsCache, int blockIndex ) {
        BlockKey blockKey = new BlockKey( getQueryKey(), blockIndex );
        currentBlock = queryResultsCache.get( blockKey );

        currentBlockIndex = blockIndex;
        currentBlockIsDirty = false;
    }

    private Serializable[] makeRowFromDynaBean( DynaBean bean, int index ) {
        Serializable[] row = new Serializable[getColumnCount()];
        DynaProperty[] properties = resultsDescriptor.getDynaProperties();
        for( int i = 0; i < properties.length; i++ ) {
            Object object = bean.get( properties[i].getName() );
            if( object instanceof Serializable || object == null ) {
                row[i] = (Serializable) object;
            } else {
                log.warn( "Object not serializable updating " + getQueryKey() + ":" + index );
                log.warn( "    " + ( ( object == null ) ? "null" : object.getClass().getName() ) );
            }
        }
        return row;
    }

    private void saveCurrentBlock( QueryResultsCache queryResultsCache ) {
        if( currentBlock != null ) {
            BlockKey blockKey = new BlockKey( getQueryKey(), currentBlockIndex );
            TimeU.Mark mark = TimeU.I.mark();
            queryResultsCache.put( blockKey, currentBlock );
            log.info( "saved block: " + blockKey + " in " + mark.elapsedInSeconds() + " seconds" );
        }
        currentBlockIsDirty = false;
    }

    /**
     * 
     * @param startIndex
     * @param listSize
     * @param ascending
     * @return the start index, adjusted for the sort order. No attempt is made to put the value
     *         back into range of the list size.
     */
    protected static int getOffset( int index, int listSize, boolean ascending ) {
        return ( ascending ) ? index : ( listSize - 1 - index );
    }

    protected static class KeyBuilder {
        private static int queryKeySequence = 0;

        public static synchronized String makeQueryKey() {
            return TimeU.I.formatNowForSortable() + "-" + queryKeySequence++;
        }
    }

}
