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

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.efs.openreports.ORStatics;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.objects.ReportBook;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ProviderException;

public class DeleteChapterAction extends DeleteAction implements SessionAware
{	
	private static final long serialVersionUID = 7013173552439556036L;

	private Map<Object, Object> session;
	
	private int bookId;
	private int chapterId;
	private BookChapter chapter;
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
			ReportUser user = fetchReportUser();
			restoreBookUsingSession(bookId);
			chapter = reportBook.getChapter(chapterId);
			
			if (!submitDelete && !submitCancel)
			{
				return INPUT;
			}
			
			if (submitDelete)
			{
				reportBook.removeChapter(chapterId);
				bookProvider.updateReportBook(reportBook, user.getName());
			}
		}
		catch (Exception e)
		{
		    ActionHelper.addExceptionAsError( this, e );
			return INPUT;
		}

		return SUCCESS;
	}	

    ReportUser fetchReportUser() {
        return (ReportUser) session.get( ORStatics.REPORT_USER );
    }

	void restoreBookUsingSession(int id)  throws ProviderException {
		reportBook = (ReportBook) session.get( ORStatics.REPORT_BOOK );
		
		if (reportBook == null) {
			reportBook = bookProvider.getReportBook(id);
		}
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public int getChapterId() {
		return chapterId;
	}

	public void setChapterId(int chapterId) {
		this.chapterId = chapterId;
	}
	
	public BookChapter getChapter() {
		return chapter;
	}

	public void setBookProvider(BookProvider groupProvider)
	{
		this.bookProvider = groupProvider;
	}
}