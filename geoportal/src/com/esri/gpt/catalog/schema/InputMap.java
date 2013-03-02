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
package com.esri.gpt.catalog.schema;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * Interactive map component.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="envelope.map"&gt;
 * <br/>&lt;input type="map"/&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputMap extends Input {

// class variables =============================================================
  
// instance variables ==========================================================
private int _height;
private int _width;
  
// constructors ================================================================

/** Default constructor. */
public InputMap() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputMap(InputMap objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setWidth(getDefaultWidth());
    setHeight(getDefaultHeight());
  } else {
    setWidth(objectToDuplicate.getWidth());
    setHeight(objectToDuplicate.getHeight());
  }
}

// properties ==================================================================

/**
 * Gets the default height (300).
 * @return the default size
 */
public int getDefaultHeight() {
  return 300;
}

/**
 * Gets the default width (600).
 * @return the default width
 */
public int getDefaultWidth() {
  return 600;
}

/**
 * Gets the map height.
 * @return the height
 */
public int getHeight() {
  return _height;
}
/**
 * Sets the map height.
 * @param size the height
 */
public void setHeight(int size) {
  _height = (size < 1) ? getDefaultHeight() : size;
}

/**
 * Gets the map width.
 * @return the width
 */
public int getWidth() {
  return _width;
}
/**
 * Sets the map width.
 * @param size the width
 */
public void setWidth(int size) {
  _width = (size < 1) ? getDefaultWidth() : size;
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>width height
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setWidth(Val.chkInt(DomUtil.getAttributeValue(attributes,"width"),-1));
  setHeight(Val.chkInt(DomUtil.getAttributeValue(attributes,"height"),-1));
  //setStyleClass("tundra");
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputText(this);
 */
public InputMap duplicate() {
  return new InputMap(this);
}

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
@Override
public void echo(StringBuffer sb) {
  super.echo(sb);
  sb.append(" width=\"").append(getWidth()).append("\"");
  sb.append(" height=\"").append(getHeight()).append("\"");
}

/**
 * Makes a Faces HtmlInputText component for a parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  
  // make the component
  MessageBroker msgBroker = context.extractMessageBroker();
  HtmlPanelGroup panel = new HtmlPanelGroup();
  
  // make the map tools
  HtmlPanelGroup toolbar = new HtmlPanelGroup();
  toolbar.setId("mapToolbar");
  toolbar.setStyleClass("mapToolbar");
  panel.getChildren().add(toolbar);
  
  HtmlGraphicImage btn;
  String sMsg;
  
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.zoomToWorld");
  btn.setId("mapButton-zoomToWorld");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setUrl("/catalog/images/btn-zoomToWorld-off.gif");
  toolbar.getChildren().add(btn);
  
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.zoomToInputEnvelope");
  btn.setId("mapButton-zoomToInputEnvelope");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setUrl("/catalog/images/btn-zoomToInputEnvelope-off.gif");
  toolbar.getChildren().add(btn);
    
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.drawInputEnvelope");
  btn.setId("mapTool-drawInputEnvelope");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setStyleClass("firstTool");
  btn.setUrl("/catalog/images/btn-drawInputEnvelope-off.gif");
  toolbar.getChildren().add(btn);
  
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.deactivate");
  btn.setId("mapTool-deactivate");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setUrl("/catalog/images/btn-deactivate-off.gif");
  toolbar.getChildren().add(btn);
  
  HtmlInputText text = new HtmlInputText();
  text.setId("mapInput-locate");
  text.setStyleClass("locatorInput");
  text.setMaxlength(1024);
  text.setOnkeypress("return mdeMap.onLocatorKeyPress(event);");
  toolbar.getChildren().add(text);
  
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.locate");
  btn.setId("mapButton-locate");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setUrl("/catalog/images/btn-locate-off.gif");
  toolbar.getChildren().add(btn);
  
  // make the map component
  StringBuffer sb = new StringBuffer();
  String sMapId = "interactiveMap";
  String sMapWH = "width:"+getWidth()+"px; height:"+getHeight()+"px;";
  String sCursor = " cursor:hand; cursor:pointer;";

  sb.append("<div id=\"locatorCandidates\" class=\"locatorCandidates\"></div>");
  
  sb.append("<div style=\"margin-top:1px; border:1px solid #000000; ");
  sb.append(sMapWH).append("\">");
  sb.append("<div id='").append(sMapId).append("'");
  if (getStyleClass().length() > 0) {
    sb.append(" class=\"").append(getStyleClass()).append("\"");
  }
  sb.append(" style=\"").append(sMapWH+sCursor).append("\"></div></div>");
  
  HtmlOutputText mapComponent = new HtmlOutputText();
  mapComponent.setEscape(false);
  mapComponent.setValue(sb.toString());
  panel.getChildren().add(mapComponent);
  
  return panel;
}
/**
 * Makes the Faces UI output component for a parameter.
 * <p/>
 * The output component is suitable for display on the 
 * metadata details page.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI input component
 */
public UIComponent makeOutputComponent(UiContext context,
                                       Section section,
                                       Parameter parameter) {
  
  // make the component
  MessageBroker msgBroker = context.extractMessageBroker();
  HtmlPanelGroup panel = new HtmlPanelGroup();
  
  // make the map tools
  HtmlPanelGroup toolbar = new HtmlPanelGroup();
  toolbar.setId("mapToolbar");
  toolbar.setStyleClass("mapToolbar");
  panel.getChildren().add(toolbar);
  
  HtmlGraphicImage btn;
  String sMsg;
    
  btn = new HtmlGraphicImage();
  sMsg = msgBroker.retrieveMessage("catalog.general.map.zoomToInputEnvelope");
  btn.setId("mapButton-zoomToInputEnvelope");
  btn.setAlt(sMsg);
  btn.setTitle(sMsg);
  btn.setUrl("/catalog/images/btn-zoomToInputEnvelope-off.gif");
  toolbar.getChildren().add(btn);
  
  // make the map component
  StringBuffer sb = new StringBuffer();
  String sMapId = "interactiveMap";
  String sMapWH = "width:"+getWidth()+"px; height:"+getHeight()+"px;";
  String sCursor = " cursor:hand; cursor:pointer;";
    
  sb.append("<div style=\"margin-top:1px; border:1px solid #000000; ");
  sb.append(sMapWH).append("\">");
  sb.append("<div id='").append(sMapId).append("'");
  if (getStyleClass().length() > 0) {
    sb.append(" class=\"").append(getStyleClass()).append("\"");
  }
  sb.append(" style=\"").append(sMapWH+sCursor).append("\"></div></div>");
  
  HtmlOutputText mapComponent = new HtmlOutputText();
  mapComponent.setEscape(false);
  mapComponent.setValue(sb.toString());
  panel.getChildren().add(mapComponent);
  
  return panel;
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @param parameter the associated parameter
 * @throws SchemaException if an associated Faces UIComponent cannot be located
 */
@Override
public void unBind(UiContext context, 
                   UIComponent editorForm,
                   Parameter parameter) 
  throws SchemaException {
}

}
