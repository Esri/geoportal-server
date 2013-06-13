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
import com.esri.gpt.catalog.discovery.DiscoveryClause;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyClause;
import com.esri.gpt.catalog.discovery.PropertyValueType;
import com.esri.gpt.catalog.discovery.SpatialClause;

import java.util.logging.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

/**
 * Adapts a catalog discovery LogicalClause to the Lucene BooleanQuery model.
 */
public class LogicalClauseAdapter extends DiscoveryClauseAdapter {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(LogicalClauseAdapter.class.getName());
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated query adapter.
   * @param queryAdapter the query adapter
   */
  protected LogicalClauseAdapter(LuceneQueryAdapter queryAdapter) {
    super(queryAdapter);
  }
      
  /** methods ================================================================= */
 
  /**
   * Builds a Lucene BooleanQuery by recursively traversing a
   * catalog discovery LogicalClause.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param logicalClause the logical clause to adapt
   * @throws DiscoveryException if an invalid clause is encountered
   * @throws ParseException if a Lucene query parsing exception occurs
   */
  protected void adaptLogicalClause(BooleanQuery activeBooleanQuery, 
                                    LogicalClause logicalClause) 
    throws DiscoveryException, ParseException  {
    
    // loop the the sub clauses, recurse any logical clauses
    for (DiscoveryClause clause: logicalClause.getClauses()) {
      if (clause == null) {
        throw new DiscoveryException("A null clause was encountered.");

      } else if (clause instanceof LogicalClause) {
        BooleanQuery subQuery = new BooleanQuery();
        appendQuery(activeBooleanQuery,logicalClause,subQuery);
        adaptLogicalClause(subQuery,(LogicalClause)clause);
        
      } else if (clause instanceof PropertyClause) {
        PropertyClauseAdapter adapter = new PropertyClauseAdapter(getQueryAdapter());
        PropertyClause subClause = (PropertyClause)clause;
        if ((subClause.getTarget() != null) && (subClause.getTarget().getMeaning() != null)) {
          PropertyValueType pvt = subClause.getTarget().getMeaning().getValueType();
          if ((pvt != null) && pvt.equals(PropertyValueType.TIMEPERIOD)) {
            adapter = new TimeperiodClauseAdapter(getQueryAdapter());
          }
        }
        adapter.adaptPropertyClause(activeBooleanQuery,logicalClause,subClause);
        
      } else if (clause instanceof SpatialClause) {
        SpatialClauseAdapter adapter = new SpatialClauseAdapter(getQueryAdapter());
        SpatialClause subClause = (SpatialClause)clause;
        adapter.adaptSpatialClause(activeBooleanQuery,logicalClause,subClause);
        
      } else {
        String sErr = "Unrecognized clause type:"+clause.getClass().getName();
        throw new DiscoveryException(sErr);
      }
    }
    
    // MUST_NOT causes a problem when there is only one MUST_NOT clause within 
    // a BooleanQuery, to get round it we add all documents as a SHOULD
    BooleanClause[] clauses = activeBooleanQuery.getClauses();
    if ((clauses == null) || (clauses.length == 0)) {
      // TODO this will result in no records being returned,
      // possible this should be fixed
    } else if (clauses.length == 1) {
      if (clauses[0].getOccur().equals(BooleanClause.Occur.MUST_NOT)) {
        LOGGER.finer("Fixing single MUST_NOT clause within a BooleanQuery...");
        appendSelectAll(activeBooleanQuery);
      }
    }
  }
    
}
