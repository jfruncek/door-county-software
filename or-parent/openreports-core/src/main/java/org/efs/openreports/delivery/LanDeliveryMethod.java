package org.efs.openreports.delivery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ContentManager;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.DeliveredReport;
import org.efs.openreports.objects.ReportDeliveryLog;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.util.IoU;

/**
 * Delivers a report to one or more (typically one) files. 
 * Parent directories are created, if possible.
 * If a file with the given name already exists, it first moves
 * the older version to an archive folder, if possible.
 * 
 * @author mconner
 */
public class LanDeliveryMethod implements DeliveryMethod {
    protected static Logger log = Logger.getLogger( LanDeliveryMethod.class.getName() );
    
    private DeliverySupport deliverySupport;


    public List<ReportDeliveryLog> deliverReport( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput ) {
        logInterestingInfo( reportSchedule, reportInput );

        List<ReportDeliveryLog> deliveryLogEntries = new ArrayList<ReportDeliveryLog>();
        for( String fileName : parsePaths( reportSchedule.getOutputPaths(), reportSchedule, reportInput, reportOutput ) ) {
            deliveryLogEntries.add( copyToFile( fileName, reportSchedule, reportOutput.getContentManager() ) );
        }
        return deliveryLogEntries;
    }

    private void logInterestingInfo( ReportSchedule reportSchedule, ReportEngineInput reportInput ) {
        StringBuffer buf = new StringBuffer();
        buf.append("Report Parameters:");
        Map<String, Object> params = reportInput.getParameters();
        for(String paramName : params.keySet()) {
            buf.append("  ").append( paramName ).append( "=" ).append( params.get(paramName) );
            buf.append("\n");
        }
        
        log.info( buf );
    }

    public byte[] getDeliveredReport( DeliveredReport deliveredReport ) throws DeliveryException {
        throw new DeliveryException( "Method getDeliveredReport not implemented by LanDeliveryMethod" );
    }

    public DeliveredReport[] getDeliveredReports( ReportUser user ) throws DeliveryException {
        throw new DeliveryException( "Method getDeliveredReports not implemented by LanDeliveryMethod" );
    }

    protected ReportDeliveryLog copyToFile( String path, ReportSchedule reportSchedule, ContentManager contentManager ) {
        ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog(ReportConstants.DeliveryMethod.LAN.getName(), path );
        try {
            File pathFile = new File( path );
            File parentDir = IOUtil.ensureParentDirExists( path );

            File file = new File( path );
            if( file.exists() ) {
                File archive = new File( parentDir, "archive" );
                archive.mkdir();
                Date lastModifiedDate = new Date( pathFile.lastModified() );
                String suffix = new SimpleDateFormat( "-yyyyMMdd-HHmmss.SSS" ).format( lastModifiedDate );

                File archiveFile = new File( archive, IOUtil.appendToFileName( pathFile.getName(), suffix ) );
                if( pathFile.renameTo( archiveFile ) ) {
                    log.info( "File: " + path + " exists, moved to archive, " + archiveFile );
                } else {
                    log.error( "Failed saving report. File: " + path + " exists, but can't move to archive, "
                            + archiveFile );
                }
            }
            
            InputStream content = contentManager.createInputStream(); 
            try {
                if( IOUtil.copyToFile( content, path ) < 0 ) {
                    deliveryLogEntry.markFailure( "couldn't copy data to path" );
                    log.error( deliveryLogEntry.toString());
                } else {
                    deliveryLogEntry.markSuccess(  );
                    log.info( deliveryLogEntry.toString() );
                }
            } finally {
                IoU.I.safeClose( content, reportSchedule.getReport().getName() );
            }
        } catch( IOException ioe ) {
            deliveryLogEntry.markFailure( "IOException", ioe );
            log.error( deliveryLogEntry.toString(), ioe );
        }
        return deliveryLogEntry;
    }

    protected List<String> parsePaths( String value, ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput ) {
        DeliveryMethodGroovyContext groovyContext = new DeliveryMethodGroovyContext( reportInput.getParameters(),
                reportSchedule.getReport().getName(), reportSchedule.getScheduleDescription(), reportInput.getRunDateTime());
        StringTokenizer st = new StringTokenizer( value, "\n\r\f" );
        List<String> fileNames = new ArrayList<String>();

        while( st.hasMoreElements() ) {
            String fileName = st.nextToken().trim();
            if( StringUtils.isNotBlank( fileName ) ) {
                if (groovyContext.isScript( fileName )) {
                    try {
                        Object evalResult = groovyContext.eval( fileName );
                        if (evalResult instanceof String) {
                            fileNames.add(  getEffectivePath((String) evalResult) + reportOutput.getContentExtension() );
                        } else {
                            String className = (evalResult == null) ? "null" : evalResult.getClass().getName();
                            log.error( "result of groovy eval is not a String. Ignoring. Value was of type: "
                                    + className + " value: " + evalResult.toString() );
                        }
                    } catch(ScriptException se) {
                        log.error( "Error trying to evaluate output path, ignoring" + fileName, se );
                    }
                } else {
                    fileNames.add( getEffectivePath(fileName) + reportOutput.getContentExtension() );
                }
            }
        }
        return fileNames;
    }

    private String getEffectivePath( String path ) {
        String effectivePath = path;
        if( !deliverySupport.isProductionServer() ) {
            StringBuffer buf = new StringBuffer();
            buf.append( deliverySupport.getDevelopmentFileRoot() );
            if( buf.length() > 0 ) {
                buf.append( '/' );
            }
            buf.append( path );
            effectivePath = buf.toString();
        }
        return effectivePath;
    }

    public void setDeliverySupport(DeliverySupport deliverySupport) 
    {
        this.deliverySupport = deliverySupport;
    }   

}
