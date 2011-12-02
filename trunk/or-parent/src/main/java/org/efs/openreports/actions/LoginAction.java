/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.efs.openreports.actions;

import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.UserProvider;
import org.efs.openreports.util.LocalStrings;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;


public class LoginAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 1L;

	private Map<Object, Object> session;

	protected UserProvider userProvider;

	@Override
	public String execute() {
		String DASHBOARD = "dashboard";
		String REPORTGROUP = "reportGroup";
		String REPORTADMIN = "reportAdmin";
		String USERADMIN = "userAdmin";
		String LISTSCHEDULEDREPORTS = "listScheduledReports";

		// Principal userPrincipal =
		// ServletActionContext.getRequest().getUserPrincipal();

		/*
		 * if( userPrincipal == null ) { addActionError(
		 * "no access without authentication!" );
		 * System.out.print("Error is:"+ERROR); return ERROR; }
		 */

		try {

			String userName = ServletActionContext.getRequest().getParameter(
					"username");
			String redirection = ServletActionContext.getRequest()
					.getParameter("redirection");
			ReportUser user = userProvider.getUser(userName.toLowerCase());

			if (user != null) {
				session.put("user", user);
				ActionContext.getContext().setLocale(user.getLocale());
			}
			
			else {
				addActionError(getText(LocalStrings.ERROR_INVALID_USER));
				return ERROR;
			}

			// Commented for EC-10835
			/*
			 * if( user.isDashboardUser() && ( user.getDefaultReport() != null
			 * || user.getAlerts().size() > 0 ) ) { return
			 * ORStatics.DASHBOARD_ACTION;
			 */
			// Added for EC-10835
			if (redirection.equalsIgnoreCase("dashboard")) {
				if (user.isDashboardUser()
						&& (user.getDefaultReport() != null || user.getAlerts()
								.size() > 0)) {
					return ORStatics.DASHBOARD_ACTION;
					// ServletActionContext.getRequest().getSession().removeAttribute("InvalidUser"
					// );
				}
				return DASHBOARD;

			} else if (redirection.equalsIgnoreCase("reportGroup")) {
				return REPORTGROUP;
			} else if (redirection.equalsIgnoreCase("reportAdmin")) {
				return REPORTADMIN;
			}	else if (redirection.equalsIgnoreCase("userAdmin")) {
					return USERADMIN;
			}	else if (redirection.equalsIgnoreCase("listScheduledReports")) {
				return LISTSCHEDULEDREPORTS;
			} else {
				return ERROR;
			}
			// Commented for EC-10835
			/*
			 * else { addActionError(String.format(
			 * "No user named \'%s\' is currently configured", userName));
			 * ServletActionContext.getRequest().getSession()
			 * .setAttribute("InvalidUser", userName); return INPUT; }
			 */
		}

		catch (Exception e) {
			e.printStackTrace();
			ActionHelper.addExceptionAsError(this, e);
			return ERROR;
		}

	}

	@SuppressWarnings("unchecked")
	public void setSession(Map session) {
		this.session = session;
	}

	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}

}