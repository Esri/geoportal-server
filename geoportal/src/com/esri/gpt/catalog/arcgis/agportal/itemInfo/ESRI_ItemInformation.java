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
 * @see <a href="http://dev.arcgisonline.com/apidocs/sharing/useritem.html">http://dev.arcgisonline.com/apidocs/sharing/useritem.html</a>
 */
public class ESRI_ItemInformation {

  /** instance variables ====================================================== */
  private String name = "";
  private String title = "";
  private String thumbnail = "";
  private String thumbnailUrl = "";
  private String type = "";
  private List<String> typeKeywords = new ArrayList<String>();
  private String description = "";
  private List<String> tags = new ArrayList<String>();
  private String snippet = "";
  private Envelope extent = new Envelope();
  private String accessInformation = "";
  private String licenseInfo = "";
  private String culture = "";
  
  private String id = "";
  private String item = "";
  private String itemType = "";
  private String owner = "";
  private Date   uploadedDate;
  private Date   modifiedDate;
  private String guid = "";
  private String url = "";
  private String access = "";
  private long   size;
  private long   numComments;
  private long   numRatings;
  private double avgRating;
  private long   numViews;
  private String sharingAccess = "";
  private List<String> sharingGroups = new ArrayList<String>();
  private String documentation = "";

  
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
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   * @param name name
   */
  public void setName(String name) {
    this.name = Val.chkStr(name);
  }

  /**
   * Gets thumbnail.
   * @return thumbnail
   */
  public String getThumbnail() {
    return thumbnail;
  }

  /**
   * Sets thumbnail.
   * @param thumbnail thumbnail
   */
  public void setThumbnail(String thumbnail) {
    this.thumbnail = Val.chkStr(thumbnail);
  }

