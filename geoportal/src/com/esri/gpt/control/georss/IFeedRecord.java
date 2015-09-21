/*
 * Copyright 2012 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ResourceLinks;
import com.esri.gpt.framework.geometry.Envelope;
import java.util.Date;
import java.util.Map;

/**
 * Feed record definition.
 */
public interface IFeedRecord {
  static final String STD_COLLECTION_INDEX   = "index";
  static final String STD_COLLECTION_CATALOG = "catalog";
  
  /**
   * Get object id.
   * @return object id
   */
  long getObjectId();
  /**
   * Gets UUID.
   * @return UUID
   */
  String getUuid();
  /**
   * Gets title.
   * @return title
   */
  String getTitle();
  /**
   * Gets modified date.
   * @return modified date
   */
  Date getModfiedDate();
  /**
   * Gets abstract.
   * @return abstract
   */
  String getAbstract();
  /**
   * Gets envelope.
   * @return envelope
   */
  Envelope getEnvelope();
  /**
   * Gets resource links.
   * @return resource links
   */
  ResourceLinks getResourceLinks();
  /**
   * Gets content type.
   * @return content type
   */
  String getContentType();
  /**
   * Gets resource URL.
   * @return resource URL
   */
  String getResourceUrl();
  /**
   * Gets URL to view metadata.
   * @return URL to view metadata
   */
  String getViewMetadataUrl();
  /**
   * Gets service type.
   * @return service type
   */
  String getServiceType();
  /**
   * Gets service.
   * @return service
   */
  String getService();
  /**
   * Gets file identifier.
   * @return file identifier
   */
  String getFileIdentifier();
  /**
   * Gets thumbnail url.
   * @return thumbnail url
   */
  String getThumbnailUrl();
  /**
   * Gets free data.
   * @param collection id of the collection of data
   * @return map of attributes
   */
  Map<String,IFeedAttribute> getData(String collection);
}
