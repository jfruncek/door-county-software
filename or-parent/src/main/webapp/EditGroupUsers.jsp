<%@ taglib prefix="s" uri="/struts-tags" %>

<s:include value="Banner.jsp" />

<s:actionerror/>

<a class="back-link img-report-small" href="listGroups.action"><s:text name="link.back.groups"/></a>
 
<br/><br/>

<div align="center">   

	<div class="important img-group" id="instructions" style="width: 70%;"><s:text name="editGroupUsers.title"/> <s:property value="group.name"/></div></td>
  
  <div class="filterLabel-dialog" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setCheckableItemFilterFromCookie("EditGroupUsersTable", 0); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter:</span>
      <input name="filter" onkeyup="filterCheckableItemTable(this.value, 'EditGroupUsersTable', 0)"
        type="text"/>&nbsp;<span id="EditGroupUsersTableFilterInfo"></span>
    </form>
  </div>
  
 
  <form name="editUsersForm" action="editGroupUsers.action" class="dialog-form" style="width: 75%;">  
  
  <br>
  
  
  
  <table id="EditGroupUsersTable" class="dialog">
    <s:iterator id="user" value="users">
    <tr class="a">
      <td class="boldText" width="90%" ><s:property value="name"/></td>
      <td width="10%">
        <input type="checkbox" name="userIds" value="<s:property value="id"/>"
          <s:iterator id="userForGroup" value="usersForGroup">         
            <s:if test="#user.id == #userForGroup.id">
              CHECKED
            </s:if>
          </s:iterator>
        >
      </td>
    </tr>
    </s:iterator>  
    </table>
    
    <br>    
   
    <div class="button-bar" id="buttons">
    	<input class="standardButton" type="submit" name="submitType" value="<s:text name="button.save"/>">
    </div>
    
    <input type="hidden" name="id" value="<s:property value="id"/>">
  
  	</form>
  	
  <br/> 
</div>

<s:include value="Footer.jsp" /> 
