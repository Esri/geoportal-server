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
package com.esri.gpt.framework.jsf.components;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

/**
 * UI of Live Data.
 */
public class UILiveData extends UIComponentBase implements Serializable {

// class variables =============================================================
/** The JSF Component type. */
public static final String COMPONENT_TYPE = "com.esri.gpt.faces.LiveData";
/** The JSF Component family. */
public static final String COMPONENT_FAMILY = COMPONENT_TYPE;

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================
/**
 * Gets component family.
 * @return component family ({@link UIPrepareView#COMPONENT_FAMILY})
 */
@Override
public String getFamily() {
  return COMPONENT_FAMILY;
}

/**
 * Completes begin phase of component encoding.
 * @param context faces context
 * @throws IOException if encoding fails
 */
@Override
public void encodeBegin(FacesContext context) throws IOException {
  super.encodeBegin(context);

  try {
    String url = getUrl();

    if (url.length()>0) {
      HttpServletRequest request = (new FacesContextBroker()).extractHttpServletRequest();
      String contextPath = request.getContextPath();
      String proxy = contextPath + "/catalog/livedata/liveDataProxy.page";

      ResponseWriter writer = context.getResponseWriter();

      writer.write(
        "<div dojoType=\"gpt.LiveData\" " +
        "proxy=\"" + proxy + "\" " +
        "url=\"" + url + "\" " +
        "mapStyle=\"" +getMapStyle()+ "px\" " +
        "mapService=\"" +getMapService()+ "\" " +
        "geometryService=\"" +getGeometryService()+ "\" " +
        "verbose=\"" +getVerbose()+ "\" " +
        "errorMessage=\"" +getErrorMessage()+ "\" " +
        "basemapLabel=\"" +getBasemapLabel()+ "\" " +
        "tooltips=\"" +getTooltips()+ "\" " +
        "onCreatePlaceholder=\"" +getOnCreatePlaceholder()+ "\"></div>"
        );
    }

  } catch (Throwable t) {
    MessageBroker messageBroker =
      new FacesContextBroker().extractMessageBroker();
    messageBroker.addErrorMessage(t);
    LogUtil.getLogger().log(Level.SEVERE, "Exception raised.", t);
  }
}

private String getStringAttribute(String attrName) {
  Object name = this.getAttributes().get(attrName);
  if(name instanceof ValueBinding) {
    name = ((ValueBinding) name).getValue(this.getFacesContext());
    if(name == null) {
      return "";
    }
    else return name.toString();
  }
  else if(name instanceof String) {
    return name.toString();
  }
  return "";
}

private String getMapService() {
  HttpServletRequest request = (new FacesContextBroker()).extractHttpServletRequest();
  return Val.chkStr(getStringAttribute("mapService"), RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap().getMapServiceUrl());
}

private String getGeometryService() {
  HttpServletRequest request = (new FacesContextBroker()).extractHttpServletRequest();
  return Val.chkStr(getStringAttribute("geometryService"), RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap().getGeometryServiceUrl());
}

private String getUrl() {
  return getStringAttribute("url");
}

private String getMapStyle() {
  return Val.chkStr(getStringAttribute("mapStyle"),"width: 600px; height: 400px; border:1px solid #000;");
}

private boolean getVerbose() {
  return Val.chkBool(getStringAttribute("verbose"), false);
}

private String getErrorMessage() {
  MessageBroker msg = new FacesContextBroker().extractMessageBroker();
  return Val.chkStr(getStringAttribute("errorMessage"), msg.retrieveMessage("catalog.search.liveData.errorMessage"));
}

private String getBasemapLabel() {
  MessageBroker msg = new FacesContextBroker().extractMessageBroker();
  return Val.chkStr(getStringAttribute("basemapLabel"), msg.retrieveMessage("catalog.search.liveData.basemapLabel"));
}

private String getTooltips() {
  MessageBroker msg = new FacesContextBroker().extractMessageBroker();
  return Val.chkStr(getStringAttribute("tooltips"), msg.retrieveMessage("catalog.search.liveData.tooltips"));
}

private String getOnCreatePlaceholder() {
  MessageBroker msg = new FacesContextBroker().extractMessageBroker();
  return Val.chkStr(getStringAttribute("onCreatePlaceholder"), "");
}

}
