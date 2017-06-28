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
package com.esri.gpt.control.webharvest;

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.crawl.HttpCrawlRequest;
import com.esri.gpt.framework.robots.Access;
import com.esri.gpt.framework.robots.Bots;
import static com.esri.gpt.framework.robots.BotsUtils.requestAccess;
import com.esri.gpt.framework.util.StringBuilderWriter;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default iteration context.
 */
public class DefaultIterationContext implements IterationContext {
  private static final Logger LOG = Logger.getLogger(DefaultIterationContext.class.getName());
  protected static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  protected final LinkedList<ExceptionInfo> exceptionInfos = new LinkedList<ExceptionInfo>();
  
  protected final Bots bots;

  /**
   * Creates instance with robots information.
   * @param robotsTxt robots information or <code>null</code> if no robots information available
   */
  public DefaultIterationContext(Bots robotsTxt) {
    this.bots = robotsTxt;
  }

  @Override
  public void onIterationException(Exception ex) {
    registerException(ex);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(new StringBuilderWriter(sb));
    
    for (ExceptionInfo ei: exceptionInfos) {
      writer.println(ei.toString());
    }
    
    // no need to close writer or catch any exception
    
    return sb.toString();
  }
  
  /**
   * Gets exception infos.
   * @return linked list of all registered exception infos.
   */
  public LinkedList<ExceptionInfo> getExceptionInfos() {
    return exceptionInfos;
  }
  
  /**
   * Registers an exception.
   * @param ex exception
   */
  public void registerException(Exception ex) {
    exceptionInfos.addLast(new ExceptionInfo(ex));
  }

  @Override
  public HttpClientRequest newHttpClientRequest() {
    return new HttpCrawlRequest(bots);
  }

  @Override
  public void assertAccess(String url) throws AccessException {
    if (bots!=null) {
      LOG.fine(String.format("Evaluating access to %s using robots.txt", url));
      try {
        String relativeUrl = extractRelativeUrl(url);
        Access access = requestAccess(bots, relativeUrl);
        if (!access.hasAccess()) {
          LOG.info(String.format("Access to %s disallowed by robots.txt", url));
          throw new AccessException(String.format("Access to %s disallowed by robots.txt", url), access);
        }
        LOG.fine(String.format("Access to %s allowed by robots.txt", url));
      } catch (AccessException ex) {
        throw ex;
      } catch (IOException ex) {
        LOG.log(Level.SEVERE,String.format("Unable to determine access to %s using robots.txt", url),ex);
        throw new AccessException(String.format("Unable to determine access to %s using robots.txt", url), ex);
      }
    }
  }

  @Override
  public Bots getRobotsTxt() {
    return bots;
  }
  
  private String extractRelativeUrl(String url) throws IOException {
    url = Val.chkStr(url);
    if (url.isEmpty()) return null;
    URL URL = new URL(url);
    return String.format("%s%s%s", URL.getPath(), URL.getQuery()!=null? "?"+URL.getQuery(): "", URL.getRef()!=null? "#"+URL.getRef(): "");
  }
  
  /**
   * Exception information.
   */
  public static final class ExceptionInfo {
    private final Date timestamp = new Date();
    private final Exception exception;

    /**
     * Creates instance of the exception information.
     * @param exception exception
     */
    public ExceptionInfo(Exception exception) {
      this.exception = exception;
    }

    /**
     * Gets timestamp.
     * @return timestamp
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Gets exception.
     * @return exception
     */
    public Exception getException() {
      return exception;
    }
    
    @Override
    public String toString() {
      return String.format("%s %s", DF.format(timestamp), exception.getMessage());
    }
  }
}
