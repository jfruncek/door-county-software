package org.efs.openreports.engine.javareport;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.efs.openreports.engine.ReportPostProcessExecutor;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.impl.POIXlsxExportProvider;
import org.jfree.util.Log;

import com.starkinvestments.rpt.common.ConnectionSource;
import com.starkinvestments.rpt.common.ReportBuilder;
import com.starkinvestments.rpt.common.ReportBuilderException;
import com.starkinvestments.rpt.common.ReportParameterDef;

/**
 * Ties the engine to some state, so the engine can be stateless. (may not be strictly necessary).
 * 
 * @author mconner
 */
public class JavaReportEngineContext {
    protected static Logger log = Logger.getLogger( POIXlsxExportProvider.class );
    private Report report;
    private Properties properties;
    private JavaReportEngine engine;

    protected static Map<String, String> classNameToFieldType = initClassNameToFieldType();

    private static void addMapping( Map<String, String> map, Class<?> dataType, String paramFieldType ) {
        map.put( dataType.getName(), paramFieldType );
    }

    private static Map<String, String> initClassNameToFieldType() {
        Map<String, String> map = new HashMap<String, String>();

        addMapping( map, Boolean.class, ReportParameter.BOOLEAN_PARAM );
        addMapping( map, Double.class, ReportParameter.TEXT_PARAM );
        addMapping( map, Date.class, ReportParameter.DATE_PARAM );
        addMapping( map, Integer.class, ReportParameter.TEXT_PARAM );
        addMapping( map, String.class, ReportParameter.TEXT_PARAM );
        addMapping( map, Timestamp.class, ReportParameter.TEXT_PARAM );
        return map;
    }

    public JavaReportEngineContext( JavaReportEngine engine, Report report ) {
        this.report = report;
        this.engine = engine;
    }

    /**
     * For the time being, just return what was in the input. We may eventually need to do some
     * translation.
     * 
     * @param reportEngineInput
     * @return
     */
    private Map<String, Object> buildReportParameters( ReportEngineInput reportEngineInput ) {
        return reportEngineInput.getParameters();
    }

    private String getReportBuilderClassName( Report report ) throws ProviderException {
        String builderClassName = getProperty( JavaReportEngine.PROPERTY_REPORT_BUILDER_CLASS );
        return builderClassName;
    }

    private ReportBuilder getReportBuilder( Report report ) throws ProviderException {
        String builderClassName = getReportBuilderClassName( report );
        String builderClassPath = getProperty( JavaReportEngine.PROPERTY_REPORT_BUILDER_CLASSPATH );
        ReportBuilder builder = (ReportBuilder) loadInstance( builderClassName, builderClassPath, ReportBuilder.class );
        return builder;
    }

    private Object loadInstance( String className, String classPath, Class<?> expectedType ) throws ProviderException {
        ClassLoader classLoader = getClass().getClassLoader();
        if( classPath != null ) {
            String[] paths = classPath.split( ";" );
            if( paths.length > 0 ) {
                classLoader = buildClassLoader( classLoader, paths );
            }
        }

        try {
            Class<?> clazz = classLoader.loadClass( className );
            if( isExpectedType( expectedType, clazz ) ) {
                return clazz.newInstance();
            } else {
                throw new ProviderException( "class, " + clazz.getName() + ", is not an instance of "
                        + expectedType.getName() );
            }
        } catch( ClassNotFoundException cnfe ) {
            throw new ProviderException( "Can't load report builder class: " + className, cnfe );
        } catch( InstantiationException ie ) {
            throw new ProviderException( "Can't load report builder class: " + className, ie );
        } catch( IllegalAccessException iae ) {
            throw new ProviderException( "Can't load report builder class: " + className, iae );
        }
    }

    @SuppressWarnings( "unchecked" )
    private boolean isExpectedType( Class expectedType, Class<?> clazz ) {
        return expectedType.isAssignableFrom( clazz );
    }

    private ClassLoader buildClassLoader( ClassLoader parent, String[] urlStringValues ) {
        List<URL> urlList = new ArrayList<URL>( urlStringValues.length );
        for( int i = 0; i < urlStringValues.length; i++ ) {
            String urlString = urlStringValues[i].trim();
            if( urlString.length() == 0 )
                continue;

            // force jars to be in reports area:
            urlString = engine.getDirectoryProvider().getReportDirectory() + urlString;

            File file = new File( urlString );
            URL url;
            try {
                url = file.toURI().toURL();
                urlList.add( url );
            } catch( MalformedURLException mue ) {
                Log.error( "bad url: " + urlString, mue );
            }
        }
        URL[] urls = urlList.toArray( new URL[urlList.size()] );
        return new URLClassLoader( urls, parent );
    }

    public String getProperty( String name ) throws ProviderException {
        return getReportProperties().getProperty( name );
    }

    public String getReportBuilderClassPath() throws ProviderException {
        return getReportProperties().getProperty( JavaReportEngine.PROPERTY_REPORT_BUILDER_CLASSPATH );
    }

    private Properties getReportProperties() {
        if( properties == null ) {
            properties = new Properties();
            // TODO: Ugh! Hijacking the query for report properties. Should create a dedicated
            // properties field.
            String queryText = report.getQuery();
            if( queryText != null ) {
                try {
                    properties.load( new StringReader( queryText ) );
                } catch( IOException e ) {
                    log.error( "Error loading the report properties (from query field)" );
                }
            }
        }
        return properties;

    }

    public ReportEngineOutput generateReport( ReportEngineInput reportEngineInput ) throws ProviderException {
        ReportBuilder builder = getReportBuilder( report );
        Map<String, Object> reportParameters = buildReportParameters( reportEngineInput );
        ConnectionSource connectionSource = new ConnectionSourceImpl( engine.getDataSourceProvider() );
        MemReportSink memReportSink = new MemReportSink();
        try {
            builder.buildReport( reportParameters, connectionSource, memReportSink );
        } catch( ReportBuilderException rbe ) {
            String builderName = getReportBuilderClassName( reportEngineInput.getReport() );
            throw new ProviderException( "Error building report using builder: " + builderName, rbe );
        }
        ReportEngineOutput reportEngineOutput = new ReportEngineOutput();

        // a bit silly, but we'll allow a post-process here, too
        reportEngineOutput.setContentType( memReportSink.getContentType() );
        ReportPostProcessExecutor postProcessExecutor = new ReportPostProcessExecutor( engine.getDirectoryProvider());
        reportEngineOutput.setContent( postProcessExecutor.postProcess( memReportSink.getBytes(), reportEngineInput ) );
        
        return reportEngineOutput;
    }

    public List<ReportParameter> buildParameterList() throws ProviderException {
        ReportBuilder builder = getReportBuilder( report );
        List<ReportParameterDef> reportParameterDefsFromBuilder = builder.getReportParameterDefs();
        List<ReportParameter> reportParameter = new ArrayList<ReportParameter>();
        for( ReportParameterDef defFromBuilder : reportParameterDefsFromBuilder ) {
            ReportParameter rp = new ReportParameter();
            rp.setClassName( defFromBuilder.getDataType().getName() );
            rp.setDescription( defFromBuilder.getDescription() );
            rp.setName( defFromBuilder.getName() );
            rp.setType( getFieldTypeForType( defFromBuilder.getDataType() ) );
            rp.setRequired( defFromBuilder.isRequired() );
            rp.setDefaultValue( defFromBuilder.getDefaultValue() );
            reportParameter.add( rp );
        }
        return reportParameter;
    }

    private String getFieldTypeForType( Class<?> dataType ) {
        String type = classNameToFieldType.get( dataType.getName() );
        return type;
    }
}
