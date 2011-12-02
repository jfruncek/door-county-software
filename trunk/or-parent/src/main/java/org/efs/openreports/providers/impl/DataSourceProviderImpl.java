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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.providers.DataSourceProvider;
import org.efs.openreports.providers.HibernateProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ConstraintException;
import org.efs.openreports.util.LocalStrings;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class DataSourceProviderImpl implements DataSourceProvider
{
	protected static Logger log = Logger.getLogger(DataSourceProviderImpl.class);

	private Hashtable<Integer,ReportDataSource> dataSources = new Hashtable<Integer,ReportDataSource>();
	
	private HibernateProvider hibernateProvider;
	
	public DataSourceProviderImpl(HibernateProvider hibernateProvider) throws ProviderException
	{

		this.hibernateProvider = hibernateProvider;
		loadDataSources();

		log.info("DataSourceProviderImpl");
	}

	protected void loadDataSources() throws ProviderException
	{
		Iterator<ReportDataSource> iterator =	getDataSources().iterator();

		while (iterator.hasNext())
		{
			ReportDataSource dataSource = iterator.next();
			dataSources.put(dataSource.getId(), dataSource);
		}
	}
    private Connection establishConnection() {
        Connection connection = null;
        try {
        	String DATASOURCE_CONTEXT="java:evercoreDS";
			InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup(DATASOURCE_CONTEXT);
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        }
        catch (NamingException e) {
            e.printStackTrace();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

	public Connection getConnection(Integer id) throws ProviderException
	{
		ReportDataSource dataSource = dataSources.get(id);

		try
		{
			if (dataSource.isJndi())
			{
				String DATASOURCE_CONTEXT="java:evercoreDS";

				Context initCtx = new InitialContext();

				//DataSource jndiDataSource =
				//	(DataSource) initCtx.lookup(dataSource.getUrl());
				DataSource jndiDataSource = (DataSource) initCtx.lookup(DATASOURCE_CONTEXT);

				return jndiDataSource.getConnection();
			}
			else
			{
				//return dataSource.getConnection();
				return establishConnection();
			}
		}
		catch (Exception e)
		{
			log.error(e.toString());
			throw new ProviderException(e.getMessage());
		}
	}

	public boolean isValidDataSource(Integer id)
	{
		if (dataSources.containsKey(id))
			return true;

		return false;
	}

	public ReportDataSource getDataSource(Integer id) throws ProviderException
	{
		return (ReportDataSource) hibernateProvider.load(ReportDataSource.class, id);
	}
	
	public ReportDataSource getDataSource(String name) throws ProviderException
	{
		Session session = null;
		
		try
		{
			session = hibernateProvider.openSession();
			
			Criteria criteria = session.createCriteria(ReportDataSource.class);
			criteria.add(Restrictions.eq("name", name));
			
			return (ReportDataSource) criteria.uniqueResult();
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
	public List<ReportDataSource> getDataSources() throws ProviderException
	{
		String fromClause =
			"from org.efs.openreports.objects.ReportDataSource reportDataSource order by reportDataSource.name ";
		
		return (List<ReportDataSource>) hibernateProvider.query(fromClause);
	}

	public ReportDataSource insertDataSource(ReportDataSource dataSource)
		throws ProviderException
	{
		testDataSource(dataSource);

		dataSource = (ReportDataSource) hibernateProvider.save(dataSource);
		dataSources.put(dataSource.getId(), dataSource);
		
		return dataSource;
	}

	public void updateDataSource(ReportDataSource dataSource)
		throws ProviderException
	{
		testDataSource(dataSource);

		hibernateProvider.update(dataSource);
		dataSources.put(dataSource.getId(), dataSource);		
	}

	public void deleteDataSource(ReportDataSource dataSource)
		throws ProviderException
	{
		try
		{
			hibernateProvider.delete(dataSource);
			dataSources.remove(dataSource.getId());
		}
		catch (ConstraintException ce)
		{
			throw new ProviderException(LocalStrings.ERROR_DATASOURCE_DELETION);
		}
	}	

	public void testDataSource(ReportDataSource dataSource)
		throws ProviderException
	{
		Connection conn = null;

		try
		{
			if (dataSource.isJndi())
			{
				Context initCtx = new InitialContext();			
				
				DataSource jndiDataSource =
					(DataSource) initCtx.lookup(dataSource.getUrl());
				
				conn = jndiDataSource.getConnection();				
			}
			else
			{
				log.debug("Name:"+dataSource.getName());
				log.debug("Driver Class:"+dataSource.getDriverClassName());
				log.debug("URL:"+dataSource.getUrl());
				log.debug("UserName:"+dataSource.getUsername());
				log.debug("Password:"+dataSource.getPassword());
				log.debug("Max Idle:"+dataSource.getMaxIdle());
				log.debug("Max Active:"+dataSource.getMaxActive());
				log.debug("JNDI:"+dataSource.isJndi());
				log.debug("Max Wait:"+dataSource.getMaxWait());


				conn = dataSource.getConnection();
				log.debug("After Connection");
			}
			log.debug("Connection successful - "+conn);
		}
		catch (Exception e)
		{
			throw new ProviderException(LocalStrings.ERROR_TESTING_CONNECTION
					+ ": " + e.getMessage());
		}
		finally
		{
			try
			{
				if (conn != null)
					conn.close();
			}
			catch (Exception e)
			{
				log.error(e.toString());
			}
		}
	}

}