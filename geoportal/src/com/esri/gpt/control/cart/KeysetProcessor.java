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
package com.esri.gpt.control.cart;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.local.OriginalXmlProvider;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Processes the documents associated with a set of keys.
 */
public abstract class KeysetProcessor {
  
  /** class variables ========================================================= */
  protected static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public KeysetProcessor() {}
  
  /** methods ================================================================= */
 
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   * @throws SAXException if a SAX parsing exception occurs
   */
  protected synchronized XsltTemplate getCompiledTemplate(String xsltPath)
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
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @param context the request context
   * @throws Exception if an exception occurs
   */
  public abstract void execute(HttpServletRequest request,
                               HttpServletResponse response, 
                               RequestContext context) throws Exception;

  
  /**
   * Instantiates a new keyset processor based upon the "processor"
   * parameter supplied to the HTTP request.
   * @param request the HTTP request
   * @param context the request context
   * @return the processor
   * @throws Exception if an exception occurs
   */
  public static KeysetProcessor newProcessor(HttpServletRequest request, 
      RequestContext context) throws Exception {
    String sClassName = Val.chkStr(request.getParameter("processor"));
    if (sClassName.length() == 0) {
      StringAttributeMap cfg = context.getCatalogConfiguration().getParameters();
      sClassName = Val.chkStr(cfg.getValue("catalog.cart.processor"));
    }
    if (sClassName.length() > 0) {
      Class<?> cls = Class.forName(sClassName);
      Object obj = cls.newInstance();
      if (obj instanceof KeysetProcessor) {
        return (KeysetProcessor)obj;
      } else {
        throw new Exception(sClassName+" is not a KeysetProcessor");
      }
    } else {
      throw new Exception("A processor parameter was no supplied.");
    }
  }
  
  /**
   * Reads the keys parameter for the request.
   * @param request the HTTP servlet request
   * @param context the request context
   * @param useCartMaximum if true throw an exception if the the number of keys exceeds the maximum
   * @return the keys
   * @throws Exception if an exception occurs
   */
  public String[] readKeys(HttpServletRequest request, RequestContext context, boolean useCartMaximum) 
    throws Exception {
    String[] keys = Val.chkStr(request.getParameter("keys")).split(",");
    if (useCartMaximum && (keys.length > 0)) {
      StringAttributeMap cfg = context.getCatalogConfiguration().getParameters();
      int nMax = Val.chkInt(cfg.getValue("catalog.cart.maxItems"),10);
      if (keys.length > nMax) {
        throw new Exception("Too many keys.");
      }
    }
    return keys;
  }
  
  /**
   * Reads the metadata XML for an item.
   * @param request the HTTP servlet request
   * @param context the request context
   * @param id the document id
   * @return the XML
   * @throws Exception if an exception occurs
   */
  public String readXml(HttpServletRequest request, 
                        RequestContext context, 
                        String id) 
    throws Exception {
    OperationContext opContext = new OperationContext();
    opContext.setRequestContext(context);
    OriginalXmlProvider xmlProvider = new OriginalXmlProvider(); 
    return xmlProvider.provideOriginalXml(opContext,id);
  }

}
