package org.efs.openreports.objects;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestReportBook extends TestCase {

	protected ReportBook book;
	
	protected void setUp() throws Exception {
		book = new ReportBook();
	}

	public void testIsValidGroup_PassedNullGroup() {
		assertFalse ("null group is not assigned to book", book.isValidGroup(null));
	}

	public void testIsValidGroup_PassedNullGroupAfterGroupsAssigned() {
		ReportGroup group1 = new ReportGroup();
		group1.setId(1);
		List<ReportGroup> groups = new ArrayList<ReportGroup>();
		groups.add(group1);
		book.setGroups(groups);
		assertFalse ("null group is not assigned to book", book.isValidGroup(null));
	}

	public void testIsValidGroup_PassedGroup() {
		ReportGroup group1 = new ReportGroup();
		group1.setId(1);
		ReportGroup group2 = new ReportGroup();
		group1.setId(2);
		List<ReportGroup> groups = new ArrayList<ReportGroup>();
		groups.add(group1);
		book.setGroups(groups);
		assertTrue("group is assigned to book", book.isValidGroup(group1));
		assertFalse("group is not assigned to book", book.isValidGroup(group2));
	}
	
	public void testIsValidGroup_PassedGroupBeforeAssignedGroups() {
		ReportGroup group1 = new ReportGroup();
		assertFalse("group is not assigned to book", book.isValidGroup(group1));
	}
}
