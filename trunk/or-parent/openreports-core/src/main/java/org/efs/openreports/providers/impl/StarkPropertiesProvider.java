package org.efs.openreports.providers.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.ORProperty;
import org.efs.openreports.providers.PropertiesProvider;
import org.efs.openreports.providers.ProviderException;

/**
 * PropertiesProvider that pulls a handful of read-only properties from the System.properties, and
 * delegates to another provider for the rest.
 * 
 * @author mconner
 */
public class StarkPropertiesProvider implements PropertiesProvider {

    protected static Logger log = Logger.getLogger( StarkPropertiesProvider.class.getName() );

    /** property and the default value */
    static HashMap<String, String> systemPropertyMap = initPropertyMap();

    private PropertiesProvider delegatePropertiesProvider;

    public StarkPropertiesProvider( PropertiesProvider delegatePropertiesProvider ) throws ProviderException {
        this.delegatePropertiesProvider = delegatePropertiesProvider;
        log.info( "StarkPropertiesProvider created" );
    };

    public ORProperty getProperty( String key ) throws ProviderException {
        if( isSystemProperty( key ) ) {
            return getSystemProperty( key );
        } else {
            return delegatePropertiesProvider.getProperty( key );
        }
    }

    public void setProperty( String key, String value ) throws ProviderException {
        if( isSystemProperty( key ) ) {
            throw new ProviderException( "Cannot set value for read-only property, " + key );
        }
        delegatePropertiesProvider.setProperty( key, value );
    }

    private ORProperty getSystemProperty( String key ) {
        String systemProperty = "stark.openreports." + key;
        String value = System.getProperty( systemProperty );
        if( value == null ) {
            value = systemPropertyMap.get( key );
            log.info( "System property, " + systemProperty + ", not defined. Using default value of " + value );
        }
        ORProperty prop = new ORProperty();
        prop.setKey( key );
        prop.setValue( value );
        return prop;
    }

    private boolean isSystemProperty( String key ) {
        return systemPropertyMap.containsKey( key );
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch( UnknownHostException uhe ) {
            return "unknownhost";
        }
    }

    private static HashMap<String, String> initPropertyMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( ORProperty.IS_PROD_SERVER, "false" );
        map.put( ORProperty.DEVELOPMENT_EMAIL, "it-reporting@starkinvestments.com" );
        // note that this looks weird on a windows box, but should still work, and you can override
        // it.
        map.put( ORProperty.DEVELOPMENT_FILE_ROOT, "/mnt/public/ReportingDept/ReportOutput/dev/" + getHostName() );
        // Allows enabling delivery to CommandDeliveryMethods on a development or qa environment
        map.put( ORProperty.DEVELOPMENT_ENABLE_COMMAND_DELIVERY, "false" );
        return map;
    }

}