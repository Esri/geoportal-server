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
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.framework.util.LogUtil;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;

/**
 * Defines a collection of metadata schemas.
 */
public class Schemas extends LinkedHashMap<String,Schema> {

  // class variables =============================================================
      
  // instance variables ==========================================================
  private PropertyMeanings propertyMeanings = new PropertyMeanings();
  
  // constructors ================================================================
  
  /** Default constructor. */
  public Schemas() {
    this(null);
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public Schemas(Schemas objectToDuplicate) {
    if (objectToDuplicate == null) {
    } else {
      setPropertyMeanings(objectToDuplicate.getPropertyMeanings());
      for (Schema member: objectToDuplicate.values()) {
        add(member.duplicate());
      }
    }
  }
  
  // properties ==================================================================
  
  /**
   * Gets the configured property meanings.
   * @return the property meanings
   */
  public PropertyMeanings getPropertyMeanings() {
    return propertyMeanings;
  }
  /**
   * Sets the configured property meanings.
   * @param meanings the property meanings
   */
  protected void setPropertyMeanings(PropertyMeanings meanings) {
    this.propertyMeanings = meanings;
  }
    
  // methods =====================================================================
  
  /**
   * Adds a member to the collection.
   * <br/>The member will not be added if it is null or
   * if it has an empty key.
   * @param member the member to add
   */
  public void add(Schema member) {
    if ((member != null) && (member.getKey().length() > 0)) {
      put(member.getKey(),member);
    }
  }
  
  /**
   * Locates a Schema based upon a supplied schema key.
   * @param schemaKey the key associated with the schema to locate
   * @return the associated schema object (duplicated)
   * @throws UnrecognizedSchemaException if the schema is unrecognized
   */
  public Schema locate(String schemaKey) 
    throws UnrecognizedSchemaException {
    Schema locatedSchema = get(schemaKey);
    if (locatedSchema != null) {
      return locatedSchema.duplicate();
    } else {
      throw new UnrecognizedSchemaException("Unrecognized metadata schema key.");
    }
  }
  
  /**
   * Interrogates a metadata document to locate a Schema capable of
   * processing the document.
   * @param dom the metadata document
   * @return the associated schema object (duplicated)
   * @throws UnrecognizedSchemaException if the schema is unrecognized
   */
  public Schema interrogate(Document dom) 
    throws UnrecognizedSchemaException {
    Schema locatedSchema = null;
    int locatedCount = 0;
    
    
    for (Schema schema: values()) {
      try {
        Interrogation interrogation = schema.interrogate(dom);
        if (interrogation.getMatchedNodeCount() > 0) {
          locatedSchema = schema;
          break;
        }
      } catch (XPathExpressionException e) {
        String sMsg = "Error interrogating schema: "+schema.getKey();
        LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
      }
    }
    
  
    /*
    for (Schema schema: values()) {
      try {
        Interrogation interrogation = schema.interrogate(dom);
        int nCount = interrogation.getMatchedNodeCount();
        if ((nCount > 0) && (nCount > locatedCount)) {
          locatedSchema = schema;
          locatedCount = nCount;
        }
      } catch (XPathExpressionException e) {
        String sMsg = "Error interrogating schema: "+schema.getKey();
        LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
      }
    }
    */
        
    if (locatedSchema != null) {
      return locatedSchema.duplicate();
    } else {
      throw new UnrecognizedSchemaException("Unrecognized metadata schema.");
    }
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName());
    if (size() == 0) {
      sb.append(" ()");
    } else {
      sb.append(" (\n");
      for (Schema member: values()) {
        sb.append(member).append("\n");
      }
      sb.append(") ===== end ").append(getClass().getName());
    }
    return sb.toString();
  }

}

