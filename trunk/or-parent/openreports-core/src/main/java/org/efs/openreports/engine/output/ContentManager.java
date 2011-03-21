package org.efs.openreports.engine.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.efs.openreports.util.IoU;
import org.efs.openreports.util.Releasable;

/**
 * Manages content output. Overflows to a temp file if the size of the content is too large.
 * 
 * @author mconner
 * 
 */
public class ContentManager extends OutputStream implements Releasable {
    protected static Logger log = Logger.getLogger( ContentManager.class.getName() );
    public static final int IN_MEM_THRESHOLD = 1000000; // 1MB
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    boolean closed = false;
    File overflowFile = null;
    OutputStream overflowOutputStream = null;
    private int size = 0;

    /**
     * To be called when done with the ContentStreamManager. The primary reason is to free up the
     * temporary overflow file, if necessary.
     */
    public void release() {
        log.info( "in ContentManager.release, for " + makeIdName() );
        IoU.I.safeDelete( overflowFile, "ContentManager" );
    }

    private String makeIdName() {
        return( ( overflowFile == null ) ? "memory stream" : overflowFile.getName() );
    }

    @Override
    public void close() throws IOException {
        if( !closed ) {
            if( baos != null )
                baos.close();
            if( overflowOutputStream != null )
                overflowOutputStream.close();
            closed = true;
        }
    }

    @Override
    public void flush() throws IOException {
        getUnderlyingOutputStream().flush();
    }

    /** @ return an input stream. you are responsible for closing it. */
    public InputStream createInputStream() throws IOException {
        close();
        if( baos != null ) {
            log.info( "in ContentManager.createInputStream, for  memory stream" );
            return new ByteArrayInputStream( baos.toByteArray() );
        } else {
            log.info( "in ContentManager.createInputStream, for  " + overflowFile.getPath() );
            return new BufferedInputStream( new FileInputStream( overflowFile ) );
        }
    }

    @Override
    public void write( byte[] bytes ) {
        size += bytes.length;
        try {
            getUnderlyingOutputStream().write( bytes );
        } catch( IOException e ) {
            throw new RuntimeException( "unexpected error writting to buffer", e );
        }
    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
        size += len;
        try {
            getUnderlyingOutputStream().write( b, off, len );
        } catch( IOException e ) {
            throw new RuntimeException( "unexpected error writting to buffer", e );
        }
    }

    @Override
    public void write( int b ) {
        ++size;
        try {
            getUnderlyingOutputStream().write( b );
        } catch( IOException e ) {
            throw new RuntimeException( "unexpected error writting to buffer", e );
        }
    }

    private void createOverflowOutputStream() throws IOException {
        overflowFile = java.io.File.createTempFile( "ReportEngineContent", null );
        overflowFile.deleteOnExit(); // just in case.
        FileOutputStream fos = new FileOutputStream( overflowFile );
        overflowOutputStream = new BufferedOutputStream( fos );
    }

    private OutputStream getUnderlyingOutputStream() throws IOException {
        validate();
        if( size < IN_MEM_THRESHOLD ) {
            return baos;
        } else {
            if( overflowOutputStream == null ) {
                createOverflowOutputStream();
                baos.flush();
                overflowOutputStream.write( baos.toByteArray() );
                baos = null;
            }
            return overflowOutputStream;
        }
    }

    private void validate() throws IOException {
        if( closed ) {
            throw new IOException( "attempted to write to a closed stream " );
        }
    }

    /**
     * Used for "small" content types. It would be better to write to the content stream.
     * 
     * @param bytes
     */
    public void setContent( byte[] bytes ) {
        write( bytes );
    }

    public byte[] getContentAsBytes() {
        InputStream is = null;
        try {
            is = createInputStream();
            byte[] bytes = new byte[getSize()];
            int read = is.read( bytes );
            if( read != getSize() ) {
                log.error( "Bytes read, " + read + " does not match size, " + getSize() );
            }
            return bytes;
        } catch( IOException ioe ) {
            log.error( "Cannot write content", ioe );
            return new byte[0];
        } finally {
            IoU.I.safeClose( is, makeIdName() );
        }

    }

    public int getSize() {
        return size;
    }

    public void copyToStream( OutputStream outputStream ) throws IOException {
        InputStream inputStream = createInputStream();
        try {
            IoU.I.copy( inputStream, outputStream );
        } finally {
            IoU.I.safeClose( inputStream, makeLogId() );
        }
    }

    private String makeLogId() {
        return "Content Manager" + ( ( overflowFile == null ) ? "" : overflowFile.getAbsolutePath() );
    }

}
