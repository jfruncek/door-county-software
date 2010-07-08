package org.efs.openreports.engine.javareport;

import java.sql.Connection;
import java.sql.SQLException;

import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.providers.DataSourceProvider;
import org.efs.openreports.providers.ProviderException;

import com.starkinvestments.rpt.common.ConnectionSource;

public class ConnectionSourceImpl implements ConnectionSource {
    DataSourceProvider dataSourceProvider;

    public ConnectionSourceImpl( DataSourceProvider dataSourceProvider ) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Connection getConnection( String name ) throws SQLException {
        ReportDataSource reportDataSource = getDataSourceWithName( name );

        if( reportDataSource == null ) {
            return null;
        }
        Connection connection;
        try {
            connection = dataSourceProvider.getConnection(reportDataSource.getId());
        } catch( ProviderException e ) {
            throw new SQLException("cant get a connection", e);
        }
        return connection;
    }

    private ReportDataSource getDataSourceWithName( String name ) {
        try {
            for(ReportDataSource reportDataSource: dataSourceProvider.getDataSources()) {
                if (reportDataSource.getName().equals( name )) {
                    return reportDataSource;
                }
            }
        } catch( ProviderException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
