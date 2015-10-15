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

import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Robots configuration.
 */
public class Robots {

  private static final String DEFAULT_USER_AGENT = "esri-geoportal";
  private Section defaultSection;
  private final List<Section> sections = new ArrayList<Section>();
  private final List<String> sitemaps = new ArrayList<String>();
  private final List<String> hosts = new ArrayList<String>();
  private int crawlDelay ;

  public List<String> getSitemaps() {
    return sitemaps;
  }

  public List<String> getHosts() {
    return hosts;
  }

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
  public boolean hasAccess(String relativePath) {
    return hasAccess(DEFAULT_USER_AGENT, relativePath);
  }

  /**
   * Checks if absolute path has access for this section.
   *
   * @param userAgent user agent
   * @param relativePath absolute path
   * @return <code>true</code> if has access
   */
  public boolean hasAccess(String userAgent, String relativePath) {
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

  public static Robots parseRobotsTxt(InputStream robotsTxt) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(robotsTxt, "UTF-8"));

    try {
      Robots robots = null;
      Section currentSection = null;

      boolean startSection = false;

      for (String line = reader.readLine(); line != null; line = reader.readLine()) {
        line = Val.chkStr(line);
        if (line.startsWith("#")) {
          continue;
        }
        int colonIndex = line.indexOf(":");
        if (colonIndex < 0) {
          continue;
        }

        String key = Val.chkStr(line.substring(0, colonIndex));
        String rest = line.substring(colonIndex + 1, line.length());

        int hashIndex = rest.indexOf("#");
        String value = Val.chkStr(hashIndex >= 0 ? rest.substring(0, hashIndex) : rest);

        if (key.equalsIgnoreCase("User-agent")) {
          if (!startSection && currentSection != null) {
            if (robots == null) {
              robots = new Robots();
            }
            robots.addSection(currentSection);
            currentSection = null;
          }

          if (currentSection == null) {
            currentSection = new Section();
          }

          currentSection.addUserAgent(value);
          startSection = true;
        } else if (currentSection != null && key.equalsIgnoreCase("Disallow")) {
          startSection = false;
          currentSection.addAccess(new Access(new Path(value), false));
        } else if (currentSection != null && key.equalsIgnoreCase("Allow")) {
          startSection = false;
          currentSection.addAccess(new Access(new Path(value), true));
        } else if (key.equalsIgnoreCase("Crawl-delay")) {
            if (robots == null) {
              robots = new Robots();
            }
            robots.setCrawlDelay(Val.chkInt(value, 0));
        } else if (key.equalsIgnoreCase("Sitemap")) {
            if (robots == null) {
              robots = new Robots();
            }
            robots.getSitemaps().add(value);
        } else if (key.equalsIgnoreCase("Host")) {
            if (robots == null) {
              robots = new Robots();
            }
            robots.getHosts().add(value);
        }
      }

      if (currentSection != null) {
        if (robots == null) {
          robots = new Robots();
        }
        robots.addSection(currentSection);
      }
      return robots;
    } finally {
      try {
        reader.close();
      } catch (IOException ex) {}
    }
  }
}
