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
package com.esri.gpt.framework.http.crawl;

import com.esri.gpt.framework.http.HttpClientException;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.robots.Access;
import com.esri.gpt.framework.robots.RobotsTxt;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 * Http crawl request.
 */
public class HttpCrawlRequest extends HttpClientRequest {

  private static final Logger LOG = Logger.getLogger(HttpCrawlRequest.class.getName());
  private final RobotsTxt robotsTxt;

  /**
   * Creates instance of the request.
   * @param robotsTxt robots.txt or <code>null</code> if robots not available
   */
  public HttpCrawlRequest(RobotsTxt robotsTxt) {
    this.robotsTxt = robotsTxt;
  }

  @Override
  public void execute() throws IOException {
    this.adviseRobotsTxt();
    super.execute();
  }

  @Override
  public String getUrl() {
    String url = super.getUrl();
    // if robots.txt available then "host" attribute if available to update url
    if (robotsTxt != null) {
      url = robotsTxt.applyHostAttribute(url);
    }
    return url;
  }

  private void adviseRobotsTxt() throws IOException {
    if (robotsTxt != null) {
      String url = getRelativePath();
      LOG.fine(String.format("Evaluating access to %s using robots.txt", url));
      Access access = robotsTxt.findAccess(url);
      if (!access.hasAccess()) {
        LOG.info(String.format("Access to %s disallowed by robots.txt", url));
        throw new HttpClientException(HttpServletResponse.SC_FORBIDDEN, String.format("Access to %s disallowed by robots.txt", url));
      }
      LOG.fine(String.format("Access to %s allowed by robots.txt", url));
      CrawlLocker.getInstance().enterServer(getProtocolHostPort(), robotsTxt.getCrawlDelay());
    }
  }
  
  private String getProtocolHostPort() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("%s://%s%s", u.getProtocol(), u.getHost(), u.getPort() >= 0 ? ":" + u.getPort() : "");
  }

  private String getRelativePath() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("/%s%s%s", u.getPath() != null ? u.getPath() : "", u.getQuery() != null ? "?" + u.getQuery() : "", u.getRef() != null ? "#" + u.getRef() : "");
  }
}
