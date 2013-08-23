/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat.dcat;

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
   * Gets keyword.
   * @return keyword
   */
  String getKeyword();
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
