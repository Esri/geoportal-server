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
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author Esri, Inc.
 */
public class PublisherField implements DcatFieldDefinition {
  private static final ArrayList<DcatFieldDefinition> fieldDefinitions = new ArrayList<DcatFieldDefinition>();
  static {
    fieldDefinitions.add(new StringField("publisher",DcatFieldDefinition.OBLIGATORY){
      @Override
      protected String getOutFieldName() {
        return "name";
      }
    });
  }
  
  private final String name;

  public PublisherField(String name) {
    this.name = name;
  }

  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    jsonWriter.name(name).beginObject();
    for (DcatFieldDefinition dfd: fieldDefinitions) {
      dfd.print(jsonWriter, properties, dcatSchemas, r);
    }
    jsonWriter.endObject();
  }
  
}
