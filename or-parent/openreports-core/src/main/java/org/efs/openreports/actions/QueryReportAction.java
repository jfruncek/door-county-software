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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.actions.admin.SessionHelper;
import org.efs.openreports.engine.QueryReportEngine;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.QueryEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportLog;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportLogProvider;
import org.efs.openreports.providers.ReportLogSupport;
import org.efs.openreports.util.ReleasableU;
import org.efs.openreports.util.StarkApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class QueryReportAction extends ActionSupport implements SessionAware, ApplicationContextAware {
    protected static final Logger log = Logger.getLogger( QueryReportAction.class );
    private static final long serialVersionUID = 5233748674680486245L;

    protected String html;
    protected ParameterProvider parameterProvider;
    protected Report report;
    protected ReportLogSupport reportLogSupport;

    protected Map<Object, Object> session;
    protected StarkApplicationContext starkAppContext;

    QueryReportAction() {
        log.info( "in QueryReportAction" );
    }

    public String execute() {
        // remove results of any previous query report from session
        SessionHelper sessionHelper = new SessionHelper();
        sessionHelper.remove( ORStatics.QUERY_REPORT_RESULTS );
        sessionHelper.remove( ORStatics.QUERY_REPORT_PROPERTIES );
        ReportUser user = sessionHelper.get( ORStatics.REPORT_USER, ReportUser.class );
        report = sessionHelper.get( ORStatics.REPORT, Report.class );
        ReportLog reportLog = new ReportLog( user, report, new Date() );

        try {
            log.debug( "Starting Query Report: " + report.getName() );
            log.debug( "Query: " + report.getQuery() );
            reportLogSupport.insertReportLog( reportLog );

            Map<String, Object> reportParameters = getReportParameterMap( user );
            ReportEngineInput input = new ReportEngineInput( report, reportParameters );

            QueryEngineOutput engineOutput = null;
            try {
                QueryReportEngine queryReportEngine =
                        starkAppContext.getSpringBean( "queryReportEngine", QueryReportEngine.class );
                QueryEngineOutput output = queryReportEngine.generateReport( input );
                session.put( ORStatics.QUERY_REPORT_RESULTS, output.getResults() );
                session.put( ORStatics.QUERY_REPORT_PROPERTIES, output.getProperties() );
            } finally {
                ReleasableU.I.safeRelease( engineOutput );
            }

            reportLogSupport.logSuccess( reportLog );
            log.debug( "Finished Query Report: " + report.getName() );
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            log.error( "Error with Query Report: " + report.getName() + " :" + e.getMessage() );
            reportLogSupport.safeLogFailure( reportLog, e.getMessage(), log );
            return ERROR;
        }

        return SUCCESS;
    }

    public String getHtml() {
        return html;
    }

    public Report getReport() {
        return report;
    }

    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        starkAppContext = new StarkApplicationContext( applicationContext );
    }

    public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

    public void setReportLogProvider( ReportLogProvider reportLogProvider ) {
        this.reportLogSupport = new ReportLogSupport( reportLogProvider );
    }

    @SuppressWarnings( "unchecked" )
    public void setSession( Map session ) {
        this.session = session;
    }

    @SuppressWarnings( "unchecked" )
    protected Map<String, Object> getReportParameterMap( ReportUser user ) throws ProviderException {
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        Map sessionReportParameters = (Map) ActionContext.getContext().getSession().get( ORStatics.REPORT_PARAMETERS );

        if( sessionReportParameters != null ) {
            List<ReportParameterMap> rpMap = report.getNonSubReportParameters();
            Map<String, Object> currentMap =
                    parameterProvider.getReportParametersMap( rpMap, sessionReportParameters, true );
            reportParameters.putAll( currentMap );
        }

        // add standard report parameters
        reportParameters.put( ORStatics.USER_ID, user.getId() );
        reportParameters.put( ORStatics.EXTERNAL_ID, user.getExternalId() );
        reportParameters.put( ORStatics.USER_NAME, user.getName() );

        return reportParameters;
    }

}