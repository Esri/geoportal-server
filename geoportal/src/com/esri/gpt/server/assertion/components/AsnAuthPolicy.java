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

/**
 * Represents a configured authorization policy associated with an operation.
 */
public class AsnAuthPolicy {

  /** instance variables ====================================================== */
  private boolean       allowNonLocalResourceIds = false;
  private boolean       authRequired = true;
  private AsnPrincipals createPrincipals;
  private AsnPrincipals deletePrincipals;
  private AsnPrincipals enableDisablePrincipals;
  private boolean       multiplePerUserSubjectPredicate = false;
  private AsnPrincipals queryPrincipals;
  private AsnPrincipals updatePrincipals;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnAuthPolicy() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnAuthPolicy(AsnAuthPolicy objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setAllowNonLocalResourceIds(objectToDuplicate.getAllowNonLocalResourceIds());
      this.setAuthenticationRequired(objectToDuplicate.getAuthenticationRequired());
      this.setMultiplePerUserSubjectPredicate(objectToDuplicate.getMultiplePerUserSubjectPredicate());
      if (objectToDuplicate.getCreatePrincipals() != null) {
        this.setCreatePrincipals(objectToDuplicate.getCreatePrincipals().duplicate());
      }
      if (objectToDuplicate.getDeletePrincipals() != null) {
        this.setDeletePrincipals(objectToDuplicate.getDeletePrincipals().duplicate());
      }
      if (objectToDuplicate.getEnableDisablePrincipals() != null) {
        this.setEnableDisablePrincipals(objectToDuplicate.getEnableDisablePrincipals().duplicate());
      }
      if (objectToDuplicate.getQueryPrincipals() != null) {
        this.setQueryPrincipals(objectToDuplicate.getQueryPrincipals().duplicate());
      }
      if (objectToDuplicate.getUpdatePrincipals() != null) {
        this.setUpdatePrincipals(objectToDuplicate.getUpdatePrincipals().duplicate());
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not non local resource IDs are allowed.
   * @return <code>true</code> if non local resource IDs are allowed
   */
  public boolean getAllowNonLocalResourceIds() {
    return this.allowNonLocalResourceIds;
  }
  /**
   * Sets the flag indicating whether or not non local resource IDs are allowed. 
   * @param allow <code>true</code> if non local resource IDs are allowed
   */
  public void setAllowNonLocalResourceIds(boolean allow) {
    this.allowNonLocalResourceIds = allow;
  }
  
  /**
   * Gets the flag indicating whether or not authentication is required.
   * @return <code>true</code> if authentication is required
   */
  public boolean getAuthenticationRequired() {
    return this.authRequired;
  }
  /**
   * Sets the flag indicating whether or not authentication is required. 
   * @param required <code>true</code> if authentication is required
   */
  public void setAuthenticationRequired(boolean required) {
    this.authRequired = required;
  }
  
  /**
   * Gets the accepted principals required when creating a new assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @return the principals
   */
  public AsnPrincipals getCreatePrincipals() {
    return this.createPrincipals;
  }
  /**
   * Sets the accepted principals required when creating a new assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @param principals the principals
   */
  public void setCreatePrincipals(AsnPrincipals principals) {
    this.createPrincipals = principals;
  }
  
  /**
   * Gets the accepted principals required when deleting an assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @return the principals
   */
  public AsnPrincipals getDeletePrincipals() {
    return this.deletePrincipals;
  }
  /**
   * Sets the accepted principals required when deleting an assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @param principals the principals
   */
  public void setDeletePrincipals(AsnPrincipals principals) {
    this.deletePrincipals = principals;
  }
  
  /**
   * Gets the accepted principals required when enabling/disabling an assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @return the principals
   */
  public AsnPrincipals getEnableDisablePrincipals() {
    return this.enableDisablePrincipals;
  }
  /**
   * Sets the accepted principals required when enabling/disabling an assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @param principals the principals
   */
  public void setEnableDisablePrincipals(AsnPrincipals principals) {
    this.enableDisablePrincipals = principals;
  }
  
  /**
   * Gets the flag indicating whether or not a user can have multiple assertions 
   * per subject/predicate pair.
   * @return <code>true</code> if multiple assertions per user per subject is allowed
   */
  public boolean getMultiplePerUserSubjectPredicate() {
    return this.multiplePerUserSubjectPredicate;
  }
  /**
   * Sets the flag indicating whether or not a user can have multiple assertions 
   * per subject/predicate pair.
   * @param allowed <code>true</code> if multiple assertions per user/subject/predicate is allowed
   */
  public void setMultiplePerUserSubjectPredicate(boolean allowed) {
    this.multiplePerUserSubjectPredicate = allowed;
  }
  
  /**
   * Gets the accepted principals required when querying existing assertions.
   * <br/>Authorization should given if the user has any member of the set.
   * @return the principals
   */
  public AsnPrincipals getQueryPrincipals() {
    return this.queryPrincipals;
  }
  /**
   * Sets the accepted principals required when querying existing assertions.
   * <br/>Authorization should given if the user has any member of the set.
   * @param principals the principals
   */
  public void setQueryPrincipals(AsnPrincipals principals) {
    this.queryPrincipals = principals;
  }
  
  /**
   * Gets the accepted principals required when updating an existing assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @return the principals
   */
  public AsnPrincipals getUpdatePrincipals() {
    return this.updatePrincipals;
  }
  /**
   * Sets the accepted principals required when updating an existing assertion.
   * <br/>Authorization should given if the user has any member of the set.
   * @param principals the principals
   */
  public void setUpdatePrincipals(AsnPrincipals principals) {
    this.updatePrincipals = principals;
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnAuthPolicy(this);
   * @return the duplicated object
   */
  public AsnAuthPolicy duplicate() {
    return new AsnAuthPolicy(this);
  }
    
}
