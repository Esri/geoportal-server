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
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 * Http client request for crawlers.
 */
public class CrawlHttpClientRequest extends HttpClientRequest {

  private static final Logger LOG = Logger.getLogger(CrawlHttpClientRequest.class.getName());
  private final RobotsTxt robotsTxt;

  public CrawlHttpClientRequest(RobotsTxt robotsTxt) {
    this.robotsTxt = robotsTxt;
  }

  @Override
  public void execute() throws IOException {
    if (robotsTxt != null) {
      Access access = robotsTxt.findAccess(getRelativePath());
      if (access != null && !access.hasAccess()) {
        throw new HttpClientException(HttpServletResponse.SC_FORBIDDEN, "Forbidden by robots.txt");
      }

      CrawlLocker.getInstance().enterServer(getProtocolHostPort(), robotsTxt.getCrawlDelay());
    }

    super.execute();
  }

  @Override
  public String getUrl() {
    String url = super.getUrl();
    if (robotsTxt != null) {
      String host = Val.chkStr(robotsTxt.getHost());
      if (!host.isEmpty()) {
        String[] h = host.split(":");
        if (h.length > 0) {
          try {
            URI u = new URL(url).toURI();
            url = new URI(
                    u.getScheme(),
                    u.getUserInfo(),
                    h[0],
                    h.length > 1 ? Integer.parseInt(h[1]) : u.getPort(),
                    u.getPath(),
                    u.getQuery(),
                    u.getFragment()
            ).toURL().toExternalForm();
          } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Invalid url", ex);
          }
        }
      }
    }
    return url;
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
