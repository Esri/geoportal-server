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
package com.esri.gpt.framework.http;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.http.multipart.MultiPartContentProvider;
import com.esri.gpt.framework.http.multipart.PartWriter;
import com.esri.gpt.framework.util.Val;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.AuthState;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * Provides an interface for the execution of outbound HTTP requests.
 * <p/>
 * This class delegates underlying HTTP related functonality the the Apache HttpClient.
 * <p/>
 * If a forward proxy is in place, the following system properties must be
 * configured at the Java web server level (e.g. Tomcat - catalina.properties)  
 * <ul>
 *   <li>http.proxyHost, http.proxyPort, http.nonProxyHosts</li>
 *   <li>https.proxyHost, https.proxyPort, https.nonProxyHosts</li>
 * </ul>
 * If the forward proxy requires credentials, the following system properties 
 * are considered:
 * <ul>
 *   <li>http.proxyUser, http.proxyPassword</li>
 *   <li>https.proxyUser, https.proxyPassword</li>
 * </ul>
 * The gpt.xml file can be used to set the proxyUser/proxyPassword properties 
 * based upon an encrypted password:
 * <br/>&lt;gtpConfig&gt;
 * <br/>  &lt;forwardProxyAuth 
 * <br/>   username=""
 * <br/>   password=""
 * <br/>   encrypted="true"/&gt;
 * <br/>&lt;/gtpConfig&gt;
 * <p/>
 * The above system properties are also used by the Apache Axis module for SOAP 
 * based comminication.
 */
public class HttpClientRequest {
  /** class variables ========================================================= */
  public static final int DEFAULT_CONNECTION_TIMEOUT = 2 * 60 * 1000; // two minutes
  public static final int DEFAULT_RESPONSE_TIMEOUT   = 2 * 60 * 1000; // two minutes
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(HttpClientRequest.class.getName());
  
  // methods GET PUT POST DELETE HEAD OPTIONS TRACE
  
  /** instance variables ====================================================== */
  private HttpClient         batchHttpClient;
  private CredentialProvider credentialProvider;
  private ContentHandler     contentHandler;
  private ContentProvider    contentProvider;
  private StringBuffer       executionLog = new StringBuffer();
  private MethodName         methodName;
  private Map<String,String> requestHeaders = new LinkedHashMap<String,String>();
  private ResponseInfo       responseInfo = new ResponseInfo();
  private String             url;
  private int                connectionTimeOut = DEFAULT_CONNECTION_TIMEOUT;
  private int                responseTimeOut   = DEFAULT_RESPONSE_TIMEOUT;              
  private int                retries = -1;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public HttpClientRequest() {
    this.setCredentialProvider(CredentialProvider.getThreadLocalInstance());
    this.setConnectionTimeMs(getCatalogConfiguration().getConnectionTimeOutMs());
    this.setResponseTimeOutMs(getCatalogConfiguration().getResponseTimeOutMs());
  }
  
  /** properties **************************************************************/
  
  /**
   * Gets the underlying Apache HttpClient to be used for batch requests 
   * to the same server.
   * @return the batch client
   */
  public HttpClient getBatchHttpClient() {
    return this.batchHttpClient;
  }
  /**
   * Sets the underlying Apache HttpClient to be used for batch requests 
   * to the same server.
   * @param batchHttpClient the batch client
   */
  public void setBatchHttpClient(HttpClient batchHttpClient) {
    this.batchHttpClient = batchHttpClient;
  }
   
  /**
   * Gets the connection time out in milliseconds.
   * 
   * @return the connection time out (always >= 0)
   */
  public int getConnectionTimeOutMs() {
    if(connectionTimeOut < 0) {
      connectionTimeOut = 0;
    }
    return connectionTimeOut;
  }

  /**
   * Sets the connection time out in milliseconds.
   * 
   * @param connectionTimeOut the new connection time out
   */
  public void setConnectionTimeMs(int connectionTimeOut) {
    this.connectionTimeOut = connectionTimeOut;
  }

  /**
   * Gets the response time out in milliseconds
   * 
   * @return the response time out (always >= 0)
   */
  public int getResponseTimeOutMs() {
    if(responseTimeOut < 0) {
      responseTimeOut = 0;
    }
    return responseTimeOut;
  }

