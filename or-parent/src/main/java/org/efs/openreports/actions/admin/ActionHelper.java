package org.efs.openreports.actions.admin;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.efs.openreports.objects.ReportParameterMap;
import org.efs.openreports.util.ParameterAwareHelp;

import com.opensymphony.xwork2.ActionSupport;
/**
 * Standardized error output for exceptions.  
 * There were several variations on how Exceptions were being handled:
 * <ul>
 * <li>addActionHelper(ex.getMessage())</li>
 * <li>addActionHelper(ex.toString())</li>
 * <li>addActionHelper(getText(ex.toString()))</li>
 * <li>addActionHelper(getText(ex.getMessage()))</li>
 * </ul>
 * 
 * The problem is, adding a null (which can happen with getMessage() or getText(...)), 
 * causes problems higher up the call stack.  Nulls messages cause the UI to choke, and aren't
 * particularily useful, anyway.  This implementation isn't exactly like all the original
 * code regarding the getText, but close enough, I think.    
 * 
 * @author mconner
 */
public class ActionHelper {
    protected static Log LOG = LogFactory.getLog(ActionHelper.class);
    
    public static void addExceptionAsError( ActionSupport actionSupport, Exception ex ) {
        String message  = ex.getMessage();
        if (message != null) {
            
            message = safeGetText(actionSupport, message);
        } else {
            message = ex.toString();
            message = safeGetText(actionSupport, message); 
        }
        if (LOG.isInfoEnabled()) {
            LOG.info( "Exception Thrown in action: " + message, ex);
        }
        
        actionSupport.addActionError( message );
        
    }
    
    /**
     * There's a bug in the getText implementation, where it can throw an NPE.  
     * 
     * @param actionSupport
     * @param message
     * @return
     */
    private static String safeGetText(ActionSupport actionSupport, String message) {
        try {
            return actionSupport.getText(message, message);
        } catch (RuntimeException re) {
            return message;
        }
    }
    
    
    /**
     * Utility method for dealing with the report parameter relation list.
     * @param parameterName
     * @return the ReportParamterMap with the given name, or null.
     */
    public static ReportParameterMap getReportParameter( List<ReportParameterMap> reportParameters, String parameterName ) {
        for( ReportParameterMap rpMap : reportParameters ) {
            if( rpMap.getReportParameter().getName().equals( parameterName ) ) {
                return rpMap;
            }
        }
        return null;
    }

	/**
	 * Utility method for obtaining a value entered or selected for a given parameter, given the report parameter relation list
	 * and a map containing parameter value pairs.	
	 * @param parameterName
	 * @return The parameter value. If there is no value, returns the default report parameter value.
	 */
    public static String getParamValue( List<ReportParameterMap> reportParameters, Map<String, Object> parms, String parameterName ) {
		String value = null;
		if ( parms != null) {
        	value = ParameterAwareHelp.getSingleValue( parms , parameterName );
		}
        if( value == null && reportParameters != null ) {
            value = getDefaultValue( reportParameters, parameterName, null );
        }
        return value;
    }

	/**
     * @param parameterName
     * @param value
     * @return the default value, if a parameter exists with the given name.
     */
    private static String getDefaultValue( List<ReportParameterMap> reportParameters, String parameterName, String value ) {
        ReportParameterMap rpMap = getReportParameter(reportParameters, parameterName);
        if ( rpMap != null) {
            value = rpMap.getReportParameter().getDefaultValue(); 
        }
        return value;
    }
    

}
