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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;

/** 
 * Represents the meaning associated with a discoverable property. 
 */
public class PropertyMeaning extends DiscoveryComponent {
  
  /** instance variables ====================================================== */
  private boolean                allowLeadingWildcard = true;
  private PropertyComparisonType comparisonType = PropertyComparisonType.KEYWORD;
  private DcElement              dcElement;
  private PropertyMeaningType    meaningType = PropertyMeaningType.UNKNOWN;
  private String                 name = "";
  private PropertyValueType      valueType = PropertyValueType.STRING;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs a property meaning.
   * @param name the property name
   * @param meaningType the property meaning type
   * @param valueType the property value type
   * @param comparisonType the property comparison type
   */  
  public PropertyMeaning(String name,
                         PropertyMeaningType meaningType, 
                         PropertyValueType valueType,
                         PropertyComparisonType comparisonType) {
    setName(name);
    setMeaningType(meaningType);
    setValueType(valueType);
    setComparisonType(comparisonType);
  }
  
  /** properties ============================================================== */
  
  /** 
   * Gets the status indicating whether or not "LIKE" expressions 
   * can begin with a wildcard for this property.
   * @return true if "LIKE" expressions can begin with a wildcard
   */
  public boolean getAllowLeadingWildcard() {
    return this.allowLeadingWildcard;
  }
  /** 
   * Sets the status indicating whether or not "LIKE" expressions 
   * can begin with a wildcard for this property.
   * @param allow true if "LIKE" expressions can begin with a wildcard
   */
  public void setAllowLeadingWildcard(boolean allow) {
    this.allowLeadingWildcard = allow;
  }
  
  /** 
   * Gets the comparison type for the property.
   * @return the property comparison type
   */
  public PropertyComparisonType getComparisonType() {
    return this.comparisonType;
  }
  /** 
   * Sets the comparison type for the property.
   * @param comparisonType the property comparison type
   */
  public void setComparisonType(PropertyComparisonType comparisonType) {
    if (comparisonType == null) {
      throw new IllegalArgumentException("The property comparison type can't be null.");
    }
    this.comparisonType = comparisonType;
  }
  
  /** 
   * Gets the Dublin Core element associated with this property.
   * @return the Dublin Core element
   */
  public DcElement getDcElement() {
    return dcElement;
  }
  /** 
   * Sets the Dublin Core element associated with this property.
   * @param element the Dublin Core element
   */
  public void setDcElement(DcElement element) {
    this.dcElement = element;
  }
  
  /** 
   * Gets the meaning type for the property.
   * @return the property meaning type
   */
  public PropertyMeaningType getMeaningType() {
    return meaningType;
  }
  
  /** 
   * Sets the meaning type for the property.
   * @param meaningType the property meaning type
   */
  public void setMeaningType(PropertyMeaningType meaningType) {
    if (meaningType == null) {
      throw new IllegalArgumentException("The property meaning type can't be null.");
    }
    this.meaningType = meaningType;
  }
  
  /** 
   * Gets the name of the meaning.
   * @return the associated name for the meaning
   */
  public String getName() {
    return name;
  }
  /** 
   * Sets the name of the meaning.
   * @param name the associated name for the meaning
   */
  public void setName(String name) {
    this.name = Val.chkStr(name);
  }
  
  /** 
   * Gets the value type for the property.
   * @return the property value type
   */
  public PropertyValueType getValueType() {
    return valueType;
  }
  
  /** 
   * Sets the value type for the property.
   * @param valueType the property value type
   */
  public void setValueType(PropertyValueType valueType) {
    if (valueType == null) {
      throw new IllegalArgumentException("The property value type can't be null.");
    }
    this.valueType = valueType;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    echo(sb,0);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent clause
   */
  public void echo(StringBuffer sb, int depth) {
    StringBuffer sbDepth = new StringBuffer();
    for (int i=0;i<2*depth;i++) sbDepth.append(" ");
    
    sb.append(sbDepth).append(getClass().getSimpleName()).append(":");
    sb.append("\n ").append(sbDepth);
    sb.append(" name=\"").append(getName()).append("\"");
    sb.append(" meaningType=").append(getMeaningType());
    sb.append(" valueType=").append(getValueType());
    sb.append(" comparisonType=").append(getComparisonType());
    if (this instanceof AnyText) {
      AnyText anyText = (AnyText)this;
      sb.append("\n ").append(sbDepth);
      sb.append(" namesToConsider=").append(anyText.getNamesToConsider());
    }
    if (this.getDcElement() != null) {
      getDcElement().echo(sb.append("\n"),depth+1);
    }
  }
  
  /* inner classes =========================================================== */
  
  /**
   * Any text meaning (for queries executed against multiple fields).
   */
  public static class AnyText extends PropertyMeaning {
    
    /** The property meaning names to consider for an any text query. */
    private StringSet namesToConsider = new StringSet();
    
    /** Default constructor. */
    public AnyText() {
      this("anyText");
    }
    
    /** Constructor with a supplied name. */
    public AnyText(String name) {
      super(name,
          PropertyMeaningType.ANYTEXT,PropertyValueType.STRING,PropertyComparisonType.TERMS);
    }
    
    /** Gets the property meaning names to consider for an any text query. */
    public StringSet getNamesToConsider() {
      return namesToConsider;
    }
    
    /** Gets the property meaning names to consider for an any text query. */
    public void setNamesToConsider(StringSet names) {
      namesToConsider = names;
    }
    
  }
  
  /**
   * Geometry property (Envelope based).
   */
  public static class Geometry extends PropertyMeaning {
    
    /** The default envelope. */
    private Envelope defaultEnvelope;
    
    /** Default constructor. */
    public Geometry() {
      this("geometry");
    }
    
    /** Constructor with a supplied name. */
    public Geometry(String name) {
      super(name,
          PropertyMeaningType.GEOMETRY,PropertyValueType.GEOMETRY,PropertyComparisonType.VALUE);
    }
    
    /** Gets the property meaning names to consider for an any text query. */
    public Envelope getDefaultEnvelope() {
      return this.defaultEnvelope;
    }
    
    /** Gets the property meaning names to consider for an any text query. */
    public void setDefaultEnvelope(Envelope defaultEnvelope) {
      this.defaultEnvelope = defaultEnvelope;
    }
    
  }
  
}