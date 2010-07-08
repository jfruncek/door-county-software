package org.efs.openreports.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Some utility methods for time and date related functions. 
 * 
 * @author mconner
 */
public class TimeU {
    
    public class Mark {
        protected long mark;

        public Mark( long mark ) {
            this.mark = mark;
        }

        public long getMark() {
            return mark;
        }

        public long elapsedInMilliseconds() {
            return now() - mark;
        }

        public double elapsedInSeconds() {
            return ( now() - mark ) / (double) 1000;
        }

    }

    /** the standard instance */
    public static final TimeU I = new TimeU();
    public static final int MINUTES_IN_DAY = 60 * 24;
    public static final String STD_DATE_TIME_MSG = "EEE, d MMM yyyy hh:mm:ss a z";
    public static final String SORTABLE_DATE_TIME = "yyyy-MM-dd HH:mm:ss z";

    public long minutesToMilleseconds( int minutes ) {
        return minutes * 60 * 1000;
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public long shiftTimeByMilleseconds( long timeInMilleseconds, long milleseconds ) {
        return timeInMilleseconds + milleseconds;
    }

    public long shiftTimeByMinutes( int minutes ) {
        return shiftTimeByMinutes( now(), minutes );
    }

    public long shiftTimeByMinutes( long timeInMilleseconds, int minutes ) {
        return timeInMilleseconds + minutesToMilleseconds( minutes );
    }

    public String formatTime(long timeInMilleseconds, String formatString) {
        return new SimpleDateFormat(formatString).format( new Date(timeInMilleseconds) );
    }

    public String formatTimeForMessage( long timeInMilleseconds ) {
        return formatTime( timeInMilleseconds, STD_DATE_TIME_MSG );
    }
    
    public String formatTimeForSortable( long timeInMilleseconds ) {
        return formatTime( timeInMilleseconds, SORTABLE_DATE_TIME );
    }
    
    public String formatNowForSortable() {
        return formatTimeForSortable( now() );
    }
    
    public long timeInMilleseconds(String formatString, String stringValue) {
        try {
            return new SimpleDateFormat(formatString).parse( stringValue ).getTime();
        } catch( ParseException e ) {
            throw new IllegalArgumentException("Bad value: " + stringValue + ", for date format: " + formatString);
        }
    }
    
    public long elapsedInSeconds(long startInMilleseconds, long endInMilliseconds) {
        return (endInMilliseconds - startInMilleseconds) / 1000;
    }


    public Mark mark() {
        return new Mark(now());
    }
    

}
