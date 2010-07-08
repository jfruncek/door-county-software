package org.efs.openreports;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UnitTestSupport {

    static public String loadResourceAsString( Class<?> clazz, String resourceName ) throws IOException {
        InputStream inStream = clazz.getResourceAsStream( resourceName );
        StringBuffer buff = new StringBuffer();
        InputStreamReader reader = new InputStreamReader( inStream );
        char[] charBuf = new char[1024];
        for( int readCount = reader.read( charBuf ); readCount >= 0; readCount = reader.read( charBuf ) ) {
            buff.append( charBuf, 0, readCount );
        }
        return buff.toString();
    }

}
