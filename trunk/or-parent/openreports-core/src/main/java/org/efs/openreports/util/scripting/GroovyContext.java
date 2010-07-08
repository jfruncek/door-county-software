package org.efs.openreports.util.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Base for Classes that provide some context for groovy scripting. This is basically a
 * lazy-instantiate of the engine and any bindings.
 * 
 * @author mconner
 */
public class GroovyContext {
    public static final String SCRIPT_PREFIX = "#!";
    protected ScriptEngine scriptEngine = null;

    /**
     * @param groovyScript a string known to be a Groovy script that may optionally start with
     *            {@link #SCRIPT_PREFIX}.
     * @return the value of the script, if provided or null if groovyScript is null.
     * @throws ScriptException
     */
    public Object eval( String groovyScript ) throws ScriptException {
        if( groovyScript == null ) {
            return null;
        }
        if( groovyScript.startsWith( SCRIPT_PREFIX ) ) {
            groovyScript = groovyScript.substring( SCRIPT_PREFIX.length() );
        }
        return getEngine().eval( groovyScript );
    }

    /**
     * Convenience
     * 
     * @param value
     * @return if value is a String beginning with SCRIPT_PREFIX, return all after the prefix, else
     *         return null.
     */
    public String getScript( Object value ) {
        String result = ( value instanceof String ) ? getScript( (String) value ) : null;
        return result;
    }

    /**
     * @param value
     * @return The script portion of value, if it is a script, null otherwise.
     */
    public String getScript( String value ) {
        String result = isScript( value ) ? value.substring( SCRIPT_PREFIX.length() ) : null;
        return result;
    }

    /**
     * 
     * @param value
     * @return true if it looks like this is intended to be a groovy script.
     */
    public boolean isScript( String value ) {
        return value != null && value.startsWith( SCRIPT_PREFIX );
    }
    
    /**
     * 
     * @param value
     * @return true if it looks like this is intended to be a groovy script.
     */
    public boolean isScript( Object value ) {
        if( !( value instanceof String ) ) {
            return false;
        }
        return isScript( (String) value );
    }
    

    public ScriptEngine getEngine() {
        if( scriptEngine == null ) {
            ScriptEngineManager manager = new ScriptEngineManager();
            scriptEngine = manager.getEngineByName( "groovy" );
            initEngine( scriptEngine );
        }
        return scriptEngine;
    }

    /**
     * Do any special voodoo (e.g. bind some variables) appropriate to the given context.
     */
    protected void initEngine( ScriptEngine engine ) {
    }

	/**
	 * Utility method to evaluate element that may or may not have a script tag.
	 * @param context
	 * @param element
	 * @return
	 * @throws Exception
	 */
    public static String evaluateScriptableElement(GroovyContext context, String element) throws Exception {
		try {
			if ( context.isScript( element ) ) {
				Object result = context.eval(element);
				if ( result != null )
					return ( result.toString() );
				else
					return null;
			}
			else {
				return element;
			}
		} catch (ScriptException e) {
			throw new Exception("Cannot evaluate scriptable element: " + element, e);
		}
	}
}
