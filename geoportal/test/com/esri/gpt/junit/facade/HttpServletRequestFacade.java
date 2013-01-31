package com.esri.gpt.junit.facade;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.http.HttpSession;

/**
 * Provides an implementation facade for an HttpServletRequest.
 */
@SuppressWarnings("unchecked")
public class HttpServletRequestFacade extends HttpServletRequestImpl {
  
  public String      contextPath = "/geoportal";
  public Hashtable   parameters = new Hashtable();
  public String      queryString = null;
  public String      remoteAddr = "ip.remote.address";
  public HttpSession session = new HttpSessionFacade();

  /**
   * Constructs with a supplied query string.
   * @param queryString the query string
   */
  public HttpServletRequestFacade(String queryString) {
    
    this.queryString = queryString;
    if (queryString != null) {
      String[] pairs = queryString.split("&");
      for (String pair: pairs) {
        String key = null;
        String value = null;
        //String[] parts = pair.split("=");
        int idx = pair.indexOf("=");
        if (idx > 0) {
          key = pair.substring(0,idx);
          value = pair.substring(idx+1);
        }

        //if (parts.length == 1) {
        //  key = parts[0];
        //  value = "";
        //} else if (parts.length == 2) {
        //  key = parts[0];
        //  value = parts[1];
        //}
        if (key != null) {
          String[] values = (String[])this.parameters.get(key);
          if (values == null) {
            this.parameters.put(key,new String[]{value});
          } else {
            ArrayList<String> al = new ArrayList<String>();
            for (String v: values) {
              if ((value != null) && (value.length() > 0)) {
                al.add(v);
              }
            }
            al.add(value);
            this.parameters.put(key,al.toArray(new String[0]));
          }
        }
      }
    }
    
  }
  
  /**
   * Gets the context path.
   * @return the context path
   */
  @Override
  public String getContextPath() {
    return this.contextPath;
  }
  
  /**
   * Gets the first parameter values associated with a name.
   * @param parameterName the parameter name
   * @return the parameter value
   */
  @Override
  public String getParameter(String parameterName) {
    String[] values = this.getParameterValues(parameterName);
    if ((values != null) && (values.length > 0)) {
      return values[0];
    }
    return null;
  }
  
  /**
   * Gets the parameter map.
   * @return the parameter map
   */
  @Override
  public Map getParameterMap() {
    return this.parameters;
  }
  
  /**
   * Gets the parameter names.
   * @return the parameter names
   */
  @Override
  public Enumeration getParameterNames() {
    return this.parameters.keys();
  }
  
  /**
   * Gets the parameter values associated with a name.
   * @param parameterName the parameter name
   * @return the associated values
   */
  @Override
  public String[] getParameterValues(String parameterName) {
    return (String[])this.parameters.get(parameterName);
  }
  
  /**
   * Gets the query string.
   * @return the query string
   */
  @Override
  public String getQueryString() {
    return this.queryString;
  }
  

  @Override
  public String getRemoteAddr() {
    return this.remoteAddr;
  }
  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }
  
  @Override
  public String getRequestURI() {
    return "";
  }

  @Override
  public StringBuffer getRequestURL() {
    return new StringBuffer(this.contextPath);
  }
  
  /**
   * Gets the session.
   * @return the session
   */
  @Override
  public HttpSession getSession() {
    return this.session;
  }

  /**
   * Gets the session.
   * @param create a new session if necessary
   * @return the session
   */
  @Override
  public HttpSession getSession(boolean create) {
    return this.session;
  }

}
