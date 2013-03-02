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
package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.Sortable;
import com.esri.gpt.catalog.discovery.Sortables;
import com.esri.gpt.server.csw.provider.components.ISortByParser;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;

import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the ogc:SortBy portion of a CSW request.
 */
public class SortByParser extends DiscoveryAdapter implements ISortByParser {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(SortByParser.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public SortByParser(OperationContext context) {
    super(context);
  }
  
  /** methods ================================================================= */
  
  /**
   * Parses the ogc:SortBy node for an XML based request.
   * @param context the operation context
   * @param sortByNode the ogc:SortBy node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public void parseSortBy(OperationContext context, Node sortByNode, XPath xpath) 
    throws OwsException, XPathExpressionException {
    
    // parse the sort option
    LOGGER.finer("Parsing sort by for "+sortByNode.getNodeName());
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    Sortables sortables = new Sortables();
    NodeList nlProps = null;
    if (sortByNode != null) {
      nlProps = (NodeList)xpath.evaluate("ogc:SortProperty",sortByNode,XPathConstants.NODESET);
    }
    if ((nlProps != null) && (nlProps.getLength() > 0)) {
      for (int i=0; i<nlProps.getLength(); i++) {
        Sortable sortable = this.parsePropertyName(nlProps.item(i),xpath).asSortable();
        Node ndDir = (Node)xpath.evaluate("ogc:SortOrder",nlProps.item(i),XPathConstants.NODE);
        if (ndDir != null) {
          String sSortDir = ndDir.getTextContent();
          try {
            LOGGER.finer("Setting sort direction:"+sSortDir);
            sortable.setDirection(Sortable.SortDirection.from(sSortDir));
          } catch (IllegalArgumentException e) {
            String msg = "This parameter value is not supported: "+sSortDir;
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,"SortOrder",msg);
          }
        }
        sortables.add(sortable);
      }
    }
    if (sortables.size() > 0) {
      query.setSortables(sortables);
    }
  }
 
}
