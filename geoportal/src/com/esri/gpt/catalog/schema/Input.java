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
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Super-class for an input component associated with a metadata schema.
 */
public abstract class Input extends UiComponent {

// class variables =============================================================
  
/** Delimited text area input type = "delimitedTextArea" */
public static final String INPUTTYPE_DELIMITEDTEXTAREA = "delimitedTextArea";

/** Interactive map input type = "map" */
public static final String INPUTTYPE_MAP = "map";

/** Select many checkbox input type = "selectManyCheckbox" */
public static final String INPUTTYPE_SELECTMANYCHECKBOX = "selectManyCheckbox";

/** Select one menu input type = "selectOneMenu" */
public static final String INPUTTYPE_SELECTONEMEMU = "selectOneMenu";

/** Select one menu with an "Other" option for inputting text = "selectWithOther" */
public static final String INPUTTYPE_SELECTWITHOTHER = "selectWithOther";

/** Text input type = "text" */
public static final String INPUTTYPE_TEXT = "text";

/** Text area input type = "textArea" */
public static final String INPUTTYPE_TEXTAREA = "textArea";

/** Text array input type = "textArray" */
public static final String INPUTTYPE_TEXTARRAY = "textArray";

/** Unknown input type = "unknown" */
public static final String INPUTTYPE_UNKNOWN = "unknown";
  
// instance variables ==========================================================
private String  _defaultValue = "";
private boolean _editable = true;
private String  _facesId = "";
private String  _hintMode = "inline";
private String  _hintResourceKey = "";
private String  _onChange = "";
private String  _onClick = "";
private String  _testResourceKey = "";
  
// constructors ================================================================

/** Default constructor. */
public Input() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Input(Input objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setDefaultValue(objectToDuplicate.getDefaultValue());
    setEditable(objectToDuplicate.getEditable());
    setFacesId(objectToDuplicate.getFacesId());
    setHintResourceKey(objectToDuplicate.getHintResourceKey());
    setHintMode(objectToDuplicate.getHintMode());
    setTestResourceKey(objectToDuplicate.getTestResourceKey());
    setOnChange(objectToDuplicate.getOnChange());
    setOnClick(objectToDuplicate.getOnClick());
  }
}

// properties ==================================================================

/**
 * Gets the default value.
 * @return the default value
 */
public String getDefaultValue() {
  return _defaultValue;
}
/**
 * Sets the default value.
 * <br/>The value is trimmed.
 * <br/>Null values are treated a empty strings.
 * @param value the default value
 */
public void setDefaultValue(String value) {
  _defaultValue = Val.chkStr(value);
}

/**
 * Gets the editable status.
 * @return true if this input component is editable
 */
public boolean getEditable() {
  return _editable;
}
/**
 * Sets the editable status.
 * @param editable true if this input component is editable
 */
public void setEditable(boolean editable) {
  _editable = editable;
}

/**
 * Gets the Faces ID for the component.
 * @return the Faces ID
 */
public String getFacesId() {
  return _facesId;
}
/**
 * Sets the Faces ID for the component.
 * <br/> The '.' character will be replaced with the '.' character.
 * @param id Faces ID
 */
public void setFacesId(String id) {
  _facesId = Val.chkStr(id);
  _facesId = _facesId.replace('.','_');
}

/**
 * Gets the mode for an optional hint ("inline","tip","toggle").
 * @return the hint mode
 */
public String getHintMode() {
  return _hintMode;
}
/**
 * Sets the mode for an optional hint ("inline","tip","toggle").
 * associated with the component.
 * @param mode the hint mode
 */
public void setHintMode(String mode) {
  mode = Val.chkStr(mode).toLowerCase();
  if (mode.equals("inline") || mode.equals("tip") || mode.equals("toggle")) {
    _hintMode = mode;
  } else {
    _hintMode = "inline";
  }
 
}

/**
 * Gets the UI property bundle resource key for a hint 
 * associated with the component.
 * @return the hint resource key
 */
