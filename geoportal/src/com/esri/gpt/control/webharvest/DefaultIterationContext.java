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
import com.esri.gpt.framework.robots.Access;
import com.esri.gpt.framework.robots.RobotsTxt;
import com.esri.gpt.framework.util.StringBuilderWriter;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Default iteration context.
 */
public class DefaultIterationContext implements IterationContext {
  protected static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  protected final LinkedList<ExceptionInfo> exceptionInfos = new LinkedList<ExceptionInfo>();
  
  protected RobotsTxt robotsTxt;

  /**
   * Creates instance without robots information.
   */
  public DefaultIterationContext() {
  }

  /**
   * Creates instance with robots information.
   * @param robotsTxt robots information
   */
  public DefaultIterationContext(RobotsTxt robotsTxt) {
    this.robotsTxt = robotsTxt;
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
    return new HttpClientRequest();
  }

  @Override
  public void assertAccess(String url) throws AccessException {
    if (robotsTxt!=null) {
      try {
        String relativeUrl = extractRelativeUrl(url);
        Access access = robotsTxt.findAccess(relativeUrl);
        if (access!=null && !access.hasAccess()) {
          throw new AccessException("Access denied by robots.txt", access);
        }
      } catch (AccessException ex) {
        throw ex;
      } catch (IOException ex) {
        throw new AccessException("Unable to determine access", ex);
      }
    }
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
