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
package com.esri.ontology.service.servlet;

import com.esri.ontology.service.control.OntologyProcessor;
import com.esri.ontology.service.control.Selection;
import com.esri.ontology.service.control.QueryCriteria;
import com.esri.ontology.service.control.Format;
import com.esri.ontology.service.control.ContextInitializer;
import com.esri.ontology.service.catalog.Context;
import com.esri.ontology.service.catalog.Terms;
import com.esri.ontology.service.control.OntologyWriter;
import com.esri.ontology.service.util.Val;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Ontology servlet.
 * <p/>
 * Request parameters:
 * <ul>
 * <li>term - search term [mandatory]</li>
 * <li>seealso - weight of SeeAlso bond [optional; default: 2.0]</li>
 * <li>subclassof - weight of SubClassOf bond [optional; default: 2.0]</li>
 * <li>level - tree traverse depth level [optional; default: 1]</li>
 * <li>selection - neighbors selection [optional; choices: <i>categories</i>, <i>neighbors</i>, <i>all</i>; default: <i>all</i>]</li>
 * <li>f - output format [optional; choices: <i>lucene</i>, <i>owl</i>, <i>all</i>; default: <i>lucene</i>]</li>
 * </ul>
 */
public class OntologyServlet extends HttpServlet {

  /** categories attribute key */
  private final static String CATEGORIES_KEY = "categories";
  /** gemet attribute key */
  private final static String GEMET_KEY = "gemet";
  /** log */
  private static Logger log = Logger.getLogger(OntologyServlet.class.getName());

  @Override
  public void init() throws ServletException {
    super.init();

    log.info("Initializing ontology servlet.");

    String categoriesFilePath = getInitParameter(CATEGORIES_KEY);
    String gemetFilePath = getInitParameter(GEMET_KEY);

    URL categoriesFileUrl = Thread.currentThread().getContextClassLoader().
      getResource(categoriesFilePath);
    URL gemetFileUrl = Thread.currentThread().getContextClassLoader().
      getResource(gemetFilePath);

    Context context = new Context();
    context.update(getServletContext());

    ContextInitializer contextInitializer =
      new ContextInitializer(context, categoriesFileUrl.toString(),
      gemetFileUrl.toString());

    contextInitializer.initialize();
  }

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request,
                                HttpServletResponse response)
    throws ServletException, IOException {

    // gets writer
    PrintWriter out = response.getWriter();

    // parses query
    QueryCriteria queryCriteria = extractQueryCriteria(request);
    Selection selection = extractSelection(request);
    Format format = extractFormat(request);

    // sets response attributes
    response.setCharacterEncoding("UTF-8");
    response.setContentType(format.isOwl() ? "text/html" : "text/plain");

    // creates ontology context
    Context ontContext = Context.extract(this.getServletContext());
    OntologyProcessor ontProcessor = new OntologyProcessor(ontContext,
      request.getLocale());

    // checks if ontology context is ready
    if (!ontContext.isReady()) {
      log.info("Ontology context not ready.");
      response.setStatus(response.SC_SERVICE_UNAVAILABLE);
      return;
    }

    // checks if query criteria are correct
    if (queryCriteria.getTerm().length() == 0) {
      log.info("No search term entered.");
      response.setStatus(response.SC_BAD_REQUEST);
      return;
    }

    // performs ontology search
    log.info(
      "Performing ontology search. query criteria: " + queryCriteria + "; selection: " + selection + "; format: " + format);
    Date start = new Date();
    Terms terms = ontProcessor.search(queryCriteria, selection);
    Date end = new Date();
    log.info("Ontology search has been completed in " + (end.getTime() - start.
      getTime()) + " milliseconds.");

    OntologyWriter ontWriter = new OntologyWriter(out, ontContext, queryCriteria.getTerm(), format);
    ontWriter.write(terms);

  }

  /**
   * Extracts query criteria from the request.
   * @param request HTTP request
   * @return query criteria
   */
  private QueryCriteria extractQueryCriteria(HttpServletRequest request) {

    String searchTerm = request.getParameter("term");
    float seeAlso = Val.chkFloat(request.getParameter("seealso"), 2);
    float subClassOf = Val.chkFloat(request.getParameter("subclassof"), 2);
    float threshold = Val.chkFloat(request.getParameter("threshold"), 0);
    int level = Val.chkInt(request.getParameter("level"), 1);

    level = Math.min(2, Math.max(1, level));

    QueryCriteria queryCriteria = new QueryCriteria();

    queryCriteria.setTerm(searchTerm.toLowerCase());
    queryCriteria.setSeeAlsoWeight(seeAlso);
    queryCriteria.setSubClassWeight(subClassOf);
    queryCriteria.setLevel(level);
    queryCriteria.setThreshold(threshold);

    return queryCriteria;
  }

  /**
   * Extracts output format from the request.
   * @param request HTTP request
   * @return output format
   */
  private Format extractFormat(HttpServletRequest request) {
    String sFormat = Val.chkStr(request.getParameter("f"));
    return Format.parse(sFormat.length() > 0? sFormat : "lucene");
  }

  /**
   * Extracts selection from the request.
   * @param request HTTP request
   * @return selection
   */
  private Selection extractSelection(HttpServletRequest request) {
    String sSelection = Val.chkStr(request.getParameter("selection"));
    return Selection.parse(sSelection.length() > 0 ? sSelection: "all");
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Ontology Servlet";
  }// </editor-fold>
}
