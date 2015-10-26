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
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.framework.robots.PathMatcher;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sub path matcher.
 */
public class SubPathMatcher implements PathMatcher {

  @Override
  public boolean matches(String pattern, String pathToTest) {
    if (pathToTest==null) return false;
    if (pattern==null || pattern.isEmpty()) return true;
    
    try {
      if (!pathToTest.contains("*") && !pathToTest.endsWith("*")) {
        pathToTest += "*";
      }
      Pattern pt = makePattern(pathToTest);
      Matcher matcher = pt.matcher(pattern);
      return matcher.find() && matcher.start()==0;
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
    return Pattern.compile(sb.toString(),Pattern.CASE_INSENSITIVE);
  }
}
