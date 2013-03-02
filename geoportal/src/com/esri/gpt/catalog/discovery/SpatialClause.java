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
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;

/**
 * A clause that compares a stored geometry to a supplied geometry.
 */
public class SpatialClause extends DiscoveryClause {
  
  /** instance variables ====================================================== */
  private Discoverable target;
  private Envelope boundingEnvelope = new Envelope();
  private String srsName = "";
    
  /** constructors ============================================================ */
  
  /** Default constructor. */
  private SpatialClause() {}
  
  /** properties ============================================================== */
  
  /** 
   * Gets the bounding envelope of the constraining geometry.
   * @return the bounding envelope 
   */
  public Envelope getBoundingEnvelope() {
    return boundingEnvelope;
  }
  
  /** 
   * Gets the name of the spatial reference system for the constraining geometry.
   * @return the spatial reference system name
   */
  public String getSrsName() {return srsName;}
  
  /** 
   * Sets the name of the spatial reference system for the constraining geometry.
   * @param name the spatial reference system name
   */
  public void setSrsName(String name) {
    this.srsName = Val.chkStr(name);
  }
     
  /** 
   * Gets the discoverable property targeted by the clause. 
   * <br/>This will typically be the "Geometry" property.
   * @return the target property
   */
  public Discoverable getTarget() {
    return target;
  }
  
  /** 
   * Sets the discoverable property targeted by the clause. 
   * <br/>This will typically be the "Geometry" property.
   * @param target the target property
   */
  public void setTarget(Discoverable target) {
    this.target = target;
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
    sb.append("\n").append(sbDepth);
    sb.append("  srsName=\"").append(getSrsName()).append("\"");
    sb.append("\n  ").append(sbDepth);
    getBoundingEnvelope().echo(sb);
    getTarget().echo(sb.append("\n"),depth+1);
  }
  
  /** inner classes =========================================================== */

  /** Note: Beyond, Crosses, DWithin and Touches are not implemented. */
  
  /** True if the bounding envelope of the stored geometry intersects the input geometry. */
  public static class GeometryBBOXIntersects extends SpatialClause {
    public GeometryBBOXIntersects() {super();}
  }
  
  /** True if the stored geometry fully contains the input geometry.*/
  public static class GeometryContains extends SpatialClause {
    public GeometryContains() {super();}
  }
  
  /** True if there is any positive interaction between the geometries. */
  public static class GeometryIntersects extends SpatialClause {
    public GeometryIntersects() {super();}
  }
  
  /** True if the geometries are equal. */
  public static class GeometryIsEqualTo extends SpatialClause {
    public GeometryIsEqualTo() {super();}
  }
  
  /** True if the geometries do not intersect. */
  public static class GeometryIsDisjointTo extends SpatialClause {
    public GeometryIsDisjointTo() {super();}
  }
  
  /** True if the stored geometry is fully contained by the input geometry. */
  public static class GeometryIsWithin extends SpatialClause {
    public GeometryIsWithin() {super();}
  }
  
  /** True if the geometries intersect but are not equal. */
  public static class GeometryOverlaps extends SpatialClause {
    public GeometryOverlaps() {super();}
  }
  
}
