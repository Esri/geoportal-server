/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.framework.dcat.dcat;

import java.util.List;

/**
 * DCAT record.
 */
public interface DcatRecord {
  /**
   * Gets title.
   * @return title
   */
  String getTitle();
  /**
   * Gets abstract.
   * @return abstract
   */
  String getAbstract();
  /**
   * Gets description.
   * @return description
   */
  String getDescription();
  /**
   * Gets keywords
   * @return list of keywords
   */
  List<String> getKeywords();
  /**
   * Gets identifier.
   * @return identifier
   */
  String getIdentifier();
  /**
   * Gets access level.
   * @return access level
   */
  String getAccessLevel();
  /**
   * Gets data dictionary.
   * @return data dictionary
   */
  String getDataDictionary();
  /**
   * Gets web service URL.
   * @return web service URL
   */
  String getWebService();
  /**
   * Gets access URL.
   * @return access URL
   */
  String getAccessURL();
  /**
   * Gets format.
   * @return format
   */
  String getFormat();
  /**
   * Gets spatial information.
   * @return spatial information
   */
  String getSpatial();
  /**
   * Gets temporal information.
   * @return temporal information
   */
  String getTemporal();
  
  /**
   * Gets modified date.
   * @return modified date
   */
  String getModified();
  /**
   * Gets publisher.
   * @return publisher
   */
  String getPublisher();
  /**
   * Gets person.
   * @return person
   */
  String getPerson();
  /**
   * Gets mbox.
   * @return mbox
   */
  String getMbox();
  /**
   * Gets license.
   * @return license
   */
  String getLicense();
  
  /**
   * Gets distribution.
   * @return distribution info
   */
  DcatDistributionList getDistribution();
}
