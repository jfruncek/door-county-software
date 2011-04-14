<%@ taglib prefix="s" uri="/struts-tags" %>

<s:include value="Banner.jsp" />


<script type="text/javascript">
  var settingsTabs = new YAHOO.widget.TabView("settingsTabs");
</script>

<div align="center">
   
  <br>
  
  <div id="settingsTabs" class="yui-navset" style="width: 800px;">
  <ul class="yui-nav">
    <li class="selected"><a href="settings"><em><s:text name="editProperties.tab.generalSettings"/></em></a></li>    
    <li><a href="cleanup"><em><s:text name="editProperties.tab.tempFileCleanup"/></em></a></li>    
  </ul>            
  <div class="yui-content">
  
    <div id="settings"> 

    <s:actionerror/>
  
  <form action="editProperties.action" method="post">
  <table class="dialog" >   
    <tr>
      <td class="boldText"><s:text name="label.baseDirectory"/></td>
      <td>      
      	<img id="baseDir" src="images/help.gif" title="<s:text name="tooltip.properties.baseDirectory"/>">      
      </td>
      <td>
		<input type="text" size="60" name="baseDirectory" value="<s:property value="baseDirectory"/>"><br/>
	  </td>
	</tr>	
	 <tr>
      <td class="boldText"><s:text name="label.reportGenerationDirectory"/></td>
      <td>      
      	<img id="reportGenDir" src="images/help.gif" title="<s:text name="tooltip.properties.reportGenerationDirectory"/>">   
      </td>
      <td>
		<input type="text" size="60" name="reportGenerationDirectory" value="<s:property value="reportGenerationDirectory"/>"><br/>
	  </td>
	</tr>	
    <tr>
      <td class="boldText"><s:text name="label.tempDirectory"/></td>      
      <td>
      	<img id="tempDir" src="images/help.gif" title="<s:text name="tooltip.properties.tempDirectory"/>">   
      </td>
      <td>
		<input type="text" size="60" name="tempDirectory" value="<s:property value="tempDirectory"/>"><br/>
	  </td>
	</tr>	
    <tr>
      <td class="boldText"><s:text name="label.dateFormat"/></td>
      <td>
      	&nbsp;
      </td>
      <td><input type="text" size="60" name="dateFormat" value="<s:property value="dateFormat"/>"></td>
    </tr>    
    <tr>
      <td class="boldText"><s:text name="label.queryReportMaxRows"/></td>
      <td>
      	<img id="maxrows" src="images/help.gif" title="<s:text name="tooltip.properties.maxRows"/>">   
      </td>
      <td><input type="text" size="60" name="maxRows" value="<s:property value="maxRows"/>"></td>
    </tr>
    
    <tr>
      <td class="boldText"><s:text name="label.queryReportMaxRowsSortable"/></td>
      <td>
        <img id="queryReportMaxRowsSortable" src="images/help.gif" title="<s:text name="tooltip.properties.queryReportMaxRowsSortable"/>">   
      </td>
      <td><input type="text" size="60" name="queryReportMaxRowsSortable" value="<s:property value="queryReportMaxRowsSortable"/>"></td>
    </tr>  
      
    <tr>
      <td class="boldText"><s:text name="label.queryReportCacheBlockSize"/></td>
      <td>
        <img id="queryReportCacheBlockSize" src="images/help.gif" title="<s:text name="tooltip.properties.queryReportCacheBlockSize"/>">   
      </td>
      <td><input type="text" size="60" name="queryReportCacheBlockSize" value="<s:property value="queryReportCacheBlockSize"/>"></td>
    </tr>  
    
    <tr><td colspan="3"><hr/></td></tr>  
	<tr>
      <td class="boldText"><s:text name="label.mailHost"/></td>
      <td>
      	<img id="mailhost" src="images/help.gif" title="<s:text name="tooltip.properties.mailhost"/>">   
      </td>
      <td>
		<input type="text" size="60" name="mailHost" value="<s:property value="mailHost"/>">		
	  </td>
    </tr>
    <tr>
      <td class="boldText"><s:text name="label.useMailAuthenticator"/></td>
      <td>
      	<img id="mailauthenticator" src="images/help.gif" title="<s:text name="tooltip.properties.mailAuthenticator"/>">   
      </td>
      <td>
		<s:checkbox name="mailAuthenticatorUsed" fieldValue="true" theme="simple"/> 
	  </td>
    </tr>
	<tr>
      <td class="boldText"><s:text name="label.mailAuthenticatorUser"/></td>
      <td>
      	&nbsp;
      </td>
      <td><input type="text" size="60" name="mailUser" value="<s:property value="mailUser"/>"></td>
    </tr>
	<tr>
      <td class="boldText"><s:text name="label.mailAuthenticatorPassword"/></td>
      <td>
      	&nbsp;
      </td>
      <td><input type="password" size="60" name="mailPassword" value="<s:property value="mailPassword"/>"></td>
    </tr>     
    <tr>
      <td class="boldText"><s:text name="label.mailReplyTo"/></td>
      <td>
        &nbsp;
      </td>
      <td><input type="text" size="60" name="mailReplyTo" value="<s:property value="mailReplyTo"/>"></td>
    </tr>
    
    <tr><td colspan="3"><hr/></td></tr>

    <tr>
      <td class="boldText"><s:text name="label.runStatusInactivityLogging" /></td>
      <td><img id="runStatusInactivityLogging" src="images/help.gif"
        title="<s:text name="tooltip.properties.runStatusInactivityLogging"/>"></td>
      <td><input type="text" size="60" name="runStatusInactivityLogging"
        value="<s:property value="runStatusInactivityLogging"/>"></td>
    </tr>

    <tr>
      <td class="boldText"><s:text name="label.runStatusInactivityWarning" /></td>
      <td><img id="runStatusInactivityWarning" src="images/help.gif"
        title="<s:text name="tooltip.properties.runStatusInactivityWarning"/>"></td>
      <td><input type="text" size="60" name="runStatusInactivityWarning"
        value="<s:property value="runStatusInactivityWarning"/>"></td>
    </tr>

    <tr>
      <td class="boldText"><s:text name="label.runStatusInactivityTimeout" /></td>
      <td><img id="runStatusInactivityTimeout" src="images/help.gif"
        title="<s:text name="tooltip.properties.runStatusInactivityTimeout"/>"></td>
      <td><input type="text" size="60" name="runStatusInactivityTimeout"
        value="<s:property value="runStatusInactivityTimeout"/>"></td>
    </tr>

    <tr><td colspan="3"><hr/></td></tr>
    <tr>
      <td class="boldText"><s:text name="label.xmlaURI"/></td>
      <td>
        &nbsp;
      </td>
      <td><input type="text" size="60" name="xmlaUri" value="<s:property value="xmlaUri"/>"></td>
    </tr>     
    <tr>
      <td class="boldText"><s:text name="label.xmlaDataSource"/></td>
      <td>
        &nbsp;
      </td>
      <td><input type="text" size="60" name="xmlaDataSource" value="<s:property value="xmlaDataSource"/>"></td>
    </tr>     
    <tr>
      <td class="boldText"><s:text name="label.xmlaCatalog"/></td>
      <td>
        &nbsp;
      </td>
      <td><input type="text" size="60" name="xmlaCatalog" value="<s:property value="xmlaCatalog"/>"></td>
    </tr>     
    <tr><td colspan="4"><hr></td></tr>
    
    <tr>
      <td class="boldText"><s:text name="label.isProductServer"/></td>
      <td>
        &nbsp;
      </td>
      <td>
          <s:checkbox name="ProductionServer" disabled="true" theme="simple"/> 
      </td>
    </tr>     

    <tr>
      <td class="boldText"><s:text name="label.developmentEmail"/></td>
      <td>
        &nbsp;
      </td>
      <td><s:property value="developmentEmail"/></td>
    </tr>     
    <tr>
      <td class="boldText"><s:text name="label.developmentFileRoot"/></td>
      <td>
        &nbsp;
      </td>
      <td><s:property value="developmentFileRoot"/></td>
    </tr>     

    <tr><td colspan="4"><hr></td></tr>
     
    <tr>
      <td align="center" class="dialogButtons" colspan="3">      	
      	<input class="standardButton" type="submit" name="submitType" value="<s:text name="button.save"/>">     	
      </td>
    </tr>    
   </table>
  </form> 
  
