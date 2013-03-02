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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;


/**
 * The Class UISupport.  Supporting methods for
 */
public class UISupport {

// class variables =============================================================
/** The class logger. */
private static Logger  LOG  = 
  Logger.getLogger(UISupport.class.getCanonicalName());

// methods =====================================================================
/**
 * Creates the action binding.
 * 
 * @param expression the expression
 * 
 * @return the method binding
 */
public MethodBinding createActionBinding(String expression) {
  if(LOG.isLoggable(Level.FINER)) {
    LOG.finer("CreateMethod expression = " + expression);
  }
  return   FacesContext.getCurrentInstance().getApplication()
      .createMethodBinding(expression, null);
}

/**
 * Creates the action listener method binding.
 * 
 * @param expression the expression
 * 
 * @return the method binding
 */
public MethodBinding createActLstnrMethodBinding(String expression) {
  if(LOG.isLoggable(Level.FINER)) {
    LOG.finer("CreateMethod expression = " + expression);
  }
  return FacesContext.getCurrentInstance().getApplication()
      .createMethodBinding(expression, new Class[] { ActionEvent.class });

}

/**
 * Creates the validator binding.
 * 
 * @param expression the expression
 * 
 * @return the method binding
 */
public MethodBinding createValidatorBinding(String expression) {

  return FacesContext.getCurrentInstance().getApplication()
      .createMethodBinding(expression,
          new Class[] { FacesContext.class, UIComponent.class, Object.class });

}

/**
 * Creates the value binding.
 * 
 * @param expression the expression
 * 
 * @return the value binding
 */
public ValueBinding createValueBinding(String expression) {
  if(LOG.isLoggable(Level.FINER)) {
    LOG.finer("CreateValue expression " + expression);
  }
  return FacesContext.getCurrentInstance().getApplication().createValueBinding(
      expression);

}

/**
 * Render children.
 * 
 * @param component the component
 * @param context the context
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 */
@SuppressWarnings("unchecked")
public void renderChildren(UIComponent component, FacesContext context)
    throws IOException {

  Iterator childComps = component.getChildren().iterator();
  while (childComps.hasNext()) {
    renderRecursive((UIComponent) childComps.next(), context);
  }

}

/**
 * Recursive render a component.
 * 
 * @param component the component
 * @param context the context
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 */
@SuppressWarnings("unchecked")
public void renderRecursive(UIComponent component, FacesContext context)
    throws IOException {

  if (!component.isRendered())
    return;

  component.encodeBegin(context);

  if (component.getRendersChildren()) {
    component.encodeChildren(context);

  } else {
    Iterator childComps = component.getChildren().iterator();
    while (childComps.hasNext()) {

      renderRecursive((UIComponent) childComps.next(), context);
    }
  }
  component.encodeEnd(context);

}

/**
 * Gets the bean expression. Generates #{beanName}
 * 
 * @param beanName the bean name
 * 
 * @return the bean expression
 */
public String getBeanExpression(String beanName) {

  if (LOG.isLoggable(Level.FINER)) {

    LOG.finer("Recieved beanName " + beanName);
  }
  if (!beanName.startsWith("{") && !beanName.startsWith("#"))
    beanName = "{" + beanName;
  if (!beanName.startsWith("#"))
    beanName = "#" + beanName;
  if (!beanName.endsWith("}"))
    beanName = beanName + "}";
  if (LOG.isLoggable(Level.FINER)) {
    LOG.finer("Created Bean expression " + beanName);
  }
  return beanName;
}

/**
 * Gets the expression. Generates #{beanName.property}
 * 
 * @param beanName the bean name
 * @param property the property
 * 
 * @return the expression
 */
public String getBeanExpression(String beanName, String property) {

  if (!beanName.startsWith("{") && !beanName.startsWith("#"))
    beanName = "{" + beanName;
  if (!beanName.startsWith("#"))
    beanName = "#" + beanName;
  if (!beanName.endsWith(".") && !beanName.endsWith("}"))
    beanName = beanName + ".";

  beanName += property + "}";

  
  if(LOG.isLoggable(Level.FINER)) {
    LOG.finer("Created Property expression " + beanName);
  }
  return beanName;
}


/**
 * Find component.  The default findComponent in JSF objects do not work 
 * as intended.
 * 
 * @param id the id
 * @param context the context (if null, FacesContext.getCurrentInstance() used)
 * 
 * @return the UI component (null if not found)
 */
@SuppressWarnings("unchecked")
public UIComponent findComponent(FacesContext context, String id) {
  
  if(context == null) {
    context = FacesContext.getCurrentInstance();
  }
  UIComponent component = null;
  if (context != null) {
    Iterator itr = context.getViewRoot().getFacetsAndChildren();
    while (itr.hasNext()) {
      component = findComponent((UIComponent) itr.next(), id);
      if (component != null)
        return component;
    }
  }

  return component;
}

/**
 * Find component.  UIComponent.findComponent method does not work
 * 
 * @param root Component to start at
 * @param id The id to look for
 * 
 * @return the UI component
 */
@SuppressWarnings("unchecked")
public UIComponent findComponent(UIComponent root, String id) {
  
  if(root == null || id == null) {
    return null;
  }
  if (id.equals(root.getId())) {
    return root;
  }
  
  Iterator iter = root.getFacetsAndChildren();
  UIComponent component = null;
  while (iter.hasNext()) {
    Object obj = iter.next();
    if(!(obj instanceof UIComponent)) {
      continue;
    }
    component = (UIComponent) obj;
    component = findComponent(component, id);
    if(component != null) {
      break;
    }
  }
  return component;
}

/**
 * Enclosed in form.
 * 
 * @param comp the comp
 * 
 * @return true, if HtmlForm object is parent of component
 */
public boolean enclosedInForm(UIComponent comp) {

  return this.findEnclosingFormOf(comp) != null;

}

/**
 * Find enclosing form of the component.
 * 
 * @param comp the component to find the enclsing form of
 * 
 * @return the form component (null if form parent not found)
 */
public UIForm findEnclosingFormOf(UIComponent comp) {
	while (comp != null && !(comp instanceof HtmlForm)) {

	    comp = comp.getParent();

	}
	if(comp instanceof UIForm) {
		return (UIForm) comp;
	}
	return null;
	
}


}
