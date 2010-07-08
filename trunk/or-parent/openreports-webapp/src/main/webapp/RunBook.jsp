<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />
<s:head/>
<s:if test="'bookRun'.equals(actionName)">
	<a class="back-link img-group-small" href="reportGroup.action"><s:text name="link.back.groups"/></a>	
</s:if>
<s:elseif test="'runBook'.equals(actionName)">
	<a class="back-link img-books-small" href="listBooks.action"><s:text name="link.back.books"/></a>
</s:elseif>
 
<br/>

<div align="center">  

    <s:set name="chapters" value="chapters" scope="request" />
	
	<s:if test="chapters.size == 0"><s:text name="runBook.message.noBooks"/><s:property value="reportBook.name"/>
	</s:if>
	<s:else>
	
	<div class="important img-book" id="instructions" style="width: 70%;"><s:text name="runBook.title"/>
	  <s:property value="reportBook.name"/>
	</div>

	<form action="<s:if test="'bookRun'.equals(actionName)">bookRun.action</s:if><s:else>runBook.action</s:else>" 
	      name="runBook" method="post" class="dialog-form" style="width: 75%;" >  

	 <table class="dialog">
	 	<tr>
	 		<td colspan="2"><s:text name="runBook.message.description"/></td>
	 	</tr> 
	 	<tr>
	 		<td colspan="2"><pre><s:property value="outputPath" /></pre></td>
	 	</tr> 
	  <s:if test="commonReportParameters.size > 0">   
  		<tr>
	 		<td colspan="2"><s:text name="runBook.message.override"/></td>
	 	</tr> 
	 	</s:if>
	 	<s:iterator id="reportParameterMap" value="commonReportParameters">    
		<tr>
	     <td class="boldText">     
	     	<s:property value="reportParameter.description"/>
	     </td>     
	     <s:if test="reportParameter.type.equals('Text')">        
	     <td>
	       <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getCommonParamValue(reportParameter.name)"/>" >       
	     </td>
	     </s:if>    
	     <s:if test="reportParameter.type.equals('Date')">   
	      <td> 
	       <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getCommonParamValue(reportParameter.name)"/>" >       
	      </td>   
	     </s:if>     
	     <s:if test="reportParameter.type.equals('Query') || reportParameter.type.equals('List') || reportParameter.type.equals('Boolean') ">         
	     <td>       
	       <select name="<s:property value="reportParameter.name"/>"  <s:if test="reportParameter.multipleSelect">size="4" multiple</s:if> >        
					  <option value="" SELECTED>(None)</option>
						<s:iterator id="value" value="reportParameter.values">		            
						  <option value="<s:property value="id"/>"><s:property value="description"/></option>
						</s:iterator>
	       </select>
	     </td>
	     </s:if>
	   </tr>      
	   </s:iterator>     
	  <s:if test="otherReportParameters.size > 0">   
	 	<tr>
	 		<td colspan="2"><s:text name="runBook.message.overrideOther"/></td>
	 	</tr> 
	 	</s:if>
	 	<s:iterator id="reportParameterMap" value="otherReportParameters">    
		<tr id="otherParameters">
	     <td class="boldText">     
	     	<s:property value="reportParameter.description"/> (<s:property value="report.name"/>)
	     </td>     
	     <s:if test="reportParameter.type.equals('Text')">        
	     <td>
	       <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getOtherParamValue(reportParameter.name)"/>" >       
	     </td>
	     </s:if>    
	     <s:if test="reportParameter.type.equals('Date')">   
	      <td> 
	       <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getOtherParamValue(reportParameter.name)"/>" >       
	      </td>   
	     </s:if>     
	     <s:if test="reportParameter.type.equals('Query') || reportParameter.type.equals('List') || reportParameter.type.equals('Boolean') ">         
	     <td>       
	       <select name="<s:property value="reportParameter.name"/>"  <s:if test="reportParameter.multipleSelect">size="4" multiple</s:if> >        
					  <option value="" SELECTED>(None)</option>
						<s:iterator id="value" value="reportParameter.values">		            
						  <option value="<s:property value="id"/>"><s:property value="description"/></option>
						</s:iterator>
	       </select>
	     </td>
	     </s:if>
	   </tr>      
	   </s:iterator>       
	   
	   <s:if test="! ''.equals(changedReports)">
	 	<tr>
	 	  <td colspan="2" class="warning img-warning">
	 	    <s:text name="runBook.message.warning"/>
		  </td>
	 	</tr> 
	 	<tr>
	 	  <td colspan="2" class="warning">
	 	    <s:property value="changedReports"/>
	 	</tr>
	   </s:if>
	</table>
	
	<div class="button-bar" id="buttons" >
	     <input class="standardButton" type="submit" name="submitRun" value="<s:text name="button.run"/>">
	     <input type="hidden" name="id" value="<s:property value="id"/>">
	</div>

	</form>   

	<br/>
	<display:table name="chapters" class="displayTag" sort="list" requestURI="bookRun.action" 
	               decorator="org.efs.openreports.util.ChapterDecorator"> 	      
	  <display:column property="dynamicChapterName" titleKey="label.name" sortable="true"/>
  	<display:column property="report.name" titleKey="label.report" sortable="true" />
  	<display:column property="exportTypeName" titleKey="label.type" sortable="true" />
    <display:footer><tr><td colspan="3"><s:property value="chapters.size"/> Reports</td></tr></display:footer>  	     
	</display:table>    

	</s:else>

 	<div class="error">
 		<s:actionerror/>
 	</div> 
 
</div>
    
<s:include value="Footer.jsp" />
