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
package com.esri.gpt.catalog.arcims;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides functionality to send metadata document modification events
 * (publish/delete) to a remote CSW repository. 
 */
class CswRemoteRepository {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(CswRemoteRepository.class.getName());
  
  /** instance variables ====================================================== */
  private String cswURL = "";
  private boolean isActive = false;
  private RequestContext requestContext;
  private StringSet schemaKeys = new StringSet();
  private boolean sendDelete = true;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied request context.
   * @param requestContext the active request context
   */
  protected CswRemoteRepository(RequestContext requestContext) {
    this.requestContext = requestContext;
    StringAttributeMap params = requestContext.getCatalogConfiguration().getParameters();
    this.cswURL = Val.chkStr(params.getValue("cswRemoteRepository.url"));
    String sKeys = Val.chkStr(params.getValue("cswRemoteRepository.schemaKeys"));
    String[] aKeys = Val.tokenize(sKeys,",");
    for (String sKey: aKeys) schemaKeys.add(sKey);
    this.isActive = ((cswURL.length() > 0) && (schemaKeys.size() > 0));
    this.sendDelete = Val.chkBool(params.getValue("cswRemoteRepository.sendDelete"),true);
  }
  
  /** methods ================================================================= */
  
  /**
   * Indicates if a remote repository has been configured.
   * @return true if a remote repository has been configured.
   */
  protected boolean isActive() {
    return this.isActive;
  }
  
  /**
   * Deletes a collection of identifiers from the remote repository.
   * @param identifiers the collection of identifiers to delete.
   */
  private void delete(StringSet identifiers) throws IOException {
    
    // make and send the CSW request
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("\r\n<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"");
    sb.append(" xmlns:ogc=\"http://www.opengis.net/ogc\">");
    sb.append("\r\n<csw:Delete>");
    sb.append("\r\n<csw:Constraint version=\"1.1.0\">");
    sb.append("\r\n<ogc:Filter>");
    sb.append("\r\n<ogc:Or>");
    
    for (String identifier: identifiers) {
      sb.append("\r\n<ogc:PropertyIsEqualTo>");
      sb.append("\r\n<ogc:PropertyName>").append("dc:identifier").append("</ogc:PropertyName>");
      sb.append("\r\n<ogc:Literal>").append(identifier).append("</ogc:Literal>");
      sb.append("\r\n</ogc:PropertyIsEqualTo>");
    }
   
    sb.append("\r\n</ogc:Or>");
    sb.append("\r\n</ogc:Filter>");
    sb.append("\r\n</csw:Constraint>");
    sb.append("\r\n</csw:Delete>");
    sb.append("\r\n</csw:Transaction>");
    sendRequest(sb.toString());
  }  
  
  /**
   * Informs the remote repository of record deletions.
   * @param identifiers the file identifiers associated with the deleted records
   */
  protected void onRecordsDeleted(StringSet identifiers) {
    if (!this.isActive() || !this.sendDelete) return;
    try {
      if ((identifiers == null) || (identifiers.size() == 0)) return;
      delete(identifiers);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,"Deletion from remote service failed.",e);
    }
  }
  
