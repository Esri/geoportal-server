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

import com.esri.gpt.framework.geometry.Envelope;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Map based renderer.
 */
/*packge*/ abstract class MapBasedRenderer implements IRenderer {

  /**
   * Gets layer declaration.
   * @return layer declaration code
   */
  protected abstract String newLayerDeclaration();

  /**
   * Gets map height adjustment.
   * @return map height adjustment
   */
  protected abstract int getMapHeightAdjustment();

  /**
   * Finalizes live data layer.
   * @return layer finalization code
   */
  protected String finalizeNewLayer() { return ""; };
  
  /**
   * Gets extent.
   * @return extent
   */
  protected Envelope getExtent() { return null; }

  /**
   * Checks if base map layer supposed to be generated.
   * @return <code>true</code> if base map layer supposed to be generated
   */
  protected boolean generateBaseMap() {
    return true;
  }

  public void render(Writer writer) throws IOException {
    String newLayer = newLayerDeclaration();
    boolean isNewLayer = newLayer.length()>0;

    PrintWriter pwriter = new PrintWriter(writer);
    pwriter.println("{");
    pwriter.println("init: function(widget){");
    pwriter.println(
      "    var node = widget.getPlaceholder();"+
      "    var style = widget.getMapStyle();"+
      "    var styles = style.split(\";\");" +
      "    for (var i=0; i<styles.length; i++) {" +
      "       if (styles[i].indexOf(\"height\")>=0) {" +
      "          var helm = styles[i].split(\":\");" +
      "          if (helm.length==2) {" +
      "             var height = parseInt(helm[1]);" +
      "             height = height + (" +getMapHeightAdjustment()+ ");" +
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
      "    style = styles.join(\";\");"
    );
    pwriter.print("node.innerHTML =\"");
    writeToolbarNode(pwriter,isNewLayer);
    writeMapNode(pwriter);
    pwriter.println("\";");
    pwriter.println("node.geometryService = (widget.getGeometryServiceUrl()!=null && widget.getGeometryServiceUrl().length>0)? new esri.tasks.GeometryService(widget.getGeometryServiceUrl()): null;");
    pwriter.println("node.liveDataMap = new esri.Map(widget.getMapId());");
    pwriter.println("dojo.connect(window,\"onresize\",node.liveDataMap,\"reposition\");");
    if (generateBaseMap()) {
      pwriter.println("node.baseMapLayer = new esri.layers.ArcGISDynamicMapServiceLayer(widget.getMapServiceUrl());");
      pwriter.println("node.baseMapLayer.setImageFormat(\"jpg\");");
      pwriter.println("node.liveDataMap.addLayer(node.baseMapLayer);");
    }
    if (isNewLayer) {
      pwriter.println("node.liveDataLayer = " + newLayer + ";");
      pwriter.println(finalizeNewLayer());
      String extentDef = createExtentDef();
      if (!generateBaseMap()) {
        pwriter.println("node.liveDataMap.addLayer(node.liveDataLayer);");
      }
      if (extentDef!=null) {
        pwriter.println("dojo.connect(node.liveDataMap,\"onLoad\",null,function(map) {gpt.LiveData.setExtent(node.liveDataMap," +extentDef+ ",node.geometryService);});");
      }
    }
    pwriter.println("node.navToolbar = new esri.toolbars.Navigation(node.liveDataMap);");
    pwriter.println("dojo.connect(dojo.byId(\"zoomfullext-hook\"),\"onclick\",null,function(event) { gpt.LiveData.setExtent(node.liveDataMap,new esri.geometry.Extent({xmin: -90, ymin: -180, xmax: 90, ymax: 180, spatialReference: {wkid: 4326}}),node.geometryService); });");
    pwriter.println("dojo.connect(dojo.byId(\"zoomprev-hook\"),\"onclick\",null,function(event) {node.navToolbar.zoomToPrevExtent();});");
    pwriter.println("dojo.connect(dojo.byId(\"zoomnext-hook\"),\"onclick\",null,function(event) {node.navToolbar.zoomToNextExtent();});");
    if (generateBaseMap() && isNewLayer) {
      pwriter.println("dojo.connect(dojo.byId(\"showhide-hook\"),\"onclick\",null,function(event) {if (dojo.byId(\"showhide\").checked) {node.baseMapLayer.show();} else {node.baseMapLayer.hide();} });");
    }
    pwriter.println("dijit.Tooltip.defaultPosition = [\"below\", \"above\"];");
    pwriter.println("new dijit.Tooltip( { connectId: [\"zoomfullext-hook\"], label: \"<div class=\\\"toolTip\\\">\" + widget.getTooltips()[0] + \"</div>\" } );");
    pwriter.println("new dijit.Tooltip( { connectId: [\"zoomprev-hook\"], label: \"<div class=\\\"toolTip\\\">\" + widget.getTooltips()[1] + \"</div>\" } );");
    pwriter.println("new dijit.Tooltip( { connectId: [\"zoomnext-hook\"], label: \"<div class=\\\"toolTip\\\">\" + widget.getTooltips()[2] + \"</div>\" } );");
    if (generateBaseMap() && isNewLayer) {
      pwriter.println("new dijit.Tooltip( { connectId: [\"showhide-hook\"], label: \"<div class=\\\"toolTip\\\">\" + widget.getTooltips()[3] + \"</div>\" } );");
    }
    pwriter.println("} }");
  }

  /**
   * Writes toolbar node.
   * @param writer writer
   * @param isNewLayer flag indicating if there is a new layer
   * @throws java.io.IOException if writting fails
   */
  private void writeToolbarNode(PrintWriter writer, boolean isNewLayer) throws IOException {
    writer.print("<div id=\\\"navToolbar\\\" class=\\\"dijit dijitToolbar\\\">");
    writer.print("<table width=\\\"100%\\\" border=\\\"0\\\">");
    writer.print("<tr>");
    writer.print("<td>");
    writeButtonNode(writer, "zoomfullext", "zoomfullextIcon");
    writeButtonNode(writer, "zoomprev", "zoomprevIcon");
    writeButtonNode(writer, "zoomnext", "zoomnextIcon");
    if (generateBaseMap() && isNewLayer) {
      writer.print("<span id=\\\"showhide-hook\\\" class=\\\"dijit dijitReset dijitLeft dijitInline dijitButton previewButton\\\"");
      writer.print("<span class=\\\"dijitReset dijitRight dijitInline\\\">");
      writer.print("<span class=\\\"dijitReset dijitInline\\\">");
      writer.print("<input type=\\\"checkbox\\\" id=\\\"showhide\\\" checked=\\\"checked\\\" style=\\\"vertical-align: center;\\\"/><label for=\\\"showhide\\\" style=\\\"vertical-align: center;\\\">\" +widget.getBasemapLabel()+ \"</label>");
      writer.print("</span>");
      writer.print("</span>");
      writer.print("</span>");
    }
    writer.print("</td>");
    writer.print("</tr>");
    writer.print("</table>");
    writer.print("</div>");
  }

  /**
   * Writes button node.
   * @param writer writer
   * @param id button id
   * @param styleClass additional style class determining background icon
   * @param onClick onclick event code
   * @throws java.io.IOException if writting fails
   */
  private void writeButtonNode(PrintWriter writer, String id, String styleClass) throws IOException {
    writer.print("<span id=\\\"" +id+ "-hook\\\" class=\\\"dijit dijitReset dijitLeft dijitInline dijitButton previewButton\\\"");
    writer.print("<span class=\\\"dijitReset dijitRight dijitInline\\\">");
    writer.print("<span class=\\\"dijitReset dijitInline dijitButtonNode\\\">");
    writer.print("<button waistate=\\\"labelledby-zoomin_label\\\" type=\\\"button\\\" name=\\\"\\\" class=\\\"dijitReset dijitStretch dijitButtonContents\\\" id=\\\"" +id+ "\\\" tabindex=\\\"-1\\\" style=\\\"-moz-user-select: none;\\\">");
    writer.print("<span class=\\\"dijitReset dijitInline " +styleClass+ "\\\">");
    writer.print("<span class=\\\"dijitReset dijitToggleButtonIconChar\\\">✓</span>");
    writer.print("</span>");
    writer.print("</button>");
    writer.print("</span>");
    writer.print("</span>");
    writer.write("</span>");
  }

  /**
   * Writes map node.
   * @param writer writer
   * @throws java.io.IOException if writting fails
   */
  private void writeMapNode(PrintWriter writer) throws IOException {
    writer.print("<div id=\\\"\" +widget.getMapId()+ \"\\\" class=\\\"tundra\\\" style=\\\"\" + widget.getMapStyle() + \"\\\">");
  }

  /**
   * Creates extent definition.
   * @return extent definition
   */
  protected String createExtentDef() {
    Envelope e = getExtent();
    return e!=null? "new esri.geometry.Extent({" +
      "xmin:" +e.getMinX()+ "," +
      "ymin:" +e.getMinY()+ "," +
      "xmax:" +e.getMaxX()+ "," +
      "ymax:" +e.getMaxY()+ "," +
      "spatialReference:{wkid:" +e.getWkid()+ "}})": null;
  }
}
