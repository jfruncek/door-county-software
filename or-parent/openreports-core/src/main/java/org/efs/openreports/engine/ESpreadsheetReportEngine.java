package org.efs.openreports.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.efs.openreports.engine.input.ReportEngineInput;
import org.efs.openreports.engine.output.ReportEngineOutput;
import org.efs.openreports.objects.Report;
import org.efs.openreports.objects.ReportDataSource;
import org.efs.openreports.objects.ReportParameter;
import org.efs.openreports.providers.ProviderException;
import org.jfree.util.Log;

import com.f1j.data.source.DataSetCache;
import com.f1j.data.source.JDBC;
import com.f1j.data.source.Source;
import com.f1j.ss.Book;
import com.f1j.ss.BookModel;
import com.f1j.ss.Document;
import com.f1j.ss.DocumentOpenCallback;
import com.f1j.ss.DocumentSaveCallback;
import com.f1j.ss.DocumentType;
import com.f1j.ss.report.ReportParameterCollection;
import com.f1j.ss.report.ReportParameter.EType;
import com.f1j.util.F1Exception;
import com.f1j.util.Group;

public class ESpreadsheetReportEngine extends ReportEngine {
    protected static Logger LOG = Logger.getLogger(ESpreadsheetReportEngine.class.getName());


    protected static Map<Short, OReportParamInfo> essTypesToOReportParamInfo = initEssTypesToOreportClassesMap();
    
    /**
     * Just holds mapping infor from ess to oreport.
     */
    private static class OReportParamInfo {

        private final String className;
        private final String fieldType;

        OReportParamInfo( String className, String fieldType ) {
            this.className = className;
            this.fieldType = fieldType;
        }

        public String getClassName() {
            return className;
        }

        public String getFieldType() {
            return fieldType;
        }

        @Override
        public String toString() {
            return className + ", " + fieldType;
        }
    }

    private static void addMapping( Map<Short, OReportParamInfo> map, EType eType, String paramClass,
            String paramFieldType ) {
        OReportParamInfo opi = new OReportParamInfo( paramClass, paramFieldType );
        short value = eType.getValue();
        LOG.debug( "adding mapping: value:  " + value + ", opi: " + opi );
        map.put( value, opi );
    }

    private static Map<Short, OReportParamInfo> initEssTypesToOreportClassesMap() {
        Map<Short, OReportParamInfo> map = new HashMap<Short, OReportParamInfo>();

        addMapping( map, EType.EBoolean, ReportParameter.BOOLEAN, ReportParameter.BOOLEAN_PARAM );
        addMapping( map, EType.ECurrency, ReportParameter.DOUBLE, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EDate, ReportParameter.DATE, ReportParameter.DATE_PARAM );
        addMapping( map, EType.EDecimal, ReportParameter.DOUBLE, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EDouble, ReportParameter.DOUBLE, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EFormula, ReportParameter.STRING, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EInteger, ReportParameter.INTEGER, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EOracleCursor, ReportParameter.STRING, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EString, ReportParameter.STRING, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.EStruct, ReportParameter.STRING, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.ETable, ReportParameter.STRING, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.ETime, ReportParameter.TIMESTAMP, ReportParameter.TEXT_PARAM );
        addMapping( map, EType.ETimeStamp, ReportParameter.TIMESTAMP, ReportParameter.TEXT_PARAM );
        return map;
    }

    public ESpreadsheetReportEngine() {
    }

    @Override
    public List<ReportParameter> buildParameterList( Report report ) throws ProviderException {
        String reportDesignPath = getReportDesignPath( report );

        Document document = null;
        try {
            document = openDocument( reportDesignPath );
            BookModel bookModel = openBookModel( document );
            try {
                bookModel.getLock();
                ArrayList<ReportParameter> oreportParameters = new ArrayList<ReportParameter>();
                ReportParameterCollection essParams = bookModel.getReportParameterCollection();
                for( com.f1j.ss.report.ReportParameter essReportParameter : essParams.get() ) {
                    if( !essReportParameter.isSystem() ) { // Don't care about the AC_... parameters
                        ReportParameter rp = new ReportParameter();
                        rp.setClassName( getOpenReportsClassNameForEssParameter( essReportParameter ) );
                        rp.setDescription( essReportParameter.getDisplayName() );
                        rp.setName( essReportParameter.getName() );
                        rp.setType( getOpenReportsParamTypeForEssParameter( essReportParameter ) );
                        oreportParameters.add( rp );
                    }
                }
                return oreportParameters;
            } finally {
                LOG.info( "bookModel.releaseLock()" );
                bookModel.releaseLock();
            }
        } finally {
            safeRelease( document, "in-mem doc created from " + reportDesignPath );
        }
    }

