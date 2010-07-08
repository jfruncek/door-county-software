package org.efs.openreports.providers.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.efs.openreports.providers.DirectoryProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.providers.XlsxExportProvider;
import org.efs.openreports.util.DisplayProperty;
import org.efs.openreports.util.IoU;

/**
 * Exports to Excel xlsx format. Because the XML for xlsx is so verbose, we were getting out of
 * memory exceptions running larger reports. Solution is to build the data to a file and stream that
 * back into the workbook when writing back to the output stream (which will be a response output
 * stream in OpenReports). This is based on the BigGridDemo class provided as a proof-of-concept:
 * http
 * ://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/
 * BigGridDemo.java
 * 
 * @author mconner
 */
public class POIXlsxExportProvider implements XlsxExportProvider {
    protected static Logger log = Logger.getLogger( POIXlsxExportProvider.class );

    private DirectoryProvider directoryProvider;

    @Override
    public void export( Iterator<DynaBean> data, DisplayProperty[] properties, OutputStream output, String exportId )
            throws ProviderException {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet( "Results" );
        Map<String, XSSFCellStyle> styles = createStyles( wb );

        // name of the zip entry holding sheet data, e.g. /xl/worksheets/sheet1.xml
        String sheetRef = sheet.getPackagePart().getPartName().getName();
        File templateFile = creatTemplateFile( wb );
        File sheetXMLFile = null;
        try {
            sheetXMLFile = createSheetXMLFile( data, properties, styles, exportId );
            substitute( templateFile, sheetXMLFile, sheetRef.substring( 1 ), output );
            log.info( "POIXlsxExportProvider export identified by: " + exportId + " complete" );
        } catch( IOException ioe ) {
            throw new ProviderException( "Error exporting in xlsx format", ioe );
        } finally {
            IoU.I.safeDelete( sheetXMLFile, POIXlsxExportProvider.class.getName() ); 
            IoU.I.safeDelete( templateFile, POIXlsxExportProvider.class.getName() ); 
        }
    }

    public void setDirectoryProvider( DirectoryProvider directoryProvider ) {
        this.directoryProvider = directoryProvider;
    }

    private File createSheetXMLFile( Iterator<DynaBean> data, DisplayProperty[] properties,
            Map<String, XSSFCellStyle> styles, String exportId ) throws ProviderException {
        File sheetXMLFile = null;
        try {
            sheetXMLFile = makeTempfile( "sheet", ".xml" );
            Writer fw = new OutputStreamWriter( new FileOutputStream( sheetXMLFile ), "UTF-8" );
            try {
                generate( data, properties, fw, styles, exportId );
            } finally {
                fw.close();
            }
            return sheetXMLFile;
        } catch( IOException ioe ) {
            IoU.I.safeDelete( sheetXMLFile, POIXlsxExportProvider.class.getName() ); 
            throw new ProviderException( "Cant create export sheet file", ioe );
        }
    }

    private File creatTemplateFile( XSSFWorkbook wb ) throws ProviderException {
        try {

            File tmp = makeTempfile( "execlexport", ".xlsx" );
            FileOutputStream os = new FileOutputStream( tmp );
            try {
                wb.write( os );
                os.flush();
            } finally {
                os.close();
            }
            return tmp;
        } catch( IOException ioe ) {
            throw new ProviderException( "Can't create template file for xlsx export", ioe );
        }
    }

    private File makeTempfile( String prefix, String suffix ) {
        String tempDir = directoryProvider.getTempDirectory();
        File tempFile = new File( tempDir + "/" + prefix + new Random().nextLong() + suffix );
        return tempFile;
    }


