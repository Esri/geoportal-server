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
package com.esri.gpt.catalog.gxe;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * Serializes a Geoportal XML editor definition to JSON format.
 */
public class GxeJsonSerializer {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GxeJsonSerializer.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GxeJsonSerializer() {
    super();
  }
    
  /** methods ================================================================= */
  
  /**
   * Returns a JSON representation for a Geoportal XML editor definition.
   * @param context the processing context
   * @param definition the definition to be serialized
   * @return the JSON string
   * @throws IOException if an exception occurs
   */
  public String asJson(GxeContext context, GxeDefinition definition) throws IOException {
    PrintWriter pw = null;
    try {
      StringWriter sw = new StringWriter();
      pw = new PrintWriter(sw);
      this.toJson(context,definition.getRootElement(),pw,0,true);
      pw.flush();
      return sw.toString();
    } finally {
      try {if (pw != null) pw.close();} catch (Exception ef) {}
    }
  }  
  
  /**
   * Evaluates a GXE XmlNode value.
   * @param context the processing context
   * @param xmlNode the GXE XmlNode that is actively being processed
   * @param value the node value to evaluate
   * @return the evaluated node value
   */
  protected String evaluateNodeValue(GxeContext context, XmlNode xmlNode, String value) {
    if (value == null) return null;
    
    if (value.startsWith("$i18nBase")) {
      boolean bIsI18NBaseAttribute = false;
      XmlNodeInfo ni = xmlNode.ensureNodeInfo();
      String ns = Val.chkStr(ni.getNamespaceURI());
      String ln = Val.chkStr(ni.getLocalName());
      if (ns.equals(GxeContext.URI_GXE) && ln.equals("i18nBase")) {
        bIsI18NBaseAttribute = true;
      }
      if (!bIsI18NBaseAttribute) {
        String i18nBase = "";
        String i18nBaseSfx = "";
        XmlElement parent = xmlNode.getParent();
        while (parent != null) {
          if (parent.getAttributes() != null) {
            String tmp = Val.chkStr(parent.getAttributeValue(GxeContext.URI_GXE,"i18nBase"));
            if (tmp.length() > 0) {
              if (tmp.startsWith("$i18nBase")) {
                i18nBaseSfx = Val.chkStr(tmp.substring(9))+i18nBaseSfx;
              } else {
                i18nBase = tmp+i18nBaseSfx;
                break;
              }
            }
          }
          parent = parent.getParent();
        }
        if (i18nBase.length() > 0) {
          String i18nKey = i18nBase+Val.chkStr(value.substring(9));
          return this.lookupI18N(context,i18nKey,xmlNode,value);
        } else {
          return null;
        }
      }

    //
    } else if (value.startsWith("$i18n.")) {
      String i18nKey = Val.chkStr(value.substring(6));
      return this.lookupI18N(context,i18nKey,xmlNode,value);
      
    } else if (value.startsWith("i18n:")) {
      String i18nKey = Val.chkStr(value.substring(5));
      return this.lookupI18N(context,i18nKey,xmlNode,value);
      
    // a JSF based expression
    } else if ((value.length() > 3) && (value.indexOf("#{") != -1)) {
      String sExpr = value;
      value = null;
      try {
        FacesContext fc = context.getFacesContext();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ELContext elContext = fc.getELContext();
        ValueExpression vExpr = ef.createValueExpression(elContext,sExpr,Object.class);
        Object oValue = vExpr.getValue(elContext);
        if (oValue != null) {
          value = Val.chkStr(oValue.toString());
          if (value.length() == 0) value = null;
        }
      } catch (Exception e) {
        value = null;
        String msg = "JSF expression failed to bind:\n"+sExpr+"\n"+e.toString();
        LOGGER.log(Level.SEVERE,msg,e);
      }
      
    }
    return value;
  }
  
