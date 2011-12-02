package org.efs.openreports.scheduler.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.efs.openreports.UnitTestSupport;
import org.efs.openreports.delivery.DeliverySupport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.PropertiesProvider;
import org.efs.openreports.providers.ProviderException;

public class TestRunStatusNotificationSupport extends TestCase {

    public void testNotification() throws IOException {
        RunStatusNotificationSupport ns = new RunStatusNotificationSupport();
        ns.setDeliverySupport( new DeliverySupport( new TestPropertyProvider() ) );
        MockMailProvider mailProvider = new MockMailProvider();
        ns.setMailProvider( mailProvider );

        RunStatus grandparent = new RunStatus( RunType.BOOK, "TestBook", "Test Book Desc", "refkeyBook001" );
        RunStatus parent1 = new RunStatus( RunType.BATCH, "TestBatch1", "Test Batch 1 Desc", "refkeyBatch001" );
        RunStatus parent2 = new RunStatus( RunType.BATCH, "TestBatch2", "Test Batch 2 Desc", "refkeyBatch002" );
        RunStatus child1 = new RunStatus( RunType.REPORT, "TestReport1", "Test Report 1 Desc", "refkeyReport001" );
        RunStatus child2 = new RunStatus( RunType.REPORT, "TestReport2", "Test Report 2 Desc", "refkeyReport002" );
        RunStatus child3 = new RunStatus( RunType.REPORT, "TestReport3", "Test Report 3 Desc", "refkeyReport003" );
        RunStatus child4 = new RunStatus( RunType.REPORT, "TestReport4", "Test Report 4 Desc", "refkeyReport004" );
        grandparent.addChild( parent1 );
        grandparent.addChild( parent2 );
        parent1.addChild( child1 );
        parent1.addChild( child2 );
        parent2.addChild( child3 );
        parent2.addChild( child4 );

        child1.markComplete( "Success", true, null );
        child2.markComplete( "Its All Good", true, null );
        child3.markComplete( "FAILURE", false, Collections.singletonList( "No such report" ) );
        child4.markComplete( "SUCCESS", true, Collections.singletonList( "OKEY-DOKEY" ) );
        parent1.markComplete( "SUCCESS", true, null );
        parent2.markComplete( "SUCCESS", true, null );
        grandparent.markComplete( "SUCCESS", true, null );

        grandparent.setNotificationRecipients( "mconner@starkinvestments.com;foo@bar.com" );

        ns.sendRunStatusCompleteNotification( grandparent );
        assertTrue( mailProvider.sentMessages.size() == 1 );
        MailMessage msg = mailProvider.sentMessages.get( 0 );
        assertEquals( 2, msg.getRecipients().size() );
        String recipient = msg.getRecipients().get( 0 );
       //Commented for EC-10835
        assertEquals( "mconner@starkinvestments.com", recipient );
        //assertEquals( ORProperty.MAIL_REPLY_TO, recipient );

        assertEquals( "Book, TestBook, completed with 1 errors", msg.getSubject() );
        assertEquals( UnitTestSupport.loadResourceAsString( TestRunStatusNotificationSupport.class,
                "expectedNotificationMessage1.txt" ), msg.getText() );
    }

    static class MockMailProvider implements MailProvider {
        List<MailMessage> sentMessages = new ArrayList<MailMessage>();

        public void sendMail( MailMessage mail ) throws ProviderException {
            sentMessages.add( mail );
        }

        public void setMailHost( String mailHost ) {
            throw new UnsupportedOperationException();
        }

        public void setPassword( String password ) {
            throw new UnsupportedOperationException();
        }

        public void setUseMailAuthenticator( boolean useMailAuthenticator ) {
            throw new UnsupportedOperationException();
        }

        public void setUserName( String userName ) {
            throw new UnsupportedOperationException();
        }

    }

    static class TestPropertyProvider implements PropertiesProvider {
        static Map<String, ORProperty> values = new HashMap<String, ORProperty>();
        static {
            put( makeORProperty( ORProperty.IS_PROD_SERVER, Boolean.FALSE.toString() ) );
            put( makeORProperty( ORProperty.DEVELOPMENT_EMAIL, "it-reporting@starkinvestments.com" ) );
            //put( makeORProperty( ORProperty.DEVELOPMENT_EMAIL, "sbalantrapu@teramedica.com" ) );

          //Commented for EC-10835
            //put( makeORProperty( ORProperty.MAIL_REPLY_TO, "sbalantrapu@teramedica.com" ) );
            put( makeORProperty( ORProperty.MAIL_REPLY_TO, "it-reporting@starkinvestments.com" ) );
        }

        public ORProperty getProperty( String key ) throws ProviderException {

            ORProperty prop = values.get( key );
            if( prop != null )
                return prop;
            throw new UnsupportedOperationException( "getProperty for key " + key );
        }

        public void setProperty( String key, String value ) throws ProviderException {
            throw new UnsupportedOperationException( "getProperty for key " + key );
        }

        static ORProperty makeORProperty( String key, String value ) {
            ORProperty orProperty = new ORProperty();
            orProperty.setKey( key );
            orProperty.setValue( value );
            return orProperty;
        }

        static void put( ORProperty prop ) {
            values.put( prop.getKey(), prop );
        }
    }

}
