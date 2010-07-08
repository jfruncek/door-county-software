/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.engine.ChartReportEngine;
import org.efs.openreports.engine.JasperVirtualizerHolder;
import org.efs.openreports.engine.ReportEngine;
import org.efs.openreports.engine.ReportEngineProvider;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ContentManager;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportChart;
import org.efs.openreports.objects.ReportLog;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportLogProvider;
import org.efs.openreports.providers.ReportLogSupport;
import org.efs.openreports.util.IoU;
import org.efs.openreports.util.ReleasableU;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class ReportRunAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 7473180642590984527L;

    protected static Logger log = Logger.getLogger( ReportRunAction.class );

    private Map<Object, Object> session;

    private ReportLogSupport reportLogSupport;
    private DirectoryProvider directoryProvider;
    private ParameterProvider parameterProvider;
    private ReportEngineProvider reportEngineProvider;

    int exportTypeCode = -1;

    public int getExportTypeCode() {
        return exportTypeCode;
    }

    public void setExportTypeCode( int exportTypeCode ) {
        this.exportTypeCode = exportTypeCode;
    }

    public String execute() {
        ReportUser user = (ReportUser) getSession().get( ORStatics.REPORT_USER );
        Report report = (Report) getSession().get( ORStatics.REPORT );
        ExportType exportType = ExportType.findByCode( initExportType() );

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        disableResponseCaching( response );

        ReportLog reportLog = new ReportLog( user, report, new Date() );
        reportLog.setExportType( exportType.getCode() );

        JasperVirtualizerHolder virtualizerHolder = new JasperVirtualizerHolder();
        ReportEngineOutput reportEngineOutput = null;
        try {
            if( handlePDFContype( exportType, request, response ) ) {
                return NONE;
            }
            reportLogSupport.insertReportLog( reportLog );
            ReportEngineInput reportEngineInput = buildReportInput( user, report, exportType );
            virtualizerHolder.setup( reportEngineInput, directoryProvider );
            addChart( reportEngineInput );
            ReportEngine reportEngine = reportEngineProvider.getUIReportEngine( report );
            reportEngineOutput = reportEngine.generateReport( reportEngineInput );
            response.setContentType( reportEngineOutput.getContentType() );

            if( exportType != ExportType.HTML && exportType != ExportType.IMAGE ) {
                response.setHeader( "Content-disposition", "inline; filename="
                        + StringUtils.deleteWhitespace( report.getName() ) + reportEngineOutput.getContentExtension() );
            }

            if( exportType == ExportType.IMAGE ) {
                if( report.isJasperReport() ) {
                    Object jasperPrint = reportEngineOutput.getSupplementaryContent().get( ORStatics.JASPERPRINT );
                    if (jasperPrint != null) {
                        session.put( ORStatics.JASPERPRINT, jasperPrint );
                    }
                }
            } else {
                ContentManager cm = reportEngineOutput.getContentManager();
                InputStream content = cm.createInputStream();
                OutputStream out = response.getOutputStream();
                response.setContentLength( cm.getSize() );
                IoU.I.copy( content,  out);
                IoU.I.safeClose( content, report.getName() );
                IoU.I.safeFlushAndClose( out, report.getName() + " response output stream");
            }

            reportLogSupport.logSuccess( reportLog );

            log.debug( "Finished report: " + report.getName() );
        } catch( Exception e ) {
            log.error( "Error running report: " + report.getName() + ", user: " + user.getName(), e );
            ActionHelper.addExceptionAsError( this, e );
            reportLogSupport.safeLogFailure( reportLog, e.getMessage(), log);
            return ERROR;
        } finally {
            virtualizerHolder.cleanup();
            ReleasableU.I.safeRelease(reportEngineOutput);
        }

        if( exportType == ExportType.IMAGE )
            return SUCCESS;

        return NONE;
    }

    /** set headers to disable caching */
    private void disableResponseCaching( HttpServletResponse response ) {
        response.setHeader( "Pragma", "public" );
        response.setHeader( "Cache-Control", "max-age=0" );
    }

    private ReportEngineInput buildReportInput( ReportUser user, Report report, ExportType exportType )
            throws ProviderException {
        Map<String, Object> reportParameters = getReportParameterMap( user, report, exportType );
        Map<?, ?> imagesMap = getImagesMap();
        ReportEngineInput reportEngineInput = new ReportEngineInput( report, reportParameters );
        reportEngineInput.setExportType( exportType );
        reportEngineInput.setImagesMap( imagesMap );
        reportEngineInput.setInlineImages( false );
        return reportEngineInput;
    }

    private int initExportType() {
        if( exportTypeCode == -1 ) {
            exportTypeCode = Integer.parseInt( (String) getSession().get( ORStatics.EXPORT_TYPE ) );
        }
        return exportTypeCode;
    }

    /** convenience */
    private Map<?, ?> getSession() {
        return ActionContext.getContext().getSession();
    }

    /**
     * Add chart, if any
     * 
     * @param reportInput
     * @throws ProviderException
     */
    private void addChart( ReportEngineInput reportInput ) throws ProviderException {
        Report report = reportInput.getReport();
        ReportChart reportChart = report.getReportChart();
        if( reportChart != null ) {
            log.debug( "Adding chart: " + reportChart.getName() );
            ChartReportEngine chartEngine = reportEngineProvider.getChartReportEngine( report );
            ReportEngineOutput chartOutput = (ReportEngineOutput) chartEngine.generateReport( reportInput );
            try {
                reportInput.getParameters().put( "ChartImage", chartOutput.getContentAsBytes() );
            } finally {
                chartOutput.release();
            }
        }
    }

    private boolean handlePDFContype( ExportType exportType, HttpServletRequest request, HttpServletResponse response )
            throws IOException {
        if( exportType == ExportType.PDF ) {
            // Handle "contype" request from Internet Explorer
            if( "contype".equals( request.getHeader( "User-Agent" ) ) ) {
                response.setContentType( "application/pdf" );
                response.setContentLength( 0 );

                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.close();

                return true;
            }
        }
        return false;
    }

    @SuppressWarnings( "unchecked" )
    protected Map<String, Object> getReportParameterMap( ReportUser user, Report report, ExportType exportType )
            throws ProviderException {
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        Map sessionReportParameters = (Map) getSession().get( ORStatics.REPORT_PARAMETERS );

        if( sessionReportParameters != null ) {
            List<ReportParameterMap> rpMap = report.getNonSubReportParameters();

            Map<String, Object> currentMap = parameterProvider.getReportParametersMap( rpMap, sessionReportParameters,
                    true );
            reportParameters.putAll( currentMap );

        }
        // add standard report parameters
        reportParameters.put(ORStatics.USER_ID, user.getId());
        reportParameters.put(ORStatics.EXTERNAL_ID, user.getExternalId());
        reportParameters.put(ORStatics.USER_NAME, user.getName());

        reportParameters.put( ORStatics.IMAGE_DIR, new File( directoryProvider.getReportImageDirectory() ) );
        reportParameters.put( ORStatics.REPORT_DIR, new File( directoryProvider.getReportDirectory() ) );
        reportParameters.put( ORStatics.EXPORT_TYPE_PARAM, new Integer( exportType.getCode() ) );

        return reportParameters;
    }

    protected Map<?, ?> getImagesMap() {
        // used by JasperReports HTML export
        // see ImageLoaderAction for more information
        Map<?, ?> imagesMap = null;
        if( getSession().get( ORStatics.IMAGES_MAP ) != null ) {
            imagesMap = (Map<?, ?>) getSession().get( ORStatics.IMAGES_MAP );
        } else {
            imagesMap = new HashMap<Object, Object>();
            session.put( ORStatics.IMAGES_MAP, imagesMap );
        }

        return imagesMap;
    }

    @SuppressWarnings( "unchecked" )
    public void setSession( Map session ) {
        this.session = session;
    }

    public void setReportLogProvider( ReportLogProvider reportLogProvider ) {
        this.reportLogSupport =  new ReportLogSupport(reportLogProvider);
    }

    public void setDirectoryProvider( DirectoryProvider directoryProvider ) {
        this.directoryProvider = directoryProvider;
    }

    public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

    public void setReportEngineProvider( ReportEngineProvider reportEngineProvider ) {
        this.reportEngineProvider = reportEngineProvider;
    }

}