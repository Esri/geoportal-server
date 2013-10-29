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
package com.esri.gpt.catalog.schema.indexable.tp;

/**
 * Represents the boundaries of a time period interval.
 * <p/>
 * Boundaries are milliseconds since the epoch 
 * (January 1, 1970, 00:00:00 GMT).
 * <p/>
 * Indeterminate: unknown now now.lower now.upper 
 */
public class TpInterval {
  
  /** instance variables ====================================================== */
  private String indeterminate;
  private Long   lower;
  private Long   upper;

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TpInterval() {}
  
  /** 
   * Construct with a lower and upper bound. 
   * @param lower the lower boundary
   * @param upper the upper boundary
   */
  public TpInterval(Long lower, Long upper) {
    this(lower,upper,null);
  }
  
  /** 
   * Construct with a lower and upper bound. 
   * @param lower the lower boundary
   * @param upper the upper boundary
   * @param indeterminate the indeterminate
   */
  public TpInterval(Long lower, Long upper, String indeterminate) {
    this.setLower(lower);
    this.setUpper(upper);
    this.setIndeterminate(indeterminate);
  }

  /** properties ============================================================== */
  
  /**
   * Gets the predicate for an indeterminate interval.
   * <br/>unknown now now.lower now.upper 
   * @return the indeterminate predicate (null if not set)
   */
  public String getIndeterminate() {
    return this.indeterminate;
  }
  /**
   * Sets the predicate for an indeterminate interval.
   * <br/>unknown now now.lower now.upper 
   * @param indeterminate the indeterminate predicate (null if not set)
   */
  public void setIndeterminate(String indeterminate) {
    this.indeterminate = indeterminate;
  }
  
  /**
   * Gets the lower boundary.
   * @return the lower boundary
   */
  public Long getLower() {
    return this.lower;
  }
  /**
   * Sets the lower boundary.
   * @param lower the lower boundary
   */
  public void setLower(Long lower) {
    this.lower = lower;
  }
  
  /**
   * Gets the upper boundary.
   * @return the upper boundary
   */
  public Long getUpper() {
    return this.upper;
  }
  /**
   * Sets the upper boundary.
   * @param upper the upper boundary
   */
  public void setUpper(Long upper) {
    this.upper = upper;
  }

}
