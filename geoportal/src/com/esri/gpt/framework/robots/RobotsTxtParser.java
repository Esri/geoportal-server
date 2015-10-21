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

import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.http.ContentHandler;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robots TXT parser.
 */
public class RobotsTxtParser {

  private static final Logger LOG = Logger.getLogger(RobotsTxtParser.class.getName());

  private static final boolean DEFAULT_ENABLED = true;
  private static final boolean DEFAULT_OVERRIDE = true;
  private static final String DEFAULT_AGENT = "geoportalbot";

  private static final String BOT_ENABLED_PARAM = "bot.robotstxt.enabled";   // default: DEFAULT_ENABLED
  private static final String BOT_OVERRIDE_PARAM = "bot.robotstxt.override"; // default: DEFAULT_OVERRIDE
  private static final String BOT_AGENT_PARAM = "bot.agent";               // default: DEFAULT_AGENT

  private final boolean enabled;
  private final boolean override;
  private final String userAgent;

  private static RobotsTxtParser defaultInstance;

  /**
   * Gets default instance.
   *
   * @return instance
   */
  public static RobotsTxtParser getDefaultInstance() {
    if (defaultInstance == null) {
      ApplicationContext appCtx = ApplicationContext.getInstance();
      ApplicationConfiguration appCfg = appCtx.getConfiguration();

      boolean enabled = Val.chkBool(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_ENABLED_PARAM), DEFAULT_ENABLED);
      boolean override = Val.chkBool(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_OVERRIDE_PARAM), DEFAULT_OVERRIDE);
      String userAgent = Val.chkStr(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_AGENT_PARAM), DEFAULT_AGENT);

      LOG.info(String.format("Creating default RobotsTxtParser :: enabled: %b, override: %b, user-agend: %s", enabled, override, userAgent));

      defaultInstance = new RobotsTxtParser(enabled, override, userAgent);
    }
    return defaultInstance;
  }

  /**
   * Creates instance of the parser.
   */
  private RobotsTxtParser() {
    this(DEFAULT_ENABLED, DEFAULT_OVERRIDE, DEFAULT_AGENT);
  }

  /**
   * Creates instance of the parser.
   *
   * @param enabled <code>true</code> if robots.txt should be used
   * @param userAgent user agent
   */
  private RobotsTxtParser(boolean enabled, boolean override, String userAgent) {
    this.enabled = enabled;
    this.override = override;
    this.userAgent = Val.chkStr(userAgent);
  }

  /**
   * Gets user agent.
   * @return user agent
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Checks if using robots.txt is enabled.
   *
   * @return <code>true</code> if using robots.txt is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Checks if robots.txt enabled flag can be overridden.
   *
   * @return <code>true</code> if robots.txt enabled flag can be overridden
   */
  public boolean canOverride() {
    return override;
  }

  /**
   * Parses context of the Robots.txt file if available.
   *
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link RobotsTxt} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public RobotsTxt parseRobotsTxt(RobotsTxtMode mode, String serverUrl) {
    if (canParse(mode) && serverUrl != null) {
      try {
        return parseRobotsTxt(mode, new URL(serverUrl));
      } catch (MalformedURLException ex) {
        LOG.log(Level.WARNING, String.format("Invalid server url: %s", serverUrl), ex);
      }
    }
    return null;
  }

  /**
   * Parses context of the Robots.txt file if available.
   *
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link RobotsTxt} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public RobotsTxt parseRobotsTxt(RobotsTxtMode mode, URL serverUrl) {
    if (canParse(mode) && serverUrl != null) {
      LOG.log(Level.INFO, String.format("Accessing robots.txt for: %s", serverUrl.toExternalForm()));
      try {
        URL robotsTxtUrl = getRobotsTxtUrl(serverUrl);
        if (robotsTxtUrl != null) {
          RobotsContentHandler handler = new RobotsContentHandler(mode);
          HttpClientRequest request = new HttpClientRequest();
          request.setRequestHeader("User-Agent", userAgent);
          request.setUrl(robotsTxtUrl.toExternalForm());
          request.setContentHandler(handler);
          request.execute();
          RobotsTxt robots = handler.getRobots();

          if (robots != null) {
            LOG.log(Level.INFO, String.format("Robotx.txt for: %s\n%s", serverUrl.toExternalForm(), robots.toString()));
          }

          return robots;
        }
      } catch (IOException ex) {
        LOG.log(Level.WARNING, String.format("Unable to access robots.txt for: %s", serverUrl.toExternalForm()), ex);
      }
    }
    return null;
  }

  /**
   * Parses robots TXT
   *
   * @param mode robots.txt mode
   * @param robotsTxt stream of data
   * @return instance or RobotsTxt or <code>null</code>
   */
  public RobotsTxt parseRobotsTxt(RobotsTxtMode mode, InputStream robotsTxt) {
    BufferedReader reader = null;

    try {
      RobotsTxtImpl robots = null;
      
      if (canParse(mode)) {

        reader = new BufferedReader(new InputStreamReader(robotsTxt, "UTF-8"));
        Section currentSection = null;

        boolean startSection = false;

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          String[] kvp = parseLineToKVP(line);
          if (kvp == null) {
            continue;
          }

          // --------- User-agent ------------------------------------------------
          if (kvp[0].equalsIgnoreCase("User-agent")) {
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

            currentSection.addUserAgent(kvp[1]);
            startSection = true;

            // --------- Disallow --------------------------------------------------
          } else if (currentSection != null && kvp[0].equalsIgnoreCase("Disallow")) {
            startSection = false;
            currentSection.addAccess(new AccessImpl(new AccessPath(kvp[1]), false));

            // --------- Allow -----------------------------------------------------
          } else if (currentSection != null && kvp[0].equalsIgnoreCase("Allow")) {
            startSection = false;
            currentSection.addAccess(new AccessImpl(new AccessPath(kvp[1]), true));

            // --------- Crawl-delay -----------------------------------------------
          } else if (kvp[0].equalsIgnoreCase("Crawl-delay")) {
            startSection = false;
            if (currentSection != null) {
              try {
                int crawlDelay = Integer.parseInt(kvp[1]);
                currentSection.setCrawlDelay(crawlDelay);
              } catch (NumberFormatException ex) {
              }
            }

            // --------- Sitemap ---------------------------------------------------
          } else if (kvp[0].equalsIgnoreCase("Sitemap")) {
            if (robots == null) {
              robots = newRobots();
            }
            robots.getSitemaps().add(kvp[1]);

            // --------- Host ------------------------------------------------------
          } else if (kvp[0].equalsIgnoreCase("Host")) {
            if (robots == null) {
              robots = newRobots();
            }
            robots.setHost(kvp[1]);
          }

          if (currentSection != null) {
            if (robots == null) {
              robots = newRobots();
            }
            robots.addSection(currentSection);
          }
        }
      }

      return robots;
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "Unable to parse robots.txt", ex);
      return null;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  private boolean canParse(RobotsTxtMode mode) {
    mode = mode != null ? mode : RobotsTxtMode.getDefault();

    switch (mode) {
      case inherit:
        return isEnabled();
      case always:
        return true;
      case never:
        return false;
    }

    return DEFAULT_ENABLED;
  }

  private String[] parseLineToKVP(String line) throws UnsupportedEncodingException {
    line = Val.chkStr(line);
    if (line.startsWith("#")) {
      return null;
    }
    int colonIndex = line.indexOf(":");
    if (colonIndex < 0) {
      return null;
    }

    String key = Val.chkStr(line.substring(0, colonIndex));

    String rest = line.substring(colonIndex + 1, line.length());
    int hashIndex = rest.indexOf("#");

    String value = URLDecoder.decode(Val.chkStr(hashIndex >= 0 ? rest.substring(0, hashIndex) : rest), "UTF-8");

    return new String[]{key, value};
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

  private RobotsTxtImpl newRobots() {
    return new RobotsTxtImpl(userAgent);
  }

  private class RobotsContentHandler extends ContentHandler {

    private final RobotsTxtMode mode;
    private RobotsTxt robots;

    public RobotsContentHandler(RobotsTxtMode mode) {
      this.mode = mode;
    }

    @Override
    public void readResponse(HttpClientRequest request, InputStream responseStream) throws IOException {
      robots = parseRobotsTxt(mode, responseStream);
    }

    public RobotsTxt getRobots() {
      return robots;
    }
  }
}
