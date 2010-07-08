package org.efs.openreports.util;

import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

/**
 * Shows a message instead of null for the Report Template column that is shown when lists of Reports are shown on the JSP page.

 * @author jfruncek
 */
public class ReportTemplateColumnDecorator implements DisplaytagColumnDecorator {

	public static final String NONE = "(none)";
	public static final String NULL_VALUE = "-1";

	@Override
	public Object decorate(Object object, PageContext pageContext, MediaTypeEnum mediaTypeEnum)
			throws DecoratorException {
		if ( object instanceof String && NULL_VALUE.equals((String) object) ) {
			return NONE;
		}
		return object;
	}

}
