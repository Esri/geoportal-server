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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.framework.util.Val;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Super-class for the adaptation of a catalog discovery clause 
 * to the Lucene model.
 */
public class DiscoveryClauseAdapter {
      
  /** instance variables ====================================================== */
  private LuceneQueryAdapter queryAdapter;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated query adapter.
   * @param queryAdapter the query adapter
   */
  protected DiscoveryClauseAdapter(LuceneQueryAdapter queryAdapter) {
    this.queryAdapter = queryAdapter;
  }
  
  /** properties ============================================================== */
    
  /**
   * Gets the the query adapter.
   * @return the the query adapter
   */
  protected LuceneQueryAdapter getQueryAdapter() {
    return this.queryAdapter;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends a null-check query to the active boolean query.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param comparisonFieldName the name of the stored, non-tokenized field to be checked
   * @throws DiscoveryException if an invalid clause is encountered
   */
  protected void appendNullCheck(BooleanQuery activeBooleanQuery, 
                                 String comparisonFieldName) 
    throws DiscoveryException {
    comparisonFieldName = Val.chkStr(comparisonFieldName);
    if (comparisonFieldName.length() == 0) {
      throw new IllegalArgumentException("An comparisonFieldName was not supplied.");
    } else {
      appendQuery(activeBooleanQuery,new LogicalClause.LogicalNot(),
          new TermRangeQuery(comparisonFieldName,null,null,false,false));
    }
  }
  
  /**
   * Appends a processed query to the active boolean query.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param queryToAppend the Lucene query to append
   * @throws DiscoveryException if an invalid clause is encountered
   */
  protected void appendQuery(BooleanQuery activeBooleanQuery, 
                             LogicalClause activeLogicalClause,
                             Query queryToAppend) 
    throws DiscoveryException {
    if (activeLogicalClause instanceof LogicalClause.LogicalAnd) {
      activeBooleanQuery.add(queryToAppend,BooleanClause.Occur.MUST);
    } else if (activeLogicalClause instanceof LogicalClause.LogicalOr) {
      activeBooleanQuery.add(queryToAppend,BooleanClause.Occur.SHOULD);
    } else if (activeLogicalClause instanceof LogicalClause.LogicalNot) {
      activeBooleanQuery.add(queryToAppend,BooleanClause.Occur.MUST_NOT);
    } else {
      String sErr = "Unrecognized logical clause type: ";
      throw new DiscoveryException(sErr+activeLogicalClause.getClass().getName());
    }
  }
  
  /**
   * Appends a select all query to the active boolean query.
   * @param activeBooleanQuery the active Lucene boolean query
   * @throws DiscoveryException if an invalid clause is encountered
   */
  protected void appendSelectAll(BooleanQuery activeBooleanQuery) {
    Query q = new org.apache.lucene.search.MatchAllDocsQuery();
    activeBooleanQuery.add(q,BooleanClause.Occur.SHOULD);
  }
  
}
