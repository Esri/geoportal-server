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
import java.io.Serializable;
import org.apache.lucene.queryParser.ParseException;

/**
 * Proxy to the external query parser.
 * <p/>
 * Allows to parse query by some external mechanizm.
 * @see <a href="http://lucene.apache.org/java/2_4_0/queryparsersyntax.html">Lucene Query Syntax</a>
 */
public interface IParserProxy extends Serializable {

  /**
   * Delegates query parsing to some external mechanizm.
   * @param queryText query text to parse
   * @return response received from the other mechanizm conforming <i>Lucene</i>
   * query syntax.
   * @throws org.apache.lucene.queryParser.ParseException if unable to parse term
   */
  String parse(String queryText) throws ParseException;
}
