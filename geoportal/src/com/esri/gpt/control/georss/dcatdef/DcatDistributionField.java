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
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dcat distribution definition.
 */
public class DcatDistributionField implements DcatFieldDefinition {

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
    jsonWriter.name(name).beginArray();
    for ( ResourceLink l: r.getResourceLinks()) {
      printLink(jsonWriter, properties, dcatSchemas, l);
    }
    jsonWriter.endArray();
  }
  
  /**
   * Prints a link.
   * @param jsonWriter underlying json writer
   * @param properties proeprties
   * @param dcatSchemas dcat schemas
   * @param link link to print
   * @throws IOException if printing fails
   */
  protected void printLink(JsonWriter jsonWriter, Properties properties, DcatSchemas dcatSchemas, ResourceLink link) throws IOException {
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
    
    String sUrl = checkUrl(link.getUrl());
    if (linkName!=null && !sUrl.isEmpty()) {
      jsonWriter.beginObject();
      jsonWriter.name(linkName).value(sUrl);
      jsonWriter.name("mediaType").value(mediaType);
      jsonWriter.name("format").value(format);
      jsonWriter.endObject();
    }
  }
  
  private String checkUrl(String url) {
    try {
      URL u = new URL(url);
      u.toURI();
      return url;
    } catch (Exception ex) {
      return "";
    }
  }
  
}
