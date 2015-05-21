/*
 * Copyright 2015 Esri, Inc..
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
package com.esri.gpt.server.csw.components;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Provider factory helper.
 */
public class ProviderFactoryHelper {
    
    /**
     * Creates instance of the provider factory.
     * @param requestContext request context
     * @return instance of the provider factory.
     */
    public static IProviderFactory newInstance(RequestContext requestContext) {
        if (requestContext!=null) {
            ServletRequest servletRequest = requestContext.getServletRequest();
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest)servletRequest;
                String version = Val.chkStr(request.getParameter("version"));
                if ("2.0.2".equals(version)) {
                    return new com.esri.gpt.server.csw.provider.local.ProviderFactory();
                }
            }
        }
        return new com.esri.gpt.server.csw.provider3.local.ProviderFactory();
    }
}
