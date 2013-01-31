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
<%@page import="com.esri.ontology.service.catalog.Context"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%
    Context context = Context.extract(request.getSession().getServletContext());
    String queryUrl = request.getRequestURL().toString().replaceAll("/[^/]+$", "/") + "query?";
%>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Ontology Service</title>
    <link rel="stylesheet" type="text/css" href="styles/main.css"/>
    <script type="text/javascript">
      var url = "";

      function evaluateQueryUrl() {
        var query = "";
        var term = encodeURI(document.getElementById("term").value.replace(/^[ \t]+|[ \t]+$/g, ""));
        if (term.length>0) {
          query = document.getElementById("queryUrl").value;
          query += "term="+term;
          if (document.getElementById("categories").checked || document.getElementById("neighbors").checked) {
            if (document.getElementById("categories").checked && document.getElementById("neighbors").checked) {
              //query += "&selection=all";
            } else if (document.getElementById("categories").checked) {
              query += "&selection=categories";
            } else if (document.getElementById("neighbors").checked) {
              query += "&selection=neighbors";
            }
          }
          if (document.getElementById("lucene").checked || document.getElementById("owl").checked) {
            if (document.getElementById("lucene").checked && document.getElementById("owl").checked) {
              query += "&f=all";
            } else if (document.getElementById("lucene").checked) {
              //query += "&f=lucene";
            } else if (document.getElementById("owl").checked) {
              query += "&f=owl";
            }
          }

          var subclassof = eval(document.getElementById("subclassof").value.replace(/^[ \t]+|[ \t]+$/g, ""));
          if (isNaN(subclassof)) {
            subclassof = 2.0;
          }
          if (subclassof!=2.0) {
            query += "&subclassof=" + subclassof;
          }

          var seealso = eval(document.getElementById("seealso").value.replace(/^[ \t]+|[ \t]+$/g, ""));
          if (isNaN(seealso)) {
            seealso = 2.0;
          }
          if (seealso!=2.0) {
            query += "&seealso=" + seealso;
          }

          var level = eval(document.getElementById("level").value.replace(/^[ \t]+|[ \t]+$/g, ""));
          if (isNaN(level)) {
            level = 1;
          }
          if (level!=1) {
            query += "&level=" + level;
          }

          var threshold = eval(document.getElementById("threshold").value.replace(/^[ \t]+|[ \t]+$/g, ""));
          if (isNaN(threshold)) {
            threshold = 0;
          }
          if (threshold!=0) {
            query += "&threshold=" + threshold;
          }
        }
        return query;
      }

      function update() {
        url = evaluateQueryUrl();
        var rest = document.getElementById("rest");
        rest.href = url;
        rest.innerHTML = url;
      }

      function onSubmit() {
        location.href = url;
      }
    </script>
  </head>
  <body onload="update()">
    <input type="hidden" value="<%=queryUrl%>" id="queryUrl"></input>
    <table class="header" width="100%">
      <tr>
        <td>Ontology Service</td>
        <td align="right" valign="top" class="status">
          <% if (context.isReady()) { %>
            <div class="ready">Status: ready</div>
          <% } else { %>
            <div class="notready">Status: not ready yet!</div>
          <% } %>
        </td>
      </tr>
    </table>
    <table class="form">
      <tr>
        <td>Search term:</td>
        <td><input id="term" onkeyup="update();"/></td>
      </tr>
      <tr>
        <td>Selection:</td>
        <td>
          <input type="checkbox" id="categories" onclick="update();">categories</input>
          <input type="checkbox" id="neighbors" onclick="update();">neighbors</input>
        </td>
      </tr>
      <tr>
        <td>Format:</td>
        <td>
          <input type="checkbox" id="lucene" onclick="update();">Lucene</input>
          <input type="checkbox" id="owl" onclick="update();">OWL</input>
        </td>
      </tr>
      <tr>
        <td>SubClassOf weight:</td>
        <td>
          <input id="subclassof" onkeyup="update();"/>
        </td>
      </tr>
      <tr>
        <td>SeeAlso weight:</td>
        <td>
          <input id="seealso" onkeyup="update();"/>
        </td>
      </tr>
      <tr>
        <td>Level:</td>
        <td>
          <input id="level" onkeyup="update();"/>
        </td>
      </tr>
      <tr>
        <td>Threshold:</td>
        <td>
          <input id="threshold" onkeyup="update();"/>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><button id="submit" name="Submit" value="Submit" onclick="onSubmit();">Submit</button></td>
      </tr>
    </table>
    <table class="footer" width="100%">
      <tr>
        <td align="left">REST: <a id="rest" href=""></a></td>
      </tr>
    </table>
  </body>
</html>
