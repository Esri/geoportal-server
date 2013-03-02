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
import com.esri.gpt.framework.xml.DomUtil;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents an operation response.
 */
public class OperationResponse {

  /** instance variables ====================================================== */
  private String   outputFormat;
  private Document responseDom;
  private String   responseXml;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public OperationResponse() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the HTTP output format (MIME type).
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
   * Gets the response XML.
   * @return the response XML (can be null)
   */
  public String getResponseXml() {
    return this.responseXml;
  }
  /**
   * Sets the response XML.
   * @param xml the response XML
   */
  public void setResponseXml(String xml) {
    this.responseXml = xml;
  }
  
  /**
   * Gets the XML response document under construction.
   * @return the XML response document (can be null)
   */
  public Document getResponseDom() {
    return this.responseDom;
  }
  /**
   * Sets the XML response document under construction.
   * @param responseDom the XML response document
   */
  public void setResponseDom(Document responseDom) {
    this.responseDom = responseDom;
  }
  
  /** methods ================================================================= */
    
  /**
   * Creates and appends the root element to the XML document.
   * @param rootName the name of the root element
   * @return the root element
   */
  private Element appendRootElement(String rootName) {
    if (!rootName.startsWith("csw:")) {
      rootName = "csw:"+rootName;
    }
    Element root = this.getResponseDom().createElementNS(CswNamespaces.URI_CSW,rootName);
    root.setAttribute("xmlns:csw",CswNamespaces.URI_CSW);
    root.setAttribute("xmlns:dc", CswNamespaces.URI_DC);
    root.setAttribute("xmlns:dct",CswNamespaces.URI_DCT);
    root.setAttribute("xmlns:gml",CswNamespaces.URI_GML);
    root.setAttribute("xmlns:ows",CswNamespaces.URI_OWS);
    root.setAttribute("xmlns:dcmiBox",CswNamespaces.URI_dcmiBox);
    root.setAttribute("xmlns:xsd",CswNamespaces.URI_XSD);
    this.getResponseDom().appendChild(root);
    return root;
  }
    
  /**
   * Creates a new XML document for response construction.
   * @return the XML response document
   * @throws Exception if the document fails during creation
   */
  public Document newResponseDom() throws Exception {
    return DomUtil.newDocument();
  }
  
  /**
   * Creates a new XML document for response construction.
   * @param rootName the name of the root element
   * @return the root element
   * @throws DiscoveryException if the document fails to create
   */
  public Element newResponseDom(String rootName) throws Exception {
    this.setResponseDom(this.newResponseDom());
    return appendRootElement(rootName);
  }
  
  /**
   * Converts a Timestamp to ISO-8601 format.
   * @param timestamp the timestamp
   * @return the formatted result
   */
  public String toIso8601(Timestamp timestamp) {
    String sTimestamp = "";
    if (timestamp != null) {
      SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
      sTimestamp = ISO8601FORMAT.format(timestamp);
      sTimestamp = sTimestamp.substring(0,sTimestamp.length()-2)
                   + ":" + sTimestamp.substring(sTimestamp.length()-2);
    }
    return sTimestamp;
  }
  
  /**
   * Converts a Timestamp to ISO-8601 Date format.
   * @param timestamp the timestamp
   * @return the formatted result
   */
  public String toIso8601Date(Timestamp timestamp) {
    String sTimestamp = "";
    if (timestamp != null) {
      SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'Z'");
      sTimestamp = ISO8601FORMAT.format(timestamp);  
    }
    return sTimestamp;
  }
  
}
