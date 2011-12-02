package org.efs.openreports.providers.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelEscapeUtils {
    private static final Pattern EXCEL_CHARACTER_ESCAPE_PATTERN = Pattern.compile( "_x\\p{XDigit}{4}?_" );

    public static String escapeSpecialChar( char c ) {
        return String.format( "_x%04x_", (int) c );
    }

    /**
     * Excel uses the format _xhhhh_ to represent certain illegal characters (where hhhh is the hex
     * asii for the char). Therefore, underscores must also be escaped to avoid being recognized as
     * part of an escape sequence when they are just plain text. Since the trailing underscore in
     * the escaped underscore could also build an escape sequence, this gets a bit messy.
     * 
     * @param stringToEscape
     * @return
     */
    public static String escapeUnderscoreForExcel( String stringToEscape ) {
        StringBuffer buf = new StringBuffer();
        int lastIndex = 0;
        Matcher m = EXCEL_CHARACTER_ESCAPE_PATTERN.matcher( stringToEscape );
        while( m.find( lastIndex ) ) {
            buf.append( stringToEscape.substring( lastIndex, m.start() ) );
            buf.append( "_x005F" );
            buf.append( m.group().substring( 0, 6 ) );
            lastIndex = m.end() + -1; // back up one to reprocess trailing underscore
        }
        buf.append( stringToEscape.substring( lastIndex ) );
        return buf.toString();
    }

}
