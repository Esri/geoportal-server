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
package com.esri.gpt.framework.util;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Stores from and to date proxies for date range queries.
 */
public class DateRange implements Serializable {
  
// class variables =============================================================
  
// instance variables ==========================================================
private DateProxy _fromDate = new DateProxy();
private DateProxy _toDate = new DateProxy();

// constructors ================================================================

/** Default constructor. */
public DateRange() {}

// properties ==================================================================

/**
 * Gets the from date.
 * @return the from date
 */
public DateProxy getFromDate() {
  return _fromDate;
}

/**
 * Returns a timestamp applicable for the from part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp getFromTimestamp() {
  return _fromDate.asFromTimestamp();
}

/**
 * Gets the to date.
 * @return the to date
 */
public DateProxy getToDate() {
  return _toDate;
}

/**
 * Returns a timestamp applicable for the to part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp getToTimestamp() {
  return _toDate.asToTimestamp();
}

// methods =====================================================================

/**
 * Checks the range.
 * <br/>The from and to dates will be flipped if the from date is
 * greater than the to date.
 */
public void check() {
  if (getFromDate().getIsValid() && getToDate().getIsValid()) {
    long nFrom = getFromDate().asFromTimestamp().getTime();
    long nTo = getToDate().asFromTimestamp().getTime();
    if (nFrom > nTo) {
      DateProxy fromDate = _fromDate;
      _fromDate = _toDate;
      _toDate = fromDate;
    }
  }
}

/**
 * Returns a string representation this object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append("fromDate=").append(getFromDate()).append("\n");
  sb.append("toDate=").append(getToDate()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}


}
