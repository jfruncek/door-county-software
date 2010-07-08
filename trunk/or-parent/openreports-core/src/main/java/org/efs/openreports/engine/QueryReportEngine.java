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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.util.JRQueryExecuter;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.QueryEngineOutput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.engine.querycache.ResultSetDynaBeanIterator;
import org.efs.openreports.engine.sqlsupport.SqlU;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ExportProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.DisplayProperty;
import org.efs.openreports.util.ORUtil;
import org.efs.openreports.util.StarkApplicationContext;
import org.efs.openreports.util.TimeU;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

/**
 * QueryReport ReportEngine implementation.
 * 
 * @author Erik Swenson
 */
public class QueryReportEngine extends ReportEngine implements ApplicationContextAware {

    protected static Logger log = Logger.getLogger( QueryReportEngine.class );
    private StarkApplicationContext applicationContext;
    QueryResultsBuilder queryResultsBuilder;

    public QueryReportEngine() {
    }

    public List<ReportParameter> buildParameterList( Report report ) throws ProviderException {
        throw new ProviderException( "QueryReportEngine: buildParameterList not implemented." );
    }

    public QueryEngineOutput generateReport( ReportEngineInput input ) throws ProviderException {
        Connection conn = null;
        PreparedStatement pStmt = null;
        Report report = input.getReport();

        try {
            Map<String, Object> parameters = input.getParameters();
            ReportDataSource dataSource = report.getDataSource();

            conn = dataSourceProvider.getConnection( dataSource.getId() );
            pStmt = createStatement( conn, report.getQuery(), parameters );

            if( !report.isQueryReport() ) {
                return buildEngineOutput( pStmt, null, report.getName() );
            } else {
                String exportProviderBeanId = getExportProviderBeanId( input.getExportType() );
                if( exportProviderBeanId == null ) {
                    Integer maxRows = propertiesProvider.getIntValue( ORProperty.QUERYREPORT_MAXROWS, null );
                    if( maxRows != null ) {
                        pStmt.setMaxRows( maxRows + 1 ); // add 1 so we can tell if we went beyond
                        // the limit.
                    }
                    return buildEngineOutput( pStmt, maxRows, report.getName() );
                } else {
                    ResultSet resultSet = null;
                    try {
                        resultSet = pStmt.executeQuery();
                        DynaClass resultDescriptor = SqlU.I.buildDynaClassFromMetaData( resultSet );

                        QueryEngineOutput output =
                                buildEngineOutputForExport( resultSet, resultDescriptor, report.getName() );
                        String exportId = report.getName().replace( " ", "" ) + TimeU.I.formatNowForSortable();
                        ExportProvider exportProvider =
                                applicationContext.getSpringBean( exportProviderBeanId, ExportProvider.class );
                        Iterator<DynaBean> iter = new ResultSetDynaBeanIterator( resultDescriptor, resultSet );
                        exportProvider.export( iter, output.getProperties(), output.getContentManager(), exportId );
                        setExportContentType( output, input.getExportType() );
                        return output;

                    } finally {
                        SqlU.I.safeClose( resultSet, report.getName(), log );
                    }

                }
            }

        } catch( Exception e ) {
            String msg = "Error executing query: " + e.getMessage() + " Query: /" + report.getQuery() + "/";
            log.error( msg, e );
            throw new ProviderException( msg );
        } finally {
            SqlU.I.safeClose( pStmt, input.getReport().getName(), log );
            SqlU.I.safeClose( conn, input.getReport().getName(), log );
        }
    }

