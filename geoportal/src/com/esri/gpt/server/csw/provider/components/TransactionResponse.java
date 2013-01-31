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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Logger;

/**
 * Generates a CSW Transaction response.
 */
public class TransactionResponse implements IResponseGenerator {
    
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TransactionResponse.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TransactionResponse() {
    super();
  }
          
  /** methods ================================================================= */
  
  /**
   * Generates the response.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void generateResponse(OperationContext context) throws Exception {
    
    // initialize
    LOGGER.finer("Generating csw:Transaction response...");
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
     
    String cswNamespace = "http://www.opengis.net/cat/csw/2.0.2";
    String version = "2.0.2";
    String requestId = tOptions.getRequestId();
    int numInserted = tOptions.getSummary().getTotalInserted();
    int numUpdated = tOptions.getSummary().getTotalUpdated();
    int numDeleted = tOptions.getSummary().getTotalDeleted();
    
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("\r\n<csw:TransactionResponse");
    sb.append(" xmlns:csw=\"").append(cswNamespace).append("\"");
    if ((version != null) && (version.length() > 0)) {
      sb.append(" version=\"").append(Val.escapeXml(version)).append("\"");
    }
    sb.append(">");
    sb.append("\r\n<csw:TransactionSummary");
    if ((requestId != null) && (requestId.length() > 0)) {
      sb.append(" requestId=\"").append(Val.escapeXml(requestId)).append("\"");
    }
    sb.append(">");
    sb.append("\r\n<csw:totalInserted>").append(numInserted).append("</csw:totalInserted>");
    sb.append("\r\n<csw:totalUpdated>").append(numUpdated).append("</csw:totalUpdated>");
    sb.append("\r\n<csw:totalDeleted>").append(numDeleted).append("</csw:totalDeleted>");
    sb.append("\r\n</csw:TransactionSummary>");
    
    // insert verbose csw:InsertResult records here
    
    sb.append("\r\n</csw:TransactionResponse>");
    context.getOperationResponse().setResponseXml(sb.toString()); 
  }
    
}
