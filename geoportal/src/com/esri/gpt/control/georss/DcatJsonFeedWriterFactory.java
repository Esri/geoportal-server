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
import com.esri.gpt.framework.context.RequestContext;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Esri, Inc.
 */
public class DcatJsonFeedWriterFactory {
  private static DcatJsonFeedWriterFactory instance = new DcatJsonFeedWriterFactory();
  
  public static DcatJsonFeedWriterFactory getInstance() {
    return instance;
  }
  
  protected DcatJsonFeedWriterFactory() {}
  
  public DcatJsonFeedWriter create(HttpServletRequest request, RequestContext context, PrintWriter writer, RestQuery query) {
    return new Dcat11JsonFeedWriter(request, context, writer, query, Boolean.TRUE);
  }
  public DcatJsonFeedWriter create(RequestContext context, PrintWriter writer, RestQuery query) {
    return new Dcat11JsonFeedWriter(context, writer, query);
  }
}
