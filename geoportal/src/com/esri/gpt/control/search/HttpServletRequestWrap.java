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
package com.esri.gpt.control.search;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.esri.gpt.framework.util.Val;


// TODO: Auto-generated Javadoc
/**
 * The Class HttpServletRequestWrap.
 */
public class HttpServletRequestWrap extends HttpServletRequestWrapper {

// instance variables ==========================================================
/** The rest request url. */
String restRequestUrl;

/** The request params. */
Map<String, String[]> requestParams;

// constructors ================================================================
/**
 * Instantiates a new http servlet request wrap.
 * 
 * @param request the request
 */
public HttpServletRequestWrap(HttpServletRequest request) {
  super(request);
}



// properties ==================================================================

/**
 * Gets the rest request url.
 * 
 * @return the rest request url (trimmed, never null)
 */
public String getRestRequestUrl() {
  return Val.chkStr(restRequestUrl);
}

/**
 * Sets the rest request url.
 * 
 * @param restRequestUrl the new rest request url
 */
public void setRestRequestUrl(String restRequestUrl) {
  this.restRequestUrl = restRequestUrl;
  this.requestParams = null;
}

/**
 * Gets the request uri.
 * 
 * @return the request uri
 * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
 */
@Override
public String getRequestURI() {
  return this.getRequestURL().toString();
}

/**
 * Gets the request url.
 * 
 * @return the request url
 * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
 */
@Override
public StringBuffer getRequestURL() {
  return new StringBuffer(this.restRequestUrl);
}



/**
 * Gets the parameter.
 * 
 * @param name the name
 * @return the parameter
 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
 */
@Override
public String getParameter(String name) {
  Map<String, String[]> map = this.getParameterMap();
  String value[] = map.get(name);
  if(value == null || value.length < 1) {
    return null;
  }
  return value[0];
}

/**
 * Gets the parameter map.
 * 
 * @return the parameter map
 * @see javax.servlet.ServletRequestWrapper#getParameterMap()
 */
@Override
public Map<String, String[]> getParameterMap() {
  if(requestParams == null) {
    requestParams = this.readParameterMap();
  }
  return requestParams;
}

/**
 * Gets the parameter names.
 * 
 * @return the parameter names
 * @see javax.servlet.ServletRequestWrapper#getParameterNames()
 */
@Override
public Enumeration<String> getParameterNames() {
  Vector<String> vector = new Vector<String>();
  Iterator<String> iter = this.getParameterMap().keySet().iterator();
  while(iter.hasNext()) {
    vector.add(iter.next());
  }
  return vector.elements();
}

/**
 * Gets the parameter values.
 * 
 * @param name the name
 * @return the parameter values
 * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
 */
@Override
public String[] getParameterValues(String name) {
  Map<String, String[]> paramMap = this.getParameterMap();
  Collection<String[]> collection = paramMap.values();
  ArrayList<String> aValues = new ArrayList<String>();
  Iterator<String[]> iter = collection.iterator();
  while(iter.hasNext()) {
    String vals[] = iter.next();
    for(int i = 0; i < vals.length; i++) {
      aValues.add(vals[i]);
    }
  }
  return (String[]) aValues.toArray();
}

/**
 * Gets the query string.
 * 
 * @return the query string null if no querystring, else trimmed querystring
 * @see javax.servlet.http.HttpServletRequestWrapper#getQueryString()
 */
public String getQueryString() {
  String reqUrl = getRestRequestUrl();
  int qIndex = reqUrl.indexOf("?");
  if(qIndex < 0 || reqUrl.length()-1 <= qIndex) {
    return null;
  }
  return this.getRestRequestUrl().substring(qIndex + 1).trim();
}

// methods =====================================================================
/**
 * Read parameter map.
 * 
 * @return the map
 */
private Map<String, String[]> readParameterMap() {
  Map<String, String[]> paramMap = new TreeMap<String, String[]>(
      String.CASE_INSENSITIVE_ORDER);
  String queryString = getQueryString();
  String params[] = queryString.split("&");
  for (int i = 0; i < params.length; i++) {
    String kvp = params[i];
    String kv[] = kvp.split("=");
    if (kv.length < 2) {
      continue;
    }
    String v[] = paramMap.get(kv[0]);
    try {
      if (v == null || v.length == 0) {

        paramMap.put(kv[0], new String[] { URLDecoder.decode(kv[1], "UTF-8") });

      } else {
        String newV[] = new String[v.length + 1];
        System.arraycopy(v, 0, newV, 0, v.length);
        newV[v.length] = URLDecoder.decode(kv[1], "UTF-8");
        paramMap.put(kv[0], newV);

      }
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  return paramMap;
}




}