    @Override
    public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
        this.applicationContext = new StarkApplicationContext( applicationContext );
    }

    public void setQueryResultsBuilder( QueryResultsBuilder queryResultsBuilder ) {
        this.queryResultsBuilder = queryResultsBuilder;
    }

    String getExportProviderBeanId( ExportType exportType ) {
        if( exportType == null )
            return null;

        switch( exportType ) {
        case EXCEL:
        case XLS:
            return "excelExportProvider";
        case CSV:
            return "csvExportProvider";
        case XLSX:
            return "xlsxExportProvider";
        default:
            return null;
        }
    }

    private DisplayProperty[] buildDisplayProperties( DynaClass dynaClass, boolean sortable ) {
        DynaProperty[] dynaProperties = dynaClass.getDynaProperties();
        DisplayProperty[] properties = new DisplayProperty[dynaProperties.length];
        for( int i = 0; i < dynaProperties.length; i++ ) {
            properties[i] =
                    new DisplayProperty( dynaProperties[i].getName(), dynaProperties[i].getType().getName(), sortable );
        }
        return properties;
    }

    private DisplayProperty[] buildDisplayProperties( QueryResults queryResults ) throws ProviderException {
        boolean sortable = isSortable( queryResults );
        return buildDisplayProperties( queryResults.getResultsDescriptor(), sortable );
    }

    private QueryEngineOutput buildEngineOutput( PreparedStatement statement, Integer maxRows, String logId )
            throws SQLException, ProviderException {
        ResultSet resultSet = statement.executeQuery();
        try {
            QueryResults queryResults = queryResultsBuilder.build( resultSet, maxRows );
            QueryEngineOutput output = new QueryEngineOutput();
            output.setResults( queryResults );
            output.setProperties( buildDisplayProperties( queryResults ) );
            return output;
        } finally {
            SqlU.I.safeClose( resultSet, logId, log );
        }
    }

    private QueryEngineOutput buildEngineOutputForExport( ResultSet resultSet, DynaClass resultDescriptor, String logId )
            throws SQLException {
        QueryEngineOutput output = new QueryEngineOutput();
        output.setProperties( buildDisplayProperties( resultDescriptor, true ) );
        return output;
    }

    private PreparedStatement createStatement( Connection conn, String queryText, Map<String, Object> parameters )
            throws SQLException, JRException {
        if( CollectionUtils.isEmpty( parameters ) ) {
            return conn.prepareStatement( queryText );
        } else {
            return createStatementWithParameters( conn, queryText, parameters );
        }
    }

    /**
     * Use JasperReports Query logic to parse parameters in chart queries
     * 
     * @param conn
     * @param report
     * @param parameters
     * @return
     * @throws JRException
     */
    private PreparedStatement createStatementWithParameters( Connection conn, String queryText,
            Map<String, Object> parameters ) throws JRException {
        PreparedStatement pStmt;
        JRDesignQuery query = new JRDesignQuery();
        query.setText( queryText );

        @SuppressWarnings( "unchecked" )
        // convert parameters to JRDesignParameters so they can be parsed
        Map jrParameters = ORUtil.buildJRDesignParameters( parameters );

        pStmt = JRQueryExecuter.getStatement( query, jrParameters, parameters, conn );
        return pStmt;
    }

    private boolean isSortable( QueryResults queryResults ) throws ProviderException {
        int maxSortableRowCount =
                propertiesProvider.getIntValue( ORProperty.QUERYREPORT_MAXROWS_SORTABLE,
                        ORProperty.DEFAULT_QUERYREPORT_MAX_ROWS_SORTABLE );
        return queryResults.getCount() <= maxSortableRowCount;
    }

    private void setExportContentType( QueryEngineOutput output, ExportType exportType ) {
        switch( exportType ) {
        case EXCEL:
        case XLS:
            output.setContentType( ReportEngineOutput.CONTENT_TYPE_XLS );
            return;
        case CSV:
            output.setContentType( ReportEngineOutput.CONTENT_TYPE_CSV );
            return;
        case XLSX:
            output.setContentType( ReportEngineOutput.CONTENT_TYPE_XLSX );
            return;
        default:
            log.warn( "Not seeting content type for export type: " + exportType );
            return;
        }
    }

}
