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
import com.esri.gpt.server.assertion.AsnConfig;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnValueType;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generates the API associated with configured operations.
 */
public class AsnApiHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnApiHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handles an assertion operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
        
    // initialize
    context.getAuthorizer().authorizeQuery(context);
    AsnConfig config = context.getAssertionFactory().getConfiguration();
    PrintWriter writer = null;
    context.getOperationResponse().setOutputFormat("text/plain");
    try {
      String msg;
      StringWriter sw = new StringWriter();
      writer = new PrintWriter(sw);
      
      // header
      writer.println("Assertion Operations");
      writer.println("  enabled="+config.getAreAssertionsEnabled());
      writer.println();
      writer.flush();
      
      // request patterns
      writer.println("Service Request Patterns");
      msg = "[contextPath]/assertion/operations";
      writer.println("  "+msg);
      msg = "[contextPath]/assertion?s=[subject]&p=[predicate]&v=[value]&f=[format]";
      msg += "&start=[start]&max=[max]";
      writer.println("  "+msg);
      writer.println();
      writer.flush();
      
      // request parameters
      writer.println("Service Request Parameters");
      msg = "  s=[subject]   - always required";
      writer.println(msg);
      msg = "  p=[predicate] - always required";
      writer.println(msg);
      msg = "  v=[value]     - only required for operations that have a defined valueType,";
      writer.println(msg);
      msg = "                  a value can be posted in the HTTP request body";
      writer.println(msg);
      msg = "  f=[format]    - response format (optional, xml|json|pjson)";
      writer.println(msg);
      msg = "  start=[start] - starting record";
      msg += " (optional, for queries that return multiple records)";
      writer.println(msg);      
      msg = "  max=[max]     - max records to return";
      msg += " (optional, for queries that return multiple records)";
      writer.println(msg);
      writer.println();
      writer.flush();
      
      // list all operations
      for (AsnOperation op: config.getOperations().values()) {
        String subject = op.getSubject().getURNPrefix();
        if (op.getSubject().getRequiresValuePart()) {
          subject = subject+":[identifier]";
        }
        String predicate = op.getPredicate().getURN();
        writer.println("Operation");
        writer.println("  subject="+subject);
        writer.println("  predicate="+predicate);
        
        // value type
        AsnValueType vType = null;
        if (op.getValue() != null) vType = op.getValue().getValueType();
        if ((vType != null) && vType.getRequired()) {
          String vtName = vType.getValueTypeName();
          if (vtName.equals(AsnValueType.VALUE_TYPENAME_FREE)) {
            writer.println("  valueType="+vtName+", maxLength="+vType.getMaxCharacters());
           } else if (vtName.equals(AsnValueType.VALUE_TYPENAME_CONSTRAINED)) {
            if (vType.getSupportedValues() != null) {
              writer.println("  valueType="+vtName);
              for (String supported: vType.getSupportedValues().values()) {
                writer.println("    supportedValue="+supported);
              }
            }
          }
        }
        
        // index reference
        if (op.getIndexReference() != null) {
          writer.println("  indexReference="+op.getIndexReference().getName());
        }
        
        // authentication required
        boolean authRequired = op.getAuthPolicy().getAuthenticationRequired();
        writer.println("  authenticationRequired="+authRequired);
        writer.println();
        writer.flush();
      }
      
      writer.flush();
      context.getOperationResponse().setResponseString(sw.toString());
    } finally {
      try {if (writer != null) writer.close();} catch (Exception ef) {}
    }
    
  }

}
