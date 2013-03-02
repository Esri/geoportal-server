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
<% // searchProxy.jsp - Serves as a JSON proxy for rest based catolog search requests %>
<%@page language="java" contentType="application/json; charset=UTF-8"%>
<%@page session="false"%>
<%@page import="com.esri.gpt.framework.http.HttpClientRequest"%>
<%@page import="com.esri.gpt.framework.util.*" %>
<%@page import="java.io.*" %>
<%@page import="java.util.logging.Level"%>

<% 
  String responseData = "";
  String url = chkStr(request.getParameter("url"));
  String callback = chkStr(request.getParameter("callback"));
  LogUtil.getLogger().finer("searchProxy.jsp, query= "+url);
  if (url.indexOf("rest/find/document") != -1) {
    try {
      responseData = chkStr(execute(request,response,url)); 
      LogUtil.getLogger().finer("searchProxy.jsp response for: "+url+"\n"+responseData);
    } catch (Throwable t) {
      responseData = "Error: "+t.toString();
      LogUtil.getLogger().log(Level.SEVERE,"searchProxy.jsp exception for: "+url,t);
    }
  }
%> 

<%!

	/**
	 * Check a string value.
	 * @param s the string to check
	 * @return the checked string (trimmed, zero length if the supplied String was null)
	 */
	private String chkStr(String s) {
	  if (s == null) return "";
	  else return s.trim();
	}

	/**
	 * Escapes a string for JSON.
	 * @param s the string to escape
	 * @return the escaped string
	 */
	private String escapeForJSON(String s) {
	  if (s == null) return null;
	  
	  StringBuffer sb = new StringBuffer();
	  for(int i=0;i<s.length();i++){
	    char ch = s.charAt(i);
	    switch(ch){
	    case '"':
	      sb.append("\\\"");
	      break;
	    case '\\':
	      sb.append("\\\\");
	      break;
	    case '\b':
	      sb.append("\\b");
	      break;
	    case '\f':
	      sb.append("\\f");
	      break;
	    case '\n':
	      sb.append("\\n");
	      break;
	    case '\r':
	      sb.append("\\r");
	      break;
	    case '\t':
	      sb.append("\\t");
	      break;
	    case '/':
	      sb.append("\\/");
	      break;
	    default:
	      if ((ch >= '\u0000') && (ch <= '\u001F')) {
	        String s2 =Integer.toHexString(ch);
	        sb.append("\\u");
	        for(int k=0;k<4-s2.length();k++){
	          sb.append('0');
	        }
	        sb.append(s2.toUpperCase());
	      } else{
	        sb.append(ch);
	      }
	    }
	  }
	  return sb.toString();
	}

  /**
   * Execute the proxy request.
   * @param request the HTTP request
   * @param response the HTTP response
   * @param restUrl the rest based query url
   * @throws Exception if an exception occurs
   */
  private String execute(HttpServletRequest request, 
                         HttpServletResponse response,
                         String restUrl) 
    throws Exception {
         
    String responseData = "";
    HttpClientRequest client = new HttpClientRequest();
    client.setUrl(restUrl);
    String hdrVal = Val.chkStr(request.getHeader("accept-language"));
    if (hdrVal.length() > 0) client.setRequestHeader("accept-language",hdrVal);
    
    try {
      responseData = client.readResponseAsCharacters();
    } catch (IOException e) {
      int responseCode = client.getResponseInfo().getResponseCode();
      String responseMessage = chkStr(client.getResponseInfo().getResponseMessage());
      if ((responseCode == 500) && (responseMessage.length() > 0)) {
        String msg = e.getMessage()+" cause:"+responseMessage;
        throw new IOException(msg);
      } else {
        throw e;
      }
    }
    return responseData;
  }

%>

<%=callback%>({"innerHTML": "<%=escapeForJSON(responseData)%>"});


