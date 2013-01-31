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
package com.esri.gpt.framework.context;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.security.codec.Base64;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Super-class for HttpServlet end-points.
 */
public abstract class BaseServlet extends HttpServlet {
  
// class variables =============================================================
private static Logger LOGGER = Logger.getLogger(BaseServlet.class.getName());
  
// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================

/**
 * Authenticate credentials found within and HTTP request.
 * @param context the active request context
 * @param credentials the credentials to authenticate
 * @throws CredentialsDeniedException if credentials are denied
 * @throws IdentityException if a system error occurs preventing authentication
 * @throws SQLException if a database communication exception occurs
 */
protected void authenticate(RequestContext context, Credentials credentials) 
  throws CredentialsDeniedException, IdentityException, SQLException {
  getLogger().finer("Authenticating user...");
  IdentityAdapter idAdapter = context.newIdentityAdapter();
  User user = context.getUser();
  user.reset();
  user.setCredentials(credentials);
  try {
    idAdapter.authenticate(user);
  } catch(CredentialsDeniedException e) {
    if (credentials instanceof UsernamePasswordCredentials) {
      String sUser = ((UsernamePasswordCredentials)credentials).getUsername();
      getLogger().finer("Authentication failed for: "+sUser);
    } else {
      getLogger().finer("Authentication failed.");
    }
    throw e;
  }
}

/**
 * Handles a GET request.
 * <p/>
 * The default behavior is the execute the doPost method.
 * @param request the servlet request
 * @param response the servlet response
 */
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
  doPost(request,response);
}

/**
 * Handles a POST request.
 * <p/>
 * The default behavior:
 * <li>set the character encoding (UTF-8) if it is null</li>
 * <li>instantiate a RequestContext</li>
 * <li>authenticate credentials if found within the header</li>
 * <li>invoke the abstract "execute" method</li>
 * <li>release the request context</li>
 * @param request the servlet request
 * @param response the servlet response
 */
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
  RequestContext context = null;
  try {
    getLogger().finer("Query string="+request.getQueryString());
    logHeader(request);
    String sEncoding = request.getCharacterEncoding();
    if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
      request.setCharacterEncoding("UTF-8");
    }
    context = RequestContext.extract(request);
    
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    String autoAuthenticate = Val.chkStr(params.getValue("BaseServlet.autoAuthenticate"));
    if (!autoAuthenticate.equalsIgnoreCase("false")) {
      Credentials credentials = getCredentials(request);
      if (credentials != null) {
        authenticate(context,credentials);
      }
    }
    
    execute(request,response,context);
  } catch (CredentialsDeniedException e) {
    String sRealm = this.getRealm(context);
    response.setHeader("WWW-Authenticate","Basic realm=\""+sRealm+"\"");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  } catch (NotAuthorizedException e) {
    String sRealm = this.getRealm(context);
    response.setHeader("WWW-Authenticate","Basic realm=\""+sRealm+"\"");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);    
  } catch (Throwable t) {
    String sErr = "Exception occured while processing servlet request.";
    getLogger().log(Level.SEVERE,sErr,t);
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  } finally {
    if (context != null) context.onExecutionPhaseCompleted();
  }
}

/**
 * Logs the header for an incoming request.
 * @param request the HTTP request
 */
private void logHeader(HttpServletRequest request) {
  if (LOGGER.isLoggable(Level.FINEST)) {
    StringBuffer sb = new StringBuffer();
    sb.append("HTTP Header ======================================");
    java.util.Enumeration enNames = request.getHeaderNames();
    while (enNames.hasMoreElements()) {
      Object o = enNames.nextElement();
      if ((o != null) && (o instanceof String)) {
        String sName = (String)o;
        String sValue = request.getHeader(sName);
        sb.append("\n  "+sName+"="+sValue);
      }
    }
    LOGGER.finest(sb.toString());
  }
}

/**
 * Processes the HTTP request.
 * @param request the HTTP request
 * @param response HTTP response
 * @param context request context
 * @throws Exception if an exception occurs
 */
protected abstract void execute(HttpServletRequest request, 
                                HttpServletResponse response,
                                RequestContext context) 
  throws Exception;

/**
 * Looks for username:password credentials within the Authorization 
 * header parameter of the HTTP request.
 * @param request the servlet request
 * @return the credentials (null if none were located)
 * @throws IOException if an IO exception occurs
 * @throws CredentialsDeniedException if empty or non-basic credentials were located
 */
