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
package com.esri.ontology.service.adaptors;

/**
 * Ontology adaptor exception.
 */
public class OntologyAdaptorException extends Exception {

  /**
   * Creates a new instance of <code>OntologyAdaptorException</code> without
   * detail message.
   */
  public OntologyAdaptorException() {
  }

  /**
   * Constructs an instance of <code>OntologyAdaptorException</code> with the
   * specified detail message.
   * @param msg the detail message.
   */
  public OntologyAdaptorException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of <code>OntologyAdaptorException</code> with the
   * specified detail message.
   * @param msg the detail message.
   * @param t cause`
   */
  public OntologyAdaptorException(String msg, Throwable t) {
    super(msg, t);
  }
}
