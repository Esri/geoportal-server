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
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnProperty;
import com.esri.gpt.server.assertion.components.AsnUIResource;

/**
 * Handles operations associated with UI resources.
 */
public class AsnUIResourcesHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnUIResourcesHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handles an assertion operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
        
    // initialize
    AsnOperation operation = context.getOperation();
    AsnProperty property = operation.getUIResources();
    context.getAuthorizer().authorizeQuery(context);
      
    // get the message broker
    MessageBroker msgBroker = context.getMessageBroker();
    if (msgBroker == null) {
      msgBroker = new MessageBroker();
      msgBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
    }

    // update each resource, generate the response
    for (AsnProperty child: property.getChildren()) {
      if (child instanceof AsnUIResource) {
        AsnUIResource uiResource = (AsnUIResource)child;
        String resourceKey = Val.chkStr(uiResource.getResourceKey());
        if (resourceKey.length() > 0) {
          String resourceValue = msgBroker.retrieveMessage(resourceKey);
          uiResource.setResourceValue(resourceValue);
          //System.err.println(resourceKey+" = "+uiResource.getDefaultValue());
        }
      }
    }
    context.getOperationResponse().generateResponse(context,property.getChildren());
  }

}
