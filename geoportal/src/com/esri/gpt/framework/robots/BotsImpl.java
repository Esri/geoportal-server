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
package com.esri.gpt.framework.robots;

import com.esri.gpt.framework.util.StringBuilderWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Robots TXT implementation.
 */
class BotsImpl implements Bots {
  private static final Logger LOG = Logger.getLogger(BotsImpl.class.getName());

  private Section defaultSection;
  private final List<Section> sections = new ArrayList<Section>();
  
  private String host;
  private final List<String> sitemaps = new ArrayList<String>();
  
  private final String userAgent;
  
  /**
   * Creates instance of the RobotsTxt implementation
   * @param userAgent 
   */
  public BotsImpl(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public String getHost() {
    return host;
  }

  /**
   * Sets host.
   * @param host host name
   */
  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public List<String> getSitemaps() {
    return sitemaps;
  }

  @Override
  public Integer getCrawlDelay() {
    Integer crawlDelay = null;
    Section sec = findSectionByAgent(sections, userAgent);
    if (sec!=null) {
      crawlDelay = sec.getCrawlDelay();
    }
    if (defaultSection!=null) {
      crawlDelay = defaultSection.getCrawlDelay();
    }
    LOG.fine(String.format("Crawl-delay: %d", crawlDelay));
    return null;
  }

  /**
   * Adds section.
   *
   * @param section section
   */
  public void addSection(Section section) {
    if (section!=null) {
      if (section.isAnyAgent()) {
        this.defaultSection = section;
      } else {
        sections.add(section);
      }
    }
  }

  @Override
  public Access findAccess(String path) {
    try {
      path = URLDecoder.decode(path, "UTF-8");
      String relativePath = assureRelative(path);
      Access access = relativePath!=null && !"/robots.txt".equalsIgnoreCase(relativePath)? findAccess(userAgent, relativePath): Access.ALLOW;
      LOG.fine(String.format("Access: %s", access));
      return access;
    } catch (IOException ex) {
      return Access.DISALLOW;
    } catch (URISyntaxException ex) {
      return Access.DISALLOW;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(new StringBuilderWriter(sb));
    
    if (defaultSection!=null) {
      writer.println(defaultSection.toString());
    }
      
    for (Section section: sections) {
      writer.println(section.toString());
    }

    if (host!=null) {
      writer.printf("Host: %s", host);
      writer.println();
    }

    for (String sitemap: sitemaps) {
      writer.printf("Sitemap: %s", sitemap);
      writer.println();
    }
    
    // no need to close writer or catch any exception
    
    return sb.toString();
  }

  /**
   * Checks if absolute path has access for this section.
   *
   * @param userAgent user agent
   * @param relativePath absolute path
   * @return access information or <code>null</code> if no access information found
   */
  private Access findAccess(String userAgent, String relativePath) {
    if (!(userAgent==null || relativePath==null)) {
      Section sec = findSectionByAgent(sections, userAgent);
      if (sec!=null) {
        Access access = sec.findAccess(userAgent, relativePath);
        if (access!=null) {
          return access;
        }
      }
      if (defaultSection!=null) {
        Access defaultAccess = defaultSection.findAccess(userAgent, relativePath);
        if (defaultAccess!=null) {
          return defaultAccess;
        }
      }
    }
    return Access.ALLOW;
  }
  
  private Section findSectionByAgent(List<Section> sections, String userAgent) {
    for (Section sec: sections) {
      if (sec.matchUserAgent(userAgent)) {
        return sec;
      }
    }
    return null;
  }
  
  private String assureRelative(String path) throws URISyntaxException, MalformedURLException {
    URI uri = new URI(path);
    if (uri.isAbsolute()) {
      URL url = uri.toURL();
      return String.format("/%s%s%s", url.getPath(), url.getQuery()!=null? "#"+url.getQuery(): "", url.getRef()!=null? "#"+url.getRef(): "").replaceAll("/+", "/");
    }
    return path;
  }
}
