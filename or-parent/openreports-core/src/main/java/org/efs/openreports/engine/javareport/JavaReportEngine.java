package org.efs.openreports.engine.javareport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.efs.openreports.engine.ReportEngine;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.DataSourceProvider;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.ProviderException;

/**
 * A report engine that frees you from the tyranny of any particular reporting tool. Basically, it
 * loads, in a separate ClassLoader, an instance of some class that implements ReportBuilder, which
 * does whatever it needs to do to dump some data to an output stream.
 * 
 * @author mconner
 * 
 */
public class JavaReportEngine extends ReportEngine {
    protected static Logger LOG = Logger.getLogger( JavaReportEngine.class.getName() );

    public static final String PROPERTY_REPORT_BUILDER_CLASS = "javareport.class";
    public static final String PROPERTY_REPORT_BUILDER_CLASSPATH = "javareport.classpath";
    private static final PropertyDef PROPERTYDEF_REPORT_BUILDER =
            new PropertyDef( PROPERTY_REPORT_BUILDER_CLASS,
                    "the fully qualified class, must implement com.starkinvestments.rpt.common.ReportBuilder",
                    "com.starkinvestments.rpt.reportgroup.XXXReport" );


    public JavaReportEngine( ) {
    }

    // eventually, we could have reports define their required properties (e.g. for datasource
    // names).
    public Set<PropertyDef> buildPropertyDefs() {
        HashSet<PropertyDef> propertyDefs = new HashSet<PropertyDef>();
        propertyDefs.add( PROPERTYDEF_REPORT_BUILDER );
        return propertyDefs;
    }
    
    @Override
    public List<ReportParameter> buildParameterList( Report report ) throws ProviderException {
        JavaReportEngineContext context = new JavaReportEngineContext( this, report );
        List<ReportParameter> result = context.buildParameterList();
        if( result == null ) {
            result = new ArrayList<ReportParameter>();
        }
        return result;
    }

    @Override
    public ReportEngineOutput generateReport( ReportEngineInput oReportEngineInput ) throws ProviderException {
        JavaReportEngineContext context = new JavaReportEngineContext( this, oReportEngineInput.getReport() );
        return context.generateReport(oReportEngineInput);
    }

    public DirectoryProvider getDirectoryProvider() {
        return directoryProvider;
    }

    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

}
