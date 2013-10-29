/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.filter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Locale filer.
 * It will set a correct locale depending on the user session or the cookie.<br/>
 * The filter must be configured in web.xml with one initialization parameter: 
 * "locale-method". This parameter accepts two types of values:<br/>
 * <ul>
 * <li><b>cookie:&lt;cookie name&gt;</b> - for locale configuration in the cookie</li>
 * <li><b>cookie:&lt;session parameter name&gt;</b> - for locale configuration in the session parameter</li>
 * </ul>
 * Content of the cookie or value of the attribute must be a valid locale id like: 
 * "en" for english, "fr" for french, etc.
 */
public class LocaleFilter implements Filter {
  
  private static final boolean debug = true;
  // The filter configuration object we are associated with.  If
  // this value is null, this filter instance is not currently
  // configured. 
  private FilterConfig filterConfig = null;
  private ILocaleMethod localeMethod;
  
  public LocaleFilter() {
  }  
  
  /**
   *
   * @param request The servlet request we are processing
   * @param response The servlet response we are creating
   * @param chain The filter chain we are processing
   *
   * @exception IOException if an input/output error occurs
   * @exception ServletException if a servlet error occurs
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
          FilterChain chain)
          throws IOException, ServletException {
    
    Throwable problem = null;
    try {
      ServletRequest wrapedRequest = request;
      if (localeMethod!=null) {
        wrapedRequest = new LocaleWrapperHttpServletRequest((HttpServletRequest) request,localeMethod);       
      }
      chain.doFilter(wrapedRequest, response);
    } catch (Throwable t) {
      // If an exception is thrown somewhere down the filter chain,
      // we still want to execute our after processing, and then
      // rethrow the problem after that.
      problem = t;
      t.printStackTrace();
    }

    // If there was a problem, we want to rethrow it if it is
    // a known type, otherwise log it.
    if (problem != null) {
      if (problem instanceof ServletException) {
        throw (ServletException) problem;
      }
      if (problem instanceof IOException) {
        throw (IOException) problem;
      }
      sendProcessingError(problem, response);
    }
  }
  
  /**
   * Request wrapper that overrides "getLocale" method.
   */
  private static class LocaleWrapperHttpServletRequest extends
          HttpServletRequestWrapper {
  	
  	  final private HttpServletRequest httpServletRequest;
  	  final private ILocaleMethod localeMethod;
  	  final private Locale defaultLocale;
  	  final private Enumeration<Locale> defaultLocales;

  	  
      /**
       * Constructor.
       *
       * @param request The HTTP servlet request.
       */
      public LocaleWrapperHttpServletRequest(HttpServletRequest request,ILocaleMethod localeMethod) {
          super(request);
          this.httpServletRequest = request;
          this.localeMethod = localeMethod;
          this.defaultLocale = request.getLocale();
          this.defaultLocales = request.getLocales();
      }

      @Override
      public Locale getLocale() {
        Locale locale = localeMethod.getLocale(this);
        if (locale==null) {
          return defaultLocale;
        }
        return locale;
      }

      @Override
      public Enumeration<Locale> getLocales() {
        Locale locale = localeMethod.getLocale(this);
        if (locale==null) {
          return defaultLocales;
        }
        Vector<Locale> locales = new Vector<Locale>();
        locales.add(locale);
        return locales.elements();
      }

  }

  /**
   * Return the filter configuration object for this filter.
   */
  public FilterConfig getFilterConfig() {
    return (this.filterConfig);
  }

  /**
   * Set the filter configuration object for this filter.
   *
   * @param filterConfig The filter configuration object
   */
  public void setFilterConfig(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  /**
   * Destroy method for this filter
   */
  @Override
  public void destroy() {    
  }

  /**
   * Init method for this filter
   */
  @Override
  public void init(FilterConfig filterConfig) {    
    this.filterConfig = filterConfig;
    if (filterConfig != null) {
      String method = filterConfig.getInitParameter("locale-method");
      this.localeMethod = parseLocaleMethod(method);
    }
  }
  
  private ILocaleMethod parseLocaleMethod(String method) {
    ILocaleMethod locMeth = null;
    if (method!=null && !method.isEmpty()) {
      String [] parts = method.split(":");
      if (parts.length==2) {
        if (parts[0].equals("cookie")) {
          locMeth = new CookieLocaleMethod(parts[1]);
        } else if (parts[0].equals("session")) {
          locMeth = new SessionLocaleMethod(parts[1]);
        }
      }
    }
    return locMeth;
  }

  /**
   * Return a String representation of this object.
   */
  @Override
  public String toString() {
    if (filterConfig == null) {
      return ("LocaleFilter()");
    }
    StringBuilder sb = new StringBuilder("LocaleFilter(");
    sb.append(filterConfig);
    sb.append(")");
    return (sb.toString());
  }
  
  private void sendProcessingError(Throwable t, ServletResponse response) {
    String stackTrace = getStackTrace(t);    
    
    if (stackTrace != null && !stackTrace.equals("")) {
      try {
        response.setContentType("text/html");
        PrintStream ps = new PrintStream(response.getOutputStream());
        PrintWriter pw = new PrintWriter(ps);        
        pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

        // PENDING! Localize this for next official release
        pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");        
        pw.print(stackTrace);        
        pw.print("</pre></body>\n</html>"); //NOI18N
        pw.close();
        ps.close();
        response.getOutputStream().close();
      } catch (Exception ex) {
      }
    } else {
      try {
        PrintStream ps = new PrintStream(response.getOutputStream());
        t.printStackTrace(ps);
        ps.close();
        response.getOutputStream().close();
      } catch (Exception ex) {
      }
    }
  }
  
  private static String getStackTrace(Throwable t) {
    String stackTrace = null;
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      pw.close();
      sw.close();
      stackTrace = sw.getBuffer().toString();
    } catch (Exception ex) {
    }
    return stackTrace;
  }
  
  public void log(String msg) {
    filterConfig.getServletContext().log(msg);    
  }
  
  private Locale createLocaleFromString(String locale) {
    if (locale==null) return null;
    String [] elements = locale.split("_");
    if (elements==null) return null;
    switch (elements.length) {
      case 1: return new Locale(elements[0]);
      case 2: return new Locale(elements[0],elements[1]);
      case 3: return new Locale(elements[0],elements[1],elements[2]);
    }
    return null;
  }
  
  private interface ILocaleMethod {
    Locale getLocale(HttpServletRequest request);
  }
  
  private class CookieLocaleMethod implements ILocaleMethod {
    
    private String name;
    
    public CookieLocaleMethod(String name) {
      this.name = name;
    }

    @Override
    public Locale getLocale(HttpServletRequest request) {
      return readLocaleFromCookie(request, name);
    }
  
    private Locale readLocaleFromCookie(HttpServletRequest request, String cookieName) {
      for (Cookie cookie: request.getCookies()) {
        if (cookie.getName().endsWith(cookieName)) {
          return createLocaleFromString(cookie.getValue());
        }
      }
      return null;
    }
  }
  
  private class SessionLocaleMethod implements ILocaleMethod {
    
    private String name;
    
    public SessionLocaleMethod(String name) {
      this.name = name;
    }

    @Override
    public Locale getLocale(HttpServletRequest request) {
      return readLocaleFromSession(request, name);
    }
  
    private Locale readLocaleFromSession(HttpServletRequest request, String attributeName) {
      Object attribute = request.getSession(true).getAttribute(attributeName);
      if (attribute instanceof String) {
        return createLocaleFromString((String)attribute);
      }
      return null;
    }
    
  }
  
}
