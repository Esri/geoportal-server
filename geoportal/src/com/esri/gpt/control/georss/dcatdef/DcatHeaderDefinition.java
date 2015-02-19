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

import static com.esri.gpt.framework.util.Val.chkStr;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Header defintion.
 */
public class DcatHeaderDefinition {
  /**
   * Prints header.
   * @param jsonWriter underlying json writer
   * @param properties properties
   * @throws IOException if printing fails
   */
  public void print(JsonWriter jsonWriter, Properties properties) throws IOException {
    String context = chkStr(properties.getProperty("@id"));
    jsonWriter.name("@context").value("https://project-open-data.cio.gov/v1.1/schema/catalog.jsonld");
    if (!context.isEmpty()) {
      jsonWriter.name("@id").value(properties.getProperty("@id"));
    }
    jsonWriter.name("@type").value("dcat:Catalog");
    jsonWriter.name("conformsTo").value("https://project-open-data.cio.gov/v1.1/schema");
    jsonWriter.name("describedBy").value("https://project-open-data.cio.gov/v1.1/schema/catalog.json");
  }
}
