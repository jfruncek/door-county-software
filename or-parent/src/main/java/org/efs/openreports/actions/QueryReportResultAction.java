/*
 * Copyright (C) 2004 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.efs.openreports.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.SessionHelper;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.util.DisplayProperty;

import com.opensymphony.xwork2.ActionContext;

public class QueryReportResultAction extends DisplayTagAction {
    protected static Logger log = Logger.getLogger( QueryReportResultAction.class );

    /**
     * items to display on the page. Preferably, this would be defined on the jsp, but when using
     * external sorting and paging, the displaytag doesn't appear to pass along the pagesize.
     * Hardcoding it here for the time being.
     */
    private static final int PAGE_SIZE = 20;
    private static final long serialVersionUID = -6173123870225020481L;
    
    private boolean atMaxRows;
    private String dir;
    private int page = 1;
    private DisplayProperty[] properties;
    private Report report;
    private Map<String, Object> reportParameters;
    private DynaBeanPagenatedList results;
    private String sort;

    public String execute() {
        SessionHelper sessionHelper = new SessionHelper();
        ReportUser user = sessionHelper.get( ORStatics.REPORT_USER, ReportUser.class );
        report = sessionHelper.get( ORStatics.REPORT, Report.class );
        reportParameters = getReportParameterMap( user );
        QueryResults queryResults = sessionHelper.get( ORStatics.QUERY_REPORT_RESULTS, QueryResults.class );
        results = new DynaBeanPagenatedList( queryResults, page, PAGE_SIZE, sort, getSortOrder( dir ) );
        atMaxRows = queryResults.isAtMaxRows();
        properties =
                (DisplayProperty[]) ActionContext.getContext().getSession().get( ORStatics.QUERY_REPORT_PROPERTIES );

        if( report.getFile() != null && report.getFile().endsWith( ".ftl" ) ) {
            return ORStatics.QUERY_REPORT_TEMPLATE_ACTION;
        }

        return SUCCESS;
    }

    public DisplayProperty[] getProperties() {
        return properties;
    }

    public Report getReport() {
        return report;
    }

    public Map<String, Object> getReportParameters() {
        return reportParameters;
    }

    public DynaBeanPagenatedList getResults() {
        return results;
    }

    public boolean isAtMaxRows() {
        return atMaxRows;
    }

    public void setDir( String dir ) {
        this.dir = dir;
    }

    public void setPage( int page ) {
        this.page = page;
    }

    public void setSort( String sort ) {
        this.sort = sort;
    }

    protected Map<String, Object> getReportParameterMap( ReportUser user ) {
        SessionHelper sessionHelper = new SessionHelper();
        @SuppressWarnings( "unchecked" )
        Map<String, Object> uncheckedReportParameters = sessionHelper.get( ORStatics.REPORT_PARAMETERS, Map.class );
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        if( uncheckedReportParameters != null ) {
            reportParameters = uncheckedReportParameters;
        }

        // add standard report parameters
        reportParameters.put( ORStatics.USER_ID, user.getId() );
        reportParameters.put( ORStatics.EXTERNAL_ID, user.getExternalId() );
        reportParameters.put( ORStatics.USER_NAME, user.getName() );

        return reportParameters;
    }

    private SortOrderEnum getSortOrder( String orderString ) {
        if( "asc".equals( orderString ) ) {
            return SortOrderEnum.ASCENDING;
        } else if( "desc".equals( orderString ) ) {
            return SortOrderEnum.DESCENDING;
        }
        return null;
    }

}