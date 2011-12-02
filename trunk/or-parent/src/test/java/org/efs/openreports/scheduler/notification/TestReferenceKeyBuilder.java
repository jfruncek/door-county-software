package org.efs.openreports.scheduler.notification;

import junit.framework.TestCase;

public class TestReferenceKeyBuilder extends TestCase {

    public void testBuilder() throws Exception {
        ReferenceKeyBuilder builder = new ReferenceKeyBuilder();
        RunStatus runStatus = new RunStatus( RunType.BOOK, "name", "description" );
        String key1 = builder.makeReferenceKey( runStatus );
        String key2 = builder.makeReferenceKey( runStatus );
        sleep( 20 );
        String key3 = builder.makeReferenceKey( runStatus );

        assertTrue( key1.endsWith( ":1" ) );
        assertTrue( key2.endsWith( ":2" ) );
        assertTrue( key3.endsWith( ":3" ) );
    }

    private void sleep( int ms ) {
        try {
            Thread.sleep( ms );
        } catch( InterruptedException e ) {
            throw new RuntimeException( "Interrupted???", e );
        }

    }

}
