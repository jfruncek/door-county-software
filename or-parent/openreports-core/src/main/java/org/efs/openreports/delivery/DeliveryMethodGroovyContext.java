package org.efs.openreports.delivery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;

import org.efs.openreports.util.scripting.DateSupport;
import org.efs.openreports.util.scripting.GroovyContext;

public class DeliveryMethodGroovyContext extends GroovyContext {
    public static final String  RUN_DATE = "runDate";
    public static final String  RUN_DATE_TIME = "runDateTime";
    public static final String  REPORT_NAME = "reportName";
    public static final String  SCHEDULE_NAME = "scheduleName";
    public static final Set<String> SYSTEM_VALUES = initSystemValues();
    
    
    protected static Set<String> initSystemValues() {
        Set<String> results = new HashSet<String>();
        results.add( RUN_DATE );
        results.add( RUN_DATE_TIME );
        results.add( REPORT_NAME );
        results.add( SCHEDULE_NAME );
        return results;
    };
    
    public static boolean isSystemValue(String valueName) {
        return SYSTEM_VALUES.contains( valueName );
    }
    
    
    private Map<String, Object> params;
    private String reportName;
    private Date runDateTime;
    private String scheduleName;

    public DeliveryMethodGroovyContext( Map<String, Object> params, String reportName, String scheduleName, Date runDateTime ) {
        this.params = params;
        this.reportName = reportName;
        this.runDateTime = runDateTime;
        this.scheduleName = scheduleName;
    }

    @Override
    protected void initEngine( ScriptEngine engine ) {
        super.initEngine( engine );
        
        Date runDate = DateSupport.trunc( runDateTime );
        engine.put("runDate", runDate);
        engine.put("runDateTime", runDateTime);
        engine.put("reportName", reportName);
        engine.put("scheduleName", scheduleName);
        for(String paramName : params.keySet()) {
            Object value = params.get(paramName);
            engine.put( paramName, value );
        }
    }
    
    String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format( date );
    }

}
