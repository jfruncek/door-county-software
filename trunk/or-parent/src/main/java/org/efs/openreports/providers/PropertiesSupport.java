package org.efs.openreports.providers;

import org.apache.log4j.Logger;
import org.efs.openreports.engine.QueryReportEngine;
import org.efs.openreports.objects.ORProperty;

/**
 * Proxies a PropertiesProvider, giving additional  
 * @author mconner
 */
public class PropertiesSupport implements PropertiesProvider {
    protected static Logger log = Logger.getLogger(QueryReportEngine.class);
    PropertiesProvider provider; 
    public PropertiesSupport(PropertiesProvider provider) {
        this.provider = provider;
    }
    
    public ORProperty getProperty( String key ) throws ProviderException {
        return provider.getProperty( key );
    }
    
    public void setProperty( String key, String value ) throws ProviderException {
        provider.setProperty( key, value );
    }
    
    public void setIntProperty( String key, int value ) throws ProviderException {
        provider.setProperty( key, Integer.toString( value ) );
    }
    
    public String getValue(String key, String defaultValue) throws ProviderException {
        ORProperty property = provider.getProperty(key);
        if (property == null ) {
            return defaultValue;
        }
        String value = property.getValue();
        return (value == null) ? defaultValue : value;
    }
    
    public boolean getBooleanValue( String key, boolean defaultValue ) throws ProviderException {
        ORProperty property = provider.getProperty( key );
        if( property == null ) {
            return defaultValue;
        }
        return new Boolean(property.getValue()).booleanValue();
    }

    
    public Integer getIntValue( String key, Integer defaultValue ) throws ProviderException {
        ORProperty property = provider.getProperty( key );
        if( property == null ) {
            return defaultValue;
        }
        String value = property.getValue();
        try {
            return new Integer( value );
        } catch( NumberFormatException nfe ) {
            log.warn( "NumberFormatException parsing property: " + key + ", value: " + value
                    + ", using defaultValue: " + defaultValue );
            return defaultValue;
        }
    }
}
