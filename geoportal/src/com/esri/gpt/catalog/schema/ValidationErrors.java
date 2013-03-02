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
import java.util.ArrayList;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 * Defines a list of validation errors.
 */
public class ValidationErrors extends ArrayList<ValidationError> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public ValidationErrors() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null.
 * @param member the member to add
 * @return true if the member was added
 */
@Override
public boolean add(ValidationError member) {
  if (member != null) {
    return super.add(member);
  } else {
    return false;
  }
}

/**
 * Adds a validation error with an ValidationError.REASONCODE_ISINVALID
 * reason code.
 * @param section the section containing the invalid parameter
 * @param parameter the invalid parameter
 */
public void addInvalidParameter(Section section, Parameter parameter) {
  add(new ValidationError(
      section,parameter,ValidationError.REASONCODE_PARAMETER_ISINVALID));
}

/**
 * Builds validation error messages.
 * @param messageBroker the message broker
 * @param includeXpath if true, include parameter content xpath selection expressions
 */
public void buildMessages(MessageBroker messageBroker, boolean includeXpath) {
  IMessageAppender appender = new MessageBrokerAppender(messageBroker);
  buildMessages(messageBroker, appender, includeXpath);
}

/**
 * Builds validation error messages.
 * @param messageBroker the message broker
 * @param includeXpath if true, include parameter content xpath selection expressions
 */
public void buildMessages(MessageBroker messageBroker, 
                          List<String> list, 
                          boolean includeXpath) {
  IMessageAppender appender = new ListAppender(messageBroker, list);
  buildMessages(messageBroker, appender, includeXpath);
}


/**
 * Builds validation error messages.
 * @param messageBroker the message broker
 * @param appender appender
 * @param includeXpath if true, include parameter content xpath selection expressions
 */
private void buildMessages(MessageBroker messageBroker, 
                           IMessageAppender appender, 
                           boolean includeXpath) {
  for (ValidationError error: this) {
    String sCode = error.getReasonCode();
    if (sCode.equals(ValidationError.REASONCODE_TITLE_ISREQUIRED)) {
      appender.append("mdValidation.titleIsRequired");
    } else if (sCode.equals(ValidationError.REASONCODE_ENVELOPE_ISINVALID)) {
      appender.append("mdValidation.envelopeIsInvalid");
    } else if (sCode.equals(ValidationError.REASONCODE_XML_ISINVALID)) {
      appender.append(error.getMessage());
    } else if (sCode.equals(ValidationError.REASONCODE_XSD_ISINVALID)) {
      appender.append(error.getMessage());
    } else if (sCode.equals(ValidationError.REASONCODE_XSD_VIOLATION)) {
      appender.append(error.getMessage());
    } else if (sCode.equals(ValidationError.REASONCODE_SCHEMATRON_EXCEPTION)) {
      appender.appendDirect(error.getMessage());
    } else if (sCode.equals(ValidationError.REASONCODE_SCHEMATRON_VIOLATION)) {
      appender.appendDirect(error.getMessage());
      if (includeXpath) {
        String sLocation = Val.chkStr(error.location);
        if (sLocation.length() > 0) {
          appender.appendDirect("... "+sLocation);
        }
      }
    } else {
      
      String sMsg = "???";
      Section section = error.getSection();
      Parameter parameter = error.getParameter();
      ArrayList<String> alMsgs = new ArrayList<String>();     
      if (parameter != null) {
        sMsg = parameter.getKey();
        if (parameter.getLabel() != null) {
          sMsg = messageBroker.retrieveMessage(parameter.getLabel().getResourceKey());
        }
      }
      alMsgs.add(sMsg);
      while (section != null) {
        sMsg = section.getKey();
        if (section.getLabel() != null) {
          sMsg = messageBroker.retrieveMessage(section.getLabel().getResourceKey());
          alMsgs.add(sMsg);
        }
        section = section.getParent();
      }
      
      sMsg = "";
      for (int i=alMsgs.size()-1;i>=0;i--) {
        if (sMsg.length() == 0) {
          sMsg = alMsgs.get(i);
        } else {
          String[] args = {sMsg,alMsgs.get(i)};
          sMsg = messageBroker.retrieveMessage("mdValidation.parentChild",args);
        }
      }
      if (sMsg.length() == 0) sMsg = "???";
      
      String sReason = "mdValidation.isInvalid";
      if (sCode.equals(ValidationError.REASONCODE_PARAMETER_ISREQUIRED)) {
        sReason = "mdValidation.isRequired";
      }
      String[] args = {sMsg};
      appender.append(sReason,args);
      if (includeXpath) {
        String sXPath = error.getParameterXPath();
        if (sXPath.length() > 0) {
          appender.appendDirect(sXPath);
        }
      }
    } 
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName());
  if (size() == 0) {
    sb.append(" ()");
  } else {
    sb.append(" (\n");
    for (ValidationError member: this) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

/**
 * Message appender.
 */
private interface IMessageAppender {
/**
 * Appends string directly.
 */  
void appendDirect(String message);
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 */  
void append(String resourceKey);
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 * @param args array of arguments to be embeded within message
 */  
void append(String resourceKey, String [] args);
}

/**
 * Message appender which appends messages to the message broker.
 */
private class MessageBrokerAppender implements IMessageAppender {
/**
 * Message broker
 */  
private MessageBroker _messageBroker;  
/**
 * Appends string directly.
 */  
public void appendDirect(String message) {
  _messageBroker.addMessage(
    new FacesMessage(FacesMessage.SEVERITY_ERROR,Val.chkStr(message),null));
}
/**
 * Creates instance of the appender.
 * @param messageBroker message broker
 */
public MessageBrokerAppender(MessageBroker messageBroker) {
  _messageBroker = messageBroker;
}
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 */  
public void append(String resourceKey) {
  _messageBroker.addErrorMessage(resourceKey);
}
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 * @param args array of arguments to be embeded within message
 */  
public void append(String resourceKey, String [] args) {
  _messageBroker.addErrorMessage(resourceKey, args);
}
}

/**
 * Message appender which appends messages to message list.
 */
private class ListAppender implements IMessageAppender {
/**
 * Message broker
 */  
private MessageBroker _messageBroker;  
/**
 * Message list to append error messages.
 */
private List<String> _messages;  
/**
 * Creates instance of the appender.
 * @param messageBroker message broker
 * @param messages message list to append error messages
 */
public ListAppender(MessageBroker messageBroker, List<String> messages) {
  _messageBroker = messageBroker;
  _messages = messages;
}
/**
 * Appends string directly.
 */  
public void appendDirect(String message) {
  _messages.add(Val.chkStr(message));
}
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 */  
public void append(String resourceKey) {
  _messages.add(_messageBroker.retrieveMessage(resourceKey));
}
/**
 * Appends message identified by resource key.
 * @param resourceKey resource key
 * @param args array of arguments to be embeded within message
 */  
public void append(String resourceKey, String [] args) {
  _messages.add(_messageBroker.retrieveMessage(resourceKey, args));
}
}
}

