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

import java.util.regex.Pattern;

/**
 * Flash video renderer factory.
 */
class FlashVideoRendererFactory extends NonMapBasedRendererFactory  {

  /** legal types of HTTP content */
  private static Pattern pattern = Pattern.compile("application/x-shockwave-flash");

  @Override
  protected IRenderer createRenderer(final String url) {
      return new FlashVideoRenderer() {

        @Override
        protected String getUrl() {
          return url;
        }
      };
  }

  @Override
  protected Pattern getEligibleContentTypePattern() {
    return pattern;
  }

  @Override
  public boolean isDefinitive() {
    return false;
  }

}
