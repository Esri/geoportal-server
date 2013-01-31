<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<% // http.jsp - HTTP client utilities. %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"%>
<%@page import="com.esri.gpt.framework.http.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="javax.xml.transform.*, javax.xml.transform.stream.*"%>
<%
	String sEncoding = request.getCharacterEncoding();
	if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
	  request.setCharacterEncoding("UTF-8");
	}

  String url = chkStr(request.getParameter("url"));
  String usr = chkStr(request.getParameter("usr"));
  String pwd = chkStr(request.getParameter("pwd"));
  String method = chkStr(request.getParameter("method"));
  String requestContent = chkStr(request.getParameter("requestContent"));
  String requestContentType = chkStr(request.getParameter("requestContentType"));
  if (method.length() == 0) requestContentType = "text/xml";
  String responseStatus = "";
  String responseString = "";
  String callbackMarkup = "";
  String callbackRequestId = chkStr(request.getParameter("callbackId"));
  boolean responseAsString = false;
  boolean showHeaders = false;
  
  // execute the HTTP request
  if ((method.length() > 0) && (url.length() > 0)) {
    HttpClientRequest client = null;
    try {
      client = new HttpClientRequest();
      client.setUrl(url);
      client.setMethodName(HttpClientRequest.MethodName.valueOf(method));
      if ((usr.length() > 0) && (pwd.length() > 0)) {
        client.setCredentialProvider(new CredentialProvider(usr,pwd));
      }
      
      // configure the request content handler
      if ((method.equals("POST") || method.equals("PUT")) && (requestContent.length() > 0)) {
        if (requestContentType.length() == 0) requestContentType = "text/xml";
        String ct = requestContentType;
        if (ct.indexOf("charset") == -1) ct += "; charset=UTF-8";
        client.setContentProvider(new StringProvider(requestContent,ct));
      }
      
      // configure the response content handler
      ByteArrayHandler responseHandler = new ByteArrayHandler();
      client.setContentHandler(responseHandler);
      
      // encute the request, handle the response
      client.execute();
      boolean readCharacters = false;
      ResponseInfo responseInfo = client.getResponseInfo();
      responseStatus = "HTTP "+responseInfo.getResponseCode();
      responseStatus += ", content-type="+responseInfo.getContentType();
      responseStatus += ", charset="+responseInfo.getContentEncoding();
      if (responseInfo.getContentType() != null) {
        String ct = responseInfo.getContentType().toLowerCase();
        
        if (ct.startsWith("text/") || ct.endsWith("/xml") || ct.endsWith("+xml")) {
          readCharacters = true;
        } else if (ct.equals("application/vnd.google-earth.kmz")) {
          readCharacters = true;
          
        } else if (ct.startsWith("image/")) {
          readCharacters = false;
          clearOldCallbacks();
          String id = ""+System.currentTimeMillis();
          callbackMarkup = "<img src=\""+request.getRequestURI()+"?callbackId="+id+"\"/>";
          CALLBACKS.put(id,responseHandler.getContent());
        }
      } 
      
      // read the response as a string
      if (responseAsString || readCharacters) {
        String enc = responseInfo.getContentEncoding();
        if ((enc == null) || (enc.length() == 0)) enc = "UTF-8";
        StringHandler sh = new StringHandler();
        sh.readResponse(client,new ByteArrayInputStream(responseHandler.getContent()));
        responseString = sh.getContent();
      }

    } catch (Exception e) {
      responseStatus = "Exception";
      if ((client != null) && (client.getResponseInfo() != null)) {
        ResponseInfo responseInfo = client.getResponseInfo();
        responseStatus += " HTTP "+responseInfo.getResponseCode();
        responseStatus += ", content-type="+responseInfo.getContentType();
        responseStatus += ", charset="+responseInfo.getContentEncoding();
      }
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      responseString = sw.toString();
    } finally {
      
      if (showHeaders) {
        //client.getResponseInfo().
      }
    }
  }
  
  // write response bytes on a callback
  if (callbackRequestId.length() > 0) {
    ServletOutputStream sos = null;
    try {
	    response.reset();
	    out.clear();
	    sos = response.getOutputStream();
	    byte[] bytes = CALLBACKS.get(callbackRequestId);
	    if (bytes != null) {
	      sos.write(bytes);
	    }
      return;
    } finally {
      try {
        if (sos != null) sos.flush();
      } catch (Exception ef) {
        ef.printStackTrace(System.err);
      }
    }
  }
  
