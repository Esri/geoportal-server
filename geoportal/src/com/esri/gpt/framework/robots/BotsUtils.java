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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Robots txt utility class/shortcut methods
 */
public final class BotsUtils {
  private static final Logger LOG = Logger.getLogger(BotsUtils.class.getName());
  private static final MatchingStrategy DEFAULT_MATCHING_STRATEGY = MatchingStrategy.SIMPLE_PATTERN_STRATEGY;
  private static final WinningStrategy DEFAULT_WINNIG_STRATEGY = WinningStrategy.LONGEST_PATH_STRATEGY;
  
  /**
   * Gets default parser
   * @return default parser (never <code>null</code>)
   */
  public static BotsParser parser() {
    return BotsParser.getDefaultInstance();
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, String serverUrl) {
    return parser().readRobotsTxt(mode, matchingStrategy, winningStrategy, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param matchingStrategy matching strategy
   * @param winningStrategy winning strategy
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, URL serverUrl) {
    return parser().readRobotsTxt(mode, matchingStrategy, winningStrategy, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, String serverUrl) {
    return parser().readRobotsTxt(mode, DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }
  
  /**
   * Reads robots.txt
   * @param mode robots.txt mode
   * @param serverUrl url of the server which is expected to have robots.txt
   * present
   * @return instance of {@link Bots} or <code>null</code> if unable to
   * obtain robots.txt
   */
  public static Bots readBots(BotsMode mode, URL serverUrl) {
    return parser().readRobotsTxt(mode, DEFAULT_MATCHING_STRATEGY, DEFAULT_WINNIG_STRATEGY, serverUrl);
  }
  
  /**
   * Request access to the resource.
   * @param bots robots
   * @param path relative path to the resource
   * @return access (never <code>null</code>
   */
  public static Access requestAccess(Bots bots, String path) {
    if (bots!=null) {
      List<Access> matching = bots.select(path, bots.getMatchingStrategy());
      Access winner = bots.getWinningStrategy().selectWinner(matching);
      if (winner!=null) {
        return winner;
      }
    }
    return Access.ALLOW;
  }
  
  /**
   * Replaces host part in given url if "host" directive found in robots.txt
   * @param bots robots.txt
   * @param url url to transform
   * @return transformed url
   */
  public static String transformUrl(Bots bots, String url) {
    if (bots!=null) {
      String orgUrl = url;
      ProtocolHostPort php = parseHostAttribute(bots.getHost());
      url = updateUrl(url, php);
      if (!url.equals(orgUrl)) {
        LOG.fine(String.format("Url updated from %s to %s", orgUrl, url));
      }
    }
    return url;
  }
  
  /**
   * Compiles wildcard pattern into a regular expression.
   * <p>
   * Allowed wildcards:<br>
   * <br>
   * &nbsp;&nbsp;&nbsp;* - matches any sequence of characters<br>
   * &nbsp;&nbsp;&nbsp;$ - matches end of sequence<br>
   * @param patternWithWildcards pattern with wildcards
   * @return compiled pattern
   */
  public static Pattern compileWildcardPattern(String patternWithWildcards) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<patternWithWildcards.length(); i++) {
      char c = patternWithWildcards.charAt(i);
      switch (c) {
        case '*':
          sb.append(".*");
          break;
        case '$':
          if (i==patternWithWildcards.length()-1) {
            sb.append(c);
          } else {
            sb.append("[").append(c).append("]");
          }
          break;
        case '[':
        case ']':
          sb.append("[").append("\\").append(c).append("]");
          break;
        default:
          sb.append("[").append(c).append("]");
      }
    }
    return Pattern.compile(sb.toString());
  }
  
  /**
   * Decodes URL octets except %2f (i.e. / character)
   * @param str string to encode
   * @return encoded string
   */
  /*package*/ static String decode(String str) throws UnsupportedEncodingException {
    if (str!=null) {
      StringBuilder sb = new StringBuilder();
      for (int idx = str.toLowerCase().indexOf("%2f"); idx>=0; idx = str.toLowerCase().indexOf("%2f")) {
        sb.append(URLDecoder.decode(str.substring(0, idx),"UTF-8")).append(str.substring(idx, idx+3));
        str = str.substring(idx+3);
      }
      sb.append(URLDecoder.decode(str,"UTF-8"));
      str = sb.toString();
    }
    return str;
  }
  
  private static String updateUrl(String url, ProtocolHostPort php) {
    if (url!=null && php!=null) {
      try {
        URL u = new URL(url);
        url = new URL(
                // update protocol if requested protocol not null
                php.protocol!=null? php.protocol: u.getProtocol(),
                // update host if requested host not null
                php.host!=null? php.host: u.getHost(),
                // update port ONLY if requested HOST not null
                php.host!=null? php.port!=null? php.port: -1: u.getPort(),
                u.getPath()
        ).toExternalForm();
      } catch (MalformedURLException ex) {

      }
    }
    return url;
  }
  
  private static ProtocolHostPort parseHostAttribute(String host) {
    host = Val.chkStr(host);
    if (!host.isEmpty()) {
      // parse protocol
      String protocolPart = null;
      int protocolStopIdx = host.indexOf("://");
      if (protocolStopIdx>=0) {
        protocolPart = protocolStopIdx>0? host.substring(0,protocolStopIdx): null;
        host = host.substring(protocolStopIdx+"://".length());
      }
      
      // parse host:port
      String hostPart = null;
      Integer portPart = null;
      if (!host.isEmpty()) {
        int hostStopIdx = host.indexOf(":");
        if (hostStopIdx<0) {
          hostPart = host;
        } else {
          hostPart = hostStopIdx>0? host.substring(0, hostStopIdx): null;
          try {
            portPart = Integer.parseInt(host.substring(hostStopIdx+":".length()));
          } catch (NumberFormatException ex) {
            
          }
        }
      }
      
      if (protocolPart!=null || hostPart!=null || portPart!=null) {
        return new ProtocolHostPort(protocolPart, hostPart, portPart);
      }
    }
    return null;
  }
  
  private static class ProtocolHostPort {
    String  protocol;
    String  host;
    Integer port;

    public ProtocolHostPort(String protocol, String host, Integer port) {
      this.protocol = protocol;
      this.host = host;
      this.port = port;
    }
  }
  
}