    @Override
    public ReportEngineOutput generateReport( ReportEngineInput oreportInput ) throws ProviderException {
        return runReport( oreportInput );
    }

    private String converToIntegerFormat( Object value ) {
        if( value instanceof Number ) {
            return "" + ( (Number) value ).intValue();
        }
        return value.toString();
    }

    /**
     * The ESS API only allows setting the parameters as strings, so we need to convert the object values. From some
     * investigation, the Espreadsheet Designer supports the following parameter types which match the following types
     * on the ReportParameter.EType:
     * <ul>
     * <li>Boolean: isBoolean</li>
     * <li>Currency: isCurrency, isNumeric</li>
     * <li>DateTime: isDateOrTime, isTimestamp</li>
     * <li>Integer: isInteger, isNumeric</li>
     * <li>Float: isDouble, isNumeric</li>
     * <li>String: isString</li>
     * <li>Formula: isString</li>
     * </ul>
     * We convert accordingly.
     * 
     * @param value
     * @return
     */
    private String convertParamValueToString( com.f1j.ss.report.ReportParameter essParam, Object value ) {
        if( value == null )
            return null;
        com.f1j.ss.report.ReportParameter.EType paramType = essParam.getType();

        if( paramType.isBoolean() ) {
            return convertToBooleanFormat( value );
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
            return new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss.SSS" ).format( (java.util.Date) value );
        }
        return value.toString(); // rely on object
    }

    private String convertToDoubleFormat( Object value ) {
        if( value instanceof Number ) {
            return "" + ( (Number) value ).doubleValue();
        }
        return value.toString();
    }

    /**
     * While the engine can handle more than this, the Designer only handles the following parameter types, which return
     * true for the given methods.
     * <ul>
     * <li>Boolean: isBoolean</li>
     * <li>Currency: isCurrency, isNumeric</li>
     * <li>DateTime: isDateOrTime, isTimestamp</li>
     * <li>Integer: isInteger, isNumeric</li>
     * <li>Float: isDouble, isNumeric</li>
     * <li>
     * String: isString</li>
     * <li>Formula: isString</li>
     * </ul>
     */
    private String getOpenReportsClassNameForEssParameter( com.f1j.ss.report.ReportParameter essReportParam ) {
        OReportParamInfo paramInfo = getReportParmaInfo( essReportParam );

        if( paramInfo == null ) {
            LOG.warn( "Unsupported Ess Parmeter Type: " + essReportParam.getType().getValue() + ", using: "
                    + ReportParameter.STRING );
            return ReportParameter.STRING;
        }
        return paramInfo.getClassName();
    }

    private String getOpenReportsParamTypeForEssParameter( com.f1j.ss.report.ReportParameter essReportParam ) {
        OReportParamInfo info = getReportParmaInfo( essReportParam );

        if( info == null ) {
            LOG.warn( "Unsupported Ess Parmeter Type: " + essReportParam.getType().getValue() + ", using: "
                    + ReportParameter.TEXT_PARAM );
            return ReportParameter.TEXT_PARAM;
        }
        return info.getFieldType();
    }

    private String getReportDesignPath( Report report ) {
        return directoryProvider.getReportDirectory() + report.getFile();
    }

    private OReportParamInfo getReportParmaInfo( com.f1j.ss.report.ReportParameter essReportParam ) {
        short essTypeValue = essReportParam.getType().getValue();
        if( looksLikeDateAsDateTimeParam( essReportParam ) ) {
            essTypeValue = EType.EDate.getValue();
        }
        OReportParamInfo result = essTypesToOReportParamInfo.get( essTypeValue );
        return result;
    }

