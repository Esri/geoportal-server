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
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Merges the XML documents associated with a set of keys into a single 
 * document, applies an XSLT, then returns the response.
 */
public class MergeThenTransform extends KeysetProcessor {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public MergeThenTransform() {}
  
  /** methods ================================================================= */
    
  /**
   * Appends an XML attribute value.
   * @param xml the XML being constructed
   * @param name the attribute name
   * @param value the attribute value
   */
  protected StringBuilder appendXmlAttribute(StringBuilder xml, 
                                             String name, 
                                             String value) {
    String s = Val.chkStr(Val.escapeXml(value));
    xml.append(" ").append(name).append("=\"").append(s).append("\"");
    return xml;
  }
  
  /**
   * Appends an XML property element.
   * <br/>&lt;property name="[name]"/&gt;[value]&lt;/property&gt;
   * @param xml the XML being constructed
   * @param name the property name
   * @param value the property value
   */
  protected StringBuilder appendPropertyElement(StringBuilder xml, 
                                                String nl,
                                                String name, 
                                                String value) {
    String s = Val.chkStr(Val.escapeXml(value));
    xml.append(nl).append("<property");
    xml.append(" ").append("name").append("=\"").append(name).append("\">");
    xml.append(s).append("</property>");
    return xml;
  }
  
  /**
   * Appends an XML property element based upon a gpt.xml 
   * configuration parameter.
   * <br/>&lt;property name="[name]"/&gt;[value]&lt;/property&gt;
   * @param cfgParams the configuration parameters
   * @param xml the XML being constructed
   * @param nl the new line prefix
   * @param cfgKey the configuration parameter key
   */
  protected StringBuilder appendPropertyElement(StringAttributeMap cfgParams, 
                                                StringBuilder xml, 
                                                String nl, 
                                                String cfgKey) {
    if (cfgKey.indexOf("*") == -1) {
      this.appendPropertyElement(xml,nl,cfgKey,cfgParams.getValue(cfgKey));
    } else if (cfgKey.equals("*")) {
      for (StringAttribute attr: cfgParams.values()) {
        String sKey = attr.getKey();
        this.appendPropertyElement(xml,nl,sKey,cfgParams.getValue(sKey));
      }
    } else {
      String[] parts = cfgKey.split("\\*");
      for (StringAttribute attr: cfgParams.values()) {
        String sKey = attr.getKey();
        boolean bMatches = false;
        for (String sPart: parts) {
          int nIdx = sKey.indexOf(sPart);
          if (nIdx == -1) {
            bMatches = false;
            break;
          } else {
            bMatches = true;
            sKey = sKey.substring(nIdx+sPart.length());
          }
        }
        if (bMatches) {
          sKey = attr.getKey();
          this.appendPropertyElement(xml,nl,sKey,cfgParams.getValue(sKey));
        }
      } 
    }
    return xml;
  }
    
  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  public void execute(HttpServletRequest request,
                      HttpServletResponse response, 
                      RequestContext context) 
    throws Exception {
    String[] keys = this.readKeys(request,context,true);
    
    //String sXsltPath = Val.chkStr(request.getParameter("xslt"));
    //String sMimeType = Val.chkStr(request.getParameter("mimeType"));
    //String sContentDisposition = Val.chkStr(request.getParameter("contentDisposition"));
    
    StringAttributeMap cfgParams = context.getCatalogConfiguration().getParameters();    
    String sCfgPfx = "catalog.cart.processor.mergeThenTransform";
    String sXsltPath = Val.chkStr(
        cfgParams.getValue(sCfgPfx+".xslt"));
    String sProperties = Val.chkStr(
        cfgParams.getValue(sCfgPfx+".xslt.properties"));
    String sMimeType = Val.chkStr(
        cfgParams.getValue(sCfgPfx+".response.mimeType"));
    String sContentDisposition = Val.chkStr(
        cfgParams.getValue(sCfgPfx+".response.contentDisposition"));
    
    if ((keys.length > 0) && (sXsltPath.length() > 0)) {
      XsltTemplate template = this.getCompiledTemplate(sXsltPath);
      ServletOutputStream out = response.getOutputStream(); 
      
      if (sProperties.length() == 0) {
        sProperties = "catalog.cart.*";
      }
      
      if (sMimeType.length() == 0) {
        sMimeType = "text/plain";
      }
      response.setContentType(sMimeType+";charset=UTF-8"); 
      if (sContentDisposition.length() > 0) {
        response.addHeader("Content-Disposition",sContentDisposition);
      }
      
      String nl = "\r\n";
      try {   
        StringBuilder sbXmls = new StringBuilder();
        for (String sKey: keys) {
          String sXml = Val.chkStr(this.readXml(request,context,sKey));
          if (sXml.startsWith("<?xml ")) {
            sXml = Val.chkStr(sXml.substring(sXml.indexOf("?>") + 2));
          }
          if (sXml.length() > 0) {
            sbXmls.append(nl).append(sXml);
          }
        }
        
        if (sbXmls.length() > 0) {
          StringBuilder sb = new StringBuilder();
          sb.append("<collection>");
          
          // append client properties
          sb.append(nl).append("<client");
          appendXmlAttribute(sb,"session-id",request.getSession(true).getId());
          appendXmlAttribute(sb,"session-rid",request.getRequestedSessionId());
          appendXmlAttribute(sb,"remote-ip",request.getRemoteAddr());
          appendXmlAttribute(sb,"remote-host",request.getRemoteHost());
          appendXmlAttribute(sb,"remote-user",context.getUser().getName());
          appendXmlAttribute(sb,"user-agent",request.getHeader("User-Agent"));
          sb.append("/>");
                      
          // append configuration properties
          sb.append(nl).append("<properties>");
          this.appendPropertyElement(cfgParams,sb,nl,sProperties);
          sb.append(nl).append("</properties>");
          
          // append the XML records
          sb.append(nl).append("<records>");
          sb.append(sbXmls);
          sb.append(nl).append("</records>");
          sb.append(nl).append("</collection>");
          
          // transform then return the response
          boolean bTransform = true;
          if (bTransform) {
            String sResult = Val.chkStr(template.transform(sb.toString()));
            if (sResult.length() > 0) {
              byte[] bytes = sResult.getBytes("UTF-8");
              out.write(bytes);
              out.flush();
            }
          } else {
            byte[] bytes = sb.toString().getBytes("UTF-8");
            out.write(bytes);
            out.flush();
          }
        }
      } finally {
        out.flush();
        out.close();
      }
    }
  }

}
