package org.efs.openreports.actions.admin;

import java.util.Map;

import com.opensymphony.xwork2.ActionContext;

public class SessionHelper {
    private Map<?, ?> session;

    public SessionHelper( Map<?, ?> session ) {
        this.session = session;
    }

    /**
     * Gets the session from the ActionContext (i.e. ThreadLocal).
     */
    public SessionHelper() {
        this.session = ActionContext.getContext().getSession();
    }

    /**
     * Casts return value to expected type, providing better than ClassCastException error if there
     * is a problem. Usage:
     * 
     * <pre>
     * &lt;code&gt;
     *   Foo foo = sessionHelper.get(&quot;SomeFoo&quot;, Foo.class);
     * 
     *   &#064;SuppressWarnings( &quot;unchecked&quot; )
     *   List&lt;Foo&gt; uncheckedFooList = sessionHelper.get(&quot;SomeFooList&quot;, List.class);
     *   fooList = uncheckedFooList; // fooList previously defined. 
     *    
     * &lt;/code&gt;
     * </pre>
     * 
     * 
     * @param key
     * @param expectedType
     * @return
     */
    public <T> T get( String key, Class<T> expectedType ) {
        Object value = session.get( key );
        if( value == null ) {
            return null;
        } else if( expectedType.isInstance( value ) ) {
            return expectedType.cast( value );
        } else {
            throw new IllegalStateException( "Object with key: " + key + "is not of expected type, "
                    + expectedType.getName() + ", was: " + value.getClass().getName() );
        }
    }

    public Object get( String key ) {
        return session.get( key );
    }

    public void remove( String key ) {
        session.remove( key ); 
    }

}
