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
package com.esri.gpt.catalog.search;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;


/**
 * The Interface ISearchFilterSpatialObj. Defines methods for a spatial type
 * of search filter.
 */
public interface ISearchFilterSpatialObj extends ISearchFilter {

/**
 * Options of what bounds to use for search.
 */
public static enum OptionsBounds {
  
  /** The anywhere. */
  anywhere,       // No spatial bounds  
  /** The use geog extent. */
  useGeogExtent,  // Within + Overlaps spatial bounds
   /** The data within extent. */ 
  dataWithinExtent; // Within Spatial bounds

/**
 * Checks value given as string.
 * @param value value given as string
 * @return value as enum, or {@link OptionsBounds#anywhere} if value 
 * unrecognized
 */
public static OptionsBounds checkValueOf(String value) {
  value = Val.chkStr(value);
  for (OptionsBounds ksto : values()) {
    if (ksto.name().equalsIgnoreCase(value)) {
      return ksto;
    }
  }
  return anywhere;
}
}

/**
 * Gets the visible envelope.
 * 
 * @return the visible envelope
 */
public Envelope getVisibleEnvelope();

/**
 * Sets the visible envelope.
 * 
 * @param envelope the envelope
 * 
 *
 */
public void setVisibleEnvelope(Envelope envelope);

/**
 * Gets the envelope.
 * 
 * @return the envelope
 */
public Envelope getEnvelope();

/**
 * Sets the envelope.
 * 
 * @param envelope the new envelope
 */
public void setEnvelope(Envelope envelope);

/**
 * Sets the selected bounds options i.e. whether data envelopes are contained, anywhere
 * or intersecting.
 * 
 * @param selectedBounds the new selected bounds
 */
public void setSelectedBounds(String selectedBounds);

/**
 * Gets the selected bounds options.
 * 
 * @return the selected bounds
 */
public String getSelectedBounds();

/**
 * Sets the selected bounds options i.e. whether data envelopes are contained, anywhere
 * or intersecting.
 * 
 * @param selectedBounds the new selected bounds
 */
public void setSelectedBoundsAsEnum(OptionsBounds selectedBounds);

/**
 * Gets the selected bounds options.
 * 
 * @return the selected bounds
 */
public OptionsBounds getSelectedBoundsAsEnum();


}


