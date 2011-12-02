package org.efs.openreports.engine;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.ReportConfigurationException;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.ProviderException;
import org.jfree.util.Log;

/**
 * Executes report post-processing. It is sometimes necessary to perform some additional processing
 * not easily done within the report engine flavor. For example, setting the report headings in
 * excel doesn't work properly in eSpreadsheet. We could use an eSpreadsheet callback, but they are
 * a pain to debug, and require restarting the server once the class is loaded. (Also, we are trying
 * to get away from the eSpreadsheet reports as much as possible). This class performs
 * post-processing by loading a class, optionally from a jar (and possibly supporting jars), and
 * processes the output from a report.
 * 
 * How it works: If the report has a line in the "query" field of the form defined by
 * Report.REPORT_POST_PROCESSOR_PATTERN, it will use the first element (separated by (;)) after the
 * equals as the classname. Following that are one or more paths to jars to be loaded (from the
 * reports directory). A separate classloader is used, so that changes can be made to the report
 * without need to shut down the report server.
 * 
 * @author mconner
 */
public class ReportPostProcessExecutor {

    protected static Logger log = Logger.getLogger( ReportPostProcessExecutor.class.getName() );
    DirectoryProvider directoryProvider;

    public ReportPostProcessExecutor( DirectoryProvider directoryProvider ) {
        this.directoryProvider = directoryProvider;
    }

    /**
     * If the report has a post-processing directive, it parses it and loads an instance of the
     * processor class, which must have a public method with the signature:
     * 
     * <pre>
     * &lt;code&gt;
     * public byte[] process(byte[]) 
     * &lt;/code&gt;
     * </pre>
     * 
     * The processor can do whatever it needs to with the content.
     * 
     * @param byteArray
     * @param reportEngineInput
     * @return
     */
    public byte[] postProcess( byte[] byteArray, ReportEngineInput reportEngineInput ) throws ProviderException {
        if( !reportEngineInput.getReport().hasReportPostProcessor() ) {
            return byteArray;
        }

        String postProcessorDef = reportEngineInput.getReport().getReportPostProcessorDef();
        if( postProcessorDef == null ) {
            return byteArray;
        }

        try {
            ReportPostProcessor postProcessor = loadPostProcessor( postProcessorDef );
            return postProcessor.process( byteArray, reportEngineInput.getParameters() );
        } catch( Exception ex ) {
            throw new ProviderException( "Can't run post processor for report: "
                    + reportEngineInput.getReport().getName(), ex );
        }
    }

    /**
     * 
     * @param values
     * @param i
     */
    private ClassLoader buildClassLoader( ClassLoader parent, String[] urlStringValues, int offset ) {
        List<URL> urlList = new ArrayList<URL>( urlStringValues.length - offset );
        for( int i = offset; i < urlStringValues.length; i++ ) {
            String urlString = urlStringValues[i].trim();
            if( urlString.length() == 0 )
                continue;

            urlString = directoryProvider.getReportDirectory() + urlString; // force jars to be in
            // reports area
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

    /**
     * @param postProcessorDef string of the form: classname; [classpathURL [...]]
     * @return
     */
    private ReportPostProcessor loadPostProcessor( String postProcessorDef ) throws Exception {
        String[] values = postProcessorDef.split( ";" );
        if( values == null ) {
            return null;
        }
        String className = values[0].trim();
        ClassLoader classLoader = getClass().getClassLoader();
        if( values.length > 1 ) {
            classLoader = buildClassLoader( classLoader, values, 1 );
        }

        try {
            Class<?> clazz = classLoader.loadClass( className );
            checkIsReportPostProcessor( clazz );
            Object object = clazz.newInstance();
            return (ReportPostProcessor) object;
        } catch( ClassNotFoundException cnfe ) {
            throw new ReportConfigurationException( "Can't load post processor class: " + className, cnfe );
        } catch( InstantiationException ie ) {
            throw new ReportConfigurationException( "Can't instantiate post processor class: " + className, ie );
        } catch( IllegalAccessException iae ) {
            throw new ReportConfigurationException( "Can't access post processor class: " + className, iae );
        }
    }

    private void checkIsReportPostProcessor( Class<?> clazz ) throws ReportConfigurationException {
        if( !ReportPostProcessor.class.isAssignableFrom( clazz ) ) {
            throw new ReportConfigurationException( "Configured post processor: " + clazz.getName() + " is not a "
                    + ReportPostProcessor.class.getName() );
        }
    }

}
