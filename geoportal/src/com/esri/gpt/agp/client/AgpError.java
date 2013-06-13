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
package com.esri.gpt.agp.client;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An ArcGIS Portal communication error object.
 */
public class AgpError {
  
  /** instance variables ====================================================== */
  private int      code = -1;
  private String[] details;
  private String   message;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpError() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the error code.
   * @return the error code
   */
  public int getCode() {
    return this.code;
  }
  /**
   * Sets the error code.
   * @param code the error code
   */
  public void setCode(int code) {
    this.code = code;
  }
  
  /**
   * Gets the error details.
   * @return the error details
   */
  public String[] getDetails() {
    return this.details;
  }
  /**
   * Sets the error details.
   * @param details the error details
   */
  public void setDetails(String[] details) {
    this.details = details;
  }
  
  /**
   * Gets the error message.
   * @return the error message
   */
  public String getMessage() {
    return this.message;
  }
  /**
   * Sets the error message.
   * @param message the error message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parse the JSON response.
   * @param jsoResponse the JSON response
   * @throws Exception if an exception occurs
   */
  public void parse(JSONObject jsoResponse) throws Exception {
    String sProp = "error";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.parseError(jsoResponse.getJSONObject(sProp));
    }
  }
  
  /**
   * Parse the JSON error object.
   * @param jsoError the JSON error object
   * @throws Exception if an exception occurs
   */
  private void parseError(JSONObject jsoError) throws Exception {
    String sProp;
    
    sProp = "code";
    if (jsoError.has(sProp) && (!jsoError.isNull(sProp))) {
      this.setCode(jsoError.getInt(sProp));
    }
    sProp = "message";
    if (jsoError.has(sProp) && (!jsoError.isNull(sProp))) {
      this.setMessage(jsoError.getString(sProp));
    }
    sProp = "details";
    if (jsoError.has(sProp) && (!jsoError.isNull(sProp))) {
      JSONArray jsoValues = jsoError.getJSONArray(sProp);
      int n = jsoValues.length();
      if (n > 0) {
        String[] aValues = new String[n];
        for (int i=0;i<n;i++) {
          aValues[i] = jsoValues.getString(i);
        }
        this.setDetails(aValues);
      }
    }
  }
  
}