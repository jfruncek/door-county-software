/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.efs.openreports.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.GroupProvider;
import org.efs.openreports.util.LocalStrings;

import com.opensymphony.xwork2.ActionContext;

public class ReportListAction extends DisplayTagAction implements SessionAware {
	private static final long serialVersionUID = -899415288967666034L;

	protected static Logger log = Logger.getLogger(ReportListAction.class);

	private Map<Object, Object> session;

	private int groupId = Integer.MIN_VALUE;

	private List<Report> reports;

	private List<ReportBook> reportBooks;

	private BookProvider bookProvider;

	private GroupProvider groupProvider;

	private ReportGroup reportGroup;

	public String execute() {
		try {
			if (groupId == Integer.MIN_VALUE) {
				reportGroup = (ReportGroup) ActionContext.getContext()
						.getSession().get(ORStatics.REPORT_GROUP);
			} else {
				ReportUser reportUser = (ReportUser) ActionContext.getContext()
						.getSession().get(ORStatics.REPORT_USER);

				reportGroup = groupProvider.getReportGroup(new Integer(groupId));

				if (reportUser.isValidGroup(reportGroup)) {
					session.put(ORStatics.REPORT_GROUP, reportGroup);
				} else {
					addActionError(getText(LocalStrings.ERROR_REPORTGROUP_NOTAUTHORIZED));
					return ERROR;
				}
			}

			if (reportGroup != null) {
				reports = reportGroup.getReportsForDisplay();
				if (reports != null) Collections.sort(reports);
				reportBooks = bookProvider.getBooksForGroup(reportGroup);
				if (reportBooks != null) Collections.sort(reportBooks);
			}

			return SUCCESS;
		} catch (Exception e) {
			ActionHelper.addExceptionAsError(this, e);
			return ERROR;
		}
	}

	@SuppressWarnings("unchecked")
	public void setSession(Map session) {
		this.session = session;
	}

	public List<ReportBook> getReportBooks() {
		return reportBooks;
	}

	public List<Report> getReports() {
		return reports;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public ReportGroup getReportGroup() {
		return reportGroup;
	}

	public void setGroupProvider(GroupProvider groupProvider) {
		this.groupProvider = groupProvider;
	}

	public void setBookProvider(BookProvider bookProvider) {
		this.bookProvider = bookProvider;
	}
}