/*
 * Copyright 2014 Esri, Inc..
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
package com.esri.gpt.control.georss.dcatdef;

import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedRecord;
import static com.esri.gpt.control.georss.dcatdef.DcatFieldDefinition.OBLIGATORY;
import com.esri.gpt.framework.geometry.Envelope;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Spatial field.
 */
public class SpatialField extends BaseDcatField {

  /**
   * Creates instance of the class.
   * @param fldName field name
   */
  public SpatialField(String fldName) {
    super(fldName);
  }

  /**
   * Creates instance of the class.
   * @param fldName field name
   * @param flags flags
   */
  public SpatialField(String fldName, long flags) {
    super(fldName, flags);
  }
  
  /**
   * Gets default value.
   * @param properties properties
   * @return default value
   */
  protected String getDefaultValue(Properties properties) {
    return "";
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    
    String value;
    Envelope envelope = r.getEnvelope();
    if (envelope==null) {
      if ((flags.provide(r, null, properties) & OBLIGATORY)!=0) {
        value = getDefaultValue(properties);
      } else {
        return;
      }
    } else {
      value = ""+envelope.getMinX()+","+envelope.getMinY()+","+envelope.getMaxX()+","+envelope.getMaxY();
    }
    
    jsonWriter.name(getOutFieldName()).value(value);
  }
  
}
