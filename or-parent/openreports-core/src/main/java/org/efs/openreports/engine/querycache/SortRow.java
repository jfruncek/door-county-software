/**
 * 
 */
package org.efs.openreports.engine.querycache;

import java.io.Serializable;
import java.util.Comparator;

import org.efs.openreports.util.CompareU;

/**
 * Used to implement a sort of a some rows, by some value.
 * 
 * @author mconner
 */
class SortRow {

    Object sortValue;

    int unsortedIndex;

    /**
     * Depending on how the sorting is done, row may not be populated.
     */
    private Serializable[] row = null;

    public SortRow( Object sortValue, int unsortedIndex ) {
        this.unsortedIndex = unsortedIndex;
        this.sortValue = sortValue;
    }

    public SortRow( Object sortValue, int unsortedIndex, Serializable[] row ) {
        this.unsortedIndex = unsortedIndex;
        this.sortValue = sortValue;
        this.row = row;
    }

    public Serializable[] getRow() {
        return row;
    }

    public Object getSortValue() {
        return sortValue;
    }

    public int getUnsortedIndex() {
        return unsortedIndex;
    }

    /**
     * Relinquish the row. This is done so that we don't end up haning on to references for all the
     * rows at once.
     * 
     * @return
     */
    public Serializable[] giveUpRow() {
        Serializable[] temp = row;
        row = null;
        return temp;
    }

    public void setRow( Serializable[] row ) {
        this.row = row;
    }

    /**
     * Compare based on the value of a property of the DynaBean the SortHolder holds.
     * 
     * @author mconner
     * 
     */
    protected static class SortRowComparator implements Comparator<SortRow> {

        @Override
        public int compare( SortRow sh1, SortRow sh2 ) {
            if( sh1 == null ) {
                return ( sh2 == null ) ? 0 : -1;
            } else {
                return CompareU.I.compareNullLow( sh1.getSortValue(), sh2.getSortValue() );
            }
        }
    }

}