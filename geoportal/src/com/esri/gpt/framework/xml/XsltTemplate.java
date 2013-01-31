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
package com.esri.gpt.framework.xml;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;

/**
 * Repesents a compiled XSLT transformation stylesheet.
 */
public class XsltTemplate {

// class variables =============================================================
/** Class logger */
private static Logger LOGGER = Logger.getLogger(XsltTemplate.class.getName());

// instance variables ==========================================================
private String    _systemId  = "";
private Templates _templates = null;

// constructors ================================================================

/** Default constructor. */
public XsltTemplate() {}

// properties ==================================================================

/**
 * Gets the system id for the XSLT.
 * @return the system id
 */
protected final String getSystemId() {
  return _systemId;
}

/**
 * Sets the system id for the XSLT.
 * @param systemId the system id
 */
protected final void setSystemId(String systemId) {
  _systemId = Val.chkStr(systemId);
}

/**
 * Gets the compiled transformer templates.
 * @return the compiled templates
 */
protected final Templates getTemplates() {
  return _templates;
}

/**
 * Sets the compiled transformer templates.
 * @param templates compiled templates
 */
protected final void setTemplates(Templates templates) {
  _templates = templates;
}

// methods =====================================================================

/**
 * Compiles the XSLT based upon a relative resource path.
 * <br/>The resource path should be relative to the WEB-INF/classes folder. If
 * the XSLT is located at:
 * <br/><deployment folder>/WEB-INF/classes/somefolder/somefile.xslt
 * then supply a relative resource path of:
 * <br/>somefolder/somefile.xslt
 * @param resourcePath the relative resource path to XSLT file
 * @throws IOException if the is an io problem with the XSLT file
 * @throws TransformerConfigurationException if a configuration exception occurs
 */
protected void compileFromResourcePath(String resourcePath)
  throws IOException, TransformerConfigurationException {
  URL url = (new ResourcePath()).makeUrl(resourcePath);
  compileFromSystemId(url.toExternalForm());
}

/**
 * Compiles the XSLT based upon a system path.
 * <br/>systemId examples:
 * <br/>c:/somefolder/somefile.xslt
 * <br/>file:///c:/somefolder/somefile.xslt
 * @param systemId the system path to XSLT file
 * @throws TransformerConfigurationException if a configuration exception occurs
 */
protected void compileFromSystemId(String systemId)
  throws TransformerConfigurationException {
  setSystemId(systemId);
  TransformerFactory factory = TransformerFactory.newInstance();
  setTemplates(factory.newTemplates(new StreamSource(getSystemId())));
}

/**
 * Makes a compiled XSLT template based upon a relative resource path.
 * <br/>The resource path should be relative to the WEB-INF/classes folder. If
 * the XSLT is located at:
 * <br/><deployment folder>/WEB-INF/classes/somefolder/somefile.xslt
 * then supply a relative resource path of:
 * <br/>somefolder/somefile.xslt
 * @param resourcePath the relative resource path to XSLT file
 * @return the XSLT template
 * @throws TransformerConfigurationException if a configuration exception occurs
 */
public static XsltTemplate makeFromResourcePath(String resourcePath)
  throws IOException, TransformerConfigurationException {
  XsltTemplate template = new XsltTemplate();
  template.compileFromResourcePath(resourcePath);
  return template;
}

/**
 * Makes a compiled XSLT template based upon a system path.
 * <br/>systemId examples:
 * <br/>c:/somefolder/somefile.xslt
 * <br/>file:///c:/somefolder/somefile.xslt
 * @param systemId the system path to XSLT file
 * @return the XSLT template
 * @throws TransformerConfigurationException if a configuration exception occurs
 */
public static XsltTemplate makeFromSystemId(String systemId)
  throws TransformerConfigurationException {
  XsltTemplate template = new XsltTemplate();
  template.compileFromSystemId(systemId);
  return template;
}

/**
 * Makes a template by trying out various mechanisms to find the correct path
 * @param path or system id
 * @return XsltTemplate
 * @throws TransformerConfigurationException when template cannot be made
 */
public static XsltTemplate makeTemplate(String path) 
  throws TransformerConfigurationException{
  XsltTemplate template = null;
  List<Exception> exceptions = new LinkedList<Exception>();
  path = Val.chkStr(path);
  try {
    template = makeFromResourcePath(path);
    return template;
  } catch (TransformerConfigurationException e) {
    exceptions.add(e);
  } catch (IOException e) {
    exceptions.add(e);
  }
  try {
    template = makeFromSystemId(path);
    return template;
  } catch (TransformerConfigurationException e) {
    exceptions.add(e);
  } 
  if(!path.startsWith("/")) {
    path = "/" + path;
    return makeTemplate(path);
  }
  // Iterating through exceptions and printing out specific excepetions
  // before throwing a general exception
  for(Exception e : exceptions) {
    LOGGER.log(Level.SEVERE,
        "Error while making template from " + path, e);
  }
  // if code reached here then template was not made
  throw new TransformerConfigurationException 
    ("Could not make xslt template from " + path);

}

/**
* Transforms an xml document using the parameters specifiled.
* @param xml document to be transformed
* @return resulting xml document
* @throws TransformerException if an exception occurs during transformation
* @throws TransformerConfigurationException if a configuration exception occurs
*/
public String transform(String xml)
  throws TransformerException, TransformerConfigurationException {
  return transform(xml,null);
}

/**
* Transforms an xml document using the parameters specified.
* @param xml document to be transformed
* @param mapParams parameters to be used for transformation (can be null)
* @return resulting xml document
* @throws TransformerException if an exception occurs during transformation
* @throws TransformerConfigurationException if a configuration exception occurs
*/
@SuppressWarnings("unchecked")
public String transform(String xml, Map mapParams)
  throws TransformerException, TransformerConfigurationException {
  
  StringReader reader = new StringReader(xml);
  StringWriter writer = new StringWriter();
  this.transform(new StreamSource(reader), new StreamResult(writer), mapParams);
  return writer.toString();
}

/**
 * Transforms an xml document using the parameters specified.
 * 
 * @param source the source
 * @param result the result
 * @param mapParams the map params (can be null)
 * 
 * @return the result
 * 
 * @throws TransformerException the transformer exception
 * @throws TransformerConfigurationException the transformer configuration exception
 */
@SuppressWarnings("unchecked")
public Result transform(Source source, Result result, Map mapParams) 
throws TransformerException, TransformerConfigurationException {
  if (getTemplates() == null) {
    String sMsg = "The XsltTemplate has not been compiled: "+getSystemId();
    throw new TransformerConfigurationException(sMsg);
  }
  Transformer transformer = getTemplates().newTransformer();
  if (mapParams != null) {
    for (Iterator it = mapParams.entrySet().iterator();it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      transformer.setParameter(entry.getKey().toString(),entry.getValue().toString());
    }
  }
  
  transformer.transform(source,result);
  return result;
}

/**
* Transforms an xml document using the parameters specified.
* @param xsl the xsl transformation string
* @param xml document to be transformed
* @param mapParams parameters to be used for transformation (can be null)
* @return resulting xml document
* @throws TransformerException if an exception occurs during transformation
* @throws TransformerConfigurationException if a configuration exception occurs
*/
@SuppressWarnings("unchecked")
public String transform(String xsl, String xml, Map mapParams)
  throws TransformerException, TransformerConfigurationException {
  
  StringReader xslReader = new StringReader(xsl);
  TransformerFactory factory = TransformerFactory.newInstance();
  Templates templates = factory.newTemplates(new StreamSource(xslReader));
  Transformer transformer = templates.newTransformer();
  if (mapParams != null) {
    for (Iterator it = mapParams.entrySet().iterator();it.hasNext();) {
      Map.Entry entry = (Map.Entry)it.next();
      transformer.setParameter(entry.getKey().toString(),entry.getValue().toString());
    }
  }
  
  StringReader reader = new StringReader(xml);
  StringWriter writer = new StringWriter();
  StreamSource source = new StreamSource(reader);
  Result result = new StreamResult(writer);
  transformer.transform(source,result);
 
  return writer.toString();
}

}