public String getHintResourceKey() {
  return _hintResourceKey;
}
/**
 * Sets the UI property bundle resource key for a hint 
 * associated with the component.
 * <br/>The key will be trimmed. 
 * <br/>A null key is treated as an empty string.
 * @param key the hint resource key
 */
public void setHintResourceKey(String key) {
  _hintResourceKey = Val.chkStr(key);
}

/**
 * Gets the Javascript associated with the onchange event.
 * @return the Javascript
 */
public String getOnChange() {
  return _onChange;
}

/**
 * Sets the Javascript associated with the onclick event.
 * @param js the Javascript
 */
public void setOnChange(String js) {
  _onChange =  Val.chkStr(js);
}

/**
 * Gets the Javascript associated with the onclick event.
 * @return the Javascript
 */
public String getOnClick() {
  return _onClick;
}

/**
 * Sets the Javascript associated with the onclick event.
 * @param js the Javascript
 */
public void setOnClick(String js) {
  _onClick =  Val.chkStr(js);
}

/**
 * Gets the UI property bundle resource key for a test link button
 * associated with the component.
 * @return the hint resource key
 */
public String getTestResourceKey() {
  return _testResourceKey;
}

/**
 * Sets the UI property bundle resource key for a test link button 
 * associated with the component.
 * <br/>The key will be trimmed. 
 * <br/>A null key is treated as an empty string.
 * @param key the hint resource key
 */
public void setTestResourceKey(String key) {
  _testResourceKey =  Val.chkStr(key);
}


// methods =====================================================================

/**
 * Applies a hint to an input component if a hint resource key was configured.
 * <p/>
 * If a hintResourceKey was configured, the supplied inputComponent and an 
 * HtmlOutputText representing the hint will be wrapped within an 
 * HtmlPanelGroup and the HtmlPanelGroup will be returned.
 * <p/>
 * If a hintResourceKey was not configured, the supplied inputComponent will
 * be returned.
 * @param context the UI context
 * @param inputComponent the subject input component
 * @return the resultant UI component
 */
protected UIComponent applyHint(UiContext context, 
                                UIComponent inputComponent) {
  if ((inputComponent != null) && (getHintResourceKey().length() > 0)) {
    MessageBroker msgBroker = context.extractMessageBroker();
    String sHint = msgBroker.retrieveMessage(getHintResourceKey());
    
    if (this.getHintMode().equalsIgnoreCase("tip")) {
    
      // use reflection to set the tip (i.e. the HTML "title" attribute)
      if ((sHint != null) && (sHint.length() > 0)) {
        try {
          java.lang.reflect.Method method;
          Class[] sig = {java.lang.String.class};
          Object[] args = {sHint};
          method = inputComponent.getClass().getDeclaredMethod("setTitle",sig);
          if (method != null) {
            method.invoke(inputComponent,args);
          }
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
      }
      
    } else if (this.getHintMode().equalsIgnoreCase("toggle")) {
      
      // toggled hint (icon to open/close an element containing the hint)
      
      HtmlPanelGroup panel = new HtmlPanelGroup();
      panel.getChildren().add(inputComponent);
      
      HtmlOutputText space = new HtmlOutputText();
      space.setEscape(false);
      space.setValue("&nbsp;");
      panel.getChildren().add(space);
      
      HtmlGraphicImage img = new HtmlGraphicImage();
      img.setStyleClass("hintIcon");
      img.setUrl("/catalog/images/hint.gif");
      img.setOnclick("mdeToggleHint(this);");
      panel.getChildren().add(img);
      
      HtmlPanelGroup hintSection = new HtmlPanelGroup();
      hintSection.setStyle("display:none;");
      hintSection.setStyleClass("hintSection");
      HtmlOutputText outText = new HtmlOutputText();
      outText.setStyleClass("hint");
      outText.setEscape(false);
      outText.setValue(sHint);
      hintSection.getChildren().add(outText);
      panel.getChildren().add(hintSection);
      
      return panel;
      
    } else {
      
      // inline hint (original component + output text wrapped in a panel)
      HtmlPanelGroup panel = new HtmlPanelGroup();
      panel.getChildren().add(inputComponent);
      HtmlOutputText outText = new HtmlOutputText();
      outText.setStyleClass("hint");
      outText.setEscape(false);
      outText.setValue(" "+sHint);
      panel.getChildren().add(outText);
      return panel;
      
    }
    
  }
  return inputComponent;
}

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>defaultValue editable hintResourceKey hintMode onchange onclick testResourceKey
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setDefaultValue(DomUtil.getAttributeValue(attributes,"defaultValue"));
  setEditable(Val.chkBool(DomUtil.getAttributeValue(attributes,"editable"),true));
  setHintResourceKey(DomUtil.getAttributeValue(attributes,"hintResourceKey"));
  setHintMode(DomUtil.getAttributeValue(attributes,"hintMode"));
  setTestResourceKey(DomUtil.getAttributeValue(attributes,"testResourceKey"));
  setOnChange(DomUtil.getAttributeValue(attributes,"onchange"));
  setOnClick(DomUtil.getAttributeValue(attributes,"onclick"));
}

