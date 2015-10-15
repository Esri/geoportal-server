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

import com.esri.gpt.framework.http.ContentHandler;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robots TXT parser.
 */
public class RobotsTxtParser {

  private static final Logger LOG = Logger.getLogger(RobotsTxtParser.class.getName());

  private static final String DEFAULT_USER_AGENT = "esri-geoportal";

  private final String userAgent;

  public static void main(String[] args) throws Exception {
    RobotsTxt robotsTxt = new RobotsTxtParser().parseRobotsTxt("http://www.data.gov");
    if (robotsTxt!=null) {
      System.out.println(robotsTxt.toString());
    }
    System.out.println("Ready");
  }
  
  /**
   * Creates instance of the parser.
   */
  public RobotsTxtParser() {
    this(DEFAULT_USER_AGENT);
  }

  /**
   * Creates instance of the parser.
   *
   * @param userAgent user agent
   */
  public RobotsTxtParser(String userAgent) {
    this.userAgent = userAgent;
  }

  public RobotsTxt parseRobotsTxt(String serverUrl) {
    if (serverUrl!=null) {
      try {
        return parseRobotsTxt(new URL(serverUrl));
      } catch (MalformedURLException ex) {
        LOG.log(Level.WARNING, "Invalid server url.", ex);
      }
    }
    return null;
  }
  
  public RobotsTxt parseRobotsTxt(URL serverUrl) {
    if (serverUrl != null) {
      try {
        URL robotsTxtUrl = getRobotsTxtUrl(serverUrl);
        if (robotsTxtUrl != null) {
          RobotsContentHandler handler = new RobotsContentHandler();
          HttpClientRequest request = new HttpClientRequest();
          request.setUrl(robotsTxtUrl.toExternalForm());
          request.setContentHandler(handler);
          request.execute();
          return handler.getRobots();
        }
      } catch (IOException ex) {
        LOG.log(Level.WARNING, "Unable to access robots.txt", ex);
      }
    }
    return null;
  }

  private URL getRobotsTxtUrl(URL baseUrl) {
    try {
      if (baseUrl != null) {
        if (baseUrl.getPort() >= 0) {
          return new URL(String.format("%s://%s:%d/robots.txt", baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort()));
        } else {
          return new URL(String.format("%s://%s/robots.txt", baseUrl.getProtocol(), baseUrl.getHost()));
        }
      }
    } catch (MalformedURLException ex) {
      LOG.log(Level.WARNING, "Invalid robots.txt url.", ex);
    }
    return null;
  }

  /**
   * Parses robots TXT
   *
   * @param robotsTxt stream of data
   * @return instance or RobotsTxt or <code>null</code>
   */
  public RobotsTxt parseRobotsTxt(InputStream robotsTxt) {
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new InputStreamReader(robotsTxt, "UTF-8"));
      RobotsTxtImpl robots = null;
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
              robots = newRobots();
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
            robots = newRobots();
          }
          robots.setCrawlDelay(Val.chkInt(value, 0));
        } else if (key.equalsIgnoreCase("Sitemap")) {
          if (robots == null) {
            robots = newRobots();
          }
          robots.setSitemap(value);
        } else if (key.equalsIgnoreCase("Host")) {
          if (robots == null) {
            robots = newRobots();
          }
          robots.setHost(value);
        }
      }

      if (currentSection != null) {
        if (robots == null) {
          robots = newRobots();
        }
        robots.addSection(currentSection);
      }
      return robots;
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Unable to parse robots.txt", ex);
      return null;
    } finally {
      if (reader!=null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  private RobotsTxtImpl newRobots() {
    return new RobotsTxtImpl(userAgent);
  }
  
  private class RobotsContentHandler extends ContentHandler {
    private RobotsTxt robots;

    @Override
    public void readResponse(HttpClientRequest request, InputStream responseStream) throws IOException {
      robots = parseRobotsTxt(responseStream);
    }

    public RobotsTxt getRobots() {
      return robots;
    }
    
  }
}
