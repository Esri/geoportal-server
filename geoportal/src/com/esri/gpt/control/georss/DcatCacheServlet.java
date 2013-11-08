/*
 * Copyright 2013 Esri.
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

import com.esri.gpt.control.georss.dcatcache.DcatCache;
import com.esri.gpt.control.georss.dcatcache.DcatCacheUpdateRequest;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DCAt cache servlet.
 */
public class DcatCacheServlet extends BaseServlet {

  @Override
  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context) throws Exception {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Content-disposition", "attachment; filename=\"dcat.json\"");
    
    PrintWriter writer = response.getWriter();
    String callback = request.getParameter("callback");
    if (callback != null) {
      writer.print(callback + "(");
    }

    DcatCache cache = DcatCache.getInstance();
    InputStream cacheStream = null;

    try {
      cacheStream = cache.createInputCacheStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(cacheStream));

      char[] buffer = new char[1024];
      int length = -1;

      while ((length = reader.read(buffer)) > 0) {
        writer.write(buffer, 0, length);
      }

    } catch (FileNotFoundException ex) {
      writer.println("[]");

      DcatCacheUpdateRequest cureq = new DcatCacheUpdateRequest();
      cureq.execute();
    } finally {
      if (cacheStream != null) {
        try {
          cacheStream.close();
        } catch (IOException ex) {
        }
      }
    }

    if (callback != null) {
      writer.print(")");
    }
  }
}
