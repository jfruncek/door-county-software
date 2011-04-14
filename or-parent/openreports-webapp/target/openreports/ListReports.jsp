<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>


<s:include value="Banner.jsp" />

<s:actionerror/>


<br/>
  <div class="img-report important" id="instructions">
      <s:text name="reportAdmin.message.title"/>  
  </div>  

<br/>

<div align="center">
  	
	<div class="instructions" id="instructions">
  		<a href="editReport.action?command=add">
  			<img border="0" src="images/add.gif"/> <s:text name="link.admin.addReport"/>
  		</a>  	  
      	<s:if test="#session.user.uploader">      
  		<a href="reportUpload.action">
  			<img border="0" src="images/upload.gif"/> <s:text name="link.admin.uploadReport"/>
  		</a>
  		</s:if>
  		<a href="reportAdmin.action?command=add">
  			<img border="0" src="images/back.gif"/> <s:text name="link.back.admin"/>
  		</a>
  	</div>


	<s:set name="reports" value="reports" scope="request" />  

  <div class="filterLabel" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setFilterFromCookie("ListReportsTable", 0); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter:</span>
      <input name="filter" onkeyup="filterTable(this.value, 'ListReportsTable', 0)"
        type="text"/>&nbsp;<span id="ListReportsTableFilterInfo"></span>
    </form>
  </div>

  <display:table id="ListReportsTable"  name="reports" class="displayTag" sort="list" requestURI="listReports.action" decorator="org.efs.openreports.util.HRefColumnDecorator">
  
    <display:column property="name" href="editReport.action?command=edit" paramId="id" paramProperty="id" titleKey="label.name" sortable="true" headerClass="sortable"/>    	     	      	     
    <display:column property="file" titleKey="label.reportFile" sortable="true" headerClass="sortable" decorator="org.efs.openreports.util.ReportTemplateColumnDecorator"/>
    <display:column property="description" titleKey="label.description" sortable="true" headerClass="sortable"/>           	 
    <display:column property="addToGroupLink" title="" href="editReportGroups.action" paramId="id" paramProperty="id"/>  	     	         	        	     	     
    <display:column property="removeLink" title="" href="deleteReport.action" paramId="id" paramProperty="id"/> 	   
  </display:table> 
    
  <br>  
  
</div>

<s:include value="Footer.jsp" />


