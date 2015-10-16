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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Path.
 */
class AccessPath {
  private final String path;

  /**
   * Creates instance of the path.
   * @param relativePath path relative to the host
   */
  public AccessPath(String relativePath) {
    this.path = relativePath;
  }

  /**
   * Gets path.
   * @return path
   */
  public String getPath() {
    return path;
  }
  
  /**
   * Gets length (number of names) of the path.
   * @return length (number of names) of the path
   */
  public int getLength() {
    return path.length();
  }
  
  /**
   * Checks if given path matches.
   * @param relativePath path to check
   * @return <code>true</code> if path matches
   */
  public boolean match(String relativePath) {
    if (relativePath==null) return false;
    
    boolean reverse = false;
    String pattern = path;
    if (pattern.endsWith("$")) {
      pattern = pattern.replaceAll("\\$+$", "");
      reverse = true;
    }
    List<String> chain = split(pattern);
    List<String> input = split(relativePath);
    if (reverse) {
      Collections.reverse(chain);
      Collections.reverse(input);
    }
    
    while (!chain.isEmpty()) {
      String current = chain.remove(0);
      String next = !chain.isEmpty()? chain.get(0): null;
      
      if ("*".equals(current)) {
        if (input.isEmpty()) return false;
        input.remove(0);
      } else if ("**".equals(current)) {
        do {
          if (input.isEmpty()) return false;
          if (next!=null) {
            String text = input.get(0);
            if (match(next, text)) {
              break;
            }
          }
          input.remove(0);
        } while (!input.isEmpty());
      } else {
        if (input.isEmpty()) return false;
        if (!match(current,input.get(0))) return false;
        input.remove(0);
      }
    }
    
    return true;
  }
  
  @Override
  public String toString() {
    return path;
  }
  
  private List<String> split(String inputPath) {
    List<String> elements = new ArrayList(Arrays.asList(inputPath.split("/")));
    elements.removeIf(new Predicate<String>() {
      @Override
      public boolean test(String t) {
        return t.isEmpty();
      }
    });
    return elements;
  }
  
  private boolean match(String patternWithWildcards, String text) {
    try {
      return makePattern(patternWithWildcards).matcher(text).matches();
    } catch (Exception ex) {
      return false;
    }
  }
  
  private Pattern makePattern(String patternWithWildcards) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<patternWithWildcards.length(); i++) {
      char c = patternWithWildcards.charAt(i);
      switch (c) {
        case '*':
          sb.append(".*?");
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
}
