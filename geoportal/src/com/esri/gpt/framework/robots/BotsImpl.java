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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Robots TXT implementation.
 */
/*package*/class BotsImpl implements Bots {

  private static final Logger LOG = Logger.getLogger(BotsImpl.class.getName());

  private Section defaultSection;
  private final List<Section> sections = new ArrayList<Section>();

  private Integer crawlDelay;
  private String host;
  private final List<String> sitemaps = new ArrayList<String>();

  private final String userAgent;
  private final MatchingStrategy matchingStrategy;
  private final WinningStrategy winningStrategy;

  /**
   * Creates instance of the RobotsTxt implementation
   *
   * @param userAgent user agent
   * @param winningStrategy winning strategy
   */
  public BotsImpl(String userAgent, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy) {
    this.userAgent = userAgent;
    this.matchingStrategy = matchingStrategy;
    this.winningStrategy = winningStrategy;
  }

  @Override
  public String getUserAgent() {
    return userAgent;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public MatchingStrategy getMatchingStrategy() {
    return matchingStrategy;
  }

  @Override
  public WinningStrategy getWinningStrategy() {
    return winningStrategy;
  }

  /**
   * Sets host.
   *
   * @param host host name
   */
  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public List<String> getSitemaps() {
    return sitemaps;
  }

  /**
   * Sets crawl delay.
   * @param crawlDelay crawl delay. 
   */
  public void setCrawlDelay(Integer crawlDelay) {
    this.crawlDelay = crawlDelay;
  }

  @Override
  public Integer getCrawlDelay() {
    return crawlDelay;
  }

  /**
   * Adds section.
   *
   * @param section section
   */
  public void addSection(Section section) {
    if (section != null) {
      if (section.isAnyAgent()) {
        if (this.defaultSection==null) {
          this.defaultSection = section;
        } else {
          this.defaultSection.getAccessList().importAccess(section.getAccessList());
        }
      } else {
        Section exact = findExactSection(section);
        if (exact==null) {
          sections.add(section);
        } else {
          exact.getAccessList().importAccess(section.getAccessList());
        }
      }
    }
  }

  @Override
  public List<Access> select(String path, MatchingStrategy matchingStrategy) {
    String relativePath = assureRelative(path);

    if (relativePath != null && !"/robots.txt".equalsIgnoreCase(relativePath)) {
      return select(userAgent, relativePath, matchingStrategy);
    } else {
      return Collections.EMPTY_LIST;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(new StringBuilderWriter(sb));

    if (defaultSection != null) {
      writer.println(defaultSection.toString());
    }

    for (Section section : sections) {
      writer.println(section.toString());
    }
    
    if (crawlDelay!=null && crawlDelay>0) {
      writer.printf("Crawl-delay: %d", crawlDelay);
      writer.println();
    }

    if (host != null) {
      writer.printf("Host: %s", host);
      writer.println();
    }

    for (String sitemap : sitemaps) {
      writer.printf("Sitemap: %s", sitemap);
      writer.println();
    }

    // no need to close writer or catch any exception
    return sb.toString();
  }
  
  /**
   * Finds exact section.
   * @param section section to find exact
   * @return exact section or {@code null} if no exact section found
   */
  private Section findExactSection(Section section) {
    for (Section s: sections) {
      if (s.isExact(section)) {
        return s;
      }
    }
    return null;
  }

  private List<Access> select(String userAgent, String relativePath, MatchingStrategy matchingStrategy) {
    ArrayList<Access> selected = new ArrayList<Access>();
    if (!(userAgent == null || relativePath == null)) {
      Section sec = findSectionByAgent(sections, userAgent);
      if (sec != null) {
        selected.addAll(sec.select(userAgent, relativePath, matchingStrategy));
      }
      if (defaultSection != null) {
        selected.addAll(defaultSection.select(userAgent, relativePath, matchingStrategy));
      }
    }
    return selected;
  }

  private Section findSectionByAgent(List<Section> sections, String userAgent) {
    for (Section sec : sections) {
      if (sec.matchUserAgent(userAgent)) {
        return sec;
      }
    }
    return null;
  }

  private String assureRelative(String path) {
    try {
      URI uri = new URI(path);
      if (uri.isAbsolute()) {
        URL url = uri.toURL();
        path = String.format("/%s%s%s", url.getPath(), url.getQuery() != null ? "#" + url.getQuery() : "", url.getRef() != null ? "#" + url.getRef() : "").replaceAll("/+", "/");
      }
      return path;
    } catch (Exception ex) {
      return path;
    }
  }
}
