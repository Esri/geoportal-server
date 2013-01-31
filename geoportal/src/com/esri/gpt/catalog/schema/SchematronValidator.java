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
package com.esri.gpt.catalog.schema;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;
import java.io.IOException;
import java.util.logging.Level;


import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Validates a document against a Schematron XSLT.
 * <p/>
 * The XSLT must produce an SVRL document (Schematron Validation Report Language).
 */
public class SchematronValidator {

  // class variables =============================================================
  private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
    
  // instance variables ==========================================================
  private ValidationErrors _validationErrors;
  
  // constructors ================================================================
  
  /** Default constructor. */
  public SchematronValidator() {}
  
  // properties ==================================================================
  
  /**
   * Gets the validation errors.
   * @return the validation errors
   */
  protected ValidationErrors getValidationErrors() {
    return _validationErrors;
  }
  /**
   * Sets the validation errors.
   * @param errors validation errors
   */
  protected void setValidationErrors(ValidationErrors errors) {
    _validationErrors = errors;
    if (_validationErrors == null) _validationErrors = new ValidationErrors();
  }
  
  // methods =====================================================================
  
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
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
   * Validates an XML string against a Schematron XSLT associated with a schema.
   * @param schema the schema being validated
   * @param xml the XML string to be validated
   * @throws ValidationException if validation errors were located
   */
  public void validate(Schema schema, String xml) throws ValidationException {
    setValidationErrors(new ValidationErrors());
    if (schema.getSchematronXslt().length() > 0) {  
      String schematronXslt = null;
      try {
        
        String[] tokens = schema.getSchematronXslt().split(",");
        for (String token: tokens) {
          schematronXslt = Val.chkStr(token);
          if (schematronXslt.length() == 0) continue;
          
        
          // run the validation xsl
          XsltTemplate template = this.getCompiledTemplate(schematronXslt);
          String result = template.transform(xml);
          
          // load the result SVRL document
          Document dom = DomUtil.makeDomFromString(result,true);
          Namespaces namespaces = new Namespaces();
          namespaces.add("svrl","http://purl.oclc.org/dsdl/svrl");
          NamespaceContextImpl nsc = new NamespaceContextImpl(namespaces);
          XPath xpath = XPathFactory.newInstance().newXPath();
          xpath.setNamespaceContext(nsc);
          
          // check for failed assertions
          NodeList nl = (NodeList)xpath.evaluate("//svrl:failed-assert",dom,XPathConstants.NODESET);
          for (int i=0;i<nl.getLength();i++) {
            Node nd = nl.item(i);
            String sLocation = Val.chkStr(xpath.evaluate("@location",nd));
            String sText = Val.chkStr(xpath.evaluate("svrl:text",nd));
            if (sText.length() == 0) {
              sText = "Untitled Schematron assertion failure.";
            }
            ValidationError error = new ValidationError();
            //error.location = sLocation;
            error.setMessage(sText);
            error.setReasonCode(ValidationError.REASONCODE_SCHEMATRON_VIOLATION);
            getValidationErrors().add(error);
          }
        }
        
      } catch (Exception e) {
        String sMsg = "Error executing schematron validation, schema="+
            schema.getKey()+" schematronXslt="+schematronXslt;
        LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
        ValidationError error = new ValidationError();
        error.setMessage(sMsg);
        error.setReasonCode(ValidationError.REASONCODE_SCHEMATRON_EXCEPTION);
        getValidationErrors().add(error);
        throw new ValidationException(schema.getKey(),sMsg,getValidationErrors());
      }
    }
    if (getValidationErrors().size() > 0) {
      throw new ValidationException(schema.getKey(),"Schematron violation.",getValidationErrors());
    }
  }

}
