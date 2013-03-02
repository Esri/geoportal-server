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
<% // kmzBridge.jsp - Serves as a proxy for the KMZ files %>
<%@page session="false"%>
<%@page import="java.net.*,java.io.*,java.util.zip.*" %>
<%@page import="com.esri.gpt.framework.util.KmlUtil" %>

<% execute(request,response); %>

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
		    postData = readCharacters(requestStream,request.getCharacterEncoding());
		  } finally {
		    try {if (requestStream != null) requestStream.close();} catch (Exception ef) {}
		  }

		  // open a connection to the targeted server
		  URL url = new URL(request.getQueryString());
		  HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
		  httpCon.setDoInput(true);
		  httpCon.setRequestMethod(request.getMethod());
		  //httpCon.setConnectTimeout(10000);

      // set request properties
      httpCon.setRequestProperty("Content-Type",request.getContentType());
      httpCon.setRequestProperty("Content-Length",""+postData.length());

		  // post data to the targeted server
		  if (postData.length() > 0) {
		    httpCon.setDoOutput(true);
		    OutputStream postStream = null;
		    try {
		      postStream = httpCon.getOutputStream();
		      postStream.write(postData.getBytes("UTF-8"));
		      postStream.flush();
		    } finally {
		      try {if (postStream != null) postStream.close();} catch (Exception ef) {}
		    }
		  }

		  // read the response from the targeted server
		  String responseData = "";
		  InputStream responseStream = null;
      String ct = httpCon.getContentType();
      boolean kmz = (ct != null) && (ct.toLowerCase().indexOf("application/vnd.google-earth.kmz") != -1);
      InputStream wrappedStream = null;
		  try {
		    responseStream = httpCon.getInputStream();
        if (kmz) {
          wrappedStream = KmlUtil.extractKmlStream(responseStream);
        } else {
          wrappedStream = new BufferedInputStream(responseStream);
        }
        wrappedStream = KmlUtil.convertToFeaturesStream(wrappedStream, url.toExternalForm());
		    responseData = readCharacters(wrappedStream,"UTF-8");
		  } finally {
		    try {if (responseStream != null) responseStream.close();} catch (Exception ef) {}
		    try {if (wrappedStream != null) wrappedStream.close();} catch (Exception ef) {}
		  }

		  // write the response to the proxy client
		  PrintWriter writer = null;
		  try {
		    if (responseData.length() > 0) {
		      response.setCharacterEncoding("UTF-8");
		      response.setContentType(httpCon.getContentType());
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
		      System.err.println("proxy.jsp: Error closing PrintWriter: "+ef.toString());
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
      if (stream!=null) {
        try {
          if ((charset == null) || (charset.trim().length() == 0)) charset = "UTF-8";
          char cbuf[] = new char[2048];
          int n = 0;
          int nLen = cbuf.length;
          ir = new InputStreamReader(stream,charset);
          br = new BufferedReader(ir);
          while ((n = br.read(cbuf,0,nLen)) > 0) sb.append(cbuf,0,n);
        } finally {
          try {if (br != null) br.close();} catch (Exception ef) {}
          try {if (ir != null) ir.close();} catch (Exception ef) {}
        }
      }
	  return sb.toString();
	}
%>