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
package com.esri.ontology.service.control;

import com.esri.ontology.service.catalog.Context;
import com.esri.ontology.service.catalog.Term;
import com.esri.ontology.service.catalog.Terms;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

/**
 * Ontology response writer.
 */
public class OntologyWriter {
  /** log */
  private static final Logger log = Logger.getLogger(OntologyWriter.class.getName());

  /** print writer */
  private PrintWriter writer;
  /** ontology context */
  private Context ontContext;
  /** search term */
  private String searchTerm;
  /** output format */
  private Format format;

  /**
   * Creates instance of the writer.
   * @param format output format
   */
  /**
   * Creates instance of the writer.
   * @param writer writer to write output
   * @param ontContext ontology context
   * @param searchTerm original search term
   * @param format output format
   */
  public OntologyWriter(PrintWriter writer, Context ontContext, String searchTerm, Format format) {
    this.writer = writer;
    this.ontContext = ontContext;
    this.searchTerm = searchTerm;
    this.format = format;
  }

  /**
   * Writes terms.
   * @param terms collection of terms
   */
  public void write(Terms terms) {

    // lucene query
    StringBuilder qq = new StringBuilder();
    qq.append("\"" + searchTerm + "\"^1");

    String tab = "";

    if (format.isOwl()) {
      writer.println("<html>");
      writer.println("<head><title>Semantic Search</title></head>");
      writer.println("<body>");
    }

    for (Term term: terms) {
      if (term.getRelationship() != null) {
        if (format.isOwl()) {
          if (term.isTopicCategory()) {
            writer.println("<br>Topic Category: " + " <b>" + term.getSubject() + " (" + ontContext.
              getCategories().get(term.getSubject()) + ") " + "</b> (" + term.
              getCount() + ")<br>");
            tab = "&nbsp;";
          } else {
            writer.println(tab + " <b>" + term.getSubject() + "</b> (" + term.
              getCount() + ")" + " " +
              term.getRelationship() + " " + term.getCategory() + "<br>");
            tab = tab + "&nbsp;";
          }
        }
        qq.append(" \"" + term.getSubject() + "\"^" + term.getCount());
      }

    }

    if (format.isLucene()) {
      try {
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser query = new QueryParser("", analyzer);
        writer.println(query.parse(qq.toString()).toString());
        log.info(query.parse(qq.toString()).toString());
      } catch (ParseException e) {
        log.log(Level.SEVERE, "Error preparing lucene response.", e);
      }
    }

    if (format.isOwl()) {
      writer.println("</body>");
      writer.println("</html>");
    }
  }
}