protected UsernamePasswordCredentials getCredentials(HttpServletRequest request) 
  throws IOException, CredentialsDeniedException {
  UsernamePasswordCredentials creds = null;
  String sAuthorization = request.getHeader("Authorization");
  getLogger().finer("Authorization header="+sAuthorization);
  if (sAuthorization != null) {
    creds = new UsernamePasswordCredentials();
    if (sAuthorization.startsWith("Basic ")) {
      
      // look for a Basic encoded username:password
      // (ignore Digest we can't handle it at the moment,
      //  requires password retrieval from LDAP)
      sAuthorization = sAuthorization.substring(6);
      if (sAuthorization.length() > 0) {
        String sDecoded = Base64.decode(sAuthorization,"UTF-8");
        int nIdx = sDecoded.indexOf(':');
        if (nIdx > 0) {
          creds.setUsername(sDecoded.substring(0,nIdx));
          creds.setPassword(sDecoded.substring(nIdx+1));
        }
      }
    }
    getLogger().finer("Authorization username="+creds.getUsername());
    if ((creds.getUsername().length() == 0) ||
        (creds.getPassword().length() == 0)) {
      throw new CredentialsDeniedException("Invalid credentials.");
    }
  }
  return creds;
}

/**
 * Gets the logger.
 * @return the logger
 */
protected Logger getLogger() {
  return LOGGER;
}

/**
 * Gets a request parameter value.
 * @param request the HTTP request
 * @param name the parameter name
 * @return ther parameter value
 */
protected String getParameterValue(HttpServletRequest request, String name) {
  Map<String, String[]> parMap = request.getParameterMap();
  for (Map.Entry<String, String[]> e : parMap.entrySet()) {
    if (e.getKey().equalsIgnoreCase(name)) {
      if (e.getValue().length > 0) {
        return Val.chkStr(e.getValue()[0]);
      } else {
        return "";
      }
    }
  }
  return "";
}

/**
 * Gets the identity store realm (used as an identifier during HTTP 401 
 * credential challenge/response).
 * @param context the active request context
 * @return the identity store realm
 */
protected String getRealm(RequestContext context) {
  String realm = Val.chkStr(context.getIdentityConfiguration().getRealm());
  if (realm.length() == 0) {
    realm = "Geoportal";
  }
  return realm;
}

/**
 * Fully reads the characters from the request input stream.
 * @param request the HTTP servlet request
 * @return the characters read
 * @throws IOException if an exception occurs
 */
protected String readInputCharacters(HttpServletRequest request)
  throws IOException {
  StringBuffer sb = new StringBuffer();
  InputStream is = null;
  InputStreamReader ir = null;
  BufferedReader br = null;
  try {
    //if (request.getContentLength() > 0) {
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      String sEncoding = request.getCharacterEncoding();
      if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
        sEncoding = "UTF-8";
      }
      is = request.getInputStream();
      ir = new InputStreamReader(is,sEncoding);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf,0,nLen)) > 0) {
        sb.append(cbuf,0,n);
      }
    //}
  } finally {
    try {if (br != null) br.close();} catch (Exception ef) {}
    try {if (ir != null) ir.close();} catch (Exception ef) {}
    try {if (is != null) is.close();} catch (Exception ef) {}
  }
  return sb.toString();
}

/**
 * Writes characters to the response stream.
 * @param response the servlet response
 * @param content the content to write
 * @param charset the response character encoding 
 * @param contentType the response content type
 * @throws IOException if an IO exception occurs
 */
protected void writeCharacterResponse(HttpServletResponse response, 
                                      String content,
                                      String charset,
                                      String contentType) 
  throws IOException {
  PrintWriter writer = null;
  try {
    if (content.length() > 0) {
      response.setCharacterEncoding(charset);
      response.setContentType(contentType);
      writer = response.getWriter();
      writer.write(content);
      writer.flush();
    }
  } finally {
    try {
      if (writer != null) {
        writer.flush();
        writer.close();
      }
    } catch (Exception ef) {
      getLogger().log(Level.SEVERE,"Error closing PrintWriter.",ef);
    }
  }
}

/**
 * Convience method for writeCharacterResponse.
 * <br/>charset="UTF-8"
 * <br/>contentType="text/html; charset=UTF-8"
 * @param response the servlet response
 * @param content the content to write
 * @throws IOException if an IO exception occurs
 */
protected void writeHtmlResponse(HttpServletResponse response, 
                                 String content) 
  throws IOException {
  writeCharacterResponse(response,content,"UTF-8","text/html; charset=UTF-8");
}

/**
 * Convience method for writeCharacterResponse.
 * <br/>charset="UTF-8"
 * <br/>contentType="text/xml; charset=UTF-8"
 * @param response the servlet response
 * @param content the content to write
 * @throws IOException if an IO exception occurs
 */
protected void writeXmlResponse(HttpServletResponse response, 
                                String content) 
  throws IOException {
  writeCharacterResponse(response,content,"UTF-8","text/xml; charset=UTF-8");
}
  
}
