/*
 * Copyright (C) 2002 Erik Swenson - erik@oreports.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.efs.openreports.actions.admin;

import java.util.List;
import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.actions.DisplayTagAction;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.LocalStrings;

public class ListChaptersAction extends DisplayTagAction implements SessionAware
{	
	private static final long serialVersionUID = -8368547771698520258L;

	private Map<Object, Object> session;
	
	private int id = Integer.MIN_VALUE;
	private List<BookChapter> chapters;
	private ReportBook reportBook;

	private BookProvider bookProvider;
	
    @SuppressWarnings("unchecked")
	public void setSession(Map session) 
	{
		this.session = session;
	}
    
	public String execute()
	{  	
		try
		{			
			restoreBookUsingSession(id);

            if ( reportBook == null ) {
                addActionError(getText(LocalStrings.ERROR_BOOK_INVALID));
    			return INPUT;
    		}

			chapters = reportBook.getChapters();
			session.put(ORStatics.REPORT_BOOK, reportBook);
		}
		catch(ProviderException pe)
		{
		    ActionHelper.addExceptionAsError( this, pe );
			return ERROR;	
		}	
		
		return SUCCESS;		
	}
	
	private void restoreBookUsingSession(int id)  throws ProviderException {
		
		if (id > Integer.MIN_VALUE) {
			reportBook = bookProvider.getReportBook(id);
		}
		else {
			reportBook = (ReportBook) session.get( ORStatics.REPORT_BOOK );
		}
	}

	public List<BookChapter> getChapters()
	{
		return chapters;
	}

	public void setBookProvider(BookProvider groupProvider)
	{
		this.bookProvider = groupProvider;
	}

	public ReportBook getReportBook() {
		return reportBook;
	}

	public void setReportBook(ReportBook book) {
		this.reportBook = book;
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