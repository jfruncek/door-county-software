package org.efs.openreports.objects;

import java.io.Serializable;
import java.util.Map;

import org.efs.openreports.ReportConstants.ExportType;

public class BookChapter implements Serializable {

	private static final long serialVersionUID = 1076671997013083853L;

	private Integer id;
	private ReportBook reportBook;
	private String name;
	private Report report;
	private Map<String,Object> parameters;
	private int exportType;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Report getReport() {
		return report;
	}
	
	public void setReport(Report report) {
		this.report = report;
	}
	
	public void setParameters(Map<String,Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String,Object> getParameters() {
		return parameters;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setExportType(int exportType) {
		this.exportType = exportType;
	}
	
	public int getExportType() {
		return exportType;
	}
	
	public void setReportBook(ReportBook reportBook) {
		this.reportBook = reportBook;
	}
	
	public ReportBook getReportBook() {
		return reportBook;
	}

	public String getExportTypeName() {
		return ExportType.findByCode(exportType).name().toLowerCase();
	}

}