/**
 * Produces a deep clone of the object.
 * <p/>
 * The typical approach is to invoke a duplication constructor.
 * <br/>Example: return new InputText(this);
 */
public abstract Input duplicate();

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
@Override
public void echo(StringBuffer sb) {
  super.echo(sb);
  sb.append(" defaultValue=\"").append(getDefaultValue()).append("\"");
  sb.append(" editable=\"").append(getEditable()).append("\"");
  if (getHintResourceKey().length() > 0) {
    sb.append(" hintResourceKey=\"").append(getHintResourceKey()).append("\"");
    sb.append(" hintMode=\"").append(getHintMode()).append("\"");
  }
  if (getTestResourceKey().length() > 0) {
    sb.append(" testResourceKey=\"").append(getTestResourceKey()).append("\"");
  }
  if (getOnChange().length() > 0) {
    sb.append(" onchange=\"").append(getOnChange()).append("\"");
  }
  if (getOnClick().length() > 0) {
    sb.append(" onclick=\"").append(getOnClick()).append("\"");
  }
}

/**
 * Evaluates the default value for the input component.
 * <p/>
 * @param context the UI context
 * @param currentValue the current value of the component
 * @return the default or current value
 */
protected String evaluateDefault(UiContext context, String currentValue) {
  String sValue = Val.chkStr(currentValue);
  if ((sValue.length() == 0) && context.getIsCreateDocument()) {
    sValue = getDefaultValue();
      
    // evaluate a Faces binding, ignore exceptions
    if ((sValue.length() > 3) && (sValue.indexOf("#{") != -1)) {
      String sExpr = sValue;
      sValue = "";
      try {
        FacesContext fc = context.getFacesContext();
        ValueBinding vb = fc.getApplication().createValueBinding(sExpr);
        Object obj = vb.getValue(fc);
        if (obj != null) {
          sValue = Val.chkStr(obj.toString());
        }
      } catch (Exception e) {
        sValue = "";
        if (LogUtil.getLogger().isLoggable(Level.FINER)) {
          String sMsg = "Expression failed to bind:\n"+sExpr+"\n"+e.toString();
          LogUtil.getLogger().finer(sMsg);
        }
      }
    }
  }
  return sValue;
}

/**
 * Finds the associated Faces UIInput component. 
 * <p/>
 * The Faces UIInput component is located using the supplied editorForm
 * and the Faces ID for this Input instance, example: <br/>
 * editorForm.findComponent(getFacesId());
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @throws SchemaException if the associated Faces UIInput component cannot be located
 */
protected UIInput findInputComponent(UiContext context, UIComponent editorForm) 
  throws SchemaException {
  UIComponent component = editorForm.findComponent(getFacesId());
  if ((component != null) && (component instanceof UIInput)) {
    return (UIInput)component;
  } else {
    String sMsg = "The Faces UIInput component cannot be located for: ";
    throw new SchemaException(sMsg+getFacesId());
  }
}

/**
 * Formats a value associated with a parameter.
 * <p/>
 * Currently, only Date type values are formatted.
 * @param parameter the associated parameter
 * @param value the value to format
 */
