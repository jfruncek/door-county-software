/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.efs.openreports.util.CtrlDataObject;

public class Report implements CtrlDataObject, Comparable<Report>, Serializable
{
	private static final long serialVersionUID = 4068258161793785996l;

	public static final Pattern REPORT_PROCESSOR_PATTERN = Pattern.compile( "^--report-processor=(\\S+)\\s*$", Pattern.MULTILINE );

	public static final Pattern REPORT_POST_PROCESSOR_PATTERN = Pattern.compile( "^--report-postprocessor=(.*)$", Pattern.MULTILINE );

	private Integer id;
	
	private String ctrlUser;
	
	private Date ctrlDate;

	private String name;

	private String description;
	
	private String docUrl;
	
	private String notes;

	private String file;

	private String query;

	private ReportDataSource dataSource;

	private ReportChart reportChart;

	private List<ReportParameterMap> parameters;

	private boolean pdfExportEnabled;

	private boolean htmlExportEnabled;

	private boolean csvExportEnabled;

	private boolean xlsExportEnabled;

	private boolean rtfExportEnabled;

	private boolean textExportEnabled;

	private boolean excelExportEnabled;
	
	private boolean imageExportEnabled;
	
	private boolean virtualizationEnabled;
	
	private boolean hidden;

	private ReportExportOption reportExportOption;
	
	private transient boolean displayInline;

	public Report()
	{
	}

	public boolean isBirtReport()
	{
		return hasFileExtension("rptdesign");
	}

    public boolean isESpreadsheetReport() {
        return hasFileExtension("sox") || hasFileExtension("sod"); 
    }
	

	public boolean isJasperReport()
	{
	    return hasFileExtension("jasper");
	}
	
	/**
     * Kludge: keep this implementation in sync with actual engine implementations (but better than putting it in the
     * jsps.
     * 
     * @return
     */
	public boolean isBuildParametersListImplemented() {
	    return isBirtReport() || isESpreadsheetReport() || isJasperReport();
	}
	
	public boolean isQueryReport() {
	    if( !hasQueryText() ) {
            return false;
        }
        if( isJXLSReport() || isJFreeReport() || isVelocityReport() || isReportProcessorReport() ||
                isESpreadsheetReport() ) {
            return false;
        }
        return true;
    }

	public boolean isChartReport()
	{
		return !hasQueryText() && !hasFile() && reportChart != null;
	}
	
    public boolean isJFreeReport()
	{
		return hasQueryText() && hasFileExtension("xml") && !isJPivotReport();
	}
	
	public boolean isJXLSReport()
	{
		return hasFileExtension("xls");
	}
	
	/**
	 * If the query contains the report processor pattern, its one of those
	 * @return
	 */
    public boolean isReportProcessorReport()
    {
        if (!hasQueryText()) return false;
        
        Matcher m = REPORT_PROCESSOR_PATTERN.matcher(query);
        return m.find();
    }

    public boolean hasReportPostProcessor() {
        return getReportPostProcessorDef() != null;
    }
    
    public String getReportPostProcessorDef() {
        if( hasQueryText() ) {
            Matcher m = Report.REPORT_POST_PROCESSOR_PATTERN.matcher( query );
            if( m.find() ) {
                return m.group( 1 );
            }
        }
        return null;
    }

    public boolean isJPivotReport()
    {
        return "datasources.xml".equalsIgnoreCase(file);
    }
    
    public boolean isVelocityReport()
    {
        return hasFileExtension("vm");
    }
    
	public void setId(Integer id)
	{
		this.id = id;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getFile()
	{
		return file;
	}

	public Integer getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}
	
	public String getCtrlUser()
	{
	    return ctrlUser;
	}
	
	public Date getCtrlDate()
	{
	    return ctrlDate;
	}
	
	public String getDocUrl()
	{
	    return docUrl;
	}
	
	public String getNotes()
	{
	    return notes;
	}
	
	public void setNotes(String notes)
	{
	    this.notes = notes;
	}
	
