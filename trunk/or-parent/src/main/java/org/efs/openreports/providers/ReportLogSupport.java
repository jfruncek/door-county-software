package org.efs.openreports.providers;

import java.util.Date;
import java.util.List;

import org.efs.openreports.objects.ReportLog;
import org.apache.log4j.Logger;

public class ReportLogSupport implements ReportLogProvider {
    ReportLogProvider reportLogProvider;

    public void deleteReportLog( ReportLog reportLog ) throws ProviderException {
        reportLogProvider.deleteReportLog( reportLog );
    }

    public ReportLog getReportLog( Integer id ) throws ProviderException {
        return reportLogProvider.getReportLog( id );
    }

    public List<ReportLog> getReportLogs( String status, Integer userId, Integer reportId, Integer alertId, int maxRows )
            throws ProviderException {
        return reportLogProvider.getReportLogs( status, userId, reportId, alertId, maxRows );
    }

    public List<Object[]> getTopAlerts() throws ProviderException {
        return reportLogProvider.getTopAlerts();
    }

    public List<Object[]> getTopAlertsByUser() throws ProviderException {
        return reportLogProvider.getTopAlertsByUser();
    }

    public List<Object[]> getTopEmptyReports() throws ProviderException {
        return reportLogProvider.getTopEmptyReports();
    }

    public List<Object[]> getTopFailures() throws ProviderException {
        return reportLogProvider.getTopFailures();
    }

    public List<Object[]> getTopNotTriggeredAlerts() throws ProviderException {
        return reportLogProvider.getTopNotTriggeredAlerts();
    }

    public List<Object[]> getTopReports() throws ProviderException {
        return reportLogProvider.getTopReports();
    }

    public List<Object[]> getTopReportsByUser() throws ProviderException {
        return reportLogProvider.getTopReportsByUser();
    }

    public List<Object[]> getTopReportsForPeriod( int daysBack ) throws ProviderException {
        return reportLogProvider.getTopReportsForPeriod( daysBack );
    }

    public List<Object[]> getTopTriggeredAlerts() throws ProviderException {
        return reportLogProvider.getTopTriggeredAlerts();
    }

    public ReportLog insertReportLog( ReportLog reportLog ) throws ProviderException {
        return reportLogProvider.insertReportLog( reportLog );
    }

    public void updateReportLog( ReportLog reportLog ) throws ProviderException {
        reportLogProvider.updateReportLog( reportLog );
    }

    public ReportLogSupport(ReportLogProvider reportLogProvider) {
        this.reportLogProvider = reportLogProvider;
    }
    
    public ReportLogProvider getProvider() {
        return reportLogProvider;
    }
    
    public void logSuccess( ReportLog reportLog ) throws ProviderException {
        reportLog.setEndTime( new Date() );
        reportLog.setStatus( ReportLog.STATUS_SUCCESS );
        reportLogProvider.updateReportLog( reportLog );
    }

    
    /**
     * Logs a failure on reportLog if it is not null, does not throw an exception. 
     * @param reportLog
     */
    public void safeLogFailure( ReportLog reportLog, String message, Logger exceptionLog ) {
        if (reportLog != null && reportLog.getId() != null)
        {
            try {
                reportLog.setEndTime( new Date() );
                reportLog.setStatus( ReportLog.STATUS_FAILURE );
                reportLog.setMessage( message );
                reportLogProvider.updateReportLog( reportLog );
            } catch( Exception ex ) {
                exceptionLog.error( "Unable to create ReportLog: "  + ex.getMessage() );
            }
        }
    }
    
}
