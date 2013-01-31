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
package com.esri.gpt.server.csw.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.esri.gpt.framework.request.Records;


/**
 * The Class CswRecords.
 * 
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CswRecords extends Records<CswRecord> {
 
/** The maximum number of hits a query generated **/
private int maximumQueryHits = Integer.MIN_VALUE;

/**
 * Instantiates a new csw records.
 */
public CswRecords() {
}


/**
 * Gets the size.
 * 
 * @return the size
 */
public int getSize() {
  return this.size();
}

/**
 * Gets the maximum query hits.
 * 
 * @return the maximum query hits (less than 0 if not defined)
 */
@Override
public int getMaximumQueryHits() {
  return maximumQueryHits;
}

/**
 * Sets the maximum query hits.
 * 
 * @param maximumQueryHits the new maximum query hits
 */
@Override
public void setMaximumQueryHits(int maximumQueryHits) {
  this.maximumQueryHits = maximumQueryHits;
}

}