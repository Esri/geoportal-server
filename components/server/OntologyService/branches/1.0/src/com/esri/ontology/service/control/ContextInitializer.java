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

import com.esri.ontology.service.catalog.Categories;
import com.esri.ontology.service.catalog.Context;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Ontology context initializer.
 */
public class ContextInitializer {

  /** ontology context to initialize */
  private Context context;
  /** categories file path */
  private String categoriesFilePath;
  /** gemet definition file path */
  private String gemetFilePath;
  /** log */
  private static final Logger log = Logger.getLogger(
    ContextInitializer.class.getName());

  /**
   * Creates instance of initializer.
   * @param context context to initialize
   * @param categoriesFilePath categories file path
   * @param gemetFilePath gemet definition file path
   */
  public ContextInitializer(Context context, String categoriesFilePath,
                            String gemetFilePath) {
    this.context = context;
    this.categoriesFilePath = categoriesFilePath;
    this.gemetFilePath = gemetFilePath;
  }

  /**
   * Starting initialization.
   */
  public void initialize() {

    Thread categoriesThread = 
      new Thread(new CategoriesInitializer(),"Categories initalizer");
    categoriesThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler());
    Thread gemetThread = new Thread(new GemetInitializer(), "Gemet initializer");
    gemetThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler());

    categoriesThread.setDaemon(true);
    gemetThread.setDaemon(true);

    categoriesThread.start();
    gemetThread.start();
  }

  /**
   * Uncaught exception handler.
   */
  private class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread t, Throwable e) {
      log.log(Level.SEVERE, "Exception in thread: "+t.getName(), e);
    }
  }

  /**
   * Categories initializer.
   */
  private class CategoriesInitializer implements Runnable {

    public void run() {
      try {

        log.info("Initializing categories...");

        DocumentBuilderFactory factory =
          DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(categoriesFilePath);
        Document dom = builder.parse(inputSource);

        Categories categories = new Categories();

        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList ctgNodes =
          (NodeList) xpath.evaluate("category", dom.getFirstChild(),
          XPathConstants.NODESET);
        for (int i = 0; i < ctgNodes.getLength(); i++) {
          Node ctgNode = ctgNodes.item(i);
          String key = (String) xpath.evaluate("@key", ctgNode,
            XPathConstants.STRING);
          String value = (String) xpath.evaluate("@value", ctgNode,
            XPathConstants.STRING);
          categories.put(key, value);
        }

        context.setCategories(categories);

        log.info("Categories initialized.");

      } catch (Exception ex) {
        log.log(Level.SEVERE, "Categories initialization failed", ex);
      }
    }
  }

  /**
   * Gemet initializer.
   */
  private class GemetInitializer implements Runnable {

    public void run() {
      try {
        log.info("Initializing gemet...");

        OntDocumentManager ontManager = new OntDocumentManager();
        OntModelSpec s = new OntModelSpec(OntModelSpec.OWL_MEM);
        OntModel model = ontManager.getOntology(gemetFilePath, s);
        
        context.setModel(model);

        log.info("Gemet initialized.");
      } catch (Exception ex) {
        log.log(Level.SEVERE, "Gement initialization failed", ex);
      }
    }
  }

}
