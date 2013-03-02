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
package com.esri.gpt.catalog.harvest.clients.exceptions;

/**
 * Invalid protocol exception.
 * Thrown when attributes defining connection protocol are invalid.
 */
public class HRInvalidProtocolException extends HRException {

/** protocol element which is invalid */
private ProtocolElement _definitionElement;

/**
 * Constructs an instance of <code>HRInvalidProtocolException</code> 
 * with the specified detail  message.
 * @param element protocol element which is invalid
 * @param msg the detail message
 */
public HRInvalidProtocolException(ProtocolElement element, String msg) {
  super(msg);
  _definitionElement = element;
}

/**
 * Constructs an instance of <code>HRInvalidProtocolException</code> 
 * with the specified detail  message and cause.
 * @param element protocol element which is invalid
 * @param msg the detail message
 * @param t cause of the exceptio (stack trace)
 */
public HRInvalidProtocolException(
  ProtocolElement element, String msg, Throwable t) {
  super(msg, t);
  _definitionElement = element;
}

/**
 * Gets protocol element which is invalid.
 * @return definition element
 */
public ProtocolElement getElement() {
  return _definitionElement;
}

/**
 * Protocol elements.
 */
public enum ProtocolElement {
/** protocol type */
protocol,  
/** Host url. */
url,
/** Port number. */
portNo,
/** Source uri. */
sourceUri,
/** User name. */
userName,
/** User password. */
userPassword,
/** Service name (ArcIMS only). */
serviceName,
/** Set (OAI only). */
set,
/** Prefix (OAI only). */
prefix,
/** Database name (Z3950 only). */
databaseName;
}
}
