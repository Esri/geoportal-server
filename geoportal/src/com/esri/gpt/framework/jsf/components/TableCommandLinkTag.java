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

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import java.util.logging.Logger;

/**
 * Table command link tag.
 */
public class TableCommandLinkTag extends UIComponentTag {

  /** The LOG. */
  private static Logger LOG =
    Logger.getLogger(TableCommandLinkTag.class.getCanonicalName());

  /** tag support */
  private final TagSupport tagSupport = new TagSupport();
  private String action;
  private String actionListener;
  private String immediate;
  private String value;
  private String accesskey;
  private String charset;
  private String coords;
  private String dir;
  private String hreflang;
  private String lang;
  private String onblur;
  private String ondblclick;
  private String onfocus;
  private String onkeydown;
  private String onkeypress;
  private String onkeyup;
  private String onmousedown;
  private String onmousemove;
  private String onmouseout;
  private String onmouseover;
  private String onmouseup;
  private String rel;
  private String rev;
  private String shape;
  private String style;
  private String styleClass;
  private String tabindex;
  private String target;
  private String title;
  private String type;

  /**
   * Gets the component type.
   *
   * @return COMPONENT_TYPE
   */
  @Override
  public String getComponentType() {
    return UITableCommandLink.COMPONENT_TYPE;
  }

  /**
   * Gets renderer type.
   * @return <code>null</code>
   */
  @Override
  public String getRendererType() {
    return null;
  }

  /**
   * Sets action.
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Sets actionlistener.
   * @param actionListener the actionListener to set
   */
  public void setActionListener(String actionListener) {
    this.actionListener = actionListener;
  }

  /**
   * Sets immediate.
   * @param immediate the immediate to set
   */
  public void setImmediate(String immediate) {
    this.immediate = immediate;
  }

  /**
   * Sets value.
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Sets accesskey.
   * @param accesskey the accesskey to set
   */
  public void setAccesskey(String accesskey) {
    this.accesskey = accesskey;
  }

  /**
   * Sets charset.
   * @param charset the charset to set
   */
  public void setCharset(String charset) {
    this.charset = charset;
  }

  /**
   * Sets coords.
   * @param coords the coords to set
   */
  public void setCoords(String coords) {
    this.coords = coords;
  }

  /**
   * Sets dir.
   * @param dir the dir to set
   */
  public void setDir(String dir) {
    this.dir = dir;
  }

  /**
   * Sets hreflang.
   * @param hreflang the hreflang to set
   */
  public void setHreflang(String hreflang) {
    this.hreflang = hreflang;
  }

  /**
   * Sets lang.
   * @param lang the lang to set
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Sets onblur.
   * @param onblur the onblur to set
   */
  public void setOnblur(String onblur) {
    this.onblur = onblur;
  }

  /**
   * Sets ondblclick.
   * @param ondblclick the ondblclick to set
   */
  public void setOndblclick(String ondblclick) {
    this.ondblclick = ondblclick;
  }

  /**
   * Sets onfocus.
   * @param onfocus the onfocus to set
   */
  public void setOnfocus(String onfocus) {
    this.onfocus = onfocus;
  }

  /**
   * Sets onkeydown.
   * @param onkeydown the onkeydown to set
   */
  public void setOnkeydown(String onkeydown) {
    this.onkeydown = onkeydown;
  }

  /**
   * Sets onkeypress.
   * @param onkeypress the onkeypress to set
   */
  public void setOnkeypress(String onkeypress) {
    this.onkeypress = onkeypress;
  }

  /**
   * Sest onkeyup.
   * @param onkeyup the onkeyup to set
   */
  public void setOnkeyup(String onkeyup) {
    this.onkeyup = onkeyup;
  }

  /**
   * Sets onmousedown.
   * @param onmousedown the onmousedown to set
   */
  public void setOnmousedown(String onmousedown) {
    this.onmousedown = onmousedown;
  }

  /**
   * Sets onmousemove.
   * @param onmousemove the onmousemove to set
   */
  public void setOnmousemove(String onmousemove) {
    this.onmousemove = onmousemove;
  }

