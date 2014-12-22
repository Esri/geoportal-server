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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.control.georss.dcatdef.DcatDefinition;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 * Writes Dcat 1.1 json response of search results using dcat mappings.
 */
public class Dcat11JsonFeedWriter extends DcatJsonFeedWriter {

  /**
   * Creates instance of the writer.
   * @param request HTTP servlet request
   * @param context request context
   * @param writer underlying writer
   * @param query REST query
   * @param pretty <code>true</code> to make output formatted
   */
  public Dcat11JsonFeedWriter(HttpServletRequest request, RequestContext context, PrintWriter writer, RestQuery query, Boolean pretty) {
    super(request, context, writer, query, pretty);
    dcatSchemas = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getDcatSchemas();
  }

  /**
   * Creates instance of the writer.
   * @param context request context
   * @param writer underlying writer
   * @param query REST query
   */
  public Dcat11JsonFeedWriter(RequestContext context, PrintWriter writer, RestQuery query) {
    super(context, writer, query);
    dcatSchemas = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getDcatSchemas();
  }

  @Override
  public void write(IFeedRecords records) {
    Properties dcatProps = makeDefaultValues();
    DcatDefinition dcatDef = new DcatDefinition();
    
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter.setIndent("  ");
    
    try {
      dcatDef.print(jsonWriter, dcatProps, dcatSchemas, records);
    } catch (IOException ex) {
      Logger.getLogger(Dcat11JsonFeedWriter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
