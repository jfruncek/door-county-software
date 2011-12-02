/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.actions.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.ORTag;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportChart;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportParameterValue;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.ChartProvider;
import org.efs.openreports.providers.DataSourceProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.TagProvider;
import org.efs.openreports.util.LocalStrings;

import com.opensymphony.xwork2.ActionSupport;

public class EditReportAction extends ActionSupport implements SessionAware {

	private static final long serialVersionUID = 6851899249540541143L;

    private static final int ID_NONE = -1; // must match hardcoded in jsp.

    private static final String CMD_ADD = "add";
    private static final String CMD_EDIT = "edit";

    private static final int ACTION_TYPE_UNKNOWN = 0;
    private static final int ACTION_TYPE_DISPLAY_FOR_EDIT = 1;
    private static final int ACTION_TYPE_DISPLAY_FOR_ADD = 2;
    private static final int ACTION_TYPE_UPDATE = 3;
    private static final int ACTION_TYPE_CREATE = 4;
    private static final int ACTION_TYPE_VALIDATE = 5; // never used
    private static final int ACTION_TYPE_DUPLICATE = 6;
    private static final int ACTION_TYPE_DUPLICATE_ON_ADD = 7;

    protected static Logger log = Logger.getLogger( EditReportAction.class );

    private Map<Object, Object> session;
    private String command;
    private int selectedTab = 1;
    private boolean submitOk;
    private boolean submitValidate;
    private boolean submitDuplicate;

    private int id;
    private String name;
    private String description;
    private String docUrl;

    private String tags;
    private String notes;
    private String file;
    private String query;
    private int dataSourceId = ID_NONE;
    private int reportChartId = ID_NONE;

    private boolean pdfExportEnabled;
    private boolean htmlExportEnabled;
    private boolean csvExportEnabled;
    private boolean xlsExportEnabled;
    private boolean rtfExportEnabled;
    private boolean textExportEnabled;
    private boolean excelExportEnabled;
    private boolean imageExportEnabled;
    private boolean virtual;
    private boolean hidden;

    private Report report;

    private ReportProvider reportProvider;
    private DataSourceProvider dataSourceProvider;
    private ChartProvider chartProvider;
    private ParameterProvider parameterProvider;
    private TagProvider tagProvider;

    private ReportParameterValue[] parameterValues;

    @Override
    public String execute() {
        try {
            int actionType = determineExecuteType();
            switch( actionType ) {
            case ACTION_TYPE_DISPLAY_FOR_EDIT:
                return executeDisplayForEdit();
            case ACTION_TYPE_DISPLAY_FOR_ADD:
                return executeDisplayForAdd();
            case ACTION_TYPE_UPDATE:
          		return executeUpdate();
            case ACTION_TYPE_CREATE:
                return executeCreate();
            case ACTION_TYPE_DUPLICATE:
                return executeDuplicate();
            case ACTION_TYPE_DUPLICATE_ON_ADD:
                return executeDuplicateOnAdd();
            case ACTION_TYPE_UNKNOWN:
            default:
                addActionError( "invalid action/submit combination" );
                return INPUT;

            }
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return INPUT;
        }
    }

    public String[] getClassNames() {
        return ReportParameter.CLASS_NAMES;
    }

