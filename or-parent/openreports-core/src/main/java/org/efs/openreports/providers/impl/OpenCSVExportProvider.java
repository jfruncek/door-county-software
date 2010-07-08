package org.efs.openreports.providers.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;
import org.efs.openreports.providers.CSVExportProvider;
import org.efs.openreports.providers.ProviderException;
import org.efs.openreports.util.DisplayProperty;

import au.com.bytecode.opencsv.CSVWriter;

public class OpenCSVExportProvider implements CSVExportProvider {

    @Override
    public void export( Iterator<DynaBean> data, DisplayProperty[] properties, OutputStream output, String exportId )
            throws ProviderException {

        try {

            CSVWriter writer = new CSVWriter( new PrintWriter( output ) );

            String[] headings = new String[properties.length];
            for( int i = 0; i < properties.length; i++ ) {
                headings[i] = properties[i].getName();
            }
            writer.writeNext( headings );

            while( data.hasNext() ) {
                DynaBean bean = data.next();
                String[] row = new String[properties.length];
                for( int i = 0; i < properties.length; i++ ) {
                    String propertyName = properties[i].getName();
                    Class<?> propertyClass = bean.getDynaClass().getDynaProperty( propertyName ).getType();
                    Object value = bean.get( propertyName );
                    String stringValue = null;
                    if( value != null ) {
                        if( propertyClass.equals( Timestamp.class ) ) {
                            SimpleDateFormat timeFormat = new SimpleDateFormat( "dd-MMM-yyyy hh:mm:ss a" );
                            stringValue = timeFormat.format( (Timestamp) value );
                        } else if( propertyClass.equals( Date.class ) ) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MMM-yyyy" );
                            stringValue = dateFormat.format( (Date) value );
                        } else {
                            stringValue = value.toString();
                        }
                    }
                    row[i] = stringValue;
                }
                writer.writeNext( row );
            }
            writer.close();
        } catch( IOException ioe ) {
            throw new ProviderException( "Cant export to CSV", ioe );
        }
    }

}
