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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;

import org.apache.log4j.Logger;
import org.efs.openreports.ORStatics;
import org.efs.openreports.ReportConstants;
import org.efs.openreports.ReportConstants.ExportType;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.DeliveredReport;
import org.efs.openreports.objects.MailMessage;
import org.efs.openreports.objects.ReportDeliveryLog;
import org.efs.openreports.objects.ReportSchedule;
import org.efs.openreports.objects.ReportUser;
import org.efs.openreports.providers.MailProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.ByteArrayDataSource;
import org.efs.openreports.util.ReportEngineOutputDataSource;


public class EMailDeliveryMethod implements DeliveryMethod 
{
    protected static Logger log = Logger.getLogger(EMailDeliveryMethod.class.getName());
    
    private MailProvider mailProvider;

    private DeliverySupport deliverySupport;
    
    
    public List<ReportDeliveryLog> deliverReport( ReportSchedule reportSchedule, ReportEngineInput reportInput,
            ReportEngineOutput reportOutput )  {
        ArrayList<DataSource> htmlImageDataSources = new ArrayList<DataSource>();

        DataSource byteArrayDataSource = exportReport( reportOutput, reportInput, reportSchedule, htmlImageDataSources );

        List<String> recipients = MailMessage
                .parseAddressList( deliverySupport.getEffectiveEmailRecipients( reportSchedule.getRecipients() ) );

        List<ReportDeliveryLog> deliveryLogEntries = new ArrayList<ReportDeliveryLog>();
        for( String recipient : recipients ) {
            deliveryLogEntries.add( sendToOneRecipient( byteArrayDataSource, htmlImageDataSources, recipient,
                    reportSchedule, reportInput, reportOutput ) );
        }
        return deliveryLogEntries;
    }
    
    private ReportDeliveryLog sendToOneRecipient(DataSource dataSource, 
            ArrayList<DataSource> htmlImageDataSources, String recipient, ReportSchedule reportSchedule, ReportEngineInput reportEngineInput, ReportEngineOutput reportEngineOutput) {
        
        ReportDeliveryLog deliveryLogEntry = new ReportDeliveryLog(ReportConstants.DeliveryMethod.EMAIL.getName(), recipient );
        
        MailMessage mail = new MailMessage();               
        mail.setDataSource(dataSource);
        mail.addHtmlImageDataSources(htmlImageDataSources);          
        mail.setSender(deliverySupport.getEmailSender());
        mail.parseRecipients( recipient );
        mail.parseReplyTos( deliverySupport.getReplyTos() );
        mail.setBounceAddress(reportSchedule.getDeliveryReturnAddress());
        mail.setSubject( deliverySupport.getEmailSubjectLine( reportSchedule, reportEngineInput, reportEngineOutput ) );
        if (reportSchedule.getExportType() != ExportType.HTML.getCode())
        {
            mail.setText( buildMailText(reportSchedule, reportEngineInput) );
        }

        try
        {
            mailProvider.sendMail(mail);
            deliveryLogEntry.markSuccess(  );
        }
        catch(ProviderException pe)
        {
            deliveryLogEntry.markFailure( "Mail provider exception", pe);
            log.error( deliveryLogEntry.toString(), pe );
        }
        return deliveryLogEntry;
    }

    private String buildMailText( ReportSchedule reportSchedule, ReportEngineInput reportInput ) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( baos );
        writer.println( reportSchedule.getReport().getName() + ": Generated on " + new Date() );

        writer.println();
        writer.println( "Parameters: " );
        Map<String, Object> reportParameters = reportInput.getParameters();
        List<String> keys = new ArrayList<String>( reportParameters.keySet() );
        Collections.sort( keys );
        boolean hasParameters = false;
        for( String parameterName : keys ) {
            if( !parameterName.startsWith( ( ORStatics.OPERREPORTS_PARAM_PREFIX ) ) ) {
                Object value = reportParameters.get( parameterName );
                if( value != null && value.getClass().isArray() ) {
                    StringBuffer buf = new StringBuffer();
                    Object[] values = (Object[]) value;
                    for( int i = 0; i < values.length; i++ ) {

                        if( i > 0 ) {
                            buf.append( ", " );
                        }
                        buf.append( values[i] );
                    }
                    value = buf.toString();
                }
                writer.println( "   " + parameterName + ": " + value );
                hasParameters = true;
            }
        }
        
        if (!hasParameters) {
            writer.println("   There are no parameters for this report.");
        }
        
        writer.println();
        writer.println();
        writer.println("If you require assistance regarding this report please contact IT - Reporting by replying to this email" +
                "(Alternately, forward to sbalantrapu@teramedica.com)" );
        deliverySupport.writeDevelopmentEmailNoticeIfNeeded( writer, reportSchedule.getRecipients() );
        writer.flush();
        writer.close();
        return baos.toString();
    }

    public byte[] getDeliveredReport(DeliveredReport deliveredReport) throws DeliveryException 
    {        
        throw new DeliveryException("Method getDeliveredReport not implemented by EMailDeliveryMethod");
    }

    public DeliveredReport[] getDeliveredReports(ReportUser user) throws DeliveryException 
    {        
        throw new DeliveryException("Method getDeliveredReports not implemented by EMailDeliveryMethod");
    }
    
    protected DataSource exportReport( ReportEngineOutput reportOutput, ReportEngineInput reportInput,
            ReportSchedule reportSchedule, ArrayList<DataSource> htmlImageDataSources ) {
        String reportName = deliverySupport.getEmailAttachmentName( reportSchedule, reportInput, reportOutput );
        DataSource dataSource = new ReportEngineOutputDataSource( reportOutput, reportName );
        if( reportSchedule.getExportType() == ExportType.HTML.getCode() ) {
            Map<String, byte[]> imageMap = getImageMap( reportOutput );

            for( Map.Entry<String, byte[]> entry : imageMap.entrySet() ) {
                ByteArrayDataSource imageDataSource =
                        new ByteArrayDataSource( entry.getValue(), getImageContentType( entry.getValue() ) );
                imageDataSource.setName( (String) entry.getKey() );
                htmlImageDataSources.add( imageDataSource );
            }
        }
        return dataSource;
    }

    @SuppressWarnings("unchecked")
    private Map<String, byte[]> getImageMap( ReportEngineOutput reportOutput ) {
        return (Map<String, byte[]>)reportOutput.getSupplementaryContent().get(ORStatics.IMAGES_MAP);
    }
    
    /**
     * Try to figure out the image type from its bytes.
     */
    private String getImageContentType(byte[] bytes)
    {
        String header = new String(bytes, 0, (bytes.length > 100) ? 100 : bytes.length);
        if (header.startsWith("GIF"))
        {
            return "image/gif";
        }

        if (header.startsWith("BM"))
        {
            return "image/bmp";
        }

        if (header.indexOf("JFIF") >= 0)
        {
            return "image/jpeg";
        }

        if (header.indexOf("PNG") >= 0)
        {
            return "image/png";
        }

        // We are out of guesses, so just guess tiff
        return "image/tiff";
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
