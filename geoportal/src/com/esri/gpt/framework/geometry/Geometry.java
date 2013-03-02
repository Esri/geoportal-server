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
package com.esri.gpt.framework.geometry;

/**
 * Super-class for a geometry.
 */
public class Geometry {
  
  // class variables =============================================================
    
  // instance variables ==========================================================
  private String wkid = "";
  
  // constructors ================================================================
   
  /** Default constructor. */
  protected Geometry() {}
   
  // properties ==================================================================
  
  /** 
   * Gets the well known id of the spatial reference system.
   * @return the well known srs id
   */
  public String getWkid() {
    return wkid;
  }
  /**
   * Sets the well known id of the spatial reference system.
   * @param wkid well known srs id
   */
  public void setWkid(String wkid) {
    this.wkid = wkid;
  }
  
  // methods =====================================================================
  
  /**
   * Appends property information for the component to a StringBuffer.
   * <br/>The method is intended to support "FINEST" logging.
   * <br/>super.echo should be invoked prior appending any local information.
   * @param sb the StringBuffer to use when appending information
   */
  public void echo(StringBuffer sb) {}
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
    echo(sb);
    sb.append("\n) ===== end ").append(getClass().getName());
    return sb.toString();
  }
  
}
