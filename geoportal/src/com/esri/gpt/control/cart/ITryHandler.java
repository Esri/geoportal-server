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
package com.esri.gpt.control.cart;

import com.esri.gpt.framework.context.RequestContext;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * "/try" handler.
 * @author Esri, Inc.
 */
public interface ITryHandler {
  /**
   * Tries keys.
   * @param request HTTP request
   * @param response HTTP response
   * @param context request context
   * @param cart cart
   * @param keys keys to try
   * @return response
   */
  TryResponse tryKeys(HttpServletRequest request,HttpServletResponse response, RequestContext context,Cart cart,List<String> keys);
}