    private ByteArrayInputStream loadDesign( String reportDesignPath ) throws ProviderException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File file = new File( reportDesignPath );
        byte[] buffer = new byte[1000];
        int readCount;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream( file );
            while( ( readCount = fis.read( buffer ) ) != -1 ) {
                baos.write( buffer, 0, readCount );
            }
        } catch( IOException ioe ) {
            throw new ProviderException( "Error trying to open espreadsheet document, " + reportDesignPath, ioe );
        } finally {
            safeClose( fis, reportDesignPath );
        }
        return new ByteArrayInputStream( baos.toByteArray() );
    }

    /**
     * Most of our parameters are just dates, but the Ess Designer only allows a choice of DateTime, which is a type of
     * TimeStamp. By default, we'll map that to the Open Reports ReportParameter.DATE, unless the name is explicitly
     * ...datetime (ignoring case).
     * 
     * @param essType
     * @return true if essReportParam is a timestamp, but does not have a name ending in datetime.
     */
    private boolean looksLikeDateAsDateTimeParam( com.f1j.ss.report.ReportParameter essReportParam ) {
        com.f1j.ss.report.ReportParameter.EType essType = essReportParam.getType();
        if( essType.isTimeStamp() ) {
            if( essReportParam.getName().toLowerCase().endsWith( "datetime" ) ) {
                return false;
            }
            return true;
        }
        return false;
    }

    private BookModel openBookModel( Document document ) throws ProviderException {
        Book book = document.getBook();
        BookModel model = BookModel.Factory.create( book );
        return model;
    }

    private Document openDocument( String reportDesignPath ) throws ProviderException {
        try {
            return openDocumentWithRetryOnOverlappingFileLockException( reportDesignPath );
        } catch( IOException ioe ) {
            throw new ProviderException( "Error trying to open espreadsheet document, " + reportDesignPath, ioe );
        } catch( F1Exception f1e ) {
            throw new ProviderException( "Error trying to open espreadsheet document, " + reportDesignPath, f1e );
        }
    }

    /**
     * Something in eSpreadsheet has intermittent problems with OverlappingFileLocks. Since we don't
     * have the source, we don't know exactly what its doing. In the rare event that this happens,
     * we'll wait a bit and retry.
     * 
     * @param reportDesignPath
     * @return
     * @throws ProviderException
     * @throws IOException
     * @throws F1Exception
     */
    private Document openDocumentWithRetryOnOverlappingFileLockException( String reportDesignPath )
            throws ProviderException, IOException, F1Exception {
        int exceptionCount = 0;
        while( true ) {
            try {
                ByteArrayInputStream bais = loadDesign( reportDesignPath );
                Locale locale = new Locale( "en-us" );
                Group group = new Group( locale );
                DocumentOpenCallback docb = new DocumentOpenCallback();
                Document bookDoc = new Document( group, bais, docb );
                return bookDoc;
            } catch( java.nio.channels.OverlappingFileLockException ofle ) {
                exceptionCount++;
                if( exceptionCount > 2 ) {
                    String errorMessage = "Got multiple (" + exceptionCount
                            + ") OverlappingFileLockExceptions.  Giving Up.  Report Design Path: " + reportDesignPath;
                    LOG.error( errorMessage, ofle );
                    throw ( new IOException( errorMessage, ofle ) );
                }
                LOG.warn(
                        "Got " + exceptionCount
                                + " OverlappingFileLockExceptions on report. Retrying. Report Design Path: "
                                + reportDesignPath, ofle );
            }
            sleep( 10000 );
        }
    }

    private void sleep( int milliseconds ) {
        try {
            Thread.sleep( milliseconds );
        } catch( InterruptedException ie ) {
            LOG.warn( "Something interrupted my sleep!", ie );
        }
    }

    private void overrideDataSource( BookModel bookModel, ReportEngineInput reportInput ) throws ProviderException {

        Source[] essSources = bookModel.getDataSourceCollection().get();
        for( Source essSource : essSources ) {
            if( essSource instanceof JDBC && !( essSource instanceof DataSetCache ) ) {
                JDBC essJDBC = (JDBC) essSource;
                String dataSourceName = essJDBC.getName();
                
                ReportDataSource oreportDataSource  = getOpenReportDataSource(dataSourceName, reportInput.getReport());
                // ex
                if( oreportDataSource.isJndi() ) {
//                        essJDBC.setJndiName( oreportDataSource.getUrl() );
                        
//                        Connection connection = getConnection( oreportDataSource );
//                        essJDBC.setConnection( new ConnectionWrapper(connection) );
                        
                        
                    essJDBC.setDriverName( JNDIWrapperDriver.class.getName() );
                    String dataSourceURL = oreportDataSource.getUrl();
                    essJDBC.setDatabase( JNDIWrapperDriver.PROTOCOL + ":" + dataSourceURL  );
                    essJDBC.setUserName( "" );
                    essJDBC.setPassword( "" );
                    essJDBC.setJndiName( "" );
                    // essJDBC.setConnectionUseOptimizationFlag( JDBC.CF_OPTIMIZE_FOR_CONNECTION_POOL_USE );
                        
                } else {
                    essJDBC.setDriverName( oreportDataSource.getDriverClassName() );
                    essJDBC.setDatabase( oreportDataSource.getUrl() );
                    essJDBC.setUserName( oreportDataSource.getUsername() );
                    essJDBC.setPassword( oreportDataSource.getPassword() );
                    essJDBC.setJndiName( "" );
                }
            }
        }
    }

    /**
     * The data source to be used for the report for the given name.   
     * @param the name of the datasource.  This will be used to look up the datasource unless edefaultDataSourceName name to use if not explicitly set on the report configuration.
     * @param report
     * @return the data source to be used for the given name
     * @throws ProviderException
     */
    private ReportDataSource getOpenReportDataSource( String defaultDataSourceName, Report report ) throws ProviderException {
        ReportDataSource oreportDateSource = report.getDataSource();
        String dataSourceName = defaultDataSourceName;
        if (oreportDateSource != null) {
            String reportDataSourceName = oreportDateSource.getName();
            if (!StringUtils.isBlank( reportDataSourceName )) {
                dataSourceName = reportDataSourceName;
            }
        }
        oreportDateSource = dataSourceProvider.getDataSource( dataSourceName ); // might throw
        return oreportDateSource;
    }

    private ReportEngineOutput runReport( ReportEngineInput oreportInput ) throws ProviderException {
        LOG.info("========== In class: " + ESpreadsheetReportEngine.class.getName() + ".start()");
        LOG.info("==========  ClassLoader: " + ESpreadsheetReportEngine.class.getClassLoader().toString());
        
        Document document = null;
        String reportDesignPath = getReportDesignPath( oreportInput.getReport() );
        try {
            document = openDocument( reportDesignPath );
            BookModel bookModel = openBookModel( document );
            try {
                bookModel.getLock();
                overrideDataSource( bookModel, oreportInput );
                setParameters( bookModel, oreportInput );
                ReportEngineOutput output = saveToExcel( bookModel, oreportInput );
                return output;
            } finally {
                LOG.info( "bookModel.releaseLock()" );
                bookModel.releaseLock();
                bookModel.getDataSourceCollection().releaseConnections();
            }
        } finally {
            safeRelease( document, "in-mem doc created from " + reportDesignPath );
        }
    }

    private void safeClose( FileInputStream fis, String path ) {
        if( fis != null ) {
            try {
                fis.close();
            } catch( IOException ioe ) {
                LOG.warn( "Can't close FileInputStream for path " + path, ioe );
            }
        }
    }

    private void safeRelease( Document document, String documentInfo ) {
        if( document != null ) {
            try {
                document.release();
                LOG.info( "Document.release()" );
            } catch( IOException ioe ) {
                LOG.error( "Error releasing document for report, " + documentInfo, ioe );
            }
        }
    }

    private ReportEngineOutput saveToExcel( BookModel essBookModel, ReportEngineInput reportEngineInput )
            throws ProviderException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            DocumentSaveCallback dscb = new DocumentSaveCallback();
            Document document = essBookModel.getBook().getDocument();
            try {
                document.fileSaveAs( baos, DocumentType.EXCEL_97_WORKBOOK, dscb );
            } catch( RuntimeException re ) {
                LOG.error( "Error saving the file: " + re.getMessage(), re );
            }
        } catch( IOException ioe ) {
            throw new ProviderException( "Cannot write excel report: ", ioe );
        } catch( F1Exception f1e ) {
            throw new ProviderException( "Cannot write excel report: ", f1e );
        } catch( RuntimeException re ) {
            throw new ProviderException( "Cannot write excel report: ", re );
        }

        ReportEngineOutput oreportOutput = new ReportEngineOutput();
        oreportOutput.setContentType( ReportEngineOutput.CONTENT_TYPE_XLS );
        ReportPostProcessExecutor executor = new ReportPostProcessExecutor( directoryProvider );
        oreportOutput.setContent( executor.postProcess( baos.toByteArray(), reportEngineInput ) );
        return oreportOutput;
    }


    private void setParameters( BookModel essBookModel, ReportEngineInput oreportInput ) throws ProviderException {
        Map<String, Object> oreportParams = oreportInput.getParameters();
        ReportParameterCollection essParams = essBookModel.getReportParameterCollection();
        for( String paramName : oreportParams.keySet() ) {
            Object value = oreportParams.get( paramName );
            com.f1j.ss.report.ReportParameter essParam = essParams.get( paramName );
            if( essParam == null ) {
                LOG.debug( "No parameter on report with name: " + paramName + ", not setting value: " + value );
            } else {
                setParameterValue( essParam, value );
            }

        }

    }

    /**
     * Convert the parameter value to a string in manner appropriate to eSpreadsheet (so that ess can convert it back).
     * 
     * @param essParam
     * @param paramName
     * @param value
     * @throws ProviderException
     */
    private void setParameterValue( com.f1j.ss.report.ReportParameter essParam, Object value ) throws ProviderException {
        String valueAsString = convertParamValueToString( essParam, value );
        try {
            essParam.setValueEx( valueAsString );
        } catch( F1Exception e ) {
            throw new ProviderException( "Cant set parameter, " + essParam.getName() + ", with value: ["
                    + valueAsString + "]" );
        }
    }

}
