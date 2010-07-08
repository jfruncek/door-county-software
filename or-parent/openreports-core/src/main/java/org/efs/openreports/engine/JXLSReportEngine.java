/*
 * Copyright (C) 2006 Erik Swenson - erik@oreports.com
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package org.efs.openreports.engine;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.efs.openreports.ORStatics;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.QueryEngineOutput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.querycache.QueryResultsDynaBeanList;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ORUtil;
import org.efs.openreports.util.ReleasableU;

/**
 * Generates a ReportEngineOutput from a ReportEngineInput. The output consists
 * of a byte[] containing an XLS spreadsheet.
 * 
 * @author Erik Swenson
 * 
 */

public class JXLSReportEngine extends ReportEngine {
	private static final String MULTIPLE_SQL_DELIMITER = "~~~";
	protected static Logger log = Logger.getLogger(JXLSReportEngine.class);
	ScriptEngineManager mgr = new ScriptEngineManager();
	QueryReportEngine queryReportEngine;
	
	public JXLSReportEngine() {
    }

	public ReportEngineOutput generateReport(ReportEngineInput reportEngineInput) throws ProviderException {
		Connection conn = null;

		try {
			Report report = reportEngineInput.getReport();

			// create new HashMap to send to JXLS in order to maintain original
			// map of parameters
			Map<String, Object> jxlsReportMap = new HashMap<String, Object>(reportEngineInput.getParameters());

			if (report.getQuery() != null && report.getQuery().trim().length() > 0) {
				if (report.getQuery().indexOf(MULTIPLE_SQL_DELIMITER) > -1) {
					createMultipleJxlsBeans(jxlsReportMap, reportEngineInput.getReport());
				} else {
					createJxlsBean(reportEngineInput, jxlsReportMap, report.getQuery());
				}

			} else {
				conn = dataSourceProvider.getConnection(report.getDataSource().getId());
				JXLSReportManagerImpl rm = new JXLSReportManagerImpl(conn, jxlsReportMap, dataSourceProvider);
				jxlsReportMap.put("rm", rm);
			}

			FileInputStream template = new FileInputStream(directoryProvider.getReportDirectory() + report.getFile());

			XLSTransformer transformer = new XLSTransformer();
			HSSFWorkbook workbook = transformer.transformXLS(template, jxlsReportMap);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);

			ReportEngineOutput output = new ReportEngineOutput();

			ReportPostProcessExecutor postProcessExecutor = new ReportPostProcessExecutor(directoryProvider);
			output.setContent(postProcessExecutor.postProcess(out.toByteArray(), reportEngineInput));
			output.setContentType(ReportEngineOutput.CONTENT_TYPE_XLS);

			return output;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ProviderException(e);
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception c) {
				log.error("Error closing");
			}
		}
	}

	public void setQueryReportEngine( QueryReportEngine queryReportEngine ) {
        this.queryReportEngine = queryReportEngine;
    }

	private void createMultipleJxlsBeans(Map<String, Object> jxlsReportMap, Report originalReport)
			throws ProviderException {
		Pattern pattern = Pattern.compile(".*jxlsbean\\s*=\\s*(\\w+):*(\\w+)*.*", Pattern.CASE_INSENSITIVE);

		ReportEngineInput input = new ReportEngineInput((Report) ORUtil.deepCopy(originalReport), jxlsReportMap);
		ReportDataSource originalDataSource = (ReportDataSource) ORUtil.deepCopy(originalReport.getDataSource());
		String[] queries = input.getReport().getQuery().split(MULTIPLE_SQL_DELIMITER);
		for (int i = 0; i < queries.length; i++) {
			String query = queries[i];
			input.getReport().setQuery(query);
			addResultsAsBean(jxlsReportMap, input, pattern, i, query, originalDataSource);
		}
	}

	private void addResultsAsBean( Map<String, Object> jxlsReportMap, ReportEngineInput input, Pattern pattern,
            int beanNo, String query, ReportDataSource defaultDataSource ) {
        Matcher matcher = pattern.matcher( query );
        String beanName = getBeanName( matcher, beanNo + 1 );
        String datasourceName = matcher.group( 2 );
        if( datasourceName != null ) {
            try {
                input.getReport().setDataSource( dataSourceProvider.getDataSource( datasourceName ) );
            } catch( ProviderException e ) {
                log.warn( "Could not get data source: " + datasourceName );
                return;
            }
        } else {
            input.getReport().setDataSource( defaultDataSource );
        }
        QueryEngineOutput reportEngineOutput = null;
        try {
            reportEngineOutput = (QueryEngineOutput) queryReportEngine.generateReport( input );
            List<DynaBean> beans = new QueryResultsDynaBeanList( reportEngineOutput.getResults() );
            jxlsReportMap.put( beanName, beans );
        } catch( ProviderException e ) {
            log.warn( "Skipped query for bean: " + beanName );
            return;
        } finally {
            ReleasableU.I.safeRelease( reportEngineOutput );
        }
    }

	private String getBeanName(Matcher matcher, int queryNo) {
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return ORStatics.JXLS_REPORT_RESULTS + queryNo;
		}

	}

	private void createJxlsBean(ReportEngineInput input, Map<String, Object> jxlsReportMap, String query)
			throws ProviderException {
		if (!query.startsWith("#!") || query.startsWith("#!sql")) {
			createBeanFromSql(input, jxlsReportMap);
		} else if (query.startsWith("#!groovy")) {
			createBeanFromScript(input, "groovy", query, jxlsReportMap);
		}

	}

	private void createBeanFromSql( ReportEngineInput input, Map<String, Object> jxlsReportMap )
            throws ProviderException {
        QueryEngineOutput output = null;
        try {
            output = (QueryEngineOutput) queryReportEngine.generateReport( input );
            jxlsReportMap.put( ORStatics.JXLS_REPORT_RESULTS, new QueryResultsDynaBeanList( output.getResults() ) );
        } finally {
            ReleasableU.I.safeRelease( output );
        }
    }

	private void createBeanFromScript(ReportEngineInput input, String language, String query,
			Map<String, Object> jxlsReportMap) throws ProviderException {
		ScriptEngine engine = mgr.getEngineByExtension(language);
		engine.put("jxlsReportMap", jxlsReportMap);
		HashMap<String, Object> params = new HashMap<String, Object>();
		engine.put("params", params);
		for (String p : input.getParameters().keySet()) {
			params.put(p, input.getParameters().get(p));
		}

		try {
			engine.eval(query);
		} catch (ScriptException e) {
			throw new ProviderException(e); // To change body of catch statement
			// use File | Settings | File
			// Templates.
		}
	}

	public List<ReportParameter> buildParameterList(Report report) throws ProviderException {
		throw new ProviderException("JXLSReportEngine: buildParameterList not implemented.");
	}

	static final int SQL = 0;
	static final int GROOVY = 1;
}