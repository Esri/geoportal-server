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

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The Class Messages.  Class used by UIComponent to get configurations for
 * the components.
 */
/* package */ class Messages {

/** The Constant BUNDLE_NAME. */
private static final String         BUNDLE_NAME     
  = "com.esri.gpt.framework.jsf.components.messages"; //$NON-NLS-1$

/** The Constant RESOURCE_BUNDLE. */
private static final ResourceBundle RESOURCE_BUNDLE 
  = ResourceBundle
                                                        .getBundle(BUNDLE_NAME);

/**
 * Instantiates a new messages.
 */
private Messages() {
}

/**
 * Gets the string.
 * 
 * @param key the key
 * 
 * @return the string
 */
public static String getString(String key) {
  try {
    return RESOURCE_BUNDLE.getString(key);
  } catch (MissingResourceException e) {
    return '!' + key + '!';
  }
}
}
