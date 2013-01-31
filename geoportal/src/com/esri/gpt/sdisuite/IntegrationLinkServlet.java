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
package com.esri.gpt.sdisuite;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Callback servlet when certain links associated with a discovered resource are clicked.
 */
public class IntegrationLinkServlet extends HttpServlet {
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(IntegrationLinkServlet.class.getName());
  
  /** User attribute key holding the SAML security token */
  private static String SDI_SECURITY_TOKEN = "sdi.security.token";

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    this.execute(request,response);
  }
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    this.execute(request,response);
  }
  
  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @throws ServletException if an exception occurs
   * @throws IOException if an I/O exception occurs
   */
  private void execute(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    LOGGER.finer("Query string="+request.getQueryString());
    
    // initialize parameters execute the appropriate request
    String lcb = request.getParameter("lcb");
    String act = request.getParameter("act");
    if ((lcb != null) && lcb.equals("true")) {
      this.executeLicenseCallback(request,response);
    } else if (act != null) {
      this.executeClick(request,response);
    } else {
      this.writeError(request,response,"No action was specified.",null);
    }
  }
  
  /**
   * Processes a click on a resource link.
   * @param request the HTTP request
   * @param response HTTP response
   * @throws ServletException if an exception occurs
   * @throws IOException if an I/O exception occurs
   */
  private void executeClick(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    LOGGER.finer("Processing resource link click...");
  
    // initialize parameters
    String act = request.getParameter("act");
    String fwd = request.getParameter("fwd");
    String resourceUrl = null;
    String addToMapHint = null;
    
    // determine the resource URL to be checked
    if (act != null) {
      if (act.equals("open")) {
        resourceUrl = fwd;
      } else if (act.equals("preview")) {
        Map<String,String> params = this.gatherParams(fwd);
        resourceUrl = params.get("url");
      } else if (act.equals("addToMap")) {
        Map<String,String> params = this.gatherParams(fwd);
        resourceUrl = params.get("resource");
        if ((resourceUrl != null) && (resourceUrl.length() > 0)) {
          if (!resourceUrl.toLowerCase().startsWith("http")) {
            int idx = resourceUrl.indexOf(":");
            if (idx != -1) {
              addToMapHint = resourceUrl.substring(0,idx);
              resourceUrl = resourceUrl.substring(idx+1);
            }
          }
        }
      } else {
        resourceUrl = fwd;
      }
    }
    
    // check the resource URL
    if ((resourceUrl != null) && (resourceUrl.length() > 0)) {
      LOGGER.finer("Checking resource URL: "+resourceUrl);
      RequestContext rc = null;
      String samlToken = null;
      try {
        rc = RequestContext.extract(request);
        User user = rc.getUser();
        IntegrationResponse resp = null;
        IntegrationContextFactory icf = new IntegrationContextFactory();
        if (icf.isIntegrationEnabled()) {
          IntegrationContext ic = icf.newIntegrationContext();
          if (ic != null) {
            resp = ic.checkUrl(resourceUrl,user,null,null,null);
          
            if ((resp != null) && resp.isLicensed()) {
              if ((user != null) && (user.getProfile() != null)) {
                if (user.getProfile().containsKey(SDI_SECURITY_TOKEN)) {
                  samlToken = ic.getBase64EncodedToken(user);
                }
              }
            }
            
          }
        }
        
        // handle a licensed URL
        if ((resp != null) && resp.isLicensed()) {
          String wssUrl = resp.getUrl();
          String licenseSelectionUrl = resp.getLicenseSelectionClientUrl();
          if ((licenseSelectionUrl != null) && (licenseSelectionUrl.length() > 0) &&
              (wssUrl != null) && (wssUrl.length() > 0)) {
            
            // save resource URL parameters
            String wssUrlParams = null;
            int idx = wssUrl.indexOf("?");
            if (idx != -1) {
              wssUrlParams = wssUrl.substring(idx+1).trim();
              wssUrl = wssUrl.substring(0,idx);
            }
            
            // make the callback URL
            String callbackUrl = RequestContext.resolveBaseContextPath(request)+"/link";
            callbackUrl += "?lcb="+URLEncoder.encode("true","UTF-8");
            callbackUrl += "&act="+URLEncoder.encode(act,"UTF-8");
            callbackUrl += "&fwd="+URLEncoder.encode(fwd,"UTF-8");
            if ((wssUrlParams != null) && (wssUrlParams.length() > 0)) {
              callbackUrl += "&rqs="+URLEncoder.encode(wssUrlParams,"UTF-8");
            }
            if ((addToMapHint != null) && (addToMapHint.length() > 0)) {
              callbackUrl += "&atmh="+URLEncoder.encode(addToMapHint,"UTF-8");
            }

            // make the full license selection URL (can set &embedded=true)
            licenseSelectionUrl += "?WSS="+URLEncoder.encode(wssUrl,"UTF-8");
            licenseSelectionUrl += "&returnURL="+URLEncoder.encode(callbackUrl,"UTF-8");
            
            // if user is logged in, 
            //   return an HTML response that immediately posts the SAML token to the license selection URL
            // else
            //   forward to the licenseSelectionUrl            
            if ((samlToken != null) && (samlToken.length() > 0)) {
              LOGGER.finer("Sending POST redirect with token to: " + licenseSelectionUrl);
              fwd = null;
              String title = "License redirect SSO page";
              StringBuilder sbHtml = new StringBuilder();
              sbHtml.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
              sbHtml.append("\r\n<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
              sbHtml.append("\r\n<head>");
              sbHtml.append("\r\n<title>").append(Val.escapeXmlForBrowser(title)).append("</title>");
              sbHtml.append("\r\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>");
              sbHtml.append("\r\n<meta http-equiv=\"Expires\" content=\"Mon, 01 Jan 1990 00:00:01 GMT\"/>");
              sbHtml.append("\r\n<meta http-equiv=\"pragma\" content=\"no-cache\"/>");
              sbHtml.append("\r\n<meta http-equiv=\"cache-control\" content=\"no-cache\"/>");
              sbHtml.append("\r\n<meta name=\"robots\" content=\"noindex\"/>");
              sbHtml.append("\r\n</head>");
              sbHtml.append("\r\n<body onload=\"document.forms[0].submit();\">");
              sbHtml.append("\r\n<form method=\"post\" action=\"").append(Val.escapeXmlForBrowser(licenseSelectionUrl)).append("\">");
              sbHtml.append("\r\n<input type=\"hidden\" name=\"ticket\" value=\"").append(Val.escapeXmlForBrowser(samlToken)).append("\"/>");
              sbHtml.append("\r\n</form>");
              sbHtml.append("\r\n</body>");
              sbHtml.append("\r\n</html>");
              this.writeCharacterResponse(response,sbHtml.toString(),"UTF-8","text/html; charset=UTF-8");
            } else {
              fwd = licenseSelectionUrl;
            }

          } else {
            String msg = "IntegrationResponse isLicensed() was true, but getLicenseSelectionClientUrl() was empty.";
            LOGGER.warning(msg);
          }
          
        // handle a secured URL
        } else if ((resp != null) && resp.isSecured()) {
          String securedUrl = resp.getUrl();
          if ((securedUrl != null) && !securedUrl.equals(resourceUrl)) {
            if (act.equals("open")) {
              fwd = securedUrl;
            } else if (act.equals("preview")) {
              fwd = this.replaceParam(fwd,"url",securedUrl);
            } else if (act.equals("addToMap")) {
              if ((addToMapHint != null) && (addToMapHint.length() > 0)) {
                securedUrl = addToMapHint+":"+securedUrl;
              }
              fwd = this.replaceParam(fwd,"resource",securedUrl);
            } else {
              fwd = securedUrl;
            }
          }
        }
        
      } catch (NotAuthorizedException e) {
        String msg = "Error checking resource URL";
        LOGGER.log(Level.SEVERE,msg,e);
        this.writeError(request,response,msg+": "+e.toString(),null);
        return;
      } catch (Exception e) {
        String msg = "Error checking resource URL";
        LOGGER.log(Level.SEVERE,msg,e);
        this.writeError(request,response,msg+": "+e.toString(),null);
        return;
      } finally {
        if (rc != null) rc.onExecutionPhaseCompleted();
      }
    }

    // send the redirect
    if ((fwd != null) && (fwd.length() > 0)) {
      LOGGER.finer("Redirecting to: "+fwd);
      response.sendRedirect(fwd);
    }
  }
  
  /**
   * Processes the response of a license selection.
   * @param request the HTTP request
   * @param response HTTP response
   * @throws ServletException if an exception occurs
   * @throws IOException if an I/O exception occurs
   */
  private void executeLicenseCallback(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    LOGGER.finer("Processing license selection callback...");
    
    // this code is a modification of Oliver's returnAction.jsp
    
    // Geoportal parameters 
    String act = request.getParameter("act");
    if ((act == null) || (act.length() == 0)) act = request.getParameter("amp;act");
    String fwd = request.getParameter("fwd");
    if ((fwd == null) || (fwd.length() == 0)) fwd = request.getParameter("amp;fwd");
    String wssUrlParams = request.getParameter("rqs");
    if ((wssUrlParams == null) || (wssUrlParams.length() == 0)) wssUrlParams = request.getParameter("amp;rqs");
    String addToMapHint = request.getParameter("atmh");
    if ((addToMapHint == null) || (addToMapHint.length() == 0)) addToMapHint = request.getParameter("amp;atmh");
    
    // license parameters
    String lLicenseReference = request.getParameter("licenseReference");
    String lWssUrl = request.getParameter("WSS");
    String lSuccess = request.getParameter("success");
    String lError = request.getParameter("errorMessage");
    
    // check for error
    if ((lSuccess == null) || !lSuccess.equals("true")) {
      if ((lError != null) && (lError.length() > 0)) {
        this.writeError(request,response,"An error occurred while acquiring a license: "+lError,Level.SEVERE);
      } else {
        this.writeError(request,response,"Canceled",null);
      }
    } else if ((act == null) || (act.length() == 0)) {
      this.writeError(request,response,"Empty parameter on license callback (act)",Level.SEVERE);
    } else if ((fwd == null) || (fwd.length() == 0)) {
      this.writeError(request,response,"Empty parameter on license callback (fwd)",Level.SEVERE);
    } else {
      
      // get license id
      String lLicenseId = null;
      IntegrationContextFactory icf = new IntegrationContextFactory();
      if (icf.isIntegrationEnabled()) {
        try {
          LOGGER.finer("Getting license id form licenseReference="+lLicenseReference);
          IntegrationContext ic = icf.newIntegrationContext();
          lLicenseId = ic.getLicenseId(lLicenseReference);
          LOGGER.finer("License id="+lLicenseId);
        } catch (Exception e) {
          String msg = "Error getting license id.";
          LOGGER.log(Level.SEVERE,msg,e);
          this.writeError(request,response,msg+": "+e.toString(),null);
          return;
        }
      }
      
      // change wss to http auth url
      if ((lWssUrl != null) && (lWssUrl.length() > 0)) {
        if ((lLicenseId != null) && (lLicenseId.length() > 0)) {
          lWssUrl = lWssUrl.replace("/WSS","/httpauth/licid-" + lLicenseId);
        }
        if (!lWssUrl.endsWith("?")) {
          lWssUrl += "?";
        }
        if ((wssUrlParams != null) && (wssUrlParams.length() > 0)) {
          lWssUrl += wssUrlParams;
        }
      } else {
        lWssUrl = "";
      }
      LOGGER.finer("Licensed WSS endpoint: "+lWssUrl);

      // determine the redirection endpoint based upon the resource link action
      if ((lWssUrl != null) && (lWssUrl.length() > 0)) {
        if (act.equals("open")) {
          fwd = lWssUrl;
        } else if (act.equals("preview")) {
          fwd = this.replaceParam(fwd,"url",lWssUrl);
        } else if (act.equals("addToMap")) {
          if ((addToMapHint != null) && (addToMapHint.length() > 0)) {
            lWssUrl = addToMapHint+":"+lWssUrl;
          }
          fwd = this.replaceParam(fwd,"resource",lWssUrl);
        } else {
          fwd = lWssUrl;
        }
      }
    
      // send the redirect
      if ((fwd != null) && (fwd.length() > 0)) {
        LOGGER.finer("Redirecting to: "+fwd);
        response.sendRedirect(fwd);
      }
    }
  }
  
  /**
   * Gather the parameters of a target URL.
   * <br/>This won't work for any URL, we are expecting a URL constructed by a ResourceLinkBuilder.
   * @param url the URL
   * @return a map of the parameters
   * @throws UnsupportedEncodingException if UTF-8 is unsupported (should never happen)
   */
  private Map<String,String> gatherParams(String url) throws UnsupportedEncodingException {
    Map<String,String> params = new HashMap<String,String>();
    int idx = url.indexOf("?");
    if (idx != -1) {
      String queryString = url.substring(idx+1);
      String[] pairs = queryString.split("&");
      for (String pair: pairs) {
        idx = pair.indexOf("=");
        if (idx > 0) {
          String key = pair.substring(0,idx);
          String value = pair.substring(idx+1);
          value = URLDecoder.decode(value,"UTF-8");
          params.put(key,value);
        }
      }
    }
    return params;
  }
  
  /**
   * Replaces a parameter value within a target URL.
   * <br/>This won't work for any URL, we are expecting a URL constructed by a ResourceLinkBuilder.
   * @param url the URL
   * @return the modified URL
   * @throws UnsupportedEncodingException if UTF-8 is unsupported (should never happen)
   */
  private String replaceParam(String url, String paramName, String paramValue) 
    throws UnsupportedEncodingException {
    String result = url;
    int idx = url.indexOf("?");
    if (idx != -1) {
      boolean bFound = true;
      String path = url.substring(0,idx);
      String queryString = url.substring(idx+1);
      String[] pairs = queryString.split("&");
      StringBuilder params = new StringBuilder();
      for (String pair: pairs) {
        idx = pair.indexOf("=");
        if (idx > 0) {
          String key = pair.substring(0,idx);
          String value = pair.substring(idx+1);
          value = URLDecoder.decode(value,"UTF-8");
          if (paramName.equals(key)) {
            value = paramValue;
            bFound = true;
          }
          if (params.length() > 0) params.append("&");
          params.append(URLEncoder.encode(key,"UTF-8"));
          params.append("=");
          params.append(URLEncoder.encode(value,"UTF-8"));
        }
      }
      if (bFound) {
        result = path+"?"+params.toString();
      }
    }
    return result;
  }
  
  /**
   * Writes characters to the response stream.
   * @param response the servlet response
   * @param content the content to write
   * @param charset the response character encoding 
   * @param contentType the response content type
   * @throws IOException if an IO exception occurs
   */
  private void writeCharacterResponse(HttpServletResponse response, 
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
        LOGGER.log(Level.SEVERE,"Error closing PrintWriter.",ef);
      }
    }
  }
  
  /**
   * Writes an error message to the response stream.
   * @param request the HTTP request
   * @param response the servlet response
   * @param message the error message
   * @param level if not null, the service side log file level
   * @throws IOException if an IO exception occurs
   */
  private void writeError(HttpServletRequest request, HttpServletResponse response, String message, Level level)
    throws IOException {
    if (level != null) {
      LOGGER.log(level,message);
    }
    String fwd = request.getContextPath()+"/catalog/tc/error.page?error="+URLEncoder.encode(message,"UTF-8");
    response.sendRedirect(fwd);
    
    /*
    String gptTitle = "Geoportal";
    String pageTitle = "Error";
    String css = request.getContextPath()+"/catalog/skins/themes/red/main.css";
    StringBuilder sbHtml = new StringBuilder();
    sbHtml.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
    sbHtml.append("\r\n<html>");
    sbHtml.append("\r\n<head>");
    sbHtml.append("\r\n<title>").append(Val.escapeXmlForBrowser(pageTitle)).append("</title>");
    sbHtml.append("\r\n<link rel=\"stylesheet\" type=\"text/css\" href=\""+Val.escapeXmlForBrowser(css)+"\"/>");
    sbHtml.append("\r\n<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>");
    sbHtml.append("\r\n<meta name=\"robots\" content=\"noindex\"/>");
    sbHtml.append("\r\n</head>");
    sbHtml.append("\r\n<body>");
    sbHtml.append("\r\n<div id=\"gptMainWrap\">");
    sbHtml.append("\r\n  <div id=\"gptBanner\">");
    sbHtml.append("\r\n    <div id=\"gptTitle\">").append(Val.escapeXmlForBrowser(gptTitle)).append("</div>");
    sbHtml.append("\r\n  </div>");
    sbHtml.append("\r\n  <p class=\"errorMessage\">").append(Val.escapeXmlForBrowser(message)).append("</p>");
    sbHtml.append("\r\n</div>");
    sbHtml.append("\r\n</body>");
    sbHtml.append("\r\n</html>");
    this.writeCharacterResponse(response,sbHtml.toString(),"UTF-8","text/html; charset=UTF-8");
    this.writeCharacterResponse(response,message,"UTF-8","text/plain; charset=UTF-8");
    */
  }
  
}
