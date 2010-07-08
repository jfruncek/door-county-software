package org.efs.openreports.scheduler.notification;

import java.util.Collections;

import org.efs.openreports.UnitTestSupport;
import org.efs.openreports.delivery.DeliverySupport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.scheduler.notification.TestRunStatusNotificationSupport.MockMailProvider;
import org.efs.openreports.scheduler.notification.TestRunStatusNotificationSupport.TestPropertyProvider;

import junit.framework.TestCase;

public class TestRunStatusRegistry extends TestCase {

    public void testRegistry1() throws Exception {

        RunStatusRegistry registry = new RunStatusRegistry();

        RunStatus book1 = registry.addRunStatus( RunType.BOOK, "TestBook1", "Test Book Desc 1" );
        RunStatus batch1 = registry.addRunStatus( RunType.BATCH, "TestBatch1", "Test Batch 1 Desc", book1 );
        RunStatus batch2 = registry.addRunStatus( RunType.BATCH, "TestBatch2", "Test Batch 2 Desc", book1 );
        RunStatus report1 = registry.addRunStatus( RunType.REPORT, "TestReport1", "Test Report 1 Desc", batch1 );
        /* RunStatus report2 = */registry.addRunStatus( RunType.REPORT, "TestReport2", "Test Report 2 Desc", batch1 );
        /* RunStatus report3 = */registry.addRunStatus( RunType.REPORT, "TestReport3", "Test Report 3 Desc", batch2 );
        RunStatus report4 = registry.addRunStatus( RunType.REPORT, "TestReport4", "Test Report 4 Desc", batch2 );

        assertEquals( 1, registry.getRootLevelCount() );
        assertEquals( 7, registry.getRunStatusCount() );

        RunStatus book2 = registry.addRunStatus( RunType.BOOK, "TestBook 2", "Test Book Desc 2" );
        /* RunStatus report5 = */registry.addRunStatus( RunType.REPORT, "TestReport5", "Test Report 5 Desc", book2 );
        RunStatus report6 = registry.addRunStatus( RunType.REPORT, "TestReport6", "Test Report 6 Desc", book2 );

        assertEquals( 2, registry.getRootLevelCount() );
        assertEquals( 10, registry.getRunStatusCount() );

        assertSame( book1, report1.getRootAncestor() );
        assertSame( book2, report6.getRootAncestor() );

        RunStatus updatedStatus = registry.markRunStatusComplete( report1.getRefererenceKey(), "SUCCESS", true, null );
        assertSame( report1, updatedStatus );

        registry.removeAllInTree( report4.getRootAncestor() );
        assertEquals( 3, registry.getRunStatusCount() );
        assertEquals( 1, registry.getRootLevelCount() );

        RunStatus updatedStatus2 = registry.markRunStatusComplete( report1.getRefererenceKey(), "SUCCESS", true, null );
        assertNull( updatedStatus2 );// report 1 was part of book1, which should have been removed.
    }

    /**
     * More of an integration test registry, notification support
     * 
     * @throws Exception
     */
    public void testRegistryMarkCompleteWithNotification() throws Exception {
        RunStatusRegistry registry = new RunStatusRegistry();
        RunStatusNotificationSupport ns = new RunStatusNotificationSupport();
        ns.setDeliverySupport( new DeliverySupport( new TestPropertyProvider() ) );
        MockMailProvider mailProvider = new MockMailProvider();
        ns.setMailProvider( mailProvider );
        registry.setRunStatusNotificationSupport( ns );

        RunStatus book1 = registry.addRunStatus( RunType.BOOK, "TestBook1", "Test Book Desc 1" );
        RunStatus batch1 = registry.addRunStatus( RunType.BATCH, "TestBatch1", "Test Batch 1 Desc", book1 );
        RunStatus batch2 = registry.addRunStatus( RunType.BATCH, "TestBatch2", "Test Batch 2 Desc", book1 );
        RunStatus report1 = registry.addRunStatus( RunType.REPORT, "TestReport1", "Test Report 1 Desc", batch1 );
        RunStatus report2 = registry.addRunStatus( RunType.REPORT, "TestReport2", "Test Report 2 Desc", batch1 );
        RunStatus report3 = registry.addRunStatus( RunType.REPORT, "TestReport3", "Test Report 3 Desc", batch2 );
        RunStatus report4 = registry.addRunStatus( RunType.REPORT, "TestReport4", "Test Report 4 Desc", batch2 );

        registry.markRunCompleteWithNotification( book1.getRefererenceKey(), "success", true, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( batch1.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( batch2.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report1.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report2.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report3.getRefererenceKey(), "fail", false, Collections
                .singletonList( "faildy-fail-fail" ) );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report4.getRefererenceKey(), "fail", false, Collections
                .singletonList( "faildy-fail-fail" ) );
        assertEquals( 1, mailProvider.sentMessages.size() );
    }

    /**
     * More of an integration test registry, notificaton support
     * 
     * @throws Exception
     */
    public void testRegistryremoveRootStatusWithNotification() throws Exception {
        RunStatusRegistry registry = new RunStatusRegistry();
        RunStatusNotificationSupport ns = new RunStatusNotificationSupport();
        ns.setDeliverySupport( new DeliverySupport( new TestPropertyProvider() ) );
        MockMailProvider mailProvider = new MockMailProvider();
        ns.setMailProvider( mailProvider );
        registry.setRunStatusNotificationSupport( ns );

        RunStatus book1 = registry.addRunStatus( RunType.BOOK, "TestBook1", "Test Book Desc 1" );
        RunStatus batch1 = registry.addRunStatus( RunType.BATCH, "TestBatch1", "Test Batch 1 Desc", book1 );
        RunStatus batch2 = registry.addRunStatus( RunType.BATCH, "TestBatch2", "Test Batch 2 Desc", book1 );
        RunStatus report1 = registry.addRunStatus( RunType.REPORT, "TestReport1", "Test Report 1 Desc", batch1 );
        RunStatus report2 = registry.addRunStatus( RunType.REPORT, "TestReport2", "Test Report 2 Desc", batch1 );
        RunStatus report3 = registry.addRunStatus( RunType.REPORT, "TestReport3", "Test Report 3 Desc", batch2 );
        // Not removing this one:
        /* RunStatus report4 = */registry.addRunStatus( RunType.REPORT, "TestReport4", "Test Report 4 Desc", batch2 );

        registry.markRunCompleteWithNotification( book1.getRefererenceKey(), "success", true, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( batch1.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( batch2.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report1.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report2.getRefererenceKey(), "success", false, null );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.markRunCompleteWithNotification( report3.getRefererenceKey(), "fail", false, Collections
                .singletonList( "faildy-fail-fail" ) );
        assertEquals( 0, mailProvider.sentMessages.size() );
        registry.removeRootStatusWithNotification( book1 );
        assertEquals( 1, mailProvider.sentMessages.size() );
        String expectedMessage =
                UnitTestSupport.loadResourceAsString( TestRunStatusNotificationSupport.class,
                        "expectedIncompleteNotification.txt" );
        MailMessage msg = mailProvider.sentMessages.get( 0 );
        assertEquals( expectedMessage, msg.getText() );
    }

}
