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
package com.esri.gpt.catalog.search;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;

/** 
 * Represents a bind-able link associated with a document or resource. 
 */
public class ResourceLink {
  
  /** class variables ========================================================= */
  
  /** Add to map tag = "addToMap" */
  public static final String TAG_ADDTOMAP = "addToMap";
  
  /** ArcGIS kml tag = "agskml" */
  public static final String TAG_AGSKML = "agskml";
  
  /** ArcGIS lyr tag = "agslyr" */
  public static final String TAG_AGSLYR = "agslyr";
  
  /** ArcGIS nmf tag = "agsnmf" */
  public static final String TAG_AGSNMF = "agsnmf";
  
  /** Content type icon tag = "contentType" */
  public static final String TAG_CONTENTTYPE = "contentType";
  
  /** View details tag = "details" */
  public static final String TAG_DETAILS = "details";
  
  /** View metadata tag = "metadata" */
  public static final String TAG_METADATA = "metadata";
  
  /** Open resource tag = "open" */
  public static final String TAG_OPEN = "open";
  
  /** Preview resource tag = "preview" */
  public static final String TAG_PREVIEW = "preview";
  
  /** Tag preview helper.  Used to know if info should be generated **/
  public static final String TAG_PREVIEW_PARAM_INFO = "previewInfo";
  
  /** Thumbnail tag = "thumbnail" */
  public static final String TAG_THUMBNAIL = "thumbnail";
  
  /** Open website tag = "website" */
  public static final String TAG_WEBSITE = "website";
  
  /** Custom link tag = "customLink" **/
  public static final String TAG_CUSTOM = "customLink";
  
  /** Zoom link.  Used in JSF.  link tag = "zoomTo" **/
  public static final String TAG_ZOOMTO = "zoomTo";
  
  /** View metadata tag = "metadata" */
  public static final String TAG_RESOURCE = "resource";
  
  /** instance variables ====================================================== */
  private String  label = "";
  private String  labelResourceKey = "";
  private StringAttributeMap parameters = new StringAttributeMap();
  private String  tag = "";
  private String  target = "_blank";
  private String  url = "";
  private boolean isForExtenalRecord = false;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ResourceLink() {}
    
  /** properties ============================================================== */
  
  /**
   * Gets the isForExtenalRecord
 * @return the isForExtenalRecord
 */
public boolean isForExtenalRecord() {
	return isForExtenalRecord;
}

/**
 * Sets the isForExtenalRecord
 * @param isForExtenalRecord the isForExtenalRecord to set
 */
public void setForExtenalRecord(boolean isForExtenalRecord) {
	this.isForExtenalRecord = isForExtenalRecord;
}

/** 
   * Gets the label.
   * @return the label
   */
  public String getLabel() {
    return label;
  }
  /** 
   * Sets the label.
   * @param label the label
   */
  public void setLabel(String label) {
    this.label = Val.chkStr(label);
  }
  
  /** 
   * Gets the key associated with the label within a message property resource bundle.
   * @return the label's resource key
   */
  public String getLabelResourceKey() {
    return labelResourceKey;
  }
  /** 
   * Sets the key associated with the label within a message property resource bundle.
   * @param key the label's resource key
   */
  public void setLabelResourceKey(String key) {
    this.labelResourceKey = Val.chkStr(key);
  }
  
  /**
   * Gets the map of arbitrarily configured parameters associated with this link.
   * @return the map of parameters
   */
  public StringAttributeMap getParameters() {
    return this.parameters;
  }
  
  /** 
   * Gets an arbitrary tag associated with this link.
   * @return the tag
   */
  public String getTag() {
    return tag;
  }
  /** 
   * Sets an arbitrary tag associated with this link.
   * @param tag the tag
   */
  public void setTag(String tag) {
    this.tag = Val.chkStr(tag);
  }
  
  /** 
   * Gets the target.
   * @return the target
   */
  public String getTarget() {
    return target;
  }
  
  /** 
   * Sets the target.
   * @param target the Target
   */
  public void setTarget(String target) {
    this.target = target;
  }
  
  /** 
   * Gets the URL.
   * @return the URL
   */
  public String getUrl() {
    return url;
  }
  /** 
   * Sets the URL.
   * @param url the URL
   */
  public void setUrl(String url) {
    this.url = Val.chkStr(url);
  }
     
  /** methods ================================================================= */
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    sb.append(getClass().getSimpleName()).append(":\n");
    sb.append(" url=").append(this.getUrl()).append("");
    sb.append(" label=").append(this.getLabel()).append("");
    sb.append(" labelResourceKey=").append(this.getLabelResourceKey()).append("");
    sb.append(" tag=").append(getTag()).append("");
    if (getParameters().size() > 0) {
      sb.append("\n").append(getParameters().toString());
    }
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    echo(sb);
    return sb.toString();
  }

}