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
import com.esri.gpt.server.assertion.handler.AsnOperationHandler;
import com.esri.gpt.server.assertion.index.AsnIndexReference;
import com.esri.gpt.server.assertion.index.AsnSystemPart;
import com.esri.gpt.server.assertion.index.AsnUserPart;

/**
 * Represents an assertion operation.
 */
public class AsnOperation {

  /** instance variables ====================================================== */
  private AsnAssertionSet   assertionSet;
  private AsnAuthPolicy     authPolicy;
  private String            handlerClass;
  private AsnIndexReference indexReference;
  private AsnPredicate      predicate;
  private AsnSubject        subject;
  private AsnSystemPart     systemPart;
  private AsnProperty       uiResources;
  private AsnUserPart       userPart;
  private AsnValue          value;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnOperation() {}
    
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnOperation(AsnOperation objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setHandlerClass(objectToDuplicate.getHandlerClass());
      if (objectToDuplicate.getAssertionSet() != null) {
        this.setAssertionSet(objectToDuplicate.getAssertionSet().duplicate());
      }
      if (objectToDuplicate.getAuthPolicy() != null) {
        this.setAuthPolicy(objectToDuplicate.getAuthPolicy().duplicate());
      }
      if (objectToDuplicate.getIndexReference() != null) {
        this.setIndexReference(objectToDuplicate.getIndexReference().duplicate());
      }
      if (objectToDuplicate.getPredicate() != null) {
        this.setPredicate(objectToDuplicate.getPredicate().duplicate());
      }
      if (objectToDuplicate.getSubject() != null) {
        this.setSubject(objectToDuplicate.getSubject().duplicate());
      }
      if (objectToDuplicate.getSystemPart() != null) {
        this.setSystemPart(objectToDuplicate.getSystemPart().duplicate());
      }
      if (objectToDuplicate.getUIResources() != null) {
        this.setUIResources(objectToDuplicate.getUIResources().duplicate());
      }
      if (objectToDuplicate.getUserPart() != null) {
        this.setUserPart(objectToDuplicate.getUserPart().duplicate());
      }
      if (objectToDuplicate.getValue() != null) {
        this.setValue(objectToDuplicate.getValue().duplicate());
      }
    } 
  }
  
  /**
   * Construct by duplicating components of an assertion set.
   * @param assertionSet the assertion set
   */
  public AsnOperation(AsnAssertionSet assertionSet) {
    this.setAssertionSet(assertionSet.duplicate());
    this.setAuthPolicy(this.getAssertionSet().getAuthPolicy());
    this.setIndexReference(this.getAssertionSet().getIndexReference());
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the assertion set.
   * @return the assertion set
   */
  public AsnAssertionSet getAssertionSet() {
    return this.assertionSet;
  }
  /**
   * Sets the assertion set.
   * @param assertionSet the assertion set
   */
  public void setAssertionSet(AsnAssertionSet assertionSet) {
    this.assertionSet = assertionSet;
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
   * Gets the fully qualified class name of the operation handler.
   * <br/>(must extend AsnOperationHandler)
   * @return the operation handler class 
   */
  public String getHandlerClass() {
    return this.handlerClass;
  }
  /**
   * Sets the fully qualified class name of the operation handler.
   * <br/>(must extend AsnOperationHandler)
   * @param handlerClass the operation handler class 
   */
  public void setHandlerClass(String handlerClass) {
    this.handlerClass = handlerClass;
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
   * Gets the predicate.
   * @return the predicate
   */
  public AsnPredicate getPredicate() {
    return this.predicate;
  }
  /**
   * Sets the predicate.
   * @param predicate the predicate
   */
  public void setPredicate(AsnPredicate predicate) {
    this.predicate = predicate;
  }
  
  /**
   * Gets the subject.
   * @return the subject
   */
  public AsnSubject getSubject() {
    return this.subject;
  }
  /**
   * Sets the subject.
   * @param subject the subject
   */
  public void setSubject(AsnSubject subject) {
    this.subject = subject;
  }
  
  /**
   * Gets the part representing system fields associated with a assertion.
   * @return the system part
   */
  public AsnSystemPart getSystemPart() {
    return this.systemPart;
  }
  /**
   * Sets the part representing system fields associated with a assertion.
   * @param systemPart the system part
   */
  public void setSystemPart(AsnSystemPart systemPart) {
    this.systemPart = systemPart;
  } 
  
  /**
   * Gets the UI resources.
   * @return the UI resources
   */
  public AsnProperty getUIResources() {
    return this.uiResources;
  }
  /**
   * Sets the UI resources.
   * @param uiResources the UI resources
   */
  public void setUIResources(AsnProperty uiResources) {
    this.uiResources = uiResources;
  }
  
  /**
   * Gets the part representing user fields associated with a assertion.
   * @return the user part
   */
  public AsnUserPart getUserPart() {
    return this.userPart;
  }
  /**
   * Sets the part representing user fields associated with a assertion.
   * @param userPart the user part
   */
  public void setUserPart(AsnUserPart userPart) {
    this.userPart = userPart;
  } 
  
  /**
   * Gets the value.
   * @return the value
   */
  public AsnValue getValue() {
    return this.value;
  }
  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(AsnValue value) {
    this.value = value;
  }

  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnOperation(this);
   * @return the duplicated object
   */
  public AsnOperation duplicate() {
    return new AsnOperation(this);
  }
  
  /**
   * Makes an operation handler based upon the associated handlerClass.
   * @param context the assertion operation context
   * @return the operation handler
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public AsnOperationHandler makeHandler(AsnContext context) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String className = Val.chkStr(this.getHandlerClass());
    if (className.length() == 0) {
      String msg = "The configured operation handlerClass was empty";
      throw new ConfigurationException(msg);
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof AsnOperationHandler) {
        return (AsnOperationHandler)obj;
      } else {
        String msg = "The configured operation handlerClass is invalid: "+ className;
        throw new ConfigurationException(msg);
      }
    }
  }
    
}
