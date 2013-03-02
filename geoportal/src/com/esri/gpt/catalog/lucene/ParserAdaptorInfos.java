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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Collection of parser adaptor infos.
 */
public class ParserAdaptorInfos extends ArrayList<ParserAdaptorInfo> {

  /**
   * Creates collection of parser proxies.
   * @return collection of parser proxies
   */
  public Map<String,IParserProxy> createParserProxies() {
    // declare a map of proxies; this is a TreeMap for quick retrieval with
    // altered comparator to have case-insensitive retrieval
    Map<String,IParserProxy> parserProxies = 
      new TreeMap<String,IParserProxy>(String.CASE_INSENSITIVE_ORDER);

    // create parser proxy for each adaptor definition.
    for (ParserAdaptorInfo info : this) {
      if (info.getName().length()>0) {
        IParserProxy proxy = info.createParserProxy();
        // createParserProxy() may fail; in that case proxy will be null
        if (proxy!=null) {
          parserProxies.put(info.getName(), proxy);
        }
      }
    }

    return parserProxies;
  }
}
