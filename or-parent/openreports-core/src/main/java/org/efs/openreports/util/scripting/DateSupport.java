package org.efs.openreports.util.scripting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Some simple date manipulation stuff useful to manipulate parameter info.
 * 
 * @author mconner
 */
public class DateSupport {

    /** a format that would sort chronologically (e.g. if part of a filename) */
    public static final String DATE_FORMAT_STRING_SORTABLE = "yyyy-MM-dd";

    public static final String DATE_FORMAT_STRING_STANDARD = "MM-dd-yyyy";

    public static Date addDays( Date date, int days ) {
        Calendar cal = makeCalendar( date );
        cal.add( Calendar.DAY_OF_MONTH, days );
        return cal.getTime();
    }

    public static Calendar makeCalendar( Date date ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( date );
        return cal;
    }

    /**
     * @param date
     * @return the date portion of date (time is cleared).
     */
    public static Date trunc( Date date ) {
        Calendar cal = makeCalendar( date );
        cal.setTime( date );
        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
        int day = cal.get( Calendar.DAY_OF_MONTH );
        cal.clear();
        cal.set( year, month, day );
        return cal.getTime();
    }

    /**
     * @return a standardized date format, as defined by DATE_FORMAT_STRING_STANDARD
     */
    public static SimpleDateFormat getStandardDateFormat() {
        return new SimpleDateFormat( DateSupport.DATE_FORMAT_STRING_STANDARD );
    }

    /**
     * @return a date format that sorts nicely, as defined by DATE_FORMAT_STRING_STANDARD
     */
    public static SimpleDateFormat getSortableDateFormat() {
        return new SimpleDateFormat( DateSupport.DATE_FORMAT_STRING_SORTABLE );
    }

}
