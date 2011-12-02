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

package org.efs.openreports.providers.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.util.JRQueryExecuter;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportChart;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.objects.ReportParameterValue;
import org.efs.openreports.providers.DataSourceProvider;
import org.efs.openreports.providers.DateProvider;
import org.efs.openreports.providers.HibernateProvider;
import org.efs.openreports.providers.ParameterProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ConstraintException;
import org.efs.openreports.util.LocalStrings;
import org.efs.openreports.util.ORUtil;
import org.efs.openreports.util.ParameterAwareHelp;
import org.efs.openreports.util.scripting.GroovyContext;
import org.efs.openreports.util.scripting.ParameterGroovyContext;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class ParameterProviderImpl implements ParameterProvider
{
	protected static Logger log =
		Logger.getLogger(ParameterProviderImpl.class.getName());	
	
	private DataSourceProvider dataSourceProvider;
	private DateProvider dateProvider;
	private HibernateProvider hibernateProvider;
	
	public ParameterProviderImpl(DataSourceProvider dataSourceProvider, DateProvider dateProvider, HibernateProvider hibernateProvider) throws ProviderException
	{
		this.dataSourceProvider = dataSourceProvider;
		this.dateProvider = dateProvider;
		this.hibernateProvider = hibernateProvider;
		
		log.info("Created");
	}		

	public ReportParameterValue[] getParamValues(
		ReportParameter reportParameter,
		Map<String,Object> parameters)
		throws ProviderException
	{
		if (reportParameter.getType().equals(ReportParameter.QUERY_PARAM))
		{
			return getParamValuesFromDataSource(reportParameter, parameters);
		}
		else if (reportParameter.getType().equals(ReportParameter.LIST_PARAM))
		{
			return parseListValues(reportParameter);
		}
		else if (reportParameter.getType().equals(ReportParameter.BOOLEAN_PARAM))
		{
			// default to Yes/No 
			if (reportParameter.getData() == null || reportParameter.getData().indexOf("|") == -1)
			{
				reportParameter.setData("true:Yes|false:No");
			}
			
			return parseListValues(reportParameter);
		}

		throw new ProviderException(
			reportParameter.getName()
				+ ": param-type "
				+ reportParameter.getType()
				+ " not supported!");
	}
	
	protected ReportParameterValue[] parseListValues(ReportParameter reportParameter)
		throws ProviderException
	{
		StringTokenizer st =
			new StringTokenizer(reportParameter.getData(), "|\n\r");

		ReportParameterValue[] values =
			new ReportParameterValue[st.countTokens()];

		int index = 0;
		while (st.hasMoreTokens())
		{			
			String token = st.nextToken();
			String id = token;
			String description = token;
			
			StringTokenizer paramValue = new StringTokenizer(token,":");			
			if (paramValue.countTokens() == 2)
			{
				id = paramValue.nextToken();
				description = paramValue.nextToken();
			}
				
			try
			{
				if (reportParameter.getClassName().equals("java.lang.Integer"))
				{
					values[index] =
						new ReportParameterValue(Integer.valueOf(id), description);
				}
				else if (
					reportParameter.getClassName().equals("java.lang.Double"))
				{
					values[index] =
						new ReportParameterValue(Double.valueOf(id), description);
				}
				else if (
					reportParameter.getClassName().equals("java.lang.Long"))
				{
					values[index] =
						new ReportParameterValue(Long.valueOf(id), description);
				}
				else if (
					reportParameter.getClassName().equals(
						"java.math.BigDecimal"))
				{
					values[index] =
						new ReportParameterValue(new BigDecimal(id), description);
				}
				else
				{
					values[index] = new ReportParameterValue(id, description);
				}
			}
			catch (Exception e)
			{
				throw new ProviderException(
					reportParameter.getData()
						+ " contains invalid "
						+ reportParameter.getClassName());
			}

			index++;
		}

		return values;
	}

	protected ReportParameterValue[] getParamValuesFromDataSource(
		ReportParameter param,
		Map<String,Object> parameters)
		throws ProviderException
	{
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;

		try
		{
			ReportDataSource dataSource = param.getDataSource();
			conn = dataSourceProvider.getConnection(dataSource.getId());			

			if (parameters == null || parameters.isEmpty())
			{
				pStmt = conn.prepareStatement(param.getData());				
			}
			else
			{
				// Use JasperReports Query logic to parse parameters in chart
				// queries

				JRDesignQuery query = new JRDesignQuery();
				query.setText(param.getData());

				// convert parameters to JRDesignParameters so they can be
				// parsed
				Map<String,JRDesignParameter> jrParameters = ORUtil.buildJRDesignParameters(parameters);

				pStmt =
					JRQueryExecuter.getStatement(
						query,
						jrParameters,
						parameters,
						conn);
			}
			
			rs = pStmt.executeQuery();

			ResultSetMetaData rsMetaData = rs.getMetaData();

			boolean multipleColumns = false;
			if (rsMetaData.getColumnCount() > 1)
				multipleColumns = true;

			ArrayList<ReportParameterValue> v = new ArrayList<ReportParameterValue>();

			while (rs.next())
			{
				ReportParameterValue value = new ReportParameterValue();

				if (param.getClassName().equals("java.lang.String"))
				{
					value.setId(rs.getString(1));
				}
				else if (param.getClassName().equals("java.lang.Double"))
				{
					value.setId(new Double(rs.getDouble(1)));
				}
				else if (param.getClassName().equals("java.lang.Integer"))
				{
					value.setId(new Integer(rs.getInt(1)));
				}
				else if (param.getClassName().equals("java.lang.Long"))
				{
					value.setId(new Long(rs.getLong(1)));
				}
				else if (param.getClassName().equals("java.math.BigDecimal"))
				{
					value.setId(rs.getBigDecimal(1));
				}
				else if (param.getClassName().equals("java.util.Date"))
				{
					value.setId(rs.getDate(1));
				}
				else if (param.getClassName().equals("java.sql.Date"))
				{
					value.setId(rs.getDate(1));
				}
				else if (param.getClassName().equals("java.sql.Timestamp"))
				{
					value.setId(rs.getTimestamp(1));
				}

				if (multipleColumns)
				{
					value.setDescription(rs.getString(2));
				}

				v.add(value);
			}

			rs.close();

			ReportParameterValue[] values = new ReportParameterValue[v.size()];
			v.toArray(values);

			return values;
		}
		catch (Exception e)
		{
			throw new ProviderException(
				"Error retreiving param values from database: "
					+ e.getMessage());
		}
		finally
		{
			try
			{
				if (pStmt != null)
					pStmt.close();
				if (conn != null)
					conn.close();
			}
			catch (Exception c)
			{
				log.error("Error closing");
			}
		}
	}

	/**
	 * 
	 * @param parameter
	 * @param value
	 * @throws ProviderException if value cannot be parsed to the type of parameter
	 */
	protected void validateParameter( ReportParameter parameter, String value, GroovyContext groovyContext ) throws ProviderException {
        parseParameter( parameter, value, groovyContext);
    }	    
	

	protected Object parseParameter(ReportParameter parameter, String value, GroovyContext groovyContext)
		throws ProviderException
	{
		try
		{
            if (groovyContext.isScript( value )) {
                return parseGroovyForParameter(parameter, value, groovyContext );
            }
            
            if (parameter.isClass(ReportParameter.STRING))
			{
				return value;
			}
			else if (parameter.isClass(ReportParameter.DOUBLE))
			{
				return new Double(value);
			}
			else if (parameter.isClass( ReportParameter.INTEGER))
			{
				return new Integer(value);
			}
			else if (parameter.isClass( ReportParameter.LONG))
			{
				return new Long(value);
			}
			else if (parameter.isClass( ReportParameter.BIGDECIMAL))
			{
				return new BigDecimal(value);
			}
			else if (parameter.isClass( ReportParameter.DATE))
			{
				return dateProvider.parseDate(value);
			}
			else if (parameter.isClass( ReportParameter.SQL_DATE))
			{
				return new java.sql.Date(dateProvider.parseDate(value).getTime());
			}
			else if (parameter.isClass( ReportParameter.TIMESTAMP))
			{
				long time = dateProvider.parseDate(value).getTime();
				return new Timestamp(time);
			}
			else if (parameter.isClass( ReportParameter.BOOLEAN))
			{
				return new Boolean(value);
			}
		}
		catch (Exception e)
		{
			throw new ProviderException(
				parameter.getDescription() + ", value of [" + value  + " is invalid: " + e.getMessage());
		}

		throw new ProviderException(
			"Parameter " + parameter.getName() + "of class type: " + parameter.getClassName() + "is currently unsupported!");
		
	}

	private Object parseGroovyForParameter( ReportParameter parameter, String groovyScript, GroovyContext groovyContext )
	    throws ProviderException {
	    try {
	        Object groovyResult = groovyContext.eval( groovyScript );
	        if (groovyResult != null) {
	            if (!parameter.getDataType().isInstance( groovyResult ) ) {
	                throw new ProviderException("Scripted parameter value of incorrect type: Parameter: " 
	                        + parameter.getName() + ", Script: " + groovyScript + ", Expected Type: " + parameter.getClassName() + ", Actual Value: " + groovyResult.getClass().getName());
	            }
	        }
	        return groovyResult; 
	    } catch (ScriptException se) {
	        throw new ProviderException("Error Parsing scripted parameter value : parameter: " + parameter.getName() + ": value " + groovyScript ,
	                se);
	    }
    }

    public List<ReportParameter> getReportParameters(Report report, String type)
		throws ProviderException
	{
		List<ReportParameter> parameters = new ArrayList<ReportParameter>();

		List<ReportParameterMap> allParameters = report.getParameters();

		if (allParameters != null)
		{
			Iterator<ReportParameterMap> iterator = allParameters.iterator();

			while (iterator.hasNext())
			{
				ReportParameterMap rpMap = iterator.next();

				if (rpMap.getReportParameter().getType().equals(type))
				{
					parameters.add(rpMap.getReportParameter());
				}
			}
		}

		return parameters;
	}	

	/**
	 * @see ParameterProvider#loadReportParameterValues(List, Map) for explanation
	 */
	public void loadReportParameterValues(
		List<ReportParameterMap> reportParameters,
		Map<String,Object> parameters)
		throws ProviderException
	{
		for (int i = 0; i < reportParameters.size(); i++)
		{
			ReportParameterMap rpMap = reportParameters.get(i);
			ReportParameter rp = rpMap.getReportParameter();

			try
			{
				if (rp.getType().equals(ReportParameter.LIST_PARAM)
						|| rp.getType().equals(ReportParameter.QUERY_PARAM)
						|| rp.getType().equals(ReportParameter.BOOLEAN_PARAM))
				{
					if (rp.getValues() == null) // only load once...
					{
						log.debug("loading parameter values: " + rp.getName());
						rp.setValues(getParamValues(rp, parameters));
					}
				}
			}
			catch (Exception e)
			{
				log.error("Error loading parameter values: " + rp.getName());
				throw new ProviderException(
					"loadReportParameterValues: " + e.getMessage());
			}
		}
	}
	
    public Map<String, Object> getReportParametersMap( List<ReportParameterMap> reportParameterMaps,
            Map<String, Object> origParameters ) throws ProviderException {
        return getReportParametersMap( reportParameterMaps, origParameters, true ); 
    }
	

    /**
     * @param reportParameterMaps the association between parameters and reports.
     * @param origParameters the original parameters. This may be a map of keys to arrays of values (even if only one
     *            value is allowed for the parameter).
     * @param convert If true, the values will be evaluated and converted to its type. Otherwise, it simply pulls the
     *            parameters it needs, but does not evaluate them.
     * @return a map of the parameters in origParameters that are defined in reportParameterMaps, parsed to actual
     *         values, if parseValues is true.
     */
	public Map<String, Object> getReportParametersMap( List<ReportParameterMap> reportParameterMaps,
            Map<String, Object> origParameters, boolean convert ) throws ProviderException {
        Map<String, Object> result = new HashMap<String, Object>();
        if( !convert ) {
            addRequestedParameters( origParameters, reportParameterMaps, result );
        } else {
            GroovyContext groovyContext = new ParameterGroovyContext(new Date());
            for( ReportParameterMap rpMap : reportParameterMaps ) {
                ReportParameter reportParameter = rpMap.getReportParameter();
                String name = reportParameter.getName();
                result.put( name, convertParameter( origParameters, groovyContext, rpMap, reportParameter, name ) );
            }
        }
        addDrillDownParameter( origParameters, result );
        return result;
    }
	
	/**
	 * Converts the original parameters to its type, as defined by the parameter.  Unlike getReportParameterMap,
	 * this will not throw an exception on failure to convert.  Instead, failValue is returned, which can be
	 * tested by a client.
	 *    
	 * @param reportParameterMaps
	 * @param origParameters
	 * @param failValue the value to be used if a parameter cannot be converted. 
	 * @return
	 */
    public Map<String, Object> convertParameters( List<ReportParameterMap> reportParameterMaps,
                Map<String, Object> origParameters, Object failValue, ParameterGroovyContext groovyContext) {
        Map<String, Object> result = new HashMap<String, Object>();
        for( ReportParameterMap rpMap : reportParameterMaps ) {
            ReportParameter reportParameter = rpMap.getReportParameter();
            String name = reportParameter.getName();
            Object convertedParam;
            try {
                convertedParam = convertParameter( origParameters, groovyContext, rpMap, reportParameter, name );
            } catch(ProviderException pe) {
                convertedParam = failValue;
            }
            result.put( name,  convertedParam );
        }
        addDrillDownParameter( origParameters, result );
        
        for(String originalParameterKey : origParameters.keySet()) {
            if (!result.containsKey( originalParameterKey )) {
                result.put( originalParameterKey, origParameters.get( originalParameterKey ) );
            }
        }
        return result;
    }

    /**
     * Converts a parameter, to its native type.
     * 
     * @param origParameters
     * @param groovyContext
     * @param rpMap
     * @param reportParameter
     * @param name
     * @throws ProviderException
     */
    private Object convertParameter( Map<String, Object> origParameters, 
            GroovyContext groovyContext, ReportParameterMap rpMap, ReportParameter reportParameter, String name )
            throws ProviderException {
        if( reportParameter.isMultipleSelect() ) {
            Object[] values = ParameterAwareHelp.getMultiValue( origParameters, name );
            String s = buildMultipleSelectString( reportParameter, values );
            if( rpMap.isRequired() || s.length() > 0 ) {
                return s;
            }
        } else {
            String value = ParameterAwareHelp.getSingleValue( origParameters, name );
            if( value != null ) {
                Object object = parseParameter( reportParameter, value, groovyContext );
                if( rpMap.isRequired() || value.length() > 0 ) {
                    return object;
                }
            }
        }
        return  null;
    }

	/**
     * Always pass drilldown chart parameter to reports if it exists
     * 
     * @param origParameters
     * @param result
     */
    private void addDrillDownParameter( Map<String, Object> origParameters, Map<String, Object> result ) {
        String drillDownParameter = ParameterAwareHelp.getSingleValue( origParameters, ReportChart.DRILLDOWN_PARAMETER );
        if( drillDownParameter != null ) {
            result.put( ReportChart.DRILLDOWN_PARAMETER, drillDownParameter );
        }
    }

	/**
	 * add the parameters in origParameters, identified by requestedParameters, to result
	 * @param origParameters
     * @param requestedParameters a list indicating the parameters that are requested.
	 * 
	 */
	private Map<String, Object> addRequestedParameters( Map<String, Object> sourceParameters, 
	        List<ReportParameterMap> requestedParameters,  Map<String, Object> destParameters) {
        for( ReportParameterMap rpMap : requestedParameters ) {
            ReportParameter reportParameter = rpMap.getReportParameter();
            Object value = sourceParameters.get( reportParameter.getName() );
            if( value != null ) {
                destParameters.put( reportParameter.getName(), value );
            }
        }
        return destParameters;
    }


    private String buildMultipleSelectString(
		ReportParameter reportParameter,
		Object[] values)
	{
		StringBuffer sb = new StringBuffer();
		
		if (values == null || values.length < 1 || values[0].equals("")) return "";

		for (int j = 0; j < values.length; j++)
		{
			String value = "";

			if (values[j] instanceof ReportParameterValue)
			{
				value = ((ReportParameterValue) values[j]).getId().toString();
			}
			else
			{
				value = (String) values[j];
			}

			if (j > 0)
			{
				sb.append(",");
			}
			
			if (reportParameter.getClassName().equals("java.lang.String"))
			{
				sb.append("'" + value + "'");
			}
			else
			{
				sb.append(value);
			}
		}

		return sb.toString();
	}

    
	public boolean validateParameters( List<ReportParameterMap> reportParameters, Map<String, Object> parameters )
            throws ProviderException {

        if( reportParameters != null && reportParameters.size() > 0 ) {
            Iterator<ReportParameterMap> iterator = reportParameters.iterator();

            
            GroovyContext groovyContext = new ParameterGroovyContext(new Date()); 
            
            while( iterator.hasNext() ) {
                ReportParameterMap rpMap = iterator.next();

                ReportParameter param = rpMap.getReportParameter();

                if( !parameters.containsKey( param.getName() ) && rpMap.isRequired() ) {
                    throw new ProviderException( LocalStrings.ERROR_PARAMETER_REQUIRED );
                }

                if( param.getType().equals( ReportParameter.TEXT_PARAM )
                        || param.getType().equals( ReportParameter.DATE_PARAM ) ) {
                    String value = ParameterAwareHelp.getSingleValue( parameters, param.getName() );

                    if( value != null && value.length() > 0 ) {
                        try {
                            validateParameter( param, value, groovyContext );
                        } catch( Exception e ) {
                            throw new ProviderException( e.getMessage() );
                        }
                    } else if( rpMap.isRequired() ) {
                        throw new ProviderException( LocalStrings.ERROR_PARAMETER_REQUIRED );
                    }
                }
            }
        }

        return true;
    }

	public List<ReportParameter> getAvailableParameters(Report report) throws ProviderException
	{
		List<ReportParameter> parameters = getReportParameters();

		Iterator<ReportParameter> iterator = parameters.iterator();
		while (iterator.hasNext())
		{
			ReportParameter rp = iterator.next();

			Iterator<ReportParameterMap> reportIterator = report.getParameters().iterator();
			while (reportIterator.hasNext())
			{
				ReportParameterMap rpMap = reportIterator.next();

				if (rp.getId().equals(rpMap.getReportParameter().getId()))
				{
					parameters.remove(rp);
					iterator = parameters.iterator();
				}
			}
		}

		return parameters;
	}	

	public ReportParameter getReportParameter(Integer id)
		throws ProviderException
	{
		return (ReportParameter) hibernateProvider.load(ReportParameter.class, id);
	}
	
	public ReportParameter getReportParameter(String name) throws ProviderException
	{
		Session session = null;
		
		try
		{
			session = hibernateProvider.openSession();
			
			Criteria criteria = session.createCriteria(ReportParameter.class);
			criteria.add(Restrictions.eq("name", name));
			
			return (ReportParameter) criteria.uniqueResult();
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
		finally
		{
			hibernateProvider.closeSession(session);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ReportParameter> getReportParameters() throws ProviderException
	{
		String fromClause =
			"from org.efs.openreports.objects.ReportParameter reportParameter order by reportParameter.name ";
		
		return (List<ReportParameter>) hibernateProvider.query(fromClause);
	}
	
	public ReportParameter insertReportParameter(ReportParameter reportParameter, String insertUser)
		throws ProviderException
	{
		return (ReportParameter) hibernateProvider.saveCtrlDataObject( reportParameter, insertUser );
	}
	
	public void updateReportParameter(ReportParameter reportParameter, String updateUserName)
		throws ProviderException
	{
		hibernateProvider.updateCtrlDataObject( reportParameter, updateUserName );
	}
	
	public void deleteReportParameter(ReportParameter reportParameter)
		throws ProviderException
	{
		try
		{
			hibernateProvider.delete(reportParameter);
		}
		catch (ConstraintException ce)
		{
			throw new ProviderException(LocalStrings.ERROR_PARAMETER_DELETION);
		}
	}

	public void setDataSourceProvider(DataSourceProvider dataSourceProvider)
	{
		this.dataSourceProvider = dataSourceProvider;
	}

	public void setDateProvider(DateProvider dateProvider)
	{
		this.dateProvider = dateProvider;
	}

}