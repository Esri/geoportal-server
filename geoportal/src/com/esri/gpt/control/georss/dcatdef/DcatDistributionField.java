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
import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.framework.util.Val;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Dcat distribution definition.
 */
public class DcatDistributionField implements DcatFieldDefinition {
  private final ArrayField accessURLField = new ArrayField("accessURL") {
    @Override
    protected String validateValue(String value) {
      return Val.chkUrl(value);
    }
  };
  private final ArrayField downloadURLField = new ArrayField("downloadURL") {
    @Override
    protected String validateValue(String value) {
      return Val.chkUrl(value);
    }
  };

  private final String name;

  /**
   * Creates instance of the class.
   * @param name field name
   */
  public DcatDistributionField(String name) {
    this.name = name;
  }
  
  @Override
  public void print(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r) throws IOException {
    DcatField accessURLDcatField = accessURLField.getAttributeField(dcatSchemas, r);
    DcatField downloadURLDcatField = downloadURLField.getAttributeField(dcatSchemas, r);
    
    List<String> accessURLValue = accessURLField.eval(properties, dcatSchemas, r);
    List<String> downloadURLValue = downloadURLField.eval(properties, dcatSchemas, r);
    
    if ((accessURLValue!=null && !accessURLValue.isEmpty()) || (downloadURLValue!=null && !downloadURLValue.isEmpty())) {
      jsonWriter.name(name).beginArray();
      
      if (accessURLValue!=null) {
        for (String value: accessURLValue) {
          jsonWriter.beginObject();
          jsonWriter.name("@type").value("dcat:Distribution");
          jsonWriter.name("accessURL").value(value);
          jsonWriter.name("mediaType").value(accessURLDcatField!=null? accessURLDcatField.guessMedia(value): "application/octet-stream");
          jsonWriter.name("format").value("API");
          jsonWriter.endObject();
        }
      }
      
      if (downloadURLValue!=null) {
        for (String value: downloadURLValue) {
          jsonWriter.beginObject();
          jsonWriter.name("@type").value("dcat:Distribution");
          jsonWriter.name("downloadURL").value(value);
          jsonWriter.name("mediaType").value(downloadURLDcatField!=null? downloadURLDcatField.guessMedia(value): "application/octet-stream");
          jsonWriter.endObject();
        }
      }
      
      jsonWriter.endArray();
    }
  }
  
  /**
   * Prints a link.
   * @param jsonWriter underlying json writer
   * @param properties proeprties
   * @param dcatSchemas dcat schemas
   * @param r record
   * @param link link to print
   * @throws IOException if printing fails
   */
  protected void printLink(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, IFeedRecord r, ResourceLink link) throws IOException {
    String linkName = null;
    String mediaType = null;
    String format = null;
    
    if (link.getTag().equals(ResourceLink.TAG_METADATA)) {
      linkName = "downloadURL";
      mediaType = "text/xml";
      format = "XML";
    }
    
    if (link.getTag().equals(ResourceLink.TAG_DETAILS)) {
      linkName = "accessURL";
      mediaType = "text/html";
      format = "HTML";
    }
    
    String sUrl = Val.chkUrl(link.getUrl());
    if (linkName!=null && !sUrl.isEmpty()) {
      jsonWriter.beginObject();
      jsonWriter.name("@type").value("dcat:Distribution");
      jsonWriter.name(linkName).value(sUrl);
      jsonWriter.name("mediaType").value(mediaType);
      jsonWriter.name("format").value(format);
      jsonWriter.endObject();
    }
  }
  
}
