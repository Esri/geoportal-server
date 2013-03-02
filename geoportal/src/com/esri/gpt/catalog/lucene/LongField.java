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
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.PropertyValueType;
import com.esri.gpt.framework.util.Val;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.NumericUtils;

/**
 * A non-tokenized field that allows for Long value comparison.
 */
public class LongField extends DatastoreField {
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the field name
   */
  protected LongField(String name) { 
    super(name,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
  }
  
  /** methods ================================================================= */
  
  /**
   * Reverts an indexable String back to a long value.
   * @param value the indexable string to revert
   * @return the long value
   */
  protected static Long longFromIndexableString(String value) {
    try {
      long lValue = NumberTools.stringToLong(value);
      return lValue;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Converts a long value to a String that can be indexed for search.
   * @param value the value to convert
   * @return the indexable string
   */
  protected static String longToIndexableString(Long value) {
    if (value == null) {
      return null;
    } else {
      return NumberTools.longToString(value);
    }
  }
  
  /**
   * Makes the fieldable to index.
   * @param value the value
   * @return the fieldable
   */
  @Override
  protected Fieldable makeFieldable(Object value) {
    Long lValue = null;
    if (value instanceof Long) {
      lValue = (Long)value;
    } else if (value instanceof String) {
      try {
        PropertyValueType valueType = PropertyValueType.LONG;
        lValue = (Long)valueType.evaluate((String)value);
      } catch (IllegalArgumentException e) {}      
    }
    if (lValue != null) {
      boolean bIndex = !this.getIndexingOption().equals(Field.Index.NO);
      NumericField fld = new NumericField(this.getName(),this.getStorageOption(),bIndex);
      fld.setLongValue(lValue);
      return fld;
    } else {
      return null;
    }
  }
  
  /**
   * Makes a range query.
   * @param literalLowerValue the literal lower boundary value
   * @param literalUpperValue the literal upper boundary value
   * @param lowerBoundaryIsInclusive (>= versus >)
   * @param upperBoundaryIsInclusive (<= versus <)
   * @return the value to query
   * @throws DiscoveryException if a supplied value cannot be converted
   */
  @Override
  protected Query makeRangeQuery(String literalLowerValue,
                                 String literalUpperValue,
                                 boolean lowerBoundaryIsInclusive,
                                 boolean upperBoundaryIsInclusive) 
    throws DiscoveryException {
    literalLowerValue = Val.chkStr(literalLowerValue);
    literalUpperValue = Val.chkStr(literalUpperValue);
    PropertyValueType valueType = PropertyValueType.LONG;
    Long lLower = null;
    Long lUpper = null;
    if (literalLowerValue.length() > 0) {
      try {
        lLower = (Long)valueType.evaluate(literalLowerValue);
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Long: "+literalLowerValue);
      }
    }
    if (literalUpperValue.length() > 0) {
      try {
        lUpper = (Long)valueType.evaluate(literalUpperValue);
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Long: "+literalUpperValue);
      }
    }
    if ((lLower == null) && (lUpper == null)) {
      throw new DiscoveryException("No range values were supplied.");
    }
    return NumericRangeQuery.newLongRange(
        this.getName(),lLower,lUpper,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
  }
  
  /**
   * Makes the value to query.
   * <br/>The value to query is derived from NumericUtils.longToPrefixCoded().
   * @param value to input query value
   * @param isLowerBoundary true if this is a lower boundary of a range query
   * @param isUpperBoundary true if this is a upper boundary of a range query
   * @return the value to query
   * @throws DiscoveryException if the supplied value cannot be converted
   */
  @Override
  protected String makeValueToQuery(String value, 
                                    boolean isLowerBoundary,
                                    boolean isUpperBoundary) 
    throws DiscoveryException {
    try {
      PropertyValueType valueType = PropertyValueType.LONG;
      Long lValue = (Long)valueType.evaluate(value);
      return NumericUtils.longToPrefixCoded(lValue);
    } catch (NumberFormatException e) {
      throw new DiscoveryException("Invalid Long: "+value);
    }
  }
  
  /**
   * Makes the value to return in the query result.
   * @param storedValue the value stored within the Lucene document
   * @return the associated object value
   */
  @Override
  protected Object makeValueToReturn(String storedValue) {
    try {
      return Long.valueOf(storedValue);
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  /**
   * Returns the sort field type.
   * @return the sort field type (SortField.LONG)
   */
  @Override
  protected int sortFieldType() {
    return SortField.LONG;
  }

}