  /**
   * Sets the response time out in milliseconds.
   * 
   * @param responseTimeOut the new response time out
   */
  public void setResponseTimeOutMs(int responseTimeOut) {
    this.responseTimeOut = responseTimeOut;
  }
  
  /**
   * Gets the provider for HTTP authorization credentials.
   * @return the credential provider
   */
  public CredentialProvider getCredentialProvider() {
    return this.credentialProvider;
  }
  /**
   * Sets the provider for HTTP authorization credentials.
   * @param provider the credential provider
   */
  public void setCredentialProvider(CredentialProvider provider) {
    this.credentialProvider = provider;
  }
  
  /**
   * Gets the handler for the content of the HTTP response body.
   * @return the response content handler
   */
  public ContentHandler getContentHandler() {
    return this.contentHandler;
  }
  /**
   * Sets the handler for the content of the HTTP response body.
   * @param handler the response content handler
   */
  public void setContentHandler(ContentHandler handler) {
    this.contentHandler = handler;
  }
  
  /**
   * Gets the provider for the content of the HTTP request body.
   * @return the request content provider
   */
  public ContentProvider getContentProvider() {
    return this.contentProvider;
  }
  /**
   * Sets the provider for the content of the HTTP request body.
   * @param provider the request content provider
   */
  public void setContentProvider(ContentProvider provider) {
    this.contentProvider = provider;
  }
  
  /**
   * Gets a buffer representing the loggable content of an executed HTTP request.
   * @return the execution log buffer
   */
  public StringBuffer getExecutionLog() {
    return this.executionLog;
  }
  
  /**
   * Gets the HTTP method name.
   * @return the method name
   */
  public MethodName getMethodName() {
    return this.methodName;
  }
  /**
   * Sets the HTTP method name.
   * @param name the method name
   */
  public void setMethodName(MethodName name) {
    this.methodName = name;
  }
  
  /**
   * Sets an HTTP request header value.
   * @param name the header paramater name
   * @param value the header paramater value
   */
  public void setRequestHeader(String name, String value) {
    this.requestHeaders.put(name,value);
  }  
  
  /**
   * Gets information associated with an HTTP response.
   * @return the HTTP response information
   */
  public ResponseInfo getResponseInfo() {
    return this.responseInfo;
  }
  /**
   * Sets information associated with an HTTP response.
   * @param info the HTTP response information
   */
  protected void setResponseInfo(ResponseInfo info) {
    this.responseInfo = info;
  }
    
  /**
   * Gets the URL for the request.
   * @return the request URL
   */
  public String getUrl() {
    return this.url;
  }
  /**
   * Sets the URL for the request.
   * @param url the request URL
   */
  public void setUrl(String url) {
    this.url = url;
  }
  
  /**
   * Gets the retries.
   * 
   * @return the retries
   */
  public int getRetries() {
    return retries;
  }

  /**
   * Sets the retries.
   * 
   * @param retries the new retries
   */
  public void setRetries(int retries) {
    this.retries = retries;
  }

  /** methods ================================================================= */
    
