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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import java.util.*;

import org.efs.openreports.util.CtrlDataObject;

public class ReportParameter implements Serializable, CtrlDataObject
{
	private static final long serialVersionUID = 667082979233371385l;
	
	public static final String TYPE_TEXT = "Text";
    public static final String TYPE_DATE = "Date";
    public static final String TYPE_LIST = "List";
    public static final String TYPE_QUERY = "Query";
    public static final String TYPE_SUBREPORT = "SubReport";
    public static final String TYPE_BOOLEAN = "Boolean";
    public static String[] TYPES =
            new String[] { TYPE_DATE, TYPE_LIST, TYPE_QUERY, TYPE_TEXT, TYPE_SUBREPORT, TYPE_BOOLEAN };

	public static final String STRING = "java.lang.String";
	public static final String DOUBLE = "java.lang.Double";
	public static final String INTEGER = "java.lang.Integer";
	public static final String LONG = "java.lang.Long";
	public static final String BIGDECIMAL = "java.math.BigDecimal";
	public static final String DATE = "java.util.Date";
	public static final String SQL_DATE = "java.sql.Date";
	public static final String TIMESTAMP = "java.sql.Timestamp";	
	public static final String BOOLEAN = "java.lang.Boolean";
	
	public static String[] CLASS_NAMES =
		new String[] {
			STRING,
			DOUBLE,
			INTEGER,
			LONG,
			BIGDECIMAL,
			DATE,
			SQL_DATE,
			TIMESTAMP,
			BOOLEAN};
	
	/**
     * Map of class names to classes. This is silliness, but this class started out with names, rather than classes, for
     * some reason. TODO: clean this up to use the classes
     */
	public static Map<String, Class<?>> dataTypeMap = initDataTypeMap();
	
	private static Map<String, Class<?>>  initDataTypeMap() {
	    Map <String, Class<?>> result = new HashMap<String, Class<?>>();
	    result.put( STRING, String.class );
        result.put( DOUBLE, Double.class );
        result.put( INTEGER, Integer.class );
        result.put( LONG, Long.class );
        result.put( BIGDECIMAL, BigDecimal.class );
        result.put( DATE, java.util.Date.class );
        result.put( SQL_DATE, java.sql.Date.class );
        result.put( TIMESTAMP, java.sql.Timestamp.class );
        result.put( BOOLEAN, Boolean.class );
	    return result;
	}
	

	public static final String QUERY_PARAM = "Query";
	public static final String LIST_PARAM = "List";
	public static final String TEXT_PARAM = "Text";
	public static final String DATE_PARAM = "Date";
	public static final String SUBREPORT_PARAM = "SubReport";
	public static final String BOOLEAN_PARAM = "Boolean";
	
	private Integer id;
	private String ctrlUser;
    private Date ctrlDate;
	private String name;
	private String type;
	private String className;
	private ReportDataSource dataSource;
	private String data;
	private ReportParameterValue[] values;
	private String description;
	private boolean required;
	private boolean multipleSelect;
	private String defaultValue;

	public ReportDataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(ReportDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public ReportParameter()
	{
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public String getClassName()
	{
		return className;
	}
	
	/**
	 * @param name one of values in {@link #CLASS_NAMES}
	 * @return true if this ReportParameter's class name matches name
	 */
	public boolean isClass(String name) {
	    return getClassName().equals(name);
	}

	public String getData()
	{
		return data;
	}

	public ReportParameterValue[] getValues()
	{
		return values;
	}
	
	public String getCtrlUser()
    {
        return ctrlUser;
    }
	
	public void setCtrlUser(String ctrlUser)
    {
        this.ctrlUser = ctrlUser;
    }
    
    public Date getCtrlDate()
    {
        return ctrlDate;
    } 
    
    public void setCtrlDate(Date ctrlDate)
    {
        this.ctrlDate = ctrlDate;
    }

	public void setValues(ReportParameterValue[] values)
	{
		this.values = values;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isRequired()
	{
		return required;
	}

	public void setRequired(boolean required)
	{
		this.required = required;
	}	
	
	public boolean isMultipleSelect()
	{
		return multipleSelect;
	}

	public void setMultipleSelect(boolean multiple)
	{
		this.multipleSelect = multiple;
	}

	public String getDefaultValue() 
	{
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) 
	{
		this.defaultValue = defaultValue;
	}
	
	public boolean isSubreportParam() {
	    return getType().equals(SUBREPORT_PARAM);
	}

    public Class<?> getDataType() {
        return dataTypeMap.get( getClassName() );
        
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ReportParameter other = (ReportParameter) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

    
}