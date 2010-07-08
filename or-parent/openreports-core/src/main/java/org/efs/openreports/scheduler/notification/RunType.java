package org.efs.openreports.scheduler.notification;

import java.util.HashMap;
import java.util.Map;

/**
 * A type of run in a RunStatus.
 * 
 * @author mconner
 */
public enum RunType {
    BATCH( "BT", "Batch" ), BOOK( "BK", "Book" ), REPORT( "RP", "Report" );

    private static Map<String, RunType> codeMap = new HashMap<String, RunType>();
    static {
        put( BOOK );
        put( BATCH );
        put( REPORT );
    }

    private String code;
    private String label;

    RunType( String code, String label ) {
        this.code = code;
        this.label = label;
    }
    
    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static RunType getForCode( String code ) {
        return codeMap.get( code );
    }

    private static void put( RunType type ) {
        codeMap.put( type.getCode(), type );
    }

}
