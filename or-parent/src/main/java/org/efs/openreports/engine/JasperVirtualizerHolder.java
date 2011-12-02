package org.efs.openreports.engine;

import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.providers.DirectoryProvider;

/**
 * Managers the Jasper virtualization setup and cleanup. While we might be able to encapsulate this
 * into the JasperReportEngine, it isn't clear at this time if the virtualizer needs to exist beyond
 * the JasperReportEngine (i.e.: to allow for chart engine to use it, or to fully export a report).
 * This just makes it a bit more convenient to setup/cleanup.
 * 
 * @author mconner
 */
public class JasperVirtualizerHolder {
    protected static final Logger log = Logger.getLogger( JasperVirtualizerHolder.class );

    Map<String, Object> reportParameters;
    JRVirtualizer virtualizer = null;

    public void cleanup() {
        if( virtualizer != null ) {
            if( reportParameters != null ) {
                reportParameters.remove( JRParameter.REPORT_VIRTUALIZER );
            }
            virtualizer.cleanup();
        }
    }

    public void setup( ReportEngineInput reportEngineInput, DirectoryProvider directoryProvider ) {
        if( reportEngineInput.getReport().isVirtualizationEnabled()
                && reportEngineInput.getExportType() != ExportType.IMAGE ) {
            log.debug( "Virtualization Enabled" );
            virtualizer = new JRFileVirtualizer( 2, directoryProvider.getTempDirectory() );
            reportEngineInput.getParameters().put( JRParameter.REPORT_VIRTUALIZER, virtualizer );
        }
    }

}
