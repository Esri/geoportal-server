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
package com.esri.ontology.service.catalog;

import com.hp.hpl.jena.ontology.OntModel;
import java.io.Serializable;
import javax.servlet.ServletContext;

/**
 * Application context.
 */
public class Context implements Serializable {

  /** categories */
  private Categories categories;
  /** ontology model */
  private OntModel model;

  /**
   * Extracts context from the session.
   * @param servletContext servlet context
   * @return context or <code>null</code> if context not available.
   */
  public static Context extract(ServletContext servletContext) {
    Object ctxObj = servletContext.getAttribute(Context.class.getCanonicalName());
    return ctxObj instanceof Context? (Context)ctxObj: null;
  }

  /**
   * Updates context stored within the session with the current context.
   * @param servletContext servlet context
   */
  public void update(ServletContext servletContext) {
    servletContext.setAttribute(Context.class.getCanonicalName(), this);
  }

  /**
   * Gets categories.
   * @return categories
   */
  public synchronized Categories getCategories() {
    return categories;
  }

  /**
   * Sets categories.
   * @param categories categories
   */
  public synchronized void setCategories(Categories categories) {
    this.categories = categories;
    if (isReady()) {
      // notify al interested that context is ready
      this.notifyAll();
    }
  }

  /**
   * Gets model.
   * @return model
   */
  public synchronized OntModel getModel() {
    return model;
  }

  /**
   * Sets model.
   * @param model model
   */
  public synchronized void setModel(OntModel model) {
    this.model = model;
    if (isReady()) {
      // notify al interested that context is ready
      this.notifyAll();
    }
  }

  /**
   * Checks if context is ready.
   * @return <code>true</code> if context is ready
   */
  public synchronized boolean isReady() {
    return categories!=null && model!=null;
  }
}
