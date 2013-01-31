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
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.index.AsnIndexReference;
import com.esri.gpt.server.assertion.index.Assertion;

/**
 * Represents a configured assertion set (i.e. comments, ratings, ...).
 */
public class AsnAssertionSet {

  /** instance variables ====================================================== */
  private String            assertionIdPrefix;
  private AsnAuthPolicy     authPolicy;
  private AsnIndexReference indexReference;
  private String            name;
  private String            urnPrefix;
  private AsnValueType      valueType;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnAssertionSet() {}
  
  /**
   * Constructs with a supplied name.
   * @param name the name associated with the set
   */
  public AsnAssertionSet(String name) {
    name = Val.chkStr(name);
    if (name.length() == 0) {
      throw new IllegalArgumentException("This name cannot be empty.");
    }
    this.setName(name);
    this.setURNPrefix(AsnConstants.APP_URN_PREFIX+":"+this.getName());
    this.setAssertionIdPrefix(this.getURNPrefix()+":assertionid");
  }
    
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnAssertionSet(AsnAssertionSet objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setAssertionIdPrefix(objectToDuplicate.getAssertionIdPrefix());
      this.setName(objectToDuplicate.getName());
      this.setURNPrefix(objectToDuplicate.getURNPrefix());
      if (objectToDuplicate.getAuthPolicy() != null) {
        this.setAuthPolicy(objectToDuplicate.getAuthPolicy().duplicate());
      }
      if (objectToDuplicate.getIndexReference() != null) {
        this.setIndexReference(objectToDuplicate.getIndexReference().duplicate());
      }
      if (objectToDuplicate.getValueType() != null) {
        this.setValueType(objectToDuplicate.getValueType().duplicate());
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the URN prefix associated with subject assertion IDs.
   * @return the assertion ID prefix
   */
  public String getAssertionIdPrefix() {
    return this.assertionIdPrefix;
  }
  /**
   * Sets the URN prefix associated with subject assertion IDs.
   * @param urnPrefix the assertion ID prefix
   */
  public void setAssertionIdPrefix(String urnPrefix) {
    this.assertionIdPrefix = urnPrefix;
  }
  
  /**
   * Gets the authorization policy.
   * @return the authorization policy
   */
  public AsnAuthPolicy getAuthPolicy() {
    return this.authPolicy;
  }
  /**
   * Sets the authorization policy.
   * @param authPolicy the authorization policy
   */
  public void setAuthPolicy(AsnAuthPolicy authPolicy) {
    this.authPolicy = authPolicy;
  }
  
  /**
   * Gets the configuration reference to index associated with the operation.
   * @return the index configuration reference
   */
  public AsnIndexReference getIndexReference() {
    return this.indexReference;
  }
  /**
   * Sets the configuration reference to index associated with the operation
   * @param indexReference the index configuration reference
   */
  public void setIndexReference(AsnIndexReference indexReference) {
    this.indexReference = indexReference;
  }
  
  /**
   * Gets the name associated with the set.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the name associated with the set.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
    
  /**
   * Gets the URN prefix for this set.
   * @return the URN prefix
   */
  public String getURNPrefix() {
    return this.urnPrefix;
  }
  /**
   * Sets the URN prefix for this set.
   * @param urnPrefix the URN prefix
   */
  public void setURNPrefix(String urnPrefix) {
    this.urnPrefix = urnPrefix;
  }
    
  /**
   * Gets the value type.
   * @return the value type
   */
  public AsnValueType getValueType() {
    return this.valueType;
  }
  /**
   * Sets the value type.
   * @param valueType the value type
   */
  public void setValueType(AsnValueType valueType) {
    this.valueType = valueType;
  }

  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnAssertionSet(this);
   * @return the duplicated object
   */
  public AsnAssertionSet duplicate() {
    return new AsnAssertionSet(this);
  }
  
  /**
   * Instantiates a new assertion.
   * @param context the assertion operation context
   * @param forCreate true if this assertion will be indexed as part of a "create" operation
   * @return the new assertion
   */
  public Assertion newAssertion(AsnContext context, boolean forCreate) {
    AsnValueType vType = this.getValueType();
    Assertion assertion = new Assertion();
    assertion.getRdfPart().setAnalyzeValue(vType.getAnalyzePriorToIndexing());
    assertion.getRdfPart().setValueField(vType.getRdfValueField());
    if (forCreate) {
      AsnOperation operation = context.getOperation();
      assertion.setSystemPart(operation.getSystemPart());
      assertion.setUserPart(operation.getUserPart());
      assertion.getRdfPart().setSubject(operation.getSubject().getURN());
      assertion.getRdfPart().setPredicate(vType.getRdfPredicate());
      assertion.getRdfPart().setValue(operation.getValue().getTextValue());
    } 
    return assertion;
  }
    
}
