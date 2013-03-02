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

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.UUID;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Represents the system fields associated with a assertion.
 */
public class AsnSystemPart {
  
  /** instance variables ====================================================== */
  private String    assertionId;
  private Timestamp editTimestamp; 
  private boolean   enabled = true;
  private String    resourceId;
  private Timestamp timestamp; 
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnSystemPart() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnSystemPart(AsnSystemPart objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setAssertionId(objectToDuplicate.getAssertionId());
      this.setEditTimestamp(objectToDuplicate.getEditTimestamp());
      this.setEnabled(objectToDuplicate.getEnabled());
      this.setResourceId(objectToDuplicate.getResourceId());
      this.setTimestamp(objectToDuplicate.getTimestamp());
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the assertion id (primary key).
   * @return the assertion id
   */
  public String getAssertionId() {
    return this.assertionId;
  }
  /**
   * Sets the assertion id (primary key).
   * @param assertionId the assertion id
   */
  public void setAssertionId(String assertionId) {
    this.assertionId = assertionId;
  }
  
  /**
   * Gets the edit timestamp (last value edit).
   * @return the timestamp
   */
  public Timestamp getEditTimestamp() {
    return this.editTimestamp;
  }
  /**
   * Sets the edit timestamp (last value edit).
   * @param timestamp the timestamp
   */
  public void setEditTimestamp(Timestamp timestamp) {
    this.editTimestamp = timestamp;
  }
  
  /**
   * Gets the flag indicating whether or not the assertion is enabled.
   * @return <code>true</code> if enabled
   */
  public boolean getEnabled() {
    return this.enabled;
  }
  /**
   * Sets the flag indicating whether or not the assertion is enabled.
   * @param enabled <code>true</code> if enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  /**
   * Gets the associated resource document uuid.
   * @return the resource document uuid
   */
  public String getResourceId() {
    return this.resourceId;
  }
  /**
   * Sets the associated resource document id.
   * @param resourceId the resource document id
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
  
  /**
   * Gets the timestamp.
   * @return the timestamp
   */
  public Timestamp getTimestamp() {
    return this.timestamp;
  }
  /**
   * Sets the timestamp.
   * @param timestamp the timestamp
   */
  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends fields to a document for indexing.
   * @param document the document
   */
  public void appendWritableFields(Document document) {
    Field fld;
    String val;
    long millis;
    
    // sys.assertionid
    val = Val.chkStr(this.getAssertionId());
    if (val.length() == 0) {
      this.setAssertionId(UUID.randomUUID().toString());
      val = Val.chkStr(this.getAssertionId());
    }
    fld = new Field(AsnConstants.FIELD_SYS_ASSERTIONID,val,
        Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
    document.add(fld);
    
    // sys.edit.timestamp
    if (this.getEditTimestamp() != null) {
      millis = this.getEditTimestamp().getTime();
      val = DateTools.timeToString(millis,DateTools.Resolution.MILLISECOND);
      fld = new Field(AsnConstants.FIELD_SYS_EDIT_TIMESTAMP,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // sys.enabled
    if (!this.getEnabled()) {
      fld = new Field(AsnConstants.FIELD_SYS_ENABLED,"false",
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // sys.resourceid
    val = Val.chkStr(this.getResourceId());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_SYS_RESOURCEID,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // sys.timestamp
    if (this.getTimestamp() == null) {
      this.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }
    millis = this.getTimestamp().getTime();
    val = DateTools.timeToString(millis,DateTools.Resolution.MILLISECOND);
    fld = new Field(AsnConstants.FIELD_SYS_TIMESTAMP,val,
        Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
    document.add(fld);
    
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnSystemPart(this);
   * @return the duplicated object
   */
  public AsnSystemPart duplicate() {
    return new AsnSystemPart(this);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent
   */
  public void echo(StringBuffer sb, int depth) {
    String pfx = "\n";
    for (int i=0;i<2*depth;i++) pfx += " ";
    sb.append(pfx).append(AsnConstants.FIELD_SYS_ASSERTIONID+"=").append(this.getAssertionId());
    sb.append(pfx).append(AsnConstants.FIELD_SYS_ENABLED+"=").append(this.getEnabled());
    sb.append(pfx).append(AsnConstants.FIELD_SYS_RESOURCEID+"=").append(this.getResourceId());
    sb.append(pfx).append(AsnConstants.FIELD_SYS_TIMESTAMP+"=").append(this.getTimestamp());
    sb.append(pfx).append(AsnConstants.FIELD_SYS_EDIT_TIMESTAMP+"=").append(this.getEditTimestamp());
  }
  
  /**
   * Reads the fields of an indexed document.
   * @param document the document
   */
  public void readFields(Document document) {
    String val;
    this.setAssertionId(document.get(AsnConstants.FIELD_SYS_ASSERTIONID));
    this.setResourceId(document.get(AsnConstants.FIELD_SYS_RESOURCEID));
    
    val = Val.chkStr(document.get(AsnConstants.FIELD_SYS_ENABLED));
    this.setEnabled(!val.equals("false"));
    
    val = document.get(AsnConstants.FIELD_SYS_TIMESTAMP);
    if ((val != null) && (val.length() > 0)) {
      try {
        long millis = DateTools.stringToTime(val);
        this.setTimestamp(new Timestamp(millis));
      } catch (ParseException e) {
        // not fatal
      }
    }
    
    val = document.get(AsnConstants.FIELD_SYS_EDIT_TIMESTAMP);
    if ((val != null) && (val.length() > 0)) {
      try {
        long millis = DateTools.stringToTime(val);
        this.setEditTimestamp(new Timestamp(millis));
      } catch (ParseException e) {
        // not fatal
      }
    }
    
  }

}
