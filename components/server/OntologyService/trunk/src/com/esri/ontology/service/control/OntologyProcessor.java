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
import com.esri.ontology.service.catalog.Term.Relationship;
import com.esri.ontology.service.catalog.Terms;
import com.esri.ontology.service.catalog.OntClassMap;
import com.esri.ontology.service.catalog.OntModelUtil;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Processes ontology query.
 */
public class OntologyProcessor {

  /** ontology context */
  private Context ontCtx;
  /** locale */
  private Locale locale;
  /** log */
  private static Logger log =
    Logger.getLogger(OntologyProcessor.class.getName());

  /**
   * Creates instance of the class.
   * @param ontCtx ontology context
   * @param locale locale
   */
  public OntologyProcessor(Context ontCtx, Locale locale) {
    this.ontCtx = ontCtx;
    this.locale = locale;
  }

  /**
   * Performs search.
   * @param queryCriteria query criteria
   * @param selection selection criteria
   * @return collection of terms
   */
  public Terms search(QueryCriteria queryCriteria, Selection selection) {

    Terms terms = new Terms();

    OntModelUtil ontModelUtil = new OntModelUtil(ontCtx.getModel());

    OntClass search = ontCtx.getModel().getOntClass(
      ontModelUtil.getCode(queryCriteria.getTerm(), locale));

    if (search == null) {
      log.fine("Query: " + queryCriteria + " returned no results.");
      return terms;
    }

    // process categories
    if (selection.isCategories()) {
      processCategories(ontModelUtil, queryCriteria, search, terms);
    }

    // process neighbors
    if (selection.isNeighbors()) {
      processNeighbors(ontModelUtil, queryCriteria, search, terms);

    }

    terms.removeByThreshold(queryCriteria.getThreshold());
    terms.sortByCount();

    return terms;
  }

  /**
   * Process categories.
   * @param ontModelUtil ontology model utility
   * @param queryCriteria query criteria
   * @param search search ontology class
   * @param terms terms
   */
  private void processCategories(
    OntModelUtil ontModelUtil,
    QueryCriteria queryCriteria,
    OntClass search,
    Terms terms) {
    OntClassMap classMap = new OntClassMap();
    Iterator<String> CatIt = ontCtx.getCategories().keySet().iterator();
    while (CatIt.hasNext()) {
      String cat = CatIt.next();
      String code = ontModelUtil.getCode(cat, locale);
      if (code != null) {
        classMap.put(cat, ontCtx.getModel().getOntClass(code));
      }
    }

    int numberrecord = 0;
    Iterator<String> CatOnt = classMap.keySet().iterator();
    while (CatOnt.hasNext()) {
      String key = CatOnt.next();
      OntTools.Path path =
        OntTools.findShortestPath(ontCtx.getModel(), search, classMap.get(key),
        Filter.any);
      if (path != null) {

        float ii = 1;
        int TopicRecord = numberrecord;
        Term countCat = new Term(key, Relationship.SubClassOf, path.size());
        terms.add(countCat);
        numberrecord++;

        for (Iterator it = path.iterator(); it.hasNext();) {

          Statement statement = (Statement) it.next();
          OntClass onts =
            ontCtx.getModel().getOntClass(statement.getSubject().getNameSpace());
          OntClass onto =
            ontCtx.getModel().getOntClass(statement.getObject().toString());
          if (statement.getPredicate().getLocalName().equalsIgnoreCase(
            "seeAlso")) {
            ii = ii - (1 - queryCriteria.getSeeAlsoWeight());
          } else if (statement.getPredicate().getLocalName().equalsIgnoreCase(
            "subClassOf")) {
            ii = ii - (1 - queryCriteria.getSubClassWeight());
          }
          Term countCatDetails = new Term(onts.getLabel(locale.getLanguage()),
            Relationship.parse(statement.getPredicate().getLocalName()),
            onto.getLabel(locale.getLanguage()), ii);
          terms.add(countCatDetails);
          numberrecord++;
          ii++;

        }
        Term countCat1 = new Term(key, Relationship.SubClassOf, ii - 1);
        terms.set(TopicRecord, countCat1);
      }
    }
  }

  /**
   * Process neighbors.
   * @param ontModelUtil ontology model utility
   * @param queryCriteria query criteria
   * @param search search ontology class
   * @param terms terms
   */
  private void processNeighbors(
    OntModelUtil ontModelUtil,
    QueryCriteria queryCriteria,
    OntClass search,
    Terms terms) {

    HashMap<Integer, List<OntClass>> ontClassMap =
      new HashMap<Integer, List<OntClass>>();

    TreeSet<String> termsFound = new TreeSet<String>();

    termsFound.add(search.getLabel(locale.getLanguage()));
    ontClassMap.put(0, Arrays.asList(new OntClass[]{search}));

    for (int lev = 0; lev < queryCriteria.getLevel(); lev++) {

      ArrayList<OntClass> ontClasses = new ArrayList<OntClass>();
      for (Iterator<OntClass> iterator = ontClassMap.get(lev).iterator(); iterator.
        hasNext();) {
        search = iterator.next();

        if (search.getSeeAlso() != null) {

          for (Iterator iter = search.listSeeAlso(); iter.hasNext();) {
            OntResource ontResource = (OntResource) iter.next();

            if (!termsFound.contains(ontResource.getLabel(locale.getLanguage()))) {
              Term term =
                new Term(search.getLabel(locale.getLanguage()),
                Relationship.SeeAlso,
                ontResource.getLabel(locale.getLanguage()),
                queryCriteria.getSeeAlsoWeight() * (lev + 1));
              terms.add(term);
              ontClasses.add(ontResource.asClass());
              termsFound.add(ontResource.getLabel(locale.getLanguage()));
            }
          }
        }

        if (search.hasSubClass()) {

          for (Iterator iter = search.listSubClasses(); iter.hasNext();) {
            OntResource ontResource = (OntResource) iter.next();

            if (!termsFound.contains(ontResource.getLabel(locale.getLanguage()))) {
              Term term =
                new Term(ontResource.getLabel(locale.getLanguage()),
                Relationship.SubClassOf,
                search.getLabel(locale.getLanguage()),
                queryCriteria.getSubClassWeight() * (lev + 1));
              terms.add(term);
              ontClasses.add(ontResource.asClass());
              termsFound.add(ontResource.getLabel(locale.getLanguage()));
            }
          }

          if (search.hasSuperClass()) {

            for (Iterator iter = search.listSuperClasses(); iter.hasNext();) {
              OntResource ontResource = (OntResource) iter.next();

              if (!termsFound.contains(
                ontResource.getLabel(locale.getLanguage()))) {
                Term term =
                  new Term(search.getLabel(locale.getLanguage()),
                  Relationship.SubClassOf,
                  ontResource.getLabel(locale.getLanguage()),
                  queryCriteria.getSubClassWeight() * (lev + 1));
                terms.add(term);
                ontClasses.add(ontResource.asClass());
                termsFound.add(ontResource.getLabel(locale.getLanguage()));
              }
            }
          }

        }
        ontClassMap.put(lev + 1, ontClasses);
      }
    }
  }
}
