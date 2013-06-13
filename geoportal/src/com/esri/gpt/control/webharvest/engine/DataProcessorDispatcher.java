/*
 * Copyright 2011 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.resource.api.Publishable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Data processor dispatcher.
 */
class DataProcessorDispatcher implements DataProcessor {
  private List<DataProcessor> processors;

  @Override
  public String getName() {
    return "Dispatcher";
  }
  
  public DataProcessorDispatcher(List<DataProcessor> processors) {
    this.processors = processors;
  }

  @Override
  public void onEnd(ExecutionUnit unit, boolean success) {
    for (DataProcessor dp: getEligibleProcessors(unit)) {
      dp.onEnd(unit, success);
    }
  }

  @Override
  public void onIterationException(ExecutionUnit unit, Exception ex) {
    for (DataProcessor dp: getEligibleProcessors(unit)) {
      dp.onIterationException(unit, ex);
    }
  }

  @Override
  public void onMetadata(ExecutionUnit unit, Publishable record) throws IOException, SQLException, CatalogIndexException, TransformerConfigurationException {
    for (DataProcessor dp: getEligibleProcessors(unit)) {
      dp.onMetadata(unit, record);
    }
  }

  @Override
  public void onStart(ExecutionUnit unit) {
    for (DataProcessor dp: getEligibleProcessors(unit)) {
      dp.onStart(unit);
    }
  }
  
  private List<DataProcessor> getEligibleProcessors(ExecutionUnit unit) {
    ArrayList<DataProcessor> eligible = new ArrayList<DataProcessor>();
    for (DataProcessor dp: processors) {
      if (unit.getRestrictions().isEmpty() || unit.getRestrictions().contains(dp.getName())) {
        eligible.add(dp);
      }
    }
    return processors;
  }
}
