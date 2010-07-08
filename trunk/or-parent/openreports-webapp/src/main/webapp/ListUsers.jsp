<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<s:include value="Banner.jsp" />

<s:actionerror/> 

<br/>

<div align="center">
  
      <div class="instructions" id="instructions">
  		<a href="editUser.action?command=add">
  			<img border="0" src="images/add.gif"/><s:text name="link.admin.addUser"/>
  		</a>
  		<a href="reportAdmin.action?command=add">
  			<img border="0" src="images/back.gif"/> <s:text name="link.back.admin"/>
  		</a>
  	  </div>
  	  
  <br/>

  <div class="filterLabel" align="left" >
    <script type="text/javascript">
      addLoadEvent(function() { setFilterFromCookie("ListUsersTable", 0); });
    </script>
    <form name="filterForm" class="instructions"><span class="filterLabel">Filter:</span>
      <input name="filter" onkeyup="filterTable(this.value, 'ListUsersTable', 0)"
        type="text"/>&nbsp;<span id="ListUsersTableFilterInfo"></span>
    </form>
  </div>

  <s:set name="users" value="users" scope="request" />
  
   
  <display:table id="ListUsersTable" name="users" class="displayTag" sort="list" requestURI="listUsers.action" decorator="org.efs.openreports.util.HRefColumnDecorator">  	      
    <display:column property="name" href="editUser.action?command=edit" paramId="id" paramProperty="id" titleKey="label.name" sortable="true" headerClass="sortable"/>
    <display:column property="removeLink" title="" href="deleteUser.action" paramId="id" paramProperty="id"/>  	     	     
  </display:table>  
  <br>  
</div>

<s:include value="Footer.jsp" /> 

