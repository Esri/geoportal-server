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

/**
 * Distribution information.
 */
public interface DcatDistribution {
  /**
   * Gets type.
   * @return type
   */
  String getType();
  /**
   * Gets access URL.
   * @return access URL
   */
  String getAccessURL();
  /**
   * Gets conforms to.
   * @return conforms to
   */
  String getConformsTo();
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
   * Gets description.
   * @return description
   */
  String getDescription();
  /**
   * Gets download URL.
   * @return download URL
   */
  String getDownloadURL();
  /**
   * Gets data format.
   * @return data format
   */
  String getFormat();
  /**
   * Gets media type.
   * @return media type
   */
  String getMediaType();
  /**
   * Gets title.
   * @return title
   */
  String getTitle();
}
