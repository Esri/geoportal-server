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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;

/**
 * The Class UITableCommandLink. Work around Component should be used instead of 
 * CommandLink in a dataTable component.  THis is so that the generated
 * links can be linked to an action or actionListener.
 */
public class UITableCommandLink extends HtmlCommandLink {

// class variables =============================================================
/** The JSF Component type. */
public static final String COMPONENT_TYPE 
= "com.esri.gpt.faces.TableCommandLink"; 

/** The JSF Component family. */
public static final String COMPONENT_FAMILY = COMPONENT_TYPE;

/** The request map. */
private static final String REQUESTMAP_KEY = COMPONENT_TYPE;

/** class logger. */
private static Logger LOG = 
  Logger.getLogger(UITableCommandLink.class.getCanonicalName());

// instance variables ==========================================================
/** The support. */
UISupport uiSupport = new UISupport();

/** The this indexed link. */
HtmlCommandLink indexedLink;

/** The attribute map. */
@SuppressWarnings("unchecked")
Map attributeMap;

// methods =====================================================================

/* (non-Javadoc)
 * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
 */
@SuppressWarnings("unchecked")
@Override
public void encodeBegin(FacesContext context) throws IOException {
	LOG.fine("Encoding UITableCommandLink " + this);
	// Initializing link that will be written out in table
	this.indexedLink = this.getIndexedCommandLink(context);
		
	// Getting the form enclosing this link
	UIForm enclosingForm = this.uiSupport.findEnclosingFormOf(this);
	if(enclosingForm == null) {
		throw new FacesException("Component " + 
				this.getId() + "not enclosed in form");
	}
	
	
	
	
	
	// Checking if thisLink.id already exists, if it does, we erase it
  UIComponent compExists 
      = this.uiSupport.findComponent(enclosingForm, indexedLink.getId());
  if(compExists != null) {
    UIForm form = new UIForm();
    form.setId(enclosingForm.getId());
    form.getChildren().add(this.indexedLink);
    	
  } else {

    enclosingForm.getChildren().add(indexedLink);
  }
	// encoding half of thisLink
	this.indexedLink.encodeBegin(context);
	
}

/**
 * Gets the id.
 * 
 * @param form the form
 * @param context the context
 * 
 * @return the id
 */
@SuppressWarnings("unchecked")
private String getId(FacesContext context) {
	
	Map map = context.getExternalContext().getRequestMap();
	
	String key = REQUESTMAP_KEY + ".lastIndex.linkId"+ this.getId();
	Object value = map.get(key);
	int index = 0;
	if(value == null || !(value instanceof Integer)) {
		index = 0;
	} else {
		index = ((Integer) value) + 1;
	}
	map.put(key, index);
	
	return "_" + this.getId() + "_" + index;
	
}


/* (non-Javadoc)
 * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
 */
@Override
public void encodeEnd(FacesContext context) throws IOException {
	this.indexedLink.encodeEnd(context);
}

/** 
 * Gets the attributemap of this Component
 * @return map of the attributes for this component
 */
@SuppressWarnings("unchecked")
@Override
public Map getAttributes() {
	if(this.attributeMap == null) {
		this.attributeMap = super.getAttributes();
	}
	if(this.attributeMap == null) {
		this.attributeMap = new LinkedHashMap();
	}
	return this.attributeMap;
}



/**
 * Gets the indexed command link.  Properties are transfered from this
 * to the new HtmlCommandLink.
 * 
 * @param context the context
 * 
 * @return the indexed command link
 */
@SuppressWarnings("unchecked")
public HtmlCommandLink getIndexedCommandLink(FacesContext context) {
	
	// copy properties
	HtmlCommandLink thisLink = new HtmlCommandLink();
	thisLink.setId(this.getId(context));
	thisLink.setAccesskey(this.getAccesskey());
	thisLink.setActionListener(this.getActionListener());
	thisLink.setAction(this.getAction());
	thisLink.setCharset(this.getCharset());
	thisLink.setCoords(this.getCoords());
	thisLink.setDir(this.getDir());
	thisLink.setHreflang(this.getHreflang());
	thisLink.setImmediate(this.isImmediate());
	thisLink.setLang(this.getLang());
	thisLink.setOnblur(this.getOnblur());
	thisLink.setOnclick(this.getOnclick());
	thisLink.setOndblclick(this.getOndblclick());
	thisLink.setOnfocus(this.getOnfocus());
	thisLink.setOnkeydown(this.getOnkeydown());
	thisLink.setOnkeypress(this.getOnkeypress());
	thisLink.setOnkeyup(this.getOnkeyup());
	thisLink.setOnmousedown(this.getOnmousedown());
	thisLink.setOnmousemove(this.getOnmousemove());
	thisLink.setOnmouseout(this.getOnmouseout());
	thisLink.setOnmouseover(this.getOnmouseover());
	thisLink.setOnmouseup(this.getOnmouseup());
	thisLink.setRel(this.getRel());
	thisLink.setRendered(this.isRendered());
	thisLink.setRendererType(this.getRendererType());
	thisLink.setRev(this.getRev());
	thisLink.setShape(this.getShape());
	thisLink.setStyle(this.getStyle());
	thisLink.setStyleClass(this.getStyleClass());
	thisLink.setTabindex(this.getTabindex());
	thisLink.setTarget(this.getTarget());
	thisLink.setTitle(this.getTitle());
	thisLink.setType(this.getType());
	thisLink.setValue(this.getValue());
		
	// attributes
	Map thisLinkAttribs = thisLink.getAttributes();
	Map thisAttribs = this.getAttributes();
	Set keys = thisAttribs.keySet();
	for(Object key : keys) {
		  thisLinkAttribs.put(key, thisAttribs.get(key));
	}
	
	// parameters
	List children = this.getChildren();
	UIParameter uiParam = null; 
	UIParameter uiChildParam = null;
	for(Object objChild : children) {
		if(objChild instanceof UIParameter) {
			uiParam = (UIParameter) objChild;
			uiChildParam = new UIParameter();
			uiChildParam.setId(this.getId(context));
			uiChildParam.setName(uiParam.getName());
			uiChildParam.setValue(uiParam.getValue());
			thisLink.getChildren().add(uiChildParam);
		}
	}
	return thisLink;
}


}
