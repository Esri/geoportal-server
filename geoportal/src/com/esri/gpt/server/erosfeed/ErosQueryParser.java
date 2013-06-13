/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.server.erosfeed;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.discovery.rest.RestQueryParser;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;

/**
 * EROS query parser.
 */
public class ErosQueryParser extends RestQueryParser {
  private Map<String, String[]> mixedParameters;

  /**
   * Creates an instance of the parser.
   * @param request HTTP request
   * @param context request context
   * @param query query
   * @param defaultParameters map of default parameters
   */
  public ErosQueryParser(HttpServletRequest request, RequestContext context, RestQuery query, Map<String, String[]> defaultParameters) {
    super(request, context, query);
    mixedParameters = mixParameters(defaultParameters, request.getParameterMap());
  }

  @Override
  public String getRequestParameter(String parameterKey) {
    for (Map.Entry<String, String[]> e : this.mixedParameters.entrySet()) {
      if (e.getKey().equalsIgnoreCase(parameterKey)) {
        if (e.getValue().length > 0) {
          return Val.chkStr(e.getValue()[0]);
        } else {
          return "";
        }
      }
    }
    return "";
  }

  private Map<String, String[]> mixParameters(Map<String, String[]> org, Map<String, String[]> ext) {
    TreeMap<String, String[]> mixed = new TreeMap<String, String[]>();
    mixed.putAll(org);
    mixed.putAll(ext);
    return mixed;
  }
  
}
