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
package com.esri.gpt.server.assertion.handler;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.assertion.index.Assertion;

/**
 * Handles the creation of an assertion.
 */
public class AsnCreateHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnCreateHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handle a create operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
                 
    // if multiple assertions per user per user/subject/predicate are not allowed,
    // throw an exception if a previous assertion exists
    AsnOperation operation = context.getOperation();
    boolean multipleAllowed = operation.getAuthPolicy().getMultiplePerUserSubjectPredicate();
    if (!multipleAllowed) {
      Assertion existing = this.getIndexAdapter().loadPreviousUserAssertion(context);
      if (existing != null) {
        String msg = "An assertion for this subject, predicate and user already exists.";
        throw new AsnInvalidOperationException(msg);
      }
    } 
        
    // index the assertion
    Assertion assertion = operation.getAssertionSet().newAssertion(context,true);
    context.getAuthorizer().authorizeCreate(context,assertion);
    this.getIndexAdapter().index(context,assertion);
    
    // return the new assertion subject ID
    String asnPfx = operation.getAssertionSet().getAssertionIdPrefix();
    String msg = asnPfx+":"+assertion.getSystemPart().getAssertionId();
    context.getOperationResponse().generateOkResponse(context,msg);
  }

}
