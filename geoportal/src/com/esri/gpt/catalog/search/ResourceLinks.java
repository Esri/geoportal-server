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
import com.esri.gpt.framework.jsf.FacesMap;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a collection of bind-able links associated with a document or resource. 
 */
public class ResourceLinks extends ArrayList<ResourceLink> {
   
  /** instance variables ====================================================== */
  private ResourceLink       icon = new ResourceLink();
  private ResourceLink       thumbnail = new ResourceLink();
  private Map<String,String> urlsByTag = null;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ResourceLinks() {}
  
  /** properties ================================================================= */
  
  /**
   * Gets the link to the classification icon.
   * @return the classification icon link
   */
  public ResourceLink getIcon() {
    return this.icon;
  }
  /**
   * Sets the link to the classification icon.
   * @param link the classification icon icon
   */
  public void setIcon(ResourceLink link) {
    this.icon = link;
  }
  
  /**
   * Gets the link to the thumbnail.
   * @return the thumbnail link
   */
  public ResourceLink getThumbnail() {
    return this.thumbnail;
  }
  /**
   * Sets the link to the thumbnail.
   * @param link the thumbnail link
   */
  public void setThumbnail(ResourceLink link) {
    this.thumbnail = link;
  }
  
  /**
   * Returns a Map interface configured to return a resource link URL 
   * based upon a supplied tag (as key).
   * <br/>Th map aids in JSF page usage.
   * <br/Example:<br/>
   * rendered="#{record.resourceLinksAsMap['website']}"
   * @return the map interface (urls keys by tag)
   */
  public Map<String,String> getUrlsByTag() {
    if (this.urlsByTag == null) {
      this.urlsByTag = new UrlsByTag(this);
    }
    return this.urlsByTag;
  }

  /** methods ================================================================= */
  
  /**
   * Adds a link to the collection.
   * @param link the link to add (null links will not be added)
   * @return true if the link was added
   */
  @Override
  public boolean add(ResourceLink link) {
    if (link != null) {
      return super.add(link);
    } else {
      return false;
    }
  }
  
  /**
   * Finds a resource link associated with a tag.
   * @param tag the resource link tag
   * @return the associated resource link (null if not found)
   */
  public ResourceLink findByTag(String tag) {
    tag = Val.chkStr(tag);
    for (ResourceLink link: this){
      if (link.getTag().equalsIgnoreCase(tag)) {
        return link;
      }
    }
    return null;
  }
  
  /**
   * Finds resource link url associated with a tag.
   * @param tag the resource link tag
   * @return url the resource link url (empty string if none was located)
   */
  public String findUrlByTag(String tag) {
    ResourceLink link = this.findByTag(tag);
    if (link != null) return link.getUrl();
    return "";
  }
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    if (size() == 0) {
      sb.append(" (No bindable-links.)");
    } else {
      for (ResourceLink member: this) {
        member.echo(sb.append("\n"));
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
  
  /** inner classes  ========================================================== */
  
  /**
   * Returns a Map interface configured to return a resource link URL 
   * based upon a supplied tag (as key).
   * <br/>Th map aids in JSF page usage.
   * <br/Example:<br/>
   * rendered="#{record.resourceLinks.urlsByTag['website']}"
   */
  class UrlsByTag extends FacesMap<String> {
    private ResourceLinks links;
  
    /**
     * Constructs based upon a supplied collection of resource links.
     * @param links the resource links
     */
    public UrlsByTag(ResourceLinks links) {
      this.links = links;
    }
  
    /**
     * Implements the "get" method for a Map to return the URL associated with
     * a tag.
     * <br/>The supplied tag should be a string.
     * @param tag the tag associated with the URL to find
     * @return the URL associated with the tag (empty id none)
     */
    @Override
    public String get(Object tag) {
      if ((tag != null) && (tag instanceof String)) {
        return this.links.findUrlByTag(String.valueOf(tag));
      }
      return "";
    }
  }

}
