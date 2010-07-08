package org.efs.openreports.engine.javareport;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.starkinvestments.rpt.common.ReportSink;

/**
 * Memory Based sink of report output data.
 * 
 * @author mconner
 */
public class MemReportSink implements ReportSink {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String contentType;

    @Override
    public OutputStream getOutputStream() {
        return baos;
    }

    @Override
    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return baos.toByteArray();
    }

    public String getContentType() {
        return contentType;
    }

}