    public String getCommand() {
        return command;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public List<ReportDataSource> getDataSources() {
        try {
            return dataSourceProvider.getDataSources();
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public String getFile() {
        return file;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public List<ReportParameterMap> getParametersInReport() {
        if( report == null || report.getParameters() == null )
            return null;

        List<ReportParameterMap> list = report.getParameters();
        Collections.sort( list );

        return list;
    }

    public ReportParameterValue[] getParameterValues() {
        return parameterValues;
    }

    public String getQuery() {
        return query;
    }

    public Report getReport() {
        return report;
    }

    public int getReportChartId() {
        return reportChartId;
    }

    public List<ReportChart> getReportCharts() {
        try {
            return chartProvider.getReportCharts();
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

    public List<String> getReportFileNames() {
        try {
            return reportProvider.getReportFileNames();
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

    public List<ReportParameter> getReportParameters() {
        try {
            return parameterProvider.getAvailableParameters( report );
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public String getTags() {
        return tags;
    }

    public String[] getTypes() {
        return ReportParameter.TYPES;
    }

    public boolean isCsvExportEnabled() {
        return csvExportEnabled;
    }

    public boolean isExcelExportEnabled() {
        return excelExportEnabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isHtmlExportEnabled() {
        return htmlExportEnabled;
    }

    public boolean isImageExportEnabled() {
        return imageExportEnabled;
    }

    public boolean isPdfExportEnabled() {
        return pdfExportEnabled;
    }

    public boolean isRtfExportEnabled() {
        return rtfExportEnabled;
    }

    public boolean isTextExportEnabled() {
        return textExportEnabled;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isXlsExportEnabled() {
        return xlsExportEnabled;
    }

    public void setChartProvider( ChartProvider chartProvider ) {
        this.chartProvider = chartProvider;
    }

    public void setCommand( String command ) {
        this.command = command;
    }

    public void setCsvExportEnabled( boolean csvExportEnabled ) {
        this.csvExportEnabled = csvExportEnabled;
    }

    public void setDataSourceId( int dataSourceId ) {
        this.dataSourceId = dataSourceId;
    }

    public void setDataSourceProvider( DataSourceProvider dataSourceProvider ) {
        this.dataSourceProvider = dataSourceProvider;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public void setDocUrl( String docUrl ) {
        this.docUrl = docUrl;
    }

    public void setExcelExportEnabled( boolean excelExportEnabled ) {
        this.excelExportEnabled = excelExportEnabled;
    }

    public void setFile( String file ) {
        this.file = file;
    }

    public void setHidden( boolean hidden ) {
        this.hidden = hidden;
    }

    public void setHtmlExportEnabled( boolean htmlExportEnabled ) {
        this.htmlExportEnabled = htmlExportEnabled;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public void setImageExportEnabled( boolean imageExportEnabled ) {
        this.imageExportEnabled = imageExportEnabled;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public void setNotes( String notes ) {
        this.notes = notes;
    }

    public void setParameterProvider( ParameterProvider parameterProvider ) {
        this.parameterProvider = parameterProvider;
    }

    public void setPdfExportEnabled( boolean pdfExportEnabled ) {
        this.pdfExportEnabled = pdfExportEnabled;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    public void setReportChartId( int reportChartId ) {
        this.reportChartId = reportChartId;
    }

    public void setReportProvider( ReportProvider reportProvider ) {
        this.reportProvider = reportProvider;
    }

    public void setRtfExportEnabled( boolean rtfExportEnabled ) {
        this.rtfExportEnabled = rtfExportEnabled;
    }

    public void setSelectedTab( int selectedTab ) {
        this.selectedTab = selectedTab;
    }

    @SuppressWarnings( "unchecked" )
    public void setSession( Map session ) {
        this.session = session;
    }

    public void setSubmitDuplicate( String submitDuplicate ) {
        if( submitDuplicate != null )
            this.submitDuplicate = true;
    }

    public void setSubmitOk( String submitOk ) {
        if( submitOk != null )
            this.submitOk = true;
    }

    public void setSubmitValidate( String submitValidate ) {
        if( submitValidate != null )
            this.submitValidate = true;
    }

    public void setTagProvider( TagProvider tagProvider ) {
        this.tagProvider = tagProvider;
    }

    public void setTags( String tags ) {
        this.tags = tags;
    }

    public void setTextExportEnabled( boolean textExportEnabled ) {
        this.textExportEnabled = textExportEnabled;
    }

    public void setVirtual( boolean virtual ) {
        this.virtual = virtual;
    }

    public void setXlsExportEnabled( boolean xlsExportEnabled ) {
        this.xlsExportEnabled = xlsExportEnabled;
    }

    private boolean commandIsAdd() {
        return command.equals( CMD_ADD );
    }

    private boolean commandIsEdit() {
        return command.equals( CMD_EDIT );
    }

    /**
     * Determine the type of execute (i.e. the subaction, which is really a combination of the
     * command field and the submit flags.
     * 
     * @return one of EXEC_TYPE
     */
    private int determineExecuteType() {
        int result = ACTION_TYPE_UNKNOWN;
        if( commandIsEdit() ) {
            if( submitOk ) {
                result = ACTION_TYPE_UPDATE;
            } else if( submitValidate ) {
                result = ACTION_TYPE_VALIDATE;
            } else if( submitDuplicate ) {
                result = ACTION_TYPE_DUPLICATE;
            } else {
                result = ACTION_TYPE_DISPLAY_FOR_EDIT;
            }
        } else if( commandIsAdd() ) {
            if( submitOk ) {
                result = ACTION_TYPE_CREATE;
            } else if( submitValidate ) {
                result = ACTION_TYPE_VALIDATE;
            } else if( submitDuplicate ) {
                result = ACTION_TYPE_DUPLICATE_ON_ADD; // silly!
            } else {
                result = ACTION_TYPE_DISPLAY_FOR_ADD;
            }
        }
        return result;
    }

    private String executeCreate() throws ProviderException {
        report = new Report();
        ReportUser reportUser = fetchReportUser();
        populateReportFromAction();        
        validate();
        if ( hasActionErrors() ) {
    		return INPUT;
        }
        report = reportProvider.insertReport( report, reportUser.getName() );
        updateTags();
        return SUCCESS;
    }

    private String executeDisplayForAdd() throws ProviderException {
        report = new Report();
        // Could do a populateActionFromReport (i think), too, but currently no need.
        return INPUT;
    }

    private String executeDisplayForEdit() throws ProviderException {
        report = fetchReport();
        populateActionFromReport();
        return INPUT;
    }

    private String executeDuplicate() throws ProviderException {
        report = fetchReport();
        report.setId( null );
        report.getReportExportOption().setId( null );
        name = dupName(name, report.getName());
        populateReportFromAction();
        ReportUser reportUser = fetchReportUser();
        if( report.getParameters() != null && !report.getParameters().isEmpty() ) {
            List<ReportParameterMap> parameters = report.getParameters();

            report.setParameters( null );
            report = reportProvider.insertReport( report, reportUser.getName() );

            ArrayList<ReportParameterMap> duplicateParameters = new ArrayList<ReportParameterMap>();
            for( int index = 0; index < parameters.size(); index++ ) {
                ReportParameterMap map = parameters.get( index );
                map.setReport( report );
                duplicateParameters.add( map );
            }

            report.setParameters( duplicateParameters );
            reportProvider.updateReport( report, reportUser.getName() );
        } else {
            report = reportProvider.insertReport( report, reportUser.getName() );
        }
        updateTags();
        return SUCCESS;
    }

    private String dupName( String candidateName, String existingName ) {
        String result = candidateName;
        if( StringUtils.equals( candidateName, existingName ) ) {
            result = "Copy of ".concat( existingName );
        }
        return result;
    }

    private String executeDuplicateOnAdd() {
        // TODO, this shouldn't really even be an option on the screen.
        addActionError( "Cannot make a duplicate of an unsaved report." );
        return INPUT;
    }

    private String executeUpdate() throws ProviderException {
        report = fetchReport();
        populateReportFromAction();
        validate();
        if ( hasActionErrors() ) {
    		return INPUT;
        }
        updateReport();
        updateTags();
        return SUCCESS;
    }

    public void validate() {
        if( StringUtils.isBlank( name ) ) {
            addActionError( getText( LocalStrings.ERROR_REPORT_NAME_BLANK ) );
        }

        if( ! hasReportTemplate() && StringUtils.isBlank( report.getQuery() ) ) {
        	addActionError( getText( LocalStrings.ERROR_REPORT_QUERY_BLANK ) );
        }
        
        String upperCaseQuery = query.toUpperCase(); 
        if ( upperCaseQuery.indexOf( "P${" ) >= 0 ) {
        	addActionError(getText( LocalStrings.ERROR_REPORT_QUERY_INVALID) );
        }
        
        validateReportParameters(query);

        // FIXME validate query as parameter...
/*        ReportParameter reportParameter = new ReportParameter();
        reportParameter.setData( query );
        reportParameter.setType( ReportParameter.QUERY_PARAM );
        reportParameter.setDataSource( report.getDataSource() );
        reportParameter.setClassName( "java.lang.String" );

        parameterValues = parameterProvider.getParamValues( reportParameter, map );
*/
    }
    
    private void validateReportParameters(String query) {
    	int beginIndex = 0;
    	int endIndex = 0;
		while (query.indexOf("$P{", endIndex) > 0) 
		{
			beginIndex = query.indexOf("$P{", endIndex);
			endIndex = query.indexOf("}", beginIndex);

			String name = query.substring(beginIndex + 3, endIndex);
			
	        ReportParameter queryParameter;
			try {
				queryParameter = parameterProvider.getReportParameter(name);
		        if (queryParameter == null) {
					addActionError(getText( LocalStrings.ERROR_REPORT_QUERY_PARAMETERS) );
		        }
			} catch (ProviderException e) {
				log.warn("Could not get report parameter: " + name);
			}
		}
    }

	private boolean hasReportTemplate() {
		return file != null && ! String.valueOf(ID_NONE).equals(file);
	}

    private ReportDataSource fetchDataSource( int dataSourceFetchId ) throws ProviderException {
        return ( dataSourceFetchId == ID_NONE ) ? null : dataSourceProvider.getDataSource( new Integer( dataSourceId ) );
    }

    private Report fetchReport() throws ProviderException {
        return reportProvider.getReport( new Integer( id ) );
    }

    private ReportChart fetchReportChart( int reportChartFetchId ) throws ProviderException {
        return ( reportChartId == ID_NONE ) ? null : chartProvider.getReportChart( new Integer( reportChartId ) );
    }

    private ReportUser fetchReportUser() {
        return (ReportUser) session.get( ORStatics.REPORT_USER );
    }

    private int fetchReportChartId() {
        int result = ( report.getReportChart() == null ) ? ID_NONE : report.getReportChart().getId().intValue();
        return result;
    }

    private int fetchReportDataSourceId() {
        int result = ( report.getDataSource() == null ) ? ID_NONE : report.getDataSource().getId().intValue();
        return result;
    }

    private void populateActionFromReport() throws ProviderException {
        name = report.getName();
        description = report.getDescription();
        docUrl = report.getDocUrl();
        tags = tagProvider.getTagsForObject( report.getId(), Report.class, ORTag.TAG_TYPE_UI );
        notes = report.getNotes();
        file = report.getFile();
        query = report.getQuery();
        id = report.getId().intValue();
        pdfExportEnabled = report.isPdfExportEnabled();
        csvExportEnabled = report.isCsvExportEnabled();
        htmlExportEnabled = report.isHtmlExportEnabled();
        xlsExportEnabled = report.isXlsExportEnabled();
        rtfExportEnabled = report.isRtfExportEnabled();
        textExportEnabled = report.isTextExportEnabled();
        excelExportEnabled = report.isExcelExportEnabled();
        imageExportEnabled = report.isImageExportEnabled();
        virtual = report.isVirtualizationEnabled();
        hidden = report.isHidden();
        dataSourceId = fetchReportDataSourceId();
        reportChartId = fetchReportChartId();
    }

    private void populateReportFromAction() throws ProviderException {
        report.setName( name );
        report.setDescription( description );
        report.setDocUrl( docUrl );
        report.setFile( file );
        report.setNotes( notes );
        report.setQuery( query );
        report.setCsvExportEnabled( csvExportEnabled );
        report.setHtmlExportEnabled( htmlExportEnabled );
        report.setPdfExportEnabled( pdfExportEnabled );
        report.setXlsExportEnabled( xlsExportEnabled );
        report.setRtfExportEnabled( new Boolean( rtfExportEnabled ) );
        report.setTextExportEnabled( new Boolean( textExportEnabled ) );
        report.setExcelExportEnabled( new Boolean( excelExportEnabled ) );
        report.setImageExportEnabled( new Boolean( imageExportEnabled ) );
        report.setVirtualizationEnabled( new Boolean( virtual ) );
        report.setHidden( new Boolean( hidden ) );

        if( StringUtils.isBlank( report.getQuery() ) && !report.isExportEnabled() ) {
            // Force pdf if not otherwise specified (why??, I don't know)
            report.setPdfExportEnabled( true );
        }

        report.setDataSource( fetchDataSource( dataSourceId ) );
        report.setReportChart( fetchReportChart( reportChartId ) );
    }

    private void updateReport() throws ProviderException {
        ReportUser reportUser = fetchReportUser();
        reportProvider.updateReport( report, reportUser.getName() );
    }

    private void updateTags() throws ProviderException {
        tagProvider.setTags( report.getId(), Report.class, tags, ORTag.TAG_TYPE_UI );
    }

}
