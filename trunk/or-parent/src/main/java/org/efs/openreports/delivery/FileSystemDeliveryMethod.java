/*
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
package org.efs.openreports.delivery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.ReportConstants;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.DeliveredReport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDeliveryLog;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.IoU;

import com.thoughtworks.xstream.XStream;

/**
 * Delivers the file to the file system in a single location, but also e-mails the recipients with a notification email.
 */
public class FileSystemDeliveryMethod implements DeliveryMethod
{  
    protected static Logger log = Logger.getLogger(FileSystemDeliveryMethod.class.getName());
    
    private MailProvider mailProvider;
    private DirectoryProvider directoryProvider;
    private DeliverySupport deliverySupport;

    
    public List<ReportDeliveryLog> deliverReport(ReportSchedule reportSchedule, ReportEngineInput reportInput, ReportEngineOutput reportOutput)
    {
        
        Report report = reportSchedule.getReport();
        ReportUser user = reportSchedule.getUser();
        
        Date runDate = new Date();
        
        String fileName = runDate.getTime() + "-"
                + StringUtils.deleteWhitespace(user.getName()) + "-"
                + StringUtils.deleteWhitespace(report.getName());                       
        
        try {
            writeReportFile( reportOutput, fileName );
        } catch( IOException ioe ) {
            ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog( ReportConstants.DeliveryMethod.FILE.getName(), fileName);
            deliveryLogEntry.markFailure( "cant write file", ioe );
            return Collections.singletonList( deliveryLogEntry );
        }        
        
        DeliveredReport info = new DeliveredReport();             
        info.setParameters(reportInput.getParameters());
        info.setReportDescription(reportSchedule.getScheduleDescription());
        info.setReportName(report.getName());
        String fileNameWithExtension = fileName + reportOutput.getContentExtension(); 
        info.setReportFileName(fileNameWithExtension);
        info.setRunDate(runDate);
        info.setUserName(user.getName());
        info.setDeliveryMethod("fileSystemDeliveryMethod");
        
        try
        {
            FileOutputStream file = new FileOutputStream(directoryProvider.getReportGenerationDirectory() + fileName + ".xml");

            XStream xStream = new XStream();
            xStream.alias("reportGenerationInfo", DeliveredReport.class);
            xStream.toXML(info, file);
        
            file.flush();
            file.close();
        }
        catch(IOException ioe)
        {
            ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog(ReportConstants.DeliveryMethod.FILE.getName(), fileName);
            deliveryLogEntry.markFailure( "cannot write reportGenerationInfo for report file", ioe);            
            return Collections.singletonList( deliveryLogEntry );
        }
        
        
        List<String> recipients = MailMessage
                .parseAddressList( deliverySupport.getEffectiveEmailRecipients( reportSchedule.getRecipients() ) );
        
        List<ReportDeliveryLog> deliveryLogEntries = new ArrayList<ReportDeliveryLog>();
        for( String recipient : recipients ) {
            deliveryLogEntries.add( sendToOneRecipient( recipient, fileNameWithExtension, reportSchedule, reportInput, reportOutput ) );
        }
        return deliveryLogEntries;
        
        
        
    }

    private void writeReportFile( ReportEngineOutput reportOutput, String fileName ) throws FileNotFoundException,
            IOException {
        FileOutputStream fos =
                new FileOutputStream( directoryProvider.getReportGenerationDirectory() + fileName
                        + reportOutput.getContentExtension() );
        try {
            reportOutput.getContentManager().copyToStream( fos );
        } finally {
            IoU.I.safeFlushAndClose( fos, fileName );
        }
    }
    
