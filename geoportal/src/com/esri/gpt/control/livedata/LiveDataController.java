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

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import javax.servlet.http.HttpServletRequest;

/**
 * Live data controller.
 */
public class LiveDataController extends BaseActionListener implements ILiveDataProperties {

  private String _frameStyle;
  private String _frameStyleWide;
  private int _mapHeightAdjustment;

  /**
   * Gets current instance of the controller.
   * @return current instance of the controller
   */
  public static LiveDataController getCurrentInstance() {
    FacesContext fc = FacesContext.getCurrentInstance();
    VariableResolver vr = fc.getApplication().getVariableResolver();
    return (LiveDataController) vr.resolveVariable(fc, "LiveDataController");
  }

  /**
   * Gets service URL.
   * @return URL of the service
   */
  public String getServiceUrl() {
    HttpServletRequest request = getServletRequest();
    return Val.chkStr(request.getParameter("url") != null ? request.getParameter("url") : request.getQueryString());
  }

  /**
   * Gets URL of the info.
   * @return URL of the info
   */
  public String getInfoUrl() {
    HttpServletRequest request = getServletRequest();
    return Val.chkStr(request.getParameter("info"));
  }

  /**
   * Gets URL for the embeded snippet.
   * @return URL for the embeded snippet
   */
  public String getEmbededUrl() {
    return getEmbededUrl(getServiceUrl());
  }

  /**
   * Gets URL for the embeded snippet.
   * @param serviceUrl service URL
   * @return URL for the embeded snippet
   */
  public String getEmbededUrl(String serviceUrl) {
    HttpServletRequest request = getServletRequest();
    return getEmbedRoot(request) + "?url=" + encodeUrlParam(serviceUrl) + getWidgetSize();
  }

  /**
   * Gets definition of the embeded snippet.
   * @return definition of the embeded snippet
   */
  public String getEmbededSnippet() {
    return "<iframe src=\"" + getEmbededUrl() + "\" style=\"" + getFrameStyle() + "\" frameborder=\"0\" scrolling=\"no\"></iframe>";
  }

  /**
   * Sets widget frame style.
   * @param frameStyle widget frame style
   */
  public void setFrameStyle(String frameStyle) {
    this._frameStyle = frameStyle;
  }

  /**
   * Gets widget frame style.
   * @return widget frame style
   */
  @Override
  public String getFrameStyle() {
    return _frameStyle;
  }

  /**
   * Sets widget frame style.
   * @param frameStyle widget frame style
   */
  public void setFrameStyleWide(String frameStyle) {
    this._frameStyleWide = frameStyle;
  }

  /**
   * Gets widget frame style.
   * @return widget frame style
   */
  public String getFrameStyleWide() {
    return _frameStyleWide;
  }

  /**
   * Gets map height adjustment.
   * @return map height adjustment
   */
  @Override
  public int getMapHeightAdjustment() {
    return _mapHeightAdjustment;
  }

  /**
   * Sets map height adjustment.
   * @param mapHeightAdjustment map height adjustment
   */
  public void setMapHeightAdjustment(int mapHeightAdjustment) {
    this._mapHeightAdjustment = mapHeightAdjustment;
  }

  /**
   * Gets widget size.
   * @return widget size string based upon frame size
   */
  private String getWidgetSize() {
    StringBuilder sb = new StringBuilder();

    String style = getInfoUrl().length() > 0? getFrameStyle(): getFrameStyleWide();
    String [] styleElements = style.split(";");
    for (String styleElement : styleElements) {
      int colonIdx = styleElement.indexOf(":");
      if (colonIdx>=0) {
        String elementName = Val.chkStr(styleElement.substring(0, colonIdx));
        String elementValue = Val.chkStr(styleElement.substring(colonIdx+1)).replaceAll(" ", "");
        if (elementName.equalsIgnoreCase("width") && elementValue.length()>0) {
          sb.append("&");
          sb.append("width=").append(elementValue);
        } else if (elementName.equalsIgnoreCase("height") && elementValue.length()>0) {
          sb.append("&");
          sb.append("height=").append(elementValue);
        }
      }
    }

    return sb.toString();
  }

  /**
   * Encodes a URL parameter value.
   * @param value the URL parameter value to encode
   * @return the encoded parameter value
   */
  private static String encodeUrlParam(String value) {
    value = Val.chkStr(value);
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LogUtil.getLogger().severe("Unsupported encoding: UTF-8");
      return value;
    }
  }

  /**
   * Gets servlet request
   * @return servlet request
   */
  private HttpServletRequest getServletRequest() {
    RequestContext context = this.extractRequestContext();
    return (HttpServletRequest) context.getServletRequest();
  }

  /**
   * Gets embded URL.
   * @param request HTTP request
   * @return URL of the embeded snippet handler
   */
  private static String getEmbedRoot(HttpServletRequest request) {
    return getContextRoot(request) + "/catalog/livedata/embed.jsp";
  }

  /**
   * Gets context root.
   * @param request HTTP request
   * @return context root
   */
  private static String getContextRoot(HttpServletRequest request) {
    return RequestContext.resolveBaseContextPath(request);
  }
}
