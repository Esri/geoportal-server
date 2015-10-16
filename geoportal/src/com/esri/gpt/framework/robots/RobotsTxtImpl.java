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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Robots TXT implementation.
 */
class RobotsTxtImpl implements RobotsTxt {

  private Section defaultSection;
  private final List<Section> sections = new ArrayList<Section>();
  
  private String host;
  private final List<String> sitemaps = new ArrayList<String>();
  
  private final String userAgent;
  
  /**
   * Creates instance of the RobotsTxt implementation
   * @param userAgent 
   */
  public RobotsTxtImpl(String userAgent) {
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
    Section sec = findSectionByAgent(sections, userAgent);
    if (sec!=null) {
      Integer crawlDelay = sec.getCrawlDelay();
      if (crawlDelay!=null) {
        return crawlDelay;
      }
    }
    if (defaultSection!=null) {
      Integer crawlDelay = defaultSection.getCrawlDelay();
      if (crawlDelay!=null) {
        return crawlDelay;
      }
    }
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

  /**
   * Checks if absolute path has access for this section.
   *
   * @param relativePath absolute path
   * @return <code>true</code> if has access
   */
  @Override
  public boolean hasAccess(String relativePath) {
    return hasAccess(userAgent, relativePath);
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
   * @return <code>true</code> if has access
   */
  private boolean hasAccess(String userAgent, String relativePath) {
    Section sec = findSectionByAgent(sections, userAgent);
    if (sec!=null) {
      Access access = sec.findAccess(userAgent, relativePath);
      if (access!=null) {
        return access.hasAccess();
      }
    }
    if (defaultSection!=null) {
      Access defaultAccess = defaultSection.findAccess(userAgent, relativePath);
      if (defaultAccess!=null) {
        return defaultAccess.hasAccess();
      }
    }
    return true;
  }
  
  private Section findSectionByAgent(List<Section> sections, String userAgent) {
    for (Section sec: sections) {
      if (sec.matchUserAgent(userAgent)) {
        return sec;
      }
    }
    return null;
  }
}
