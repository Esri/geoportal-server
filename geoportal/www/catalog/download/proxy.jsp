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
<%@page import="com.esri.gpt.framework.http.StringHandler"%>
<%@page import="com.esri.gpt.framework.http.StringProvider"%>
<%@page import="com.esri.gpt.framework.http.HttpClientRequest"%>
<%@page import="com.esri.gpt.framework.context.CredentialsMap"%>
<%@page import="com.esri.gpt.framework.http.CredentialProvider"%>
<% // proxy.jsp - Serves as a proxy for the ArcGIS Server Javascript API %>
<%@page session="false"%>
<%@page import="java.net.*,java.io.*" %>

<% execute(request, response);%>

<%!
  /**
   * Execute the proxy request.
   * @param request the HTTP request
   * @param response the HTTP response
   */
  private void execute(HttpServletRequest request, HttpServletResponse response) {
    try {
      // read the data to be posted from the incoming request
      String postData = "";
      InputStream requestStream = null;
      try {
        requestStream = request.getInputStream();
        postData = readCharacters(requestStream, request.getCharacterEncoding());
      } finally {
        try {
          if (requestStream != null) {
            requestStream.close();
          }
        } catch (Exception ef) {
        }
      }

      HttpClientRequest cr = new HttpClientRequest();
      cr.setUrl(request.getQueryString());
      if (postData.length()>0) {
        cr.setContentProvider(new StringProvider(postData, request.getContentType()));
      }
      StringHandler sh = new StringHandler();
      cr.setContentHandler(sh);
      CredentialsMap cm = CredentialsMap.extract(request);
      CredentialProvider cp = cm.get(request.getQueryString());
      cr.setCredentialProvider(cp);

      cr.execute();

      // write the response to the proxy client
      String responseData = sh.getContent();
      PrintWriter writer = null;
      try {
        if (responseData.length() > 0) {
          response.setCharacterEncoding(cr.getResponseInfo().getContentEncoding());
          response.setContentType(cr.getResponseInfo().getContentType());
          writer = response.getWriter();
          writer.write(responseData);
          writer.flush();
        }
      } finally {
        try {
          if (writer != null) {
            writer.flush();
            writer.close();
          }
        } catch (Exception ef) {
          System.err.println("proxy.jsp: Error closing PrintWriter: " + ef.toString());
        }
      }

    } catch (Exception e) {
      response.setStatus(500);
    }
  }

  /**
   * Fully reads the characters from an input stream.
   * @param stream the input stream
   * @param charset the encoding of the input stream
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  private String readCharacters(InputStream stream, String charset)
      throws IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader br = null;
    InputStreamReader ir = null;
    try {
      if ((charset == null) || (charset.trim().length() == 0)) {
        charset = "UTF-8";
      }
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(stream, charset);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf, 0, nLen)) > 0) {
        sb.append(cbuf, 0, n);
      }
    } finally {
      try {
        if (br != null) {
          br.close();
        }
      } catch (Exception ef) {
      }
      try {
        if (ir != null) {
          ir.close();
        }
      } catch (Exception ef) {
      }
    }
    return sb.toString();
  }

%>