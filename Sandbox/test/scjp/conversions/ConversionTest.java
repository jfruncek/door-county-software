/*
 * scjp.conversions.ConversionTest.java
 * Created on Dec 27, 2010 by jfruncek
 * Copyright 2010, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package scjp.conversions;

import junit.framework.TestCase;

/**
 * Tests various Java numeric conversions (from SCJP, Chapter 3, Assignments) 
 * 
 * @author jfruncek
 */
public class ConversionTest extends TestCase {

    public void testOctal() {
        assertTrue ("Octal, believe it or not!", 010 == 8);
    }
    
    public void testAnyBaseNumber() {
        Integer base7 = Integer.valueOf("26", 7);
        assertTrue(base7 == 20);
        try {
            base7 = Integer.valueOf("27", 7);
            fail("Should have thrown number format exception");
        } catch (NumberFormatException e) {
            assertTrue (true);
        }
        
    }
    
    public void testEqualityAndIdentity() {
        Integer one = -129;
        Integer two = -129;
        assertEquals("They are equal", one, two);
        assertTrue("They are not identical (not the same object", one != two);
        
        Integer three = 3;
        Integer i = 3;
        assertEquals("They are equal", three, i);
        assertTrue("They are identical! (the same object, to save memory)", three == i);
        // this includes Boolean, Byte, Character from \u0000 to \u007f, Short and Integer from -128 to 127
    }
}
