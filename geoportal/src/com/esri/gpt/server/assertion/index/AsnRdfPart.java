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
package com.esri.gpt.server.assertion.index;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Represents the RDF fields associated with a assertion.
 */
public class AsnRdfPart {
  
  /** instance variables ====================================================== */
  private boolean analyzeValue = false;
  private String  predicate;
  private String  subject;
  private String  value;
  private String  valueField;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnRdfPart() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnRdfPart(AsnRdfPart objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setAnalyzeValue(objectToDuplicate.getAnalyzeValue());
      this.setPredicate(objectToDuplicate.getPredicate());
      this.setSubject(objectToDuplicate.getSubject());
      this.setValue(objectToDuplicate.getValue());
      this.setValueField(objectToDuplicate.getValueField());
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not the value should be analyzed prior to indexing.
   * @return <code>true</code> if the value should be analyzed
   */
  public boolean getAnalyzeValue() {
    return this.analyzeValue;
  }
  /**
   * Sets the flag indicating whether or not the value should be analyzed prior to indexing.
   * @param analyzeValue <code>true</code> if the value should be analyzed
   */
  public void setAnalyzeValue(boolean analyzeValue) {
    this.analyzeValue = analyzeValue;
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
  
  /**
   * Gets the value field name.
   * @return the value field
   */
  public String getValueField() {
    if ((this.valueField == null) || (this.valueField.length() == 0)) {
      return AsnConstants.FIELD_RDF_VALUE;
    }
    return this.valueField;
  }
  /**
   * Sets the value field name.
   * @param valueField the value field
   */
  public void setValueField(String valueField) {
    this.valueField = valueField;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends fields to a document for indexing.
   * @param document the document
   */
  public void appendWritableFields(Document document) {
    Field fld;
    String val;
    
    // rdf.subject
    val = Val.chkStr(this.getSubject());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_RDF_SUBJECT,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // rdf.predicate
    val = Val.chkStr(this.getPredicate());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_RDF_PREDICATE,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // rdf.value
    val = Val.chkStr(this.getValue());
    if (val.length() > 0) {
      Field.Index opt = Field.Index.NOT_ANALYZED;
      if (this.getAnalyzeValue()) {
        opt = Field.Index.ANALYZED;
      }
      fld = new Field(this.getValueField(),val,Field.Store.YES,opt,Field.TermVector.NO);
      document.add(fld);
    }
    
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnRdfPart(this);
   * @return the duplicated object
   */
  public AsnRdfPart duplicate() {
    return new AsnRdfPart(this);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent
   */
  public void echo(StringBuffer sb, int depth) {
    String pfx = "\n";
    for (int i=0;i<2*depth;i++) pfx += " ";
    sb.append(pfx).append(AsnConstants.FIELD_RDF_SUBJECT+"=").append(this.getSubject());
    sb.append(pfx).append(AsnConstants.FIELD_RDF_PREDICATE+"=").append(this.getPredicate());
    sb.append(pfx).append(this.getValueField()+"=").append(this.getValue());
  }
  
  /**
   * Reads the fields of an indexed document.
   * @param document the document
   */
  public void readFields(Document document) {
    this.setSubject(document.get(AsnConstants.FIELD_RDF_SUBJECT));
    this.setPredicate(document.get(AsnConstants.FIELD_RDF_PREDICATE));
    this.setValue(document.get(this.getValueField()));
  }

}
