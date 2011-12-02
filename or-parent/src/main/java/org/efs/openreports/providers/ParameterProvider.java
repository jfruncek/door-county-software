/*
 * Copyright (C) 2003 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.efs.openreports.providers;

import java.util.List;
import java.util.Map;

import org.efs.openreports.objects.*;
import org.efs.openreports.util.scripting.ParameterGroovyContext;

public interface ParameterProvider {
    public ReportParameterValue[] getParamValues( ReportParameter reportParameter, Map<String, Object> parameters )
            throws ProviderException;

    /**
     * Loads allowed values for those parameters with a finite set of allowed values (e.g. list, query, boolean).
     * 
     * @param reportParameters The parameters whose allowed values (if any) are to be loaded.
     * @param parameters existing parameters that provided a context for loading the parameters (i.e. for a query)
     * @throws ProviderException
     */
    public void loadReportParameterValues( List<ReportParameterMap> reportParameters, Map<String, Object> parameters )
            throws ProviderException;

    public List<ReportParameter> getReportParameters( Report report, String type ) throws ProviderException;

    public Map<String, Object> getReportParametersMap( List<ReportParameterMap> reportParameters,
            Map<String, Object> parameters ) throws ProviderException;

    public Map<String, Object> getReportParametersMap( List<ReportParameterMap> reportParameters,
            Map<String, Object> parameters, boolean parse ) throws ProviderException;
    
    public Map<String, Object> convertParameters( List<ReportParameterMap> reportParameterMaps,
            Map<String, Object> origParameters, Object failValue, ParameterGroovyContext parameterGroovyContext );    

    public boolean validateParameters( List<ReportParameterMap> reportParameters, Map<String, Object> parameters )
            throws ProviderException;

    public ReportParameter getReportParameter( Integer id ) throws ProviderException;

    public ReportParameter getReportParameter( String name ) throws ProviderException;

    public List<ReportParameter> getReportParameters() throws ProviderException;

    public ReportParameter insertReportParameter( ReportParameter reportParameter, String insertUser ) throws ProviderException;

    public void updateReportParameter( ReportParameter reportParameter, String updateUserName ) throws ProviderException;

    public void deleteReportParameter( ReportParameter reportParameter ) throws ProviderException;

    public List<ReportParameter> getAvailableParameters( Report report ) throws ProviderException;

}