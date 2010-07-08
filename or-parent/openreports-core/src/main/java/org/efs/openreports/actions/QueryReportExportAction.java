package org.efs.openreports.actions;

import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.actions.admin.SessionHelper;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.engine.querycache.QueryResults;
import org.efs.openreports.engine.querycache.QueryResultsDynaBeanList;
import org.efs.openreports.objects.Report;
import org.efs.openreports.providers.CSVExportProvider;
import org.efs.openreports.providers.ExcelExportProvider;
import org.efs.openreports.providers.ExportProvider;
import org.efs.openreports.providers.XlsxExportProvider;
import org.efs.openreports.util.DisplayProperty;
import org.efs.openreports.util.IoU;
import org.efs.openreports.util.ORUtil;
import org.efs.openreports.util.TimeU;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Exports the results of a query report to excel or csv.
 * 
 * @author mconner
 * 
 */
public class QueryReportExportAction extends ActionSupport {

    private static final long serialVersionUID = -5794223025849527550L;

    protected static Logger log = Logger.getLogger( QueryReportExportAction.class );

    private ExcelExportProvider excelExportProvider;
    private CSVExportProvider csvExportProvider;
    private XlsxExportProvider xlsxExportProvider;
    private Report report;
    private QueryResults results;
    private DisplayProperty[] properties;

    private int exportTypeCode;

    @Override
    public String execute() throws Exception {
        ExportType exportType = ExportType.findByCode( exportTypeCode );
        String contentType = getContentType( exportType );
        String extention = ORUtil.getContentExtension( contentType );

        SessionHelper sessionHelper = new SessionHelper();
        report = sessionHelper.get( ORStatics.REPORT, Report.class );
        results = sessionHelper.get( ORStatics.QUERY_REPORT_RESULTS, QueryResults.class );
        properties = sessionHelper.get( ORStatics.QUERY_REPORT_PROPERTIES, DisplayProperty[].class );

        HttpServletResponse response = ServletActionContext.getResponse();
        OutputStream outputStream = response.getOutputStream();
        String fileName = report.getName().replace( " ", "" ) + extention;
        String exportId = report.getName().replace( " ", "" ) + TimeU.I.formatNowForSortable();
        response.setHeader( "Content-disposition", "attachment; filename=\"" + fileName + "\"" );
        response.setContentType( contentType );
        List<DynaBean> beans = new QueryResultsDynaBeanList( results );
        selectExportProvider( exportType ).export( beans.iterator(), properties, outputStream, exportId );
        IoU.I.safeFlushAndClose( outputStream, "response output stream for report, " + report.getName() );

        return NONE;
    }

    public void setExcelExportProvider( ExcelExportProvider excelExporter ) {
        this.excelExportProvider = excelExporter;
    }

    public void setCsvExportProvider( CSVExportProvider csvExportProvider ) {
        this.csvExportProvider = csvExportProvider;
    }

    public void setXlsxExportProvider( XlsxExportProvider xlsxExportProvider ) {
        this.xlsxExportProvider = xlsxExportProvider;
    }

    public void setExportTypeCode( int exportTypeCode ) {
        this.exportTypeCode = exportTypeCode;
    }

    public int getExportTypeCode() {
        return exportTypeCode;
    }

    private String getContentType( ExportType exportType ) {
        String contentType;
        switch( exportType ) {
        case CSV:
            contentType = ReportEngineOutput.CONTENT_TYPE_CSV;
            break;
        case XLSX:
            contentType = ReportEngineOutput.CONTENT_TYPE_XLSX;
            break;
        case EXCEL:
        case XLS:
        default: // TODO: should report error for unimplemented types.
            contentType = ReportEngineOutput.CONTENT_TYPE_XLS;
        }
        return contentType;
    }

    private ExportProvider selectExportProvider( ExportType exportType ) {
        switch( exportType ) {
        case CSV:
            return csvExportProvider;
        case XLSX:
            return xlsxExportProvider;
        case EXCEL:
        case XLS:
        default:
            return excelExportProvider;
        }
    }

}
