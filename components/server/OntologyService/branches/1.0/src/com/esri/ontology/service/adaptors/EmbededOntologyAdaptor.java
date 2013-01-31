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

import com.esri.ontology.service.catalog.Context;
import com.esri.ontology.service.catalog.Term;
import com.esri.ontology.service.catalog.Terms;
import com.esri.ontology.service.control.ContextInitializer;
import com.esri.ontology.service.control.OntologyProcessor;
import com.esri.ontology.service.control.OntologyWriter;
import com.esri.ontology.service.util.Val;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;

/**
 * Embeded ontology adaptor.
 */
public class EmbededOntologyAdaptor extends BaseOntologyAdaptor {

  /** ontology context */
  private final Context ontCtx = new Context();
  /** locale */
  private Locale locale = Locale.ENGLISH;

  /**
   * Initializes adaptor.
   * <p/>
   * Initialization parameters:
   * <ul>
   * <li>categoriesFilePath - categories definition file path [mandatory]</li>
   * <li>gemetFilePath - GEMET definition file path [mandatory]</li>
   * <li>lang - language [optional; default: <i>en</i>]</li>
   * </ul>
   * @param properties properties
   * @see BaseOntologyAdaptor#init
   */
  @Override
  public void init(Properties properties) {
    super.init(properties);
    String categoriesFilePath = properties.getProperty("categoriesFilePath", "");
    String gemetFilePath = properties.getProperty("gemetFilePath", "");
    String lang = properties.getProperty("lang", "");

    for (Locale l: Locale.getAvailableLocales()) {
      if (l.getLanguage().equalsIgnoreCase(lang)) {
        locale = l;
        break;
      }
    }

    ContextInitializer initializer = new ContextInitializer(ontCtx,
      categoriesFilePath, gemetFilePath);
    initializer.initialize();

  }

  public String parse(String term) throws OntologyAdaptorException {
    term = Val.chkStr(term);

    final StringBuilder sb = new StringBuilder();
    OutputStream os = new OutputStream() {

      @Override
      public void write(int b) throws IOException {
        sb.append((char) b);
      }
    };

    Terms terms = new Terms();

    if (!ontCtx.isReady()) {
      synchronized (ontCtx) {
        try {
          ontCtx.wait(10000);
        } catch (InterruptedException ex) {
        }
      }
    }

    if (ontCtx.isReady()) {
      queryCriteria.setTerm(term);
      OntologyProcessor processor = new OntologyProcessor(ontCtx, locale);
      terms = processor.search(queryCriteria, selection);
    } else {
      Term tm = new Term(term,Term.Relationship.SeeAlso,1);
      terms.add(tm);
    }

    PrintWriter writer = new PrintWriter(os);

    OntologyWriter ontologyWriter =
      new OntologyWriter(writer, ontCtx, term, format);
    ontologyWriter.write(terms);

    writer.close();

    return sb.toString();
  }
}
