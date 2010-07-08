/*
 * Copyright (C) 2006 Erik Swenson - erik@oreports.com
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.engine.ChartReportEngine;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ChartEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportLog;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.objects.chart.ChartValue;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.ReleasableU;
import org.jfree.chart.imagemap.ImageMapUtilities;

import com.opensymphony.xwork2.ActionContext;

public class ChartReportAction extends QueryReportAction {
    protected static Logger log = Logger.getLogger( ChartReportAction.class );

    private static final long serialVersionUID = -6645146769113032498L;

    private ChartReportEngine chartReportEngine;
    private long chartRequestId;
    private ChartValue[] chartValues;
    private String imageMap;

    public String execute() {
        ReportUser user = (ReportUser) ActionContext.getContext().getSession().get( ORStatics.REPORT_USER );
        report = (Report) ActionContext.getContext().getSession().get( ORStatics.REPORT );
        ReportLog reportLog = new ReportLog( user, report, new Date() );

        ChartEngineOutput chartOutput = null;
        try {
            Map<String, Object> reportParameters = getReportParameterMap( user );
            log.debug( "Starting Chart Report: " + report.getName() );

            reportLogSupport.insertReportLog( reportLog );

            ReportEngineInput input = new ReportEngineInput( report, reportParameters );

            chartOutput = (ChartEngineOutput) chartReportEngine.generateReport( input );

            chartValues = chartOutput.getChartValues();
            if( chartValues.length == 0 ) {
                addActionError( getText( LocalStrings.ERROR_REPORT_EMPTY ) );
            }

            imageMap = ImageMapUtilities.getImageMap( "chart", chartOutput.getChartRenderingInfo() );

            HashMap<String, byte[]> imagesMap = new HashMap<String, byte[]>();
            imagesMap.put( "ChartImage", chartOutput.getContentAsBytes() );

            session.put( ORStatics.IMAGES_MAP, imagesMap );

            reportLogSupport.logSuccess( reportLog );

            /*
             * set chartRequestId to the current time so that it can be added to the imageLoader URL
             * to make a unique URL and prevent image caching
             */
            chartRequestId = reportLog.getEndTime().getTime();

            log.debug( "Finished Chart Report: " + report.getName() );
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            reportLogSupport.safeLogFailure( reportLog, getText( e.getMessage() ), log );
            return ERROR;
        } finally {
            ReleasableU.I.safeRelease( chartOutput );
        }

        return SUCCESS;
    }

    public long getChartRequestId() {
        return chartRequestId;
    }

    public ChartValue[] getChartValues() {
        return chartValues;
    }

    public String getImageMap() {
        return imageMap;
    }

    public void setChartReportEngine( ChartReportEngine chartReportEngine ) {
        this.chartReportEngine = chartReportEngine;
    }

    public void setChartRequestId( long chartRequestId ) {
        this.chartRequestId = chartRequestId;
    }

    public void setImageMap( String imageMap ) {
        this.imageMap = imageMap;
    }

}