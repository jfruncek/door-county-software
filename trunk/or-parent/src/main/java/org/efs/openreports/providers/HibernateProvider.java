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

package org.efs.openreports.providers;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.efs.openreports.util.ConstraintException;
import org.efs.openreports.util.CtrlDataObject;
import org.efs.openreports.util.LocalStrings;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.type.DbTimestampType;
import org.springframework.beans.factory.DisposableBean;

public class HibernateProvider implements DisposableBean
{
	protected static Logger log = Logger.getLogger(HibernateProvider.class.getName());

	private static SessionFactory sessionFactory;

	public HibernateProvider() throws ProviderException
	{
		log.info("SessionFactory Created.");
	}

	Timestamp getTimestamp(Session session) {
        org.hibernate.type.DbTimestampType type = new DbTimestampType();
        Timestamp ts = (Timestamp) type.seed( (SessionImplementor) session );
        return ts;
    }

    public Date getDate() throws ProviderException {
        Session session = openSession();
        try {
            return new Date(getTimestamp(session).getTime());
        } catch( HibernateException he ) {
            throw new ProviderException( he );
        } finally {
            closeSession( session );
        }
    }




	public Session openSession() throws ProviderException
	{
		try
		{
			return sessionFactory.openSession();
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
	}

	public void closeSession(Session session) throws ProviderException
	{
		try
		{
			if (session != null)
				session.close();
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
	}

	public void rollbackTransaction(Transaction tx)
		throws ProviderException
	{
		try
		{
			if (tx != null)
				tx.rollback();
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
	}

	public Object save(Object object) throws ProviderException
	{
		Session session = openSession();
		Transaction tx = null;

		try
		{
			tx = session.beginTransaction();
			session.save(object);
			tx.commit();
		}
		catch (HibernateException he)
		{
			rollbackTransaction(tx);

            if (he instanceof JDBCException)
            {
                throw new ProviderException(((JDBCException)he).getSQLException());
            }

			throw new ProviderException(he);
		}
		finally
		{
			closeSession(session);
		}

		return object;
	}

    public Object saveCtrlDataObject( CtrlDataObject object, String updateUser ) throws ProviderException {
        CtrlData oldValues = new CtrlData( object );

        try {
            object.setCtrlDate( getDate() );
            object.setCtrlUser( updateUser );
            return save( object );
        } catch( ProviderException pe ) {
            oldValues.apply( object );
            throw pe;
        } catch( RuntimeException re ) {
            oldValues.apply( object );
            throw re;
        }
    }

	public void update( Object object ) throws ProviderException {
        Session session = openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.update( object );
            tx.commit();
        } catch( HibernateException he ) {
            rollbackTransaction( tx );

            if( he instanceof ConstraintViolationException ) {
                throw new ProviderException( LocalStrings.ERROR_UNIQUE_CONSTRAINT );
            }

            throw new ProviderException( he );
        } finally {
            closeSession( session );
        }
    }

    public void updateCtrlDataObject( CtrlDataObject object, String updateUser ) throws ProviderException {
        CtrlData oldValues = new CtrlData( object );
        try {
            object.setCtrlDate( getDate() );
            object.setCtrlUser( updateUser );
            update( object );
        } catch( ProviderException pe ) {
            oldValues.apply( object );
            throw pe;
        } catch( RuntimeException re ) {
            oldValues.apply( object );
            throw re;
        }
    }

	public void delete(Object object)
		throws ProviderException, ConstraintException
	{
		Session session = openSession();
		Transaction tx = null;

		try
		{
			tx = session.beginTransaction();
			session.delete(object);
			tx.commit();
		}
		catch (HibernateException he)
		{
			rollbackTransaction(tx);

			if (he instanceof ConstraintViolationException)
			{
				throw new ConstraintException(he.getMessage());
			}

			throw new ProviderException(he);
		}
		finally
		{
			closeSession(session);
		}
	}

	public Object load(Class<?> classType, Serializable id)
		throws ProviderException
	{
		Session session = openSession();

		try
		{
			Object object = session.load(classType, id);
			return object;
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
		finally
		{
			closeSession(session);
		}
	}

	public List<?> query(String fromClause) throws ProviderException
	{
		Session session = openSession();

		try
		{
			Query query = session.createQuery(fromClause);
			query.setCacheable(true);

			List<?> list = query.list();

			return list;
		}
		catch (HibernateException he)
		{
			throw new ProviderException(he);
		}
		finally
		{
			closeSession(session);
		}
	}

	public void destroy()
	{
		try
		{
// EC-11368 Spring appears to do this properly, so this line is redundant	sessionFactory.close();

			log.info("SessionFactory closed.");
		}
		catch(HibernateException he)
		{
			log.warn(he.toString());
		}
	}

	public void setSessionFactory(SessionFactory sessionFactory)
	{
		HibernateProvider.sessionFactory = sessionFactory;
	}
}