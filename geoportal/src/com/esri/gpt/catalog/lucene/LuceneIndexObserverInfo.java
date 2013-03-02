/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.catalog.lucene;

import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lucene index observer info.
 */
public class LuceneIndexObserverInfo {

  /** logger */
  private static final Logger log = Logger.getLogger(LuceneIndexObserverInfo.class.getName());
  /** observer class name */
  private String className = "";
  /** attributes */
  private StringAttributeMap attributes = new StringAttributeMap();

  /**
   * Gets class name.
   * @return class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets class name.
   * @param className adaptor class name
   */
  public void setClassName(String className) {
    this.className = Val.chkStr(className);
  }

  /**
   * Gets attributes.
   * @return attributes
   */
  public StringAttributeMap getAttributes() {
    return attributes;
  }

  /**
   * Sets attributes.
   * @param attributes attributes
   */
  public void setAttributes(StringAttributeMap attributes) {
    this.attributes = attributes != null ? attributes : new StringAttributeMap();
  }

  @Override
  public String toString() {
    return "{className=\"" + getClassName() + "\", attributes=" + getAttributes() + "}";
  }

  /**
   * Creates observer.
   * @return observer or <code>null</code> if observer can not be created
   */
  public LuceneIndexObserver createObserver() {
    if (getClassName().length() > 0) {
      try {

        // create instance of the parser proxy adaptor
        final Object instance = Class.forName(getClassName()).newInstance();
        // if adaptor has "init" method - call it
        Method method = Class.forName(getClassName()).getMethod("init", Properties.class);
        Properties props = new Properties();
        for (StringAttribute sa : getAttributes().values()) {
          props.put(sa.getKey(), sa.getValue());
        }
        method.invoke(instance, props);
        
        return (LuceneIndexObserver) instance;
        
      } catch (ClassCastException ex) {
        log.log(Level.SEVERE, "Error creating observer instance.", ex);
      } catch (NoSuchMethodException ex) {
        log.log(Level.WARNING, "Observer instance has not been initialized.", ex);
      } catch (InvocationTargetException ex) {
        log.log(Level.SEVERE, "Error initializing observer instance.", ex);
      } catch (IllegalArgumentException ex) {
        log.log(Level.SEVERE, "Error initializing observer instance.", ex);
      } catch (InstantiationException ex) {
        log.log(Level.SEVERE, "Error creating observer instance.", ex);
      } catch (IllegalAccessException ex) {
        log.log(Level.SEVERE, "Error creating observer instance.", ex);
      } catch (ClassNotFoundException ex) {
        log.log(Level.SEVERE, "Error creating observer instance.", ex);
      }
    }
    return null;
  }
}
