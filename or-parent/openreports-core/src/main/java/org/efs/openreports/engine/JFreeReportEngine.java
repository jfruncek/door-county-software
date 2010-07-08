/*
 * Copyright (C) 2006 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.efs.openreports.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.util.JRQueryExecuter;

import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.sqlsupport.SqlU;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ORUtil;
import org.jfree.report.JFreeReport;
import org.jfree.report.JFreeReportBoot;
import org.jfree.report.modules.misc.tablemodel.ResultSetTableModelFactory;
import org.jfree.report.modules.output.pageable.pdf.PDFReportUtil;
import org.jfree.report.modules.output.table.html.HtmlProcessor;
import org.jfree.report.modules.output.table.html.StreamHtmlFilesystem;
import org.jfree.report.modules.output.table.rtf.RTFProcessor;
import org.jfree.report.modules.output.table.xls.ExcelProcessor;
import org.jfree.report.modules.parser.base.ReportGenerator;
import org.jfree.report.util.CloseableTableModel;

/**
 *  JFreeReport ReportEngine implementation.
 * 
 * @author Erik Swenson
 * 
 */
public class JFreeReportEngine extends ReportEngine
{
	protected static Logger log = Logger.getLogger(JFreeReportEngine.class);
	
	public JFreeReportEngine() {
        JFreeReportBoot boot = JFreeReportBoot.getInstance();
        boot.getEditableConfig().setConfigProperty( "org.jfree.base.LogLevel", "Info" );
        boot.start();
    }	

	public ReportEngineOutput generateReport(ReportEngineInput input) throws ProviderException
	{
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		
		CloseableTableModel model = null;
		
		try
		{
			Report report = input.getReport();
			Map<String, Object> parameters = input.getParameters();
			
			ReportDataSource dataSource = report.getDataSource();
			conn = dataSourceProvider.getConnection(dataSource.getId());

			if (parameters == null || parameters.isEmpty())
			{
				pStmt = conn.prepareStatement(report.getQuery());
			}
			else
			{
				// Use JasperReports Query logic to parse parameters in chart
				// queries

				JRDesignQuery query = new JRDesignQuery();
				query.setText(report.getQuery());

				// convert parameters to JRDesignParameters so they can be
				// parsed
				Map<String, JRDesignParameter> jrParameters = ORUtil.buildJRDesignParameters(parameters);

				pStmt = JRQueryExecuter.getStatement(query, jrParameters, parameters, conn);
			}
			
			ORProperty maxRows = propertiesProvider.getProperty(ORProperty.QUERYREPORT_MAXROWS);
			if (maxRows != null && maxRows.getValue() != null)
			{	
				pStmt.setMaxRows(Integer.parseInt(maxRows.getValue()));
			}
			
			rs = pStmt.executeQuery();		
			
			model = ResultSetTableModelFactory.getInstance().createTableModel(rs);

			ReportGenerator generator = ReportGenerator.getInstance();
			
			JFreeReport jfreeReport = generator.parseReport(directoryProvider
					.getReportDirectory()
					+ report.getFile());
			jfreeReport.setData(model);

			ReportEngineOutput output = new ReportEngineOutput();
			
			if (input.getExportType() == ExportType.PDF)
			{
				output.setContentType(ReportEngineOutput.CONTENT_TYPE_PDF);
				
				PDFReportUtil.createPDF(jfreeReport, output.getContentManager());
			}
			else if (input.getExportType() == ExportType.XLS)
			{
				output.setContentType(ReportEngineOutput.CONTENT_TYPE_XLS);
				
				ExcelProcessor pr = new ExcelProcessor(jfreeReport);
				pr.setStrictLayout(false);
				pr.setDefineDataFormats(true);
				pr.setOutputStream(output.getContentManager());
				pr.processReport();				
			}			
			else if (input.getExportType() == ExportType.RTF)
			{
				output.setContentType(ReportEngineOutput.CONTENT_TYPE_RTF);
				
				RTFProcessor pr = new RTFProcessor(jfreeReport);
				pr.setStrictLayout(false);				
				pr.setOutputStream(output.getContentManager());
				pr.processReport();				
			}	
			else //default to HTML
			{
				output.setContentType(ReportEngineOutput.CONTENT_TYPE_HTML);
				
				HtmlProcessor pr = new HtmlProcessor(jfreeReport);
				pr.setStrictLayout(false);
				pr.setGenerateXHTML(true);
				pr.setFilesystem(new StreamHtmlFilesystem(output.getContentManager()));
				pr.processReport();
			}			
			

			return output;
		}
		catch (Exception e)
		{
			throw new ProviderException(e);
		}
		finally
		{
			try
			{
				if (model != null) model.close();
			}
			catch (Exception e)
			{
				log.warn(e.toString());
			}
			
		    SqlU.I.safeClose( rs, "", log );
            SqlU.I.safeClose( pStmt, "", log );
            SqlU.I.safeClose( conn, "", log );
		}
	}	
	
	public List<ReportParameter> buildParameterList(Report report) throws ProviderException
	{
		throw new ProviderException("JFreeReportEngine: buildParameterList not implemented.");
	}		
}
