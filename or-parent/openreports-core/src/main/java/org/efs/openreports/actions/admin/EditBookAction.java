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

package org.efs.openreports.actions.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.ORTag;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.GroupProvider;
import org.efs.openreports.providers.ReportProvider;
import org.efs.openreports.providers.TagProvider;

import com.opensymphony.xwork2.ActionSupport;

public class EditBookAction extends ActionSupport implements SessionAware
{	
	private static final long serialVersionUID = -8251101711142247956L;

	protected static Logger log = Logger.getLogger(EditBookAction.class);

	private String command;
	private boolean submitOk;	
	private boolean submitDuplicate;

	private int id;
	private String name;
	private String outputPath;
	private String description;
    private String tags;
    private String tagList;
	private List<ReportGroup> reportGroups;
	private int[] groupIds;

	private ReportBook reportBook;

	private GroupProvider groupProvider;
	private BookProvider bookProvider;
	private ReportProvider reportProvider;
	private TagProvider tagProvider;

    /*
     * Implement SessionAware
     */
    private Map<Object, Object> session;

    @SuppressWarnings( "unchecked" )
    public void setSession( Map session ) {
        this.session = session;
    }

    /*
     * Implement Action
     */
    public String execute()
	{
    	ReportUser user = fetchReportUser();
    	
		try
		{
			if (command.equals("edit"))
			{
				reportBook = bookProvider.getReportBook(new Integer(id));
			}
			else
			{
				reportBook = new ReportBook();
			}

			if (command.equals("edit") && !submitOk && !submitDuplicate)
			{
				name = reportBook.getName();
				outputPath = reportBook.getOutputPath();
				reportGroups = reportBook.getGroups();
                tags = tagProvider.getTagsForObject(reportBook.getId(), ReportBook.class, ORTag.TAG_TYPE_UI);                
				
                if (reportGroups != null) Collections.sort(reportGroups);
                
				groupIds = null;
			}
            tagList = tagProvider.getTagList(ReportBook.class, ORTag.TAG_TYPE_UI);
            
			if (!submitOk && !submitDuplicate)	return INPUT;
			
			 if (submitDuplicate)
	            {
	            	command = "add";
	            	reportBook.setId(null);
	            	
	            	if (reportBook.getName().equals(name))
	            	{
	            		name = "Copy of ".concat(name);
	            	}
	            }

			reportBook.setName(name);
			reportBook.setOutputPath(outputPath);
			reportBook.setGroups(convertIdsToGroups(groupIds));
            
			if (command.equals("edit"))
			{
				bookProvider.updateReportBook(reportBook, user.getName());               
			}

			if (command.equals("add"))
			{
				reportBook = bookProvider.insertReportBook(reportBook, user.getName());
			}
            
            // save tags
            tagProvider.setTags(reportBook.getId(), ReportBook.class, tags, ORTag.TAG_TYPE_UI);
			
			return SUCCESS;
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this,  e );
			return INPUT;
		}
	}
	
    public String getTagList()
    {
        return tagList;
    }
    
    public String getTags(Integer reportId)
    {
        try
        {
            return tagProvider.getTagsForObject(reportId, Report.class, ORTag.TAG_TYPE_UI);           
        }
        catch(Exception e)
        {
            ActionHelper.addExceptionAsError( this, e );
            return null;
        }
    }

	public String getCommand()
	{
		return command;
	}

	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return name;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSubmitOk(String submitOk)
	{
		if (submitOk != null) this.submitOk = true;
	}	
	
	public void setSubmitDuplicate(String submitDuplicate)
	{
		if (submitDuplicate != null) this.submitDuplicate = true;
	}

	public List<Report> getReports()
	{
		try
		{
			return reportProvider.getReports();
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this, e );
			return null;
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

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getTags() 
    {
	    return tags;
    }
        
	public void setTags(String tags)
    {
	    this.tags = tags;
    }

	public void setBookProvider(BookProvider bookProvider)
	{
		this.bookProvider = bookProvider;
	}

	public void setGroupProvider(GroupProvider groupProvider)
	{
		this.groupProvider = groupProvider;
	}

	public void setReportProvider(ReportProvider reportProvider)
	{
		this.reportProvider = reportProvider;
	}

	private List<ReportGroup> convertIdsToGroups(int[] ids)
	{
		if (ids == null)
			return new ArrayList<ReportGroup>();

		List<ReportGroup> groups = new ArrayList<ReportGroup>();

		try
		{
			for (int i = 0; i < ids.length; i++)
			{
				ReportGroup group = groupProvider.getReportGroup(new Integer(ids[i]));
				groups.add(group);
			}
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this, e );
		}

		return groups;
	}

	public int[] getGroupIds()
	{
		return groupIds;
	}

	public void setGroupIds(int[] groupIds)
	{
		this.groupIds = groupIds;
	}

	public List<ReportGroup> getReportGroups()
	{
		try
		{
			return groupProvider.getReportGroups();
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this, e );
			return null;
		}
	}

	public List<ReportGroup> getReportGroupsForBook()
	{
		return reportGroups;
	}

    public void setTagProvider(TagProvider tagProvider) 
    {
        this.tagProvider = tagProvider;
    }

    ReportUser fetchReportUser() {
        return (ReportUser) session.get( ORStatics.REPORT_USER );
    }
}