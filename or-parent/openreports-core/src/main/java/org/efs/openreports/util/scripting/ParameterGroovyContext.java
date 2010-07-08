package org.efs.openreports.util.scripting;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.script.ScriptEngine;

/**
 * Provides some context when scripting parameter values. Mostly, this is for access to the run date of the report.
 * 
 * @author mconner
 */
public class ParameterGroovyContext extends GroovyContext {
    
    Date runDateTime;

    
    public ParameterGroovyContext(Date runDateTime) {
        this.runDateTime = runDateTime;
    }

    @Override
    protected void initEngine( ScriptEngine engine ) {
        super.initEngine( engine );
        
        SimpleDateFormat standardDateFormat = DateSupport.getStandardDateFormat();
        SimpleDateFormat sortableDateFormat = DateSupport.getSortableDateFormat();

        Date runDate = DateSupport.trunc( runDateTime );
        Date runDateMinus1 = DateSupport.addDays( runDate, -1 );
        engine.put( "runDateTime", runDateTime );
        engine.put( "runDate", runDate );
        engine.put( "runDateMinus1", runDateMinus1 );
        engine.put( "runDateSQL", new java.sql.Date( runDate.getTime() ) );
        engine.put( "runDateMinus1SQL", new java.sql.Date( runDateMinus1.getTime() ) );
        engine.put( "runDateMMDDYYYY", standardDateFormat.format( runDate ) );
        engine.put( "runDateYYYYMMDD", sortableDateFormat.format( runDate ) );
        engine.put( "runDateMinus1MMDDYYYY", standardDateFormat.format( runDateMinus1 ) );
        engine.put( "runDateMinus1YYYYMMDD", sortableDateFormat.format( runDateMinus1 ) );
    }

}
