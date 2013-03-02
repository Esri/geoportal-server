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

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SearchPointOptionsIo.
 */
public class SearchPointOptionsIo {

/** The title. */
private String title;

/** The input values. */
private List<String> inputValues;

/** The output params. */
private Map<String, String> outputParams;

/**
 * Gets the title.
 * 
 * @return the title
 */
public String getTitle() {
  return title;
}

/**
 * Sets the title.
 * 
 * @param title the new title
 */
public void setTitle(String title) {
  this.title = title;
}

/**
 * Gets the input values.
 * 
 * @return the input values
 */
public List<String> getInputValues() {
  return inputValues;
}

/**
 * Sets the input values.
 * 
 * @param inputValues the new input values
 */
public void setInputValues(List<String> inputValues) {
  this.inputValues = inputValues;
}

/**
 * Gets the output params.
 * 
 * @return the output params
 */
public Map<String, String> getOutputParams() {
  return outputParams;
}

/**
 * Sets the output params.
 * 
 * @param outputParams the output params
 */
public void setOutputParams(Map<String, String> outputParams) {
  this.outputParams = outputParams;
}

}
