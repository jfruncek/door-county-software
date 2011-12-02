package org.efs.openreports.scheduler.notification;

import java.util.Collections;

import junit.framework.TestCase;

public class TestRunStatus extends TestCase {

    public void testRunStatusCreation() throws Exception {
        RunStatus rs = new RunStatus( RunType.BOOK, "TestBook", "Test Book Desc", "refkey0001" );
        assertTrue( rs.getChildren().size() == 0 );
        assertEquals( "TestBook", rs.getName() );
        assertEquals( "Test Book Desc", rs.getDescription() );
        assertEquals( "refkey0001", rs.getRefererenceKey() );
        assertTrue( rs.getCreateTime() > 0 );
        assertTrue( rs.getCompleteTime() == Long.MAX_VALUE );
        assertTrue( rs.getNotificationRecipients() == null );
        assertFalse( rs.isComplete() );
        assertFalse( rs.isFailure() );
        assertFalse( rs.isSuccess() );
        assertFalse( rs.isSubtreeComplete() );
        assertNotNull( rs.getdetailMessages() );
        assertTrue( rs.getdetailMessages().isEmpty() );
        assertNull( rs.getParent() );
        assertSame( rs, rs.getRootAncestor());
        assertSame(RunType.BOOK, rs.getRunType());
    }
    
    public void testParentChildRelationships() throws Exception {
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
        
        assertSame(grandparent, child1.getRootAncestor());
        assertSame(grandparent, child2.getRootAncestor());
        assertSame(grandparent, child3.getRootAncestor());
        assertSame(grandparent, child4.getRootAncestor());

        assertSame(grandparent, parent1.getRootAncestor());
        assertSame(grandparent, parent2.getRootAncestor());
        
        assertSame(grandparent, grandparent.getRootAncestor());
        assertTrue(grandparent.hasChildren());
        assertTrue(parent1.hasChildren());
        assertFalse(child1.hasChildren());
        
    }
    
    public void testStatistics() throws Exception {
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
        
        RunStatus.Statistics stats = grandparent.getSubtreeStatistics();
        assertEquals(7, stats.getCount());
        assertEquals(7, stats.getIncompleteCount());
        assertEquals(0, stats.getFailureCount());
        assertEquals(0, stats.getSuccessCount());
        assertEquals(4, stats.getLeafCount());
        assertEquals(3, stats.getNonLeafCount());
        assertEquals(0, stats.getLeafFailureCount());
        assertEquals(0, stats.getLeafSuccessCount());
        assertFalse(stats.hasFailures());
        assertFalse(stats.hasSuccesses());
        assertFalse(stats.hasLeafSuccesses());
        
        child1.markComplete( "Success", true, null );
        assertTrue(child1.isComplete());
        assertFalse(parent1.isComplete());
        
        stats = grandparent.getSubtreeStatistics();
        assertEquals(7, stats.getCount());
        assertEquals(6, stats.getIncompleteCount());
        assertEquals(0, stats.getFailureCount());
        assertEquals(1, stats.getSuccessCount());
        assertEquals(4, stats.getLeafCount());
        assertEquals(3, stats.getNonLeafCount());
        assertEquals(0, stats.getLeafFailureCount());
        assertEquals(1, stats.getLeafSuccessCount());
        assertFalse(stats.hasFailures());
        assertTrue(stats.hasSuccesses());
        assertTrue(stats.hasLeafSuccesses());

        stats = child1.getSubtreeStatistics();
        assertTrue(child1.isSubtreeComplete());
        assertFalse(parent1.isSubtreeComplete());
        assertFalse(grandparent.isSubtreeComplete());
        
        child2.markComplete( "Fail", false, Collections.singletonList( "Fail-Failedy-Failed" ));
        parent1.markComplete( "Success", true, null );
        assertTrue( parent1.isComplete() );
        stats = parent1.getSubtreeStatistics();
        assertTrue(stats.hasFailures());
        assertTrue(stats.hasSuccesses());
        assertEquals(1, stats.getFailureCount());
        assertEquals(2, stats.getSuccessCount());
        assertEquals(1, stats.getLeafSuccessCount());
        assertEquals(1, stats.getLeafFailureCount());
    }
    

}
