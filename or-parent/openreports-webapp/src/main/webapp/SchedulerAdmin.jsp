<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />

<a class="back-link img-report-small" href="reportAdmin.action"><s:text name="link.back.admin"/></a>

<br/>

<s:actionerror/>

<div align="center">
  
  <div class="img-schedule important" id="instructions">
		  	  <s:text name="listScheduledReports.scheduledReports"/>
  </div>     
  
  <s:set name="scheduledReports" value="scheduledReports" scope="request" />
  
  <div class="filterLabel" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setFilterFromCookie("SchedulerAdminTable", 2); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter by Description:</span>
      <input name="filter" onkeyup="filterTable(this.value, 'SchedulerAdminTable', 2)"
        type="text"/>&nbsp;<span id="SchedulerAdminTableFilterInfo"></span>
    </form>
  </div>
  
  <display:table id="SchedulerAdminTable" name="scheduledReports" class="displayTag" sort="list" requestURI="schedulerAdmin.action" decorator="org.efs.openreports.util.ScheduleHRefColumnDecorator">  	   	      
    <display:column property="user.name" titleKey="label.username" sortable="true" headerClass="sortable"/>  	     	      	        
    <display:column property="report.name" titleKey="label.report" sortable="true" headerClass="sortable"/>    	     	      	          
    <display:column property="scheduleDescription" titleKey="label.description" sortable="true" headerClass="sortable"/>    	     	      	       
    <display:column property="scheduleTypeName" titleKey="label.scheduleType" sortable="true" headerClass="sortable"/>    	     	      	       	     	      
    <display:column property="nextFireDate" titleKey="label.nextRun" sortable="true" headerClass="sortable" format="{0,date,MM/dd/yyyy h:mm a}"/>   	     	      	       	     	         
    <display:column property="stateLink" titleKey="label.scheduleState" sortable="true"/>
    <display:column property="updateLink" title=""/>  	     
    <display:column property="removeLink" title=""/>  	     	     
  </display:table>   
  
  <br>  
</div>

<s:include value="Footer.jsp" /> 


