package org.efs.openreports.engine;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.Report;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.StarkApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory for ReportEngines.
 * 
 * @author mconner
 */
public class ReportEngineProvider implements ApplicationContextAware {
    protected static final Logger log = Logger.getLogger( ReportEngineProvider.class.getName() );
    private static final String CHART_REPORT_ENGINE = "chartReportEngine";
    private static final String JASPER_REPORT_ENGINE = "jasperReportEngine";

    StarkApplicationContext appContext;

    public ChartReportEngine getChartReportEngine( Report report ) throws ProviderException {
        return appContext.getSpringBean( CHART_REPORT_ENGINE, ChartReportEngine.class );
    }

    public JasperReportEngine getJasperReport( Report report ) {
        return appContext.getSpringBean( JASPER_REPORT_ENGINE, JasperReportEngine.class );
    }

    public ReportEngine getReportEngine( Report report ) throws ProviderException {
        if( report.isChartReport() ) {
            return getEngine( CHART_REPORT_ENGINE );
        } else if( report.isJasperReport() ) {
            return getEngine( JASPER_REPORT_ENGINE );
        } else if( report.isReportProcessorReport() ) {
            return getEngine( "reportProcessorReportEngine" );
        } else if( report.isQueryReport() ) {
            return getEngine( "queryReportEngine" );
        } else if( report.isVelocityReport() ) {
            return getEngine( "velocityReportEngine" );
        } 

        String message = report.getName() + " is invalid. Please verify report definition.";
        log.error( message );
        throw new ProviderException( message );
    }

    public ReportEngine getUIReportEngine( Report report ) throws ProviderException {
        // Weird stuff to use the JasperReportEngine to generate QueryReports. TODO: clean this up
        ReportEngine reportEngine = ( report.isQueryReport() ) ? getJasperReport( report ) : getReportEngine( report );
        return reportEngine;
    }

    @Override
    public void setApplicationContext( ApplicationContext appContext ) throws BeansException {
        this.appContext = new StarkApplicationContext( appContext );
    }

    private ReportEngine getEngine( String engineBeanName ) {
        return appContext.getSpringBean( engineBeanName, ReportEngine.class );
    }

}
