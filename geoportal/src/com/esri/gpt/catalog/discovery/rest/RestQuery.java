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
package com.esri.gpt.catalog.discovery.rest;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.framework.util.Val;

/**
 * Represents a discovery query to be performed through a rest interface.
 */
public class RestQuery extends DiscoveryQuery {
  
  /** instance variables ====================================================== */
  private String responseFormat = "";
  private String responseGeometry = "";
  private String responseStyle = "";
  private String responseTarget = "";
  private String repositoryId = "";
  private String rssProviderUrl = "";
  private String rssSourceUrl = "";
  private String rssMoreUrl = "";
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public RestQuery() {
    super();
    getFilter().setRootClause(new LogicalClause.LogicalAnd());
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the ID of the repository to search.
   * @return the repository ID
   */
  public String getRepositoryId() {
    return repositoryId;
  }

  /**
   * Sets the ID of the repository to search.
   * @param repositoryId the repository ID
   */
  public void setRepositoryId(String repositoryId) {
    this.repositoryId = Val.chkStr(repositoryId);
  }
  
  /**
   * Gets the specified response format.
   * <br/>The enumeration domain is unchecked.
   * @return the response format
   */
  public String getResponseFormat() {
    return this.responseFormat;
  }
  /**
   * Sets the specified response format.
   * <br/>The enumeration domain is unchecked.
   * @param format the response format
   */
  public void setResponseFormat(String format) {
    this.responseFormat = format;
  }
  
  /**
   * Gets the specified response geometry format.
   * <br/>The enumeration domain is unchecked.
   * @return the response geometry format
   */
  public String getResponseGeometry() {
    return this.responseGeometry;
  }
  /**
   * Sets the specified response geometry format.
   * <br/>The enumeration domain is unchecked.
   * @param format the response geometry format
   */
  public void setResponseGeometry(String format) {
    this.responseGeometry = Val.chkStr(format);
  }
  
  /**
   * Gets the specified response style parameter.
   * <br/>The response style is suitable for a formatting CSS URL(s).
   * @return the response style
   */
  public String getResponseStyle() {
    return this.responseStyle;
  }
  /**
   * Sets the specified response style parameter.
   * <br/>The response style is suitable for a formatting CSS URL(s).
   * @param style the response style
   */
  public void setResponseStyle(String style) {
    this.responseStyle = Val.chkStr(style);
  }
  
  /**
   * Gets the specified response target (i.e. the target for &lt;a&gt; tags).
   * <br/>The enumeration domain is unchecked.
   * @return the response target
   */
  public String getResponseTarget() {
    return this.responseTarget;
  }
  /**
   * Sets the specified response target (i.e. the target for &lt;a&gt; tags).
   * <br/>The enumeration domain is unchecked.
   * @param target the response target
   */
  public void setResponseTarget(String target) {
    this.responseTarget = Val.chkStr(target);
  }
  
  /**
   * Gets a URL suitable for use as the RSS provider.
   * @return the RSS provider URL
   */
  public String getRssProviderUrl() {
    return this.rssProviderUrl;
  }
  /**
   * Sets a URL suitable for use as the RSS provider.
   * @param url the RSS provider URL
   */
  public void setRssProviderUrl(String url) {
    this.rssProviderUrl = Val.chkStr(url);
  }
  
  /**
   * Gets a URL suitable for use as the RSS source.
   * @return the RSS source URL
   */
  public String getRssSourceUrl() {
    return this.rssSourceUrl;
  }
  /**
   * Sets a URL suitable for use as the RSS source.
   * @param url the RSS source URL
   */
  public void setRssSourceUrl(String url) {
    this.rssSourceUrl = Val.chkStr(url);
  }

  /**
   * Gets URL to more results.
   * @return URL to more results
   */
  public String getMoreUrl() {
    return rssMoreUrl;
  }

  /**
   * Sets URL to more results.
   * @param rssMoreUrl URL to more results
   */
  public void setMoreUrl(String rssMoreUrl) {
    this.rssMoreUrl = Val.chkStr(rssMoreUrl);
  }

}