  /**
   * Gets thumbnail URL.
   * @return thumbnail URL
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * Sets thumbnail URL.
   * @param thumbnailUrl thumbnail URL
   */
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = Val.chkStr(thumbnailUrl);
  }

  /**
   * Gets id.
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   * @param id id
   */
  public void setId(String id) {
    this.id = Val.chkStr(id);
  }

  /**
   * Gets item.
   * @return item
   */
  public String getItem() {
    return item;
  }

  /**
   * Sets item.
   * @param item item
   */
  public void setItem(String item) {
    this.item = Val.chkStr(item);
  }

  /**
   * Gets item type.
   * @return item type
   */
  public String getItemType() {
    return itemType;
  }

  /**
   * Sets item type.
   * @param itemType item type 
   */
  public void setItemType(String itemType) {
    this.itemType = Val.chkStr(itemType);
  }

  /**
   * Gets title.
   * @return title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets title.
   * @param title title
   */
  public void setTitle(String title) {
    this.title = Val.chkStr(title);
  }

  /**
   * Gets description.
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description.
   * @param description description
   */
  public void setDescription(String description) {
    this.description = Val.chkStr(description);
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
   * Gets typeKeywords.
   * @return the typeKeywords list of typeKeywords
   */
  public List<String> getTypeKeywords() {
    return typeKeywords;
  }

  /**
   * Gets typeKeywords as string.
   * @return typeKeywords as space separated string
   */
  public String getTypeKeywordsAsString() {
    return asString(typeKeywords, " ");
  }

  /**
   * Sets typeKeywords.
   * @param typeKeywords list of typeKeywords
   */
  public void setTypeKeywords(List<String> typeKeywords) {
    this.typeKeywords = typeKeywords != null ? typeKeywords : new ArrayList<String>();
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
    return asString(tags, ", ");
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
   * @param tags list of tags
   */
  public void setTags(List<String> tags) {
    this.tags = tags != null ? tags : new ArrayList<String>();
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
   * Gets uploaded date.
   * @return uploaded date
   */
  public Date getUploadedDate() {
    return uploadedDate;
  }

  /**
   * Sets uploaded date.
   * @param date uploaded date
   */
  public void setUploadedDate(Date date) {
    this.uploadedDate = date;
  }
  
  /**
   * Sets extent.
   * @param extent extent
   */
  public void setExtent(Envelope extent) {
    this.extent = extent!=null? extent: new Envelope();
  }

  /**
   * Gets extent.
   * @return extent
   */
  public Envelope getExtent() {
    return extent;
  }

  /**
   * Gets accessInformation.
   * @return accessInformation
   */
  public String getAccessInformation() {
    return accessInformation;
  }

  /**
   * Sets accessInformation.
   * @param accessInformation accessInformation
   */
  public void setAccessInformation(String accessInformation) {
    this.accessInformation = Val.chkStr(accessInformation);
  }

  /**
   * Gets license info.
   * @return license info
   */
  public String getLicenseInfo() {
    return licenseInfo;
  }

  /**
   * Sets license info.
   * @param licenseInfo license info
   */
  public void setLicenseInfo(String licenseInfo) {
    this.licenseInfo = Val.chkStr(licenseInfo);
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
   * Gets average rating.
   * @return average rating
   */
  public double getAvgRating() {
    return avgRating;
  }

  /**
   * Sets average rating.
   * @param avgRating average rating
   */
  public void setAvgRating(double avgRating) {
    this.avgRating = avgRating;
  }

  /**
   * Gets number of comments.
   * @return number of comments
   */
  public long getNumComments() {
    return numComments;
  }

  /**
   * Sets number of comments.
   * @param numComments number of comments
   */
  public void setNumComments(long numComments) {
    this.numComments = numComments;
  }

  /**
   * Gets number of ratings.
   * @return number of ratings
   */
  public long getNumRatings() {
    return numRatings;
  }

  /**
   * Sets number of ratings.
   * @param numRatings number of ratings
   */
  public void setNumRatings(long numRatings) {
    this.numRatings = numRatings;
  }

  /**
   * Gets number of reviews.
   * @return number of reviews
   */
  public long getNumViews() {
    return numViews;
  }

  /**
   * Sets number of reviews.
   * @param numViews number of reviews
   */
  public void setNumViews(long numViews) {
    this.numViews = numViews;
  }

  /**
   * Gets sharing access.
   * @return sharing access
   */
  public String getSharingAccess() {
    return sharingAccess;
  }

  /**
   * Sets sharing access.
   * @param sharingAccess sharing access 
   */
  public void setSharingAccess(String sharingAccess) {
    this.sharingAccess = Val.chkStr(sharingAccess);
  }

  /**
   * Gets size.
   * @return size
   */
  public long getSize() {
    return size;
  }

  /**
   * Sets size.
   * @param size size
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Gets guid.
   * @return guid
   */
  public String getGuid() {
    return guid;
  }

  /**
   * Sets guid.
   * @param guid guid
   */
  public void setGuid(String guid) {
    this.guid = Val.chkStr(guid);
  }

  /**
   * Gets sharingGroups.
   * @return list of sharingGroups
   */
  public List<String> getSharingGroups() {
    return sharingGroups;
  }

  /**
   * Sets sharingGroups
   * @param sharingGroups list of sharingGroups
   */
  public void setSharingGroups(List<String> sharingGroups) {
    this.sharingGroups = sharingGroups!=null? sharingGroups: new ArrayList<String>();
  }

  /**
   * Gets documentation.
   * @return documentation
   */
  public String getDocumentation() {
    return documentation;
  }

  /**
   * Sets documentation.
   * @param documentation 
   */
  public void setDocumentation(String documentation) {
    this.documentation = Val.chkStr(documentation);
  }

  @Override
  public String toString() {
    return title + " ("+id+")";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ESRI_ItemInformation)) return false;
    ESRI_ItemInformation ii = (ESRI_ItemInformation)obj;
    if(!name.equals(ii.name)) return false;
    if(!title.equals(ii.title)) return false;
    if(!thumbnail.equals(ii.thumbnail)) return false;
    if(!thumbnailUrl.equals(ii.thumbnailUrl)) return false;
    if(!type.equals(ii.type)) return false;
    if(!typeKeywords.equals(ii.typeKeywords)) return false;
    if(!description.equals(ii.description)) return false;
    if(!tags.equals(ii.tags)) return false;
    if(!snippet.equals(ii.snippet)) return false;
    if(!extent.equals(ii.extent)) return false;
    if(!accessInformation.equals(ii.accessInformation)) return false;
    if(!licenseInfo.equals(ii.licenseInfo)) return false;
    if(!culture.equals(ii.culture)) return false;
    if(!id.equals(ii.id)) return false;
    if(!item.equals(ii.item)) return false;
    if(!itemType.equals(ii.itemType)) return false;
    if(!owner.equals(ii.owner)) return false;
    if((uploadedDate!=null && !uploadedDate.equals(ii.uploadedDate)) || (uploadedDate==null && ii.uploadedDate!=null)) return false;
    if((modifiedDate!=null && !modifiedDate.equals(ii.modifiedDate)) || (modifiedDate==null && ii.modifiedDate!=null)) return false;
    if(!guid.equals(ii.guid)) return false;
    if(!url.equals(ii.url)) return false;
    if(!access.equals(ii.access)) return false;
    if(size!=ii.size) return false;
    if(numComments!=ii.numComments) return false;
    if(numRatings!=ii.numRatings) return false;
    if(avgRating!=ii.avgRating) return false;
    if(numViews!=ii.numViews) return false;
    if(!sharingAccess.equals(ii.sharingAccess)) return false;
    if(!sharingGroups.equals(ii.sharingGroups)) return false;
    if(!documentation.equals(ii.documentation)) return false;
    return true;
  }
  
  /**
   * Gets array of strings as a single string.
   * @param array array of strings
   * @param separator separator
   * @return ESRI_ItemInformation as string
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
