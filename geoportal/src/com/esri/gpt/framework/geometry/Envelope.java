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
import com.esri.gpt.framework.util.Val;
import java.io.Serializable;

/**
 * Basic envelope.
 */
public class Envelope extends Geometry implements Cloneable, Serializable {

// class variables =============================================================
public static final double EMPTY = -9999;

// instance variables ==========================================================
private double _minx = EMPTY;
private double _miny = EMPTY;
private double _maxx = EMPTY;
private double _maxy = EMPTY;

// constructors ================================================================

/** Default constructor. */
public Envelope() {
  super();
}

/**
 * Instantiates a new envelope.
 * 
 * @param minX the minimum x
 * @param minY the minimum y
 * @param maxX the maximum x
 * @param maxY the maximum y
 */
public Envelope(double minX, double minY, double maxX, double maxY) {
  super();
  this.setMinX(minX);
  this.setMinY(minY);
  this.setMaxX(maxX);
  this.setMaxY(maxY);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Envelope(Envelope objectToDuplicate) {
  super();
  if (objectToDuplicate != null) {
    put(objectToDuplicate.getMinX(), objectToDuplicate.getMinY(),
        objectToDuplicate.getMaxX(), objectToDuplicate.getMaxY());
  }
}

// properties ==================================================================
/**
 * Gets the height.
 * @return the height
 */
public double getHeight() {
  return Math.abs(getMaxY() - getMinY());
}

/**
 * Gets the maximum x-coordinate.
 * @return the maximum x-coordinate
 */
public double getMaxX() {
  return _maxx;
}

/**
 * Sets the maximum x-coordinate.
 * @param d the maximum x-coordinate
 */
public void setMaxX(double d) {
  _maxx = d;
}

/**
 * Sets the maximum x-coordinate (string entry).
 * @param s the maximum x-coordinate
 */
public void setMaxX(String s) {
  _maxx = chkDbl(s);
}

/**
 * Gets the maximum y-coordinate.
 * @return the maximum y-coordinate
 */
public double getMaxY() {
  return _maxy;
}

/**
 * Sets the maximum y-coordinate.
 * @param d the maximum y-coordinate
 */
public void setMaxY(double d) {
  _maxy = d;
}

/**
 * Sets the maximum y-coordinate (string entry).
 * @param s the maximum y-coordinate
 */
public void setMaxY(String s) {
  _maxy = chkDbl(s);
}

/**
 * Gets the minimum x-coordinate.
 * @return the minimum x-coordinate
 */
public double getMinX() {
  return _minx;
}

/**
 * Sets the minimum x-coordinate.
 * @param d the minimum x-coordinate
 */
public void setMinX(double d) {
  _minx = d;
}

/**
 * Sets the minimum x-coordinate (string entry).
 * @param s the minimum x-coordinate
 */
public void setMinX(String s) {
  _minx = chkDbl(s);
}

/**
 * Gets the minimum y-coordinate.
 * @return the minimum y-coordinate
 */
public double getMinY() {
  return _miny;
}

/**
 * Sets the minimum y-coordinate.
 * @param d the minimum y-coordinate
 */
public void setMinY(double d) {
  _miny = d;
}

/**
 * Sets the minimum y-coordinate (string entry).
 * @param s the minimum y-coordinate
 */
public void setMinY(String s) {
  _miny = chkDbl(s);
}

/**
 * Gets the width.
 * @return the width
 */
public double getWidth() {
  return Math.abs(getMaxX() - getMinX());
}

/**
 * Gets x coordinate of the center of the envelope.
 * @return x coordinate of the center of the envelope
 */
public double getCenterX() {
  return getMinX() + getWidth()/2.0;
}

/**
 * Gets y coordinate of the center of the envelope.
 * @return y coordinate of the center of the envelope
 */
public double getCenterY() {
  return getMinY() + getHeight()/2.0;
}
// methods =====================================================================
/**
 * Check a double value.
 * @param s the string to check
 * @return the double value (EMPTY if the String was invalid)
 */
private double chkDbl(String s) {
  return Val.chkDbl(s,EMPTY);
}

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
public void echo(StringBuffer sb) {
  super.echo(sb);
  sb.append("Envelope:");
  sb.append(" isEmpty=\"").append(isEmpty()).append("\"");
  sb.append(" hasSize=\"").append(hasSize()).append("\"");
  sb.append(" minX=\"").append(getMinX()).append("\"");
  sb.append(" minY=\"").append(getMinY()).append("\"");
  sb.append(" maxX=\"").append(getMaxX()).append("\"");
  sb.append(" maxY=\"").append(getMaxY()).append("\"");
}

@Override
public String toString() {
  StringBuilder sb = new StringBuilder();
  if (!isValid()) {
    sb.append("invalid");
  } else if (isEmpty()) {
    sb.append("empty");
  } else {
    sb.append("minX=").append(getMinX());
    sb.append(" ,minY=").append(getMinY());
    sb.append(" ,maxX=").append(getMaxX());
    sb.append(" ,maxY=").append(getMaxY());
  }
  return "{" + sb.toString() + "}";
}

/**
 * Determines if a the envelope has size.
 * The envelope has size if it is not empty and both width and height
 * are greater than zero.
 * @return true if the envelope has size
 */
public boolean hasSize() {
  return (!isEmpty() && ((getWidth() > 0) && (getHeight() > 0)));
}

/**
 * Determines the empty staus.
 * <br/>If one coordinate is empty, the envelope is empty.
 * @return the empty status.
 */
public boolean isEmpty() {
  return ((getMinX() == EMPTY) || (getMinY() == EMPTY) ||
          (getMaxX() == EMPTY) || (getMaxY() == EMPTY));
}

/**
 * Determines if the envelope is valid.
 * <br/>At v10, logic was switched to !isEmpty()
 * @return true if the envelope is valid
 */
public boolean isValid() {
  return !isEmpty();
}

/**
 * Puts coordinate values.
 * @param minx minimum x-coordinate
 * @param miny minimum y-coordinate
 * @param maxx maximum x-coordinate
 * @param maxy maximum y-coordinate
 */
public void put(double minx, double miny, double maxx, double maxy) {
  setMinX(minx);
  setMinY(miny);
  setMaxX(maxx);
  setMaxY(maxy);
}

/**
 * Puts coordinate values.
 * @param minx minimum x-coordinate
 * @param miny minimum y-coordinate
 * @param maxx maximum x-coordinate
 * @param maxy maximum y-coordinate
 */
public void put(String minx, String miny, String maxx, String maxy) {
  put(chkDbl(minx), chkDbl(miny), chkDbl(maxx), chkDbl(maxy));
}

/**
 * Clones object.
 * @return itself
 * @see java.lang.Object#clone()
 */
@Override
public Envelope clone() {
  return new Envelope(_minx, _miny, _maxx, _maxy);
}

/**
 * Checks if two objects are equal.
 * @param obj Object to be tested
 * @return <code>true</code> if two objects are equal
 */
@Override
public boolean equals(Object obj) {
  if (!(obj instanceof Envelope)) {
    return false;
  }
  Envelope env = (Envelope) obj;
  return _minx == env._minx && _miny == env._miny && _maxx == env._maxx && _maxy == env._maxy;
}


/**
 * Merges <b>this</b> envelope with the argument envelope.
 * @param envelope the envelope to merge with
 */
public void merge(Envelope envelope) {
  if ((this.getMinX() == EMPTY) || (envelope.getMinX() < this.getMinX())) {
    this.setMinX(envelope.getMinX());
  }
  if ((this.getMaxX() == EMPTY) || (envelope.getMaxX() > this.getMaxX())) {
    this.setMaxX(envelope.getMaxX());
  }
  if ((this.getMinY() == EMPTY) || (envelope.getMinY() < this.getMinY())) {
    this.setMinY(envelope.getMinY());
  }
  if ((this.getMaxY() == EMPTY) || (envelope.getMaxY() > this.getMaxY())) {
    this.setMaxY(envelope.getMaxY());
  }
}

}
