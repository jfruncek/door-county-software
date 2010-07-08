package org.efs.openreports.engine.sqlsupport;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConnectionWrapper implements Connection {
    protected static final Logger LOG = Logger.getLogger( ConnectionWrapper.class );
    Connection delegate;
    static int connectionCount = 0;
    int connectionNumber;
    private List<PreparedStatementWrapper> preparedStatementWrappers = new ArrayList<PreparedStatementWrapper>();
    private List<StatementWrapper> statementWrappers = new ArrayList<StatementWrapper>();

    protected void log( String message ) {
        LOG.info( "ConnectionWrapper: " + getConnectionNumber() + ": " + message );
    }

    public ConnectionWrapper( Connection delegate ) {
        this.delegate = delegate;
        this.connectionNumber = ++connectionCount;
        log( "constructing" );
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public void close() throws SQLException {
        log( "close(): closing..." );
        delegate.close();
        log( "close(): closed." );
    }

    public void commit() throws SQLException {
        delegate.commit();
    }

    public Array createArrayOf( String typeName, Object[] elements ) throws SQLException {
        return delegate.createArrayOf( typeName, elements );
    }

    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    public Statement createStatement() throws SQLException {
        return new StatementWrapper( this, delegate.createStatement() );
    }

    public Statement createStatement( int resultSetType, int resultSetConcurrency, int resultSetHoldability )
            throws SQLException {
        return new StatementWrapper( this, delegate.createStatement( resultSetType, resultSetConcurrency,
                resultSetHoldability ) );
    }

    public Statement createStatement( int resultSetType, int resultSetConcurrency ) throws SQLException {
        return new StatementWrapper( this, delegate.createStatement( resultSetType, resultSetConcurrency ) );
    }

    public Struct createStruct( String typeName, Object[] attributes ) throws SQLException {
        return delegate.createStruct( typeName, attributes );
    }

    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    public String getClientInfo( String name ) throws SQLException {
        return delegate.getClientInfo( name );
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    public boolean isValid( int timeout ) throws SQLException {
        return delegate.isValid( timeout );
    }

    public boolean isWrapperFor( Class<?> iface ) throws SQLException {
        return delegate.isWrapperFor( iface );
    }

    public String nativeSQL( String sql ) throws SQLException {
        return delegate.nativeSQL( sql );
    }

    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability ) throws SQLException {
        return delegate.prepareCall( sql, resultSetType, resultSetConcurrency, resultSetHoldability );
    }

    public CallableStatement prepareCall( String sql, int resultSetType, int resultSetConcurrency ) throws SQLException {
        return delegate.prepareCall( sql, resultSetType, resultSetConcurrency );
    }

    public CallableStatement prepareCall( String sql ) throws SQLException {
        return delegate.prepareCall( sql );
    }

    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability ) throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql, resultSetType,
                resultSetConcurrency, resultSetHoldability ) );
    }

    public PreparedStatement prepareStatement( String sql, int resultSetType, int resultSetConcurrency )
            throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql, resultSetType,
                resultSetConcurrency ) );
    }

    public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys ) throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql, autoGeneratedKeys ) );
    }

    public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql, columnIndexes ) );
    }

    public PreparedStatement prepareStatement( String sql, String[] columnNames ) throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql, columnNames ) );
    }

    public PreparedStatement prepareStatement( String sql ) throws SQLException {
        return new PreparedStatementWrapper( this, sql, delegate.prepareStatement( sql ) );
    }

    public void releaseSavepoint( Savepoint savepoint ) throws SQLException {
        delegate.releaseSavepoint( savepoint );
    }

    public void rollback() throws SQLException {
        delegate.rollback();
    }

    public void rollback( Savepoint savepoint ) throws SQLException {
        delegate.rollback( savepoint );
    }

    public void setAutoCommit( boolean autoCommit ) throws SQLException {
        delegate.setAutoCommit( autoCommit );
    }

    public void setCatalog( String catalog ) throws SQLException {
        delegate.setCatalog( catalog );
    }

    public void setClientInfo( Properties properties ) throws SQLClientInfoException {
        delegate.setClientInfo( properties );
    }

    public void setClientInfo( String name, String value ) throws SQLClientInfoException {
        delegate.setClientInfo( name, value );
    }

    public void setHoldability( int holdability ) throws SQLException {
        delegate.setHoldability( holdability );
    }

    public void setReadOnly( boolean readOnly ) throws SQLException {
        delegate.setReadOnly( readOnly );
    }

    public Savepoint setSavepoint() throws SQLException {
        return delegate.setSavepoint();
    }

    public Savepoint setSavepoint( String name ) throws SQLException {
        return delegate.setSavepoint( name );
    }

    public void setTransactionIsolation( int level ) throws SQLException {
        delegate.setTransactionIsolation( level );
    }

    public void setTypeMap( Map<String, Class<?>> map ) throws SQLException {
        delegate.setTypeMap( map );
    }

    public <T> T unwrap( Class<T> iface ) throws SQLException {
        return delegate.unwrap( iface );
    }

    public int getConnectionNumber() {
        return connectionNumber;
    }

    public void add( PreparedStatementWrapper preparedStatementWrapper ) {
        preparedStatementWrappers.add( preparedStatementWrapper );
    }

    public void add( StatementWrapper statementWrapper ) {
        statementWrappers.add( statementWrapper );
    }

}
