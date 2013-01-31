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
import java.io.Serializable;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import com.esri.gpt.framework.util.Val;


/**
 * The Class UIJscript.
 */
public class UIJscript extends UIComponentBase implements StateHolder, 
 Serializable {

// class variables =============================================================
/**
 * The Enum Options.
 */
public static enum Options {

  /** The id value */
  id,
  /** The value. */
  value,
  
  /** The quoted. */
  quoted,
  
  /** The variable name. */
  variableName

}

/** The COMPONEN t_ TYPE. */
public static String COMPONENT_TYPE = "com.esri.gpt.faces.JScript"; 

/** The COMPONEN t_ FAMILY. */
public static String COMPONENT_FAMILY = COMPONENT_TYPE;

// methods =====================================================================
/**
 * Read variable name.
 * 
 * @return the string
 */
public String readVariableName () {
  Object name = this.getAttributes().get(Options.variableName.name());
  if(name != null) {
    return name.toString().trim();
  }
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

/**
 * Read quoted.
 * 
 * @return true, if successful
 */
public boolean readQuoted () {
  Object name = this.getAttributes().get(Options.quoted.name());
  if(name == null) {
    return true;
  }
  if(name instanceof ValueBinding) {
    name = ((ValueBinding) name).getValue(this.getFacesContext());
    if(name == null) {
      return true;
    }
    
  }
  
  return Val.chkBool(name.toString(), true);
}

/**
 * Read value.
 * 
 * @return the string
 */
public String readValue () {
  Object name = this.getAttributes().get(Options.value.name());
  if(name == null) {
    return "";
  }
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

/**
 * Encoding of component.
 * @param context this facesContext
 * @throws IOException If Io Problem
 * 
 */
@Override
public void encodeBegin(FacesContext context) throws IOException {
   ResponseWriter writer = context.getResponseWriter();
   String variableName = readVariableName();
   if(variableName.equals("")) {
     return;
   }
   writer.startElement("script" , null);
   writer.writeAttribute("type", "text/javascript", null);
   writer.writeAttribute("language", "javascript", null);
   writer.write("/* Component id = " + this.getId() + "*/");
   writer.write("var " + variableName + " = ");
   if(readQuoted()) {
     writer.write("\"");
   }
   
   writer.write(this.readValue());
   
   if(readQuoted()) {
     writer.write("\"");
   }
   writer.write(";");
   
}



/**
 * Prints end of encoding.
 * @param context this context
 * @throws IOException Thrown on IO Problem
 * 
 */
@Override
public void encodeEnd(FacesContext context) throws IOException { ResponseWriter writer = context.getResponseWriter();
  writer.endElement("script");
  writer.write("\n");
  
}

/**
 * Family name of this component.
 * @return Family name of this component
 * 
 */
@Override
public String getFamily() {
  return COMPONENT_FAMILY;
}


}