protected String formatValue(Parameter parameter, String value) {
  return parameter.getContent().formatValue(parameter,value);
}

/**
 * Returns the String value for a Faces UIInput component.
 * <p/>
 * If the associated value is null or not a String, 
 * an empty String is returned.
 * @param input the Faces input component whose value will be returned
 */
protected String getInputValue(UIInput input) {
  Object oValue = input.getValue();
  if ((oValue != null) && (oValue instanceof String)) {
    return (String)oValue;
  } else {
    return "";
  }
}


/**
 * Makes the Faces UI input component for a parameter.
 * <p/>
 * It is the responsibility of the sub-class to instantiate the
 * Faces UIComponent, set the id and bind to a parameter value. 
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI input component
 */
public abstract UIComponent makeInputComponent(UiContext context,
                                               Section section,
                                               Parameter parameter);

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
  MessageBroker msgBroker = context.extractMessageBroker();
  String sValue = parameter.getContent().makeDisplayValue(msgBroker,parameter);
  if (sValue.length() > 0) {
    HtmlOutputText component = new HtmlOutputText();
    component.setId(getFacesId()); 
    component.setValue(sValue);
    
    // handle a thumbnail
    if (parameter.getMeaningType().equalsIgnoreCase(Meaning.MEANINGTYPE_THUMBNAIL_URL)) {
      HtmlPanelGroup panel = new HtmlPanelGroup();
      panel.getChildren().add(component);
      panel.getChildren().add(makeBR());
      HtmlGraphicImage img = new HtmlGraphicImage();
      img.setUrl(sValue);
      img.setStyle("border: 1px solid silver");
      panel.getChildren().add(img);
      return panel;
      
    } else {
      
      // activate URL links (make sure to escape if turning JSF escaping off)
      if (sValue.toLowerCase().indexOf("http") != -1) {
        String exp = "(?<![=\"'\\/>;])http(s)?://([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\!\\@\\#\\$\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]*)?";
        Pattern pattern = Pattern.compile(exp,Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(Val.escapeXmlForBrowser(sValue));
        if (matcher.find()) {
          String sReplaced = matcher.replaceAll("<a href=\"$0\" target=\"_blank\">$0</a>");
          component.setValue(sReplaced);
          component.setEscape(false);
        }
      }
      
      // FTP - activate URL links (make sure to escape if turning JSF escaping off)
      if (sValue.toLowerCase().indexOf("ftp") != -1) {
        String exp = "(?<![=\"'\\/>;])ftp(s)?://([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\!\\@\\#\\$\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]*)?";
        Pattern pattern = Pattern.compile(exp,Pattern.DOTALL | Pattern.UNIX_LINES | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(Val.escapeXmlForBrowser(sValue));
        if (matcher.find()) {
          String sReplaced = matcher.replaceAll("<a href=\"$0\" target=\"_blank\">$0</a>");
          component.setValue(sReplaced);
          component.setEscape(false);
        }
      }
      
    }
    return component;
  }
  return null;
}

/**
 * Sets the value for an input component.
 * <p/>
 * The value set is based upon the supplied parameter's singleValue.
 * <p/>
 * Default values are evaluated if applicable.
 * Date type values are formatted.
 * @param context the UI context
 * @param input the Faces input component
 * @param parameter the associated parameter
 */
public void setComponentValue(UiContext context,
                              UIInput input, 
                              Parameter parameter) {
  String sValue = parameter.getContent().getSingleValue().getValue();
  sValue = evaluateDefault(context,sValue);
  sValue = formatValue(parameter,sValue);
  input.setValue(sValue);
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * The intent is to give input objects an opportunity to propagate 
 * local component value(s) back into the parameter's value(s).
 * <p/>
 * One example is the propagation of the input HtmlInputText value into
 * the parameter's singleValue for an InputText component.
 * <p/>
 * There is no default behavior.
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @param parameter the associated parameter
 * @throws SchemaException if an associated Faces UIComponent cannot be located
 */
public void unBind(UiContext context, 
                   UIComponent editorForm,
                   Parameter parameter) 
  throws SchemaException {
  // no default behavior
}

}
