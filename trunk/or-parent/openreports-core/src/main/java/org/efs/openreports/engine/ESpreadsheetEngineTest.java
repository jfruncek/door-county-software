package org.efs.openreports.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.efs.openreports.delivery.IOUtil;
import org.efs.openreports.providers.ProviderException;
import org.jfree.util.Log;

import com.f1j.ss.Book;
import com.f1j.ss.BookModel;
import com.f1j.ss.Document;
import com.f1j.ss.DocumentOpenCallback;
import com.f1j.ss.DocumentSaveCallback;
import com.f1j.ss.DocumentType;
import com.f1j.ss.report.ReportParameter;
import com.f1j.ss.report.ReportParameterCollection;
import com.f1j.util.F1Exception;
import com.f1j.util.Group;

public class ESpreadsheetEngineTest {

    protected static final Logger LOG = Logger.getLogger( ESpreadsheetEngineTest.class );

    public static void main( String[] args ) {
        ESpreadsheetEngineTest tester = new ESpreadsheetEngineTest();
        LOG.info( "getting started" );
        try {
            tester.generateReport();
        } catch( ProviderException pe ) {
            System.out.println( "caught a provider exception: " + pe.getMessage() );
        }

    }

    public void generateReport() throws ProviderException {
        BookModel essBookModel = open();
        essBookModel.getLock();
        try {
            overrideDataSource( essBookModel );
            setParameters( essBookModel );
            saveToExcel( essBookModel );
        } finally {
            LOG.info( "releasing the bookmodel lock" );
            essBookModel.releaseLock();
        }
    }

    private void setParameter( BookModel essBookModel, String paramName, Object value ) {
        ReportParameterCollection essParams = essBookModel.getReportParameterCollection();
        com.f1j.ss.report.ReportParameter essParam = essParams.get( paramName );
        if( essParam == null ) {
            LOG.error( "No parameter on report with name: " + paramName + ", not setting value: " + value );
        } else {
            setParameterValue( essParam, value );
        }

    }

    private void setParameterValue( com.f1j.ss.report.ReportParameter essParam, Object value ) {

        String valueAsString = convertParamValueToString( essParam, value );
        try {
            essParam.setValueEx( valueAsString );
        } catch( F1Exception e ) {
            throw new RuntimeException( "Cant set parameter, " + essParam.getName() + ", with value: [" + valueAsString
                    + "]" );
        }
    }

    private void overrideDataSource( BookModel bookModel ) throws ProviderException {
        // skipping this part, as it is not relevant to the problem.
    }

    public BookModel open() throws ProviderException {

        String designFilePath = "C:\\workspaces\\openreports\\Reports\\Test\\ESSTestParams.sox";
        File designFile = new File( designFilePath );
        try {
            Locale locale = new Locale( "en-us" );
            Group group = new Group( locale );
            DocumentOpenCallback docb = new DocumentOpenCallback();
            Document bookDoc = new Document( group, designFile, docb );
            try {
                // throws IOException, F1Exception
                Book book = bookDoc.getBook();
                BookModel model = BookModel.Factory.create( book );
                return model;
            } finally {
                // bookDoc.release();
            }
        } catch( IOException ioe ) {
            throw new ProviderException( "Error trying to open espreadsheet report, " + designFilePath, ioe );
        } catch( F1Exception f1e ) {
            throw new ProviderException( "Error trying to open espreadsheet report, " + designFilePath, f1e );
        }
    }

