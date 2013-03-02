<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"  %>

<f:subview id="svSecondaryNavigation">
   <f:subview id="svScript">
       <jsp:include page="/catalog/tc/suite_script.jsp"/>
   </f:subview>

	<h:form id="frmSecondaryNavigation"
	            rendered="#{(PageContext.tabStyleMap['catalog.publication']!='' or
	                PageContext.tabStyleMap['catalog.manage.user.role']!='' or
	                PageContext.tabStyleMap['catalog.harvest']!='' or 
	                PageContext.tabStyleMap['catalog.sdisuite']!='') and
	                PageContext.roleMap['gptPublisher']}">
	
	  <h:commandLink action="catalog.publication.manageMetadata" 
	    id="publicationManageMetadata" 
	    value="#{gptMsg['catalog.publication.manageMetadata.subMenuCaption']}" 
	    styleClass="#{PageContext.menuStyleMap['catalog.publication.manageMetadata']}"
	    actionListener="#{ManageMetadataController.processAction}"/>
	
	  <h:commandLink action="catalog.publication.addMetadata"
	    id="publicationAddMetadata"
	    value="#{gptMsg['catalog.publication.addMetadata.subMenuCaption']}"
	    styleClass="#{PageContext.menuStyleMap['catalog.publication.addMetadata']}"/>
	    
	  <h:commandLink action="catalog.publication.manage.user.role"
	    id="publicationManageUserRole"
	    rendered="#{PageContext.roleMap['gptAdministrator'] and PageContext.manageUser}"
	    value="#{gptMsg['catalog.publication.manage.user.role.subMenuCaption']}"
	    styleClass="#{PageContext.menuStyleMap['catalog.publication.manage.user.role']}"/>
	 	    
	  <f:subview id="svLinks">
	            <jsp:include page="/catalog/tc/suite_links.jsp"/>
	  </f:subview>
	  
	 </h:form>
   <f:subview id="svForms" rendered="#{(PageContext.tabStyleMap['catalog.publication']!='' or
	                PageContext.tabStyleMap['catalog.harvest']!='' or 
	                PageContext.tabStyleMap['catalog.sdisuite']!='') and
	                PageContext.roleMap['gptPublisher']}">
       <jsp:include page="/catalog/tc/suite_forms.jsp"/>
   </f:subview>
</f:subview>
