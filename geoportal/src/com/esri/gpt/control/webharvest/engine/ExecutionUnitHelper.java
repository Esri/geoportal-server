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
package com.esri.gpt.control.webharvest.engine;

/**
 * Execution unit helper.
 */
class ExecutionUnitHelper {
  private ExecutionUnit unit;
  
  public ExecutionUnitHelper(ExecutionUnit unit) {
    this.unit = unit;
  }
  
  public ExecutionUnit getUnit() {
    return unit;
  }
  
  public void setReportBuilder(ReportBuilder rp) {
    if (unit!=null) {
      unit.setAttribute(ReportBuilder.class.getCanonicalName(), rp);
    }
  }
  
  public ReportBuilder getReportBuilder() {
    return unit!=null? (ReportBuilder) unit.getAttribute(ReportBuilder.class.getCanonicalName()): null;
  }
  
  public void setSourceUris(SourceUriArray sourceUris) {
    if (unit!=null) {
      unit.setAttribute(SourceUriArray.class.getCanonicalName(), sourceUris);
    }
  }
  
  public SourceUriArray getSourceUris() {
    return unit!=null? (SourceUriArray) unit.getAttribute(SourceUriArray.class.getCanonicalName()): null;
  }
}
