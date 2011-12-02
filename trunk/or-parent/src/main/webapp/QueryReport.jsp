<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@page import="org.efs.openreports.util.DisplayProperty"%>
<%@page import="org.efs.openreports.objects.Report"%>
<%!public static final String DOT_PDF = ".pdf";
    public static final String DOT_XLS = ".xls";
    public static final String DOT_CSV = ".csv";%> 
<s:include value="Banner.jsp" />

<s:if test="report == null || !report.isDisplayInline()">

<a class="back-link img-report-small" href="reportList.action"><s:text name="link.back.reports"/></a>
<a class="back-link img-group-small" href="reportGroup.action"><s:text name="link.back.groups"/></a>  	
  
<br/>

<s:actionerror/>
	<div align="center">  
  		<div class="important img-queryreport" id="instructions"><s:property value="report.name"/></div>  
	</div>
</s:if>

<div align="center">   
    
  <s:set name="results" value="results" scope="request" />  
  <s:set name="properties" value="properties" scope="request" />
  <s:set name="report" value="report" scope="request" />

  <s:if test="atMaxRows">
  <div align="center" class="warning" >the query has reached the system row limit, there are more results not shown.</div>    
  </s:if>  
  
  <%
      DisplayProperty[] displayProperties = (DisplayProperty[]) request.getAttribute( "properties" );
      Report report = (Report) request.getAttribute( "report" );
  %> <display:table
  name="results" class="displayTag"
  requestURI="queryReportResult.action">
  <%
      for( int i = 0; i < displayProperties.length; i++ ) {
  %>
  <display:column property="<%=displayProperties[i].getName()%>"
    title="<%=displayProperties[i].getDisplayName()%>"
    decorator="<%=displayProperties[i].getDecorator()%>" 
    sortable="<%=displayProperties[i].isSortable()%>"
    headerClass="sortable"
    
     />
  <%
      }
  %>
</display:table> <s:if test="#session.user.scheduler">
  
    <s:text name="queryReport.scheduleReport"/>
<!--  broken see RPT-1463 <a href="reportOptions.action?reportId=<%=report.getId()%>&submitSchedule=true&exportType=3">CSV</a> -->
    <a href="reportOptions.action?reportId=<%=report.getId()%>&submitSchedule=true&exportType=7">Excel (97)</a>|
    <a href="reportOptions.action?reportId=<%=report.getId()%>&submitSchedule=true&exportType=9">XLSX (Excel 2003</a>|
    <a href="reportOptions.action?reportId=<%=report.getId()%>&submitSchedule=true&exportType=3">CSV</a>
<!--  broken see RPT-1462 <a href="reportOptions.action?reportId=<%=report.getId()%>&submitSchedule=true&exportType=0">PDF</a> -->    
  
  </s:if>  
  
  <br/>
  <s:text name="queryReport.exportOptions"/>
  <a href="queryReportExport.action?reportId=<%=report.getId()%>&exportTypeCode=7">Excel(97)</a> |
  <a href="queryReportExport.action?reportId=<%=report.getId()%>&exportTypeCode=9">XLSX (Excel 2003)</a> |
  <a href="queryReportExport.action?reportId=<%=report.getId()%>&exportTypeCode=3">CSV</a>

</div>

<s:if test="report == null || !report.isDisplayInline()">

<s:include value="Footer.jsp" />

</s:if>



	
