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
<%//cswProxy.jsp - Gets the record by Id%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<f:verbatim>
<script type="text/javascript">
function cpChangePage() {
  if(typeof(_cpRecordUrl) == 'string' && _cpRecordUrl.length > 2) {
    window.location = _cpRecordUrl;
  }
}

function cpLoad (eventFunc) {
  var loadFunction = window.onload;

  window.onload = function() {
    if(typeof loadFunction == 'function') {
      loadFunction();
    }
    if(typeof eventFunc == 'function') {
      eventFunc();
    }
  }
}

cpLoad(cpChangePage);


</script>
</f:verbatim>

 <gpt:jscriptVariable 
   quoted="" value="#{SearchController.fullMetadataUrl}" 
   variableName="_cpRecordUrl" 
   id="_cpRrecordUrl">
 </gpt:jscriptVariable>
 
 
