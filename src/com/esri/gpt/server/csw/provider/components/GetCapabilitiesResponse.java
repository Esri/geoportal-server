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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generates a CSW Capabilities response.
 */
public class GetCapabilitiesResponse implements IResponseGenerator {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GetCapabilitiesResponse.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GetCapabilitiesResponse() {}
          
  /** methods ================================================================= */
  
  /**
   * Generates the response.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void generateResponse(OperationContext context) throws Exception {
    
    // initialize
    LOGGER.finer("Generatiing csw:Capabilities response...");
    CapabilityOptions cOptions = context.getRequestOptions().getCapabilityOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    
    // TODO these URL's
    String httpContextPath = Val.chkStr(svcProps.getHttpContextPath());
    String cswBaseUrl = Val.chkStr(svcProps.getCswBaseURL());
    String cswSoapUrl = "";
    
    // load the resource document
    String loc = cOptions.getCapabilitiesLocation();
    //loc = "gpt/metadata/cswCapabilities-INSPIRE.xml";
    Document dom = DomUtil.makeDomFromResourcePath(loc,true);
    //dom.getDocumentElement().normalize();   
    
    // make an XPath for the CSW name space context
    CswNamespaces ns = new CswNamespaces();
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns.makeNamespaceContext());
    
    // update the OperationsMetadata endpoints
    NodeList nlRefs = (NodeList)xpath.evaluate("//@xlink:href",dom,XPathConstants.NODESET);
    for (int i=0; i<nlRefs.getLength(); i++) {
      Node ndRef = nlRefs.item(i);
      String href = ndRef.getNodeValue();
      Node ndParent = ((Attr)ndRef).getOwnerElement();
      String nsParent = Val.chkStr(ndParent.getNamespaceURI());
      String parentName = ndParent.getLocalName();
      if (nsParent.equals(CswNamespaces.URI_OWS)) {
        if (parentName.equals("Get") || parentName.equals("Post")) {
          if ((href == null) || !href.startsWith("http")) {
            href = Val.chkStr(href);
            if (href.length() > 0) {
              href = httpContextPath+href;
            } else {
              href = cswBaseUrl;
            }
            if (parentName.equals("Post")) {
              String val = xpath.evaluate("ows:Constraint[@name='PostEncoding']/ows:Value",ndParent);
              if (Val.chkStr(val).equalsIgnoreCase("SOAP")) {
                href = cswSoapUrl;
              }
            }
            ndRef.setNodeValue(href);
          }
        }
      }
    }
    
    // remove sections that were not requested
    StringSet sections = cOptions.getSections();
    if ((sections != null) && (sections.size() > 0)) {
      HashMap<String,String> keep = new HashMap<String,String>();
      for (String section: sections) {
        if (section.equalsIgnoreCase("ServiceIdentification")) {
          keep.put(CswNamespaces.URI_OWS+"#ServiceIdentification","");
        } else if (section.equalsIgnoreCase("ServiceProvider")) {
          keep.put(CswNamespaces.URI_OWS+"#ServiceProvider","");
        } else if (section.equalsIgnoreCase("OperationsMetadata")) {
          keep.put(CswNamespaces.URI_OWS+"#OperationsMetadata","");
        } else if (section.equalsIgnoreCase("Filter_Capabilities")) {
          keep.put(CswNamespaces.URI_OGC+"#Filter_Capabilities","");
        }
      }
      
      // always keep the filter capabilities
      keep.put(CswNamespaces.URI_OGC+"#Filter_Capabilities","");
      
      ArrayList<Node> remove = new ArrayList<Node>();
      Node root = (Node)xpath.evaluate("/csw:Capabilities",dom,XPathConstants.NODE);
      NodeList children = root.getChildNodes();
      for (int i=0;i<children.getLength();i++) {
        Node node = children.item(i);
        String namespace = node.getNamespaceURI();
        String localname = node.getLocalName();
        if (!keep.containsKey(namespace+"#"+localname)) {
          remove.add(node);
        }
      }
      for (Node node: remove) {
        try {
          node.getParentNode().removeChild(node);
        } catch (Exception e) {
          LOGGER.warning("Unable to remove section, reason= "+e.toString());
        }
      }
    }
    
    // check for a supplied language code (INSPIRE)
    String exprLang = "/csw:Capabilities/ows:ExtendedCapabilities/inspire_ds:Languages/inspire_ds:Language";
    NodeList nlLang = (NodeList)xpath.evaluate(exprLang,dom,XPathConstants.NODESET);
    if ((nlLang!= null) && (nlLang.getLength() > 0)) {
      String requestedLang = Val.chkStr(cOptions.getLanguageCode());
      String exprDef = "/csw:Capabilities/ows:ExtendedCapabilities/inspire_ds:Languages/inspire_ds:Language[@default='true']";
      String exprCur = "/csw:Capabilities/ows:ExtendedCapabilities/inspire_ds:CurrentLanguage";
      Node ndDefLang = (Node)xpath.evaluate(exprDef,dom,XPathConstants.NODE);
      Node ndCurLang = (Node)xpath.evaluate(exprCur,dom,XPathConstants.NODE);
      if ((ndDefLang != null) && (ndCurLang != null)) {
        String sDefLang = Val.chkStr(ndDefLang.getTextContent());
        String sCurLang = Val.chkStr(ndCurLang.getTextContent());
        String sRespLang = sDefLang;
        if (sRespLang.length() == 0) sRespLang = sCurLang;
        if ((requestedLang.length() > 0) && !requestedLang.equalsIgnoreCase(sRespLang)) {
          for (int i=0; i<nlLang.getLength(); i++) {
            Node ndLang = nlLang.item(i);
            String sLang = Val.chkStr(ndLang.getTextContent());
            if (requestedLang.equalsIgnoreCase(sLang)) {
              sRespLang = sLang;
              break;
            }
          }
        }
        if (sRespLang.length() > 0) {
          
          // update the current response language
          if (!sRespLang.equalsIgnoreCase(sCurLang)) {
            ndCurLang.setTextContent(sRespLang);
          }
          
          // remove nodes not associated with the response language
          NodeList nlLangRefs = (NodeList)xpath.evaluate("//@xml:lang",dom,XPathConstants.NODESET);
          ArrayList<Node> remove = new ArrayList<Node>();
          ArrayList<Attr> removeAttr = new ArrayList<Attr>();
          for (int i=0;i<nlLangRefs.getLength();i++) {
            Node node = nlLangRefs.item(i);
            if (node instanceof Attr) {
              String sLang = Val.chkStr(node.getTextContent());
              if (sRespLang.equalsIgnoreCase(sLang)) {
                removeAttr.add((Attr)node);
              } else {
                Node ndToRemode = ((Attr)node).getOwnerElement();
                remove.add(ndToRemode);
              }
            } 
          }
          for (Node node: remove) {
            try {
              node.getParentNode().removeChild(node);
            } catch (Exception e) {
              LOGGER.warning("Unable to remove non-requested language node, reason= "+e.toString());
            }
          }
          for (Attr attr: removeAttr) {
            try {
              attr.getOwnerElement().removeAttributeNode(attr);
            } catch (Exception e) {
              LOGGER.warning("Unable to remove xml:lang sttribute, reason= "+e.toString());
            }
          }
          
        }
      }
    }
             
    // set the response string
    String xml = XmlIoUtil.domToString(dom);
    context.getOperationResponse().setResponseXml(xml);  
  }
  
}
