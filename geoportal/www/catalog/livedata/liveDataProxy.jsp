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
<% RendererFactories factories = new RendererFactories(
  request.getContextPath(),
  "/proxy",
  "/catalog/livedata/kmzBridge.jsp");
String url = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(com.esri.gpt.framework.util.Val.chkStr(request.getParameter("url")));
CredentialProvider cp = extractCredentialProvider(request);
if (cp != null) {
  CredentialsMap cm = CredentialsMap.extract(request);
  cm.put(url, cp);
}
IRenderer renderer = factories.select(url, cp);

if (renderer == null) {
  response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
  return;
}

ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
PrintWriter printWriter = new PrintWriter(byteArrayOut);
renderer.render(printWriter);
printWriter.close();

String content = byteArrayOut.toString("UTF-8");

out.print(content);
%> 

<%!
  private CredentialProvider extractCredentialProvider(HttpServletRequest request) {
    String cpParam = request.getHeader("GPT-livedata");
    if (cpParam != null) {
      try {
        cpParam = Base64.decode(cpParam, "UTF-8");
        cpParam = URLDecoder.decode(cpParam, "UTF-8");
        String[] cp = cpParam.split(",");
        if (cp.length == 3) {
          if (Val.chkInt(cp[2], 0) >= 3) {
            return null;
          }
          return new CredentialProvider(cp[0], cp[1]);
        }
      } catch (IOException ex) {
      }
    }
    return null;
  }

%>

<%@page import="com.esri.gpt.framework.context.CredentialsMap"%>
<% // proxy.jsp - Serves as a proxy for the ArcGIS Server Javascript API %>
<%@page session="false"%>
<%@page import="java.net.*,java.io.*,com.esri.gpt.control.livedata.*" %>
<%@page import="com.esri.gpt.framework.http.CredentialProvider" %>
<%@page import="com.esri.gpt.framework.security.codec.Base64" %>
<%@page import="com.esri.gpt.framework.util.Val" %>
<%@page import="java.net.URLDecoder" %>
<%@page pageEncoding="UTF8" contentType="text/javascript" %>