  /**
   * Informs the remote repository of record publications.
   * @param schema the evaluated schema associated with the document
   * @param record the publication record
   */
  protected void onRecordUpdated(Schema schema, PublicationRecord record) {
    if (!isActive()) return;
    try {
      if (!schemaKeys.containsString(schema.getKey())) return;
      publish(schema,record.getSourceXml());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,"Publication to remote service failed.",e);
    }
  }
  
  /**
   * Informs the remote repository of record publications.
   * @param xml the raw XML associated with the record
   * @param record the publication record
   */
  protected void onRecordUpdated(String xml) {
    if (!this.isActive()) return;
    try {
      MetadataDocument mdDoc = new MetadataDocument();
      Schema schema = mdDoc.prepareForView(requestContext,xml);
      xml = mdDoc.prepareForFullViewing(xml);
      publish(schema,xml);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE,"Publication to remote service failed.",e);
    }
  }
    
  /**
   * Publishes a document to the remote repository.
   * @param schema the evaluated schema associated with the document
   * @param xml the document's XML
   * @throws IOException if an i/o exception occurs
   * @throws ParserConfigurationException if a configuration exception occurs
   * @throws SAXException if an exception occurs during XML parsing
   * @throws TransformerException if an exception occurs during XML transformation
   */
  private void publish(Schema schema, String xml) 
    throws IOException, ParserConfigurationException, SAXException, TransformerException {
    if (!schemaKeys.containsString(schema.getKey())) return;
    
    // remove the processing instruction and schema location attribute from the metadata xml
    Document dom = DomUtil.makeDomFromString(xml,true);
    for (int i=0;i<dom.getDocumentElement().getAttributes().getLength();i++) {
      Attr attr = (Attr)dom.getDocumentElement().getAttributes().item(i);
      if (attr.getLocalName().equalsIgnoreCase("schemaLocation")) {
        dom.getDocumentElement().removeAttributeNode(attr);
        break;
      }
    }
    StringWriter result = new StringWriter();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
    transformer.transform(new DOMSource(dom),new StreamResult(result));
    xml = Val.chkStr(result.toString());
    
    // make and send the CSW request
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("\r\n<csw:Transaction xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\">");
    sb.append("\r\n<csw:Insert>");
    sb.append("\r\n").append(xml);
    sb.append("\r\n</csw:Insert>");
    sb.append("\r\n</csw:Transaction>");
    sendRequest(sb.toString());
  }
  
  /**
   * Fully reads the characters from an InputStream.
   * @param strm the InputStream
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  private StringBuffer readCharacters(InputStream strm)
    throws IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader br = null;
    InputStreamReader ir = null;
    try {
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(strm, "UTF-8");
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf, 0, nLen)) > 0) {
        sb.append(cbuf, 0, n);
      }
    } finally {
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (ir != null) ir.close();} catch (Exception ef) {}
    }
    return sb;
  }
  
  /**
   * Sends data to the open HTTP connection.
   * @param httpCon the HTTP connection
   * @param data the data to send
   * @throws IOException if the send fails
   */
  private void sendData(HttpURLConnection httpCon, String data)
    throws IOException {
    OutputStream sendStream = null;
    try {
      httpCon.setRequestProperty("Content-Type","text/xml; charset=UTF-8");
      httpCon.setRequestProperty("Content-Length",""+data.length());
      sendStream = httpCon.getOutputStream();
      sendStream.write(data.getBytes("UTF-8"));
      sendStream.flush();
    } finally {
      try {
        if (sendStream != null) {
          sendStream.close();
        }
      } catch (Exception ef) {
      }
    }
  }
  
  /**
   * Sends a request to the CSW end-point.
   * @param cswRequest the request to send
   * @throws IOException if an exception occurs
   */
  private void sendRequest(String cswRequest) throws IOException {
    HttpURLConnection httpCon = null;
    InputStream responseStream = null;
    try {   
      if (LOGGER.isLoggable(Level.FINER)) {
        StringBuffer sb = new StringBuffer();
        sb.append("Sending CSW request\n");
        sb.append(" url=").append(cswURL);
        sb.append("\n").append(cswRequest);
        LOGGER.finer(sb.toString());
      }
      
      //if (true) return;
      
      URL url = new URL(cswURL);
      httpCon = (HttpURLConnection) url.openConnection();
      httpCon.setRequestMethod("POST");
      httpCon.setConnectTimeout(10000);
      httpCon.setDoInput(true);
      httpCon.setDoOutput(true);
      httpCon.setUseCaches(false); // turn off document caching
      httpCon.setRequestProperty("Connection", "Close"); // Disable keep-alive
      
      sendData(httpCon,cswRequest);
      int nResponseCode = httpCon.getResponseCode();
      
      if (LOGGER.isLoggable(Level.FINER)) {
        responseStream = httpCon.getInputStream();
        String sResponse = readCharacters(responseStream).toString();  
        StringBuffer sb = new StringBuffer();
        sb.append("Read CSW response\n");
        sb.append(" url=").append(cswURL);
        sb.append(" responseCode=").append(nResponseCode);
        sb.append("\n").append(sResponse);
        LOGGER.finer(sb.toString());
      }
      
    } finally {
      try {if (responseStream != null) responseStream.close();} catch (Exception ef) {}
    }
  }

}
