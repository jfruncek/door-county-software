/*
 * Copyright (C) 2003 Erik Swenson - erik@oreports.com
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

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.GroupProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.UserProvider;

public class DeleteGroupAction extends DeleteAction implements SessionAware
{	
	private static final long serialVersionUID = -461133184936430808L;
	
	private GroupProvider groupProvider;
    private List<ReportUser> usersForGroup;
    private UserProvider userProvider;
	private Map<Object, Object> session;

	public String execute()
	{	
		try
		{
			ReportGroup reportGroup =
				groupProvider.getReportGroup(new Integer(id));
			
			name = reportGroup.getName();
			description = reportGroup.getDescription();
	         setUsersForGroup( userProvider.getUsersForGroup(reportGroup) );

			if (!submitDelete && !submitCancel)
			{
				return INPUT;
			}
			
			if (submitDelete)
			{
			    if (usersForGroup != null) {
	                for(ReportUser user : usersForGroup) {
	                    removeGroupFromUser( user, reportGroup );
	                }
			    }
				groupProvider.deleteReportGroup(reportGroup);
			}
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this, e );
			return INPUT;
		}

		return SUCCESS;
	}	

	private void removeGroupFromUser( ReportUser user, ReportGroup reportGroup ) throws ProviderException {
        List<ReportGroup> groupsForUser = user.getGroups();
        for(ReportGroup group : groupsForUser) {
            if (group.compareTo( reportGroup ) == 0) {
                groupsForUser.remove( group );
                userProvider.updateUser( user, fetchReportUser().getName() );
                return;
            }
        }
    }

    public void setGroupProvider(GroupProvider groupProvider)
	{
		this.groupProvider = groupProvider;
	}	
	
    public void setUserProvider(UserProvider userProvider)
    {
        this.userProvider = userProvider;
    }

    public void setUsersForGroup( List<ReportUser> usersForGroup ) {
        this.usersForGroup = usersForGroup;
    }

    public List<ReportUser> getUsersForGroup() {
        return usersForGroup;
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