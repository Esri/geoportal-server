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
import java.util.ArrayList;

/**
 * A list of discoverable properties.
 */
public class Discoverables extends ArrayList<Discoverable>{
   
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public Discoverables() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds a member to the collection.
   * <br/>Null values will not be added.
   * @param member the member to add
   * @return true if the member was added
   */
  @Override
  public boolean add(Discoverable member) {
    if (member == null) {
      return false;
    } else {
      return super.add(member);
    }
  }
      
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  public void echo(StringBuffer sb) {
    if (size() == 0) {
      sb.append(" (No discoverables.)");
    } else {
      for (Discoverable member: this) {
        member.echo(sb.append("\n"));
      }
    }    
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (");
    echo(sb);
    sb.append("\n) ===== end ").append(getClass().getName());
    return sb.toString();
  }

}
