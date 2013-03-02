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
package com.esri.gpt.catalog.lucene;
import org.apache.lucene.document.Field;

/**
 * A tokenized field that allows for term and parser based expression queries.
 */
public class TermsField extends DatastoreField {
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the field name
   */
  protected TermsField(String name) { 
    super(name,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.NO);
  }
 
}
