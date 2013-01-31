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
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import java.util.Locale;

/**
 * Ontology model utility.
 */
public class OntModelUtil {

  /** ontology model */
  private OntModel model;

  /**
   * Creates instance of the util class.
   * @param model ontology model
   */
  public OntModelUtil(OntModel model) {
    this.model = model;
  }

  /**
   * Gets code assoctaied with the specified label.
   * @param label label
   * @param locale locale
   * @return code or empty string if no label found
   */
  public String getCode(String label, Locale locale) {
    QueryExecution queryExecutor = null;

    try {

      Query query = createQuery(label, locale);
      queryExecutor = QueryExecutionFactory.create(query, model);

      ResultSet results = queryExecutor.execSelect();
      String codeList = ResultSetFormatter.toList(results).toString();

      return codeList.indexOf("<") >= 0 ?
        codeList.substring(codeList.indexOf("<") + 1, codeList.indexOf(">")) :
        "";

    } finally {
      if (queryExecutor != null) {
        queryExecutor.close();
      }
    }
  }

  /**
   * Creates query.
   * @param label label
   * @return query
   */
  private Query createQuery(String label, Locale locale) {
    return QueryFactory.create(
      "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>" +
      "SELECT ?code " +
      "WHERE { ?code rdfs:label \"" + label + "\"@" +locale.getLanguage()+"}");
  }
}
