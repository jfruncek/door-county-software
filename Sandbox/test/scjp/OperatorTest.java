/*
 * scjp.OperatorTest.java
 * Created on Feb 25, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package scjp;

import junit.framework.TestCase;

/**
 * Tests various operators (from SCJP, Chapter 4, Operators) 
 * 
 * @author jfruncek
 */
public class OperatorTest extends TestCase {

    public void testInstanceOf()
    {
        Short s = 15;
        assertTrue(s instanceof Number);
        // next lien won't even compile, because it knows it can't be true!
        // assertFalse(s instanceof String);
    }
    
    public void testAssignmentHappensInExpression () {
        Boolean b = true;
        if ( b = false ) 
            assertFalse(b == true);
    }
    
    public void testTheWayStringExpressionsWork () {
        Long x = 42L;
        Long y = 44L;
        assertEquals(" " + 7 + 2 + " ", " 72 ");
        assertEquals("foo425 ", foo() + x + 5 + " ");
        assertEquals("86foo", x + y + foo());
    }

    private static String foo() {
        return "foo";
    }
}
