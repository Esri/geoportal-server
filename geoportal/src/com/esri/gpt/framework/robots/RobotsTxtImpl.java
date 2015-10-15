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
  private String sitemap;
  private int crawlDelay ;
  
  private final String userAgent;
  
  public RobotsTxtImpl(String userAgent) {
    this.userAgent = userAgent;
  }

  @Override
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
  public String getSitemap() {
    return sitemap;
  }

  public void setSitemap(String sitemap) {
    this.sitemap = sitemap;
  }

  @Override
  public int getCrawlDelay() {
    return crawlDelay;
  }

  void setCrawlDelay(int crawlDelay) {
    this.crawlDelay = crawlDelay;
  }

  /**
   * Adds section.
   *
   * @param section section
   */
  void addSection(Section section) {
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

  /**
   * Checks if absolute path has access for this section.
   *
   * @param userAgent user agent
   * @param relativePath absolute path
   * @return <code>true</code> if has access
   */
  boolean hasAccess(String userAgent, String relativePath) {
    for (Section sec : sections) {
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
  
  @Override
  public String toString() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
      
      if (defaultSection!=null) {
        writer.println(defaultSection.toString());
      }
      
      for (Section section: sections) {
        writer.println(section.toString());
      }
      
      if (crawlDelay>0) {
        writer.printf("Crawl-delay: %d", crawlDelay);
        writer.println();
      }
      
      if (host!=null) {
        writer.printf("Host: %s", host);
        writer.println();
      }
      
      if (sitemap!=null) {
        writer.printf("Sitemap: %s", sitemap);
        writer.println();
      }
      
      writer.close();
      
      return out.toString("UTF-8");
    } catch (IOException ex) {
      return "";
    }
  }
}
