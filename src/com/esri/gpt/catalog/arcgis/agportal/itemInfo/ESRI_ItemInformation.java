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
package com.esri.gpt.catalog.arcgis.agportal.itemInfo;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Stores resource metadata information.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class ESRI_ItemInformation {

  /** instance variables ====================================================== */
  private String id = "";
  private String title = "";
  private String name = "";
  private String desc = "";
  private String snippet = "";
  private String url = "";
  private String type = "";
  private String owner = "";
  private String access = "";
  private String culture = "";
  private List<String> keywords = new ArrayList<String>();
  private List<String> tags = new ArrayList<String>();
  private String resourceUrl = "";
  private String thumbnailUrl = "";
  private Envelope extent = new Envelope();
  private Date   modifiedDate;

  /**
   * Creates instance of the item information
   */
  public ESRI_ItemInformation() {
  }

  /**
   * properties variables ======================================================
   */
  /**
   * Gets name.
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = Val.chkStr(name);
  }

  /**
   * Gets thumbnail URL.
   * @return thumbnail URL
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * Sets thumbnail URL
   * @param thumbnailUrl
   *          the thumbnail URL to set
   */
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = Val.chkStr(thumbnailUrl);
  }

  /**
   * Gets id.
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = Val.chkStr(id);
  }

  /**
   * Gets title.
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets title.
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = Val.chkStr(title);
  }

  /**
   * Gets description.
   * @return the description
   */
  public String getDesc() {
    return desc;
  }

  /**
   * Sets description.
   * @param desc
   *          the description to set
   */
  public void setDesc(String desc) {
    this.desc = Val.chkStr(desc);
  }

  /**
   * Gets snippet.
   * @return snippet
   */
  public String getSnippet() {
    return snippet;
  }

  /**
   * Sets snippet.
   * @param snippet snippet
   */
  public void setSnippet(String snippet) {
    this.snippet = Val.chkStr(snippet);
  }

  /**
   * Gets URL.
   * @return  URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets URL.
   * @param url URL
   */
  public void setUrl(String url) {
    this.url = Val.chkStr(url);
  }

  /**
   * Gets keywords.
   * @return the keywords list of keywords
   */
  public List<String> getKeywords() {
    return keywords;
  }

  /**
   * Gets keywords as string.
   * @return keywords as space separated string
   */
  public String getKeywordsAsString() {
    return asString(keywords, " ");
  }

  /**
   * Sets keywords.
   * @param keywords list of keywords
   */
  public void setKeywords(List<String> keywords) {
    this.keywords = keywords != null ? keywords : new ArrayList<String>();
  }

  /**
   * Gets type.
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets type.
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = Val.chkStr(type);
  }

  /**
   * Gets tags as string.
   * @return tags as space separated string
   */
  public String getTagsAsString() {
    return asString(tags, " ");
  }

  /**
   * Gets tags.
   * @return list of tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Sets tags.
   * @param tags
   *          list of tags
   */
  public void setTags(List<String> tags) {
    this.tags = tags != null ? tags : new ArrayList<String>();
  }

  /**
   * Sets resource URL.
   * @param resourceUrl resource URL
   */
  public void setResourceUrl(String resourceUrl) {
    this.resourceUrl = Val.chkStr(resourceUrl);
  }

  /**
   * Gets resource URL.
   * @return resource URL
   */
  public String getResourceUrl() {
    return resourceUrl;
  }

  /**
   * Gets modified date.
   * @return  modified date
   */
  public Date getModifiedDate() {
    return modifiedDate;
  }
  
  /**
   * Sets modified date.
   * @param date  modified date
   */
  public void setModifiedDate(Date date) {
    this.modifiedDate = date;
  }
  
  /**
   * Sets extent.
   * @param extent extent
   */
  public void setExtent(Envelope extent) {
    this.extent = extent;
  }

  /**
   * Gets extent.
   * @return extent
   */
  public Envelope getExtent() {
    return extent;
  }

  /**
   * Gets access.
   * @return access
   */
  public String getAccess() {
    return access;
  }

  /**
   * Sets access.
   * @param access access
   */
  public void setAccess(String access) {
    this.access = Val.chkStr(access);
  }

  /**
   * Gets culture.
   * @return culture
   */
  public String getCulture() {
    return culture;
  }

  /**
   * Sets culture.
   * @param culture culture
   */
  public void setCulture(String culture) {
    this.culture = Val.chkStr(culture);
  }

  /**
   * Gets owner.
   * @return owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets owner.
   * @param owner owner
   */
  public void setOwner(String owner) {
    this.owner = Val.chkStr(owner);
  }

  /**
   * Gets array of strings as a single string.
   * @param array array of strings
   * @param separator separator
   * @return 
   */
  private String asString(List<String> array, String separator) {
    separator = Val.chkStr(separator);
    if (separator.length() == 0) {
      separator = " ";
    }
    StringBuilder sb = new StringBuilder();
    if (array != null) {
      for (String a : array) {
        a = Val.chkStr(a);
        if (a.length() > 0) {
          if (sb.length() > 0) {
            sb.append(separator);
          }
          sb.append(a);
        }
      }
    }
    return sb.toString();
  }
}
