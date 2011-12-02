package org.efs.openreports.engine;

/**
 * Thrown when the BatchReportProcessor finds an error in the output of the report it is processing,
 * (i.e.: a missing property in the output.)
 * 
 * @author mconner
 */
public class BatchReportConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BatchReportConfigException( String message ) {
        super( message );
    }

    public BatchReportConfigException( String message, Throwable cause ) {
        super( message, cause );
    }

}
