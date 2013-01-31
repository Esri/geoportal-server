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
import com.esri.gpt.server.assertion.index.Assertion;
import java.sql.Timestamp;

/**
 * Handles the update of an assertion.
 */
public class AsnUpdateHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnUpdateHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handle an update operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
    
    // update the assertion
    AsnOperation operation = context.getOperation();
    Assertion assertion = this.getIndexAdapter().loadAssertionById(context,true);
    context.getAuthorizer().authorizeUpdate(context,assertion);
    if (!assertion.getSystemPart().getEnabled()) {
      context.getOperationResponse().generateFailedResponse(context,"The assertion is disabled.");
    } else {
      
      // the user performing the update takes over ownership,
      // update modification timestamp and modification user
      assertion.setUserPart(operation.getUserPart());
      assertion.getSystemPart().setEditTimestamp(new Timestamp(System.currentTimeMillis()));
      assertion.getRdfPart().setValue(operation.getValue().getTextValue());
      this.getIndexAdapter().index(context,assertion);
      context.getOperationResponse().generateOkResponse(context,(String)null);
    }
  }

}
