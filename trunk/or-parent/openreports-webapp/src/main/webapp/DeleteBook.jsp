<%@ taglib prefix="s" uri="/struts-tags" %>

<s:include value="Banner.jsp" />

<a class="back-link img-books-small" href="listBooks.action"><s:text name="link.back.books"/></a>
 
<br/><br/>

<div align="center">

  <div class="important img-delete" id="instructions" class="dialog-form" style="width: 70%;"><s:text name="deleteBook.title"/></div>
	   	
  <form action="deleteBook.action" style="width: 75%;">
  
  <table class="dialog" >   
    <tr class="a">
      <td class="boldText" width="20%"><s:text name="label.name"/></td>
      <td><s:property value="name"/></td>
    </tr>  
   </table>
  
   <br>
    
   <div class="button-bar" id="buttons">
        <input class="standardButton" type="submit" name="submitDelete" value="<s:text name="button.delete"/>">
        <input class="standardButton" type="submit" name="submitCancel" value="<s:text name="button.cancel"/>">
         <input type="hidden" name="id" value="<s:property value="id"/>"> 
   </div>
        
  </form>  
      
  <br> 
  
  <div class="error"><s:actionerror/></div> 
  
</div>

<s:include value="Footer.jsp" /> 