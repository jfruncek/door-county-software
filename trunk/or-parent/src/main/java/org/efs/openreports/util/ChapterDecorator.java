package org.efs.openreports.util;

import org.apache.log4j.Logger;
import org.efs.openreports.objects.BookChapter;
import org.efs.openreports.util.scripting.BookGroovyContext;
import org.efs.openreports.util.scripting.GroovyContext;

public class ChapterDecorator extends HRefColumnDecorator {

	protected static Logger log = Logger.getLogger(ChapterDecorator.class);
		
	private final transient BookGroovyContext context = new BookGroovyContext();
	
	public Object getDynamicChapterName() {
		BookChapter chapter = (BookChapter) getCurrentRowObject();
		
		try {
			return GroovyContext.evaluateScriptableElement(context, chapter.getName());
		} catch (Exception e) {
			log.warn("Caught in ChapterDecorator: " + e);
		}
		
		return chapter.getName();
	}
}