  /**
   * Gets the list of attributes to render.
   * @param context the processing context
   * @param element the GXE XmlElement that is actively being processed
   * @return the list to render
   */
  protected List<XmlAttribute> getAttributesToRender(GxeContext context, XmlElement element) {
    List<XmlAttribute> list = new ArrayList<XmlAttribute>();
    for (XmlAttribute child: element.getAttributes().values()) {
      boolean bRender = true;
      XmlNodeInfo ni = child.getNodeInfo();
      String nsURI = ni.getNamespaceURI();
      if ((nsURI != null) && nsURI.equals(GxeContext.URI_GXE)) {
        String ln = ni.getLocalName();
        if (ln.equals("extends")) {
          bRender = false;
        } else if (ln.equals("src")) {
          bRender = false;
        } else if (ln.equals("extensible")) {
          bRender = false;
        } else if (ln.equals("overridable")) {
          bRender = false;
        } else if (ln.equals("i18nBase")) {
          bRender = false;          
        } else if (ln.equals("maxOccurs") || ln.equals("minOccurs") || ln.equals("use")) {
          XmlNodeInfo ni2 = element.getNodeInfo();
          String nsURI2 = ni2.getNamespaceURI();
          if ((nsURI2 != null) && nsURI2.equals(GxeContext.URI_GXE)) {
            String nv = Val.chkStr(ni.getNodeValue());
            String ln2 = ni2.getLocalName();
            if (ln2.equals("element")) {
              if (ln.equals("maxOccurs") || ln.equals("minOccurs")) {
                if (nv.equals("1")) bRender = false;
              }
            } else if (ln2.equals("attribute")) {
              if (ln.equals("maxOccurs")) {
                bRender = false;
              } else if (ln.equals("minOccurs")) {
                if (nv.equals("0")) bRender = false;
              } else if (ln.equals("use")) {
                if (nv.equals("optional")) bRender = false;
              }
            }
          }
        }
      }
      if (bRender) list.add(child);
    }
    return list;
  }
  
  /**
   * Gets the list of children to render.
   * @param context the processing context
   * @param element the GXE XmlElement that is actively being processed
   * @return the list to render
   */
  protected List<XmlElement> getChildrenToRender(GxeContext context, XmlElement element) {
    List<XmlElement> list = new ArrayList<XmlElement>();
    for (XmlElement child: element.getChildren().values()) {
      boolean bRender = true;
      XmlNodeInfo ni = child.getNodeInfo();
      String nsURI = ni.getNamespaceURI();
      if ((nsURI != null) && nsURI.equals(GxeContext.URI_GXE)) {
        String ln = ni.getLocalName();
        if (ln.equals("annotation")) {
          bRender = false;
        } else if (ln.equals("documentation")) {
          bRender = false;
        } else {
          
          if (bRender) {
            String s = Val.chkStr(child.getAttributeValue(GxeContext.URI_GXE,"rendered"));
            
            if (s.equals("$editor.isOriginalMode")) bRender = false;
            else if (s.equals("$editor.isExpertMode")) bRender = false;
            else if (s.equals("$editor.isAdvancedMode")) bRender = false;
            else if (s.equals("$editor.isSimplifiedMode")) bRender = true;
            
            /*
            if (s.equals("$editor.isOriginalMode")) bRender = true;
            else if (s.equals("$editor.isExpertMode")) bRender = false;
            else if (s.equals("$editor.isAdvancedMode")) bRender = true;
            else if (s.equals("$editor.isSimplifiedMode")) bRender = false;
            */
          }
          if (bRender) {
            String s = Val.chkStr(child.getAttributeValue(GxeContext.URI_GXE,"jsClass"));
            if (s.equals("gxe.control.SectionMenu")) bRender = false;
            else if (s.equals("gxe.control.IndexedTabArray")) bRender = false;
          }
          
        }
      }
      if (bRender) list.add(child);
    }
    return list;
  }
  
  /**
   * Looks up an I18N (internationalization and localization) message.
   * @param context the processing context
   * @param i18nKey the resource bundle key
   * @param xmlNode the GXE XmlNode that is actively being processed
   * @param originalValue the original node value
   * @return the I18N message
   */
  protected String lookupI18N(GxeContext context, 
                              String i18nKey, 
                              XmlNode xmlNode, 
                              String originalValue) {
    MessageBroker msgBroker = context.getMessageBroker();
    String rVal = Val.chkStr(msgBroker.retrieveMessage(i18nKey));
    if (rVal.length() > 0) {
      if (rVal.startsWith("??")) {
        return null;
      } else {
        //return rVal.toUpperCase();
      }
    }
    return rVal;
  }
  
