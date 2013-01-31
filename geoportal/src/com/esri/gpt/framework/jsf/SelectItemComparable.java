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
package com.esri.gpt.framework.jsf;

import javax.faces.model.SelectItem;

import com.esri.gpt.framework.util.Val;


/**
 * The Class SelectItemComparable.
 */
@SuppressWarnings("serial")
public class SelectItemComparable extends SelectItem implements
    Comparable<SelectItem> {

// constructors ================================================================
/**
 * Instantiates a new select item comparable.
 * 
 * @param value the value
 * @param label the label
 */
public SelectItemComparable(String value, String label) {
  super(value, label);
}

/**
 * Instantiates a new select item comparable.
 * 
 * @param value the value
 */
public SelectItemComparable(String value) {
  super(value);
}

/**
 * Instantiates a new select item comparable.
 * 
 * @param value the value
 * @param label the label
 * @param description the description
 */
public SelectItemComparable(String value, String label, String description) {
  super(value, label, description );
}

/**
 * Instantiates a new select item comparable.
 * 
 * @param value the value
 * @param label the label
 * @param description the description
 * @param disabled the disabled
 */
public SelectItemComparable(String value, String label, String description,
    boolean disabled) {
  super(value, label, description, disabled );
}


// methods =====================================================================
/** 
 * Compare To
 * 
 *@param o Selected Item to compare against
 *
 *@return comparison of getLabel()
 */
public int compareTo(SelectItem o) {
  return Val.chkStr(this.getLabel()).compareTo(Val.chkStr(o.getLabel()));
}

}
