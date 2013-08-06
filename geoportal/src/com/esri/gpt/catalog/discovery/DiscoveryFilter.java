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
package com.esri.gpt.catalog.discovery;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;

/**
 * Defines the filter that constrains a query.
 */
public class DiscoveryFilter extends DiscoveryComponent {
  
  /** class variables ========================================================= */
  
  /** The threshold for the maximum number of record to return = 5000 */
  public static final int THRESHOLD_MAXRECORDS = 5000;
  private static Integer maxRecordsThreshold = null;
    
  /** instance variables ====================================================== */
  private int maxRecords = 10;
  private LogicalClause rootClause;
  private int startRecord = 1;
      
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public DiscoveryFilter() {super();}
        
  /** properties ============================================================== */
  
  /**
   * Gets the maximum number of records to return.
   * @return the maximum number of records to return
   */
  public int getMaxRecords() {
    return maxRecords;
  }
  /**
   * Sets the maximum number of records to return.
   * <br/>A value of zero or less will return no records (hit count only).
   * <br/>If the supplied value exceeds the threshold, the max records
   * will be set to the threshold.
   * @param maxRecords maximum number of records to return
   */
  public void setMaxRecords(int maxRecords) {
    this.maxRecords = Math.min(maxRecords, getMaxRecordsThreshold());
  }
  
  /**
   * Gets the root clause for the filter.
   * @return the root clause
   */
  public LogicalClause getRootClause() {
    return rootClause;
  }
  /**
   * Sets the root clause for the filter.
   * @param clause the root clause
   */
  public void setRootClause(LogicalClause clause) {
    this.rootClause = clause;
  }
  
  /**
   * Gets the starting record.
   * <br/>The record set starts at 1 not 0. 
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

  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    sb.append(getClass().getSimpleName()).append(":");
    sb.append("\n  startRecord=\"").append(getStartRecord()).append("\"");
    sb.append("\n  maxRecords=\"").append(getMaxRecords()).append("\"");
    if (getRootClause() != null) {
      getRootClause().echo(sb.append("\n"),1);
    }
  }

  /**
   * Gets max records threshold.
   * Checks if is there any threshold configured in gpt.xml ("lucene.maxrecords.threshold"). 
   * If not, use THRESHOLD_MAXRECORDS.
   * @return max records threshold
   */
  private static synchronized int getMaxRecordsThreshold() {
    if (maxRecordsThreshold==null) {
      ApplicationContext appCtx = ApplicationContext.getInstance();
      ApplicationConfiguration appCfg = appCtx.getConfiguration();
      StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
      String value = parameters.getValue("lucene.maxrecords.threshold");
      maxRecordsThreshold = Val.chkInt(value, THRESHOLD_MAXRECORDS);
    }
    return maxRecordsThreshold;
  }
}
