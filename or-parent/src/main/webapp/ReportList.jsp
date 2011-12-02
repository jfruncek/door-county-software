<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />

<s:actionerror/> 

<div align="center">  

  <a class="back-link img-group-small" href="reportGroup.action"><s:text name="link.back.groups"/></a>	
  
  <br/>

	<s:if test="reportBooks.size > 0">
	  <s:set name="reportBooks" value="reportBooks" scope="request" />
	
		<div class="img-books important">
	  	<s:text name="bookList.title"/> <s:property value="reportGroup.name"/>
		</div>
	
		<display:table name="reportBooks" class="displayTag" sort="list" requestURI="listBooks.action"> 	      
		  <display:column property="name" titleKey="label.name" href="bookRun.action" paramId="id" paramProperty="id" />    	     	      	     
		</display:table>
	</s:if>
  
  <s:set name="reports" value="reports" scope="request" /> 

	<div class="img-report important">
  	<s:text name="reportList.title"/> <s:property value="reportGroup.name"/>
	</div>
	
  <div class="filterLabel" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setFilterFromCookie("ReportListTable", 0); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter:</span>
      <input name="filter" onkeyup="filterTable(this.value, 'ReportListTable', 0)"
        type="text"/>&nbsp;<span id="ReportListTableFilterInfo"></span>
    </form>
  </div>

	<display:table  id="ReportListTable" name="reports" class="displayTag" sort="list" requestURI="reportList.action" >                  
	  <display:column property="name" titleKey="label.name" href="reportDetail.action" paramId="reportId" paramProperty="id" sortable="true" headerClass="sortable"/>
      <display:column property="file" titleKey="label.reportFile" sortable="true" headerClass="sortable" decorator="org.efs.openreports.util.ReportTemplateColumnDecorator"/>
      <display:column property="description" titleKey="label.description" sortable="true" headerClass="sortable"/>              
	</display:table> 

</div>

<s:include value="Footer.jsp" /> 

