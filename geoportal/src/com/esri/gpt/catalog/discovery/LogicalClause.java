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

/**
 * A collection of clauses that will be grouped by And/Or/Not logic.
 */
public class LogicalClause extends DiscoveryClause {

  /** instance variables ====================================================== */  
  private DiscoveryClauses clauses = new DiscoveryClauses();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  private LogicalClause() {}
      
  /** properties ============================================================== */
  
  /** 
   * Gets the collection of clauses to be operated on. 
   * @return the clause collection
   */
  public final DiscoveryClauses getClauses() {
    return clauses;
  }
  
  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the clause
   */
  @Override
  public void echo(StringBuffer sb, int depth) {
    StringBuffer sbDepth = new StringBuffer();
    for (int i=0;i<2*depth;i++) sbDepth.append(" ");
    
    sb.append(sbDepth).append(getClass().getSimpleName()).append(":");
    getClauses().echo(sb,depth);
  }
  
  /** inner classes =========================================================== */
  
  /** A collection of clauses operated on with And logic. */
  public static class LogicalAnd extends LogicalClause {
    public LogicalAnd() {super();}
  }
  
  /** A collection of clauses operated on with Or logic. */
  public static class LogicalOr extends LogicalClause {
    public LogicalOr() {super();}
  }
  
  /** A collection of clauses operated on with Not logic. */
  public static class LogicalNot extends LogicalClause {
    public LogicalNot() {super();}
  }
  
}
