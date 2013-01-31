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
import com.esri.gpt.framework.util.Val;

/**
 * Describes an error that occurred during validation.
 */
public class ValidationError {

// class variables =============================================================
  
/** Reason code envelope is invalid = "envelopeIsInvalid" */
public static final String REASONCODE_ENVELOPE_ISINVALID = "envelopeIsInvalid";
  
/** Reason code parameter is invalid = "parameterIsInvalid" */
public static final String REASONCODE_PARAMETER_ISINVALID = "parameterIsInvalid";
  
/** Reason code parameter is required = "parameterIsRequired" */
public static final String REASONCODE_PARAMETER_ISREQUIRED = "parameterIsRequired";

/** Reason code Schematron exception = "schematronException" */
public static final String REASONCODE_SCHEMATRON_EXCEPTION = "schematronException";

/** Reason code Schematron rule violation = "schematronViolation" */
public static final String REASONCODE_SCHEMATRON_VIOLATION = "schematronViolation";

/** Reason code title is required = "titleIsRequired" */
public static final String REASONCODE_TITLE_ISREQUIRED = "titleIsRequired";

/** Reason code XML document is invalid = "xmlIsInvalid" */
public static final String REASONCODE_XML_ISINVALID = "xmlIsInvalid";

/** Reason code XSD reference is invalid = "xsdIsInvalid" */
public static final String REASONCODE_XSD_ISINVALID = "xsdIsInvalid";

/** Reason code XSD rule violation = "xsdViolation" */
public static final String REASONCODE_XSD_VIOLATION = "xsdViolation";
  
// instance variables ==========================================================
protected String  location;

private String    _message = "";
private Parameter _parameter;
private String    _reasonCode = ValidationError.REASONCODE_PARAMETER_ISINVALID;
private Section   _section;
  
// constructors ================================================================

/** Default constructor. */
public ValidationError() {}

/**
 * Constructs with a supplied section, the parameter that 
 * failed and a reason code
 * @param section the containing section
 * @param parameter the parameter that failed
 * @param reasonCode the reason code
 */
public ValidationError(Section section, 
                       Parameter parameter,
                       String reasonCode) {
  setSection(section);
  setParameter(parameter);
  setReasonCode(reasonCode);
}

// properties ==================================================================

/**
 * Gets the message.
 * @return the message
 */
public String getMessage() {
  return _message;
}
/**
 * Sets the message.
 * @param message the message
 */
public void setMessage(String message) {
  _message = Val.chkStr(message);
}

/**
 * Gets the parameter that failed to validate.
 * @return the parameter that failed
 */
public Parameter getParameter() {
  return _parameter;
}
/**
 * Sets the parameter that failed to validate.
 * @param parameter the parameter that failed
 */
public void setParameter(Parameter parameter) {
  _parameter = parameter;
}

/**
 * Gets the parameter resource key.
 * @return the parameter resource key
 */
public String getParameterResourceKey() {
  if ((getParameter() != null) && (getParameter().getLabel() != null)) {
    return getParameter().getLabel().getResourceKey();
  } else {
    return "";
  }
}

/**
 * Gets the parameter XPath selection expression
 * @return the parameter XPath selection expression
 */
public String getParameterXPath() {
  if (getParameter() != null) {
    return getParameter().getContent().getSelect();
  } else {
    return "";
  }
}

/**
 * Gets the reason code.
 * @return the reason code
 */
public String getReasonCode() {
  return _reasonCode;
}
/**
 * Sets the reason code.
 * @param reasonCode the reason code
 */
public void setReasonCode(String reasonCode) {
  reasonCode = Val.chkStr(reasonCode);
  if (reasonCode.equals("")) {
    _reasonCode = ValidationError.REASONCODE_PARAMETER_ISINVALID;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_ENVELOPE_ISINVALID)) {
    _reasonCode = ValidationError.REASONCODE_ENVELOPE_ISINVALID;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_PARAMETER_ISREQUIRED)) {
    _reasonCode = ValidationError.REASONCODE_PARAMETER_ISREQUIRED;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_PARAMETER_ISINVALID)) {
    _reasonCode = ValidationError.REASONCODE_PARAMETER_ISINVALID;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_SCHEMATRON_EXCEPTION)) {
    _reasonCode = ValidationError.REASONCODE_SCHEMATRON_EXCEPTION;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_SCHEMATRON_VIOLATION)) {
    _reasonCode = ValidationError.REASONCODE_SCHEMATRON_VIOLATION;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_TITLE_ISREQUIRED)) {
    _reasonCode = ValidationError.REASONCODE_TITLE_ISREQUIRED;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_XML_ISINVALID)) {
    _reasonCode = ValidationError.REASONCODE_XML_ISINVALID;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_XSD_ISINVALID)) {
    _reasonCode = ValidationError.REASONCODE_XSD_ISINVALID;
  } else if (reasonCode.equalsIgnoreCase(ValidationError.REASONCODE_XSD_VIOLATION)) {
    _reasonCode = ValidationError.REASONCODE_XSD_VIOLATION;
  } else {
    _reasonCode = ValidationError.REASONCODE_PARAMETER_ISINVALID;
  }
}

/**
 * Gets the section containing parameter that failed to validate.
 * @return the containing section
 */
public Section getSection() {
  return _section;
}
/**
 * Sets the section containing parameter that failed to validate.
 * @param section the containing section
 */
public void setSection(Section section) {
  _section = section;
}

/**
 * Gets the section resource key.
 * @return the section resource key
 */
public String getSectionResourceKey() {
  if ((getSection() != null) && (getSection().getLabel() != null)) {
    return getSection().getLabel().getResourceKey();
  } else {
    return "";
  }
}

// methods =====================================================================

}

