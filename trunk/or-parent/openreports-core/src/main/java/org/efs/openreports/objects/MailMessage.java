/*
 * Copyright (C) 2003 Erik Swenson - erik@oreports.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *  
 */

package org.efs.openreports.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataSource;

public class MailMessage implements Serializable
{
	private static final long serialVersionUID = -5816798771288286268L;
	public static final String EMAIL_SEPARATORS = "\t\n\r\f;,|";

	private String sender;
	private String subject;
	private String text;
    private String bounceAddress;

	private List<String> recipients = new ArrayList<String>();
	private List<String> attachments = new ArrayList<String>();
	private List<DataSource> htmlImageDataSources = new ArrayList<DataSource>();
    private List<String> replyTos = new ArrayList<String>();

	private DataSource dataSource;

	public MailMessage()
	{
	}

	public String getSender()
	{
		return sender;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public List<String> getRecipients()
	{
		return recipients;
	}

    public List<String> getReplyTos()
    {
        return replyTos;
    }
	
	public void setRecipients(ArrayList<String> recipients)
	{
		this.recipients = recipients;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public List<String> getAttachments()
	{
		return attachments;
	}

	public void addAttachment(String fileName)
	{
		attachments.add(fileName);
	}

	public void addRecepient(String recipient)
	{
		recipients.add(recipient);
	}
	
    public void addReplyTo(String recipient)
    {
        replyTos.add(recipient);
    }

	public String formatRecipients(String delimiter)
	{
		String addresses = "";

		for (int i = 0; i < recipients.size(); i++)
		{
			addresses += recipients.get(i) + delimiter;
		}

		return addresses.substring(0, addresses.length() - 1);
	}
	
    public static List<String> parseAddresses(String delimitedString) {
        return parseAddressList(delimitedString);
    }
    
    public static List<String> parseAddressList(String delimitedString)
    {
        StringTokenizer st = new StringTokenizer(delimitedString, EMAIL_SEPARATORS);
        ArrayList<String>addresses = new ArrayList<String>();
        while (st.hasMoreElements())
        {
            addresses .add(st.nextToken());
        }
        return addresses;
    }
	

	public void parseRecipients(String value)
	{
	    recipients = parseAddresses(value);
	}
	
    public void parseReplyTos(String value)
    {
        replyTos = parseAddresses(value);
    }


	public List<DataSource> getHtmlImageDataSources()
	{
		return htmlImageDataSources;
	}

	public void addHtmlImageDataSources(ArrayList<DataSource> htmlImageDataSources)
	{
		this.htmlImageDataSources.addAll(htmlImageDataSources);
	}
    
    public String getBounceAddress() 
    {
        return bounceAddress;
    }
    
    public void setBounceAddress(String bounceAddress) 
    {
        this.bounceAddress = bounceAddress;
    }

}