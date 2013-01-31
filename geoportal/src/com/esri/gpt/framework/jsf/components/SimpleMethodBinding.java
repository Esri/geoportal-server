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

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;


// TODO: Auto-generated Javadoc
/**
 * The Class SimpleMethodBinding.  Can be used instead of method binding
 * for actions when we need to return just a plain String.
 */
@SuppressWarnings("serial")
public class SimpleMethodBinding extends MethodBinding implements StateHolder {

// instance variables ==========================================================
/** The return string. */
private String returnString = null;

/** The transient holder. */
private boolean transientHolder; 

// constructor =================================================================
/**
 * Instantiates a new simple method binding.
 * 
 * @param returnString the return string
 */
public SimpleMethodBinding(String returnString) {
  super();
  this.setReturnString(returnString);
}

/**
 * Gets the return string.
 * 
 * @return the return string
 */
private String getReturnString() {
  return returnString;
}

// properties ==================================================================
/**
 * Sets the return string.
 * 
 * @param returnString the new return string
 */
private void setReturnString(String returnString) {
  this.returnString = returnString;
}

// methods =====================================================================
/**
 * Returns String.class
 * 
 * @param arg0 the arg0
 * 
 * @return String.class
 * 
 * @throws MethodNotFoundException the method not found exception
 */
@SuppressWarnings("unchecked")
@Override
public Class getType(FacesContext arg0) throws MethodNotFoundException {

  return String.class;
}

/**
 * Returns the returnString.
 * 
 * @param arg0 the arg0
 * @param arg1 the arg1
 * 
 * @return the return string
 * 
 * @throws EvaluationException the evaluation exception
 * @throws MethodNotFoundException the method not found exception
 */

@Override
public Object invoke(FacesContext arg0, Object[] arg1)
throws EvaluationException, MethodNotFoundException {
 
  return this.getReturnString();
}

/**
 * @return whether is transient
 * 
 */

public boolean isTransient() {
  return this.transientHolder;
}

/**
 * Restoring State 
 * @param facescontext
 * @param obj
 * 
 */

public void restoreState(FacesContext facescontext, Object obj) {
  if(obj != null) {
    this.setReturnString(obj.toString());
  }
}

/**
 * Save state.
 * 
 * @param facescontext the facescontext
 * 
 * @return the object
 */

@SuppressWarnings("unchecked")
public Object saveState(FacesContext facescontext) {
  return this.getReturnString();
}

/**
 * Setting transient
 * @param flag
 * 
 */

public void setTransient(boolean flag) {
  this.transientHolder = flag;
  
}

}


