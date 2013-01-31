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
<%@page session="false"%>
<%@page import="java.net.*,java.io.*,java.util.regex.*"%>
<%!// Servers to allow proxy access to
String[] serverUrls = {
    //"<url>[,<token>]"
    //For ex. (secured server): "http://myserver.mycompany.com/arcgis/rest/services,ayn2C2iPvqjeqWoXwV6rjmr43kyo23mhIPnXz2CEiMA6rVu0xR0St8gKsd0olv8a"
    //For ex. (non-secured server): "http://sampleserver1.arcgisonline.com/arcgis/rest/services"
    "http", // GPT needs this to be open since we do not know the endpoints will consult ahead of time
};

// Some content types (XML variants) are not accepted by browser XML parsing
// This is a map of types to override the content type to "application/xml"
String[] contentTypesToMapToXml = { "application/vnd.google-earth.kml+xml" };

private String chkString(Object obj) {
  if (obj == null) {
    return "";
  }
  return obj.toString().trim();

}%>
<%
  InputStream in = null;
  OutputStream ostream = null;
  HttpURLConnection con = null;
  try {
  String reqUrl = request.getQueryString();
  boolean allowed = false;
  String token = null;
  if (reqUrl != null) {
    for (int i = 0; i < serverUrls.length; i++) {
      String surl = serverUrls[i];
      String[] stokens = surl.split("\\s*,\\s*");
      if (reqUrl.toLowerCase().indexOf(stokens[0].toLowerCase()) >= 0) {
        allowed = true;
        if (stokens.length >= 2 && stokens[1].length() > 0) {
          token = stokens[1];
        }
        break;
      }
    }

  }

  if (!allowed) {
    response.setStatus(403);
    return;
  }
  if (token != null) {
    reqUrl = reqUrl + (reqUrl.indexOf("?") > -1 ? "&" : "?") + "token="
        + token;
  }
  if (reqUrl.startsWith("uri=?") || reqUrl.startsWith("url=?")) {
    reqUrl = reqUrl.substring(5);
  }
  if (reqUrl.startsWith("uri=") || reqUrl.startsWith("url=")) {
    reqUrl = reqUrl.substring(4);
  }
  try {
    reqUrl = URLDecoder.decode(reqUrl, "UTF-8");
  } catch (Exception e) {
    e.printStackTrace();
  }

  // Internal way of getting the authorization parameters
  String auth = request.getParameter("esriflexauthparam");
  if (auth != null && reqUrl.indexOf("esriflexauthparam") > 1) {
    reqUrl = reqUrl.substring(0, reqUrl.indexOf("esriflexauthparam") - 1);
  }

  // decode and re-encode args
  URL url;
  /* 
  TM: Removed.  Transforms parameters of parameters
  
  Pattern p = Pattern.compile("(.+\\?)(.+)");
  Matcher m = p.matcher(reqUrl);
  if (m.matches()) {
    String args = URLDecoder.decode(m.group(2), "US-ASCII");
    String encodedArgs = URLEncoder.encode(args, "US-ASCII");
    encodedArgs = Pattern.compile("%3d", Pattern.CASE_INSENSITIVE).matcher(
        encodedArgs).replaceAll("=");
    encodedArgs = Pattern.compile("%26").matcher(encodedArgs).replaceAll(
        "&");
    url = new URL(m.group(1) + encodedArgs);
  } else {
    url = new URL(reqUrl);
  } */
  url = new URL(reqUrl);
  con = (HttpURLConnection) url.openConnection();
  
  // transfer headers
  java.util.Enumeration e = request.getHeaderNames();
  while (e.hasMoreElements()) {
    Object obj = e.nextElement();
    if (obj == null) {
      continue;
    }
    
    if (obj.toString().toLowerCase().trim().equals("host")) {
      // eliminating host from being sent since we are not the host
      continue;
    }
    if (obj.toString().toLowerCase().trim().startsWith("if-")) {
      // eliminate caching
      continue;
    }
    String value = request.getHeader(obj.toString());
    con.addRequestProperty(obj.toString(), value);

  }
  // Since we are proxy, lets inform the server
  con.addRequestProperty("X-FORWARDED-FOR", request.getRemoteAddr());

  if (auth != null) {
    con.addRequestProperty("Authorization", URLDecoder
        .decode(auth, "UTF-8"));
  }

  con.setDoOutput(true);
  con.setRequestMethod(request.getMethod());
  int clength = request.getContentLength();
  if (clength > 0) {
    con.setDoInput(true);
    InputStream istream = request.getInputStream();
    OutputStream os = con.getOutputStream();
    final int length = 5000;
    byte[] bytes = new byte[length];
    int bytesRead = 0;
    while ((bytesRead = istream.read(bytes, 0, length)) > 0) {
      os.write(bytes, 0, bytesRead);
    }
  }
  out.clear();
  out = pageContext.pushBody();

  java.util.Map map = con.getHeaderFields();
  java.util.Set set = map.entrySet();

  java.util.Iterator iterator = set.iterator();
  while (iterator.hasNext()) {
    java.util.Map.Entry entry = (java.util.Map.Entry) iterator.next();
    String key = (entry.getKey() == null) ? "" : chkString(entry.getKey()
        .toString());

    if ("".equals(key) || key == null) {
      continue;
    }
    if (!(entry.getValue() instanceof java.util.List)) {
      continue;
    }
    java.util.List lValues = (java.util.List) entry.getValue();
    for (int i = 0; i < lValues.size(); i++) {
      String sValue = chkString((String) lValues.get(i));
      if (key.toLowerCase().equals("transfer-encoding")
          && sValue.toLowerCase().equals("chunked")) {
        // Data is no longer chunked.  It's buffered by httpurlconnection
        continue;
      }
      response.addHeader(key, sValue);
    }

  }
  int responseCode = con.getResponseCode();
  if(responseCode == 401) {
    response.setStatus(401);
  }
  String responseMessage = con.getResponseMessage();
  //response.setStatus(responseCode);  
  responseMessage = responseMessage.toLowerCase();

  ostream = response.getOutputStream();

  // Return content type
  String contentType = con.getContentType(); 

  response.setContentType(contentType);
  // Override content types with known issues
  if (contentType != null) {
    for (int i = 0; i < contentTypesToMapToXml.length; i++) {
      if (contentType.indexOf(contentTypesToMapToXml[i]) >= 0) {
        response.setContentType("application/xml");
        break;

      }
    }
  }
  if (responseCode == 401) {
    return;
  }
  in = con.getInputStream();
  final int length = 5000;
  byte[] bytes = new byte[length];
  int bytesRead = 0;
  while ((bytesRead = in.read(bytes, 0, length)) > 0) {

    ostream.write(bytes, 0, bytesRead);

  }
} catch (Exception e) {
  response.setStatus(500);
  e.printStackTrace();
  
} finally {
  try{in.close();}catch(Throwable e){}
  try{ostream.close();}catch(Throwable e){}
  try{con.disconnect();}catch(Throwable e){}
}
%>
