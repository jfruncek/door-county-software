/*
 * Copyright (C) 2005 Erik Swenson - erik@oreports.com
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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.providers.DateProvider;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.PropertiesProvider;
import org.efs.openreports.providers.PropertiesSupport;
import org.efs.openreports.providers.ProviderException;

import com.opensymphony.xwork2.ActionSupport;

import org.efs.openreports.actions.admin.ActionHelper;


public class EditPropertiesAction extends ActionSupport 
{	
	private static final long serialVersionUID = -2577286917721890875L;

	protected static Logger log = Logger.getLogger(EditPropertiesAction.class);

	private String submitType;

	private String dateFormat;
	private String baseDirectory;
	private String tempDirectory;
	private String mailHost;
	private boolean mailAuthenticatorUsed;
	private String mailUser;
	private String mailPassword;	
    private String mailReplyTo;    
	private int maxRows;
    private int queryReportMaxRowsSortable;
    private Integer queryReportCacheBlockSize;
	private String reportGenerationDirectory;
	
	private int numberOfFiles;
	private String directorySize;
	
	private String runStatusInactivityLogging;       
	private String runStatusInactivityWarning;
	private String runStatusInactivityTimeout;	
    
    private String xmlaUri;
    private String xmlaDataSource;
    private String xmlaCatalog;

    /** Is this a production server? */
    private boolean isProductionServer;

    /** Email address to which all email will go in development/QA environments. */
    private String developmentEmail;
    

    /** Path to which all file output will go in development/QA environments. */
    private String developmentFileRoot; 
    

	private PropertiesProvider propertiesProvider;
	private DateProvider dateProvider;
	private DirectoryProvider directoryProvider;
	private MailProvider mailProvider;


	
	public String execute() {
        try {
            if( isForDisplay() ) {
                buildForDisplay();
                return INPUT;
            } else {
                updateProperties();
                return SUCCESS;
            }
        } catch( Exception e ) {
            ActionHelper.addExceptionAsError( this, e );
            return INPUT;
        }
    }

    private void buildForDisplay() throws ProviderException {
        PropertiesSupport propertiesSupport= new PropertiesSupport(propertiesProvider);
        dateFormat = dateProvider.getDateFormatPattern();				
        baseDirectory = directoryProvider.getReportDirectory();	
        tempDirectory = directoryProvider.getTempDirectory();
        reportGenerationDirectory = directoryProvider.getReportGenerationDirectory();
        
        mailHost = propertiesSupport.getValue( ORProperty.MAIL_SMTP_HOST, mailHost );
        mailAuthenticatorUsed =
                propertiesSupport.getBooleanValue( ORProperty.MAIL_SMTP_AUTH, mailAuthenticatorUsed );
        mailUser = propertiesSupport.getValue( ORProperty.MAIL_AUTH_USER, mailUser );
        mailPassword = propertiesSupport.getValue( ORProperty.MAIL_AUTH_PASSWORD, mailPassword );
        mailReplyTo = propertiesSupport.getValue( ORProperty.MAIL_REPLY_TO, mailReplyTo );
        maxRows = propertiesSupport.getIntValue( ORProperty.QUERYREPORT_MAXROWS, ORProperty.DEFAULT_MAX_ROWS );
        queryReportMaxRowsSortable = propertiesSupport.getIntValue( ORProperty.QUERYREPORT_MAXROWS_SORTABLE,
                ORProperty.DEFAULT_QUERYREPORT_MAX_ROWS_SORTABLE );
        queryReportCacheBlockSize = propertiesSupport.getIntValue( ORProperty.QUERYREPORT_CACHE_BLOCKSIZE,
                ORProperty.DEFAULT_QUERYREPORT_CACHE_BLOCKSIZE );
        runStatusInactivityLogging = propertiesSupport.getValue( ORProperty.RUN_STATUS_INACTIVITY_LOGGING, runStatusInactivityLogging );
        runStatusInactivityWarning = propertiesSupport.getValue( ORProperty.RUN_STATUS_INACTIVITY_WARNING, runStatusInactivityWarning );
        runStatusInactivityTimeout = propertiesSupport.getValue( ORProperty.RUN_STATUS_INACTIVITY_TIMEOUT, runStatusInactivityWarning );
        xmlaCatalog = propertiesSupport.getValue( ORProperty.XMLA_CATALOG, xmlaCatalog );
        xmlaDataSource = propertiesSupport.getValue( ORProperty.XMLA_DATASOURCE, xmlaDataSource );
        xmlaUri = propertiesSupport.getValue( ORProperty.XMLA_URL, xmlaUri ); 
        
        File tempDirFile = new File(directoryProvider.getTempDirectory());
        long size = FileUtils.sizeOfDirectory(tempDirFile);			
        directorySize = FileUtils.byteCountToDisplaySize(size);	
        numberOfFiles = tempDirFile.listFiles().length;				
        
        isProductionServer = propertiesSupport.getBooleanValue( ORProperty.IS_PROD_SERVER, isProductionServer );
        developmentEmail = propertiesSupport.getValue( ORProperty.DEVELOPMENT_EMAIL, developmentEmail ); 
        developmentFileRoot = propertiesSupport.getValue( ORProperty.DEVELOPMENT_FILE_ROOT, developmentFileRoot );
    }
    
    private void updateProperties() throws ProviderException {
        PropertiesSupport propertiesSupport= new PropertiesSupport(propertiesProvider);
        propertiesSupport.setProperty(ORProperty.BASE_DIRECTORY, baseDirectory);
        if (baseDirectory != null) directoryProvider.setReportDirectory(baseDirectory);
        
        propertiesSupport.setProperty(ORProperty.TEMP_DIRECTORY, tempDirectory);
        if (tempDirectory != null) directoryProvider.setTempDirectory(tempDirectory);
        
        propertiesSupport.setProperty(ORProperty.REPORT_GENERATION_DIRECTORY, reportGenerationDirectory);
        if (tempDirectory != null) directoryProvider.setReportGenerationDirectory(reportGenerationDirectory);
        
        propertiesSupport.setProperty(ORProperty.DATE_FORMAT, dateFormat);
        if (dateFormat != null) dateProvider.setDateFormat(dateFormat);
        
        propertiesSupport.setProperty(ORProperty.MAIL_AUTH_PASSWORD, mailPassword);
        if (mailPassword != null) mailProvider.setPassword(mailPassword);

        propertiesSupport.setProperty(ORProperty.MAIL_REPLY_TO, mailReplyTo);
        
        propertiesSupport.setProperty(ORProperty.MAIL_AUTH_USER, mailUser);
        if (mailUser !=null) mailProvider.setUserName(mailUser);
        
        propertiesSupport.setProperty(ORProperty.MAIL_SMTP_AUTH, String.valueOf(mailAuthenticatorUsed));
        mailProvider.setUseMailAuthenticator(mailAuthenticatorUsed);
        
        propertiesSupport.setProperty(ORProperty.MAIL_SMTP_HOST, mailHost);
        if (mailHost != null) mailProvider.setMailHost(mailHost);
       
                                       
        propertiesSupport.setIntProperty(ORProperty.QUERYREPORT_MAXROWS, maxRows);
        propertiesSupport.setIntProperty(ORProperty.QUERYREPORT_MAXROWS_SORTABLE, queryReportMaxRowsSortable);
        propertiesSupport.setIntProperty(ORProperty.QUERYREPORT_CACHE_BLOCKSIZE, queryReportCacheBlockSize);
        propertiesSupport.setProperty(ORProperty.RUN_STATUS_INACTIVITY_LOGGING, runStatusInactivityLogging);
        propertiesSupport.setProperty(ORProperty.RUN_STATUS_INACTIVITY_WARNING, runStatusInactivityWarning);
        propertiesSupport.setProperty(ORProperty.RUN_STATUS_INACTIVITY_TIMEOUT, runStatusInactivityTimeout);
        

        propertiesSupport.setProperty(ORProperty.XMLA_CATALOG, xmlaCatalog);
        propertiesSupport.setProperty(ORProperty.XMLA_DATASOURCE, xmlaDataSource);
        propertiesSupport.setProperty(ORProperty.XMLA_URL, xmlaUri);
    }

    private boolean isForDisplay() {
        return submitType == null;
    }

	public String getSubmitType()
	{
		return submitType;
	}

	public void setSubmitType(String submitType)
	{
		this.submitType = submitType;
	}

	public String getBaseDirectory()
	{
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory)
	{
		this.baseDirectory = baseDirectory;
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public boolean isMailAuthenticatorUsed()
	{
		return mailAuthenticatorUsed;
	}

	public void setMailAuthenticatorUsed(boolean mailAuthenticatorUsed)
	{
		this.mailAuthenticatorUsed = mailAuthenticatorUsed;
	}

	public String getMailHost()
	{
		return mailHost;
	}

	public void setMailHost(String mailHost)
	{
		this.mailHost = mailHost;
	}

	public String getMailPassword()
	{
		return mailPassword;
	}

	public void setMailPassword(String mailPassword)
	{
		this.mailPassword = mailPassword;
	}
	
    public String getMailReplyTo()
    {
        return mailReplyTo;
    }

    public void setMailReplyTo(String mailReplyTo)
    {
        this.mailReplyTo = mailReplyTo;
    }
	

	public String getMailUser()
	{
		return mailUser;
	}

	public void setMailUser(String mailUser)
	{
		this.mailUser = mailUser;
	}	

	public void setPropertiesProvider(PropertiesProvider propertiesProvider)
	{
		this.propertiesProvider = propertiesProvider;
	}

	public void setDateProvider(DateProvider dateProvider)
	{
		this.dateProvider = dateProvider;
	}
	
	public void setDirectoryProvider(DirectoryProvider directoryProvider)
	{
		this.directoryProvider = directoryProvider;
	}

	public void setMailProvider(MailProvider mailProvider)
	{
		this.mailProvider = mailProvider;
	}

	public String getTempDirectory()
	{
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}

	public int getMaxRows()
	{
		return maxRows;
	}

	public void setMaxRows(int maxRows)
	{
		this.maxRows = maxRows;
	}

	public String getDirectorySize()
	{
		return directorySize;
	}

	public int getNumberOfFiles()
	{
		return numberOfFiles;
	}

	public String getReportGenerationDirectory()
	{
		return reportGenerationDirectory;
	}

	public void setReportGenerationDirectory(String reportGenerationDirectory)
	{
		this.reportGenerationDirectory = reportGenerationDirectory;
	}
    
    public String getXmlaCatalog() 
    {
        return xmlaCatalog;
    }
    
    public void setXmlaCatalog(String xmlaCatalog) 
    {
        this.xmlaCatalog = xmlaCatalog;
    }
    
    public String getXmlaDataSource() 
    {
        return xmlaDataSource;
    }
    
    public void setXmlaDataSource(String xmlaDataSource) 
    {
        this.xmlaDataSource = xmlaDataSource;
    }
    
    public String getXmlaUri() 
    {
        return xmlaUri;
    }
    
    public void setXmlaUri(String xmlaUri)
    {
        this.xmlaUri = xmlaUri;
    }

    public boolean isProductionServer() {
        return isProductionServer;
    }

    public void setProductionServer( boolean isProductionServer ) {
        this.isProductionServer = isProductionServer;
    }

    
    public String getDevelopmentEmail( ) {
        return this.developmentEmail;
    }

    public void setDevelopmentEmail( String developmentEmail ) {
        this.developmentEmail = developmentEmail;
    }

    public String getDevelopmentFileRoot( ) {
        return this.developmentFileRoot;
    }
    
    public void setDevelopmentFileRoot( String developmentFileRoot ) {
        this.developmentFileRoot = developmentFileRoot;
    }

    public String getRunStatusInactivityLogging() {
        return runStatusInactivityLogging;
    }

    public void setRunStatusInactivityLogging( String runStatusInactivityLogging ) {
        this.runStatusInactivityLogging = runStatusInactivityLogging;
    }
    
    public String getRunStatusInactivityWarning() {
        return runStatusInactivityWarning;
    }

    public void setRunStatusInactivityWarning( String runStatusInactivityWarning ) {
        this.runStatusInactivityWarning = runStatusInactivityWarning;
    }
    
    public String getRunStatusInactivityTimeout() {
        return runStatusInactivityTimeout;
    }

    public void setRunStatusInactivityTimeout( String runStatusInactivityTimeout ) {
        this.runStatusInactivityTimeout = runStatusInactivityTimeout;
    }

    
    public void setQueryReportMaxRowsSortable( int queryReportMaxRowsSortable ) {
        this.queryReportMaxRowsSortable = queryReportMaxRowsSortable;
    }

    public int getQueryReportMaxRowsSortable() {
        return queryReportMaxRowsSortable;
    }

    public void setQueryReportCacheBlockSize( Integer queryReportCacheBlockSize ) {
        this.queryReportCacheBlockSize = queryReportCacheBlockSize;
    }

    public Integer getQueryReportCacheBlockSize() {
        return queryReportCacheBlockSize;
    }

}