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
import com.esri.gpt.framework.robots.Bots;
import static com.esri.gpt.framework.robots.BotsUtils.parser;
import static com.esri.gpt.framework.robots.BotsUtils.requestAccess;
import static com.esri.gpt.framework.robots.BotsUtils.transformUrl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpMethodBase;

/**
 * Http crawl request.
 */
public class HttpCrawlRequest extends HttpClientRequest {

  private static final Logger LOG = Logger.getLogger(HttpCrawlRequest.class.getName());
  private final Bots bots;

  /**
   * Creates instance of the request.
   * @param robotsTxt robots.txt or <code>null</code> if robots not available
   */
  public HttpCrawlRequest(Bots robotsTxt) {
    this.bots = robotsTxt;
  }

  @Override
  public void execute() throws IOException {
    this.adviseRobotsTxt();
    super.execute();
  }

  @Override
  protected HttpMethodBase createMethod() throws IOException {
    HttpMethodBase method = super.createMethod();
    if (bots!=null && !parser().getUserAgent().isEmpty()) {
      method.setRequestHeader("User-Agent", parser().getUserAgent());
    }
    return method;
  }

  @Override
  public String getUrl() {
    String url = super.getUrl();
    // if robots.txt available then "host" attribute if available to update url
    return transformUrl(bots,url);
  }

  private void adviseRobotsTxt() throws IOException {
    if (bots != null) {
      String url = getRelativePath();
      LOG.fine(String.format("Evaluating access to %s using robots.txt", getUrl()));
      Access access = requestAccess(bots, url);
      if (!access.hasAccess()) {
        LOG.info(String.format("Access to %s disallowed by robots.txt", getUrl()));
        throw new HttpClientException(HttpServletResponse.SC_FORBIDDEN, String.format("Access to %s disallowed by robots.txt", getUrl()));
      }
      LOG.fine(String.format("Access to %s allowed by robots.txt", getUrl()));
      CrawlLocker.getInstance().enterServer(getProtocolHostPort(), bots.getCrawlDelay());
    }
  }
  
  private String getProtocolHostPort() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("%s://%s%s", u.getProtocol(), u.getHost(), u.getPort() >= 0 ? ":" + u.getPort() : "");
  }

  private String getRelativePath() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("%s%s%s", u.getPath() != null ? u.getPath() : "/", u.getQuery() != null ? "?" + u.getQuery() : "", u.getRef() != null ? "#" + u.getRef() : "");
  }
}
