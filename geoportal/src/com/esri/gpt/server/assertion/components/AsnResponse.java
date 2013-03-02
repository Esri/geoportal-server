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
import java.io.IOException;

/**
 * Represents an assertion operation response.
 */
public class AsnResponse {

  /** instance variables ====================================================== */
  private String      outputFormat;
  private AsnProperty responseProperty;
  private String      responseString;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnResponse() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the HTTP response MIME type.
   * @return the response MIME type
   */
  public String getMimeType() {
    String fmt = Val.chkStr(this.getOutputFormat());
    if (fmt.equalsIgnoreCase("json") || fmt.equalsIgnoreCase("application/json")) {
      return "application/json";
    } else if (fmt.equalsIgnoreCase("pjson")) {
      return "text/plain";
    } else if (fmt.equalsIgnoreCase("text/plain")) {
      return "text/plain";
    } else {
      return "application/xml";
    }
  }
  
  /**
   * Gets the output format.
   * @return the output format (can be null)
   */
  public String getOutputFormat() {
    return this.outputFormat;
  }
  /**
   * Sets the HTTP output format (MIME type).
   * @param outputFormat the output format
   */
  public void setOutputFormat(String outputFormat) {
    this.outputFormat = outputFormat;
  }
    
  /**
   * Gets the root property for the response.
   * @return the root response property
   */
  public AsnProperty getResponseProperty() {
    return this.responseProperty;
  }
  /**
   * Sets the root property for the response.
   * @param responseProperty the root response property
   */
  public void setResponseProperty(AsnProperty responseProperty) {
    this.responseProperty = responseProperty;
  }
  
  /**
   * Gets the response string.
   * @return the response string (can be null)
   */
  public String getResponseString() {
    return this.responseString;
  }
  /**
   * Sets the response string.
   * @param response the response string
   */
  public void setResponseString(String response) {
    this.responseString = response;
  }
  
  /** methods ================================================================= */
  
  /**
   * Generates an exception based operation response.
   * @param context the assertion operation context
   * @param e the exception
   * @throws IOException if an I/O exception occurs
   */
  public void exceptionToResponse(AsnContext context, Exception e) throws IOException {
    String message = Val.chkStr(e.getMessage());
    if (message.length() == 0) message = e.toString();
    this.generateFailedResponse(context,message);
  }
  
  /**
   * Generates a failed operation response.
   * @param context the assertion operation context
   * @param message the message (RDF value, can be null)
   * @throws IOException if an I/O exception occurs
   */
  public void generateFailedResponse(AsnContext context, String message) throws IOException {
    if (message != null) {
      message = message.trim();
      if (message.length() == 0) message = null;
    }
    String subject = AsnConstants.OPERATION_EXCEPTION;
    String predicate = AsnConstants.OPERATION_STATUS_FAILED;
    AsnProperty response = new AsnProperty(subject,predicate,message);
    this.setResponseProperty(response);
    this.propertyToResponseString();
  }
  
  /**
   * Generates an OK operation response.
   * @param context the assertion operation context
   * @param message the message (RDF value, can be null)
   * @throws IOException if an I/O exception occurs
   */
  public void generateOkResponse(AsnContext context, String message) throws IOException {
    message = Val.chkStr(message);
    if (message.length() == 0) {
      message = AsnConstants.OPERATION_STATUS_OK;
    }
    AsnOperation operation = context.getOperation();
    String subject = operation.getSubject().getURN();
    String predicate = operation.getPredicate().getURN()+":response";
    AsnProperty response = new AsnProperty(subject,predicate,message);
    this.setResponseProperty(response);
    this.propertyToResponseString();
  }
  
  /**
   * Generates an operation response.
   * @param context the assertion operation context
   * @param body a property representing the body of the response
   * @throws IOException if an I/O exception occurs
   */
  public void generateResponse(AsnContext context, AsnProperty body) throws IOException {
    AsnOperation operation = context.getOperation();
    String subject = operation.getSubject().getURN();
    String predicate = operation.getPredicate().getURN()+":response";
    AsnProperty response = new AsnProperty(subject,predicate,null);
    this.setResponseProperty(response);
    if (body != null) {
      response.getChildren().add(body);
    }
    this.propertyToResponseString();
  }
  
  /**
   * Generates an operation response.
   * @param context the assertion operation context
   * @param body a properties representing the body of the response
   * @throws IOException if an I/O exception occurs
   */
  public void generateResponse(AsnContext context, AsnProperties body) throws IOException {
    AsnOperation operation = context.getOperation();
    String subject = operation.getSubject().getURN();
    String predicate = operation.getPredicate().getURN()+":response";
    AsnProperty response = new AsnProperty(subject,predicate,null);
    this.setResponseProperty(response);
    if (body != null) {
      response.setChildren(body);
    }
    this.propertyToResponseString();
  }
  
  /**
   * Generates a response string from the active response property
   * @throws IOException if an I/O exception occurs
   */
  public void propertyToResponseString() throws IOException {
    AsnProperty prop = this.getResponseProperty();
    if (prop != null) {
      String fmt = Val.chkStr(this.getOutputFormat());
      boolean isJson = fmt.equalsIgnoreCase("json") || 
                       fmt.equalsIgnoreCase("pjson")  ||
                       fmt.equalsIgnoreCase("application/json");
      if (isJson) {
        this.setResponseString(prop.asJson());
      } else {
        this.setResponseString(prop.asXml());
      }
    }
  }
  
}
