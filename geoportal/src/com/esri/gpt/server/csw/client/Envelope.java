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
package com.esri.gpt.server.csw.client;

/**
 * Envelope defined as in geographical coordinate system. Used for CSW search
 * criteria. By default all values are Double.MIN_VALUE
 */
public class Envelope {

// instance variables ==========================================================
/** The max x. */
private double _maxX = Double.MIN_VALUE;

/** The max y. */
private double _maxY = Double.MIN_VALUE;

/** The min x. */
private double _minX = Double.MIN_VALUE;

/** The min y. */
private double _minY = Double.MIN_VALUE;

// constructors ================================================================
/**
 * Envelope constructor
 */
public Envelope() {
}

/**
 * Envelope constructor
 * 
 * @param minX    Min longitude of the envelope
 * @param minY    Min latitude of the envelope
 * @param maxX    Max longitude of the envelope
 * @param maxY    Max latitude of the envelope
 */
public Envelope(double minX, double minY, double maxX, double maxY) {
  setMinX(minX);
  setMinY(minY);
  setMaxX(maxX);
  setMaxY(maxY);
}

// properties ==================================================================
/**
 * Gets the max x.
 * 
 * @return the max x
 */
public double getMaxX() {
  return _maxX;
}

/**
 * Gets the max y.
 * 
 * @return the max y
 */
public double getMaxY() {
  return _maxY;
}

/**
 * Gets the min x.
 * 
 * @return the min x
 */
public double getMinX() {
  return _minX;
}

/**
 * Gets the min y.
 * 
 * @return the min y
 */
public double getMinY() {
  return _minY;
}

/**
 * 
 * @param maxX
 */
public void setMaxX(double maxX) {
  _maxX = maxX;
}

/**
 * 
 * @param maxY
 */
public void setMaxY(double maxY) {
  _maxY = maxY;
}

/**
 * 
 * @param minX
 */
public void setMinX(double minX) {
  _minX = minX;
}

/**
 * 
 * @param minY
 */
public void setMinY(double minY) {
  _minY = minY;
}

// methods =====================================================================
/**
 * Checks if Envelope is valid.
 * 
 * @return true, if is valid
 */
public boolean isValid() {
  if(
      (this.getMaxX() >= -180 || this.getMaxX() <= 180) &&
      (this.getMinX() >= -180 || this.getMinX() <= 180) &&
      (this.getMaxY() >= -90 || this.getMaxY() <= 90) &&
      (this.getMinY() >= -90 || this.getMinY() >= 180)
    ) {
    return true;
  }
  return false;
}

@Override
public String toString() {
  
  StringBuffer toString = new StringBuffer();
  toString.append("{ maxX = ").append(this.getMaxX()).append(" ,")
  .append("maxY = ").append(this.getMaxY()).append(" ,")
  .append("minX = ").append(this.getMinX()).append(" ,")
  .append("minY = ").append(this.getMinY()).append(" }");
  return this.toString();
  
}

}