    private static Calendar createCalendar( Date date ) {
        if( date instanceof Timestamp ) {
            date = new Date( ( (java.sql.Timestamp) date ).getTime() );
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime( date );
        return cal;
    }

    /**
     * Create a library of cell styles.
     */
    private static Map<String, XSSFCellStyle> createStyles( XSSFWorkbook wb ) {
        Map<String, XSSFCellStyle> styles = new HashMap<String, XSSFCellStyle>();

        XSSFDataFormat fmt = wb.createDataFormat();

        XSSFCellStyle style1 = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBoldweight( Font.BOLDWEIGHT_BOLD );
        style1.setFont( font );
        styles.put( "header", style1 );

        XSSFCellStyle style4 = wb.createCellStyle();
        style4.setAlignment( XSSFCellStyle.ALIGN_RIGHT );
        style4.setDataFormat( fmt.getFormat( "mm/dd/yyyy" ) );
        styles.put( "date", style4 );

        return styles;
    }

    private static void generate( Iterator<DynaBean> data, DisplayProperty[] properties, Writer out,
            Map<String, XSSFCellStyle> styles, String exportId ) throws IOException {
        SpreadsheetWriter sw = new SpreadsheetWriter( out );
        sw.beginSheet();

        int rowIndex = 0;
        int headerStyleIndex = styles.get( "header" ).getIndex();

        sw.insertRow( rowIndex );
        for( int i = 0; i < properties.length; i++ ) {
            sw.createCell( i, properties[i].getName(), headerStyleIndex );
        }
        sw.endRow();

        while( data.hasNext() ) {
            DynaBean bean = data.next();

            ++rowIndex;
            
            sw.insertRow( rowIndex );
            for( int i = 0; i < properties.length; i++ ) {
                DisplayProperty property = properties[i];
                Object value = bean.get( property.getName() );
                setCellValue( sw, i, value, styles );
            }
            sw.endRow();
            if (shouldLogRowAt( rowIndex ) ) {
                log.info( "Export of "  + exportId + " currently at " + rowIndex + " rows" );
            }
        }
        log.info( "Export of "  + exportId + " exported a total of " + rowIndex + " rows" );
        
        sw.endSheet();
    }
    
    private static boolean shouldLogRowAt( int count ) {
        if( count < 10000 ) {
            return count % 1000 == 0;
        } else if( count < 100000 ) {
            return count % 10000 == 0;
        }
        return count % 100000 == 0;
    }


    private static void setCellValue( SpreadsheetWriter sw, int columnIndex, Object value,
            Map<String, XSSFCellStyle> styles ) throws IOException {

        if( value != null ) {
            if( value instanceof Number ) {
                sw.createCell( columnIndex, ( (Number) value ).doubleValue() );
            } else if( value instanceof Date ) {
                sw.createCell( columnIndex, createCalendar( (Date) value ), styles.get( "date" ).getIndex() );
            } else {
                sw.createCell( columnIndex, value.toString() );
            }
        }
    }

    /**
     * 
     * @param zipfile the template file
     * @param tmpfile the XML file with the sheet data
     * @param entry the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
     * @param out the stream to write the result to
     */
    private static void substitute( File zipfile, File tmpfile, String entry, OutputStream out ) throws IOException {

        ZipFile zip = new ZipFile( zipfile );
        try {
            ZipOutputStream zos = new ZipOutputStream( out );
            try {
                @SuppressWarnings( "unchecked" )
                Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
                while( en.hasMoreElements() ) {
                    ZipEntry ze = en.nextElement();
                    if( !ze.getName().equals( entry ) ) {
                        zos.putNextEntry( new ZipEntry( ze.getName() ) );
                        InputStream is = zip.getInputStream( ze );
                        IoU.I.copy( is, zos );
                        is.close();
                    }
                }
                zos.putNextEntry( new ZipEntry( entry ) );
                InputStream is = new FileInputStream( tmpfile );
                try {
                    IoU.I.copy( is, zos );
                } finally {
                    is.close();
                }
            } finally {
                zos.close();
            }

        } finally {
            zip.close();
        }
    }

    /**
     * Writes spreadsheet data in a Writer. (YK: in future it may evolve in a full-featured API for
     * streaming data in Excel)
     */
    protected static class SpreadsheetWriter {
        private int rowNumber;
        private final Writer writer;

        public SpreadsheetWriter( Writer writer ) {
            this.writer = new BufferedWriter(writer);
        }

        public void beginSheet() throws IOException {
            writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
            writer.write( "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" );
            writer.write( "<sheetData>\n" );
        }

        public void createCell( int columnIndex, Calendar value, int styleIndex ) throws IOException {
            createCell( columnIndex, DateUtil.getExcelDate( value, false ), styleIndex );
        }

        public void createCell( int columnIndex, double value ) throws IOException {
            createCell( columnIndex, value, -1 );
        }

        public void createCell( int columnIndex, double value, int styleIndex ) throws IOException {
            String ref = new CellReference( rowNumber, columnIndex ).formatAsString();
            writer.write( "<c r=\"" );
            writer.write(  ref );
            writer.write(  "\" t=\"n\"" );
            if( styleIndex != -1 ) {
                writer.write( " s=\"" + styleIndex + "\"" );
            }
            writer.write( ">" );
            writer.write( "<v>" );
            writer.write( Double.toString( value ) );
            writer.write( "</v>" );
            writer.write( "</c>" );
        }

        public void createCell( int columnIndex, String value ) throws IOException {
            createCell( columnIndex, value, -1 );
        }

        public void createCell( int columnIndex, String value, int styleIndex ) throws IOException {

            String ref = new CellReference( rowNumber, columnIndex ).formatAsString();
            writer.write( "<c r=\"" );
            writer.write( ref );
            writer.write( "\" t=\"inlineStr\"" );
            if( styleIndex != -1 ) {
                writer.write( " s=\"" );
                writer.write( Integer.toString( styleIndex ) );
                writer.write(  "\"" );
            }
            writer.write( ">" );
            writer.write( "<is><t>" );
            escapeXml( writer, value );
            writer.write( "</t></is>" );
            writer.write( "</c>" );
        }

        /**
         * Insert row end marker
         */
        public void endRow() throws IOException {
            writer.write( "</row>\n" );
        }

        public void endSheet() throws IOException {
            writer.write( "</sheetData>" );
            writer.write( "</worksheet>" );
            writer.flush();
        }

        /**
         * Insert a new row
         * 
         * @param rownum 0-based row number
         */
        public void insertRow( int rownum ) throws IOException {
            writer.write( "<row r=\"" );
            writer.write( Integer.toString( rownum + 1 ) );
            writer.write(  "\">\n" );
            this.rowNumber = rownum;
        }

        private void escapeXml( Writer out, String str ) throws IOException {
            str = ExcelEscapeUtils.escapeUnderscoreForExcel( str );
            int len = str.length();
            for( int i = 0; i < len; i++ ) {
                char c = str.charAt( i );
                switch( c ) {
                case 34:
                    out.write( "&quot;" );
                    break;
                case 38:
                    out.write( "&amp;" );
                    break;
                case 39:
                    out.write( "&apos;" );
                    break;
                case 60:
                    out.write( "&lt;" );
                    break;
                case 62:
                    out.write( "&gt;" );
                    break;
                default: // 
                    if( isLegalXMLChar( c ) ) {
                        out.write( c );
                    } else {
                        // excel format for escaped characters
                        out.write( ExcelEscapeUtils.escapeSpecialChar( c ) );
                    }
                }
            }
        }

        /**
         * 
         * @param ch
         * @return true if a legal xml char, as defined by From
         *         http://www.w3.org/TR/2006/REC-xml-20060816/#charsets #x9 | #xA | #xD |
         *         [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
         */
        private boolean isLegalXMLChar( char ch ) {
            if( ch == 0x9 || ch == 0xA || ch == 0xD || ( 0x20 <= ch && ch <= 0xD7FF )
                    || ( 0xE000 <= ch && ch <= 0xFFFD ) || ( 0x10000 <= ch && ch <= 0x0000 ) ) {
                return true;
            }
            return false;
        }
    }

}
