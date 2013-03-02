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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides utilities supporting request parsing.
 */
public class ParseHelper {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public ParseHelper() {}
  
  /** methods ================================================================= */

  /**
   * Gets the HTTP request parameter values associated with a key.
   * @param request the HTTP request
   * @param name the parameter name
   * @return the parameter values (null if not found)
   */
  @SuppressWarnings("unchecked")
  public String[] getParameterValues(HttpServletRequest request, String name) {
    Map<String, String[]> requestParameterMap = request.getParameterMap();
    for (Map.Entry<String, String[]> e : requestParameterMap.entrySet()) {
      if (e.getKey().equalsIgnoreCase(name)) {
        return e.getValue();
      }
    }
    return null;
  }
  
  /**
   * Gets the HTTP request parameter values associated with a key then tokenizes 
   * all values based upon a supplied delimiter.
   * @param request the HTTP request
   * @param name the parameter name
   * @param delimiter the delimiter
   * @return the parameter values (null if not found)
   */
  public String[] getParameterValues(HttpServletRequest request, String name, String delimiter) {
    String[] values = this.getParameterValues(request,name);
    if (delimiter == null) {
      return values;
    } else if (values != null) {
      List<String> list = new ArrayList<String>();
      for (String tokens: values) {
        StringTokenizer st = new StringTokenizer(tokens,delimiter);
        while (st.hasMoreElements()) {
          list.add((String)st.nextElement());
        }
      }
      return list.toArray(new String[0]); 
    }
    return null;
  }
  
  /**
   * Generates a String array of child text node values.
   * @param parent the parent node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @param expr the XPath locator expression
   * @return an array of text node values associated with the supplied expression
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public String[] getParameterValues(Node parent, XPath xpath, String expr) 
    throws XPathExpressionException {
    if (parent != null) {
      NodeList nl = (NodeList)xpath.evaluate(expr,parent,XPathConstants.NODESET);
      if ((nl != null) && (nl.getLength() > 0)) {
        List<String> list = new ArrayList<String>();
        for (int i=0;i<nl.getLength();i++) {
          list.add(nl.item(i).getTextContent());
        }
        return list.toArray(new String[0]);
      }
    }
    return null;
  }
      
}
