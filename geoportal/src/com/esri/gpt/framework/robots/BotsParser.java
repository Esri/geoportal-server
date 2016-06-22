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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser of "robots.txt" file.
 */
public class BotsParser {

  private static final Logger LOG = Logger.getLogger(BotsParser.class.getName());

  private static final boolean DEFAULT_ENABLED  = true;
  private static final boolean DEFAULT_OVERRIDE = true;
  private static final String  DEFAULT_AGENT    = "GeoportalServer";

  private static final String BOT_ENABLED_PARAM  = "bot.robotstxt.enabled";  // default: DEFAULT_ENABLED
  private static final String BOT_OVERRIDE_PARAM = "bot.robotstxt.override"; // default: DEFAULT_OVERRIDE
  private static final String BOT_AGENT_PARAM    = "bot.agent";              // default: DEFAULT_AGENT

  private final boolean enabled;
  private final boolean override;
  private final String userAgent;

  private static BotsParser defaultInstance;

  /**
   * Gets default instance.
   *
   * @return instance
   */
  public static BotsParser getDefaultInstance() {
    if (defaultInstance == null) {
      ApplicationContext appCtx = ApplicationContext.getInstance();
      ApplicationConfiguration appCfg = appCtx.getConfiguration();

      boolean enabled = Val.chkBool(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_ENABLED_PARAM), DEFAULT_ENABLED);
      boolean override = Val.chkBool(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_OVERRIDE_PARAM), DEFAULT_OVERRIDE);
      String userAgent = Val.chkStr(appCfg.getCatalogConfiguration().getParameters().getValue(BOT_AGENT_PARAM), DEFAULT_AGENT);

      LOG.info(String.format("Creating default RobotsTxtParser :: enabled: %b, override: %b, user-agent: %s", enabled, override, userAgent));

      defaultInstance = new BotsParser(enabled, override, userAgent);
    }
    return defaultInstance;
  }

  /**
   * Creates instance of the parser.
   */
  /*package*/BotsParser() {
    this(DEFAULT_ENABLED, DEFAULT_OVERRIDE, DEFAULT_AGENT);
  }

  /**
   * Creates instance of the parser.
   *
   * @param enabled <code>true</code> if robots.txt should be used
   * @param userAgent user agent
   */
  /*package*/BotsParser(boolean enabled, boolean override, String userAgent) {
    this.enabled = enabled;
    this.override = override;
    this.userAgent = Val.chkStr(userAgent);
  }

  /**
   * Gets user agent.
   *
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
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public Bots readRobotsTxt(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, String serverUrl) {
    if (canParse(mode) && serverUrl != null) {
      try {
        return BotsParser.this.readRobotsTxt(mode, matchingStrategy, winningStrategy, new URL(serverUrl));
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
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public Bots readRobotsTxt(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, URL serverUrl) {
    if (canParse(mode) && serverUrl != null) {
      LOG.log(Level.INFO, String.format("Accessing robots.txt for: %s", serverUrl.toExternalForm()));
      try {
        URL robotsTxtUrl = getRobotsTxtUrl(serverUrl);
        if (robotsTxtUrl != null) {
          RobotsContentHandler handler = new RobotsContentHandler(mode,matchingStrategy, winningStrategy);
          HttpClientRequest request = new HttpClientRequest();
          request.setRequestHeader(Directive.UserAgent.toString(), userAgent);
          request.setUrl(robotsTxtUrl.toExternalForm());
          request.setContentHandler(handler);
          request.execute();
          Bots robots = handler.getRobots();

          if (robots != null) {
            LOG.info(String.format("Received Robotx.txt for: %s", serverUrl.toExternalForm()));
            LOG.fine(String.format("Robotx.txt for: %s\n%s", serverUrl.toExternalForm(), robots.toString()));
          }

          return robots;
        }
      } catch (IOException ex) {
        LOG.log(Level.FINE, String.format("Unable to access robots.txt for: %s", serverUrl.toExternalForm()));
        LOG.log(Level.FINEST,"",ex);
      }
    }
    return null;
  }

  /**
   * Parses robots TXT
   *
   * @param mode robots.txt mode
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param robotsTxt stream of data
   * @return instance or RobotsTxt or <code>null</code>
   */
  public Bots readRobotsTxt(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, InputStream robotsTxt) {
    Bots robots = null;
    BotsReader reader = null;

    try {
      if (canParse(mode)) {
        reader = new BotsReader(userAgent, matchingStrategy, winningStrategy, robotsTxt);
        robots = reader.readRobotsTxt();
      }
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

    return robots;
  }

  private boolean canParse(BotsMode mode) {
    mode = mode != null ? mode : BotsMode.getDefault();

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
   * Custom content handler for HTTP request.
   */
  private class RobotsContentHandler extends ContentHandler {

    private final BotsMode mode;
    private final MatchingStrategy matchingStrategy;
    private final WinningStrategy winningStrategy;
    private Bots robots;

    public RobotsContentHandler(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy) {
      this.mode = mode;
      this.matchingStrategy = matchingStrategy;
      this.winningStrategy = winningStrategy;
    }

    @Override
    public void readResponse(HttpClientRequest request, InputStream responseStream) throws IOException {
      try {
        robots = BotsParser.this.readRobotsTxt(mode, matchingStrategy, winningStrategy, responseStream);
      } finally {
        try {
          responseStream.close();
        } catch (IOException ex) {
        }
      }
    }

    public Bots getRobots() {
      return robots;
    }
  }
}
