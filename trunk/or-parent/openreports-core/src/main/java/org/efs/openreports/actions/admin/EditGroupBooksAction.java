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
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportGroup;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.GroupProvider;

import com.opensymphony.xwork2.ActionSupport;

public class EditGroupBooksAction extends ActionSupport implements SessionAware
{
	private static final long serialVersionUID = 986983582590901449L;

	protected static Logger log = Logger.getLogger(EditGroupBooksAction.class);
	
	private int id;
	private int[] bookIds = new int[0];

	private String submitType;

	private ReportGroup group;
	
	private List<ReportBook> books;
	private List<ReportBook> booksForGroup;

	private GroupProvider groupProvider;
	private BookProvider bookProvider;

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
			group = groupProvider.getReportGroup(new Integer(id));
			booksForGroup = bookProvider.getBooksForGroup(group);
			books = bookProvider.getReportBooks();			

			if (submitType == null) return INPUT;		
			
			Iterator<ReportBook> iterator = books.iterator();
			while(iterator.hasNext())
			{
				ReportBook book = (ReportBook) iterator.next();

				boolean bookHasGroup = false;
				
				for (int i = 0; i < bookIds.length; i++)
				{
					if (book.getId().equals(new Integer(bookIds[i])))
					{
						bookHasGroup = true;
						
						if (!book.isValidGroup(group))
						{
							book.getGroups().add(group);
							bookProvider.updateReportBook(book, user.getName());
						}
					}
				}
				
				if (!bookHasGroup)
				{					
					for (int i=0; i < book.getGroups().size(); i++)
					{					
						ReportGroup reportGroup = book.getGroups().get(i);
						if (group.getId().equals(reportGroup.getId()))
						{
							book.getGroups().remove(reportGroup);	
							bookProvider.updateReportBook(book, user.getName());	
							
							i=0;
						}
					}						
				}
			}	
			
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

	public List<ReportBook> getBooksForGroup()
	{
		return booksForGroup;
	}

	public List<ReportBook> getBooks()
	{
		return books;
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

	public void setBookProvider(BookProvider bookProvider)
	{
		this.bookProvider = bookProvider;
	}

	public int[] getBookIds()
	{
		return bookIds;
	}

	public void setBookIds(int[] bookIds)
	{
		this.bookIds = bookIds;
	}

	public String getSubmitType()
	{
		return submitType;
	}

	public void setSubmitType(String submitType)
	{
		this.submitType = submitType;
	}
	
    ReportUser fetchReportUser() {
        return (ReportUser) session.get( ORStatics.REPORT_USER );
    }
}