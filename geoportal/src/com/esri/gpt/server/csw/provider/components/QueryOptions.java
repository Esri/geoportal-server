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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;

/**
 * Options associated with a CSW query request.
 * <p>
 * Applies to the GetRecordById and GetRecords operations.
 */
public class QueryOptions {
  
  /** instance variables ====================================================== */
  private StringSet elementNames = new StringSet();
  private String    elementSetType;
  private StringSet elementSetTypeNames;
  private StringSet ids = new StringSet();
  private int       maxRecords = 10;
  private int       maxRecordsThreshold = 5000;
  private String    outputSchema;
  private String    queryConstraintCql;
  private String    queryConstraintVersion;
  private StringSet queryTypeNames;
  private String    resultType;
  private int       startRecord = 1;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public QueryOptions() {
    super();
  }
  
  /** properties ============================================================== */
    
  /**
   * Gets the requested response element names (specific returnables).
   * @return the element names
   */
  public StringSet getElementNames() {
    return this.elementNames;
  }
  /**
   * Sets the requested response element names (specific returnables).
   * @param elementNames the element names
   */
  public void setElementNames(StringSet elementNames) {
    this.elementNames = elementNames;
  }
  
  /**
   * Gets the response element set type (brief,summary,full).
   * @return the element set type
   */
  public String getElementSetType() {
    return this.elementSetType;
  }
  /**
   * Sets the response element set type (brief,summary,full).
   * @param elementSetType the element set type
   */
  public void setElementSetType(String elementSetType) {
    this.elementSetType = elementSetType;
  }
  
  /**
   * Gets the type names specified for the element set type.
   * @return the element set type names
   */
  public StringSet getElementSetTypeNames() {
    return this.elementSetTypeNames;
  }
  /**
   * Sets the type names specified for the element set type.
   * @param elementSetTypeNames the element set type names
   */
  public void setElementSetTypeNames(StringSet elementSetTypeNames) {
    this.elementSetTypeNames = elementSetTypeNames;
  }
  
  /**
   * Gets the requested IDs.
   * @return the IDs
   */
  public StringSet getIDs() {
    return this.ids;
  }
  /**
   * Sets the requested IDs.
   * @param ids the IDs
   */
  public void setIDs(StringSet ids) {
    this.ids = ids;
  }
  
  /**
   * Gets the maximum number of records to return.
   * <br/>Default = 10.
   * @return the maximum number of records to return
   */
  public int getMaxRecords() {
    return this.maxRecords;
  }
  /**
   * Sets the maximum number of records to return.
   * <br/>A value of zero or less will return no records (hit count only).
   * <br/>If the supplied value exceeds the threshold, the max records
   * will be set to the threshold.
   * @param maxRecords maximum number of records to return
   */
  public void setMaxRecords(int maxRecords) {
    this.maxRecords = maxRecords;
    if (this.maxRecords > this.getMaxRecordsThreshold()) {
      this.maxRecords = this.getMaxRecordsThreshold();
    }
  }
  
  /**
   * Gets the threshold for the maximum number of record to return.
   * <br/>Default = 5000.
   * @return the maximum number of records threshhold
   */
  public int getMaxRecordsThreshold() {
    return this.maxRecordsThreshold;
  }
  /**
   * Sets the threshold for the maximum number of record to return.
   * @param maxRecordsThreshold the maximum number of records threshhold
   */
  public void setMaxRecordsThreshold(int maxRecordsThreshold) {
    this.maxRecordsThreshold = maxRecordsThreshold;
  }
  
  /**
   * Gets the requested output XML schema.
   * @return the output schema (can be null)
   */
  public String getOutputSchema() {
    return this.outputSchema;
  }
  /**
   * Sets the requested output XML schema.
   * @param outputSchema the output schema
   */
  public void setOutputSchema(String outputSchema) {
    this.outputSchema = outputSchema;
  }
    
  /**
   * Gets the query constraint CQL text.
   * @return the query constraint CQL
   */
  public String getQueryConstraintCql() {
    return this.queryConstraintCql;
  }
  /**
   * Sets the query constraint CQL text.
   * @param cql the query constraint CQL
   */
  public void setQueryConstraintCql(String cql) {
    this.queryConstraintCql = cql;
  }
  
  /**
   * Gets the query constraint version.
   * @return the query constraint version
   */
  public String getQueryConstraintVersion() {
    return this.queryConstraintVersion;
  }
  /**
   * Sets the query constraint version
   * @param version the query constraint version
   */
  public void setQueryConstraintVersion(String version) {
    this.queryConstraintVersion = version;
  }
  
  /**
   * Gets the type names specified for the query.
   * @return the query type names
   */
  public StringSet getQueryTypeNames() {
    return this.queryTypeNames;
  }
  /**
   * Sets the type names specified for the query.
   * @param queryTypeNames the query type names
   */
  public void setQueryTypeNames(StringSet queryTypeNames) {
    this.queryTypeNames = queryTypeNames;
  }
  
  /**
   * Gets the query result type (hits,results,validate).
   * @return the result type
   */
  public String getResultType() {
    return this.resultType;
  }
  /**
   * Sets the query result type (hits,results,validate).
   * @param resultType the result type
   */
  public void setResultType(String resultType) {
    this.resultType = resultType;
  }
  
  /**
   * Gets the schema name query filter.
   * <br/>Only applies to non Dublin Core responses.
   * @return the schema name query filter
   */
  public String getSchemaFilter() {
    if (!this.isDublinCoreResponse()) {
      return this.getOutputSchema();
    } else {
      return null;
    }
  }
  
  /**
   * Gets the starting record.
   * @return the starting record
   */
  public int getStartRecord() {
    return startRecord;
  }
  /**
   * Sets the starting record.
   * <br/>If the supplied value is less that 1, the start record will be set to 1.
   * @param startRecord the starting record
   */
  public void setStartRecord(int startRecord) {
    this.startRecord = startRecord;
    if (this.startRecord < 1) this.startRecord = 1;
  }
  
  /**
   * Determines if the response is Dublin Core based.
   * @return true if the response is Dublin Core
   */
  public boolean isDublinCoreResponse() {
    String schema = Val.chkStr(this.getOutputSchema());
    if (schema.length() == 0) {
      return true;
    } else if (schema.equalsIgnoreCase("csw:Record")) {
      return true;
    } else if (schema.equalsIgnoreCase("http://www.opengis.net/cat/csw/2.0.2")) {
      return true;   
    } else {
      return false;    
    }
  
  }
  
}
