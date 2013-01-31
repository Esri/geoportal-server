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
package com.esri.gpt.control.search.browse;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * Holds context properties and summary information for a build toc request.
 */
public class TocContext {
  
  /** instance variables ====================================================== */
  private TocIndexAdapter indexAdapter = new TocIndexAdapter();
  private MessageBroker   messageBroker;
  private String          outputFormat;
	private RequestContext  requestContext;
	private String          subjectResourceId;
	
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TocContext() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the Lucene index adapter.
   * @return the index adapter.
   */
  public TocIndexAdapter getIndexAdapter() {
    return this.indexAdapter;
  }
  
  /**
   * Gets the resource bundle message broker.
   * @return the message broker (can be null)
   */
  public MessageBroker getMessageBroker() {
    return this.messageBroker;
  }
  
  /**
   * Sets the resource bundle message broker.
   * @param messageBroker the message broker
   */
  public void setMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
  }
  
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
   * Gets the underlying request context.
   * @return the request context
   */
  public RequestContext getRequestContext() {
    return this.requestContext;
  }
  /**
   * Sets the underlying request context.
   * @param requestContext the request context
   */
  public void setRequestContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }
  
  /**
   * Gets the subject resource identifier (if applicable).
   * @return the resource id
   */
  public String getSubjectResourceID() {
    return this.subjectResourceId;
  }
  
  /**
   * Sets the subject resource identifier (if applicable).
   * @param id the resource id
   */
  public void setSubjectResourceID(String id) {
    this.subjectResourceId = id;
  }

}
