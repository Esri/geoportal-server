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
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveryClause;
import com.esri.gpt.catalog.discovery.DiscoveryFilter;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyClause;
import com.esri.gpt.catalog.discovery.PropertyMeaningType;
import com.esri.gpt.catalog.discovery.SpatialClause;
import com.esri.gpt.catalog.discovery.LogicalClause.LogicalAnd;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.CapabilityOptions;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.CswNamespaces;
import com.esri.gpt.server.csw.provider.components.IFilterParser;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.QueryOptions;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses the ogc:Filter portion of a CSW query request (GetRecords).
 */
public class QueryFilterParser extends DiscoveryAdapter implements IFilterParser {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(QueryFilterParser.class.getName());
  
  /** instance variables ====================================================== */
  private OperationContext opContext;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public QueryFilterParser(OperationContext context) {
    super(context);
    this.opContext = context;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parses the ogc:Filter node for an XML based request.
   * @param context the operation context
   * @param filterNode the ogc:Filter node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public void parseFilter(OperationContext context, Node filterNode, XPath xpath) 
    throws OwsException, XPathExpressionException {
    
    // initialize the discovery filter
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    DiscoveryFilter filter = query.getFilter();
    filter.setRootClause(null);
    filter.setStartRecord(qOptions.getStartRecord());
    if (qOptions.getResultType().equals(CswConstants.ResultType_Hits)) {
      filter.setMaxRecords(0);
    } else {
      filter.setMaxRecords(qOptions.getMaxRecords());
    }
        
    // parse the ogc:Filter
    if (filterNode != null) {
      LOGGER.finer("Parsing ogc:Filter....");
      filter.setRootClause(new LogicalAnd());
      LogicalClause rootClause = filter.getRootClause();
      this.parseLogicalClause(filterNode,xpath,rootClause);
      if (rootClause.getClauses().size() == 1) {
        DiscoveryClause onlySubClause = rootClause.getClauses().get(0);
        if (onlySubClause instanceof LogicalClause) {
          LogicalClause onlyLogicalSubClause = (LogicalClause) onlySubClause;
          filter.setRootClause(onlyLogicalSubClause);
        }
      }
    }
    
  }
  
  /**
   * Parses a parent node for logical, property comparison and spatial sub-clauses.
   * <br/>Any logical clauses encountered will be recursively parsed.
   * @param parent the parent node from which sub-clauses will read
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @param logicalClause the active logical clause to which sub-clauses will be added
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  protected void parseLogicalClause(Node parent, XPath xpath, LogicalClause logicalClause) 
    throws OwsException, XPathExpressionException {
    NodeList children = parent.getChildNodes();
    if (children == null) return;

    int n = children.getLength();
    for (int i=0; i<n; i++) {
      Node subNode = children.item(i);
      String uri = Val.chkStr(subNode.getNamespaceURI());
      if (uri.length() > 0) {
        String localName = Val.chkStr(subNode.getLocalName());
        LOGGER.finer("Parsing node ("+uri+")"+localName);
        if (uri.equals(CswNamespaces.URI_OGC)) {

          // logical clauses - add then recurse
          if (localName.equals("And")) {
            LogicalClause logical = new LogicalClause.LogicalAnd();
            logicalClause.getClauses().add(logical);
            this.parseLogicalClause(subNode,xpath,logical);

          } else if (localName.equals("Or")) {
            LogicalClause logical = new LogicalClause.LogicalOr();
            logicalClause.getClauses().add(logical);
            this.parseLogicalClause(subNode,xpath,logical);

          } else if (localName.equals("Not")) {
            LogicalClause logical = new LogicalClause.LogicalNot();
            logicalClause.getClauses().add(logical);
            this.parseLogicalClause(subNode,xpath,logical);

          // property comparison clauses
          } else if (localName.equals("PropertyIsBetween")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
               new PropertyClause.PropertyIsBetween());

          } else if (localName.equals("PropertyIsEqualTo")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsEqualTo());

          } else if (localName.equals("PropertyIsGreaterThan")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsGreaterThan());

          } else if (localName.equals("PropertyIsGreaterThanOrEqualTo")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsGreaterThanOrEqualTo());

          } else if (localName.equals("PropertyIsLessThan")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsLessThan());

          } else if (localName.equals("PropertyIsLessThanOrEqualTo")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsLessThanOrEqualTo());

          } else if (localName.equals("PropertyIsLike")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsLike());

          } else if (localName.equals("PropertyIsNotEqualTo")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsNotEqualTo());

          } else if (localName.equals("PropertyIsNull")) {
            this.parsePropertyClause(subNode,xpath,logicalClause,
                new PropertyClause.PropertyIsNull());

            // spatial clauses
          } else if (localName.equals("BBOX")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryBBOXIntersects());

          } else if (localName.equals("Contains")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryContains());

          } else if (localName.equals("Disjoint")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryIsDisjointTo());

          } else if (localName.equals("Equals")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryIsEqualTo());

          } else if (localName.equals("Intersects")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryIntersects());

          } else if (localName.equals("Overlaps")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryOverlaps());

          } else if (localName.equals("Within")) {
            this.parseSpatialClause(subNode,xpath,logicalClause,
                new SpatialClause.GeometryIsWithin());

          } else if (localName.equals("Beyond")  || localName.equals("Crosses") ||
                     localName.equals("DWithin") || localName.equals("Touches")) {
            String locator = subNode.getLocalName();
            String msg = "Spatial operator "+subNode.getNodeName()+" is not supported.";
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
            
          } else {
            
            String locator = subNode.getLocalName();
            String msg = "Operator "+subNode.getNodeName()+" is not supported.";
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
          }
        }
      }
    }
  }
  
  /**
   * Parses the property and literal elements underlying a comparison operator.
   * <br/>If the resulting property clause is valid, it will be added to the
   * clause collection of the supplied logical clause.
   * @param parent the parent node (the node of the property operator)
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @param logicalClause the logical clause which will contain the comparison clause
   * @param propertyClause the populate
   * @throws OwsException OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  protected void parsePropertyClause(Node parent, 
                                     XPath xpath,
                                     LogicalClause logicalClause, 
                                     PropertyClause propertyClause)
    throws OwsException, XPathExpressionException {

    // TODO what if this is a Geometry property, also need to validate literals
    
    // initialize
    LOGGER.finer("Parsing property clause for "+parent.getNodeName());
    String sErr = parent.getNodeName();
    Discoverable discoverable = this.parsePropertyName(parent,xpath);
    propertyClause.setTarget(discoverable);
    
    // anytext queries are only supported for PropertyIsLike
    if (discoverable.getMeaning().getMeaningType().equals(PropertyMeaningType.ANYTEXT)) {
      if (!(propertyClause instanceof PropertyClause.PropertyIsLike)) {
        String sPropName = "AnyText";
        Node ndPropName = (Node)xpath.evaluate("ogc:PropertyName",parent,XPathConstants.NODE);
        if (ndPropName != null) {
          sPropName = Val.chkStr(ndPropName.getTextContent());
        }
        String msg = sErr+" - PropertyIsLike is the only supported operand for PropertyName: "+sPropName;
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,"PropertyName",msg);
      }
    }

    // between comparison - set lower and upper boundaries
    if (propertyClause instanceof PropertyClause.PropertyIsBetween) {
      PropertyClause.PropertyIsBetween between;
      between = (PropertyClause.PropertyIsBetween)propertyClause;
      Node ndLower = (Node)xpath.evaluate("ogc:LowerBoundary",parent,XPathConstants.NODE);
      Node ndUpper = (Node)xpath.evaluate("ogc:UpperBoundary",parent,XPathConstants.NODE);
      String sLower = "";
      String sUpper = "";
      if ((ndLower == null) && (ndUpper == null)) {
        String msg = sErr+" - a LowerBoundary or UpperBoundary was not found.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,"PropertyIsBetween",msg);
      }
      if (ndLower != null) {
        sLower = ndLower.getTextContent();
        between.setLowerBoundary(sLower);
        // TODO validate content
      }
      if (ndUpper != null) {
        sUpper = ndUpper.getTextContent();
        between.setUpperBoundary(sUpper);
        // TODO validate content
      }
      if ((sLower == null) || (sLower.length() == 0)) {
        if ((sUpper == null) || (sUpper.length() == 0)) {
          String msg = sErr+" - the LowerBoundary and UpperBoundary are empty.";
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,"PropertyIsBetween",msg);
        }
      }

      // null check - no literal required
    } else if (propertyClause instanceof PropertyClause.PropertyIsNull) {

      // non-range clauses
    } else {
      Node ndLiteral = (Node)xpath.evaluate("ogc:Literal", parent,XPathConstants.NODE);
      if (ndLiteral == null) {
        String msg = sErr+" - an ogc:Literal was not found.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,"Literal",msg);
      }
      String sLiteral = ndLiteral.getTextContent();
      propertyClause.setLiteral(sLiteral);
      // TODO validate content
      if ((sLiteral == null) || (sLiteral.length() == 0)) {
        String msg = sErr+".ogc:Literal - the supplied literal was empty.";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,"Literal",msg);
      }

      // set like comparison attributes
      if (propertyClause instanceof PropertyClause.PropertyIsLike) {
        PropertyClause.PropertyIsLike like;
        like = (PropertyClause.PropertyIsLike) propertyClause;
        like.setEscapeChar(xpath.evaluate("@escapeChar", parent));
        like.setSingleChar(xpath.evaluate("@singleChar", parent));
        like.setWildCard(xpath.evaluate("@wildCard", parent));
      }
      
      // initialize the language code, (INSPIRE requirement but generally applicable)
      // INSPIRE requirement, specify the language for exceptions in this manner 
      // doesn't seem to be a good approach
      if ((this.opContext != null) && (propertyClause instanceof PropertyClause.PropertyIsEqualTo)) {
        if (discoverable.getMeaning().getName().equals("apiso.Language")) {
          if ((sLiteral != null) && (sLiteral.length() > 0)) {
            CapabilityOptions cOptions = this.opContext.getRequestOptions().getCapabilityOptions();
            if (cOptions.getLanguageCode() == null) {
              cOptions.setLanguageCode(sLiteral);
            }
          }
        }
      }
      
    }

    // add the clause
    logicalClause.getClauses().add(propertyClause);
  }
    
  /**
   * Parses the spatial operand underlying a spatial operator. 
   * <br/>The spatial operand is used to populate the bounding envelope 
   * associated with the supplied spatial clause. 
   * <br/>If the envelope is valid, the spatial clause will be added to the 
   * clause collection of the supplied logical clause.
   * @param parent the parent node (specifies the spatial operator)
   * @param xpath an XPath to enable queries (properly configured with namespaces)
   * @param logicalClause the logical clause which will contain the spatial clause
   * @param spatialClause the spatial clause to populate
   * @throws OwsException OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  protected void parseSpatialClause(Node parent, 
                                    XPath xpath,
                                    LogicalClause logicalClause, 
                                    SpatialClause spatialClause)
    throws OwsException, XPathExpressionException {

    // initialize
    LOGGER.finer("Parsing spatial clause for "+parent.getNodeName());
    String sErr = parent.getNodeName();
    Envelope envelope = spatialClause.getBoundingEnvelope();
    // TODO ensure that the discoverable is a geometry
    Discoverable discoverable = this.parsePropertyName(parent,xpath);
    spatialClause.setTarget(discoverable);

    /*
     * 
     * gml:Envelope <attribute name="gid" type="ID" use="optional"/> <attribute
     * name="srsName" type="anyURI" use="optional"/>
     * 
     * <element name="lowerCorner" type="gml:DirectPositionType"/> <attribute
     * name="gid" type="ID" use="optional"/> <attribute name="srsName"
     * type="anyURI" use="optional"/> space separated x y <element
     * name="upperCorner" type="gml:DirectPositionType"/> <attribute name="gid"
     * type="ID" use="optional"/> <attribute name="srsName" type="anyURI"
     * use="optional"/> space separated x y
     * 
     * <element ref="gml:pos" minOccurs="2" maxOccurs="2"> both are
     * type="gml:DirectPositionType" (deprecated)
     * 
     * <element ref="gml:coord" minOccurs="2" maxOccurs="2"/> <element X> <element
     * Y>
     * 
     * <element ref="gml:coordinates"/> <attribute name="decimal" type="string"
     * use="optional" default="."/> <attribute name="cs" type="string"
     * use="optional" default=","/> <attribute name="ts" type="string"
     * use="optional" default=" "/>
     * 
     * gml:Box <attribute name="gid" type="ID" use="optional"/> <attribute
     * name="srsName" type="anyURI" use="optional"/> <element ref="gml:coord"
     * minOccurs="2" maxOccurs="2"/> <element X> <element Y> <element
     * ref="gml:coordinates"/> <attribute name="decimal" type="string"
     * use="optional" default="."/> <attribute name="cs" type="string"
     * use="optional" default=","/> <attribute name="ts" type="string"
     * use="optional" default=" "/>
     * 
     * gml:Point <attribute name="gid" type="ID" use="optional"/> <attribute
     * name="srsName" type="anyURI" use="optional"/> <element ref="gml:coord"/>
     * <element X> <element Y> <element ref="gml:coordinates"/> <attribute
     * name="decimal" type="string" use="optional" default="."/> <attribute
     * name="cs" type="string" use="optional" default=","/> <attribute name="ts"
     * type="string" use="optional" default=" "/>
     * 
     * gml:LineString <attribute name="gid" type="ID" use="optional"/> <attribute
     * name="srsName" type="anyURI" use="optional"/> <element ref="gml:coord"
     * minOccurs="2" maxOccurs="unbounded"/> <element X> <element Y> <element
     * ref="gml:coordinates"/> <attribute name="decimal" type="string"
     * use="optional" default="."/> <attribute name="cs" type="string"
     * use="optional" default=","/> <attribute name="ts" type="string"
     * use="optional" default=" "/>
     */

    // determine the envelope
    Node ndSpatial = (Node)xpath.evaluate("gml:Envelope",parent,XPathConstants.NODE);
    if (ndSpatial == null) {
      ndSpatial = (Node)xpath.evaluate("gml:Box",parent,XPathConstants.NODE);
      if (ndSpatial == null) {
        ndSpatial = (Node)xpath.evaluate("gml:Point",parent,XPathConstants.NODE);
      }
    }
    if (ndSpatial != null) {
      LOGGER.finest("Parsing "+ndSpatial.getNodeName()+"...");
      sErr += "."+ndSpatial.getNodeName();
      spatialClause.setSrsName(xpath.evaluate("@srsName",ndSpatial));
      Node ndLower = (Node)xpath.evaluate("gml:lowerCorner",ndSpatial,XPathConstants.NODE);
      Node ndUpper = (Node)xpath.evaluate("gml:upperCorner",ndSpatial,XPathConstants.NODE);
      Node ndCoords = (Node)xpath.evaluate("gml:coordinates",ndSpatial,XPathConstants.NODE);
      NodeList nlCoord = (NodeList)xpath.evaluate("gml:coord",ndSpatial,XPathConstants.NODESET);

      // handle a lower and upper boundary
      if ((ndLower != null) && (ndUpper != null)) {
        String sLower = Val.chkStr(ndLower.getTextContent());
        String sUpper = Val.chkStr(ndUpper.getTextContent());
        LOGGER.finest("Parsing gml:lowerCorner=\""+sLower+"\" gml:upperCorner=\""+sUpper+"\"");
        String[] xyLower = Val.tokenize(sLower," ");
        String[] xyUpper = Val.tokenize(sUpper," ");
        if ((xyLower.length == 2) && (xyUpper.length == 2)) {
          envelope.put(xyLower[0],xyLower[1],xyUpper[0],xyUpper[1]);
        }

        // handle a delimited list of coordinates
      } else if (ndCoords != null) {

        // separators: decimal, ts (between coordinate pairs), cs (between x/y values)
        char cDecSep = '.';
        String decSep = xpath.evaluate("@decimal",ndCoords);
        if ((decSep != null) && (decSep.length() == 1)) cDecSep = decSep.charAt(0);
        String tsSep = xpath.evaluate("@ts",ndCoords);
        if ((tsSep == null) || (tsSep.length() != 1)) tsSep = " ";
        String csSep = xpath.evaluate("@cs", ndCoords);
        if ((csSep == null) || (csSep.length() != 1)) csSep = ",";
        String sepMsg = "decimal=\""+cDecSep+"\" ts=\""+tsSep+"\" cs=\""+csSep+"\"";

        // collect individual string values
        String sCordinates = Val.chkStr(ndCoords.getTextContent());
        LOGGER.finest("Parsing gml:coordinates "+sepMsg+" coordinates=\""+sCordinates+"\"");
        ArrayList<String> values = new ArrayList<String>();
        String[] tsValues = Val.tokenize(sCordinates,tsSep);
        for (String tsValue : tsValues) {
          String[] csValues = Val.tokenize(tsValue,csSep);
          for (String csValue: csValues) {
            LOGGER.finer("Adding coordinate value: "+csValue);
            values.add(csValue.replace(cDecSep,'.'));
          }
        }

        // determine the minimum and maximum envelope values from the coordinate list
        for (int i=0; i<values.size(); i=i+2) {
          try {
            LOGGER.finest("Handling coordinate: "+values.get(i)+" "+values.get(i + 1));
            double x = Double.parseDouble(values.get(i));
            double y = Double.parseDouble(values.get(i+1));
            envelope.merge(new Envelope(x,y,x,y));
          } catch (NumberFormatException e) {
            LOGGER.warning(e.getMessage());
          }
        }

      // handle a collection of coordinate elements
      } else if (nlCoord.getLength() > 0) {
        LOGGER.finest("Parsing gml:coord elements...");
        for (int i=0; i< nlCoord.getLength(); i++) {
          Node ndX = (Node)xpath.evaluate("gml:X",nlCoord.item(i),XPathConstants.NODE);
          Node ndY = (Node)xpath.evaluate("gml:Y",nlCoord.item(i),XPathConstants.NODE);
          if ((ndX != null) && (ndY != null)) {
            try {
              LOGGER.finest("Handling coordinate: "+ndX.getTextContent()+" "+ndY.getTextContent());
              double x = Double.parseDouble(ndX.getTextContent());
              double y = Double.parseDouble(ndY.getTextContent());
              envelope.merge(new Envelope(x,y,x,y));
            } catch (NumberFormatException e) {
              LOGGER.warning(e.getMessage());
            }
          }
        }
      }

    }

    // add the clause if the envelope is not empty
    if (!envelope.isEmpty()) {
      logicalClause.getClauses().add(spatialClause);
    } else {
      String msg = sErr+" - the geometry of the spatial operand was not valid.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,parent.getLocalName(),msg);
    }
  }
 
}
