/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.server.csw.client;

import com.esri.gpt.framework.http.ContentProvider;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.HttpClientRequest.MethodName;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * CswClient class is used to submit CSW search request.
 * 
 * CswClient is a wrapper class of .NET HttpWebRequest and HttpWebResponse.
 * It basically submits a HTTP request then return a text response.
 */
public class CswClient {

// class variables =============================================================
/** Class Logger **/
private static Logger LOG = Logger
                              .getLogger(CswClient.class.getCanonicalName());

/** The REQUES Timeout parameter. */
public static int DEFAULT_REQUEST_TIMEOUT = -1;


// instance variables ==========================================================
/**Connection timeout for query **/
private int connectTimeout = DEFAULT_REQUEST_TIMEOUT;

private int readTimeOut = DEFAULT_REQUEST_TIMEOUT;

// constructor =================================================================
private HttpClient batchHttpClient;
/**
 * use static variable to be thread safe 
 * (FROM C# CODE: MAY BE REMOVED LATER)
 * 
 */
//	private static CookieContainer _cookieContainer;
//	private CredentialCache _credentialCache;
/**
 * Constructor
 */
public CswClient() {
}

// properties ==================================================================

public void setBatchHttpClient(HttpClient batchHttpClient) {
  this.batchHttpClient = batchHttpClient;
}

public HttpClient getBatchHttpClient() {
  return batchHttpClient;
}

/**
 * Gets the timeout.
 * 
 * @return the timeout
 */
public int getConnectTimeout() {
  return this.connectTimeout;
}


/**
 * Sets the timeout.
 * 
 * @param timeout the new timeout
 */
public void setConnectTimeout(int timeout) {
  this.connectTimeout = timeout;
}

/**
 * Gets the timeout.
 * 
 * @return the timeout
 */
public int getReadTimeout() {
  return this.readTimeOut;
}


/**
 * Sets the timeout.
 * 
 * @param timeout the new timeout
 */
public void setReadTimeout(int timeout) {
  this.readTimeOut = timeout;
}
// methods =====================================================================
/**
 * Encode PostBody
 * 
 * Encode special characters (such as %, space, <, >, \, and &) to percent
 * values.
 * @return Encoded text.
 * 
 * @param postbody    Text to be encoded
 */
public String encodePostbody(String postbody) {

  String strOutput = postbody;
  strOutput = replaceSubString(strOutput, "&amp;amp;", "&");
  strOutput = replaceSubString(strOutput, "&amp;", "&");
  strOutput = replaceSubString(strOutput, "&lt;", "<");
  strOutput = replaceSubString(strOutput, "&apos;", "'");
  strOutput = replaceSubString(strOutput, "&quot;", "\"");
  strOutput = replaceSubString(strOutput, "&#45;", "-");
  return strOutput;
}

/**
 * Replace sub string.
 * 
 * @param source the source
 * @param pattern the pattern
 * @param replace the replace
 * 
 * @return the replaced string
 */
public static String replaceSubString(String source, String pattern,
    String replace) {
  if (source != null) {
    int len = pattern.length();
    StringBuffer sb = new StringBuffer();
    int found = -1;
    int start = 0;

    while ((found = source.indexOf(pattern, start)) != -1) {
      sb.append(source.substring(start, found));
      sb.append(replace);
      start = found + len;
    }
    sb.append(source.substring(start));
    return sb.toString();
  } else
    return null;
}

/**
 * Submit HTTP Request
 * 
 * @return Response in plain text
 * 
 * @param method    HTTP Method. for example "POST", "GET"
 * @param url    URL to send HTTP Request to
 * @param postdata    Data to be posted
 * @throws IOException 
 */
public InputStream submitHttpRequest(String method, String url, String postdata)
    throws IOException {
  return submitHttpRequest(method, url, postdata, "", "");
}

/**
 * Submit HTTP Request (Both GET and POST). Return InputStream object from the response
 * 
 * Submit an HTTP request.
 * @return Response in plain text.
 * 
 * @param method    HTTP Method. for example "POST", "GET"
 * @param urlString    URL to send HTTP Request to
 * @param postdata    Data to be posted
 * @param usr    Username
 * @param pwd    Password
 * @throws IOException in IOException
 * @throws  java.net.SocketTimeoutException if connect or read timeout
 */
public InputStream submitHttpRequest(String method, String urlString,
    String postdata, String usr, String pwd) throws IOException {

	
  if (LOG.isLoggable(Level.FINER)) {
    LOG.finer("Data being sent.  URL = " + urlString + "\n Data = " + postdata);
  }

  urlString = Utils.chkStr(urlString);
  urlString = urlString.replaceAll("\\n", "");
  urlString = urlString.replaceAll("\\t", "");
  urlString = urlString.replaceAll("\\r", "");
  urlString = urlString.replaceAll(" ", "");

  HttpClientRequest client = HttpClientRequest.newRequest();
  client.setBatchHttpClient(getBatchHttpClient());
  client.setUrl(urlString);
  client.setRetries(1);
  
  usr = Val.chkStr(usr);
  pwd = Val.chkStr(pwd);
  if (usr.length() > 0 || pwd.length() > 0) {
    CredentialProvider provider = new CredentialProvider(usr, pwd);
    client.getCredentialProvider();
    client.setCredentialProvider(provider);
  }
  if (this.getReadTimeout() > 0) {
    client.setResponseTimeOutMs(this.getReadTimeout());
  }
  if (this.getConnectTimeout() > 0) {
    client.setConnectionTimeMs(this.getConnectTimeout());
  }

  // Send a request
  if (Val.chkStr(method).equalsIgnoreCase("post")) {
    ContentProvider contentProvider = new StringProvider(postdata, "text/xml");
    client.setContentProvider(contentProvider);
    
  } else {
    client.setMethodName(MethodName.GET);
  }

  String response = client.readResponseAsCharacters();
  LOG.finer(" CSW Response : " + response);
  return new ByteArrayInputStream(response.getBytes("UTF-8"));
  
}

/**
 * Returns content encoding value from content type
 * @param contentType content type
 * @return last content encoding
 */
private String getLastContentEncoding(String contentType){
  if(contentType != null){
	contentType = contentType.trim();
	String[] contentTypes = contentType.split(";");
	for(String c:contentTypes){
	  if (c != null) {
		int index = c.trim().toLowerCase().indexOf("charset=");
	    if(index == 0)
	    	return c.trim().substring(index+8).trim();
	  }
	}
  }
  return "UTF-8";
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
    if ((charset == null) || (charset.trim().length() == 0)) charset = "UTF-8";
    char cbuf[] = new char[4096];
    int n = 0;
    int nLen = cbuf.length;
    ir = new InputStreamReader(stream,charset);
    br = new BufferedReader(ir);
    while ((n = br.read(cbuf,0,nLen)) >= 0) sb.append(cbuf,0,n);
  } finally {
    try {if (br != null) br.close();} catch (Exception ef) {}
    try {if (ir != null) ir.close();} catch (Exception ef) {}
  }
  return sb.toString();
}

/**
 * submit HTTP Request (Both GET and POST). Parse the response into an xml document element.
 * @param method
 * @param urlString
 * @param postdata
 * @param usr
 * @param pwd
 * @return response
 * @throws IOException
 * @throws ParserConfigurationException
 * @throws SAXException
 */
public Element submitHttpRequestAndGetDom(String method, String urlString,
    String postdata, String usr, String pwd) throws IOException,
    ParserConfigurationException, SAXException {

  InputStream inStream = null;
  Element response;
  try {
    inStream = submitHttpRequest(method, urlString, postdata, "", "");
    // Get a response
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    Document doc = docBuilder.parse(inStream);

    response = doc.getDocumentElement();
    response.normalize();
  } finally {
    Utils.close(inStream);
  }

  return response;
}

}