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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.StringSet;

/**
 * Options associated with a CSW DescribeRecord request.
 */
public class DescribeRecordOptions {
  
  /** instance variables ====================================================== */
  private String    describeRecordLocation;
  private String    schemaLanguage;
  private StringSet typeNames = new StringSet();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public DescribeRecordOptions() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the location of the CSW DescribeRecord resource XML.
   * @return the DescribeRecord XML location
   */
  public String getDescribeRecordLocation() {
    return this.describeRecordLocation;
  }
  /**
   * Sets the location of the CSW DescribeRecord resource XML.
   * @param location the DescribeRecord XML location
   */
  public void setDescribeRecordLocation(String location) {
    this.describeRecordLocation = location;
  }
  
  /**
   * Gets the requested schema language.
   * @return the schema language
   */
  public String getSchemaLanguage() {
    return this.schemaLanguage;
  }
  /**
   * Sets the requested schema language.
   * @param schemaLanguage the schema language
   */
  public void setSchemaLanguage(String schemaLanguage) {
    this.schemaLanguage = schemaLanguage;
  }
  
  /**
   * Gets the type names to describe.
   * @return the type names
   */
  public StringSet getTypeNames() {
    return this.typeNames;
  }
  /**
   * Sets the type names to describe.
   * @param typeNames the type names
   */
  public void setTypeNames(StringSet typeNames) {
    this.typeNames = typeNames;
  }
  
}
