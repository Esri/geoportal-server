/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.livedata;

import com.esri.gpt.control.livedata.selector.HttpRequestDefinition;
import com.esri.gpt.control.livedata.selector.IHttpResponseListener;
import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.http.ResponseInfo;
import com.esri.gpt.framework.util.Val;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;

/**
 * Non-map based renderer factory.
 */
/**package*/ abstract class NonMapBasedRendererFactory implements IRendererFactory {

  public boolean isDefinitive() {
    return false;
  }
  
  public void register(IRegistry reg, final ISetter setter, final String url) {
    reg.register(new HttpRequestDefinition(url), new IHttpResponseListener() {
      public void onResponse(ResponseInfo info, String strContent, Document docContent) {
        if (assertContentType(info.getContentType())) {
          setter.set(createRenderer(url));
        }
      }
    });
  }

  /**
   * Creates renderer for the given URL.
   * @param url URL
   * @return renderer
   */
  protected abstract IRenderer createRenderer(String url);

  /**
   * Gets eligible content types pattern.
   * @return pattern
   */
  protected abstract Pattern getEligibleContentTypePattern();

  /**
   * Asserts content type.
   * @param contentType content type to assert
   * @throws IllegalArgumentException if illegal content type
   */
  private boolean assertContentType(String contentType) {
    contentType = Val.chkStr(contentType);
    if (contentType.length()==0) return false;
    Pattern pattern = getEligibleContentTypePattern();
    if (pattern==null) return false;
    Matcher matcher = pattern.matcher(contentType);
    if (matcher.matches()) return true;
    return false;
  }

}
