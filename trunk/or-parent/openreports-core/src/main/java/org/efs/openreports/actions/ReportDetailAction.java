/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.DateProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.ParameterAwareHelp;

import com.opensymphony.xwork2.ActionSupport;

/**
 * TODO: the nomenclature here and elsewhere is poor, and should be cleaned up. 
 * 1) report parameters refers to both ReportParameterMap and a Map<String, Object> of key values pairs. 
 * 2) ReportParameterMap is not actually a map, but a relation, so its a bit confusing.   
 * 3) There's also a getReportParameter ON the ReportParameterMap
 * So there's three different things all being called report parameter, at some level.
 */
public class ReportDetailAction extends ActionSupport implements SessionAware, ParameterAware
{	
	private static final long serialVersionUID = 724821018564650888L;
	
	private Map<Object,Object> session;
	private Map<String,Object> parameters;
	
	private Report report; 
	private int reportId = Integer.MIN_VALUE;
	
	private String submitType;

	private ParameterProvider parameterProvider;
	private ReportProvider reportProvider;
	private DateProvider dateProvider;

	private List<ReportParameterMap> reportParameters;
	private int step = 0;
	
	private boolean displayInline;	

	@Override
	public String execute() {
        try {
            ReportUser user = (ReportUser) session.get( ORStatics.REPORT_USER );

            report = reportProvider.getReport( new Integer( reportId ) );

            if( !reportLoadedOK( report ) )
                return ERROR;
            if( !userAuthorized( user, report ) )
                return ERROR;

            report.setDisplayInline( displayInline );
            reportParameters = report.getReportParametersByStep( step );

            if( initialSubmit() ) {
                // First time through create new map and add standard report parameters
                HashMap<String, Object> newParameterMap = buildInitialParameterMap( user );
                clearParametersInSession();
                setParametersInSession( newParameterMap );
                session.put( ORStatics.REPORT, report );

                if( report.hasNonSubReportParameters() ) {
                    parameterProvider.loadReportParameterValues( reportParameters, newParameterMap );
                    return INPUT;
                } else {
                    return SUCCESS;
                }
            } else {
                parameterProvider.validateParameters( reportParameters, parameters );

                Map<String, Object> map = getReportParametersFromSession();
                Map<String, Object> currentMap = parameterProvider
                        .getReportParametersMap( reportParameters, parameters, false );
                map.putAll( currentMap ); 
                setParametersInSession( map );

                step++;
                reportParameters = report.getReportParametersByStep( step );

                if( reportParameters.size() > 0 ) {
                    parameterProvider.loadReportParameterValues( reportParameters, map );
                    return INPUT;
                }

                return SUCCESS;
            }
        } catch( Exception e ) {
            // Make an attempt to ensure that the allowed parameter values got loaded, regardless:
            Map<String, Object> sessionParameters = getReportParametersFromSession();
            try {
                parameterProvider.loadReportParameterValues( reportParameters, sessionParameters );
            } catch( ProviderException pe ) {
                ActionHelper.addExceptionAsError( this, pe );
            }

            ActionHelper.addExceptionAsError( this, e );
            return INPUT;
        }
    }

    private HashMap<String, Object> buildInitialParameterMap( ReportUser user ) {
        HashMap<String,Object> newParameterMap = new HashMap<String,Object>();
        newParameterMap.put(ORStatics.USER_ID, user.getId());
        newParameterMap.put(ORStatics.EXTERNAL_ID, user.getExternalId());
        newParameterMap.put(ORStatics.USER_NAME, user.getName());
        return newParameterMap;
    }

    private boolean initialSubmit() {
        return submitType == null;
    }

    private boolean userAuthorized( ReportUser user, Report report2 ) {
        if( user.isValidReport( report2 ) )
            return true;
        addActionError( getText( LocalStrings.ERROR_REPORT_NOTAUTHORIZED ) );
        return false;
    }

