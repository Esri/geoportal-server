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
import com.esri.gpt.catalog.discovery.IStoreable;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.indexable.IndexableContext;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 * The process for analyzing indexable elements associated with 
 * the time period of the content.
 */
public class TpAnalyzer {
 
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(TpAnalyzer.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TpAnalyzer() {}
  
  /** methods ================================================================= */
  
  /**
   * Analyzes indexable elements associated with the time period of the content.
   * @param context the context
   * @param schema the schema
   * @param dom the XML document
   * @param documentName the document name (used for logging only)
   */
  public void analyze(IndexableContext context, 
      Schema schema, Document dom, String documentName) {
    
    String sAnalysisField = "timeperiod.analyze";
    String sTargetField = "timeperiod";
    IStoreable storeable = context.getStoreables().get(sTargetField);
    if (storeable != null) return;
    LOGGER.finest("Analyzing the time period of the content.");
    
    TpParser parser = new TpParser();
    TpIntervals intervals = new TpIntervals();
    parser.setDocumentName(documentName);
    storeable = context.getStoreables().get(sAnalysisField);

    if (storeable != null) {
      Object[] values = storeable.getValues();
      if (values != null) {
        for (Object value: values) {
          if ((value != null) && (value instanceof String)) {
            String sDescriptor = Val.chkStr((String)value);
            if (sDescriptor.length() > 0) {
              parser.parseDescriptor(sDescriptor);
            }
          }
        }
        storeable.setValues(null);
        
        if (parser.hasWarnings()) {
          LOGGER.info((parser.warningsToString()));
          intervals = null;
        } else {
          parser.processResult();
          intervals = parser.getIntervals();
        }
        
        //LOGGER.info(parser.toString()); 
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest(parser.toString());
        }
      }
    }
    
    PropertyMeaning meaning = context.getPropertyMeanings().get(sTargetField);
    if (intervals != null) {
      context.addStoreableValue(meaning,intervals);
    } else {
      context.addStoreableValue(meaning,"invalid");
    }
  }  

}
