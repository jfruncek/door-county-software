package org.efs.openreports.actions;

import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;
import org.efs.openreports.engine.querycache.QueryResults;

public class DynaBeanPagenatedList implements PaginatedList {
    private int objectsPerPage;
    private int pageNumber;
    private QueryResults queryResults;
    private String sortCriterion;
    private SortOrderEnum sortOrder;

    DynaBeanPagenatedList( QueryResults queryResults, int pageNumber, int objectsPerPage, String sortCriterion,
            SortOrderEnum sortOrder ) {
        this.queryResults = queryResults;
        this.pageNumber = pageNumber;
        this.objectsPerPage = objectsPerPage;
        this.sortCriterion = sortCriterion;
        this.sortOrder = sortOrder;
    }

    @Override
    public int getFullListSize() {
        return queryResults.getCount();
    }

    @Override
    public List<DynaBean> getList() {
        int startOffset = getStartOffset();
        int endOffsetNonInclusive = getEndOffsetNonInclusive( startOffset );
        boolean ascending = ( sortOrder == null || SortOrderEnum.ASCENDING.equals( sortOrder ) );
        return queryResults.getResultsAsDynaBeans( startOffset, endOffsetNonInclusive, sortCriterion, ascending );
    }

    @Override
    public int getObjectsPerPage() {
        return objectsPerPage;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public String getSearchId() {
        // not currently necessary
        return "42";
    }

    @Override
    public String getSortCriterion() {
        return sortCriterion;
    }

    @Override
    public SortOrderEnum getSortDirection() {
        return sortOrder;
    }

    private int getEndOffsetNonInclusive( int startOffset ) {
        if( getFullListSize() == 0 ) {
            return 0;
        }
        int endOffset = startOffset + getObjectsPerPage();
        return ( endOffset > getFullListSize() ) ? getFullListSize() : endOffset;
    }

    private int getStartOffset() {
        int zeroBasedPage = getPageNumber() - 1;
        int startOffset = zeroBasedPage * objectsPerPage;
        if( startOffset >= getFullListSize() ) {
            startOffset = getFullListSize() - getObjectsPerPage();
        }
        if( startOffset < 0 ) {
            startOffset = 0;
        }
        return startOffset;
    }

}
