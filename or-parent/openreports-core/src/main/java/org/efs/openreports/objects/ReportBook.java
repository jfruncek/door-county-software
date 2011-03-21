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

package org.efs.openreports.objects;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.efs.openreports.util.CtrlDataObject;

public class ReportBook implements Comparable<ReportBook>, CtrlDataObject, Serializable
{	
	private static final long serialVersionUID = 2527972724817260066L;

	private Integer id;
	private String name;
	private String outputPath;
	private String ctrlUser;
	private Date ctrlDate;

	private List<ReportGroup> groups;

	private List<BookChapter> chapters;

	public ReportBook()
	{
	}

	public String toString()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}	

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public int compareTo(ReportBook reportGroup)
	{		
		return name.compareTo(reportGroup.getName());
	}

	public List<ReportGroup> getGroups() 
	{
		return groups;
	}

	public void setGroups(List<ReportGroup> groups)
	{
		this.groups = groups;
	}
	
	public boolean isValidGroup(ReportGroup reportGroup)
	{
		if (reportGroup == null) return false;
		
		if (groups != null && groups.size() > 0)
		{
			Iterator<ReportGroup> iterator = groups.iterator();
			while (iterator.hasNext())
			{
				ReportGroup group = iterator.next();
				if (group.getId().equals(reportGroup.getId()))
					return true;
			}
		}

		return false;
	}

	public List<BookChapter> getChapters() {
		return chapters;
	}

	public void setChapters(List<BookChapter> chapters) {
		this.chapters = chapters;
	}

	public BookChapter getChapter(int id) throws IllegalArgumentException {
		for (BookChapter chapter : chapters) {
			if (chapter.getId() == id) {
				return chapter;
			}
		}
		throw new IllegalArgumentException("No chapter with id=" + id + " exist on book: " + name);
	}

	public void removeChapter(int id) throws IllegalArgumentException {
		for (BookChapter chapter : chapters) {
			if (chapter.getId() == id) {
				chapters.remove(chapter);
				return;
			}
		}
		throw new IllegalArgumentException("No chapter with id=" + id + " exist on book: " + name);
	}

	public Date getCtrlDate() {
	    return ctrlDate;
	}

	public String getCtrlUser() {
	    return ctrlUser;
	}

	public void setCtrlDate(Date modifyDate) {
		this.ctrlDate = modifyDate;
	}

	public void setCtrlUser(String user) {
		this.ctrlUser = user;
	}
}