</div>

<div id="cleanup">

<form action="imageCleanup.action" method="post">
  <table class="dialog" >   
    <tr>
      <td class="boldText"><s:text name="label.tempDirectory"/></td>
      <td><s:property value="tempDirectory"/></td>
    </tr>
    <tr>
      <td class="boldText"><s:text name="label.numberOfFiles"/></td>
      <td><s:property value="numberOfFiles"/></td>
    </tr>
    <tr>
      <td class="boldText"><s:text name="label.directorySize"/></td>
      <td><s:property value="directorySize"/></td>
	</tr>	    
    <tr><td colspan="2"><hr></td></tr> 
    <tr>
      <td align="center" class="dialogButtons" colspan="2">      	
      	<input class="standardButton" type="submit" name="submitType" value="<s:text name="button.cleanup"/>">     	
      </td>
    </tr>    
   </table>
  </form> 
  
</div>

</div>

</div>

<s:include value="Footer.jsp" />

<script type="text/javascript">
  var baseDirTooltip = new YAHOO.widget.Tooltip("baseDirTooltip", { context:"baseDir" } );
  var reportGenDirTooltip = new YAHOO.widget.Tooltip("reportGenDirTooltip", { context:"reportGenDir" } );
  var tempDirTooltip = new YAHOO.widget.Tooltip("tempDirTooltip", { context:"tempDir" } );
  var maxrowsTooltip = new YAHOO.widget.Tooltip("maxrowsTooltip", { context:"maxrows" } );  
  var mailhostTooltip = new YAHOO.widget.Tooltip("mailhostTooltip", { context:"mailhost" } );
  var mailauthenticatorTooltip = new YAHOO.widget.Tooltip("mailauthenticatorTooltip", { context:"mailauthenticator" } );
</script>