  /**
   * Writes a JSON representation of the property.
   * @param context the processing context
   * @param element the GXE element that is actively being processed
   * @param writer the writer
   * @param depth the depth of the parent
   * @param isLast <code>true</code if this is the last child element for a collection of children
   * @throws IOException if an I/O exception occurs
   */
  public void toJson(GxeContext context, 
                     XmlElement element, 
                     PrintWriter writer, 
                     int depth, 
                     boolean isLast) throws IOException {
    String pfx = "";
    String sep2 = "";
    String sep4 = "";
    boolean bIndent = false;
    if (bIndent) {
      sep2 = "  ";
      sep4 = "    ";
      for (int i=0;i<2*depth;i++) pfx += " ";
    }

    List<XmlAttribute> attributes = this.getAttributesToRender(context,element);
    List<XmlElement> children = this.getChildrenToRender(context,element);
    
    boolean compactGxdNS = true;
    boolean hasChildren = (children.size() > 0);
    String line;
    writer.println(pfx+"{");
    
    XmlNodeInfo nodeInfo = element.ensureNodeInfo();
    String nsURI = nodeInfo.getNamespaceURI();
    String name = nodeInfo.getLocalName();
    String value = this.evaluateNodeValue(context,element,nodeInfo.getNodeValue());
    if (compactGxdNS && (name.indexOf(":") == -1) && (nsURI != null) && 
       (nsURI.equals(GxeContext.URI_GXE) || nsURI.equals(GxeContext.URI_GXEHTML))) {
       if (nsURI.equals(GxeContext.URI_GXE)) name = "g:"+name;
       else name = "h:"+name;
       nsURI = null;
    }
    line = pfx+sep2;
    if (nsURI != null) {
      line +="\"namespace\":\""+Val.escapeStrForJson(nsURI)+"\",";
    }
    line +="\"name\":\""+Val.escapeStrForJson(name)+"\",";
    if (value != null) {
      line +=" \"value\":\""+Val.escapeStrForJson(value)+"\",";
    }
    writer.println(line);
    
    line = pfx+sep2+"\"attributes\":[";
    writer.println(line);
    int nCount = 0;
    int nLast = attributes.size();
    //int nLast = element.getAttributes().size();
    //for (XmlAttribute child: element.getAttributes().values()) {
    for (XmlAttribute child: attributes) {
      nCount++;
      boolean bLast = (nCount == nLast);
      nodeInfo = child.ensureNodeInfo();
      nsURI = nodeInfo.getNamespaceURI();
      name = nodeInfo.getLocalName();
      value = this.evaluateNodeValue(context,child,nodeInfo.getNodeValue());
      if (compactGxdNS && (name.indexOf(":") == -1) && (nsURI != null) && 
         (nsURI.equals(GxeContext.URI_GXE) || nsURI.equals(GxeContext.URI_GXEHTML))) {
        if (nsURI.equals(GxeContext.URI_GXE)) name = "g:"+name;
        else name = "h:"+name;
        nsURI = null;
      }
      line = pfx+sep4+"{";
      if (nsURI != null) {
        line +="\"namespace\":\""+Val.escapeStrForJson(nsURI)+"\",";
      }
      line +="\"name\":\""+Val.escapeStrForJson(name)+"\"";
      if (value != null) {
        line +=",";
        line +="\"value\":\""+Val.escapeStrForJson(value)+"\"";
      }
      line +="}";
      
      if (!bLast) line +=",";
      writer.println(line);
    }
    if (hasChildren) {
      writer.println(pfx+sep2+"],");
    } else {
      writer.println(pfx+sep2+"]");
    }
    
    if (hasChildren) {  
      line = pfx+sep2+"\"children\":[";
      writer.println(line);
      nCount = 0;
      nLast = children.size();
      for (XmlElement child: children) {
        nCount++;
        boolean bLast = (nCount == nLast);
        this.toJson(context,child,writer,(depth+2),bLast);
      }
      writer.println(pfx+sep2+"]");
    }
    
    line = pfx+"}";
    if (!isLast) line +=",";
    writer.println(line);
  }
  
}