  /**
   * Adds an retry handler to an HTTP method.
   * @param method the HttpMethod to be executed
   */
  private void addRetryHandler(HttpMethod method) {
    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        new DefaultHttpMethodRetryHandler(3,false));
  }
  
  /**
   * Applies authentication and forward proxy settings if required.
   * @param client the Apache HttpClient
   * @param url the target URL
   * @throws MalformedURLException if the URL is malformed
   */
  private void applyAuthAndProxySettings(HttpClient client, String url) 
    throws MalformedURLException {
    
    // initialize the target settings
    URL targetURL = new URL(url);
    String targetHost = targetURL.getHost();
    int targetPort = targetURL.getPort();
    String targetProtocol = targetURL.getProtocol();
    if (targetPort == -1) {
      if (targetProtocol.equalsIgnoreCase("https")) {
        targetPort = 443;
      } else {
        targetPort = 80;
      }
    }

    // TODO: is the host setting required?
    //HostConfiguration config = client.getHostConfiguration();
    //config.setHost(targetHost,targetPort,targetProtocol);
    
    // establish authentication credentials
    if (this.getCredentialProvider() != null) {
      String username = this.getCredentialProvider().getUsername();
      String password = this.getCredentialProvider().getPassword();
      if ((username != null) && (username.length() > 0) && (password != null)) {
        AuthScope scope = new AuthScope(null,-1);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username,password);
        client.getState().setCredentials(scope,creds);
        
        // NTLM is based upon username pattern (domain\\username)
        int ntDomainIdx = username.indexOf("\\"); 
        if (ntDomainIdx > 0) {
          String left = username.substring(0,ntDomainIdx);
          String right = username.substring(ntDomainIdx+1);
          if ((left.length() > 0) && (right.length() > 0)) {
            String ntDomain = left;
            username = right;
            String ntHost = targetHost; // TODO: should this be the host sending the request?
            AuthScope ntScope = new AuthScope(null,-1,null,AuthPolicy.NTLM);
            NTCredentials ntCreds = new NTCredentials(username,password,ntHost,ntDomain);
            client.getState().setCredentials(ntScope,ntCreds);
          }
        }
      }
    }
       
    // initialize the proxy settings
    String proxyHost = Val.chkStr((String)System.getProperty(targetProtocol+".proxyHost"));
    int proxyPort = Val.chkInt((String)System.getProperty(targetProtocol+".proxyPort"),-1);
    String nonProxyHosts = Val.chkStr((String)System.getProperty(targetProtocol+".nonProxyHosts"));
    String proxyUser = Val.chkStr((String)System.getProperty(targetProtocol+".proxyUser"));
    String proxyPassword = (String)System.getProperty(targetProtocol+".proxyPassword");
    if (proxyPort == -1) {
      proxyPort = 80;
    }
    
    // check for a non-proxy host match
    boolean isNonProxyHost = false;
    if ((proxyHost.length() > 0) && (nonProxyHosts.length() > 0)) {
      StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts,"|\"");
      while (tokenizer.hasMoreTokens()) {
        String nonProxyHost = Val.chkStr(tokenizer.nextToken());
        if (nonProxyHost.length() > 0) {
          if (nonProxyHost.indexOf("*") != -1) {
            StringBuffer sb = new StringBuffer();
            sb.append('^');
            for (int i = 0;i<nonProxyHost.length();i++) {
              char c = nonProxyHost.charAt(i);
              switch(c) {
                case '*':
                  sb.append(".*");
                  break;
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\': case '?':
                 sb.append("\\").append(c);
                 break;
                default:
                 sb.append(c);
                 break;
              }
            }
            sb.append('$');
            nonProxyHost = sb.toString();
          }
          try {
            if (targetHost.matches(nonProxyHost)) {
              isNonProxyHost = true;
              break;
            }
          } catch (PatternSyntaxException pse) {
            // TODO: warn if the pattern syntax is incorrect?
            //pse.printStackTrace(System.err);
          }
        }
      }
    }
    
    // set the proxy and authentication credentials if required
    if ((proxyHost.length() > 0) && !isNonProxyHost) {

      // configure the proxy host and port
      client.getHostConfiguration().setProxy(proxyHost,proxyPort);
          
      // establish proxy-authentication credentials // TODO: lookup gpt.xml proxy-auth
      String username = proxyUser;
      String password = proxyPassword;
      if ((username != null) && (username.length() > 0) && (password != null)) {
        AuthScope scope = new AuthScope(null,-1);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username,password);
        client.getState().setProxyCredentials(scope,creds);
        
        // NTLM is based upon username pattern (domain\\username)
        int ntDomainIdx = username.indexOf("\\"); 
        if (ntDomainIdx > 0) {
          String left = username.substring(0,ntDomainIdx);
          String right = username.substring(ntDomainIdx+1);
          if ((left.length() > 0) && (right.length() > 0)) {
            String ntDomain = left;
            username = right;
            String ntHost = proxyHost; // TODO: should this be the host sending the request?
            AuthScope ntScope = new AuthScope(null,-1,null,AuthPolicy.NTLM);
            NTCredentials ntCreds = new NTCredentials(username,password,ntHost,ntDomain);
            client.getState().setProxyCredentials(ntScope,ntCreds);
          }
        }
      }
    }
    
  }
        
  /**
   * Create the HTTP method.
   * <br/>A GetMethod will be created if the RequestEntity associated with
   * the ContentProvider is null. Otherwise, a PostMethod will be created.
   * @return the HTTP method
   */
  private HttpMethodBase createMethod() throws IOException {
    HttpMethodBase method = null;
    MethodName name = this.getMethodName();

    // make the method
    if (name == null) {
      if (this.getContentProvider() == null) {
        this.setMethodName(MethodName.GET);
        method = new GetMethod(this.getUrl());
      } else {
        this.setMethodName(MethodName.POST);
        method = new PostMethod(this.getUrl());
      }
    } else if (name.equals(MethodName.DELETE)) {
      method = new DeleteMethod(this.getUrl());
    } else if (name.equals(MethodName.GET)) {
      method = new GetMethod(this.getUrl());
    } else if (name.equals(MethodName.POST)) {
      method = new PostMethod(this.getUrl());
    } else if (name.equals(MethodName.PUT)) {
      method = new PutMethod(this.getUrl());
    } 
    
    // write the request body if necessary
    if (this.getContentProvider() != null) {
      if (method instanceof EntityEnclosingMethod) {
        EntityEnclosingMethod eMethod = (EntityEnclosingMethod)method;
        RequestEntity eAdapter = 
                getContentProvider() instanceof MultiPartContentProvider?
                new MultiPartProviderAdapter(this, eMethod, (MultiPartContentProvider)getContentProvider()):
                new ApacheEntityAdapter(this,this.getContentProvider());
        eMethod.setRequestEntity(eAdapter);
        if (eAdapter.getContentType() != null) {
          eMethod.setRequestHeader("Content-type",eAdapter.getContentType());
        }
      } else {
        // TODO: possibly will need an exception here in the future
      }
    }
    
    // set headers, add the retry method
    for (Map.Entry<String,String> hdr: this.requestHeaders.entrySet()) {
      method.addRequestHeader(hdr.getKey(),hdr.getValue());
    }

    // declare possible gzip handling
    method.setRequestHeader("Accept-Encoding", "gzip");
    
    this.addRetryHandler(method);
    return method;
  }
  
  /**
   * Determines basic information associted with an HTTP response.
   * <br/>For example: response status message, Content-Type, Content-Length
   * @param method the HttpMethod that was executed
   */
  private void determineResponseInfo(HttpMethodBase method) {
    this.getResponseInfo().setResponseMessage(method.getStatusText());
    this.getResponseInfo().setResponseHeaders(method.getResponseHeaders());
    Header contentTypeHeader = method.getResponseHeader("Content-Type");
    if (contentTypeHeader != null) {
      HeaderElement values[] = contentTypeHeader.getElements();
      // Expect only one header element to be there, no more, no less
      if (values.length == 1) {
        this.getResponseInfo().setContentType(values[0].getName());
        NameValuePair param = values[0].getParameterByName("charset");
        if (param != null) {
          // If invalid, an UnsupportedEncondingException will result
          this.getResponseInfo().setContentEncoding(param.getValue());
        }
      }
    }
    this.getResponseInfo().setContentLength(method.getResponseContentLength());
  }
  
  /**
   * Executes the HTTP request.
   * @throws IOException if an Exception occurs
   */
  public void execute() throws IOException {
    
    // initialize
    this.executionLog.setLength(0);
    StringBuffer log = this.executionLog;
    ResponseInfo respInfo = this.getResponseInfo();
    respInfo.reset();
    InputStream responseStream = null;
    HttpMethodBase method = null;
    
    try {
      log.append("HTTP Client Request\n").append(this.getUrl());
      
      // make the Apache HTTPClient
      HttpClient client = this.batchHttpClient;
      if (client == null) {
        client = new HttpClient();
        boolean alwaysClose = Val.chkBool(Val.chkStr(ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters().getValue("httpClient.alwaysClose")), false); 
        if (alwaysClose) {
          client.setHttpConnectionManager(new SimpleHttpConnectionManager(true));
        }
      }
      
      
      // setting timeout info
      client.getHttpConnectionManager().getParams().setConnectionTimeout(
          getConnectionTimeOutMs());
      client.getHttpConnectionManager().getParams().setSoTimeout(
          getResponseTimeOutMs());
      
      // setting retries
      int retries = this.getRetries();
      
      
      // create the client and method, apply authentication and proxy settings
      method = this.createMethod();
      //method.setFollowRedirects(true);
      if(retries > -1) {
        // TODO: not taking effect yet?
        DefaultHttpMethodRetryHandler retryHandler = 
          new DefaultHttpMethodRetryHandler(retries, true);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
            retryHandler);
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
            retryHandler);
      }
     
      this.applyAuthAndProxySettings(client,this.getUrl());
 
      
      // execute the method, determine basic information about the response
      respInfo.setResponseCode(client.executeMethod(method));
      this.determineResponseInfo(method);
      
      // collect logging info
      if (LOGGER.isLoggable(Level.FINER)) {
        log.append("\n>>").append(method.getStatusLine());
        log.append("\n--Request Header");
        for (Header hdr: method.getRequestHeaders()) {
          log.append("\n  ").append(hdr.getName()+": "+hdr.getValue());
        }
        log.append("\n--Response Header");
        for (Header hdr: method.getResponseHeaders()) {
          log.append("\n  ").append(hdr.getName()+": "+hdr.getValue());
        }
        
        //log.append(" responseCode=").append(this.getResponseInfo().getResponseCode());
        //log.append(" responseContentType=").append(this.getResponseInfo().getContentType());
        //log.append(" responseContentEncoding=").append(this.getResponseInfo().getContentEncoding());
        //log.append(" responseContentLength=").append(this.getResponseInfo().getContentLength());
        
        if (this.getContentProvider() != null) {
          String loggable = this.getContentProvider().getLoggableContent();
          if (loggable != null) {
            log.append("\n--Request Content------------------------------------\n").append(loggable);
          }
        }
      }
      
      // throw an exception if an error is encountered
      if ((respInfo.getResponseCode() < 200) || (respInfo.getResponseCode() >= 300)) {
        String msg = "HTTP Request failed: " + method.getStatusLine();
        if (respInfo.getResponseCode() == HttpStatus.SC_UNAUTHORIZED) {
          AuthState authState = method.getHostAuthState();
          AuthScheme authScheme = authState.getAuthScheme();          
          HttpClient401Exception authException = new HttpClient401Exception(msg);
          authException.setUrl(this.getUrl());
          authException.setRealm(authState.getRealm());  
          authException.setScheme(authScheme.getSchemeName());
          if ((authException.getRealm() == null) || (authException.getRealm().length() == 0)) {
            authException.setRealm(authException.generateHostBasedRealm());
          }
          throw authException;
        } else {
          throw new HttpClientException(respInfo.getResponseCode(),msg);
        }
      }
           
      // handle the response
      if (this.getContentHandler() != null) {
        if (getContentHandler().onBeforeReadResponse(this)) {
          responseStream = getResponseStream(method);
          if (responseStream != null) {
            this.getContentHandler().readResponse(this,responseStream);
          }
        }
 
        // log thre response content
        String loggable = this.getContentHandler().getLoggableContent();
        long nBytesRead = this.getResponseInfo().getBytesRead();
        long nCharsRead = this.getResponseInfo().getCharactersRead();
        if ((nBytesRead >= 0) || (nCharsRead >= 0) || (loggable != null)) {
          log.append("\n--Response Content------------------------------------");
          if (nBytesRead >= 0) log.append("\n(").append(nBytesRead).append(" bytes read)");
          if (nCharsRead >= 0) log.append("\n(").append(nCharsRead).append(" characters read)");
          if (loggable != null) log.append("\n").append(loggable);
        }
      }
      
    } finally {
      
      // cleanup
      try {
        if (responseStream != null) responseStream.close();
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE,"Unable to close HTTP response stream.",t);
      }
      try {
        if (method != null) method.releaseConnection();
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE,"Unable to release HttpMethod",t);
      }
      
      // log the request/response
      if (LOGGER.isLoggable(Level.FINER)) {
        LOGGER.finer(this.getExecutionLog().toString());
      }
    }  
  }

  /**
   * Gets the HTTP response stream.
   * @param method the HTTP method
   * @return the response stream
   * @throws IOException if an i/o exception occurs 
   */
  private InputStream getResponseStream(HttpMethodBase method) throws IOException {
    // Check is Content-Encoding is gzip
    Header hdr = method.getResponseHeader("Content-Encoding");
    if (hdr!=null && "gzip".equals(hdr.getValue())) {
      return new GZIPInputStream(method.getResponseBodyAsStream());
    }
    return method.getResponseBodyAsStream();
  }
  
  /**
   * Instantiates a new HTTP client request.
   * @return the HTTP client request
   */
  public static HttpClientRequest newRequest() {
    return new HttpClientRequest();
  }
  
  /**
   * Instantiates a new HTTP client request.
   * @param name the HTTP method name
   * @param url the target URL
   * @return the HTTP client request
   */
  public static HttpClientRequest newRequest(MethodName name, String url) {
    HttpClientRequest request = HttpClientRequest.newRequest();
    request.setMethodName(name);
    request.setUrl(url);
    return request;
  }
 
  /**
   * Exceutes the HPPR request and returns the response as a string.
   * @return the HTTP response
   * @throws IOException if an i/o exception occurs 
   */
  public String readResponseAsCharacters() throws IOException {
    StringHandler handler = new StringHandler();
    this.setContentHandler(handler);
    this.execute();
    return Val.removeBOM(handler.getContent());
  }

  /**
   * Gets catalog configuration.
   * @return catalog configuration
   */
  private CatalogConfiguration getCatalogConfiguration() {
    return ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration();
  }
  /** inner classes =========================================================== */
 
  /** The enumeration of upported HTTP method names. */
  public enum MethodName {
    GET,
    DELETE,
    POST,
    PUT;
  }

  private static class PartWriterAdapter implements PartWriter {
    ArrayList<org.apache.commons.httpclient.methods.multipart.Part> parts;
    
    public PartWriterAdapter(ArrayList<org.apache.commons.httpclient.methods.multipart.Part> parts) {
      this.parts = parts;
    }

    @Override
    public void write(String name, String value) throws IOException {
      parts.add(new StringPart(name, value, "UTF-8"));
    }

    @Override
    public void write(String name, final File file, String fileName, String contentType, String charset, final boolean deleteAfterUpload) throws IOException {
      parts.add(new FilePart(name, fileName!=null && !fileName.isEmpty()? fileName: file.getName(), file, contentType, charset){
        @Override
        protected void sendData(OutputStream out) throws IOException {
          super.sendData(out);
          if (deleteAfterUpload) {
            try {
              boolean deleted = file.delete();
              if (!deleted) {
                LOGGER.warning("Unable to delete file: "+file.getAbsolutePath());
              }
            } catch (SecurityException ex) {
              LOGGER.warning("Unable to delete file: "+file.getAbsolutePath());
            }
          }
        }
      });
    }

    @Override
    public void write(String name, byte[] bytes, String fileName, String contentType, String charset) throws IOException {
      parts.add(new FilePart(name, new ByteArrayPartSource(fileName, bytes), contentType, charset));
    }
  }
  
  /**
   * Multi part provider adapter.
   */
  private static class MultiPartProviderAdapter implements RequestEntity {
    private HttpClientRequest request;
    private EntityEnclosingMethod method;
    private MultiPartContentProvider provider;
    private MultipartRequestEntity entity;

    public MultiPartProviderAdapter(HttpClientRequest request, EntityEnclosingMethod method, MultiPartContentProvider provider) throws IOException {
      this.request = request;
      this.method = method;
      this.provider = provider;
//      ArrayList<org.apache.commons.httpclient.methods.multipart.Part> parts = new ArrayList<org.apache.commons.httpclient.methods.multipart.Part>();
//      provider.writeParts(new PartWriterAdapter(parts));
//      this.entity = new MultipartRequestEntity(parts.toArray(new org.apache.commons.httpclient.methods.multipart.Part[parts.size()]), method.getParams());
    }

    private MultipartRequestEntity getEntity() throws IOException {
      if (entity==null) {
        ArrayList<org.apache.commons.httpclient.methods.multipart.Part> parts = new ArrayList<org.apache.commons.httpclient.methods.multipart.Part>();
        provider.writeParts(new PartWriterAdapter(parts));
        entity = new MultipartRequestEntity(parts.toArray(new org.apache.commons.httpclient.methods.multipart.Part[parts.size()]), method.getParams());
      }
      return entity;
    }
    
    @Override
    public boolean isRepeatable() {
      try {
        return getEntity().isRepeatable();
      } catch (IOException ex) {
        return true;
      }
    }

    @Override
    public void writeRequest(OutputStream out) throws IOException {
      getEntity().writeRequest(out);
    }

    @Override
    public long getContentLength() {
      try {
        return getEntity().getContentLength();
      } catch (IOException ex) {
        return 0;
      }
    }

    @Override
    public String getContentType() {
      try {
        return getEntity().getContentType();
      } catch (IOException ex) {
        return "";
      }
    }
  }
}
