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
<%// resultsBody.jsp - Create searchResults(JSF body)%>
<%// Used to inject results using ajax %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<html>
<body>
<f:view>
  <f:loadBundle basename="gpt.resources.gpt" var="gptMsg"/>
  <h:panelGroup id="rsltsCmPlPgpGptMessages">
        <h:messages id="rsltsCmPlMsgsPageMessages" layout="list" 
          infoClass="successMessage" 
          errorClass="errorMessage"/>
  </h:panelGroup>
  
  <!-- Form element stripped out on the searchcriteria page.  This
  form is here so that JSF can generate elements to much the
  names expected on the search criteria page. -->
  <h:form id="frmSearchCriteria">
 
    
      <jsp:include page="/catalog/search/results.jsp" />
    
  
  </h:form>
</f:view>
</body>
</html>