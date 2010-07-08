<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />

<s:actionerror/> 

<br/>

<div align="center">
  
  <div class="instructions" id="instructions">
  	<a href="editReportParameter.action?command=add">
  		<img border="0" src="images/add.gif"/> <s:text name="link.admin.addReportParameter"/>
  	</a>
  	<a href="reportAdmin.action?command=add">
  			<img border="0" src="images/back.gif"/> <s:text name="link.back.admin"/>
  	</a>
  </div>
    
  <br/>
  
  <div class="filterLabel" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setFilterFromCookie("ListReportParametersTable", 0); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter:</span>
      <input name="filter" onkeyup="filterTable(this.value, 'ListReportParametersTable', 0)"
        type="text"/>&nbsp;<span id="ListReportParametersTableFilterInfo"></span>
    </form>
  </div>
  <s:set name="reportParameters" value="reportParameters" scope="request" />
  
  <display:table id="ListReportParametersTable" name="reportParameters" class="displayTag" sort="list"  requestURI="listReportParameters.action" decorator="org.efs.openreports.util.HRefColumnDecorator">  	      
    <display:column property="name" href="editReportParameter.action?command=edit" paramId="id" paramProperty="id" titleKey="label.name" sortable="true" headerClass="sortable"/>
    <display:column property="description" titleKey="label.description" sortable="true" headerClass="sortable"/>  	     	        	     	     
    <display:column property="removeLink" title="" href="deleteReportParameter.action" paramId="id" paramProperty="id"/>  	     	     
  </display:table>
  <br> 
</div>

<s:include value="Footer.jsp" /> 

