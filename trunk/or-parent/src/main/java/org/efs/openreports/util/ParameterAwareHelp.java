package org.efs.openreports.util;

import java.util.Map;

/**
 * Provides support for classes implementing ParameterAware, or working with the maps it provides.
 * 
 * NOTE: These are lenient, in that if it isn't an array, or the array element isn't String, it will still work. I think
 * this would have been necessary when typed objects were being rewritten back to the parameters. Since this is no
 * longer the case, it should be possible to say these will always be Map<String, String[]>
 * 
 * @see org.apache.struts2.interceptor.ParameterAware
 */
public class ParameterAwareHelp {

    /**
     * Gets a value from Map whose values may be an array.
     * 
     * @param map
     * @param key
     * @return A single value
     */
    public static String getSingleValue( Map<String, Object> map, String key ) {
        Object value = map.get( key );
        return getSingleValue(value, key);
    }
    
    public static boolean isSingleValue(Object value) {
        if (value == null) return false;
        if (value instanceof String) return true;
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return array.length == 1;
        }
        return false;
    }
    
    public static String getSingleValue( Object value, String description ) {
    
        if( value == null ) {
            return null;
        }

        if( value instanceof String ) {
            return (String) value;
        }

        if( value.getClass().isArray() ) {
            Object[] array = (Object[]) value;
            switch( array.length ) {
            case 0:
                return null;
            case 1:
                return ( array[0] == null ) ? null : array[0].toString();
            default:
                throw new IllegalStateException( "more than one (" + array.length + ") value for " + description );
            }
        }
        throw new IllegalStateException( "value for " + description + " is neither a String nor array of strings: " + value.getClass() );
    }

    /**
     * @param map
     * @param key
     * @return the object at key, if an array, else an Object[] whose single value is the object at key, or null if
     *         there is no such object.
     */
    public static Object[] getMultiValue( Map<String, Object> map, String key ) {
        Object value = map.get( key );
        if( value == null ) {
            return null;
        }
        if( !( value instanceof Object[] ) ) {
            value = new Object[] { value };
        }
        return (Object[]) value;
    }
}
