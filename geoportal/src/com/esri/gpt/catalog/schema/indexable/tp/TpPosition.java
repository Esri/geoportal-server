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
import java.util.GregorianCalendar;

/**
 * Represents a declared time position.
 * <p/>
 * Time positions are used by the parser while analyzing the
 * time periods for a source document.
 */
public class TpPosition {
  
  /** instance variables ====================================================== */
  private String     dateDescriptor;
  private String     descriptor;
  private String     indeterminate;
  private TpInterval interval;
  private boolean    isValid;
  private String     positionDescriptor;
  private String     tag;
  private String     type;
  private String     warning;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TpPosition() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the date part of the descriptor.
   * @return the date part of the descriptor
   */
  public String getDateDescriptor() {
    return this.dateDescriptor;
  }
  /**
   * Sets the date part of the descriptor.
   * @param dateDescriptor the date part of the descriptor
   */
  public void setDateDescriptor(String dateDescriptor) {
    this.dateDescriptor = dateDescriptor;
  }
  
  /**
   * Gets the full descriptor.
   * @return the the full descriptor
   */
  public String getDescriptor() {
    return this.descriptor;
  }
  /**
   * Sets the full descriptor.
   * @param descriptor the full descriptor
   */
  public void setDescriptor(String descriptor) {
    this.descriptor = descriptor;
  }
  
  /**
   * Gets the predicate for an indeterminate boundary.
   * <br/>after before now present unknown
   * @return the indeterminate predicate (null if not set)
   */
  public String getIndeterminate() {
    return this.indeterminate;
  }
  /**
   * Sets the predicate for an indeterminate boundary.
   * <br/>after before now present unknown
   * @param indeterminate the indeterminate predicate (null if not set)
   */
  public void setIndeterminate(String indeterminate) {
    this.indeterminate = indeterminate;
  }
  
  /**
   * Gets the time period interval.
   * @return the time period interval
   */
  public TpInterval getInterval() {
    return this.interval;
  }
  
  /**
   * Sets the time period interval.
   * @param interval the time period interval
   */
  public void setInterval(TpInterval interval) {
    this.interval = interval;
  }
  
  /**
   * True if the position is valid.
   * @return true if valid
   */
  public boolean getIsValid() {
    return this.isValid;
  }
  /**
   * True if the position is valid.
   * @param isValid true if valid
   */
  public void setIsValid(boolean isValid) {
    this.isValid = isValid;
  }
  
  /**
   * Gets the position part of the descriptor.
   * @return the position part of the descriptor
   */
  public String getPositionDescriptor() {
    return this.positionDescriptor;
  }
  /**
   * Sets the position part of the descriptor.
   * @param positionDescriptor the position part of the descriptor
   */
  public void setPositionDescriptor(String positionDescriptor) {
    this.positionDescriptor = positionDescriptor;
  }
  
  /**
   * Gets the tag.
   * @return the tag
   */
  public String getTag() {
    return this.tag;
  }
  /**
   * Sets the tag.
   * @param tag the tag
   */
  public void setTag(String tag) {
    this.tag = tag;
  }
  
  /**
   * Gets the type.
   * <br/>position beginPosition endPosition
   * @return the type
   */
  public String getType() {
    return this.type;
  }
  /**
   * Sets the type.
   * <br/>position beginPosition endPosition
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Gets the warning message associated with an invalid declaration.
   * @return the warning message
   */
  public String getWarning() {
    return this.warning;
  }
  /**
   * Sets the warning message associated with an invalid declaration.
   * @param warning the warning message
   */
  public void setWarning(String warning) {
    this.warning = warning;
  }
  
  /** methods ================================================================= */

  /**
   * Prints a string representation of the object to a buffer.
   * @param sb the buffer
   * @param depth the indentation depth
   */
  public void echo(StringBuilder sb, int depth) {
    String pfx = "\r\n";
    for (int i=0;i<2*depth;i++) pfx += " ";
    
    StringBuilder sbI = new StringBuilder();
    if (this.isValid) {
      GregorianCalendar c = new GregorianCalendar();
      String sInd = this.getInterval().getIndeterminate();
      Long nLower = this.getInterval().getLower();
      Long nUpper = this.getInterval().getUpper();
      sbI.append(pfx).append("interval.indeterminate: ").append(sInd);
      sbI.append(pfx).append("interval.lower: ").append(nLower);
      sbI.append(pfx).append("interval.upper: ").append(nUpper);
      
      StringBuilder sbT = new StringBuilder();
      sbT.append("[");
      if (nLower == null) {
        sbT.append("null");
      } else {
        c.setTimeInMillis(nLower);
        sbT.append(TpUtil.printIsoDateTime(c));
      }
      sbT.append(" TO ");
      if (nUpper == null) {
        sbT.append("null");
      } else {
        c.setTimeInMillis(nUpper);
        sbT.append(TpUtil.printIsoDateTime(c));
      }
      sbT.append("]");
    
      sbI.append(pfx).append("interval: ").append(sbT.toString());
    }
    
    sb.append(pfx).append("descriptor: ").append(this.descriptor);
    sb.append(pfx).append("positionDescriptor: ").append(this.positionDescriptor);
    sb.append(pfx).append("dateDescriptor: ").append(this.dateDescriptor);
    sb.append(pfx).append("indeterminate: ").append(this.indeterminate);
    sb.append(pfx).append("isValid: ").append(this.isValid);
    sb.append(pfx).append("tag: ").append(this.tag);
    sb.append(pfx).append("type: ").append(this.type);
    sb.append(pfx).append("warning: ").append(this.warning);
    sb.append(sbI);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TpPosition: {");
    this.echo(sb,1);
    sb.append("\r\n}");
    return sb.toString();
  }
  
}
