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
 * Represents the user fields associated with a assertion.
 */
public class AsnUserPart {
  
  /** instance variables ====================================================== */
  private String id;
  private String ipAddress;
  private String key;
  private String name;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnUserPart() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnUserPart(AsnUserPart objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setID(objectToDuplicate.getID());
      this.setIPAddress(objectToDuplicate.getIPAddress());
      this.setKey(objectToDuplicate.getKey());
      this.setName(objectToDuplicate.getName());
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the user id (local geoportal database id).
   * @return the user id
   */
  public String getID() {
    return this.id;
  }
  /**
   * Sets the user id (local geoportal database id).
   * @param id the user id
   */
  public void setID(String id) {
    this.id = id;
  }
  
  /**
   * Gets the IP address associated with the user.
   * @return the IP address
   */
  public String getIPAddress() {
    return this.ipAddress;
  }
  /**
   * Sets the IP address associated with the user.
   * @param ipAddress the IP address
   */
  public void setIPAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
    
  /**
   * Gets the user key (typically the distinguished name).
   * @return the user key
   */
  public String getKey() {
    return this.key;
  }
  /**
   * Sets the user key (typically the distinguished name).
   * @param key the user key
   */
  public void setKey(String key) {
    this.key = key;
  }
  
  /**
   * Gets the username.
   * @return the username
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the username.
   * @param username the username
   */
  public void setName(String username) {
    this.name = username;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends fields to a document for indexing.
   * @param document the document
   */
  public void appendWritableFields(Document document) {
    Field fld;
    String val;
    
    // user.id
    val = Val.chkStr(this.getID());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_USER_ID,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // user.ipaddress
    val = Val.chkStr(this.getIPAddress());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_USER_IPADDRESS,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // user.key
    val = Val.chkStr(this.getKey());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_USER_KEY,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
    // user.name
    val = Val.chkStr(this.getName());
    if (val.length() > 0) {
      fld = new Field(AsnConstants.FIELD_USER_NAME,val,
          Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
      document.add(fld);
    }
    
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnUserPart(this);
   * @return the duplicated object
   */
  public AsnUserPart duplicate() {
    return new AsnUserPart(this);
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent
   */
  public void echo(StringBuffer sb, int depth) {
    String pfx = "\n";
    for (int i=0;i<2*depth;i++) pfx += " ";
    sb.append(pfx).append(AsnConstants.FIELD_USER_ID+"=").append(this.getID());
    sb.append(pfx).append(AsnConstants.FIELD_USER_IPADDRESS+"=").append(this.getIPAddress());
    sb.append(pfx).append(AsnConstants.FIELD_USER_KEY+"=").append(this.getKey());
    sb.append(pfx).append(AsnConstants.FIELD_USER_NAME+"=").append(this.getName());
  }
  
  /**
   * Reads the fields of an indexed document.
   * @param document the document
   */
  public void readFields(Document document) {
    this.setID(document.get(AsnConstants.FIELD_USER_ID));
    this.setIPAddress(document.get(AsnConstants.FIELD_USER_IPADDRESS));
    this.setKey(document.get(AsnConstants.FIELD_USER_KEY));
    this.setName(document.get(AsnConstants.FIELD_USER_NAME));
  }

}
