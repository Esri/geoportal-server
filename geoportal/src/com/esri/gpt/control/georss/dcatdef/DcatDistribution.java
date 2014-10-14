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

import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedRecord;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Esri, Inc.
 */
public class DcatDistribution implements DcatFieldDefinition {

  private final String name;

  public DcatDistribution(String name) {
    this.name = name;
  }
  
  @Override
  public void print(DcatPrinter printer, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    printer.startArray(name);
    for ( ResourceLink l: r.getResourceLinks()) {
      printLink(printer, properties, dcatSchemas, l);
    }
    printer.endArray();
  }
  
  protected void printLink(DcatPrinter printer, Properties properties, DcatSchemas dcatSchemas, ResourceLink link) throws IOException {
    boolean printLink = false;
    String mediaType = null;
    String format = null;
    if (link.getTag().equals(ResourceLink.TAG_METADATA)) {
      printLink = true;
      mediaType = "text/xml";
      format = "XML";
    }
    if (link.getTag().equals(ResourceLink.TAG_DETAILS)) {
      printLink = true;
      mediaType = "text/html";
      format = "HTML";
    }
    if (printLink) {
      printer.startObject();
      printer.printAttribute("accessURL", link.getUrl());
      printer.printAttribute("mediaType", mediaType);
      printer.printAttribute("format", format);
      printer.endObject();
    }
  }
  
}