    private void saveToExcel( BookModel essBookModel ) throws ProviderException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            DocumentSaveCallback dscb = new DocumentSaveCallback();
            Document bookDoc = essBookModel.getBook().getDocument();
            try {
                bookDoc.fileSaveAs( baos, DocumentType.EXCEL_97_WORKBOOK, dscb );
            } catch( RuntimeException re ) {
                LOG.error( "Error saving the file: " + re.getMessage(), re );
            } finally {
                safeRelease( bookDoc );
            }
        } catch( IOException ioe ) {
            throw new ProviderException( "Cannot write excel report: ", ioe );
        } catch( F1Exception f1e ) {
            throw new ProviderException( "Cannot write excel report: ", f1e );
        } catch( RuntimeException re ) {
            throw new ProviderException( "Cannot write excel report: ", re );
        }

        copyToFile( "C:\\temp\\esstest\\ESSTestParams.xls", baos.toByteArray() );
        // ReportEngineOutput oreportOutput = new ReportEngineOutput();
        // oreportOutput.setContentType( ReportEngineOutput.CONTENT_TYPE_XLS );
        //        
        // oreportOutput.setContent( baos.toByteArray() );
    }

    private void safeRelease( Document bookDoc ) {
        try {
            bookDoc.release();
        } catch( IOException ioe ) {
            LOG.warn( "error releasing document for report, " );
        }
    }

    protected void copyToFile( String path, byte[] content ) {
        try {
            File pathFile = new File( path );
            File parentDir = IOUtil.ensureParentDirExists( path );

            File file = new File( path );
            if( file.exists() ) {
                File archive = new File( parentDir, "archive" );
                archive.mkdir();
                Date lastModifiedDate = new Date( pathFile.lastModified() );
                String suffix = new SimpleDateFormat( "-yyyyMMdd-HHmmss.SSS" ).format( lastModifiedDate );

                File archiveFile = new File( archive, IOUtil.appendToFileName( pathFile.getName(), suffix ) );
                if( pathFile.renameTo( archiveFile ) ) {
                    LOG.info( "File: " + path + " exists, moved to archive, " + archiveFile );
                } else {
                    LOG.error( "Failed saving report. File: " + path + " exists, but can't move to archive, "
                            + archiveFile );
                }
            }
            if( IOUtil.copyToFile( new ByteArrayInputStream( content ), path ) < 0 ) {
                LOG.error( "Failed saving report, " + path );
            }
        } catch( IOException ioe ) {
            LOG.error( "Failed saving scheduled report, to file: " + path + ".  Cause:  " + ioe.getMessage(), ioe );
        }
    }


    private void outputTypeInformation( com.f1j.ss.report.ReportParameter essParam ) {

        ReportParameter.EType paramType = essParam.getType();
        boolean isBoolean = paramType.isBoolean();
        boolean isCurrency = paramType.isCurrency();
        boolean isDate = paramType.isDate();
        boolean isDateOrTime = paramType.isDateOrTime();
        boolean isDecimal = paramType.isDecimal();
        boolean isDouble = paramType.isDouble();
        boolean isFormula = paramType.isFormula();
        boolean isInteger = paramType.isInteger();
        boolean isNumeric = paramType.isNumeric();
        //boolean isOracleCursor = paramType.isOracleCursor();
        boolean isString = paramType.isString();
        boolean isStruct = paramType.isStruct();
        boolean isTable = paramType.isTable();
        boolean isTime = paramType.isTime();
        boolean isTimeStamp = paramType.isTimeStamp();

        // From this, we learn that parameters configured in the designer as :
        // Boolean are Boolean
        // DateTime are DateOrTime and timestamp
        // 

        System.out.print( "for param: " + essParam.getName());
        System.out.print( ", shortType: " + paramType.getShortType() );
        System.out.print( ", value: " + paramType.getValue() );
        if( isBoolean )
            System.out.print( ", bool" );
        if( isCurrency )
            System.out.print( ", currency" );
        if( isDate )
            System.out.print( ", date" );
        if( isDateOrTime )
            System.out.print( ", dateOrTime" );
        if( isDecimal )
            System.out.print( ", dec" );
        if( isDouble )
            System.out.print( ", double" );
        if( isFormula )
            System.out.print( ", formula" );
        if( isInteger )
            System.out.print( ", integer" );
        if( isNumeric )
            System.out.print( ", numeric" );
        if( isString )
            System.out.print( ", string" );
        if( isStruct )
            System.out.print( ", struct" );
        if( isTable )
            System.out.print( ", table" );
        if( isTime )
            System.out.print( ", time" );
        if( isTimeStamp )
            System.out.print( ", timestamp" );
        System.out.println();

    }

    private void setParameters( BookModel essBookModel ) throws ProviderException {
        setParameter( essBookModel, "EssBooleanParam", Boolean.TRUE );
        setParameter( essBookModel, "EssDateTimeParam", new Date() );
        setParameter( essBookModel, "EssIntegerParam", 3700 );
        setParameter( essBookModel, "EssFloatParam", Math.PI );
        setParameter( essBookModel, "EssStringParam", "goodbye" );
        setParameter( essBookModel, "EssFormulaParam", "=100 * 5" );
        setParameter( essBookModel, "EssCurrencyParam", new Double( 1000000.123456 ) );
        // Hack to fake a failure:
        // throw new ProviderException( "No parameter on report with name:" + "FOOBAR" );
    }

    /**
     * The ESS API only allows setting the parameters as strings, so we need to convert the object values.  From
     * some investigation, the Espreadsheet Designer supports the following parameter types which match the following
     * types on the ReportParameter.EType:
     * <ul>
     * <li>Boolean:  isBoolean</li>
     * <li>Currency:  isCurrency, isNumeric</li>
     * <li>DateTime: isDateOrTime, isTimestamp</li>
     * <li>Integer: isInteger, isNumeric</li>
     * <li>Float: isDouble, isNumeric</li>
     * <li>String:  isString</li>
     * <li>Formula:  isString</li>
     * </ul>
     * We convert accordingly.
     * 
     * @param value
     * @return
     */
    private String convertParamValueToString( com.f1j.ss.report.ReportParameter essParam, Object value ) {
        if( value == null )
            return null;
        ReportParameter.EType paramType = essParam.getType();

        outputTypeInformation( essParam );

        if( paramType.isBoolean() ) {
            return convertToBooleanFormat(value);
        } else if( paramType.isCurrency() ) {
            return convertToCurrencyFormat( value );
        } else if( paramType.isDateOrTime() ) {
            return convertToDateTimeFormat( value );
        } else if( paramType.isDouble() ) {
            return convertToDoubleFormat( value );
        } else if( paramType.isInteger() ) {
            return converToIntegerFormat( value );
        } else {
            Log.info( "unhandled type: " + paramType );
            return value.toString(); // hope for the best
        }
    }

    private String convertToBooleanFormat( Object value ) {
        return new Boolean( value.toString() ).toString(); // normalize it.
    }

    private String convertToCurrencyFormat( Object value ) {
        return value.toString();
    }
    
    private String convertToDateTimeFormat( Object value ) {
        if( value instanceof java.util.Date ) {
            return new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss.SSS aa" ).format( (java.util.Date) value );
        }
        return value.toString(); // rely on object
    }

    private String convertToDoubleFormat( Object value ) {
        if( value instanceof Number ) {
            return "" + ( (Number) value ).doubleValue();
        }
        return value.toString();
    }

    private String converToIntegerFormat( Object value ) {
        if( value instanceof Number ) {
            return "" + ( (Number) value ).intValue();
        }
        return value.toString();
    }

}
