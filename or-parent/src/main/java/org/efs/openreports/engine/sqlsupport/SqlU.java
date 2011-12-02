package org.efs.openreports.engine.sqlsupport;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.sql.CLOB;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.querycache.OrderedQueryResults;

/**
 * Utility for common SQL operations.
 * 
 * @author mconner
 */
public class SqlU {
    private static final Logger log = Logger.getLogger( OrderedQueryResults.class );
    public static final SqlU I = new SqlU();

    public void safeClose( ResultSet resultSet, String logIdentifier, Logger log ) {
        if( resultSet != null ) {
            try {
                if( !resultSet.isClosed() ) {
                    resultSet.close();
                }
            } catch( SQLException se ) {
                log.error( "Error closing ResultSet, type:" + resultSet.getClass().getName() + ", " + logIdentifier
                        + ": ", se );
            }
        }
    }

    public void safeClose( Statement statement, String logIdentifier, Logger log ) {
        if( statement != null ) {
            try {
                if( !statement.isClosed() ) {
                    statement.close();
                }
            } catch( SQLException se ) {
                log.error( "Error closing " + statement.getClass().getName() + ", " + logIdentifier + ": ", se );
            }
        }
    }

    public void safeClose( Connection connection, String logIdentifier, Logger log ) {
        if( connection != null ) {
            try {
                if( !connection.isClosed() ) {
                    connection.close();
                }
            } catch( SQLException se ) {
                log.error( "Error closing " + connection.getClass().getName() + ", " + logIdentifier + ": ", se );
            }
        }
    }

    /**
     * Introspect the metadata associated with a ResultSet to build a DynaClass.
     * 
     * @param resultSet The <code>resultSet</code> whose metadata is to be introspected
     * @throws SQLException if an error is encountered processing the result set metadata
     */
    public DynaClass buildDynaClassFromMetaData( ResultSet resultSet ) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        DynaProperty[] properties = new DynaProperty[metadata.getColumnCount()];
        for( int i = 0; i < properties.length; i++ ) {
            properties[i] = createDynaProperty( metadata, i + 1 );
        }
        BasicDynaClass basicDynaClass = new BasicDynaClass( null, null, properties );
        return basicDynaClass;
    }

    /**
     * <p>
     * Factory method to create a new DynaProperty for the given index into the result set metadata.
     * </p>
     * 
     * @param metadata is the result set metadata
     * @param column is the column index in the metadata
     * @return the newly created DynaProperty instance
     */
    protected DynaProperty createDynaProperty( ResultSetMetaData metadata, int column ) throws SQLException {
        String name = metadata.getColumnName( column );
        name = name.toLowerCase(); // standardize the name

        String className = null;
        try {
            className = metadata.getColumnClassName( column );
        } catch( SQLException e ) {
            // this is a patch for HsqlDb to ignore exceptions
            // thrown by its metadata implementation
        }

        // Default to Object type if no class name could be retrieved
        // from the metadata
        Class<?> clazz = Object.class;
        if( CLOB.class.getName().equals( className ) ) {
            clazz = String.class;
        } else if( className != null ) {
            clazz = loadClass( className );
        }
        return new DynaProperty( name, clazz );
    }

    /**
     * <p>
     * Loads and returns the <code>Class</code> of the given name. By default, a load from the
     * thread context class loader is attempted. If there is no such class loader, the class loader
     * used to load this class will be utilized.
     * </p>
     * 
     * @throws SQLException if an exception was thrown trying to load the specified class
     */
    protected Class<?> loadClass( String className ) throws SQLException {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if( cl == null ) {
                cl = this.getClass().getClassLoader();
            }
            return cl.loadClass( className );
        } catch( Exception e ) {
            throw new SQLException( "Cannot load column class '" + className + "': " + e );
        }

    }
    
    public Serializable[] createRow( ResultSet resultSet, DynaClass dynaClass ) throws SQLException {
        DynaProperty[] properties = dynaClass.getDynaProperties();
        Serializable[] row = new Serializable[properties.length];
        // We are assuming the properties are in the same order as the columns in the result set
        // Safe since the properties were created from the result set.
        for( int i = 0; i < properties.length; i++ ) {
            Object object = resultSet.getObject( i + 1 );
            if( object instanceof CLOB ) {
                CLOB clob = (CLOB) object;
                row[i] = clob.getSubString( 1, ( (int) clob.length() ) );
            } else if( object == null || object instanceof Serializable ) {
                row[i] = (Serializable) object;
            } else {
                log.error( "Object not serializable from ResultSet for property: " + properties[i].getName()
                        + "was class: " + object.getClass().getName() );
            }
        }
        return row;
    }


}
