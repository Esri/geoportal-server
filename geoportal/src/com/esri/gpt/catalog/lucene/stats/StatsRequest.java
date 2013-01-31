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
package com.esri.gpt.catalog.lucene.stats;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.lucene.AclFilter;
import com.esri.gpt.catalog.lucene.IsPartOfFilter;
import com.esri.gpt.catalog.lucene.Storeable;
import com.esri.gpt.catalog.lucene.Storeables;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * Request to obtain Lucene index statistics.
 */
public class StatsRequest {
  
  /** instance variables ====================================================== */
  private String         baseQueryUrl = "/geoportal/rest/find/document";
  private String         baseStatsUrl = "/geoportal/rest/index/stats";
  private OpenBitSet     documentFilterBitSet;
  private RequestContext requestContext;
  private String         responseFormat = "html";
  private PrintWriter    responseWriter = new PrintWriter(System.err,true);
  private String         sortBy;
  private Storeables     storeables;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated request context.
   * @param requestContext the active request context
   */
  public StatsRequest(RequestContext requestContext) {
    CatalogConfiguration catCfg = requestContext.getCatalogConfiguration();
    PropertyMeanings meanings = catCfg.getConfiguredSchemas().getPropertyMeanings();
    this.requestContext = requestContext;
    this.storeables = new Storeables(meanings);
    if ((requestContext != null) && (requestContext.getServletRequest() != null) &&
        (requestContext.getServletRequest() instanceof HttpServletRequest)) {
      HttpServletRequest http = (HttpServletRequest)requestContext.getServletRequest();
      if (http.getContextPath() != null) {
        this.baseQueryUrl = http.getContextPath()+"/rest/find/document";
        this.baseStatsUrl = http.getContextPath()+"/rest/index/stats";
      }
    }  
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the base URL used to construct a REST query.
   * <br/>e.g. /geoportal/rest/find/document
   * @return the base query url
   */
  public String getBaseQueryUrl() {
    return this.baseQueryUrl;
  }
  
  /**
   * Gets the base URL used to construct a stats request.
   * <br/>e.g. /geoportal/rest/index/stats
   * @return the base stats url
   */
  public String getBaseStatsUrl() {
    return this.baseStatsUrl;
  }
  
  /**
   * Gets the filter bitset representing the set of documents for which statistics
   * will be collected.
   * @return the document filter bitset (can be null)
   */
  public OpenBitSet getDocumentFilterBitSet() {
    return this.documentFilterBitSet;
  }
  
  /**
   * Gets the underlying request context.
   * @return the request context
   */
  public RequestContext getRequestContext() {
    return this.requestContext;
  }
  
  /**
   * Gets the response format (html|json)
   * @return the response format
   */
  public String getResponseFormat() {
    return this.responseFormat;
  }
  /**
   * Sets the response format (html|json)
   * @param format the response format
   */
  public void setResponseFormat(String format) {
    this.responseFormat = format;
  }
  
  /**
   * Gets the writer to be used wjen generating the response.
   * @return the response writer
   */
  public PrintWriter getResponseWriter() {
    return this.responseWriter;
  }
  /**
   * Sets the writer to be used wjen generating the response.
   * @param writer the response writer
   */
  public void setResponseWriter(PrintWriter writer) {
    this.responseWriter = writer;
  }
  
  /**
   * Gets the sort by parameter.
   * @return the sort by parameter
   */
  public String getSortBy() {
    return this.sortBy;
  }
  /**
   * Sets the sort by parameter.
   * @param sortBy the sort by parameter
   */
  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }
  
  /**
   * Gets the field names for which statistics can be collected.
   * @param reader the Lucene index reader
   * @return the array of field names
   */
  public String[] getCollectableFieldNames(IndexReader reader) {
    List<String> collectable = new ArrayList<String>();
    Collection<String> indexed = reader.getFieldNames(IndexReader.FieldOption.INDEXED);
    for (String name: indexed) {
      if (this.isFieldCollectable(name)) {
        collectable.add(name);
      }
    }
    return collectable.toArray(new String[0]);
  }

  /**
   * Gets the storable associated with a field name.
   * @param fieldName the subject field name
   * @return the associated storable (can be null)
   */
  public Storeable getStoreable(String fieldName) {
    if (Val.chkStr(fieldName).equalsIgnoreCase("body")) {
      return null;
    } else {
      return (Storeable)this.storeables.get(fieldName);
    }
  }
  
  /**
   * Determines if statistics can be collected for a field.
   * @param fieldName the field name
   * @return true if statistics can be collected for the field
   */
  public boolean isFieldCollectable(String fieldName) {
    return (this.getStoreable(fieldName) != null);
  }
  
  /**
   * Prepares the request prior to statistics collection.
   * <br/>A document filter bitset will be created associated with only those
   * documents that are visible to the active user.
   * @param reader the Lucene index reader
   * @throws IOException if an index i/o exception occurs
   */
  public void prepare(IndexReader reader) throws IOException {    
    MetadataAcl acl = new MetadataAcl(this.requestContext);
    AuthenticationStatus auth = this.requestContext.getUser().getAuthenticationStatus();
    boolean bAdmin = auth.getAuthenticatedRoles().hasRole("gptAdministrator");
    Filter filter = null;
    if (!bAdmin && !acl.isPolicyUnrestricted()) {
      String[] aclValues = acl.makeUserAcl();
      filter = new AclFilter(Storeables.FIELD_ACL,aclValues);
    }
    filter = IsPartOfFilter.make(this.getRequestContext(),filter);
    if (filter != null) {
      this.documentFilterBitSet = (OpenBitSet)filter.getDocIdSet(reader);
    }
  }
  
}
