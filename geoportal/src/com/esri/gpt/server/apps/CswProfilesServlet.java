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

package com.esri.gpt.server.apps;

import com.esri.gpt.catalog.search.SearchGptXslProfiles;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswProfiles;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides list of the CSW profiles.
 */
public class CswProfilesServlet extends BaseServlet {
  @Override
  protected void execute(HttpServletRequest request, 
                         HttpServletResponse response,
                         RequestContext context) throws Exception {    
    
    StringBuilder sbProfiles = new StringBuilder();
    
    try {
      SearchGptXslProfiles profiles = new SearchGptXslProfiles();
      profiles.loadProfilefromConfig();
      
      CswProfiles cswProfiles = profiles.getCswProfiles();
      for (CswProfile cswProfile: cswProfiles.getProfilesAsCollection()) {
        String id = cswProfile.getId();
        String name = cswProfile.getName();
        
        String cswJsonProfile = "{ \"id\": \"" +Val.escapeStrForJson(id)+ "\", \"name\": \"" +Val.escapeStrForJson(name)+ "\"}";
        if (sbProfiles.length()>0) {
          sbProfiles.append(",");
        }
        sbProfiles.append(cswJsonProfile);
      }
    } catch (Exception ex) {
      
    }
    
    String callback = Val.chkStr(request.getParameter("callback"));
    String responseText = (!callback.isEmpty()? callback+"(": "")+"{ \"cswProfiles\": [" + sbProfiles.toString() + "]}" + (!callback.isEmpty()? ")": "");
    
    response.setContentType("application/json");
    response.getWriter().write(responseText);
  }
}
