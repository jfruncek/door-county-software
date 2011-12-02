package org.efs.openreports.util;

import junit.framework.TestCase;

public class TestTimeU extends TestCase {

    public void testFormatTime() throws Exception {
        long someTime = 1270665463000L; // wed,
        String timeString = TimeU.I.formatTime( someTime, TimeU.STD_DATE_TIME_MSG );
        assertEquals( "Wed, 7 Apr 2010 01:37:43 PM CDT", timeString );
    }

    public void testMakeTimeInMilleseconds() {
        String timeString = "2010-04-07 13:37:43 CDT";
        long expectedTime = 1270665463000L;
        long newTime = TimeU.I.timeInMilleseconds( TimeU.SORTABLE_DATE_TIME, timeString );
        assertEquals( expectedTime, newTime );
    }

}
