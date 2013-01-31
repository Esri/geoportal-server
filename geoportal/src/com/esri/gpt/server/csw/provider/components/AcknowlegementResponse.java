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
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.sql.Timestamp;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Generates a CSW GetRecords acknowlegement response (resultType=validate).
 */
public class AcknowlegementResponse implements IResponseGenerator {
    
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AcknowlegementResponse.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AcknowlegementResponse() {
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
    LOGGER.finer("Generating csw:Acknowledgement response...");
    Document requestDom = context.getRequestOptions().getRequestDom();
    OperationResponse response = context.getOperationResponse();
    
    // make the root node
    Element root = response.newResponseDom("Acknowledgement");
    Document responseDom = response.getResponseDom();
    String sTimestamp = response.toIso8601(new Timestamp(System.currentTimeMillis()));
    root.setAttribute("timeStamp", sTimestamp);
      
    // echo the request, complete the response
    Element elEcho = responseDom.createElementNS(CswNamespaces.URI_CSW,"EchoedRequest");
    Node ndRequest = requestDom.getFirstChild().cloneNode(true);
    elEcho.appendChild(responseDom.adoptNode(ndRequest));
    root.appendChild(elEcho);
    response.setResponseXml(XmlIoUtil.domToString(responseDom));
  }
    
}
