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
 * A list of discovery clauses.
 */
public class DiscoveryClauses extends ArrayList<DiscoveryClause>{
   
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public DiscoveryClauses() {}
  
  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the clause
   */
  public void echo(StringBuffer sb, int depth) {
    if (size() == 0) {
      sb.append(" (No sub-clauses.)");
    } else {
      for (DiscoveryClause member: this) {
        member.echo(sb.append("\n"),depth+1);
      }
    }    
  }
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
    echo(sb,0);
    sb.append("\n) ===== end ").append(getClass().getName());
    return sb.toString();
  }

}
