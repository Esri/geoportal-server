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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;

/**
 * Represents an assertion value type.
 */
public class AsnValueType {
  
  /** class variables ========================================================= */
  
  /** Value type name = "constrained" */
  public static final String VALUE_TYPENAME_CONSTRAINED = "constrained";
  
  /** Value type name = "free" */
  public static final String VALUE_TYPENAME_FREE = "free";

  /** instance variables ====================================================== */
  private boolean            analyzePriorToIndexing = false;
  private int                maxCharacters = 4000;
  private String             rdfPredicate;
  private String             rdfValueField;
  private boolean            required;
  private AsnSupportedValues supportedValues;
  private String             valueFilterClass;
  private String             valueTypeName;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnValueType() {}
    
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnValueType(AsnValueType objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setAnalyzePriorToIndexing(objectToDuplicate.getAnalyzePriorToIndexing());
      this.setMaxCharacters(objectToDuplicate.getMaxCharacters());
      this.setRdfPredicate(objectToDuplicate.getRdfPredicate());
      this.setRdfValueField(objectToDuplicate.getRdfValueField());
      this.setRequired(objectToDuplicate.getRequired());
      this.setValueFilterClass(objectToDuplicate.getValueFilterClass());
      this.setValueTypeName(objectToDuplicate.getValueTypeName());
      if (objectToDuplicate.getSupportedValues() != null) {
        this.setSupportedValues(objectToDuplicate.getSupportedValues().duplicate());
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not the value should be analyzed prior to indexing.
   * @return <code>true</code> if the value should be analyzed
   */
  public boolean getAnalyzePriorToIndexing() {
    return this.analyzePriorToIndexing;
  }
  /**
   * Sets the flag indicating whether or not the value should be analyzed prior to indexing.
   * @param analyze <code>true</code> if the value should be analyzed
   */
  public void setAnalyzePriorToIndexing(boolean analyze) {
    this.analyzePriorToIndexing = analyze;
  }
  
  /**
   * Gets the maximum number of characters allowed for the value.
   * <br/>Default = 4000.
   * @return the maximum number of characters allowed
   */
  public int getMaxCharacters() {
    return this.maxCharacters;
  }
  /**
   * Sets the maximum number of characters allowed for the value.
   * @param maxCharacters the maximum number of characters allowed
   */
  public void setMaxCharacters(int maxCharacters) {
    this.maxCharacters = maxCharacters;
  }
  
  /**
   * Gets the RDF predicate that will be indexed for this value type.
   * @return the RDF predicate (Lucene field rdf.predicate)
   */
  public String getRdfPredicate() {
    return this.rdfPredicate;
  }
  /**
   * Sets the RDF predicate that will be indexed for this value type.
   * @param rdfPredicate the RDF predicate (Lucene field rdf.predicate)
   */
  public void setRdfPredicate(String rdfPredicate) {
    this.rdfPredicate = rdfPredicate;
  }
  
  /**
   * Gets the RDF value field name within the Lucene index.
   * @return the RDF value field name
   */
  public String getRdfValueField() {
    return this.rdfValueField;
  }
  /**
   * Sets the RDF value field name within the Lucene index.
   * @param rdfValueField the RDF value field name
   */
  public void setRdfValueField(String rdfValueField) {
    this.rdfValueField = rdfValueField;
  }
  
  /**
   * Gets the flag indicating whether or not a value is required.
   * @return <code>true</code> if required
   */
  public boolean getRequired() {
    return this.required;
  }
  /**
   * Sets the flag indicating whether or not a value is required.
   * @param required <code>true</code> if required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }
  
  /**
   * Gets the domain values supported values.
   * @return the supported values (null if unconstrained) 
   */
  public AsnSupportedValues getSupportedValues() {
    return this.supportedValues;
  }
  /**
   * Sets the domain values supported values.
   * @param supportedValues the supported values (null if unconstrained) 
   */
  public void setSupportedValues(AsnSupportedValues supportedValues) {
    this.supportedValues = supportedValues;
  }
  
  /**
   * Gets the fully qualified class name of the value filter.
   * <br/>(must extend AsnValueFilter)
   * @return the value filter class  
   */
  public String getValueFilterClass() {
    return this.valueFilterClass;
  }
  /**
   * Sets the fully qualified class name of the value filter.
   * <br/>(must extend AsnValueAnalyzer)
   * @param valueFilterClass the value filter class 
   */
  public void setValueFilterClass(String valueFilterClass) {
    this.valueFilterClass = valueFilterClass;
  }
  
  /**
   * Gets the value type name.
   * @return the value type name
   */
  public String getValueTypeName() {
    return this.valueTypeName;
  }
  /**
   * Sets the value type name.
   * @param valueTypeName the value type name
   */
  public void setValueTypeName(String valueTypeName) {
    this.valueTypeName = valueTypeName;
  }
  
  /** methods ================================================================= */
    
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnValueType(this);
   * @return the duplicated object
   */
  public AsnValueType duplicate() {
    return new AsnValueType(this);
  }
  
  /**
   * Makes a value filter based upon the associated valueFilterClass.
   * @param context the assertion operation context
   * @return the value filter (can be null)
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public AsnValueFilter makeValueFilter(AsnContext context) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String className = Val.chkStr(this.getValueFilterClass());
    if (className.length() == 0) {
      return null;
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof AsnValueFilter) {
        return (AsnValueFilter)obj;
      } else {
        String msg = "The configured valueFilterClass is invalid: "+ className;
        throw new ConfigurationException(msg);
      }
    }
  }
    
}
