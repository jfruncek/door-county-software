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

import org.efs.openreports.actions.DisplayTagAction;
import org.efs.openreports.providers.BookProvider;
import org.efs.openreports.providers.ProviderException;

public class ListBooksAction extends DisplayTagAction  
{	
	private static final long serialVersionUID = 5658073268480591650L;

	private List reportBooks;
	
	private BookProvider bookProvider;
	
	public List getReportBooks()
	{
		return reportBooks;
	}

	public String execute()
	{  	
		try
		{			
			reportBooks = bookProvider.getReportBooks();
		}
		catch(ProviderException pe)
		{
		    ActionHelper.addExceptionAsError( this, pe );
			return ERROR;	
		}	
		
		return SUCCESS;		
	}
	
	public void setBookProvider(BookProvider bookProvider)
	{
		this.bookProvider = bookProvider;
	}

}