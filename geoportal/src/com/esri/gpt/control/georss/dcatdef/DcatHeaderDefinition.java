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

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Esri, Inc.
 */
public class DcatHeaderDefinition {
  public void print(DcatPrinter printer, Properties properties) throws IOException {
    printer.printAttribute("conformsTo", "https://project-open-data.cio.gov/v1.1/schema");
    printer.printAttribute("describedBy", "https://project-open-data.cio.gov/v1.1/schema/catalog.json");
  }
}
