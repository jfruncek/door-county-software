/*
 * Copyright (C) 2007 Erik Swenson - erik@oreports.com
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

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.QueryEngineOutput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.querycache.QueryResultsDynaBeanList;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ReleasableU;

public class VelocityReportEngine extends ReportEngine {
    protected static Logger log = Logger.getLogger( VelocityReportEngine.class );
    QueryReportEngine queryReportEngine;

    public VelocityReportEngine( ) {
    }

    public List<ReportParameter> buildParameterList( Report report ) throws ProviderException {
        throw new ProviderException( "VelocityReportEngine: buildParameterList not implemented." );
    }

    public ReportEngineOutput generateReport( ReportEngineInput input ) throws ProviderException {
        Report report = input.getReport();
        Map<String, Object> parameters = input.getParameters();

        QueryEngineOutput queryEngineOutput = null;
        try {
            Properties properties = new Properties();
            properties.setProperty( "file.resource.loader.path", directoryProvider.getReportDirectory() );
            properties.setProperty( "runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem" );

            Velocity.init( properties );

            VelocityContext context = new VelocityContext();
            context.put( "parameters", parameters );
            context.put( "report", report );

            /*
             * if report has a query, process it like a QueryReport and put the results in the
             * VelocityContext for use in templates
             */
            if( report.hasQueryText() ) {
                queryEngineOutput = (QueryEngineOutput) queryReportEngine.generateReport( input );
                context.put( "results", new QueryResultsDynaBeanList( queryEngineOutput.getResults() ) );
                context.put( "properties", queryEngineOutput.getProperties() );
            }

            ReportEngineOutput output = new ReportEngineOutput();
            OutputStreamWriter writer = new OutputStreamWriter(output.getContentManager());

            Template template = Velocity.getTemplate( report.getFile() );
            template.merge( context,  writer);
            writer.flush();

            if( input.getExportType() == ExportType.HTML ) {
                output.setContentType( ReportEngineOutput.CONTENT_TYPE_HTML );
            } else {
                output.setContentType( ReportEngineOutput.CONTENT_TYPE_TEXT );
            }

            return output;
        } catch( Exception e ) {
            throw new ProviderException( e );
        } finally {
            ReleasableU.I.safeRelease(queryEngineOutput);
        }
    }

    public void setQueryReportEngine( QueryReportEngine queryReportEngine ) {
        this.queryReportEngine = queryReportEngine;
    }
}