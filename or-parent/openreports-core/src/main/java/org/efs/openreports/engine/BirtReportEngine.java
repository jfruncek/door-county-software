/*
 * Copyright (C) 2006 by Open Source Software Solutions, LLC and Contributors
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Original Author : Roberto Nibali - rnibali@pyx.ch Contributor(s) : Erik Swenson - erik@oreports.com
 */

package org.efs.openreports.engine;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.BirtProvider;
import org.efs.openreports.providers.ProviderException;

/**
 * 
 * @author Roberto Nibali
 * @author Erik Swenson
 * 
 * @author mconner (modified by, anyway). Original did some odd things that weren't used in the servlet context.
 * 
 */
public class BirtReportEngine extends ReportEngine {
    protected static Logger log = Logger.getLogger( BirtReportEngine.class );
    BirtProvider birtProvider;

    public BirtReportEngine( ) {
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public List<ReportParameter> buildParameterList( Report report ) throws ProviderException {
        IReportEngine engine = birtProvider.getBirtEngine();

        String designFile = directoryProvider.getReportDirectory() + report.getFile();
        IReportRunnable design = null;

        try {
            design = engine.openReportDesign( designFile );
        } catch( Throwable e ) {
            log.error( "getParameterNames Exception", e );
            throw new ProviderException( e.toString() );
        }

        IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask( design );

        try {

            Collection<IParameterDefnBase> params = getParameterDefns( task );

            ArrayList<ReportParameter> parameters = new ArrayList<ReportParameter>();

            Iterator<IParameterDefnBase> iter = params.iterator();
            // Iterate over all parameters
            while( iter.hasNext() ) {
                IParameterDefnBase param = iter.next();
                // Group section found
                if( param instanceof IParameterGroupDefn ) {
                    // Get Group Name
                    IParameterGroupDefn group = (IParameterGroupDefn) param;

                    // Get the parameters within a group
                    Iterator<IScalarParameterDefn> i2 = group.getContents().iterator();
                    while( i2.hasNext() ) {
                        IScalarParameterDefn birtScalarParamDef = i2.next();
                        ReportParameter rp = buildParameter( birtScalarParamDef );
                        parameters.add( rp );
                    }
                } else {
                    // Parameters are not in a group
                    IScalarParameterDefn birtScalarParamDef = (IScalarParameterDefn) param;
                    ReportParameter rp = buildParameter( birtScalarParamDef );
                    parameters.add( rp );
                }
            }
            return parameters;

        } finally {
            task.close();
        }

    }

    /**
     * Generates a report from a BIRT report design.
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public ReportEngineOutput generateReport( ReportEngineInput input ) throws ProviderException {
        Report report = input.getReport();

        ReportEngineOutput output = new ReportEngineOutput();

        IReportEngine engine = birtProvider.getBirtEngine();

        // Set options for task
        HTMLRenderOption renderOption = new HTMLRenderOption();
        renderOption.setOutputStream( output.getContentManager() );
        renderOption.setImageDirectory( directoryProvider.getTempDirectory() );
        renderOption.setBaseImageURL( "report-images" );

        try {
            String designFile = directoryProvider.getReportDirectory() + report.getFile();

            log.info( "Loading BIRT report design: " + report.getFile() );

            IReportRunnable design = engine.openReportDesign( designFile );

            handleDataSourceOverrides( design );

            if( input.getExportType() == ExportType.PDF ) {
                output.setContentType( ReportEngineOutput.CONTENT_TYPE_PDF );
                renderOption.setOutputFormat( IRenderOption.OUTPUT_FORMAT_PDF );
            } else if( input.getExportType() == ExportType.HTML || input.getExportType() == ExportType.HTML_EMBEDDED ) {
                output.setContentType( ReportEngineOutput.CONTENT_TYPE_HTML );
                renderOption.setOutputFormat( IRenderOption.OUTPUT_FORMAT_HTML );

                if( input.getExportType() == ExportType.HTML_EMBEDDED ) {
                    renderOption.setEmbeddable( true );
                }
            } else if( input.getExportType() == ExportType.XLS ) {
                output.setContentType( ReportEngineOutput.CONTENT_TYPE_XLS );
                renderOption.setOutputFormat( "xls" );
            } else {
                log.error( "Export type not yet implemented: " + input.getExportType() );
            }

            Map<String, Object> parameters = adjustParameters( input.getParameters(), design, engine );

            IRunAndRenderTask task = engine.createRunAndRenderTask( design );
            task.setRenderOption( renderOption );
            task.setParameterValues( parameters );
            task.validateParameters();

            if( input.getLocale() != null ) {
                task.setLocale( input.getLocale() );
            }

            if( input.getXmlInput() != null ) {
                ByteArrayInputStream stream = new ByteArrayInputStream( input.getXmlInput().getBytes() );
                task.getAppContext().put( "org.eclipse.datatools.enablement.oda.xml.inputStream", stream );
            }

            log.info( "Generating BIRT report: " + report.getName() );

            task.run();
            task.close();

            log.info( "Finished Generating BIRT report: " + report.getName() );

        } catch( Throwable e ) {
            log.error( "generateReport Exception", e );
            throw new ProviderException( e.toString() );
        }

        return output;
    }

    public void setBirtProvider( BirtProvider birtProvider ) {
        this.birtProvider = birtProvider;
    }

    private void adjustParameter( Map<String, Object> parameters, IScalarParameterDefn scalar ) {
        String key = scalar.getName();
        Object value = parameters.get( key );
        if( value == null )
            return;

        switch( scalar.getDataType() ) {
        case IScalarParameterDefn.TYPE_DATE:
            parameters.put( key, toJavaSQLDate( value ) );
            break;
        case IScalarParameterDefn.TYPE_FLOAT:
            parameters.put( key, toFloat( value ) );
            break;
        case IScalarParameterDefn.TYPE_TIME:
            parameters.put( key, toJavaSQLTime( value ) );
            break;
        }
    }

    /**
     * 
     * @param parameters
     * @return
     */
    private Map<String, Object> adjustParameters( Map<String, Object> parameters, IReportRunnable design,
            IReportEngine engine ) {

        // make a copy
        Map<String, Object> result = new HashMap<String, Object>( parameters );

        IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask( design );

        try {
            Collection<IParameterDefnBase> params = getParameterDefns( task );
            for( IParameterDefnBase param : params ) {
                if( param instanceof IParameterGroupDefn ) {
                    Collection<IScalarParameterDefn> scalars = getParameterGroupContents( param );
                    for( IScalarParameterDefn scalar : scalars ) {
                        adjustParameter( result, scalar );
                    }
                } else {
                    adjustParameter( result, (IScalarParameterDefn) param );
                }
            }

        } finally {
            task.close();
        }
        return result;
    }

    private ReportParameter buildParameter( IScalarParameterDefn birtScalarParamDef ) {
        ReportParameter rp = new ReportParameter();
        rp.setClassName( getParamClassName( birtScalarParamDef ) );
        rp.setDescription( birtScalarParamDef.getPromptText() );
        rp.setName( birtScalarParamDef.getName() );
        rp.setType( getParamType( birtScalarParamDef ) );
        rp.setRequired(birtScalarParamDef.isRequired());
        return rp;
    }

    /**
     * Get the OpenReports class name of the data type.
     * 
     * @param scalar
     * @return
     */
    private String getParamClassName( IScalarParameterDefn birtScalar ) {
        switch( birtScalar.getDataType() ) {
        case IScalarParameterDefn.TYPE_ANY:
            return ReportParameter.STRING;
        case IScalarParameterDefn.TYPE_STRING:
            return ReportParameter.STRING;
        case IScalarParameterDefn.TYPE_FLOAT:
            return ReportParameter.DOUBLE;
        case IScalarParameterDefn.TYPE_DECIMAL:
            return ReportParameter.DOUBLE;
        case IScalarParameterDefn.TYPE_DATE_TIME:
            return ReportParameter.TIMESTAMP;
        case IScalarParameterDefn.TYPE_BOOLEAN:
            return ReportParameter.BOOLEAN;
        case IScalarParameterDefn.TYPE_INTEGER:
            return ReportParameter.INTEGER;
        case IScalarParameterDefn.TYPE_DATE:
            // Keep param types as generic as possible, though birt actually uses java.sql.Date.
            return ReportParameter.DATE;
        case IScalarParameterDefn.TYPE_TIME:
            return ReportParameter.TIMESTAMP;
        }
        log.warn( "unexpected BIRT parameter data type: " + birtScalar.getDataType() + ". Using "
                + ReportParameter.STRING );
        return ReportParameter.STRING;
    }
    

    @SuppressWarnings("unchecked")
    private Collection<IParameterDefnBase> getParameterDefns( IGetParameterDefinitionTask task ) {
        return task.getParameterDefns( true );
    }
    
    @SuppressWarnings("unchecked")
    private Collection<IScalarParameterDefn> getParameterGroupContents( IParameterDefnBase param ) {
        return ( (IParameterGroupDefn) param ).getContents();
    }

    

    /**
     * 
     * @param birtScalarParamDef
     * @return one {@link ReportParameter} XXX_PARAM values.
     */
    private String getParamType( IScalarParameterDefn birtScalarParamDef ) {
        switch( birtScalarParamDef.getDataType() ) {
        case IScalarParameterDefn.TYPE_ANY:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_STRING:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_FLOAT:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_DECIMAL:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_DATE_TIME:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_BOOLEAN:
            return ReportParameter.BOOLEAN_PARAM;
        case IScalarParameterDefn.TYPE_INTEGER:
            return ReportParameter.TEXT_PARAM;
        case IScalarParameterDefn.TYPE_DATE:
            // Param type should be Date, Birt should convert to java.sql.date
            return ReportParameter.DATE_PARAM;
        case IScalarParameterDefn.TYPE_TIME:
            return ReportParameter.TEXT_PARAM;
        }
        log.warn( "unexpected BIRT parameter data type: " + birtScalarParamDef.getDataType() + ". Using "
                + ReportParameter.TEXT_PARAM );
        return ReportParameter.TEXT_PARAM;
    }

    /*
     * Overrides BIRT DataSources with OpenReports DataSources. In order for DataSources to be overriden, the name of
     * the DataSource in the BIRT rptdesign file must match the name of an existing OpenReports DataSource.
     */
    private void handleDataSourceOverrides( IReportRunnable design ) {
        ReportDesignHandle reportDH = (ReportDesignHandle) design.getDesignHandle();

        List<?> birtDataSources = reportDH.getAllDataSources();

        if( birtDataSources == null )
            return;

        Iterator<?> iterator = birtDataSources.iterator();
        while( iterator.hasNext() ) {
            Object dataSource = iterator.next();
            if( dataSource instanceof OdaDataSourceHandle ) {
                OdaDataSourceHandle dataSH = (OdaDataSourceHandle) dataSource;

                try {
                    ReportDataSource reportDataSource = dataSourceProvider.getDataSource( dataSH.getName() );

                    if( reportDataSource != null ) {
                        log.info( "Overriding BIRT DataSource: " + dataSH.getName() );

                        log.debug( "Original connection properties for: " + dataSH.getName() );
                        log.debug( "URL:    " + dataSH.getStringProperty( "odaURL" ) );
                        log.debug( "DRIVER:    " + dataSH.getStringProperty( "odaDriverClass" ) );
                        log.debug( "USER:   " + dataSH.getStringProperty( "odaUser" ) );
                        log.debug( "JNDI:   " + dataSH.getStringProperty( "odaJndiName" ) );

                        try {
                            if( reportDataSource.isJndi() ) {
                                dataSH.setStringProperty( "odaJndiName", reportDataSource.getUrl() );

                                dataSH.setStringProperty( "odaURL", "" );
                                dataSH.setStringProperty( "odaDriverClass", "" );
                                dataSH.setStringProperty( "odaUser", "" );
                                dataSH.setStringProperty( "odaPassword", "" );
                            } else {
                                dataSH.setStringProperty( "odaURL", reportDataSource.getUrl() );
                                dataSH.setStringProperty( "odaDriverClass", reportDataSource.getDriverClassName() );
                                dataSH.setStringProperty( "odaUser", reportDataSource.getUsername() );
                                dataSH.setStringProperty( "odaPassword", reportDataSource.getPassword() );

                                dataSH.setStringProperty( "odaJndiName", "" );
                            }

                        } catch( SemanticException e ) {
                            log.error( "SemanticException", e );
                        }

                        log.debug( "New connection properties for: " + dataSH.getName() );
                        log.debug( "URL:    " + dataSH.getStringProperty( "odaURL" ) );
                        log.debug( "DRIVER:    " + dataSH.getStringProperty( "odaDriverClass" ) );
                        log.debug( "USER:   " + dataSH.getStringProperty( "odaUser" ) );
                        log.debug( "JNDI:   " + dataSH.getStringProperty( "odaJndiName" ) );
                    } else {
                        log.info( "Unknown data source: " + dataSH.getName() );
                    }
                } catch( ProviderException pe ) {
                    log.error( "ProviderException", pe );
                }
            }
        }

        design.setDesignHandle( reportDH );
    }
    
    private Object toFloat( Object value ) {
        if( value instanceof Number ) {
            return ((Number)value).floatValue();
        } else {
            warnNoConvert(value, Float.class);
            log.warn( "Unexpected type to convert to java.lang.Float. Not converting. Class: "
                    + value.getClass().getName() + "value: " + value );
            return value;
        }
    }

    private Object toJavaSQLDate( Object value ) {
        if( value instanceof java.sql.Date ) {
            return value;
        } else if( value instanceof java.util.Date ) {
            java.util.Date utilDate = (java.util.Date) value;
            return new java.sql.Date( utilDate.getTime() );
        } else {
            warnNoConvert(value, java.sql.Date.class);
            return value;
        }
    }

    private Object toJavaSQLTime( Object value ) {
        if( value instanceof java.sql.Timestamp ) {
            return new java.sql.Time( ((java.sql.Timestamp) value).getTime() );
        } else {
            warnNoConvert(value, java.sql.Date.class);
            return value;
        }
    }

    private void warnNoConvert( Object value, Class<?> expectedClass ) {
        log.warn( "Unexpected type to convert to " + expectedClass.getName() + ". Not converting. Class: "
                + value.getClass().getName() + "value: " + value );
    }
}
