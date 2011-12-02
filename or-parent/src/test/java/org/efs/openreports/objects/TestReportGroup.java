package org.efs.openreports.objects;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestReportGroup extends TestCase {

	protected ReportGroup group;

	protected void setUp() throws Exception {
		createReportGroup();
	}

	protected void createReportGroup() {
		group = new ReportGroup();
	}

	public void testGetReportsForDisplay_OmitsHiddenReports() {
		Report report1 = new Report();
		report1.setHidden(false);
		Report report2 = new Report();
		report2.setHidden(true);
		Report report3 = new Report();
		report3.setHidden(true);
		Report report4 = new Report();
		report4.setHidden(false);
		List<Report> reports = new ArrayList<Report>();
		reports.add(report1);
		reports.add(report2);
		reports.add(report3);
		reports.add(report4);
		group.setReports(reports);
		List<Report> displayReports = group.getReportsForDisplay();
		assertNotNull("displayed reports is not null", displayReports);
		assertEquals("number of displayed reports", 2, displayReports.size());
		for (Report report : displayReports) {
			assertFalse("Displayed report is not hidden", report.isHidden());
		}
	}

	public void testIsValidReport_PassedNullReport() {
		assertFalse ("null report is not assigned to group", group.isValidReport(null));
	}

	public void testIsValidReport_PassedReport() {
		Report report1 = new Report();
		report1.setId(1);
		Report report2 = new Report();
		report1.setId(2);
		List<Report> reports = new ArrayList<Report>();
		reports.add(report1);
		group.setReports(reports);
		assertTrue("report is assigned to group", group.isValidReport(report1));
		assertFalse("report is not assigned to group", group.isValidReport(report2));
	}
}
