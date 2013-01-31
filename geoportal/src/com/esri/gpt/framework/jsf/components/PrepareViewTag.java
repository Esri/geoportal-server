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

/**
 * Initializes view of the page.
 * Calls actionListener to initialize page and updates error/success messages.
 * <p/>
 * Tag has only one attribute: <i>actionListener</i> which is a required 
 * attribute.
 * <p/>
 * Example of tag usage is as follows:<br/>
 * <br/>
 * <blockquote><pre>
 * &lt;gpt:pageViewInit actionListener="#{<i>Controller.method</i>}"/>
 * </pre></blockquote>
 * @see UIPrepareView
 */
public class PrepareViewTag extends UIComponentTag {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================
/**
 * Gets component type.
 * @return component type ({@link UIPrepareView#COMPONENT_TYPE})
 */
@Override
public String getComponentType() {
  return UIPrepareView.COMPONENT_TYPE;
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
 * Releases tag.
 */
@Override
public void release() {
  super.release();
}

/**
 * Sets properties.
 * @param component component
 */
@Override
protected void setProperties(UIComponent component) {
  super.setProperties(component);
}
}
