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
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of configured assertion operations.
 */
public class AsnOperations {

  /** instance variables ====================================================== */
  private List<AsnOperation> operations = new ArrayList<AsnOperation>();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnOperations() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds a configured operation to the collection.
   * @param operation the operation to add
   */
  public void add(AsnOperation operation) {
    if (operation == null) {
      throw new IllegalArgumentException("The operation cannot be null.");
    } else if (operation.getSubject() == null) {
      throw new ConfigurationException("The operation.subject cannot be null.");
    } else if (operation.getPredicate() == null) {
      throw new ConfigurationException("The operation.predicate cannot be null.");
    } else if (operation.getAuthPolicy() == null) {
      throw new ConfigurationException("The operation.authPolicy cannot be null.");
    } 
    this.operations.add(operation);
  }
  
  
  /**
   * Makes the operation associated with the assertion request.
   * @param context the assertion operation context
   * @return the associated operation
   * @throws AsnInvalidOperationException if the subject/predicate combination was invalid
   */
  public AsnOperation makeOperation(AsnContext context) 
    throws AsnInvalidOperationException {
    AsnOperation operation = null;
    
    // determine the the operation based upon the subject and predicate
    String subjectURN = Val.chkStr(context.getRequestOptions().getSubject());
    String predicateURN = Val.chkStr(context.getRequestOptions().getPredicate());
    boolean wasSubjectRecognized = false;
    boolean wasPredicateRecognized = false;
    if (subjectURN.length() == 0) {
      throw new AsnInvalidOperationException("The subject URN was empty.");
    } else if (predicateURN.length() == 0) {
      throw new AsnInvalidOperationException("The predicate URN was empty.");
    } else {
      for (AsnOperation configuredOp: this.operations) {
        AsnSubject subject = configuredOp.getSubject();
        AsnPredicate predicate = configuredOp.getPredicate();
        if ((subject != null) && (predicate != null)) {
          boolean subjectFound = false;
          String stPfx = Val.chkStr(subject.getURNPrefix());
          if (subject.getRequiresValuePart()) {
            if (!stPfx.endsWith(":")) stPfx += ":";
            subjectFound = subjectURN.startsWith(stPfx);
          } else {
            subjectFound = subjectURN.equals(stPfx);
          }
          if (subjectFound) {
            wasSubjectRecognized = true;
            String ptUrn = Val.chkStr(predicate.getURN());
            if (predicateURN.equals(ptUrn)) {
              wasPredicateRecognized = true;
              if (subject.getRequiresValuePart()) {
                String subjValuePart = subjectURN.substring(stPfx.length());
                if (subjValuePart.length() == 0) {
                  String msg = "The value part of the subject URN was empty "+subjectURN;
                  throw new AsnInvalidOperationException(msg);
                } else {
                  operation = configuredOp.duplicate();
                  operation.getSubject().setURN(subjectURN);
                  operation.getSubject().setValuePart(subjValuePart);
                }
              } else {
                operation = configuredOp.duplicate();
                operation.getSubject().setURN(subjectURN);
              }
            }
          } else if (!wasPredicateRecognized) {
            String ptUrn = Val.chkStr(predicate.getURN());
            if (predicateURN.equals(ptUrn)) {
              wasPredicateRecognized = true;
            }
          }
        }
      }
    }
    if (operation == null) {
      if (!wasSubjectRecognized) {
        String msg = "The subject URN was not recognized "+subjectURN;
        throw new AsnInvalidOperationException(msg);
      } else if (!wasPredicateRecognized) {
        String msg = "The predicate URN was not recognized "+predicateURN;
        throw new AsnInvalidOperationException(msg);        
      } else {
        throw new AsnInvalidOperationException();        
      }
    }  else {
      return operation;
    }
  }
  
  /**
   * Returns the list of operations.
   * @return the list of operations
   */
  public List<AsnOperation> values() {
    return this.operations;
  }
    
}
