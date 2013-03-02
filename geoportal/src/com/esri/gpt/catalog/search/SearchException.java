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
package com.esri.gpt.catalog.search;

import com.esri.gpt.framework.context.ApplicationException;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;




/**
 * General exception class used by package.  Can get messages from
 * property files too.
 */
@SuppressWarnings("serial")
public class SearchException extends ApplicationException {

// instance variables ==========================================================
/** The has user message. */
private boolean hasUserMessage;


/**
 * Checks if exception should be changed to a gui type exception.  If true
 * do not bother printing exception in logs (general user error)
 * 
 * @return true, if exception has non searchException cause
 */
public boolean getHasUserMessage() {
  return hasUserMessage;
}

/**
 * 
 * Sets the checks for user message.
 * 
 * @param hasUserMessage the new checks for user message
 */
private void setHasUserMessage(boolean hasUserMessage) {
  this.hasUserMessage = hasUserMessage;
}

// constructor =================================================================
/**
 * Instantiates a new search exception.
 * 
 * @param detailedMessage the detailed message
 * 
 */
public SearchException(String detailedMessage) {
  super(getElaborateMsg(detailedMessage, null));
  this.setHasUserMessage(true);

}

/**
 * Instantiates a new search exception.
 * 
 * @param detailedMessage the detailed message
 * @param args the arguments to be put into the message
 */
public SearchException(String detailedMessage, Object[] args) {
  super(getElaborateMsg(detailedMessage, args));
  this.setHasUserMessage(true);
}


/**
 * Instantiates a new search exception.
 * 
 * @param exception Exception to be included
 */
public SearchException(Exception exception) {
  super(exception);
  if(exception instanceof SearchException){
    this.setHasUserMessage(((SearchException) exception).getHasUserMessage());
  } else {
    this.setHasUserMessage(false);
  }
}
  
/**
 * Instantiates a new search exception.
 * 
 * @param exception the exception
 * @param detailedMessage the detailed message
 */
public SearchException(String detailedMessage, Throwable exception) {
  super(getElaborateMsg(detailedMessage, null), exception);
  if(exception instanceof SearchException){
    this.setHasUserMessage(((SearchException) exception).getHasUserMessage());
  } else {
    this.setHasUserMessage(false);
  }

}

/**
 * Instantiates a new search exception.
 * 
 * @param detailedMessage the detailed message
 * @param exception the exception
 * @param args the args for the detailed message
 */
public SearchException(String detailedMessage,  
    Throwable exception, Object[] args) {
  super(getElaborateMsg(detailedMessage, args), exception);

}

// methods =====================================================================
/**
 * Gets the property string, or returns the original string if
 * original string is determined not to be a property.
 * 
 * @param propMsg the property message or just any string
 * @param args the args
 * 
 * @return the property string or propMsg
 */
private static String getElaborateMsg( String propMsg, 
                                      Object[] args) {
  
  String msg = Val.chkStr(propMsg);
  // checking if string could be a property match
  if(msg.matches("(\\p{Alnum}+[.])+\\p{Alnum}+")){
    FacesContextBroker fcsBroker = new FacesContextBroker();
    MessageBroker msgBroker = fcsBroker.extractMessageBroker();
    String tmp = msgBroker.retrieveMessage(msg, args);
    if(tmp.length() > 0) {
      msg = tmp;
    }
  }  
      
  return msg;
}
}
