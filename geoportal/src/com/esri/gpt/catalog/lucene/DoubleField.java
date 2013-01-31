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
 * A non-tokenized field that allows for Double value comparison.
 */
public class DoubleField extends DatastoreField {
  
  /** class variables ========================================================= */
  
  /** The default storage precision = 8 */
  protected static final int DEFAULT_PRECISION = 8;

  /** instance variables ====================================================== */
  private int precision;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name and precision.
   * @param name the field name
   * @param precision the storage precision
   */
  protected DoubleField(String name, int precision) { 
    super(name,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
    this.precision = precision;
  }
 
  /** properties ============================================================== */
    
  /**
   * Gets the storage precision.
   * @return the storage precision
   */
  protected int getPrecision() {
    return precision;
  }
  
  /** methods ================================================================= */
  
  /**
   * Reverts an indexable String back to a double value.
   * @param value the indexable string to revert
   * @return the double value
   */
  protected static Double doubleFromIndexableString(String value, int precision) {
    try {
      long lValue = NumberTools.stringToLong(value);
      long lConversionFactor = (long)Math.pow((double)10,(double)precision);
      return (double)lValue / lConversionFactor;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Converts a double value to a String that can be indexed for search.
   * @param value the value to convert
   * @return the indexable string
   */
  protected static String doubleToIndexableString(Double value, int precision) {
    if (value == null) {
      return null;
    } else {
      long lConversionFactor = (long)Math.pow((double)10,(double)precision);
      long lValue = Math.round(lConversionFactor * value);
      return NumberTools.longToString(lValue);
    }
  }
  
  /**
   * Makes the fieldable to index.
   * @param value the value
   * @return the fieldable
   */
  @Override
  protected Fieldable makeFieldable(Object value) {
    Double dValue = null;
    if (value instanceof Double) {
      dValue = (Double)value;
    } else if (value instanceof String) {
      try {
        PropertyValueType valueType = PropertyValueType.DOUBLE;
        dValue = (Double)valueType.evaluate((String)value);
      } catch (IllegalArgumentException e) {}      
    }
    if (dValue != null) {
      boolean bIndex = !this.getIndexingOption().equals(Field.Index.NO);
      NumericField fld = new NumericField(this.getName(),this.getStorageOption(),bIndex);
      fld.setDoubleValue(dValue);
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
    PropertyValueType valueType = PropertyValueType.DOUBLE;
    Double dLower = null;
    Double dUpper = null;
    if (literalLowerValue.length() > 0) {
      try {
        dLower = (Double)valueType.evaluate(literalLowerValue);
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Double: "+literalLowerValue);
      }
    }
    if (literalUpperValue.length() > 0) {
      try {
        dUpper = (Double)valueType.evaluate(literalUpperValue);
      } catch (NumberFormatException e) {
        throw new DiscoveryException("Invalid Double: "+literalUpperValue);
      }
    }
    if ((dLower == null) && (dUpper == null)) {
      throw new DiscoveryException("No range values were supplied.");
    }
    return NumericRangeQuery.newDoubleRange(
        this.getName(),dLower,dUpper,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
  }
  
  /**
   * Makes the value to query.
   * <br/>The value to query is derived from NumericUtils.doubleToPrefixCoded().
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
      PropertyValueType valueType = PropertyValueType.DOUBLE;
      Double dValue = (Double)valueType.evaluate(value);
      return NumericUtils.doubleToPrefixCoded(dValue);
    } catch (NumberFormatException e) {
      throw new DiscoveryException("Invalid Double: "+value);
    }
  }
  
  /**
   * Makes the value to return in the query result.
   * <br/>The value to return is derived from doubleFromIndexableString().
   * @param storedValue the value stored within the Lucene document
   * @return the associated object value
   */
  @Override
  protected Object makeValueToReturn(String storedValue) {
    try {
      return Double.parseDouble(storedValue);
    } catch (NumberFormatException ex) {
      return null;
    }
  }
  
  /**
   * Returns the sort field type.
   * @return the sort field type (SortField.DOUBLE)
   */
  @Override
  protected int sortFieldType() {
    return SortField.DOUBLE;
  }

}
