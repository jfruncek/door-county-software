/*
 * scjp.fileio.FileWriterTest.java
 * Created on Oct 15, 2010 by jfruncek
 * Copyright 2010, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package scjp.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Compares performance (memory and speed) of various Java I/O classes. 
 * 
 * @author jfruncek
 */
public class FileIOTest extends TestCase {

    private static final int NUMBER_OF_WRITES = 1000;
    private static final String TEST_FILE_NAME =  "FileWriterTest.txt";
    private static final String CENT_CHARS =  "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    private long time;
    private FileWriter fw;
    private File file;
    private BufferedWriter bw;
    private FileReader fr;
    private BufferedReader br;
    private char[] buffer = new char[101];
    
    @Override
    protected void setUp() throws Exception {
        file = new File(TEST_FILE_NAME);
        if ( file.exists() ) {
            file.delete();
        }
        file.createNewFile();
        fw = new FileWriter(file);
        bw = new BufferedWriter(fw);
        fr = new FileReader(file);
        br = new BufferedReader(fr); 
    }

    protected void tearDown() {
        file.delete();
    }

    private void snapTime() {
        time = System.nanoTime();
    }
    
    private void snapTime(String operation) {
        float delta = (float)(System.nanoTime() - time) / 1000000;
        System.out.printf("Operation: " + operation + " took %10.6f ms\n", delta);
    }

    public void testIOUsingFileWriter() {
        snapTime();
        try {
            for (int i = 0; i < NUMBER_OF_WRITES; i++) {
                fw.write(CENT_CHARS);
            }
            fw.flush();
            snapTime("FileWriter: ");
            StringBuilder sb = new StringBuilder();
            snapTime();
            int ch;
            do {
                if ( (ch = fr.read()) != -1 ) {
                    sb.append((char) ch);
                }
            } while ( ch != -1 );
            snapTime("FileReader: ");
            assertEquals("Size of file read back same as written out", CENT_CHARS.length() * NUMBER_OF_WRITES, sb.length());
        }
        catch (IOException e) {
            fail("Cannot create test file");
        }
    }

    public void testIOUsingBufferedWriter() {
        snapTime();
        try {
            for (int i = 0; i < NUMBER_OF_WRITES; i++) {
                bw.write(CENT_CHARS);
            }
            bw.flush();
            snapTime("BufferedWriter: ");
            StringBuilder sb = new StringBuilder();
            snapTime();
            int charsRead;
            int numChars = 0;
            do {
                if ((charsRead = br.read(buffer)) != -1) {
                    numChars += charsRead;
//                    System.out.println("Chars read: " + charsRead + " Total: " + numChars);
                    sb.append(String.valueOf(buffer).substring(0, charsRead));
                }
            } while ( charsRead != -1 );
            snapTime("BufferedReader: ");
            assertEquals("Size of file read back same as written out", CENT_CHARS.length() * NUMBER_OF_WRITES, sb.length());
        }
        catch (IOException e) {
            fail("Cannot create test file");
        }
    }
}