	public void setDocUrl(String docUrl)
	{
	    this.docUrl = docUrl;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setFile(String file)
	{
		this.file = file;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setCtrlUser(String ctrlUser)
	{
	    this.ctrlUser = ctrlUser;
	}
	
	public void setCtrlDate(Date ctrlDate)
	{
	    this.ctrlDate = ctrlDate;
	}

	public List<ReportParameterMap> getParameters()
	{
		return parameters;
	}
	
    public List<ReportParameterMap> getNonSubReportParameters() {
        ArrayList<ReportParameterMap> result = new ArrayList<ReportParameterMap>();
        if( parameters != null ) {
            for( ReportParameterMap rpMap : parameters ) {
                if( !rpMap.getReportParameter().isSubreportParam() ) {
                    result.add( rpMap );
                }
            }
        }
        return result;
    }
    
    public boolean hasNonSubReportParameters() {
        if( parameters != null ) {
            for( ReportParameterMap rpMap : parameters ) {
                if( !rpMap.getReportParameter().isSubreportParam() ) {
                    return true;
                }
            }
        }
        return false;
    }

	public List<ReportParameterMap> getSubReportParameters()
	{
		ArrayList<ReportParameterMap> subReportParameters = new ArrayList<ReportParameterMap>();

		if (parameters != null)
		{
			Iterator<ReportParameterMap> iterator = parameters.iterator();
			while (iterator.hasNext())
			{
				ReportParameterMap rpMap = iterator.next();

				if (rpMap.getReportParameter().getType().equals(
						ReportParameter.SUBREPORT_PARAM))
				{
					subReportParameters.add(rpMap);
				}
			}
		}

		return subReportParameters;
	}

	public void setParameters(List<ReportParameterMap> parameters)
	{
		this.parameters = parameters;
	}

	public ReportParameterMap getReportParameterMap(Integer parameterId)
	{
		Iterator<ReportParameterMap> iterator = parameters.iterator();
		while (iterator.hasNext())
		{
			ReportParameterMap rpMap = iterator.next();

			if (rpMap.getReportParameter().getId().equals(parameterId))
			{
				return rpMap;
			}
		}

		return null;
	}

	public List<ReportParameterMap> getReportParametersByStep(int step)
	{
		List<ReportParameterMap> list = new ArrayList<ReportParameterMap>();

		Iterator<ReportParameterMap> iterator = parameters.iterator();
		while (iterator.hasNext())
		{
			ReportParameterMap rpMap = iterator.next();

			if (rpMap.getStep() == step)
			{
				list.add(rpMap);
			}
		}

		Collections.sort(list);

		return list;
	}

	public ReportDataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(ReportDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public int compareTo(Report report)
	{		
		return name.compareTo(report.getName());
	}

	public ReportChart getReportChart()
	{
		return reportChart;
	}

	public void setReportChart(ReportChart reportChart)
	{
		this.reportChart = reportChart;
	}

	public boolean isCsvExportEnabled()
	{
		return csvExportEnabled;
	}

	public void setCsvExportEnabled(boolean csvExportEnabled)
	{
		this.csvExportEnabled = csvExportEnabled;
	}

	public boolean isHtmlExportEnabled()
	{
		return htmlExportEnabled;
	}

	public void setHtmlExportEnabled(boolean htmlExportEnabled)
	{
		this.htmlExportEnabled = htmlExportEnabled;
	}

	public boolean isPdfExportEnabled()
	{
		return pdfExportEnabled;
	}

	public void setPdfExportEnabled(boolean pdfExportEnabled)
	{
		this.pdfExportEnabled = pdfExportEnabled;
	}

	public boolean isXlsExportEnabled()
	{
		return xlsExportEnabled;
	}

	public void setXlsExportEnabled(boolean xlsExportEnabled)
	{
		this.xlsExportEnabled = xlsExportEnabled;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public boolean isExcelExportEnabled()
	{
		return excelExportEnabled;
	}

	public void setExcelExportEnabled(Boolean excelExportEnabled)
	{
		if (excelExportEnabled == null) excelExportEnabled = new Boolean(false);
		this.excelExportEnabled = excelExportEnabled.booleanValue();
	}

	public boolean isRtfExportEnabled()
	{
		return rtfExportEnabled;
	}

	public void setRtfExportEnabled(Boolean rtfExportEnabled)
	{
		if (rtfExportEnabled == null) rtfExportEnabled = new Boolean(false);
		this.rtfExportEnabled = rtfExportEnabled.booleanValue();
	}

	public boolean isTextExportEnabled()
	{
		return textExportEnabled;
	}

	public void setTextExportEnabled(Boolean textExportEnabled)
	{
		if (textExportEnabled == null) textExportEnabled = new Boolean(false);
		this.textExportEnabled = textExportEnabled.booleanValue();
	}
	
	public boolean isImageExportEnabled()
	{
		return imageExportEnabled;
	}

	public void setImageExportEnabled(Boolean imageExportEnabled)
	{
		if (imageExportEnabled == null) imageExportEnabled = new Boolean(false);
		this.imageExportEnabled = imageExportEnabled.booleanValue();
	}

	public ReportExportOption getReportExportOption()
	{
		if (reportExportOption == null) reportExportOption = new ReportExportOption();
		return reportExportOption;
	}

	public void setReportExportOption(ReportExportOption reportExportOption)
	{
		this.reportExportOption = reportExportOption;
	}

	/**
	 * applies only to Jasper reports.
	 * @return
	 */
	public boolean isVirtualizationEnabled()
	{
		return virtualizationEnabled;
	}

	public void setVirtualizationEnabled(Boolean virtualizationEnabled)
	{
		if (virtualizationEnabled == null) virtualizationEnabled = new Boolean(false);
		this.virtualizationEnabled = virtualizationEnabled.booleanValue();
	}
	
	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(Boolean hidden)
	{
		if (hidden == null) hidden = new Boolean(false);
		this.hidden = hidden.booleanValue();
	}

	public boolean isDisplayInline()
	{
		return displayInline;
	}

	public void setDisplayInline(boolean displayInline)
	{
		this.displayInline = displayInline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Report other = (Report) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
    private boolean hasFileExtension(String extension) {
        return file != null && file.endsWith(extension);
    }
    
    /**
     * Indicates that the query text is not blank. Note that in some cases, the query field has been
     * hijacked to serve other purposes. As such, it may contain multiple queries, or even
     * directives to the report engine.
     * 
     * @return
     */
    public boolean hasQueryText() {
        return StringUtils.isNotBlank( query );
    }

    private boolean hasFile() {
        return file != null && ! file.equals("-1");
    }

    public boolean isExportEnabled() {
        boolean result = isPdfExportEnabled() ||isHtmlExportEnabled()
        || isCsvExportEnabled() || isXlsExportEnabled() || isRtfExportEnabled()
        || isTextExportEnabled() || isExcelExportEnabled() || isImageExportEnabled();
        return result;
    }
    
}

