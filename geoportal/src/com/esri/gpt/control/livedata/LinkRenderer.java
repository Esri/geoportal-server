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

import java.io.IOException;
import java.io.Writer;

/**
 * Simple link renderer.
 */
/*package*/ abstract class LinkRenderer implements IRenderer {

  /**
   * Gets service URL.
   * @return service URL
   */
  protected abstract String getUrl();

  public void render(Writer writer) throws IOException {
    writer.write(
      "{ init: function(widget){" +
      "    var node = widget.getPlaceholder();"+
      "    node.innerHTML = \"<a href=\\\"" +getUrl()+ "\\\">" +getUrl()+ "</a>\";" +
      "} }");
  }

  @Override
  public String toString() {
    return LinkRenderer.class.getSimpleName() + "("+getUrl()+")";
  }

}
