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
package com.esri.gpt.catalog.discovery;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;

import java.util.Map;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * Contains the configured collection of discoverble/storable property meanings.
 */
@SuppressWarnings("serial")
public class PropertyMeanings extends CaseInsensitiveMap<PropertyMeaning> {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(PropertyMeanings.class.getName());
  
  /** Date modified property name = "dateModified" */
  public static final String NAME_DATEMODIFIED = "dateModified";
  
  /** Document UUID (primary key) name = "uuid" */
  public static final String NAME_UUID = "uuid";
  
  /** instance variables ====================================================== */
  private AliasedDiscoverables allAliased = new AliasedDiscoverables();
  private PropertySets         dcPropertySets = new PropertySets();
  private IStoreables          storeables = new Storeables();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public PropertyMeanings() {
    super(false);
  }
  
  /** properties ============================================================== */
  
  /** 
   * Gets the entire map of aliased discoverables. 
   * @return the map of aliased discoverables
   */
  public AliasedDiscoverables getAllAliased() {
    return allAliased;
  }
  
  /**
   * Gets the configured Dublin Core property sets.
   * @return the property sets
   */
  public PropertySets getDcPropertySets() {
    return dcPropertySets;
  }
  
  /** methods ================================================================= */
    
  /**
   * Adds a property meaning to the collection.
   * @param meaning the property meaning to add
   */
  private void add(PropertyMeaning meaning) {
    if (meaning != null) {
      put(meaning.getName(),meaning);
    }
  }
  
  /**
   * Loads property meanings from an XML document.
   * <p/>
   * The XML document  is based upon the configuration file:<br/>
   * gpt/metadata/property-meanings.xml
   * @param dom the XML document associated with the property meanings configuration file
   * @throws XPathExpressionException if an invalid XPath expression was encountered
   */
  public void load(Document dom) throws XPathExpressionException {
    this.clear();
    
    XPath xpath = XPathFactory.newInstance().newXPath();
    Node root = (Node)xpath.evaluate("/property-meanings",dom,XPathConstants.NODE);
    if (root != null) {
      NodeList nlProps = (NodeList)xpath.evaluate("property-meaning",root,XPathConstants.NODESET);
      String name;
      PropertyMeaning meaning;
      PropertyMeaningType meaningType;
      PropertyValueType valueType;
      PropertyComparisonType compType;
      
      // default allow leading wildcard setting
      String alwExpr = "/property-meanings/property-meaning[@name='anytext']/@allowLeadingWildcard";
      boolean alwDefault = Val.chkStr(xpath.evaluate(alwExpr,dom)).equalsIgnoreCase("true");

      // loop through the properties
      for (int i=0;i<nlProps.getLength();i++) {
        Node ndProp = nlProps.item(i);
        name = Val.chkStr(xpath.evaluate("@name",ndProp));        
        meaningType = PropertyMeaningType.from(xpath.evaluate("@meaningType",ndProp));
        valueType = PropertyValueType.from(xpath.evaluate("@valueType",ndProp));
        compType = PropertyComparisonType.from(xpath.evaluate("@comparisonType",ndProp));
        
        meaning = new PropertyMeaning(name,meaningType,valueType,compType);
        
        if (meaning.getMeaningType().equals(PropertyMeaningType.ANYTEXT)) {
          meaning = new PropertyMeaning.AnyText();
          String names = xpath.evaluate("consider/text()",ndProp);
          ((PropertyMeaning.AnyText)meaning).getNamesToConsider().addDelimited(names);
          
        } else if (meaning.getMeaningType().equals(PropertyMeaningType.GEOMETRY)) {
          meaning = new PropertyMeaning.Geometry();
          Envelope envelope = null;
          String tokens = xpath.evaluate("defaultEnvelope/text()",ndProp);
          String[] coords = Val.tokenize(tokens," ");
          if (coords.length == 4) {
            envelope = new Envelope();
            envelope.put(coords[0],coords[1],coords[2],coords[3]);
            if (envelope.isValid()) {
              ((PropertyMeaning.Geometry)meaning).setDefaultEnvelope(envelope);
            }
          }
          
        }
        
        meaning.setAllowLeadingWildcard(
            Val.chkBool(xpath.evaluate("@allowLeadingWildcard",ndProp),alwDefault));
        this.add(meaning);
        
        NodeList nlChildren = ndProp.getChildNodes();
        for (int j=0;j<nlChildren.getLength();j++) {
          Node ndChild = nlChildren.item(j);
          if (ndChild.getNodeType() == Node.ELEMENT_NODE) { 
            String qName = Val.chkStr(xpath.evaluate("@name",ndChild));
            StringSet qAliases = new StringSet();
            if (qName.length() > 0) {
              
              // Dublin Core elements
              if (ndChild.getNodeName().equalsIgnoreCase("dc")) {
                DcElement dc = new DcElement(qName);
                dc.getAliases().add(dc.getElementName());
                dc.getAliases().addDelimited(xpath.evaluate("@aliases",ndChild));
                dc.setScheme(xpath.evaluate("@scheme",ndChild));
                meaning.setDcElement(dc);
                qAliases = dc.getAliases();
                
              // other elements
              } else {
                qAliases.add(qName);
                qAliases.addDelimited(xpath.evaluate("@aliases",ndChild));
              }
              
              // append to the all aliased discoverables collection
              IStoreable storeable = storeables.connect(meaning);
              if (storeable != null) {
                Discoverable discoverable = new Discoverable(qName);
                discoverable.setMeaning(meaning);
                discoverable.setStoreable(storeable);
                for (String alias: qAliases) {
                  this.getAllAliased().put(alias,discoverable);
                }
                this.getAllAliased().put(meaning.getName(),discoverable);
              }
              
            }
          }
        }        
         
      }
    }
    
    // load explicit aliases
    if (root != null) {
      NodeList nl = (NodeList)xpath.evaluate("property-alias",root,XPathConstants.NODESET);      
      for (int i=0;i<nl.getLength();i++) {
        Node nd = nl.item(i);
        String name = Val.chkStr(xpath.evaluate("@meaning-name",nd));        
        String value = Val.chkStr(xpath.evaluate("@value",nd)); 
        PropertyMeaning meaning = this.get(name);
        if (meaning == null) {
          LOGGER.warning("property-meanings.xml/property-alias@meaning-name="+name+" is invalid.");
        } else {
          Discoverable discoverable = this.getAllAliased().get(name);
          if (discoverable != null) {
            StringSet aliases = new StringSet();
            aliases.addDelimited(value);
            for (String alias: aliases) {
              this.getAllAliased().put(alias,discoverable);
            }
          }
        }
      }
    }
    
    // make the Dublin Core aliased map
    AliasedDiscoverables dcAliased = dcPropertySets.getAllAliased();
    for (PropertyMeaning meaning: values()) {
      DcElement dcElement = meaning.getDcElement();
      if ((dcElement != null) && (dcElement.getElementName().length() > 0)) {
        IStoreable storeable = storeables.connect(meaning);
        if (storeable != null) {
          Discoverable discoverable = new Discoverable(dcElement.getElementName());
          discoverable.setMeaning(meaning);
          discoverable.setStoreable(storeable);
          
          StringSet aliases = dcElement.getAliases();
          for (String alias: aliases) {
            dcAliased.put(alias,discoverable);
          }
          dcAliased.put(meaning.getName(),discoverable);
        }
      }
    }
    
    // parse the property sets
    if (root != null) {
      Node ndSets = (Node)xpath.evaluate("propertySets",root,XPathConstants.NODE);
      if (ndSets != null) {
        Node ndBrief = (Node)xpath.evaluate("brief",ndSets,XPathConstants.NODE);
        Node ndSummary = (Node)xpath.evaluate("summary",ndSets,XPathConstants.NODE);
        Node ndFull = (Node)xpath.evaluate("full",ndSets,XPathConstants.NODE);
        loadDcPropertySet(ndBrief,xpath,dcAliased,dcPropertySets.getBrief());
        loadDcPropertySet(ndSummary,xpath,dcAliased,dcPropertySets.getSummary());
        loadDcPropertySet(ndFull,xpath,dcAliased,dcPropertySets.getFull());
      }
    }
    
  }
  
