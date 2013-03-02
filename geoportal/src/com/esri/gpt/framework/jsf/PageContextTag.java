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
package com.esri.gpt.framework.jsf;

import com.esri.gpt.framework.util.Val;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspException;

/**
 * Provides <code>&lt;gpt:page/&gt;</code> context tag functionality.
 * <p/>
 * Tag <code>&lt;gpt:page/&gt;</code> accepts the followingattributes:
 * <ul>
 * <li>id [required] - key of the page,</li>
 * <li>captionkey - resource id of the page caption,</li>
 * <li>caption - page caption (overrides caption sets by caption key),</li>
 * <li>help - page id for help sub-system.</li>
 * </ul>
 * This tag has to be element of each page. To use it, insert the following two 
 * lines into the .jsp page definition:<br/><br/>
 * <code>&lt;%@taglib uri="http://www.esri.com/gpt" prefix="gpt" %&gt;</code><br/>
 * <code>&lt;gpt:page key="catalog.identity.login" caption="Login" help="login-help.html"/&gt;</code><br/><br/>
 */
public class PageContextTag extends SimpleTagSupport {

private String _id = "";
private String _captionkey = "";
private String _caption = "";
private String _prepareView = "";
private String _tabId = "";

/**
 * Called by the container to invoke this tag. 
 * The implementation of this method is provided by the tag library developer,
 * and handles all tag processing, body iteration, etc.
 */
@Override
public void doTag() throws JspException {
  PageContext pc = PageContext.extract();
  pc.setPageId(_id);
  if (_captionkey.length() > 0) {
    pc.setCaptionResourceKey(_captionkey);
  } else {
    pc.setCaptionResourceKey(_id + ".caption");
  }
  if (_caption.length() > 0) {
    pc.setCaption(_caption);
  }
  if (_tabId.length() > 0) {
    pc.setTabId(_tabId);
  }
  pc.setPrepareView(getPrepareView());
}

/**
 * Sets 'key' attribute.
 * @param key key attribute value.
 */
public void setId(String key) {
  _id = evaluateValue(key);
}

/**
 * Sets 'captionkey' attribute.
 * @param captionkey captionkey attribute value.
 */
public void setCaptionkey(String captionkey) {
  _captionkey = evaluateValue(captionkey);
}

/**
 * Sets 'caption' attribute.
 * @param caption caption attribute value.
 */
public void setCaption(String caption) {
  _caption = evaluateValue(caption);
}

/**
 * Sets tab id.
 * @param tabId tab id
 */
public void setTabId(String tabId) {
  _tabId = evaluateValue(tabId);
}


/**
 * Evaluates value.
 * <p/>
 * Checks if the value is an EL expression. If so evaluates it.
 * @param value value to evaluate.
 * @return evaluated value.
 */
private String evaluateValue(String value) {
  value = Val.chkStr(value);
  if (UIComponentTag.isValueReference(value)) {
    FacesContext fc = FacesContext.getCurrentInstance();
    ValueBinding vb = fc.getApplication().createValueBinding(value);
    return vb.getValue(fc).toString();
  } else {
    return Val.chkStr(value);
  }
}

/**
 * Gets expression used to prepare view.
 * @return expression used to prepare view
 */
public String getPrepareView() {
  return _prepareView;
}

/**
 * Sets expression used to prepare view.
 * @param prepareView expression used to prepare view
 */
public void setPrepareView(String prepareView) {
  _prepareView = Val.chkStr(prepareView);
}
}
