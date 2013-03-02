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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A tree structure or properties with subject, predicate and value.
 */
public class AsnProperty {
  
  /** instance variables ====================================================== */
  private AsnProperties children = new AsnProperties();
  private String        predicate;
  private boolean       quoteJsonValue = true;
  private String        subject;
  private String        value;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnProperty() {}
  
  /**
   * Constructs with a subject, predicate and value.
   * @param subject the subject
   * @param predicate the predicate
   * @param value the value
   */
  public AsnProperty(String subject, String predicate, String value) {
    this.setSubject(subject);
    this.setPredicate(predicate);
    this.setValue(value);
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnProperty(AsnProperty objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setSubject(objectToDuplicate.getSubject());
      this.setPredicate(objectToDuplicate.getPredicate());
      this.setValue(objectToDuplicate.getValue());
      if (objectToDuplicate.hasChildren()) {
        this.setChildren(objectToDuplicate.getChildren().duplicate());
      }
    }
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the child properties.
   * @return the child properties
   */
  public AsnProperties getChildren() {
    return this.children;
  }
  /**
   * Sets the child properties.
   * @param children the child properties
   */
  public void setChildren(AsnProperties children) {
    this.children = children;
  }
  
  /**
   * Gets the predicate.
   * @return the predicate
   */
  public String getPredicate() {
    return this.predicate;
  }
  /**
   * Sets the predicate.
   * @param predicate the predicate
   */
  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }
  
  /**
   * Gets the flag indicating whether or not the JSON value should be quoted.
   * @return <code>true</code> if quoted (i.e. a Javascript string)
   */
  public boolean getQuoteJsonValue() {
    return this.quoteJsonValue;
  }
  /**
   * Sets the flag indicating whether or not the JSON value should be quoted.
   * @param quote <code>true</code> if quoted (i.e. a Javascript string)
   */
  public void setQuoteJsonValue(boolean quote) {
    this.quoteJsonValue = quote;
  }
  
  /**
   * Gets the subject.
   * @return the subject
   */
  public String getSubject() {
    return this.subject;
  }
  /**
   * Sets the subject.
   * @param subject the subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
  
  /**
   * Gets the value.
   * @return the value
   */
  public String getValue() {
    return this.value;
  }
  /**
   * Sets the value.
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }
  
  /** methods ================================================================= */
  
  /**
   * Returns a JSON representation of the property.
   * @return the JSON string
   * @throws IOException if an exception occurs
   */
  public String asJson() throws IOException {
    PrintWriter pw = null;
    try {
      StringWriter sw = new StringWriter();
      pw = new PrintWriter(sw);
      this.toJson(pw,0,true);
      pw.flush();
      return sw.toString();
    } finally {
      try {if (pw != null) pw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Returns an XML representation of the property.
   * @return the XML string
   * @throws IOException if an exception occurs
   */
  public String asXml() throws IOException {
    PrintWriter pw = null;
    try {
      StringWriter sw = new StringWriter();
      pw = new PrintWriter(sw);
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      this.toXml(pw,0);
      pw.flush();
      return sw.toString();
    } finally {
      try {if (pw != null) pw.close();} catch (Exception ef) {}
    }
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnProperty(this);
   * @return the duplicated object
   */
  public AsnProperty duplicate() {
    return new AsnProperty(this);
  }
  
  /**
   * Gets the child properties (ensures that the collection has been instantiated).
   * @return the child properties
   */
  public AsnProperties ensureChildren() {
    if (this.getChildren() == null) {
      this.setChildren(new AsnProperties());
    }
    return this.getChildren();
  }
  
  /**
   * Determines whether or not this property has children.
   * @return <code>true</code> if this property has children
   */
  public boolean hasChildren() {
    return (this.getChildren() != null) && (this.getChildren().size() > 0);
  }
  
  /**
   * Writes a JSON representation of the property.
   * @param writer the writer
   * @param depth the depth of the parent
   * @throws IOException if an exception occurs
   */
  public void toJson(PrintWriter writer, int depth, boolean isLast) throws IOException {
    String pfx = "";
    for (int i=0;i<2*depth;i++) pfx += " ";
    
    String s = Val.chkStr(this.getSubject());
    String p = Val.chkStr(this.getPredicate());
    String v = Val.chkStr(this.getValue());
    boolean hc = this.hasChildren();
    String line;
    
    writer.println(pfx+"{");
    if (s.length() > 0) {
      line = pfx+"  \"subject\": \""+Val.escapeStrForJson(s)+"\"";
      if (hc || (p.length() > 0) || (v.length() > 0)) line +=",";
      writer.println(line);
    }
    if (p.length() > 0) {
      line = pfx+"  \"predicate\": \""+Val.escapeStrForJson(p)+"\"";
      if (hc || (v.length() > 0)) line +=",";
      writer.println(line);
    }
    if (v.length() > 0) {
      if (this.getQuoteJsonValue()) {
        line = pfx+"  \"value\": \""+Val.escapeStrForJson(v)+"\"";
      } else {
        line = pfx+"  \"value\": "+Val.escapeStrForJson(v);
      }
      if (hc) line +=",";
      writer.println(line);
    }
    
    if (hc) {  
      line = pfx+"  \"properties\": [";
      writer.println(line);
      for (int i=0; i<this.getChildren().size(); i++) {
        AsnProperty child = this.getChildren().get(i);
        boolean bLast = (i >= (this.getChildren().size() - 1));
        child.toJson(writer,(depth+2),bLast);
      }
      writer.println(pfx+"  ]");
    }
    
    line = pfx+"}";
    if (!isLast) line +=",";
    writer.println(line);
  }
    
  /**
   * Writes an XML representation of the property.
   * @param writer the writer
   * @param depth the depth of the parent
   * @throws IOException if an exception occurs
   */
  public void toXml(PrintWriter writer, int depth) throws IOException {
    String pfx = "";
    for (int i=0;i<2*depth;i++) pfx += " ";
    
    String s = Val.chkStr(this.getSubject());
    String p = Val.chkStr(this.getPredicate());
    String v = Val.chkStr(this.getValue());
    
    writer.print(pfx+"<property");
    if (s.length() > 0) {
      writer.print(" subject=\""+Val.escapeXml(s)+"\"");
    }
    if (p.length() > 0) {
      writer.print(" predicate=\""+Val.escapeXml(p)+"\"");
    }
    
    if (this.hasChildren()) {
      if (v.length() > 0) {
        writer.print(">");
        writer.println(Val.escapeXml(v));
      } else {
        writer.println(">");
      }  
      for (AsnProperty child: this.getChildren()) {
        child.toXml(writer,(depth+1));
      }
      if (depth == 0) {
        writer.print(pfx+"</property>");
      } else {
        writer.println(pfx+"</property>");
      }
      
    } else if (v.length() > 0) { 
      writer.print(">");
      writer.print(Val.escapeXml(v));
      if (depth == 0) {
        writer.print("</property>");
      } else {
        writer.println("</property>");
      }
      
    } else {
      if (depth == 0) {
        writer.print("/>");
      } else {
        writer.println("/>");
      }
    }
  }
  
}
