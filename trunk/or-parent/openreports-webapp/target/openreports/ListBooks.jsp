<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />

<s:actionerror/> 

<br/>

<div class="img-books important" id="instructions">
    <s:text name="bookAdmin.message.title"/>  
</div>  

<br/>

<div align="center">
  
      <div class="instructions" id="instructions">
  		<a href="editBook.action?command=add">
  			<img border="0" src="images/add.gif"/><s:text name="link.admin.addReportBook"/>
  		</a>
  	 	<a href="reportAdmin.action?command=add">
  			<img border="0" src="images/back.gif"/> <s:text name="link.back.admin"/>
  		</a>
  	</div>
  	

  <br/>
  
  <s:set name="reportBooks" value="reportBooks" scope="request" />
  
  <display:table name="reportBooks" class="displayTag" sort="list" requestURI="listBooks.action" decorator="org.efs.openreports.util.HRefColumnDecorator"> 	      
    <display:column property="name" href="editBook.action?command=edit" paramId="id" paramProperty="id" titleKey="label.name" sortable="true" headerClass="sortable"/>    	     	      	     
    <display:column property="runBookLink" title="" href="runBook.action" paramId="id" paramProperty="id"/>
    <display:column property="chaptersLink" title="" href="listChapters.action" paramId="id" paramProperty="id"/>    	     
    <display:column property="removeLink" title="" href="deleteBook.action" paramId="id" paramProperty="id"/>  	     	     
  </display:table>    
  <br>
</div>

<s:include value="Footer.jsp" />
