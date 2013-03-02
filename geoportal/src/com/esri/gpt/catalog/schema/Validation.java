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
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Validation component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * &lt;validation required="true"/&gt;
 */
public class Validation extends Component {

// class variables =============================================================
   
/** Date = "date" */
public static final String VALUETYPE_DATE = "date";

/** Double = "double" */
public static final String VALUETYPE_DOUBLE = "double";

/** Email address = "email" */
public static final String VALUETYPE_EMAIL = "email";

/** Integer = "integer" */
public static final String VALUETYPE_INTEGER = "integer";

/** String = "string" (this is the default) */
public static final String VALUETYPE_STRING = "string";
  
// instance variables ==========================================================
private boolean _required = false;
private String  _valueType = Validation.VALUETYPE_STRING;
private String  _nilReasonValue = "";
  
// constructors ================================================================

/** Default constructor. */
public Validation() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Validation(Validation objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    this.setRequired(objectToDuplicate.getRequired());
    this.setValueType(objectToDuplicate.getValueType());
    this.setNilReasonValue(objectToDuplicate.getNilReasonValue());
  }
}

// properties ==================================================================

/**
 * Gets the associated ISO nil-reason value (if any).
 * @return the nil-reason value
 */
public String getNilReasonValue() {
  return _nilReasonValue;
}
/**
 * Sets the associated ISO nil-reason value (if any).
 * @param reason the nil-reason value
 */
public void setNilReasonValue(String reason) {
  _nilReasonValue = Val.chkStr(reason);
}

/**
 * Gets the required status.
 * @return true if the parameter is required
 */
public boolean getRequired() {
  return _required;
}
/**
 * Sets the required status.
 * @param required true if the parameter is required
 */
public void setRequired(boolean required) {
  _required = required;
}

/**
 * Gets the value type.
 * @return the value type
 */
public String getValueType() {
  return _valueType;
}
/**
 * Sets the value type.
 * @param type the value type
 */
public void setValueType(String type) {
  type = Val.chkStr(type);
  if (type.equals("")) {
    _valueType = Validation.VALUETYPE_STRING;
  } else if (type.equalsIgnoreCase(Validation.VALUETYPE_DATE)) {
    _valueType = Validation.VALUETYPE_DATE;
  } else if (type.equalsIgnoreCase(Validation.VALUETYPE_DOUBLE)) {
    _valueType = Validation.VALUETYPE_DOUBLE;
  } else if (type.equalsIgnoreCase(Validation.VALUETYPE_EMAIL)) {
    _valueType = Validation.VALUETYPE_EMAIL;
  } else if (type.equalsIgnoreCase(Validation.VALUETYPE_INTEGER)) {
    _valueType = Validation.VALUETYPE_INTEGER;
  } else if (type.equalsIgnoreCase(Validation.VALUETYPE_STRING)) {
    _valueType = Validation.VALUETYPE_STRING;
  } else {
    _valueType = Validation.VALUETYPE_STRING;
  }
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>required valueType
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setRequired(Val.chkBool(DomUtil.getAttributeValue(attributes,"required"),false));
  setValueType(DomUtil.getAttributeValue(attributes,"valueType"));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Validation(this);
 */
public Validation duplicate() {
  return new Validation(this);
}

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
@Override
public void echo(StringBuffer sb) {
  super.echo(sb);
  sb.append(" required=\"").append(getRequired()).append("\"");
  sb.append(" valueType=\"").append(getValueType()).append("\"");
  if ((this.getNilReasonValue() != null) && (this.getNilReasonValue().length() > 0)) {
    sb.append(" nilReasonValue=\"").append(getNilReasonValue()).append("\"");
  }
}

/**
 * Executes the validation of a parameter
 * <p/>
 * Encountered errors should be appended to the 
 * schema.getValidationErrors() collection.
 * <p/>
 * Parameter values associated with meaning should be used to populate
 * schema.getMeaning() values.
 * @param schema the schema being validated
 * @param section the section being validated
 * @param parameter the parameter being validated
 */
public void validate(Schema schema, Section section, Parameter parameter) {
  
  ValidationErrors errors = schema.getValidationErrors();
  boolean bIsEmpty = parameter.getContent().isValueEmpty();
  boolean bIsSingleValue = parameter.getContent().isSingleValue();
  
  if (getRequired() && bIsEmpty) {
    if ((this.getNilReasonValue() == null) || (this.getNilReasonValue().length() == 0)) {
      errors.add(new ValidationError(section,parameter,ValidationError.REASONCODE_PARAMETER_ISREQUIRED));
    }
    
  } else if (!bIsEmpty && bIsSingleValue) {
    String sValue = parameter.getContent().getSingleValue().getValue();
    String sValueType = getValueType();
      
    // check a Date type
    if (sValueType.equals(Validation.VALUETYPE_DATE)) {
      DateProxy dp = new DateProxy();
      dp.setDate(sValue);
      if (!dp.getIsValid()) {
        errors.addInvalidParameter(section,parameter);
      }
      
    // check a Double type
    } else if (sValueType.equals(Validation.VALUETYPE_DOUBLE)) {
      try {
        Double.valueOf(sValue);
      } catch (NumberFormatException e) {
        errors.addInvalidParameter(section,parameter);
      }
      
    // check an Email address type
    } else if (sValueType.equals(Validation.VALUETYPE_EMAIL)) {
      if (!Val.chkEmail(sValue)) {
        errors.addInvalidParameter(section,parameter);
      }
      
    // check an Integer type
    } else if (sValueType.equals(Validation.VALUETYPE_INTEGER)) {
      try {
        Integer.valueOf(sValue);
      } catch (NumberFormatException e) {
        errors.addInvalidParameter(section,parameter);
      }
    }         
  } 
    
}

}
