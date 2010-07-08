<%@ taglib prefix="s" uri="/struts-tags" %>

<s:include value="Banner.jsp" />

<script type="text/javascript" src="js/yui-ext/yui-ext-core.js"></script>
<script type="text/javascript" src="js/add-to-user.js"></script>

<script type="text/javascript">
  var tags = [<s:property escape="false" value="tagList"/>];            
</script>

<a class="back-link img-books-small" href="listBooks.action"><s:text name="link.back.books"/></a>
 
<br/>

<div align="center">  
  
  <div class="important img-book" id="instructions" style="width: 70%;">
  	   <s:if test="command.equals('add')">
	   	<s:text name="editBook.addBook"/>
	   </s:if>
	   <s:if test="!command.equals('add')">
	 	<s:text name="editBook.selectedBook"/> <s:property value="name"/>
	   </s:if> 
  </div>	  
  
  <form action="editBook.action" class="dialog-form" style="width: 75%;">
  
  <table class="dialog" >    
    <tr class="a">
      <td class="boldText" width="20%"><s:text name="label.name"/></td>
      <td colspan="2"><input type="text" size="60" name="name" value="<s:property value="name"/>"></td>
    </tr>
    <tr class="a">
      <td class="boldText" width="20%"><s:text name="label.outputPath"/></td>
      <td colspan="2"><input type="text" size="60" name="outputPath" value="<s:property value="outputPath"/>"></td>
    </tr>
    <tr>
      <td class="boldText"><s:text name="label.tags"/></td>
      <td colspan="2"><input type="text" size="60" name="tags" value="<s:property value="tags"/>"></td>
    </tr>
    <tr>
      <td class="boldText" valign="top"><s:text name="label.groups"/></td>
      <td>
      	<ul id="currentGroups" class="checklist" style="height:12em;width:24em;">
      		<s:iterator id="reportGroup" value="reportGroupsForBook">      	   
      		<li>
      			 <input type="checkbox" name="groupIds" value="<s:property value="id"/>" CHECKED>
			      <s:property value="name"/>
			 </li>
			 </s:iterator>
		</ul>
	  </td>
      <td valign="top">
        </ul><input class="standardButton" type="button" id="showAddGroup" value="<s:text name="button.addGroup"/>">
      </td>
	</tr>    
  </table>
   
  <div class="button-bar" id="buttons" >
       <input class="standardButton" type="submit" name="submitOk" value="<s:text name="button.save"/>">
       <input class="standardButton" type="submit" name="submitDuplicate" value="<s:text name="button.duplicate"/>">
  </div>
    
  <input type="hidden" name="id" value="<s:property value="id"/>">
  <input type="hidden" name="command" value="<s:property value="command"/>">    
  
  </form>  

  <div class="error">
  	<s:actionerror/>
  </div> 
 
</div>

<div id="addGroupDialog">
  <div class="hd">Add Group to Book</div>
  <div class="bd">  
    <form  >  
    <table class="dialog"  >    
      <tr>
        <td class="boldText" width="20%"><s:text name="label.filterBy"/></td>
        <td>
            <input type="radio" id="filterByName" name="filterType" value="name" CHECKED>Name 
            <input type="radio" id="filterByTag" name="filterType" value="tag">Tag
        </td>
      </tr> 
      <tr>
        <td class="boldText" width="20%"><s:text name="label.filter"/></td>
        <td><input type="text" id="filter" size="35" ></td>
      </tr>  
      <tr>
        <td valign="top" class="boldText"><s:text name="label.groups"/></td>
        <td>
          <select id="availableGroups" class="checklist" size="16" style="width: 20em;" multiple>        
          <s:iterator id="reportGroup" value="reportGroups">            
            <s:set name="available" value="true"/>    
            <s:iterator id="reportGroupForBook" value="reportGroupsForBook">     
              <s:if test="#reportGroup.id == #reportGroupForBook.id">
                <s:set name="available" value="false"/>  
              </s:if>
            </s:iterator>
            <s:if test="#available"> 
              <option id="<s:property value="id"/>|$!action.getTags($reportGroup.id)" value="<s:property value="id"/>"><s:property value="name"/></option>
            </s:if>
           </s:iterator>            
          </select>          
        </td>
      </tr>
    </table>
    </form>
  </div>  
</div>
  
<s:include value="Footer.jsp" />

