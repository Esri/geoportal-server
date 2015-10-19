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
import javax.servlet.http.HttpServletResponse;

/**
 * Http client request for crawlers.
 */
public class CrawlHttpClientRequest extends HttpClientRequest {
  private final RobotsTxt robotsTxt;

  public CrawlHttpClientRequest(RobotsTxt robotsTxt) {
    this.robotsTxt = robotsTxt;
  }

  @Override
  public void execute() throws IOException {
    if (robotsTxt!=null) {
      Access access = robotsTxt.findAccess(getRelativePath());
      if (access!=null && !access.hasAccess()) {
        throw new HttpClientException(HttpServletResponse.SC_FORBIDDEN, "Forbidden by robots.txt");
      }
      
      CrawlLocker.getInstance().enterServer(getProtocolHostPort(), robotsTxt.getCrawlDelay());
    }
    
    super.execute();
  }
  
  private String getProtocolHostPort() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("%s://%s%s", u.getProtocol(), u.getHost(), u.getPort()>=0? ":"+u.getPort(): "");
  }
  
  private String getRelativePath() throws MalformedURLException {
    URL u = new URL(getUrl());
    return String.format("/%s%s%s", u.getPath()!=null? u.getPath(): "", u.getQuery()!=null? "?"+u.getQuery():"", u.getRef()!=null? "#"+u.getRef(): "");
  }
}