  /**
   * Loads a Dublin core property set.
   * @param parent the parant node for the set
   * @param xpath and XPath that can be used for dom queries
   * @param aliased the full set of aliased discoverables
   * @param discoverables the collection of discoverables to populate
   * @throws XPathExpressionException if an invalid XPath expression was encountered
   */
  private void loadDcPropertySet(Node parent,  
                                 XPath xpath, 
                                 AliasedDiscoverables aliased,
                                 Discoverables discoverables) 
   throws XPathExpressionException {
   StringSet names = new StringSet();
   if (parent != null) {
     Node ndDc = (Node)xpath.evaluate("dc",parent,XPathConstants.NODE);
     if (ndDc != null) {
       NodeList nlNames = (NodeList)xpath.evaluate("meaning-names",ndDc,XPathConstants.NODESET);
       for (int i=0;i<nlNames.getLength();i++) {
         names.addDelimited(nlNames.item(i).getTextContent());
       }
     }
     for (String name: names) {
       Discoverable discoverable = aliased.get(name);
       if ((discoverable != null) && (discoverable.getMeaning().getDcElement() != null)) {
         String dcName = Val.chkStr(discoverable.getMeaning().getDcElement().getElementName());
         if (dcName.length() > 0) {
           discoverables.add(aliased.get(name));
         }
       }
     }
   }
   
  }
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    if (size() == 0) {
      sb.append(" (No meanings.)");
    } else {
      for (Map.Entry<String,PropertyMeaning> entry: this.entrySet()) {
        sb.append("\nkey=\"").append(entry.getKey()).append("\"\n");
        ((PropertyMeaning)entry.getValue()).echo(sb,1);
      }
    }   
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (");
    echo(sb);
    sb.append("\n) ===== end ").append(getClass().getName());
    return sb.toString();
  }
  
}