  /**
   * Sets onmouseout.
   * @param onmouseout the onmouseout to set
   */
  public void setOnmouseout(String onmouseout) {
    this.onmouseout = onmouseout;
  }

  /**
   * Sets onmouseover.
   * @param onmouseover the onmouseover to set
   */
  public void setOnmouseover(String onmouseover) {
    this.onmouseover = onmouseover;
  }

  /**
   * Sets onmouseup.
   * @param onmouseup the onmouseup to set
   */
  public void setOnmouseup(String onmouseup) {
    this.onmouseup = onmouseup;
  }

  /**
   * Sets rel.
   * @param rel the rel to set
   */
  public void setRel(String rel) {
    this.rel = rel;
  }

  /**
   * Sets rev.
   * @param rev the rev to set
   */
  public void setRev(String rev) {
    this.rev = rev;
  }

  /**
   * Sets shape.
   * @param shape the shape to set
   */
  public void setShape(String shape) {
    this.shape = shape;
  }

  /**
   * Sets style.
   * @param style the style to set
   */
  public void setStyle(String style) {
    this.style = style;
  }

  /**
   * Sets style class.
   * @param styleClass the styleClass to set
   */
  public void setStyleClass(String styleClass) {
    this.styleClass = styleClass;
  }

  /**
   * Sets tab index.
   * @param tabindex the tabindex to set
   */
  public void setTabindex(String tabindex) {
    this.tabindex = tabindex;
  }

  /**
   * Sets target.
   * @param target the target to set
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Sets title.
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets type.
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Sets properties.
   * @param component component to set properties on
   */
  @Override
  protected void setProperties(UIComponent component) {
    super.setProperties(component);

    LOG.finer("Setting UIComponent properties");

    tagSupport.setActionBind(component, "action", action);
    tagSupport.setPropMethodBind(component, "actionListener", actionListener);
    tagSupport.setPropValueBind(component, "accesskey", this.accesskey);
    tagSupport.setPropValueBind(component, "charset", this.charset);
    tagSupport.setPropValueBind(component, "coords", this.coords);
    tagSupport.setPropValueBind(component, "dir", this.dir);
    tagSupport.setPropValueBind(component, "hreflang", this.hreflang);
    component.setId(getId());
    tagSupport.setPropValueBind(component, "immediate", this.immediate);
    tagSupport.setPropValueBind(component, "lang", this.lang);

    tagSupport.setPropValueBind(component, "onblur", this.onblur);
    tagSupport.setPropValueBind(component, "ondblclick", this.ondblclick);
    tagSupport.setPropValueBind(component, "onfocus", this.onfocus);
    tagSupport.setPropValueBind(component, "onkeydown", this.onkeydown);
    tagSupport.setPropValueBind(component, "onkeypress", this.onkeypress);
    tagSupport.setPropValueBind(component, "onkeyup", this.onkeyup);
    tagSupport.setPropValueBind(component, "onmousedown", this.onmousedown);
    tagSupport.setPropValueBind(component, "onmousemove", this.onmousemove);
    tagSupport.setPropValueBind(component, "onmouseout", this.onmouseout);
    tagSupport.setPropValueBind(component, "onmouseover", this.onmouseover);
    tagSupport.setPropValueBind(component, "onmouseup", this.onmouseup);

    tagSupport.setPropValueBind(component, "rel", this.rel);
    tagSupport.setPropValueBind(component, "rev", this.rev);
    tagSupport.setPropValueBind(component, "shape", this.shape);
    tagSupport.setPropValueBind(component, "style", this.style);
    tagSupport.setPropValueBind(component, "styleClass", this.styleClass);
    tagSupport.setPropValueBind(component, "tabindex", this.tabindex);
    tagSupport.setPropValueBind(component, "target", this.target);
    tagSupport.setPropValueBind(component, "title", this.title);
    tagSupport.setPropValueBind(component, "type", this.type);
    tagSupport.setPropValueBind(component, "value", this.value);

  }
}
