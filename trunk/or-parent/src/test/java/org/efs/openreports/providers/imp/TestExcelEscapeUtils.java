package org.efs.openreports.providers.imp;

import org.efs.openreports.providers.impl.ExcelEscapeUtils;

import junit.framework.TestCase;

public class TestExcelEscapeUtils extends TestCase {
    
    public void testEscapeText() throws Exception {
        
        
        assertEquals("", doEscape( "" ));
        assertEquals("hello", doEscape( "hello" ));
        assertEquals("This_is_a_test", doEscape( "This_is_a_test" ));
        assertEquals("Three_x-aaa", doEscape( "Three_x-aaa" ));
        assertEquals("Four_x005F_x0020_bbb", doEscape( "Four_x0020_bbb" ));
        assertEquals("hello", doEscape( "hello" ));
        assertEquals("Seventeen_x005F_x005F_x005F_x0020_cccc", doEscape( "Seventeen_x005F_x0020_cccc" ));
        assertEquals("Seven_x02_rrr", doEscape( "Seven_x02_rrr" ));
        assertEquals("TestOnefoo", doEscape( "TestOnefoo" ));
    }
    
    public void testEscapeIllegalChar() {
        assertEquals("_x0002_", doEscapeChar('\2'));
        assertEquals("_x0020_", doEscapeChar((char) 0x20));
    }

    private String doEscapeChar( char c ) {
        return ExcelEscapeUtils.escapeSpecialChar( c );
    }

    private String doEscape( String string ) {
        return ExcelEscapeUtils.escapeUnderscoreForExcel( string );
    }

}
