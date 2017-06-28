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

import static com.esri.gpt.framework.robots.BotsUtils.decode;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Reader of the robots.txt file.
 */
/*package*/class BotsReader extends Reader {

  private final String userAgent;
  private final MatchingStrategy matchingStrategy;
  private final WinningStrategy winningStrategy;
  private final BufferedReader reader;
  private BotsImpl robots;

  public BotsReader(String userAgent, MatchingStrategy matchingStrategy, WinningStrategy winningStrategy, InputStream inputStream) throws UnsupportedEncodingException {
    this.userAgent = Val.chkStr(userAgent);
    this.matchingStrategy = matchingStrategy;
    this.winningStrategy = winningStrategy;
    this.reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return reader.read(cbuf, off, len);
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  public Bots readRobotsTxt() throws IOException {
    Section currentSection = null;
    boolean startSection = false;

    for (Map.Entry<Directive, String> entry = readEntry(); entry != null; entry = readEntry()) {
      switch (entry.getKey()) {
        case UserAgent:
          if (!startSection && currentSection != null) {
            addSection(currentSection);
            currentSection = null;
          }

          if (currentSection == null) {
            currentSection = new Section();
          }

          currentSection.addUserAgent(entry.getValue());
          startSection = true;
          break;

        case Disallow:
          if (currentSection != null) {
            boolean access = !entry.getValue().isEmpty()? false: true;
            currentSection.addAccess(new AccessImpl(new AccessPath(entry.getValue()), access));
            startSection = false;
          }
          break;

        case Allow:
          if (currentSection != null) {
            boolean access = !entry.getValue().isEmpty()? true: false;
            currentSection.addAccess(new AccessImpl(new AccessPath(entry.getValue()), access));
            startSection = false;
          }
          break;

        case CrawlDelay:
          if (currentSection != null) {
            try {
              assureRobots();
              int crawlDelay = Integer.parseInt(entry.getValue());
              robots.setCrawlDelay(crawlDelay);
            } catch (NumberFormatException ex) {
            }
            startSection = false;
          }
          break;
          
        case Host:
          assureRobots();
          robots.setHost(entry.getValue());
          startSection = false;
          break;
          
        case Sitemap:
          assureRobots();
          robots.getSitemaps().add(entry.getValue());
          startSection = false;
          break;
          
        default:
          startSection = false;
          break;
      }
    }
      
    if (currentSection != null) {
      addSection(currentSection);
    }
    
    return robots;
  }

  /**
   * Reads next entry from the reader.
   *
   * @return entry or <code>null</code> if no more data in the stream
   * @throws IOException if reading from stream fails
   */
  protected Map.Entry<Directive, String> readEntry() throws IOException {
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      Map.Entry<Directive, String> entry = parseEntry(line);
      if (entry != null) {
        return entry;
      }
    }
    return null;
  }

  /**
   * Parses line into entry.
   * <p>
   * Skip empty lines. Skip comments. Skip invalid lines.
   *
   * @param line line to parse
   * @return entry or <code>null</code> if skipped
   * @throws IOException parsing line fails
   */
  protected Map.Entry<Directive, String> parseEntry(String line) throws IOException {
    line = Val.chkStr(line);
    if (line.startsWith("#")) {
      return null;
    }

    int colonIndex = line.indexOf(":");
    if (colonIndex < 0) {
      return null;
    }

    String key = Val.chkStr(line.substring(0, colonIndex));
    Directive dir = Directive.parseDirective(key);
    if (dir == null) {
      return null;
    }

    String rest = line.substring(colonIndex + 1, line.length());
    int hashIndex = rest.indexOf("#");

    String value = Val.chkStr(hashIndex >= 0 ? rest.substring(0, hashIndex) : rest);

    value = decode(value);

    return new Entry(dir, value);
  }

  private void addSection(Section section) {
    assureRobots();
    robots.addSection(section);
  }

  private void assureRobots() {
    if (robots == null) {
      robots = new BotsImpl(userAgent,matchingStrategy,winningStrategy);
    }
  }

  /**
   * Local implementation of Map.Entry interface.
   */
  protected final static class Entry implements Map.Entry<Directive, String> {

    private final Directive key;
    private String value;

    public Entry(Directive key, String value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public Directive getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public String setValue(String value) {
      String oldValue = value;
      this.value = value;
      return oldValue;
    }

    @Override
    public String toString() {
      return String.format("%s: %s", key, value != null ? value : "");
    }
  }
}
