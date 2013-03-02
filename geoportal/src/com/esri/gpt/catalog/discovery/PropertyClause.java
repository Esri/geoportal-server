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

/**
 * A clause that compares a stored property to a supplied literal.
 */
public class PropertyClause extends DiscoveryClause {
    
  /** instance variables ====================================================== */
  private String literal;
  private Discoverable target;
      
  /** constructors ============================================================ */
  
  /** Default constructor. */
  private PropertyClause() {}
  
  /** properties ============================================================== */
  
  /** 
   * Gets the literal value that constrains the clause. 
   * @return the literal value
   */
  public String getLiteral() {
    return literal;
  }
  /**
   * Sets the literal value that constrains the clause. 
   * @param literal the literal value
   */
  public void setLiteral(String literal) {
    this.literal = literal;
  }
  
  /** 
   * Gets the discoverable property targeted by the clause. 
   * @return the target
   */
  public Discoverable getTarget() {
    return target;
  }
  
  /** 
   * Sets the discoverable property targeted by the clause. 
   * @param target the target
   */
  public void setTarget(Discoverable target) {
    this.target = target;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the clause
   */
  @Override
  public void echo(StringBuffer sb, int depth) {
    StringBuffer sbDepth = new StringBuffer();
    for (int i=0;i<2*depth;i++) sbDepth.append(" ");
    
    sb.append(sbDepth).append(getClass().getSimpleName()).append(":");
    if (this instanceof PropertyIsBetween) {
      PropertyIsBetween between = (PropertyIsBetween)this;
      sb.append("\n").append(sbDepth);
      sb.append("  lowerBoundary=\"").append(
          between.getLowerBoundary()).append("\"");
      sb.append("\n").append(sbDepth);
      sb.append("  upperBoundary=\"").append(
          between.getUpperBoundary()).append("\"");
    } else {
      sb.append("\n").append(sbDepth);
      sb.append("  literal=\"").append(getLiteral()).append("\"");
      if (this instanceof PropertyIsLike) {
        PropertyIsLike like = (PropertyIsLike)this;
        sb.append("\n").append(sbDepth).append(" ");
        sb.append(" escapeChar=\"").append(like.getEscapeChar()).append("\"");
        sb.append(" singleChar=\"").append(like.getSingleChar()).append("\"");
        sb.append(" wildCard=\"").append(like.getWildCard()).append("\"");
      }
    }
    getTarget().echo(sb.append("\n"),depth+1);
  }
  
  /** inner classes =========================================================== */
  
  /** A property comparison between two literals. */
  public static class PropertyIsBetween extends PropertyClause {
    
    /* instance variables */
    private String lowerBoundary;
    private String upperBoundary;
    
    /** Default constructor. */
    public PropertyIsBetween() {super();}
    
    /** Gets the lower boundary. */
    public String getLowerBoundary() {return lowerBoundary;}

    /** Sets the lower boundary. */
    public void setLowerBoundary(String value) {lowerBoundary = value;}
    
    /** Gets the upper boundary. */
    public String getUpperBoundary() {return upperBoundary;}
    
    /** Sets the upper boundary. */
    public void setUpperBoundary(String value) {upperBoundary = value;}
  }
  
  /** A property is equal to a literal comparison. */
  public static class PropertyIsEqualTo extends PropertyClause {
    public PropertyIsEqualTo() {super();}
  }
  
  /** A property is greater than a literal comparison. */
  public static class PropertyIsGreaterThan extends PropertyClause {
    public PropertyIsGreaterThan() {super();}
  }
  
  /** A property is greater than or equal to a literal comparison. */
  public static class PropertyIsGreaterThanOrEqualTo extends PropertyClause {
    public PropertyIsGreaterThanOrEqualTo() {super();}
  }
  
  /** A property is less than a literal comparison. */
  public static class PropertyIsLessThan extends PropertyClause {
    public PropertyIsLessThan() {super();}
  }
  
  /** A property is less than or equal to a literal comparison. */
  public static class PropertyIsLessThanOrEqualTo extends PropertyClause {
    public PropertyIsLessThanOrEqualTo() {super();}
  }
  
  /** A property is like a literal comparison. */
  public static class PropertyIsLike extends PropertyClause {
    
    /* instance variables */
    private String escapeChar = "";
    private String singleChar = "";
    private String wildCard = "";
    
    /** Default constructor. */
    public PropertyIsLike() {super();}
    
    /** Gets the escape character.*/
    public String getEscapeChar() {return escapeChar;}
    
    /** Sets the escape character. */
    public void setEscapeChar(String escapeChar) {
      this.escapeChar = escapeChar;
      if (this.escapeChar.length() > 1) this.escapeChar = "";
    }
    
    /** Gets the single pattern match character. */
    public String getSingleChar() {return singleChar;}
    
    /** Sets the single pattern match character. */
    public void setSingleChar(String singleChar) {
      this.singleChar = singleChar;
      if (this.singleChar.length() > 1) this.singleChar = "";
    }
    
    /** Gets the wild card character. */
    public String getWildCard() {return wildCard;}
    
    /** Sets the wild card character. */
    public void setWildCard(String wildCard) {
      this.wildCard = wildCard;
      if (this.wildCard.length() > 1) this.wildCard = "";
    }
  }
  
  /** A property is not equal to a literal comparison. */
  public static class PropertyIsNotEqualTo extends PropertyClause {
    public PropertyIsNotEqualTo() {super();}
  }
  
  /** A property is null comparison */
  public static class PropertyIsNull extends PropertyClause {
    public PropertyIsNull() {super();}
  }
  
}
