<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />
<s:head/>

<style type="text/css">
   table {margin: 0px;}
   table td {padding: 0px;}
   table.dialog {margin: 10px;}
   table.dialog td {padding: 3px;}   
</style>

<script language="JavaScript" type="text/JavaScript">

function setDefaultExportType()
{
	if (chapterForm.exportType.length == 0)
  {
		chapterForm.exportType[0].checked=true
 	}
 	else
 	{
 		chapterForm.exportType.checked=true
 	}
}

function setBlankTarget()
{	
	optionsForm.target="_blank";		
}

function setNoTarget()
{
	optionsForm.target="";		
}
</script>

<br/>

<a class="back-link" href="listChapters.action?id=<s:property value="bookId"/>"><s:text name="link.back.reports"/></a>
  
<br/>

<div align="center">  

	<div class="important img-report" id="instructions" style="width: 70%;">
	  <s:if test="chapterName == null">
	  	<s:text name="addChapter.title"/>
	  </s:if>
	  <s:else>
	  	<s:text name="editChapter.title"/>
	  </s:else>
	  <s:property value="reportBook.name"/>
	</div>

  <form action="editChapter.action" name="chapterForm" method="post" class="dialog-form" style="width: 75%;" >  
  
  <table class="dialog"> 
  
    <tr class="a">
      <td class="boldText"><s:text name="label.report"/>*</td>
      <td>
        &nbsp;
      </td>
      <td>
			  <select name="reportId" onchange="submit()">
					<option value="-1">(None)</option>   
						<s:iterator id="report" value="reports">
					    <option value="<s:property value="id"/>" <s:if test="id == [0].getReport().getId()">selected="selected"</s:if> /><s:property value="name"/>
				    </s:iterator>
			  </select>		
			  <input type="hidden" name="bookId" value="<s:property value="bookId"/>"> 
			  <input type="hidden" name="chapterId" value="<s:property value="chapter.id"/>">
      </td>
    </tr>
    
    <s:if test="chapterName != null">
    <tr class="b">
      <td class="boldText"><s:text name="label.name"/>*</td>
      <td>
        &nbsp;
      </td>
      <td>
        <input type="text" size="80" name="chapterName" value="<s:property value="chapterName"/>">
      </td>
    </tr>
    <tr class="b">
      <td class="boldText"><s:text name="label.exportTypes"/>*</td>   
      <td>
        &nbsp;
      </td>
      <td>
      <s:if test="report.pdfExportEnabled">
       <input type="radio" name="exportType" value="0" <s:if test="exportType == \"0\"">checked</s:if> >PDF
      </s:if>
      <s:if test="report.htmlExportEnabled">      
        <input type="radio" name="exportType" value="2" <s:if test="exportType == \"2\"">checked</s:if> >HTML
      </s:if>
      <s:if test="report.csvExportEnabled">   
        <input type="radio" name="exportType" value="3" <s:if test="exportType == \"3\"">checked</s:if> >CSV      
      </s:if>
      <s:if test="report.xlsExportEnabled || report.jXLSReport">      
        <input type="radio" name="exportType" value="1" <s:if test="exportType == \"1\"">checked</s:if> >XLS
      </s:if>
      <s:if test="report.rtfExportEnabled">   
        <input type="radio" name="exportType" value="5" <s:if test="exportType == \"5\"">checked</s:if> >RTF
      </s:if>
      <s:if test="report.textExportEnabled">   
        <input type="radio" name="exportType" value="6" <s:if test="exportType == \"6\"">checked</s:if> >Text
      </s:if>
      <s:if test="report.excelExportEnabled">        
        <input type="radio" name="exportType" value="7" <s:if test="exportType == \"7\"">checked</s:if> >Excel
      </s:if>
      <s:if test="report.jasperReport && report.imageExportEnabled">       
        <input type="radio" name="exportType" value="4" <s:if test="exportType == \"4\"">checked</s:if> >Image
      </s:if>
      <script language="JavaScript" type="text/JavaScript">
        setDefaultExportType()
	  	</script>        
      </td>
      <td>
        &nbsp;
      </td>
    </tr>

    <tr><td colspan="4"><hr/></td></tr> 
    <tr><td colspan="3">&nbsp;</td><td><img src="images/lock.gif" alt="Select parameters that you wish to lock (ie., keep from being overridden from the run page)."></td></tr> 
    
  	<s:iterator id="reportParameterMap" value="reportParameters">    
    <tr>
      <td class="boldText">     
      	<s:property value="reportParameter.description"/>
      </td>     
      <td>
        &nbsp;
      </td>
      <s:if test="reportParameter.type.equals('Text')">        
      <td>
        <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getParamValue(reportParameter.name)"/>" >       
      </td>
      </s:if>    
      <s:if test="reportParameter.type.equals('Date')">   
       <td> 
        <input type="text" size="80" name="<s:property value="reportParameter.name"/>" value="<s:property value="getParamValue(reportParameter.name)"/>" >       
       </td>   
      </s:if>     
      <s:if test="reportParameter.type.equals('Query') || reportParameter.type.equals('List') || reportParameter.type.equals('Boolean') ">         
      <td>       
        <select name="<s:property value="reportParameter.name"/>"  <s:if test="reportParameter.multipleSelect">size="4" multiple</s:if> >        
		  <s:if test="required && reportParameter.defaultValue == null !reportParameter.type.equals('Boolean') ">  
		    <option value="" SELECTED>(None)</option>
		  </s:if>
		  <s:iterator id="value" value="reportParameter.values">		            
            <option value="<s:property value="id"/>" <s:if test="getParamValue(reportParameter.name) == id">SELECTED</s:if> ><s:property value="description"/></option>
          </s:iterator>
        </select>
      </td>
      </s:if>
      <td>
		<input type="checkbox" name="<s:property value="reportParameter.name"/>_lock" value="true" <s:if test="isLocked(reportParameter.name)">checked</s:if>>        
      </td>
    </tr>      
    </s:iterator>    
    
    </s:if>

  </table>  
    
  <s:if test="chapterName != null">
	  <div class="button-bar" id="buttons" >
	       <input class="standardButton" type="submit" name="submitSave" value="<s:text name="button.save"/>">
	  </div>
  </s:if>
    
  </form>  
  
  <br/>
  
  <div class="error">
  	<s:actionerror/>
  </div> 
  
  <div class="message">
    <s:actionmessage/>
  </div> 
  
 
</div>

<s:include value="Footer.jsp" />