    private ReportDeliveryLog sendToOneRecipient( String recipient, String fileName, ReportSchedule reportSchedule,
            ReportEngineInput reportInput, ReportEngineOutput reportOutput ) {
        MailMessage mail = new MailMessage();               
        mail.setSender(deliverySupport.getEmailSender());        
        mail.parseRecipients(deliverySupport.getEffectiveEmailRecipients(reportSchedule.getRecipients()));
        mail.parseReplyTos( deliverySupport.getReplyTos() );
        
        mail.setText(buildMailText(reportSchedule, fileName ) );
        
        mail.setBounceAddress(reportSchedule.getDeliveryReturnAddress());
        mail.setSubject(deliverySupport.getDescription( reportSchedule, reportInput, reportOutput));
        
        ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog(ReportConstants.DeliveryMethod.FILE.getName(), fileName + ", " + recipient);
        try
        {
            mailProvider.sendMail(mail);    
            deliveryLogEntry.markSuccess(  );
        }
        catch(ProviderException pe)
        {
            deliveryLogEntry.markFailure( "cant send notification email", pe);            
        }
        return deliveryLogEntry;
    }
   
    private String buildMailText( ReportSchedule reportSchedule, String fileNameWithExtension ) {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        PrintWriter writer = new PrintWriter(baos);
        writer.println( reportSchedule.getReport().getName() + ": Generated on " + new Date() );
        writer.println( );
        writer.println( "File: " + fileNameWithExtension );
        writer.println( );
        
        writer.print( "The report can be viewed from: ");
        writer.print( "URL: http://");
        writer.print( deliverySupport.getBaseURLString() );
        
        // the following doesn't work with current authentication mechanism
        // buf.append( "/report-files?fileName=" ).append( fileNameWithExtension );
        
		deliverySupport.writeDevelopmentEmailNoticeIfNeeded( writer, reportSchedule.getRecipients() );
        
        writer.flush();
        writer.close();
        return baos.toString();
    }

    public DeliveredReport[] getDeliveredReports(ReportUser user) throws DeliveryException
    {        
        IOFileFilter extensionFilter = FileFilterUtils.suffixFileFilter("xml");
        
        File directory = new File(directoryProvider.getReportGenerationDirectory());

        ArrayList<DeliveredReport> deliveredReports = new ArrayList<DeliveredReport>();

        Iterator<File> iterator = iterateFiles( extensionFilter, directory );
        while (iterator.hasNext())
        {
            File file = iterator.next();

            if (FilenameUtils.wildcardMatch(file.getName(), "*" + user.getName() + "*"))
            {
                XStream xStream = new XStream();
                xStream.alias("reportGenerationInfo", DeliveredReport.class);
                
                try
                {
                    FileInputStream inputStream = new FileInputStream(file);
            
                    DeliveredReport report = (DeliveredReport) xStream.fromXML(inputStream);                    
            
                    deliveredReports.add(report);
            
                    inputStream.close();
                }
                catch(IOException io)
                {
                    log.warn(io.toString());
                }
            }
        }   
        
        DeliveredReport[] reports = new DeliveredReport[deliveredReports.size()];
        deliveredReports.toArray(reports);
        
        return reports;
    }

    @SuppressWarnings("unchecked")
    private Iterator<File> iterateFiles( IOFileFilter extensionFilter, File directory ) {
        return (Iterator<File>) FileUtils.iterateFiles(directory, extensionFilter, null);
    }    

    public byte[] getDeliveredReport(DeliveredReport deliveredReport) throws DeliveryException
    {
        try
        {
            File file = new File(directoryProvider.getReportGenerationDirectory() + deliveredReport.getReportFileName());        
            return FileUtils.readFileToByteArray(file);
        }
        catch(IOException ioe)
        {
            throw new DeliveryException(ioe);
        }        
    }

    public void setDirectoryProvider(DirectoryProvider directoryProvider)
    {
        this.directoryProvider = directoryProvider;
    }
    
    public void setMailProvider(MailProvider mailProvider) 
    {
        this.mailProvider = mailProvider;
    }
    
    public void setDeliverySupport(DeliverySupport deliverySupport) 
    {
        this.deliverySupport = deliverySupport;
    }   
}
