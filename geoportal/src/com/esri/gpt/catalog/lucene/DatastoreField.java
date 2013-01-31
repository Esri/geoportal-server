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
import com.esri.gpt.framework.util.Val;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Represents a field that is held within the underlying data store.
 */
public class DatastoreField {
  
  /** class variables ========================================================= */
  
  /** The Logger */
  private static final Logger LOGGER = Logger.getLogger(DatastoreField.class.getName());
    
  /** instance variables ====================================================== */
  private Field.Index      indexingOption = Field.Index.ANALYZED;
  private String           name = "";
  private Field.Store      storeageOption = Field.Store.YES;
  private Field.TermVector termVectorOption = Field.TermVector.NO;
  
  /** constructors ============================================================ */
    
  /**
   * Constructs with fully supplied parameters.
   * @param name the field name
   * @param storageOption the storage option
   * @param indexingOption the indexing option
   * @param termVectorOption the term vector option
   */
  protected DatastoreField(String name, 
                           Field.Store storageOption, 
                           Field.Index indexingOption,
                           Field.TermVector termVectorOption) {  
    setName(name);
    setStorageOption(storageOption);
    setIndexingOption(indexingOption);
    setTermVectorOption(termVectorOption);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the indexing option.
   * @return the indexing option
   */
  protected Field.Index getIndexingOption() {
    return indexingOption;
  }
  /**
   * Sets the indexing option.
   * @param indexingOption the indexing option
   */
  protected void setIndexingOption(Field.Index indexingOption) {
    this.indexingOption = indexingOption;
    if (this.indexingOption == null) this.indexingOption = Field.Index.ANALYZED;
  }
  
  /**
   * Gets the field name.
   * @return the field name
   */
  protected String getName() {
    return name;
  }
  /**
   * Sets the field name.
   * <br/>The name will be trimmed, a null name is treated as an empty string.
   * @param name the field name
   */
  protected void setName(String name) {
    this.name = Val.chkStr(name);
  }
    
  /**
   * Gets the storage option.
   * @return the storage option
   */
  protected Field.Store getStorageOption() {
    return storeageOption;
  }
  /**
   * Sets the storage option.
   * @param storageOption the storage option
   */
  protected void setStorageOption(Field.Store storageOption) {
    this.storeageOption = storageOption;
    if (this.storeageOption == null) this.storeageOption = Field.Store.YES;
  }
  
  /**
   * Gets the term vector option.
   * @return the term vector  option
   */
  protected Field.TermVector getTermVectorOption() {
    return termVectorOption;
  }
  /**
   * Sets the term vector  option.
   * @param termVectorOption the term vector option
   */
  protected void setTermVectorOption(Field.TermVector termVectorOption) {
    this.termVectorOption = termVectorOption;
    if (this.termVectorOption == null) this.termVectorOption = Field.TermVector.NO;
  }
    
  /** methods ================================================================= */
  
  /**
   * Appends the field to a document prior to writing the document to the index.
   * <p/>
   * The field will not be appended if it's name or value is empty.
   * @param document the Lucene document
   * @param value the input value to write
   */
  protected void appendForWrite(Document document, Object value) {
    String sName = getName();
    Fieldable fld = null;
    String sValueInput = null;
    if (value != null) {
      sValueInput = value.toString();
      fld = this.makeFieldable(value);
    }
    if (sName.length() == 0) {
      LOGGER.fine("The field has not been named and will not be stored.");
    } else if (fld == null) {
      LOGGER.log(Level.FINER, "{0} has an empty value and will not be stored.", sName);
    } else {
      
      if (fld != null) {
        if (LOGGER.isLoggable(Level.FINER)) {
          String sTmp = fld.stringValue();
          if (sTmp.length() > 101) sTmp = sTmp.substring(0,101)+" ...";
          StringBuilder sb = new StringBuilder();
          sb.append("Appending field:\n ");
          sb.append(" name=\"").append(fld.name()).append("\"");
          sb.append(" stored=\"").append(fld.isStored()).append("\"");
          sb.append(" indexed=\"").append(fld.isIndexed()).append("\"");
          sb.append(" analyzed=\"").append(fld.isTokenized()).append("\"");
          sb.append(" termVector=\"").append(fld.isTermVectorStored()).append("\"");
          sb.append("\n  storeValue=\"").append(sTmp).append("\"");
          if ((sValueInput != null) && !sValueInput.equals(fld.stringValue())) {
            sTmp = sValueInput;
            if (sTmp.length() > 101) sTmp = sTmp.substring(0,101)+" ...";
            sb.append("\n  inputValue=\"").append(sValueInput).append("\"");
          }
          LOGGER.finer(sb.toString());
        }
        document.add(fld);
      }
    }
  }
  
  /**
   * Makes the fieldable to index.
   * <p/>
   * Sub-classes should override this method if converted values need to
   * be written for subsequent search (ex. Doubles, Longs, Timestamps ...).
   * <p/>
   * The default behavior is to return the supplied value.
   * <br/>Null or empty values will not be stored.
   * @param value the value
   * @return the fieldable
   */
  protected Fieldable makeFieldable(Object value) {
    if ((value != null) && (value instanceof String)) {
      String sValue = (String)value;
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
   * <p/>
   * Sub-classes should override this method if values need to be converted
   * for a search (ex. Doubles, Longs, Timestamps, ...).
   * <p/>
   * The default behavior is to return a new TermRangeQuery.
   * @param literalLowerValue the literal lower boundary value
   * @param literalUpperValue the literal upper boundary value
   * @param lowerBoundaryIsInclusive (>= versus >)
   * @param upperBoundaryIsInclusive (<= versus <)
   * @throws DiscoveryException if the supplied value cannot be converted
   */
  protected Query makeRangeQuery(String literalLowerValue,
                                 String literalUpperValue,
                                 boolean lowerBoundaryIsInclusive,
                                 boolean upperBoundaryIsInclusive) 
    throws DiscoveryException {
    return new TermRangeQuery(this.getName(),
        literalLowerValue,literalUpperValue,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
  }
  
  /**
   * Makes the value to query.
   * <p/>
   * Sub-classes should override this method if values need to be converted
   * for a search (ex. Doubles, Longs, Timestamps, ...).
   * <p/>
   * The default behavior is to return the supplied value.
   * @param value to input query value
   * @param isLowerBoundary true if this is a lower boundary of a range query
   * @param isUpperBoundary true if this is a upper boundary of a range query
   * @return the value to query
   * @throws DiscoveryException if the supplied value cannot be converted
   */
  protected String makeValueToQuery(String value, 
                                    boolean isLowerBoundary,
                                    boolean isUpperBoundary) 
    throws DiscoveryException {
    return value;
  }
  
  /**
   * Makes the value to return in the query result.
   * <p/>
   * The value should be converted if required (example: from a double stored
   * as an indexable string within the document back to it's original form).
   * <p/>
   * The default behavior is to return the supplied value of the field. Sub-classes
   * should override this method if the value needs to be converted back to a
   * Double, Long, Timestamp, ...
   * @param storedValue the value stored within the Lucene document
   * @return the associated object value
   */
  protected Object makeValueToReturn(String storedValue) {
    return storedValue;
  }
  
  /**
   * Returns the sort field type.
   * <p/>
   * Sub-classes should override this method if values are numeric
   * (ex. Doubles, Longs, Timestamps, ...).
   * @return the sort field type (by default SortField.STRING)
   */
  protected int sortFieldType() {
    return SortField.STRING;
  }
   
}
