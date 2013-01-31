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
import java.util.ArrayList;


/**
 * Maintains a collection of records.
 * <p>
 * The collection is typically generated through a database query,
 * or a query to a remote service.
 * <p>
 * <br/>generic: T represents the record type
 * 
 * @param <T> the generic type
 */
public class Records<T extends Record> extends ArrayList<T> {

// class variables =============================================================
  
// instance variables ==========================================================
/** The _max query hits. */
private int _maxQueryHits = Integer.MIN_VALUE;

// constructors ================================================================

/** Default constructor. */
public Records() {}
  
// properties ==================================================================
/**
 * Sets the maximum query hits.
 * 
 * @param maxQueryHits the new maximum query hits
 */
public void setMaximumQueryHits(int maxQueryHits) {
  this._maxQueryHits = maxQueryHits;
}

/**
 * Gets the maximum query hits.
 * 
 * @return the maximum query hits
 */
public int getMaximumQueryHits() {
  return this._maxQueryHits;
}


// methods =====================================================================

}
