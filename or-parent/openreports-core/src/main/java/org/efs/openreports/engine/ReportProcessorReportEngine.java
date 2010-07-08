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

package org.efs.openreports.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.util.JRQueryExecuter;

import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.QueryEngineOutput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.engine.querycache.QueryResultsDynaBeanList;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.providers.ExcelExportProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.impl.POIExcelExportProvider;
import org.efs.openreports.util.DisplayProperty;
import org.efs.openreports.util.ORUtil;
import org.efs.openreports.util.StarkApplicationContext;
import org.efs.openreports.util.TimeU;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A Report Engine that provides a hook for processing the results. The bones of this engine were
 * lifted from QueryReportEngine, but since this will actually generate content, it will be useful
 * in a scheduled report job.
 */
public class ReportProcessorReportEngine extends ReportEngine implements ApplicationContextAware {
    protected static Logger log = Logger.getLogger( ReportProcessorReportEngine.class );
    private StarkApplicationContext appContext;
    private QueryResultsBuilder queryResultsBuilder;

    public ReportProcessorReportEngine() {
    }

    @SuppressWarnings( "unchecked" )
    public List buildParameterList( Report report ) throws ProviderException {
        throw new ProviderException( "QueryReportEngine: buildParameterList not implemented." );
    }

    public ReportEngineOutput generateReport( ReportEngineInput input ) throws ProviderException {
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;

        try {
            Report report = input.getReport();
            Map<String, Object> parameters = input.getParameters();

            ReportDataSource dataSource = report.getDataSource();
            conn = dataSourceProvider.getConnection( dataSource.getId() );

            ReportProcessor processor = getQueryReportProcessor( report.getQuery() );

            if( parameters == null || parameters.isEmpty() ) {
                pStmt = conn.prepareStatement( report.getQuery() );
            } else {
                // Use JasperReports Query logic to parse parameters in chart
                // queries

                JRDesignQuery query = new JRDesignQuery();
                query.setText( report.getQuery() );

                // convert parameters to JRDesignParameters so they can be
                // parsed
                Map<String, JRDesignParameter> jrParameters = ORUtil.buildJRDesignParameters( parameters );

                pStmt = JRQueryExecuter.getStatement( query, jrParameters, parameters, conn );
            }
            
            Integer maxRows = propertiesProvider.getIntValue( ORProperty.QUERYREPORT_MAXROWS, null );
            if( maxRows != null ) {
                pStmt.setMaxRows( maxRows + 1);
            }

            rs = pStmt.executeQuery();

            QueryResults queryResults = queryResultsBuilder.build(rs, maxRows );
            rs.close();

            processor.handleResults( queryResults, input );

            QueryEngineOutput output = generateOutput( queryResults, report );
            return output;
        } catch( Exception e ) {
            throw new ProviderException( "Error executing report query: " + e.getMessage(), e );
        } finally {
            try {
                if( pStmt != null )
                    pStmt.close();
                if( conn != null )
                    conn.close();
            } catch( Exception c ) {
                log.error( "Error closing" );
            }
        }
    }

    private DisplayProperty[] buildDisplayProperties( QueryResults queryResults ) {
        DynaProperty[] dynaProperties = queryResults.getResultsDescriptor().getDynaProperties();
        DisplayProperty[] properties = new DisplayProperty[dynaProperties.length];
        for( int i = 0; i < dynaProperties.length; i++ ) {
            properties[i] = new DisplayProperty( dynaProperties[i].getName(), dynaProperties[i].getType().getName(), true );
        }
        return properties;
    }

    private QueryEngineOutput generateOutput( QueryResults queryResults, Report report ) throws ProviderException {
        QueryEngineOutput output = new QueryEngineOutput();
        output.setResults( queryResults );
        DisplayProperty[] properties = buildDisplayProperties( queryResults );

        output.setProperties( properties );
        ExcelExportProvider exportProvider = new POIExcelExportProvider();
        String exportId = report.getName().replace( " ", "" ) + TimeU.I.formatNowForSortable();
        exportProvider.export( new QueryResultsDynaBeanList( queryResults ).iterator(), properties, output.getContentManager(), exportId );
        output.setContentType( ReportEngineOutput.CONTENT_TYPE_XLS );
        return output;
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.appContext = new StarkApplicationContext( applicationContext );
    }

    private ReportProcessor getQueryReportProcessor( String query ) throws ProviderException {
        Matcher m = Report.REPORT_PROCESSOR_PATTERN.matcher( query );
        if( m.find() ) {
            String springBeanId = m.group( 1 );
            return getReportProcessor( springBeanId );
        }
        return null;
    }

    private ReportProcessor getReportProcessor( String springBeanId ) {
        return appContext.getSpringBean( springBeanId, ReportProcessor.class );
    }

    public void setQueryResultsBuilder( QueryResultsBuilder queryResultsBuilder ) {
        this.queryResultsBuilder = queryResultsBuilder;
    }



}