    private boolean reportLoadedOK( Report report ) {
        if( report != null )
            return true;
        addActionError( getText( LocalStrings.ERROR_REPORT_INVALID ) );
        return false;
    }

    private void setParametersInSession( Map<String, Object> newMap ) {
        session.put(ORStatics.REPORT_PARAMETERS, newMap);
    }

    private void clearParametersInSession() {
        session.remove(ORStatics.REPORT_PARAMETERS);
    }    
   
	@SuppressWarnings("unchecked")
	public void setSession(Map session) 
	{
		this.session = session;
	}
	
	@SuppressWarnings("unchecked")
	public void setParameters(Map parameters) 
	{
		this.parameters = parameters;
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> getReportParametersFromSession() 
	{
		return (Map) session.get(ORStatics.REPORT_PARAMETERS);
	}
		
	public String getSubmitType()
	{
		return submitType;
	}

	public void setSubmitType(String submitType)
	{
		this.submitType = submitType;
	}

	public int getReportId()
	{
		return reportId;
	}

	public void setReportId(int reportId)
	{
		this.reportId = reportId;
	}

	public void setParameterProvider(ParameterProvider parameterProvider)
	{
		this.parameterProvider = parameterProvider;
	}

	public void setReportProvider(ReportProvider reportProvider)
	{
		this.reportProvider = reportProvider;
	}

	public List<ReportParameterMap> getReportParameters()
	{
		return reportParameters;
	}

	public int getStep()
	{
		return step;
	}

	public void setStep(int step)
	{
		this.step = step;
	}
	
	public void setDateProvider(DateProvider dateProvider)
	{
		this.dateProvider = dateProvider;
	}
	
	public String getDateFormat()
	{
		return dateProvider.getDateFormatPattern();
	}
	
	public String getDefaultDate()
	{
		return dateProvider.formatDate(new Date());
	}
	
	public Report getReport()
	{
		return report;
	}
	
	public void setReport(Report report)
	{
		this.report = report;
	}

	public boolean isDisplayInline()
	{
		return displayInline;
	}

	public void setDisplayInline(boolean displayInline)
	{
		this.displayInline = displayInline;
	}
	
	public String getParamValue( String parameterName ) {
        String value = ParameterAwareHelp.getSingleValue( parameters, parameterName );
        if( value == null ) {
            value = getDefaultValue( parameterName, null );
        }
        return value;
    }

	/**
     * @param parameterName
     * @param value
     * @return the default value, if a parameter exists with the given name.
     */
    private String getDefaultValue( String parameterName, String value ) {
        ReportParameterMap rpMap = getReportParameter(parameterName);
        if ( rpMap != null) {
            value = rpMap.getReportParameter().getDefaultValue(); 
        }
        return value;
    }


    /**
     * @param parameterName
     * @param testValue
     * @return true if testValue is selected for the parameter. 
     */
    public boolean isSelectedListValue( String parameterName, String testValue ) {
        Object[] selectedValues = getSelectedValues( parameterName );
        for( Object selectedValue : selectedValues ) {
            if( selectedValue.equals( testValue ) ) {
                return true;
            }
        }
        return false;
    }

   /**
     * @param parameterName
     * @return the selected values (if any), or default value for the parameter with the given name, or null, if no
     *         parameter found.
     */
    private Object[] getSelectedValues( String parameterName ) {
        Object[] selectedValues = ParameterAwareHelp.getMultiValue( parameters, parameterName );
        if( selectedValues == null ) {
            Object defaultValue = getDefaultValue( parameterName, null );
            selectedValues = ( defaultValue == null ) ? new Object[] {} : new Object[] { defaultValue };
        }
        return selectedValues;
    }
   
   /**
    * @param parameterName
    * @return the ReportParamterMap with the given name, or null.
    */
   protected ReportParameterMap getReportParameter( String parameterName ) {
       for( ReportParameterMap rpMap : getReportParameters() ) {
           if( rpMap.getReportParameter().getName().equals( parameterName ) ) {
               return rpMap;
           }
       }
       return null;
   }

}