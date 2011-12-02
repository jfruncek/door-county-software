/*
 * Copyright (C) 2007 Erik Swenson - erik@oreports.com
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

package org.efs.openreports.actions.admin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.GroupProvider;
import org.efs.openreports.providers.UserProvider;

import com.opensymphony.xwork2.ActionSupport;

public class EditGroupUsersAction	extends ActionSupport implements SessionAware
{
	private static final long serialVersionUID = -3762924762519626228L;

	protected static Logger log = Logger.getLogger(EditGroupUsersAction.class);
	
	private Map<Object,Object> session;

	private int id;
	private int[] userIds = new int[0];

	private String submitType;

	private ReportGroup group;
	
	private List<ReportUser> users;
	private List<ReportUser> usersForGroup;

	private GroupProvider groupProvider;
	private UserProvider userProvider;

	public String execute()
	{
		try
		{
			group = groupProvider.getReportGroup(new Integer(id));
			usersForGroup = userProvider.getUsersForGroup(group);
			users = userProvider.getUsers();	
			ReportUser currentUser = fetchReportUser();

			if (submitType == null) return INPUT;		
			
			Iterator<ReportUser> iterator = users.iterator();
			while(iterator.hasNext())
			{
				ReportUser user = iterator.next();

				boolean userHasGroup = false;
				
				for (int i = 0; i < userIds.length; i++)
				{
					if (user.getId().equals(new Integer(userIds[i])))
					{
						userHasGroup = true;
						
						if (!user.isValidGroup(group))
						{
							user.getGroups().add(group);
							userProvider.updateUser(user, currentUser.getName());
						}
					}
				}
				
				if (!userHasGroup)
				{					
					for (int i=0; i < user.getGroups().size(); i++)
					{					
						ReportGroup reportGroup = user.getGroups().get(i);
						if (group.getId().equals(reportGroup.getId()))
						{
							user.getGroups().remove(reportGroup);	
							userProvider.updateUser(user, currentUser.getName());	
							
							i=0;
						}
					}						
				}
			}	
			
			//refresh current user
			currentUser  = userProvider.getUser(currentUser.getId());
    		session.put(ORStatics.REPORT_USER, currentUser);
			
			return SUCCESS;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ActionHelper.addExceptionAsError( this, e );
			return INPUT;
		}
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public List<ReportUser> getUsersForGroup()
	{
		return usersForGroup;
	}

	public List<ReportUser> getUsers()
	{
		return users;
	}		

	public ReportGroup getGroup()
	{
		return group;
	}

	public void setGroup(ReportGroup group)
	{
		this.group = group;
	}	
	
	public void setGroupProvider(GroupProvider groupProvider)
	{
		this.groupProvider = groupProvider;		
	}

	public void setUserProvider(UserProvider userProvider)
	{
		this.userProvider = userProvider;
	}

	public int[] getUserIds()
	{
		return userIds;
	}

	public void setUserIds(int[] userIds)
	{
		this.userIds = userIds;
	}

	public String getSubmitType()
	{
		return submitType;
	}

	public void setSubmitType(String submitType)
	{
		this.submitType = submitType;
	}

	@SuppressWarnings("unchecked")
	public void setSession(Map session)
	{
		this.session = session;
	}
	
    private ReportUser fetchReportUser() {
        return (ReportUser) session.get( ORStatics.REPORT_USER );
    }
}