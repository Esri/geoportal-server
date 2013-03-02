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
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;

/**
 * Options associated with a CSW operation request.
 */
public class RequestOptions {
  
  /** instance variables ====================================================== */
  private Map<String,Object>    additionalOptions = new HashMap<String,Object>();
  private CapabilityOptions     capabilityOptions = new CapabilityOptions();
  private DescribeRecordOptions describeRecordOptions = new DescribeRecordOptions();
  private QueryOptions          queryOptions = new QueryOptions();
  private Document              requestDom;
  private String                requestXml;
  private TransactionOptions    transactionOptions = new TransactionOptions();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public RequestOptions() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the free form map of additional options.
   * @return the additional options
   */
  public Map<String,Object> getAdditionalOptions() {
    return this.additionalOptions;
  }
  /**
   * Sets the free form map of additional options.
   * @param additionalOptions the additional options
   */
  public void setAdditionalOptions(Map<String,Object> additionalOptions) {
    this.additionalOptions = additionalOptions;
  }
  
  /**
   * Gets the GetCapabilities request options.
   * @return the GetCapabilities options
   */
  public CapabilityOptions getCapabilityOptions() {
    return this.capabilityOptions;
  }
  /**
   * Sets the DescribeRecord request options.
   * @param options the DescribeRecord options
   */
  public void setCapabilityOptions(CapabilityOptions options) {
    this.capabilityOptions = options;
  }
    
  /**
   * Gets the DescribeRecord request options.
   * @return the DescribeRecord options
   */
  public DescribeRecordOptions getDescribeRecordOptions() {
    return this.describeRecordOptions;
  }
  /**
   * Sets the DescribeRecord request options.
   * @param options the DescribeRecord options
   */
  public void setDescribeRecordOptions(DescribeRecordOptions options) {
    this.describeRecordOptions = options;
  }
    
  /**
   * Gets the query options.
   * <br/>Applies to the GetRecordById and GetRecords operations.
   * @return the query options
   */
  public QueryOptions getQueryOptions() {
    return this.queryOptions;
  }
  /**
   * Sets the query options.
   * <br/>Applies to the GetRecordById and GetRecords operations.
   * @param options the query options
   */
  public void setQueryOptions(QueryOptions options) {
    this.queryOptions = options;
  }
  
  /**
   * Gets the XML request document.
   * @return the XML request document (can be null)
   */
  public Document getRequestDom() {
    return this.requestDom;
  }
  /**
   * Sets the XML request document.
   * @param requestDom the XML request document
   */
  public void setRequestDom(Document requestDom) {
    this.requestDom = requestDom;
  }
    
  /**
   * Gets the request XML.
   * @return the request XML (can be null)
   */
  public String getRequestXml() {
    return this.requestXml;
  }
  /**
   * Sets the request XML.
   * @param xml the request XML (can be null)
   */
  public void setRequestXml(String xml) {
    this.requestXml = xml;
  }
          
  /**
   * Gets the transaction options.
   * <br/>Applies to the Insert, Update and Delete.
   * @return the transaction options
   */
  public TransactionOptions getTransactionOptions() {
    return this.transactionOptions;
  }
  /**
   * Sets the transaction options.
   * <br/>Applies to the Insert, Update and Delete.
   * @param options the transaction options
   */
  public void setTransactionOptions(TransactionOptions options) {
    this.transactionOptions = options;
  }
  
}
