package org.efs.openreports.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.efs.openreports.engine.sqlsupport.ConnectionWrapper;

/**
 * A work-around for a problem with eSpreadsheet not properly handling JNDI connections. There's a bug in the
 * eSpreadsheet code where it doesn't handle JNDI connections properly. This is a simple driver which treats the url as
 * the JNDI name. the full url is: PROTOCOL + <JNDI name>
 * 
 * For example: jdbc:starkjndi:java:OracleReportDS.
 * 
 * @author mconner
 */
public class JNDIWrapperDriver implements java.sql.Driver {

    public static final String PROTOCOL = "jdbc:starkjndi";

    protected static final Logger LOG = Logger.getLogger( JNDIWrapperDriver.class );

    static {
        // Register self on load
        register();
    }

    @Override
    public boolean acceptsURL( String url ) throws SQLException {
        if( url == null )
            return false;
        return( url.startsWith( PROTOCOL ) );
    }

    /**
     * @param url this must be the JNDI name.
     * @param info unused.
     */
    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
        if (!url.startsWith( PROTOCOL + ":" )) {
            throw new SQLException("invalid protocol: " + url + " expecting: " + PROTOCOL + ":...");
        }
        String jndiName = url.substring( PROTOCOL.length() + 1 ); // strip off protocol
        try {
            Context initCtx = new InitialContext();
            DataSource jndiDataSource = (DataSource) initCtx.lookup( jndiName );
            
            Connection connection = jndiDataSource.getConnection();
            //return connection;
            return new ConnectionWrapper(connection);
        } catch( Exception ex ) {
            throw new SQLException( "Cannot get connection: " + ex.getMessage(), ex );
        }
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
        return new DriverPropertyInfo[] {};
    }

    @Override
    public boolean jdbcCompliant() {
        return true; // We assume the DataSource is complies to JDBC.
    }

    private static void register() {
        try {
            DriverManager.registerDriver( new JNDIWrapperDriver() );
        } catch( SQLException e ) {
            LOG.info( "cant register JDBC Driver" + JNDIWrapperDriver.class.getName(), e );
        }
    }

}
