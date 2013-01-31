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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.util.Val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.ParseException;

/**
 * Resolves term through a chain of different proxies. Each prozy can change
 * passed term into equivalent Lucene term. The example of using resolver is
 * ontology service.
 */
/**package*/class TermResolver {

  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TermResolver.class.getName());
  
  /** services stream pattern */
  private static final Pattern streamPattern = Pattern.compile("([a-zA-Z0-9_]+\\|)+");
  /** quote pattern */
  private static final Pattern quotePatern = Pattern.compile("(^|[^\\\\])\"");
  
  /** map of proxies */
  private Map<String, IParserProxy> proxies;
  
  /**
   * Creates instance of the streamer.
   * @param proxies proxies
   */
  public TermResolver(Map<String, IParserProxy> proxies) {
    this.proxies = proxies!=null? proxies: new HashMap<String, IParserProxy>();
  }
  
  /**
   * Resolves term with equivalent lucene syntax compliant string.
   * @param serviceNames term string to alter
   * @return lucene syntax compliant string
   * @throws org.apache.lucene.queryParser.ParseException if altering term fails
   */
  public String resolve(String termStr) throws ParseException {
    if (proxies.isEmpty()) return termStr;
    
    String orgTermStr = termStr = Val.chkStr(termStr);
  
    // replaceableSections
    Sections sections = createSections(termStr);
  
    // alter each sectionText with the string received from the external parsing
    // mechanizm
    for (Section group : sections) {
      List<String> chainStr = Arrays.asList(group.toArray());
      Section termSection = advanceTerm(termStr, group.end);
      String term = termSection.toString();
      termStr = termStr.substring(0, group.start) +
        translate(chainStr,term) +
        (termSection.end < termStr.length() ? termStr.substring(termSection.end) : "");
    }
  
    while (termStr.startsWith("(") && termStr.endsWith(")")) {
      termStr = termStr.replaceAll("^\\(|\\)$","");
    }
  
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("Streaming: "+orgTermStr+" -> "+termStr);
    }
  
    return termStr;
  }
  
  /**
   * Advances term to the end
   * @param termStr term to advance
   * @param start starting point of advancing
   * @return section describing term
   */
  private Section advanceTerm(String termStr, int start) {
    boolean escapeMode = false;
    boolean quotationMode = false;
    Stack<Character> expectedBrackets = new Stack<Character>();
  
    while (start < termStr.length() && Character.isWhitespace(termStr.charAt(start))) {
      start++;
    }
  
    int index = start;
  
  loop:
    while (index < termStr.length()) {
      char c = termStr.charAt(index);
      if (Character.isWhitespace(c)) {
        if (!quotationMode && expectedBrackets.empty()) {
          break;
        }
      } else {
        if (!escapeMode) {
          if (c == '\\') {
            escapeMode = true;
          } else {
            if (!quotationMode) {
              if (c == '\"') {
                quotationMode = true;
              } else {
                if (!expectedBrackets.empty() && c == expectedBrackets.peek()) {
                  expectedBrackets.pop();
                } else {
                  switch (c) {
                    case '(':
                      expectedBrackets.push(')');
                      break;
                    case '<':
                      expectedBrackets.push('>');
                      break;
                    case '{':
                      expectedBrackets.push('}');
                      break;
                    case ')':
                    case '>':
                    case '}':
                      break loop;
                  }
                }
              }
            } else {
              if (c == '\"') {
                quotationMode = false;
              }
            }
          }
        } else {
          escapeMode = false;
        }
      }
      index++;
    }
  
    return new Section(termStr, start, index);
  }
  
  /**
   * Finds quoted sections of the string.
   * @param termStr term to translate
   * @return quoted sections
   */
  private Sections findQuotedSections(String termStr) {
    // collection of quoted sections
    Sections quotedSections = new Sections();
  
    Matcher quoteMatcher = quotePatern.matcher(termStr);
    int quoteIndex = 0;
    while (quoteIndex < termStr.length()) {
      if (quoteIndex >= termStr.length() || !quoteMatcher.find(quoteIndex)) {
        break;
      }
      int start = quoteMatcher.start();
      quoteIndex = quoteMatcher.end() + 1;
      if (quoteIndex >= termStr.length() || !quoteMatcher.find(quoteIndex)) {
        break;
      }
      int end = quoteMatcher.end();
      quoteIndex = quoteMatcher.end() + 1;
      Section section = new Section(termStr, start, end);
      quotedSections.add(section);
    }
    return quotedSections;
  }
  
  /**
   * Create replaceableSections of expressions to replace by external parser.
   * @param termStr term to translate
   * @return collection of sections
   */
  private Sections createSections(String termStr) {
    // collection of quoted sections
    Sections quotedSections = findQuotedSections(termStr);
  
    // collection of replaceable replaceable sections
    Sections replaceableSections = new Sections();
  
    // create serviceProxy stream matcher
    Matcher streamMatcher = streamPattern.matcher(termStr);
  
    // find all replaceableSections having serviceProxy stream definition within the string
    int findStart = 0;
    while (findStart < termStr.length() && streamMatcher.find(findStart)) {
      int start = streamMatcher.start();
      int end = streamMatcher.end();
  
      Section section = new Section(termStr, start, end);
  
      if (!quotedSections.contains(section)) {
        replaceableSections.add(section);
      }
  
      findStart = end + 1;
    }
  
    // reverse collection of replaceableSections
    Collections.reverse(replaceableSections);
  
    return replaceableSections;
  }
  
  /**
   * Delegates term to the serviceProxy.
   * @param serviceNames chain of service names
   * @param termStr term to translate
   * @return term term to translate
   * @throws org.apache.lucene.queryParser.ParseException if unable to delegate term
   */
  private String translate(List<String> serviceNames, String termStr) throws ParseException {
    if (serviceNames.size()>0) {
      String serviceName = serviceNames.get(serviceNames.size()-1);
      serviceNames = serviceNames.subList(0, serviceNames.size()-1);
      termStr = Val.chkStr(termStr).replaceAll("^\"|\"$", "");
      String newTermStr = translate(serviceNames, translate(serviceName, termStr));
      if (!newTermStr.equals(termStr)) {
        termStr = "(" + newTermStr + ")";
      }
    }
    return termStr;
  }
  
  /**
   * Translate term by the serviceName. Sends a term into translating service
   * through the corresponding proxy.
   * @param serviceName serviceName name
   * @param term term to translate
   * @return translated term
   * @throws ParseException if unable to translate term
   */
  private String translate(String serviceName, String term) throws ParseException {
    serviceName = Val.chkStr(serviceName);
    if (serviceName.length()==0) {
      throw new ParseException("Invalid service name: \""+Val.chkStr(serviceName)+"\".");
    }
    IParserProxy serviceProxy = proxies.get(serviceName);
    if (serviceProxy==null) {
      throw new ParseException("Unrecognized service name: \""+Val.chkStr(serviceName)+"\".");
    }
    return serviceProxy.parse(term);
  }
  
  /**
   * Section of the string.
   */
  private class Section {
    /** omplete string */
    String str;
    /** start of sectionText */
    int start;
    /** end of sectionText */
    int end;
  
    /**
     * Creates instance of the sectionText.
     * @param complete string from which a section is being cut off
     * @param start start of sectionText
     * @param end end of sectionText
     */
    Section(String str, int start, int end) {
      this.str = str;
      this.start = start;
      this.end = end;
    }
  
    /**
     * Checks if section contains another section.
     * @param section another section which might be contained within the current section
     * @return <code>true</code> if current section contains given section
     */
    boolean contains(Section section) {
      return start <= section.start && end >= section.end;
    }
  
    @Override
    public String toString() {
      return str.substring(start, end);
    }
  
    /**
     * Changes into array of strings.
     * @return array of strings
     */
    public String [] toArray() {
      return toString().replaceAll("^\\||\\|$", "").split("\\|+");
    }
  }
  
  /**
   * Collection of sections.
   */
  private static class Sections extends ArrayList<Section> {
  
    /**
     * Checks if is there any section which contains given section.
     * @param section section to check
     * @return <code>true</code> if there is at least one section which contains given section
     */
    boolean contains(Section section) {
      for (Section s : this) {
        if (s.contains(section)) {
          return true;
        }
      }
      return false;
    }
  }

}
