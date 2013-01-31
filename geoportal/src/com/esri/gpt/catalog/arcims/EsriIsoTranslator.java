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
import com.esri.gpt.framework.security.codec.Base64;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Handles the translation of documents to EsriIso format.
 * <p/>
 * When a schema is neither FGDC or ISO19115, the document must
 * be translated to ISO19115 and enclosed as a binary node prior
 * to publishing to the ArcIMS metdata server.
 * <p/>
 * The translation process uses a Java implementation of a metadata
 * document transformation mechanism developed for ArcGIS Desktop.
 * <p/>
 * The underlying files for the process are stored within:
 * gpt/metadata/ArcGIS_Translator
 * <p/>
 * The API for translation is found within esri-translator.jar.
 */
public class EsriIsoTranslator {
  
// class variables =============================================================
private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();

// instance variables ==========================================================
  
// constructors ================================================================

/** Default constructor. */
public EsriIsoTranslator() {}
    
// properties ================================================================== 
  
// methods =====================================================================
  
/**
 * Gets a compiled XSLT template.
 * @param xsltPath the path to an XSLT
 * @return the compiled template
 * @throws IOException if an IO exception occurs
 * @throws TransformerException if a transformation exception occurs
 * @throws SAXException if a SAX parsing exception occurs
 */
private synchronized XsltTemplate getCompiledTemplate(String xsltPath)
  throws TransformerException {
  String sKey = xsltPath;
  XsltTemplate template = XSLTTEMPLATES.get(sKey);
  if (template == null) {
    template = XsltTemplate.makeTemplate(xsltPath);
    XSLTTEMPLATES.put(sKey,template);
  }
  return template;
}
  

/**
 * Inserts a document enclosure into an EsriIso document.
 * @param dom the EsriIso document
 * @param originalXml the original XML for the document to enclose
 * @throws IOException if an IO exception occurs
 */
private void insertEnclosure(Document dom, String originalXml) 
  throws IOException {
  
  /*
  Structure for an enclosed document:
  <metadata>
    <Binary>
      <Enclosure>
        <Data SourceMetadata="yes" 
              OriginalFileName="source_metadata.xml" 
              SourceMetadataDigest="0e6a7512e472b810acb569c1714fdc" 
              EsriPropertyType="Base64">base64</Data>
      </Enclosure>
    </Binary>
  </metadata>
  
  Other possible Data attributes:
    enclosure="process" 
    SourceMetadataDocId="{//gmd:fileIdentifier[1]/gco:CharacterString}"
  */
  
  // ensure the Binary node
  Node ndRoot = dom.getDocumentElement();
  Node ndBinary = DomUtil.findFirst(ndRoot,"Binary");
  if (ndBinary == null) {
    ndBinary = dom.createElement("Binary");
    ndBinary = ndRoot.appendChild(ndBinary);
  }
  
  // ensure the Enclosure node
  Node ndEnclosure = DomUtil.findFirst(ndBinary,"Enclosure");
  if (ndEnclosure == null) {
    ndEnclosure = dom.createElement("Enclosure");
    ndEnclosure = ndBinary.appendChild(ndEnclosure);
  }
  
  // remove an existing Data node, recreate the Data node
  Node ndData = DomUtil.findFirst(ndEnclosure,"Data");
  if (ndData != null) {
    ndEnclosure.removeChild(ndData);
  }
  ndData = dom.createElement("Data");
  ndData = ndEnclosure.appendChild(ndData);
 
  // set attributes
  Attr attr;
  
  // property type
  attr = dom.createAttribute("EsriPropertyType");
  attr.setValue("Base64");
  ndData.getAttributes().setNamedItem(attr);
  

  // MD5 hash of the original xml for the document,
  // this is not yet implemented
  /*
  String sMd5Hash = "";
  try {
    MessageDigest md;
    md = MessageDigest.getInstance("MD5");
    md.update(originalXml.getBytes("UTF-8"));
    //String sTmp = new String(md.digest(),"UTF-8");
    //System.err.println(sTmp);
  } catch (NoSuchAlgorithmException e) {
    // should never happen
    e.printStackTrace(System.err);
  } catch (UnsupportedEncodingException e) {
    // should never happen
    e.printStackTrace(System.err);
  }
  if ((sMd5Hash != null) && (sMd5Hash.length() > 0)) {
    attr = dom.createAttribute("SourceMetadataDigest");
    attr.setValue(sMd5Hash);
    ndData.getAttributes().setNamedItem(attr);
  }
  */
  
  // file name
  attr = dom.createAttribute("OriginalFileName");
  attr.setValue("source_metadata.xml");
  ndData.getAttributes().setNamedItem(attr);
  
  // source metadata flag
  attr = dom.createAttribute("SourceMetadata");
  attr.setValue("yes");
  ndData.getAttributes().setNamedItem(attr);
 
  // add the text node
  String sBase64 = Base64.encode(originalXml,null);
  Node ndText = dom.createTextNode(sBase64);
  ndText = ndData.appendChild(ndText);
}

/**
 * Transforma an original XML string to EsriIso format, then
 * encloses the original XML as a base64 node with the EsriIso document.
 * @param uuid the document UUID
 * @param fileIdentifier the file identifier
 * @param originalXml the original XML string for the metadata document
 * @param toEsriIsoXslt the path to an XSLT that generates the EsriIso document
 * @return the EsriIso document
 * @throws TransformerException if a transformation exception occurs
 * @throws ParserConfigurationException if a configuration exception occurs
 * @throws SAXException if an exception occurs during XML parsing
 * @throws IOException if an i/o exception occurs
 */
public String transformAndEnclose(String uuid,
                                  String fileIdentifier,
                                  String originalXml, 
                                  String toEsriIsoXslt)
  throws TransformerException, ParserConfigurationException, SAXException, IOException {
  HashMap<String,String> params = new HashMap<String,String>();
  params.put("documentUuid",Val.chkStr(uuid));
  params.put("fileIdentifier",Val.chkStr(fileIdentifier));
  XsltTemplate template = getCompiledTemplate(toEsriIsoXslt);
  String sXml = template.transform(originalXml,params);
  Document dom = DomUtil.makeDomFromString(sXml,false);
  insertEnclosure(dom,originalXml);
  return XmlIoUtil.domToString(dom);
}

}
