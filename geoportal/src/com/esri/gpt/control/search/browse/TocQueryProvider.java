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
package com.esri.gpt.control.search.browse;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;
import org.w3c.dom.Node;

/**
 * Provides the rest query associated with a TOC item.
 */
public class TocQueryProvider {

  /** constructors ============================================================ */
  
  /** Default constructor */
  public TocQueryProvider() {}
  
  /** methods ================================================================= */
  
  /**
   * Makes a TocQueryProvider instance based upon a class name.
   * @param context the operation context
   * @param className the fully qualified class name
   * @return the instance
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public static TocQueryProvider makeProviderInstance(TocContext context, String className) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    className = Val.chkStr(className);
    if (className.length() == 0) {
      return new TocQueryProvider();
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof TocQueryProvider) {
        return (TocQueryProvider)obj;
      } else {
        String msg = "The configured query.className is invalid: "+className;
        throw new ConfigurationException(msg);
      }
    }
  }
  
  /**
   * Processes a TOC item's query node.
   * @param context the operation context
   * @param parent the parent item
   * @param node the query node
   * @throws Exception if an exception occurs
   */
  public void processQueryNode(TocContext context, TocItem parent, Node node) throws Exception {
    
    String sQuery = Val.chkStr(node.getTextContent());
    if (sQuery.startsWith("urn:esri:geoportal:browse:resourceRelationship:")) {
      StringBuilder sbQuery = new StringBuilder();
      String resourceId = Val.chkStr(context.getSubjectResourceID());
      sQuery = sQuery.trim().replace("urn:esri:geoportal:browse:resourceRelationship:","");
      String[] parts = sQuery.split("-");
      if ((resourceId.length() > 0) && (parts != null) && (parts.length == 2)) {
        String fromField = Val.chkStr(parts[0]);
        String toField = Val.chkStr(parts[1]);
        if ((fromField.length() > 0) && (toField.length() > 0)) {
          TocIndexAdapter adapter = context.getIndexAdapter();
          String[] values = adapter.queryFieldByUuid(context,fromField,resourceId);
          if ((values != null) && (values.length > 0)) {
            
            for (String value: values) {
              value = Val.chkStr(value);
              if (value.length() > 0) {
                if (sbQuery.length() > 0) sbQuery.append(" OR ");
                sbQuery.append(toField).append(":");
                sbQuery.append("\"").append(value).append("\"");
              }
            }
          }
        }
      }
      if (sbQuery.length() > 0) {
        sQuery = java.net.URLEncoder.encode(sbQuery.toString(),"UTF-8");
        parent.setQuery("searchText="+sQuery);
      } else {
        parent.setQuery("contentType=noMatchFound");
      }

    } else {
      parent.setQuery(node.getTextContent());
    }
  }

}
