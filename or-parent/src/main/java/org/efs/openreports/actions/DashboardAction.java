/*
 * Copyright (C) 2006 Erik Swenson - erik@oreports.com
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.objects.ReportUserAlert;
import org.efs.openreports.providers.AlertProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.UserProvider;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class DashboardAction extends ActionSupport  
{		
    private static final long serialVersionUID = 4818598674109355225L;
    
    private List<ReportUserAlert> alerts;
	private Report report;

	private int id;
	
	private AlertProvider alertProvider;	
	 protected UserProvider userProvider;
	
	@Override
	public String execute() {
		
		  ReportUser user = (ReportUser)
		  ActionContext.getContext().getSession().get( ORStatics.REPORT_USER);
		 

		/*// EC-10835
		try {
			String userName = ServletActionContext.getRequest().getParameter(
					"username");
			ReportUser user = userProvider.getUser(userName.toLowerCase());
			// EC-10835 end
*/
			alerts = new ArrayList<ReportUserAlert>();

			Iterator<ReportUserAlert> iterator = user.getAlerts().iterator();
			while (iterator.hasNext()) {
				try {

					ReportUserAlert userAlert = iterator.next();
					userAlert.setUser(user);

					ReportUserAlert alert = alertProvider.executeAlert(
							userAlert, false);

					alerts.add(alert);
				} catch (ProviderException pe) {
					ActionHelper.addExceptionAsError(this, pe);
				}
			}

			report = user.getDefaultReport();

			/*// EC-10835
		} catch (Exception e) {
			e.printStackTrace();
			ActionHelper.addExceptionAsError(this, e);
			return ERROR;
		}// EC-10835 end
*/		return INPUT;
	}		

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}	

	public List<ReportUserAlert> getAlerts()
	{
		return alerts;
	}

	public void setAlertProvider(AlertProvider alertProvider)
	{
		this.alertProvider = alertProvider;
	}
	
	public Report getReport()
	{
		return report;
	}

	public void setReport(Report report)
	{
		this.report = report;
	}
}