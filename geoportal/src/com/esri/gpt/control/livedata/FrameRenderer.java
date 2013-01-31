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
 * &lt;iframe&gt; renderer.
 */
/*package*/ abstract class FrameRenderer implements IRenderer {

  /**
   * Gets service URL.
   * @return service URL
   */
  protected abstract String getUrl();

  @Override
  public void render(Writer writer) throws IOException {
    writer.write(
      "{ init: function(widget){" +
      "    var node = widget.getPlaceholder();"+
      "    var style = widget.getMapStyle();"+
      "    var styles = style.split(\";\");" +
      "    for (var i=0; i<styles.length; i++) {" +
      "       if (styles[i].indexOf(\"height\")>=0) {" +
      "          var helm = styles[i].split(\":\");" +
      "          if (helm.length==2) {" +
      "             var height = parseInt(helm[1]);" +
      "             height -= 2;" +
      "             styles[i] = \"height: \" + height + \"px\";" +
      "          }" +
      "       }" +
      "       if (styles[i].indexOf(\"width\")>=0) {" +
      "          var welm = styles[i].split(\":\");" +
      "          if (welm.length==2) {" +
      "             var width = parseInt(welm[1]);" +
      "             width -= 2;" +
      "             styles[i] = \"width: \" + width + \"px\";" +
      "          }" +
      "       }" +
      "    }" +
      "    style = styles.join(\";\");" +
      "    node.innerHTML = \"<iframe style=\\\"\" + style + \"\\\" src=\\\"" +getUrl()+ "\\\" scrolling=\\\"auto\\\"/>\";" +
      "} }");
  }

  @Override
  public String toString() {
    return FrameRenderer.class.getSimpleName() + "("+getUrl()+")";
  }

}
