package org.efs.openreports.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Utils for input/output
 * 
 * @author mconner
 * 
 */
public class IoU {
    public static final IoU I = new IoU();
    protected static final int READ_BUF_SIZE = 8196;
    protected static Logger LOG = Logger.getLogger( IoU.class.getName() );

    public int copy( InputStream source, OutputStream dest ) throws IOException {
        byte[] buffer = new byte[READ_BUF_SIZE];
        int readCount;
        int totalCount = 0;
        while( ( readCount = source.read( buffer, 0, READ_BUF_SIZE ) ) >= 0 ) {
            dest.write( buffer, 0, readCount );
            totalCount += readCount;
        }
        return totalCount;
    }

    public void safeClose( InputStream fis, String logId ) {
        if( fis != null ) {
            try {
                fis.close();
            } catch( IOException ioe ) {
                LOG.warn( "Can't close InputStream of type: " + fis.getClass().getName() + " identified by " + logId,
                        ioe );
            }
        }
    }

    public void safeFlush( OutputStream fos, String logId ) {
        if( fos != null ) {
            try {
                fos.flush();
            } catch( IOException ioe ) {
                LOG.warn( "Can't flush OutputStream of type: " + fos.getClass().getName() + " identified by " + logId,
                        ioe );
            }
        }
    }

    public void safeClose( OutputStream fos, String logId ) {
        if( fos != null ) {
            try {
                fos.close();
            } catch( IOException ioe ) {
                LOG.warn( "Can't close OutputStream of type: " + fos.getClass().getName() + " identified by " + logId,
                        ioe );
            }
        }
    }

    public void safeDelete( File file, String logId ) {
        if( file != null ) {
            try {
                if( !file.delete() ) {
                    LOG.warn( "could not delete file: " + file.getPath() + " identified by: " + logId );
                }
            } catch( Exception ex ) {
                LOG.warn( "Error attempting to delete file: " + file.getPath() + " identified by: " + logId );
            }
        }
    }

    public void safeFlushAndClose( OutputStream outputStream, String logId ) {
        safeFlush( outputStream, logId );
        safeClose( outputStream, logId );
    }

}
