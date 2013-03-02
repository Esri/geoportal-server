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
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.framework.util.Val;
import org.apache.lucene.document.Document;

/**
 * Represents a data store property associated with a document.
 * <p/>
 * A storable property can have multiple associated fields, for instance a
 * GeometryProperty has 5 field fields:
 * <br/>geometry.minx geometry.minx geometry.maxx geometry.maxy geometry.area
 */
public class Storeable implements IStoreable{
    
  // class variables =============================================================
      
  // instance variables ==========================================================
  private DatastoreField  comparisonField;
  private DatastoreFields fields = new DatastoreFields();
  private String          name = "";
  private DatastoreField  retrievalField;
  private DatastoreField  termsField;
  private Object[]        values = new Object[0];
   
  // constructors ================================================================

  /**
   * Constructs with a supplied name.
   * @param name the property name
   */
  public Storeable(String name) {
    this.name = Val.chkStr(name);
    if (this.name.length() == 0) {
      throw new IllegalArgumentException("A Storable name is required.");
    }
  }
    
  // properties ==================================================================
  
  /**
   * Gets the non-tokenized field for exact/range comparison queries.
   * @return the comparison field
   */
  public DatastoreField getComparisonField() {
    return comparisonField;
  }
  /**
   * Sets the non-tokenized field for exact/range comparison queries.
   * @param field the comparison field
   */
  public void setComparisonField(DatastoreField field) {
    this.comparisonField = field;
  }   
    
  /**
   * Gets the field used to retrieve data from the store.
   * @return the retrieval field
   */
  public DatastoreField getRetrievalField() {
    return retrievalField;
  }
  /**
   * Sets the field used to retrieve data from the store.
   * @param field the retrieval field
   */
  public void setRetrievalField(DatastoreField field) {
    this.retrievalField = field;
  }
  
  /**
   * Gets the underlying data store fields associated with the property.
   * @return the data store fields.
   */
  public DatastoreFields getFields() {
    return fields;
  }
  
  /**
   * Gets the property name.
   * @return the property name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Gets the tokenized field for term based queries.
   * @return the terms field
   */
  public DatastoreField getTermsField() {
    return termsField;
  }
  /**
   * Sets the tokenized field for term based queries.
   * @param field the terms field
   */
  public void setTermsField(DatastoreField field) {
    this.termsField = field;
  }
  
  /** 
   * Gets the underlying values to store.
   * <p/>
   * The values array wild be null if the field was not populated
   * within the associated document.
   * <p/> 
   * There can be multiple values associated with a field, keywords 
   * for instance.
   * @return the data values to store
   */
  public Object[] getValues() {
    return values;
  }
  
  /** 
   * Sets the value collection to a single object value.
   * @param value the object value to set
   */
  public void setValue(Object value) {
    Object[] tmp = {value};
    this.values = tmp;
  }
  
  /** 
   * Sets the underlying values to store.
   * <p/>
   * The values array wild be null if the field was not populated
   * within the associated document.
   * <p/> 
   * There can be multiple values associated with a field, keywords 
   * for instance.
   * @param values the data values to store
   */
  public void setValues(Object[] values) {
    this.values = values;
  }
    
  // methods =====================================================================
  
  /**
   * Appends underlying fields to a document prior to writing the 
   * document to the index.
   * @param document the Lucene document
   */
  protected void appendForWrite(Document document) {
    if ((values != null) && (values.length > 0)) {
      for (Object value: values) {
        for (DatastoreField field: getFields()) {
          field.appendForWrite(document,value);
        }
      }
    }
  }
  
  /**
   * Appends underlying fields to a document prior to writing the 
   * document to the index.
   * @param document the Lucene document
   * @param value the input value to write
   */
  public void appendForWrite(Document document, Object value) {
    for (DatastoreField field: getFields()) {
      field.appendForWrite(document,value);
    }
  }
    
}
