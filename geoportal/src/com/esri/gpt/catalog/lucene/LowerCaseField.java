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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import com.esri.gpt.catalog.discovery.DiscoveryException;

/**
 * A non-tokenized field stored in lower case allowing case insensitive
 * exact or range comparison.
 */
public class LowerCaseField extends DatastoreField {
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the field name
   */
  protected LowerCaseField(String name) { 
    super(name,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
  }
   
  /** methods ================================================================= */
  
  /**
   * Makes the fieldable to index.
   * @param value the value
   * @return the fieldable
   */
  @Override
  protected Fieldable makeFieldable(Object value) {
    if ((value != null) && (value instanceof String)) {
      String sValue = ((String)value).toLowerCase();
      if (sValue.length() > 0) {
        Field.Store storage = getStorageOption();
        Field.Index indexing = getIndexingOption();
        Field.TermVector termVector = getTermVectorOption();
        return new Field(this.getName(),sValue,storage,indexing,termVector);
      }
    }
    return null;
  }
  
  /**
   * Makes a range query.
   * @param literalLowerValue the literal lower boundary value
   * @param literalUpperValue the literal upper boundary value
   * @param lowerBoundaryIsInclusive (>= versus >)
   * @param upperBoundaryIsInclusive (<= versus <)
   * @throws DiscoveryException if the supplied value cannot be converted
   */
  @Override
  protected Query makeRangeQuery(String literalLowerValue,
                                 String literalUpperValue,
                                 boolean lowerBoundaryIsInclusive,
                                 boolean upperBoundaryIsInclusive) 
    throws DiscoveryException {
    if (literalLowerValue != null) literalLowerValue = literalLowerValue.toLowerCase();
    if (literalUpperValue != null) literalUpperValue = literalUpperValue.toLowerCase();
    return new TermRangeQuery(this.getName(),
        literalLowerValue,literalUpperValue,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
  }
  
  /**
   * Make the value to query.
   * @param value the input query value
   * @param isLowerBoundary true if this is a lower boundary of a range query
   * @param isUpperBoundary true if this is a upper boundary of a range query
   * @return the lower case value to query
   * @throws DiscoveryException if the supplied value cannot be converted
   */
  @Override
  protected String makeValueToQuery(String value, 
                                    boolean isLowerBoundary,
                                    boolean isUpperBoundary) 
    throws DiscoveryException {
    if (value == null) {
      return value;
    } else {
      return value.toLowerCase();
    }
  }

}
