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
package com.esri.gpt.framework.request;

/**
 * Represents the result associated with a query.
 * <p>
 * <br/>generic: RT represents the records type
 */
public class QueryResult<RT extends Records<? extends Record>> 
       extends Result {

// class variables =============================================================
  
// instance variables ==========================================================
private PageCursor _pageCursor = new PageCursor();
private RT         _records;

/** Maximum number of hits generated *. */
int maxQueryHits = Integer.MIN_VALUE;
  
// constructors ================================================================

/** Default constructor. */
public QueryResult() {
  this(null);
}

/**
 * Constructs with a supplied result record collection.
 * @param records the records
 */
public QueryResult(RT records) {
  setRecords(records);
}
  
// properties ==================================================================

/**
 * Gets the max query hits.  Gets value from records object.
 * 
 * @return the max query hits
 */
public int getMaxQueryHits() {
  return this.getRecords().getMaximumQueryHits();
}

/**
 * Sets the max query hits. Sets the value in the records object.
 * 
 * @param maxQueryHits the new max query hits
 */
public void setMaxQueryHits(int maxQueryHits) {
  this.getRecords().setMaximumQueryHits(maxQueryHits);
  this.getPageCursor().setTotalRecordCount(maxQueryHits);//T.M. v10
}


/**
 * Determines if the result has records.
 * @return true if the result has records
 */
public boolean getHasRecords() {
  return ((getRecords() != null) && (getRecords().size() > 0));
}

/**
 * Gets the UI page cursor.
 * @return the page cursor
 */
public PageCursor getPageCursor() {
  return _pageCursor;
}
/**
 * Sets the UI page cursor.
 * @param cursor the page cursor
 */
protected void setPageCursor(PageCursor cursor) {
  _pageCursor = cursor;
}

/**
 * Gets the records associated with the result.
 * @return the records
 */
public RT getRecords() {
  return _records;
}
/**
 * Sets the records associated with the result.
 * @param records the records
 */
public void setRecords(RT records) {
  _records = records;
}

// methods =====================================================================

/**
 * Resets the result.
 */
public void reset() {
  getPageCursor().setTotalRecordCount(0);
  getRecords().clear();
  this.setMaxQueryHits(Integer.MIN_VALUE);
}

}
