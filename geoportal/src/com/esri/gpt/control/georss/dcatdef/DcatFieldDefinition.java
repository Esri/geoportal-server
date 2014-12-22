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
import java.util.Properties;

/**
 * Dcat field definition.
 */
public interface DcatFieldDefinition {
  long OBLIGATORY = 0x01;
  
  /**
   * Print field according to the definition.
   * @param jsonWriter underlying json writer
   * @param properties properties
   * @param dcatSchemas dcat schemas
   * @param r record to print
   * @throws IOException if printing fails
   */
  void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException ;
}
