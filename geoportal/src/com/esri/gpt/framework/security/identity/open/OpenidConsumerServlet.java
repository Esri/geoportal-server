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
package com.esri.gpt.framework.security.identity.open;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.expressme.openid.Association;
import org.expressme.openid.Authentication;
import org.expressme.openid.Endpoint;
import org.expressme.openid.OpenIdException;
import org.expressme.openid.OpenIdManager;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 * Openid authentication consumer servlet (Openid, oAuth-Twitter).
 * <p/>
 * This is basically a replacement for org.expressme.openid.MainServlet.
 * <p/>
 * An Openid provider must return at least an identifier and an email address.
 * <p/>
 * Twitter return a screen name.
 */
public class OpenidConsumerServlet extends HttpServlet {
  
  /*
  google/yahoo
  DN = urn:openid:http://host/...?id=...
  username = email

  twitter
  DN = urn:openid:twitter:screenname
  username = screenname@twitter
  */
  
  private static Logger LOGGER = Logger.getLogger(OpenidConsumerServlet.class.getName());

  private static final long ONE_HOUR = 3600000L;
  private static final long TWO_HOUR = ONE_HOUR * 2L;
  private static final String ATTR_ALIAS = "gpt_openid_alias";
  private static final String ATTR_CBINFO = "gpt_openid_cbinfo";
  private static final String ATTR_MAC = "gpt_openid_mac";
  private static final String ATTR_TOKEN = "gpt_oauth_token";
  private static final String ATTR_TOKEN_SECRET = "gpt_oauth_token_secret";

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
    RequestContext context = null;
    boolean useFacade = false;
    String err = "";
    try { 
      LOGGER.finer("Query string="+request.getQueryString());
      
      String op = request.getParameter("op");
      context = RequestContext.extract(request);
      OpenProviders providers = context.getIdentityConfiguration().getOpenProviders();
      if ((providers == null) || (providers.size() == 0)) {
        return;
      }
      String baseContextPath = RequestContext.resolveBaseContextPath(request);
      String callbackUrl = baseContextPath+"/openid";
      String realm = baseContextPath;
      HttpSession session = request.getSession();
      
      // process a response from an Openid provider
      if (op == null) {
        String identity = null;
        String username = null;
        String email = null;
        
        // determine the callback info
        String cbinfo = Val.chkStr((String)session.getAttribute(ATTR_CBINFO));
        session.setAttribute(ATTR_CBINFO,null);
        if (cbinfo.length() == 0) {
          throw new ServletException("Invalid openid callback info.");
        }
        
        int idx = cbinfo.indexOf(",");
        long millis = Long.parseLong(cbinfo.substring(0,idx));
        cbinfo = cbinfo.substring(idx+1);
        idx = cbinfo.indexOf(",");
        String cbid = cbinfo.substring(0,idx);
        cbinfo = cbinfo.substring(idx+1);
        idx = cbinfo.indexOf(",");
        op = cbinfo.substring(0,idx);
        String fwd = cbinfo.substring(idx+1);
        LOGGER.finer("cbinfo retrieved: "+cbinfo);
        
        // determine the provider
        OpenProvider provider = providers.get(op);
        if (provider == null) {
          throw new ServletException("Invalid openid op parameter on callback: "+op);
        }
        boolean isTwitter = provider.getName().equalsIgnoreCase("Twitter");
        
        // determine the authenticated user attributes
        if (useFacade) {
          identity = "http://openidfacade/user123";
          email = "user123@openidfacade.com";
          username = email;
          
        // Twitter callback
        } else if (isTwitter) {
          try {
            LOGGER.finer("Determining user attributes for: "+op);
            String token = (String)session.getAttribute(ATTR_TOKEN);
            String tokenSecret = (String)session.getAttribute(ATTR_TOKEN_SECRET);
            Twitter twitter = new Twitter();
            twitter.setOAuthConsumer(provider.getConsumerKey(),provider.getConsumerSecret());
            AccessToken accessToken = twitter.getOAuthAccessToken(token,tokenSecret);
            twitter.setOAuthAccessToken(accessToken);
            twitter4j.User tUser = twitter.verifyCredentials();
            String screenName = Val.chkStr(tUser.getScreenName());
            if (screenName.length() > 0) {
              username = screenName+"@twitter";
              identity = "twitter:"+screenName;
            }
          } catch (Exception e) {
            err = "oAuth authentication failed.";
            LOGGER.log(Level.WARNING,err,e);
          }
          
        // Openid callback
        } else {
          try {
            
            // determine the callback UUID
            String cbidParam = Val.chkStr(request.getParameter("cbid"));
            if (cbidParam.length() == 0) {
              throw new ServletException("Empty cbid parameter on callback.");
            }
            
            if (!cbid.equals(cbidParam)) {
              throw new ServletException("Invalid openid cbid parameter on callback.");
            }
            callbackUrl += "?cbid="+java.net.URLEncoder.encode(cbid,"UTF-8");
            LOGGER.finer("cbinfo based callback: "+cbinfo);
            LOGGER.finer("Determining user attributes for: "+op);
            
            OpenIdManager manager = new OpenIdManager();
            manager.setRealm(realm); 
            manager.setReturnTo(callbackUrl);  
            
            checkNonce(request.getParameter("openid.response_nonce"));
            byte[] mac_key = (byte[])session.getAttribute(ATTR_MAC);
            String alias = (String)session.getAttribute(ATTR_ALIAS);
            Authentication authentication = manager.getAuthentication(request,mac_key,alias);
            identity = authentication.getIdentity();
            email = authentication.getEmail();
            username = email;
          } catch (Exception e) {
            err = "Openid authentication suceeded, creating local user reference failed.";
            LOGGER.log(Level.WARNING,err,e);
          }
        }
        
        // check the parameters
        identity = Val.chkStr(identity);
        username = Val.chkStr(username);
        email = Val.chkStr(email);
        LOGGER.finer("User attributes: identity="+identity+", username="+username+", email="+email);
        if (identity.length() == 0) {
          err = "Your openid idenitfier was not determined.";
        } else if (username.length() == 0) {
          if (isTwitter) {
            err = "Your opennid screen name was not determined.";
          } else {
            err = "Your opennid email address was not determined.";
          }
        } else {
          
          // establish the user
          identity = "urn:openid:"+identity;
          User user = context.getUser();
          user.reset();
          user.setKey(identity);
          user.setDistinguishedName(identity);
          user.setName(username);
          user.getProfile().setUsername(username);
          if (email.length() > 0) {
            user.getProfile().setEmailAddress(email);
          }
          user.getAuthenticationStatus().setWasAuthenticated(true);
          
          // ensure a local reference for the user
          try {
            LocalDao localDao = new LocalDao(context);
            localDao.ensureReferenceToRemoteUser(user);
          } catch (Exception e) {
            user.reset();
            err = "Openid authentication suceeded, creating local user reference failed.";
            LOGGER.log(Level.SEVERE,err,e);
          }
        }
        
        // redirect to the originating page
        String url = fwd;
        err = Val.chkStr(err);
        if (err.length() > 0) {
          if (url.indexOf("?") == -1) fwd += "?";
          else url += "&";
          url += "err="+URLEncoder.encode(err,"UTF-8");
        }
        response.sendRedirect(url);
        
      // process a request to enter Openid credentials
      } else if (op.length() > 0) {
        session.setAttribute(ATTR_CBINFO,null);
        
        // determine the provider
        OpenProvider provider = providers.get(op);
        if (provider == null) {
          throw new ServletException("Invalid openid op parameter: "+op);
        }
        boolean isTwitter = provider.getName().equalsIgnoreCase("Twitter");
        
        // determine the active Geoportal page (forward URL)
        String fwd = Val.chkStr(request.getParameter("fwd"));
        if (fwd.length() == 0) {
          throw new ServletException("Empty openid fwd parameter.");
        }
        
        // store the callback info
        String cbid = UUID.randomUUID().toString();
        long millis = System.currentTimeMillis();
        String cbinfo = millis+","+cbid+","+op+","+fwd;
        session.setAttribute(ATTR_CBINFO,cbinfo);        
        
        // determine the Openid Authentication URL
        String url = null;
        if (useFacade) {
          PrintWriter pw = response.getWriter();
          pw.println("<html><head><title>Openid Facade</title></head><body><h1>Openid Facade</h1>");
          pw.println("<a href=\""+callbackUrl+"\">Supply credentials step</a>");
          pw.println("</body></html>");
          pw.flush();
          return;
          
        // Twitter
        } else if (isTwitter) {
          try {
            LOGGER.fine("Initiating oAuth request for: "+op+", callback="+callbackUrl);
            Twitter twitter = new Twitter();
            twitter.setOAuthConsumer(provider.getConsumerKey(),provider.getConsumerSecret());
            RequestToken requestToken = twitter.getOAuthRequestToken();
            String token = requestToken.getToken();
            String tokenSecret = requestToken.getTokenSecret();
            session.setAttribute(ATTR_TOKEN,token);
            session.setAttribute(ATTR_TOKEN_SECRET,tokenSecret);
            url = requestToken.getAuthorizationURL();            
          } catch (TwitterException e) {
            err = "Unable to determine endpoint for: "+op;
            LOGGER.log(Level.SEVERE,err,e);
          }

        // Openid
        } else {
          try {
            callbackUrl += "?cbid="+java.net.URLEncoder.encode(cbid,"UTF-8");
            LOGGER.finer("Initiating openid request for: "+op+", callback="+callbackUrl);
            OpenIdManager manager = new OpenIdManager();
            manager.setRealm(realm); 
            manager.setReturnTo(callbackUrl);  
             
            // There is an issue here. It seems that the only way to set the endpoint
            // alias is through the jopenid-1.07.jar openid-providers.properties,
            // but we would to to configure the provider properties through gpt.xml

            //Endpoint endpoint = manager.lookupEndpoint(provider.getAuthenticationUrl());
            Endpoint endpoint = manager.lookupEndpoint(op);

            Association association = manager.lookupAssociation(endpoint);
            request.getSession().setAttribute(ATTR_MAC,association.getRawMacKey());
            request.getSession().setAttribute(ATTR_ALIAS,endpoint.getAlias());
            url = manager.getAuthenticationUrl(endpoint,association);
          } catch (Exception e) {
            err = "Unable to determine Openid endpoint for: "+op;
            LOGGER.log(Level.SEVERE,err,e);
          }
          
        } 
        
        // redirect to the authentication endpoint or to originating page
        err = Val.chkStr(err);
        if (err.length() > 0) {
          url = fwd;
          if (url.indexOf("?") == -1) fwd += "?";
          else url += "&";
          url += "err="+URLEncoder.encode(err,"UTF-8");
        }
        LOGGER.finer("Redirecting for authentication: "+url);
        response.sendRedirect(url);
        
      } else {
        throw new ServletException("Empty openid op parameter.");
      }
    } finally {
      if (context != null) context.onExecutionPhaseCompleted();
    }
  }
  
  /**
   * Taken from org.expressme.openid.MainServlet
   */
  private void showAuthentication(PrintWriter pw, String identity, String email) {
    pw.print("<html><body><h1>Identity</h1><p>");
    pw.print(identity);
    pw.print("</p><h1>Email</h1><p>");
    pw.print(email==null ? "(null)" : email);
    pw.print("</p></body></html>");
    pw.flush();
  }

  /**
   * Taken from org.expressme.openid.MainServlet
   */
  private void checkNonce(String nonce) {
    // check response_nonce to prevent replay-attack:
    if (nonce==null || nonce.length()<20) throw new OpenIdException("Verify failed.");
    long nonceTime = getNonceTime(nonce);
    long diff = System.currentTimeMillis() - nonceTime;
    if (diff < 0) diff = (-diff);
    if (diff > ONE_HOUR) throw new OpenIdException("Bad nonce time.");
    if (isNonceExist(nonce)) throw new OpenIdException("Verify nonce failed.");
    storeNonce(nonce, nonceTime + TWO_HOUR);
  }

  /**
   * Taken from org.expressme.openid.MainServlet
   */
  private boolean isNonceExist(String nonce) {
    // check if nonce is exist in database:
    return false;
  }

  /**
   * Taken from org.expressme.openid.MainServlet
   */
  private void storeNonce(String nonce, long expires) {
    // store nonce in database:
  }

  /**
   * Taken from org.expressme.openid.MainServlet
   */
  private long getNonceTime(String nonce) {
    try {
      return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(nonce.substring(0,19)+"+0000").getTime();
    } catch(ParseException e) {
      throw new OpenIdException("Bad nonce time.");
    }
  }

}