%>

<%!

  private static HashMap<String,byte[]> CALLBACKS = new HashMap<String,byte[]>();

  // Checks a string value.
	private String chkStr(String s) {
	  if (s == null) return "";
	  else return s.trim();
	}
	
  // clear old callback responses (anything older than 2 minutes)
  private void clearOldCallbacks() {
    long tNow = System.currentTimeMillis();
    ArrayList<String> removeKeys = new ArrayList<String>();
    synchronized (CALLBACKS) {
      for (String key: CALLBACKS.keySet()) {
        long t = Long.valueOf(key);
        if ((tNow - t) > (2 * 60000)) {
          removeKeys.add(key);
        }
      }
      for (String key: removeKeys) {
        CALLBACKS.remove(key);
        System.err.println("removed key, size="+CALLBACKS.size());
      }
    }
  }

	// Escapes special xml characters within a string.
	private String escapeXml(String s) {
	  if ((s == null) || (s.length() == 0)) {
	    return "";
	  } else {
	    char c;
	    StringBuffer sb = new StringBuffer(s.length()+20);
	    for (int i=0; i<s.length(); i++) {
	      c = s.charAt(i);
	      if      (c == '&')  sb.append("&amp;");
	      else if (c == '<')  sb.append("&lt;");
	      else if (c == '>')  sb.append("&gt;");
	      else if (c == '\'') sb.append("&apos;");
	      else if (c == '"')  sb.append("&quot;");
	      else                sb.append(c);
	    }
	    return sb.toString();
	  }
	}
	
	// transform an xml string
  private String transform(String xml) throws TransformerException {
    StringReader reader = new StringReader(xml);
    StringWriter writer = new StringWriter();
    Transformer transformer = TransformerFactory.newInstance().newTransformer() ;
    transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT,"yes");
    transformer.transform(new StreamSource(reader),new StreamResult(writer));
    xml = chkStr(writer.toString());
    return xml;
  }
	
%>
<html>
<head>
<title>HTTP Client</title>
</head>
<script type="text/javascript">
</script>
<body>

<form method="post">
	Url:&nbsp;<input type="text" name="url" size="110" value="<%=escapeXml(url)%>"/>
	<br/><br/>
	Username:&nbsp;<input type="text" name="usr" size="15" value="<%=escapeXml(usr)%>"/>
	Password:&nbsp;<input type="password" name="pwd" size="15" value="<%=escapeXml(pwd)%>"/>
	&nbsp;&nbsp;
  <input type="submit" name="method" value="GET"/>
  <input type="submit" name="method" value="DELETE"/>	
  <br/><br/>Request:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  Content-type:&nbsp;<input type="text" name="requestContentType" size="32" value="<%=escapeXml(requestContentType)%>"/> 
  &nbsp;&nbsp;
  <input type="submit" name="method" value="POST"/>
  <input type="submit" name="method" value="PUT"/>
  <br/>
  <textarea name="requestContent" rows="10" cols="90"><%=escapeXml(requestContent)%></textarea>
  <% if (responseStatus.length() > 0) { %>
  <br/><br/>Response:&nbsp;<%=escapeXml(responseStatus)%><br/>
  <% } %>
  <% if (responseString.length() > 0) { %>
	<textarea name="responseString" rows="10" cols="90"><%=escapeXml(responseString)%></textarea>
	<% } %>
	<% if (callbackMarkup.length() > 0) out.println(callbackMarkup); %>
</form>

</body>
</html>