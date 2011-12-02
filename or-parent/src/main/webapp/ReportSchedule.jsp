<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="d" uri="/struts-dojo-tags"%>

<s:include value="Banner.jsp" />
<s:head/>
<d:head />
<style type="text/css">
   table {margin: 0px;}
   table td {padding: 0px;}
   table.dialog {margin: 10px;}
   table.dialog td {padding: 3px;}   
</style>

<s:actionerror/> 

<div align="center">

  <a class="back-link img-report-small" href="reportList.action"><s:text name="link.back.reports"/></a>
  <a class="back-link img-group-small" href="reportGroup.action"><s:text name="link.back.groups"/></a>
  	
  <br/><br/>
  
   <div class="img-schedule important" id="instructions" style="width: 90%;">
		<s:text name="reportSchedule.title"/> <s:property value="report.name"/>
   </div>
		 
  <form action="<s:property value="#request.get('struts.request_uri')"/>" name="scheduleDetail" class="dialog-form" style="width: 95%;">   
  
  <table  class="dialog" >         
    <tr>
      <td align="right" class="boldText"><s:text name="label.description"/></td>         
      <td colspan="8">      
        <input type="text" style="width:100%" name="description" value="<s:property value="description"/>" >
      </td>
    </tr>
    
    <tr>
      <td class="boldText" colspan="9">
	      <a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>ScheduleDefinition">Schedule:</a>
      </td>
    </tr>
    
    <tr>
      <td align="right" class="boldText">
	      <a class="doc-link" target="DocWindow" href="<s:text name="link.scheduling.doc"/>StartDate">
		      <s:text name="label.startDate"/>
	      </a>
      </td>         
      <td colspan="8">
      	<d:datetimepicker name="startDate" value="%{startDate}" displayFormat="MM/dd/yyyy"  />
      	<s:text name="label.startTime"/>     
        <input type="text" name="startHour" value="<s:property value="startHour"/>" size="2" maxLength="2" /> :   
        <input type="text" name="startMinute" value="<s:property value="startMinute"/>" size="2" maxLength="2" />      
        <select name="startAmPm" value="<s:property value="startAmPm"/>">
          <option value="AM">AM</option>
          <option value="PM" <s:if test="startAmPm.equals('PM')">SELECTED</s:if> >PM</option>
        </select>
        <s:if test="currentStartDate != null">
        &nbsp; <i>(current Start Date/Time: <s:date name="currentStartDate" format="MM/dd/yyyy hh:mm a"/>)</i>
        </s:if>
      </td>
    </tr>    
    
    <tr>
      <td valign="top"  align="right" class="boldText">
	      <a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>ScheduleType"><s:text name="label.scheduleType"/></a>
      </td>         
      <td valign="top">
        <input type="radio" name="scheduleType" value="7" <s:if test="scheduleType == 7">CHECKED</s:if> ><s:text name="label.once"/>
      </td>     
      <td valign="top">
		<input type="radio" name="scheduleType" value="5" <s:if test="scheduleType == 5">CHECKED</s:if> ><s:text name="label.hourly"/>
		<input type="text" name="hours" value="<s:property value="hours"/>" size="5" maxLength="20" />    
      </td>     
      <td valign="top">
        <input type="radio" name="scheduleType" value="1" <s:if test="scheduleType == 1">CHECKED</s:if> ><s:text name="label.daily"/>
      </td>     
      <td valign="top">
        <input type="radio" name="scheduleType" value="4" <s:if test="scheduleType == 4">CHECKED</s:if> ><s:text name="label.weekdays"/>
      </td>     
      <td valign="top">
        <input type="radio" name="scheduleType" value="2" <s:if test="scheduleType == 2">CHECKED</s:if> ><s:text name="label.weekly"/>
      </td>     
      <td valign="top">
        <input type="radio" name="scheduleType" value="3" <s:if test="scheduleType == 3">CHECKED</s:if> ><s:text name="label.monthly"/>
      </td>
      <td valign="top">
	      <s:if test="#session.user.advancedScheduler">      
	        <input type="radio" name="scheduleType" value="6" <s:if test="scheduleType == 6">CHECKED</s:if> ><s:text name="label.cron"/>
	        <input type="text" name="cron" value="<s:property value="cron"/>" size="25" maxLength="80" />      
	        
	      </s:if>
      </td>
           
    </tr>  
    <s:if test="#session.user.alertUser">   
    <tr>
      <td align="right" class="boldText" width="20%">
		<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>ScheduleCondition"><s:text name="label.condition"/></a>
      </td>
      <td colspan="8">
        <s:select name="alertId" list="alerts" listKey="id" listValue="name" headerKey="-1" headerValue=" -- None -- " theme="simple"/>			
        <s:select name="alertOperator" list="operators" emptyOption="false" theme="simple"/>                 
        <input type="text" name="alertLimit" value="<s:property value="alertLimit"/>" size="10" maxLength="10" />      
      </td>
    </tr> 
    </s:if>  
    <s:if test="!#session.user.alertUser">   
    	<input type="hidden" name="alertId" value="-1">
    </s:if>
    
    <tr>
      <td class="boldText" colspan="9">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>Delivery">Delivery:</a>
      </td>
    </tr>
    
    
     <tr>
      <td align="right" valign="top" class="boldText">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>EmailSubject"><s:text name="label.emailSubject"/></a>
      </td>         
      <td colspan="8">      
      	<input type="text" style="width:100%"  name="emailSubjectLine" value="<s:property value="emailSubjectLine"/>" >
      </td>
    </tr>  
    
	<tr>
      <td class="boldText"></td>         
      <td colspan="8">      
        <s:property value="parsedEmailSubjectLine"/>
      </td>
    </tr>
        
     <tr>
      <td align="right" valign="top" class="boldText">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>SendToFile"><s:text name="reportSchedule.sendToFile"/></a>
      </td>         
      <td valign="top" colspan="7" >      
        <input type="checkbox" name="runToFile" value="true" <s:if test="runToFile">CHECKED</s:if> > 
		<img  id="query" src="images/help.gif" title="<s:text name="tooltip.schedule.runToFile"/>"/>      	
      </td>
    </tr>      

     <tr>
      <td align="right" valign="top" class="boldText">
		<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>EmailAttachmentName"><s:text name="label.emailAttachmentName"/></a>
      </td>         
      <td colspan="8" valign="top">      
      	<input type="text" style="width:100%" name="emailAttachmentName" value="<s:property value="emailAttachmentName"/>" >
      </td>
    </tr>      
	<tr>
      <td class="boldText"></td>         
      <td colspan="8">      
        <s:property value="parsedEmailAttachmentName"/>
      </td>
    </tr>
    
     <tr>
      <td align="right" valign="top"  class="boldText">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>EmailRecipients"><s:text name="label.recipients"/></a>
      </td>         
      <td colspan="8" valign="top" >      
        <textarea rows="8" style="width:100%"  name="recipients"><s:property value="recipients"/></textarea>
      </td>
    </tr>      
    
     <tr>
      <td align="right" valign="top" class="boldText">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>OutputPaths"><s:text name="label.outputPaths"/></a>
      </td>         
      <td colspan="8">      
        <textarea rows="3"  style="width:100%" name="outputPaths" WRAP="OFF"><s:property value="outputPaths"/></textarea>
      </td>
    </tr>      

    <s:iterator id="value" value="parsedOutputPaths">                
    <tr>
      <td class="boldText"></td>         
      <td colspan="8">
        <s:property/>      
      </td>
    </tr>
    </s:iterator>
    
    <s:if test="#session.user.advancedScheduler">      
    <tr>
      <td align="right" class="boldText">
      	<a class="doc-link"  target="DocWindow" href="<s:text name="link.scheduling.doc"/>PrintCommand"><s:text name="label.printCommand"/></a>
      </td>         
      <td colspan="8">      
        <input type="text" style="width:100%" name="printCommand" value="<s:property value="printCommand" />" >
      </td>
    </tr>

    <tr>
      <td class="boldText"></td>         
      <td colspan="8">    
        <s:property value="parsedPrintCommand"/>
      </td>
    </tr>
    
    </s:if>
      
  </table> 
  
  <div id="buttons" >
  	    <input type="hidden" name="scheduleName" value="<s:property value="scheduleName"/>">
 			    <input type="hidden" name="userId" value="<s:property value="userId"/>">
        <input type="submit" class="standardButton" name="submitValidate" value="<s:text name="button.validate"/>">
        <input type="submit" class="standardButton" name="submitScheduledReport" value="<s:text name="button.submit"/>">
  </div>
  	<br>      
  
  
  <s:set name="parameters" value="reportParameters" scope="request" /> 

  <div class="button-bar">
	  <display:table name="scriptVariables" class="displayTag" defaultsort="1" sort="list"  >
	  
	    <display:column property="source" title="Source"/>                    
	    <display:column property="name" title="Name" />                    
	    <display:column property="valueAsText" title="Value"/>                    
	    <display:column property="exampleValueAsText" title="Example" />
	    <display:column property="type" title="Type" />
	    <display:caption >Available Script Variables</display:caption>
	                        
	  </display:table>
  
  </div>
  
  

 
  
  </form>    
   
  
</div>

<s:include value="Footer.jsp" />

