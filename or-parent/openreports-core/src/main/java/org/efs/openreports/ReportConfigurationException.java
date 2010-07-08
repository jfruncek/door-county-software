package org.efs.openreports;

public class ReportConfigurationException extends ORException {
    /** */
    private static final long serialVersionUID = 1L;

    public ReportConfigurationException( String message ) {
        super( message );
    }

    public ReportConfigurationException( String message, Exception cause ) {
        super( message, cause );
    }

}
