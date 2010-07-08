package org.efs.openreports.delivery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * provides some basic IO utilities
 * 
 * @author mconner TODO: consider using commons IO.
 */
public class IOUtil {

    /**
     * Creates a new file and copies the contents of the file to it. 
     * @param inputStream
     * @param outputFileName
     * @return the number of bytes written to the new file 
     * @throws IOException
     */
    public static int copyToFile( InputStream inputStream, String outputFileName ) throws IOException {
        File outputFile = new File( outputFileName );
        if( outputFile.createNewFile() ) {
            return copyToFile( inputStream, outputFile );
        } else {
            return -1;
        }
    }

    public static int copyToFile( InputStream is, File outputFile ) throws IOException {
        FileOutputStream fos = new FileOutputStream( outputFile );
        int copyCount = copy( is, fos );
        fos.flush();
        fos.close();
        return copyCount;
    }

    public static int copy( InputStream is, OutputStream os ) throws IOException {
        byte[] buffer = new byte[1000];
        int readCount;
        int totalCount = 0;
        while( ( readCount = is.read( buffer, 0, 1000 ) ) >= 0 ) {
            os.write( buffer, 0, readCount );
            totalCount += readCount;
        }
        return totalCount;
    }

    /**
     * Attempts to ensure that a path to a parent directory exists. 
     * @param fullPathName
     * @return a File representing the parent directory.
     */
    public static File ensureParentDirExists( String fullPathName ) {
        File file = new File( fullPathName );
        File absoluteFile = file.getAbsoluteFile();
        File parentDir = absoluteFile.getParentFile();
        parentDir.mkdirs();
        return parentDir;
    }
    
    /**
     * Creates an alternate file name for a base path name.
     * 
     * @param pathName
     * @param parentDir
     * @return a File that represents a unique file name that at the time of the call does not yet exist.
     * @throws IOException
     */
    public static String appendToFileName( String path, String suffix) throws IOException {
        String base = stripExtension(path);
        String extension = getExtension(path);
        return base + suffix + extension;
    }

    /**
     * removes any extension and preceding dot.
     * @param originalFileName
     * @return
     */
    public static String stripExtension( String path ) {
        int lastDot = path.lastIndexOf( '.'  );
        int lastSep = path.lastIndexOf( File.separatorChar  );
        return (lastDot > lastSep ) ? path.substring( 0, lastDot ) : path;
    }
    
    /**
     * @param originalFileName
     * @return the extension, with .  (e.g. ".pdf")
     */
    public static String getExtension( String path ) {
        String base = path.substring( path.lastIndexOf( File.separatorChar ) + 1 );
        int extensionStart = base.lastIndexOf( '.' );
        return (extensionStart  < 0) ? "" : base.substring( extensionStart );
    }
    
}
