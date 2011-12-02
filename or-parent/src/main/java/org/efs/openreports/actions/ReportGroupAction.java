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

import com.opensymphony.xwork2.ActionContext;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.admin.ActionHelper;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.UserProvider;

public class ReportGroupAction extends DisplayTagAction implements SessionAware
{	
	private static final long serialVersionUID = 2135248301641354052L;

	private List<ReportGroup> reportGroups;
	private Map<Object, Object> session;
	 protected UserProvider userProvider;

	
	private int id;

	public String execute()
	{
		/*try{
		String userName = ServletActionContext.getRequest().getParameter("username");
		ReportUser user = userProvider.getUser( userName.toLowerCase() );*/
		ReportUser user = (ReportUser) ActionContext.getContext().getSession().get("user");		

		reportGroups = user.getGroups();		
		Collections.sort(reportGroups);
		
		//if there is only one report group, bypass group select screen
		if (reportGroups != null && reportGroups.size() == 1)
		{
			session.put(ORStatics.REPORT_GROUP, reportGroups.get(0));
			return SUCCESS;
		}
		
		/*} catch( Exception e ) {
            e.printStackTrace();
            ActionHelper.addExceptionAsError( this, e );
            return ERROR;
        }//EC-10835 end
*/		return INPUT;
		
	}	

	@SuppressWarnings("unchecked")
	public void setSession(Map session) 
	{
		this.session = session;
	}

	public List getReportGroups()
	{
		
		return reportGroups;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}