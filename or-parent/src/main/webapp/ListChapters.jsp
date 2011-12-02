<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@page import="org.efs.openreports.ReportConstants.ExportType"%>

<s:include value="Banner.jsp" />

<s:actionerror/> 

<br/>

<div class="img-report important" id="instructions">
    <s:text name="bookAdmin.message.reports"/> <s:property value="reportBook.name"/> 
</div>  

<br/>

<div align="center">
  
      <div class="instructions" id="instructions">
  		<a href="editChapter.action">
  			<img border="0" src="images/add.gif"/><s:text name="link.admin.addBookChapter"/>
  		</a>
  	 	<a href="listBooks.action">
  			<img border="0" src="images/back.gif"/> <s:text name="link.back.books"/>
  		</a>
  	</div>
  	

  <br/>
  
  <s:set name="chapters" value="chapters" scope="request" />
  
  <display:table name="chapters" class="displayTag" sort="list" requestURI="listChapters.action" 
                 decorator="org.efs.openreports.util.ChapterDecorator"> 	      
    <display:column property="dynamicChapterName" paramId="chapterId" paramProperty="id" 
                    titleKey="label.name" href="editChapter.action" sortable="true"/>
    <display:column property="report.name" titleKey="label.report" sortable="true" />
    <display:column property="exportTypeName" titleKey="label.type" sortable="true" />
    <display:column property="removeLink" title="" href="deleteChapter.action" paramId="chapterId" paramProperty="id" />  	     	     
	<display:footer><tr><td colspan="4"><s:property value="chapters.size"/> Reports</td></tr></display:footer>
  </display:table>    
  <br>
</div>

<s:include value="Footer.jsp" />
