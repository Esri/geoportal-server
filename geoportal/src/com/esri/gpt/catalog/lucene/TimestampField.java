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

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.logging.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.NumericUtils;

/**
 * A non-tokenized field that allows for date/time comparison.
 */
public class TimestampField extends DatastoreField {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TimestampField.class.getName());
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the field name
   */
  protected TimestampField(String name) {
    super(name,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
  }
 
  /** methods ================================================================= */
  
  /**
   * Makes the fieldable to index.
   * @param value the value
   * @return the fieldable
   */
  protected Fieldable makeFieldable(Object value) {
    Timestamp tsValue = null;
    if (value instanceof Timestamp) {
      tsValue = (Timestamp)value;
    } else if (value instanceof String) {
      try {
        PropertyValueType valueType = PropertyValueType.TIMESTAMP;
        tsValue = (Timestamp)valueType.evaluate((String)value);
      } catch (IllegalArgumentException e) {}      
    }
    if (tsValue != null) {
      boolean bIndex = !this.getIndexingOption().equals(Field.Index.NO);
      NumericField fld = new NumericField(this.getName(),this.getStorageOption(),bIndex);
      fld.setLongValue(tsValue.getTime());
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
  protected Query makeRangeQuery(String literalLowerValue,
                                 String literalUpperValue,
                                 boolean lowerBoundaryIsInclusive,
                                 boolean upperBoundaryIsInclusive) 
    throws DiscoveryException {
    literalLowerValue = Val.chkStr(literalLowerValue);
    literalUpperValue = Val.chkStr(literalUpperValue);
    PropertyValueType valueType = PropertyValueType.TIMESTAMP;
    Long lLower = null;
    Long lUpper = null;
    if (literalLowerValue.length() > 0) {
      try {
        Timestamp ts = (Timestamp)valueType.evaluate(literalLowerValue,true,false);
        if (ts != null) lLower = ts.getTime();
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Timestamp: "+literalLowerValue);
      }
    }
    if (literalUpperValue.length() > 0) {
      try {
        Timestamp ts = (Timestamp)valueType.evaluate(literalUpperValue,false,true);
        if (ts != null) lUpper = ts.getTime();
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Timestamp: "+literalUpperValue);
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
   * <br/>The value to query is derived from NumericUtils.longToPrefixCoded(timestamp.getTime().
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
      PropertyValueType valueType = PropertyValueType.TIMESTAMP;
      Timestamp tsValue = (Timestamp)valueType.evaluate(
          value,isLowerBoundary,isUpperBoundary);
      if (tsValue == null) return null;
      
      if (isLowerBoundary) {
        LOGGER.finer("Lower boundary timestamp to query: "+tsValue);
      } else if (isUpperBoundary) {
        LOGGER.finer("Upper boundary timestamp to query: "+tsValue);
      } else {
        LOGGER.finer("Timestamp to query: "+tsValue);
      }
      
      return NumericUtils.longToPrefixCoded(tsValue.getTime());
    } catch (IllegalArgumentException e) {
      throw new DiscoveryException("Invalid date: "+value);
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
      long lValue = Long.valueOf(storedValue);
      return new Timestamp(lValue);
    } catch (NumberFormatException e) {
      return null;
    }
  }
    
  /**
   * Reverts an indexable String back to a time stamp.
   * @param value the indexable string to revert
   * @return the update date (null if the value is invalid)
   */
  protected static Timestamp timestampFromIndexableString(String value) {
    try {
      long lValue = DateTools.stringToTime(value);
      return new Timestamp(lValue);
    } catch (ParseException e) {
      return null;
    }
  }

  /**
   * Converts a time stamp to a String that can be indexed for search.
   * @param value the times stamp to convert
   * @return the indexable string
   */
  protected static String timestampToIndexableString(Timestamp value) {
    if (value == null) {
      return null;
    } else {
      return DateTools.timeToString(value.getTime(),DateTools.Resolution.MILLISECOND);
    }
  }
  
  /**
   * Returns the sort field type.
   * @return the sort field type (SortField.LONG)
   */
  protected int sortFieldType() {
    return SortField.LONG;
  }

}
