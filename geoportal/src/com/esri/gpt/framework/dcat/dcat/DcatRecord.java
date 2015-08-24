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
   * Gets type.
   * @return type
   */
  String getType();
  /**
   * Gets title.
   * @return title
   */
  String getTitle();
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
   * Gets themes
   * @return list of themes
   */
  List<String> getThemes();
  
  /**
   * Gets modified date.
   * @return modified date
   */
  String getModified();
  /**
   * Gets publisher.
   * @return publisher
   */
//  String getPublisher();
  DcatPublisher getPublisher();
  
  /**
   * Gets contact point.
   * @return contact point
   */
  DcatContactPoint getContactPoint();
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
   * Gets bureau codes.
   * @return bureau codes
   */
  List<String> getBureauCodes();
  /**
   * Gets program codes.
   * @return program codes
   */
  List<String> getProgramCodes();
  /**
   * Gets license.
   * @return license
   */
  String getLicense();
  /**
   * Gets rights.
   * @return rights
   */
  String getRights();
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
   * Gets distribution.
   * @return distribution info
   */
  DcatDistributionList getDistribution();
  /**
   * Gets accrual periodicity.
   * @return accrual periodicity
   */
  String getAccrualPeriodicity();
  /**
   * Gets conforms to.
   * @return conforms to
   */
  String getConformsTo();
  /**
   * Gets data quality.
   * @return data quality
   */
  String getDataQuality();
  /**
   * Gets described by.
   * @return described by
   */
  String getDescribedBy();
  /**
   * Gets described by type.
   * @return described by type
   */
  String getDescribedByType();
  /**
   * Gets collection of which record is a subset.
   * @return collection of which record is a subset
   */
  String isPartOf();
  /**
   * Gets issued date.
   * @return issued date
   */
  String getIssued();
  /**
   * Gets language.
   * @return language
   */
  List<String> getLanguages();
  /**
   * Gets landing page.
   * @return landing page
   */
  String getLandingPage();
  /**
   * Gets primary IT investment UII (identifier).
   * @return primary IT investment UII
   */
  String getPrimaryITInvestmentUII();
  /**
   * Gets references.
   * @return references
   */
  List<String> getReferences();
  /**
   * Gets system records.
   * @return system records
   */
  List<String> getSystemRecords();
  /**
   * Gets theme.
   * @return theme
   */
  String getTheme();
  /**
   * Gets isPartOf.
   * @return isPartOf
   */
  String getIsPartOf();
 

  /**
   * Gets abstract.
   * @return abstract
   * @deprecated not in use
   */
  @Deprecated
  String getAbstract();
  /**
   * Gets access level comment.
   * @return access level comment
   * @deprecated not in use
   */
  @Deprecated
  String getAccessLevelComment();
  /**
   * Gets data dictionary.
   * @return data dictionary
   * @deprecated not in use
   */
  @Deprecated
  String getDataDictionary();
  /**
   * Gets web service URL.
   * @return web service URL
   * @deprecated not in use
   */
  @Deprecated
  String getWebService();
  /**
   * Gets access URL.
   * @return access URL
   * @deprecated not in use
   */
  @Deprecated
  String getAccessURL();
  /**
   * Gets download URL.
   * @return download URL
   * @deprecated not in use
   */
  @Deprecated
  String getDownloadURL();
  /**
   * Gets format.
   * @return format
   * @deprecated not in use
   */
  @Deprecated
  String getFormat();
  /**
   * Gets person.
   * @return person
   * @deprecated not in use
   */
  @Deprecated
  String getPerson();
  /**
   * Gets mbox.
   * @return mbox
   * @deprecated not in use
   */
  @Deprecated
  String getMbox();

}
