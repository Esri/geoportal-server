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
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyClause;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsBetween;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsEqualTo;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsGreaterThan;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsGreaterThanOrEqualTo;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsLessThan;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsLessThanOrEqualTo;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsNotEqualTo;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsNull;
import com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsLike;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.framework.context.RequestContext;

import java.util.Map;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Adapts a catalog discovery PropertyClause to the Lucene model.
 */
public class PropertyClauseAdapter extends DiscoveryClauseAdapter {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(PropertyClauseAdapter.class.getName());
  
  /** instance variables ====================================================== */
  private DatastoreField comparisonField;
  private Discoverable   discoverable;
  private String         expressionToQuery;
  private Storeable      storeable;
  private DatastoreField termsField;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated query adapter.
   * @param queryAdapter the query adapter
   */
  protected PropertyClauseAdapter(LuceneQueryAdapter queryAdapter) {
    super(queryAdapter);
  }
  
  /** methods ================================================================= */
 
  /**
   * Adapts a catalog discovery PropertyClause to the Lucene model.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param propertyClause the property clause to adapt
   * @throws DiscoveryException if an invalid clause is encountered
   * @throws ParseException if a Lucene query parsing exception occurs
   */
  protected void adaptPropertyClause(BooleanQuery activeBooleanQuery, 
                                     LogicalClause activeLogicalClause,
                                     PropertyClause propertyClause)
    throws DiscoveryException, ParseException {
    LOGGER.finer("Adapting PropertyClause...\n"+propertyClause);
    
    // determine the discoverable target, set the underlying storable
    discoverable = propertyClause.getTarget();
    if (discoverable == null) {
      String sErr = "The PropertyClause.target is null.";
      throw new DiscoveryException(sErr);
    }
    if (discoverable.getStorable() == null) {
      String sErr = "The PropertyClause.target.storeable is null.";
      throw new DiscoveryException(sErr);
    } else {
      storeable = (Storeable)discoverable.getStorable();
    }
    
    // execute the appropriate operation,
    if (propertyClause instanceof PropertyIsLike) {
      PropertyIsLike like = (PropertyIsLike)propertyClause;
      prepareTermsField(like);
      handleTermsClause(activeBooleanQuery,activeLogicalClause,like);
    } else {
      prepareComparisonField(propertyClause);
      handleComparisonClause(activeBooleanQuery,activeLogicalClause,propertyClause);
    }
  }
  
  /**
   * Appends a range query to the active boolean query.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param propertyClause the active property clause
   * @param lowerBoundary the lower boundary
   * @param upperBoundary the upper boundary
   * @param lowerBoundaryIsInclusive (>= versus >)
   * @param upperBoundaryIsInclusive (<= versus <)
   * @throws DiscoveryException if an invalid clause is encountered
   */
  private void appendRange(BooleanQuery activeBooleanQuery, 
                           LogicalClause activeLogicalClause,
                           PropertyClause propertyClause,
                           String lowerBoundary,
                           String upperBoundary,
                           boolean lowerBoundaryIsInclusive,
                           boolean upperBoundaryIsInclusive) 
    throws DiscoveryException {
    boolean standard = true;
    
    // there is a circumstance where a query for data valid within a range is split across 2 fields
    String fieldName = this.comparisonField.getName();
    if ((fieldName != null) && fieldName.equals("dateValidStart") && (upperBoundary != null)) {
      if (lowerBoundary == null) {
        standard = false;
        TimestampField tsEnd = new TimestampField("dateValidEnd");
        Query query = tsEnd.makeRangeQuery(
            lowerBoundary,upperBoundary,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
        appendQuery(activeBooleanQuery,activeLogicalClause,query);
      } else if (!lowerBoundary.equals(upperBoundary)) {
        standard = false;
        TimestampField tsEnd = new TimestampField("dateValidEnd");
        Query q1 = this.comparisonField.makeRangeQuery(
            lowerBoundary,null,lowerBoundaryIsInclusive,false);
        Query q2 = tsEnd.makeRangeQuery(
            null,upperBoundary,false,upperBoundaryIsInclusive);
        BooleanQuery bq = new BooleanQuery();
        bq.add(q1,BooleanClause.Occur.MUST);
        bq.add(q2,BooleanClause.Occur.MUST);
        this.appendQuery(activeBooleanQuery,activeLogicalClause,bq);
        return;
      }
    }
    
    // standard methodology
    if (standard) {
      Query query = this.comparisonField.makeRangeQuery(
          lowerBoundary,upperBoundary,lowerBoundaryIsInclusive,upperBoundaryIsInclusive);
      appendQuery(activeBooleanQuery,activeLogicalClause,query);
    }
  }
  
  /**
   * Adapts a property clause requiring an comparison field expression
   * to the Lucene model.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param propertyClause the property clause to adapt
   * @throws DiscoveryException if an invalid clause is encountered
   */
  private void handleComparisonClause(BooleanQuery activeBooleanQuery, 
                                      LogicalClause activeLogicalClause,
                                      PropertyClause propertyClause) 
    throws DiscoveryException {
    String fieldName = this.comparisonField.getName();
    String literal = propertyClause.getLiteral();
    
    // handle each operation 
    
    if (propertyClause instanceof PropertyIsBetween) {
      PropertyIsBetween between = (PropertyIsBetween)propertyClause;
      String lower = between.getLowerBoundary();
      String upper = between.getUpperBoundary();
      appendRange(activeBooleanQuery,activeLogicalClause,
          propertyClause,lower,upper,true,true);
      
    } else if (propertyClause instanceof PropertyIsEqualTo) {
      boolean checkFID = fieldName.equalsIgnoreCase(Storeables.FIELD_UUID) &&
                        (literal != null) && (literal.length() > 0);
      if (checkFID) {
        String id = literal;
        Query q1 = new TermRangeQuery(fieldName,id,id,true,true);
        Query q2 = new TermRangeQuery(Storeables.FIELD_FID,id,id,true,true);
        BooleanQuery bq = new BooleanQuery();
        bq.add(q1,BooleanClause.Occur.SHOULD);
        bq.add(q2,BooleanClause.Occur.SHOULD);
        appendQuery(activeBooleanQuery,activeLogicalClause,bq);
        
      } else {
        appendRange(activeBooleanQuery,activeLogicalClause,
            propertyClause,literal,literal,true,true);
      }
    
    } else if (propertyClause instanceof PropertyIsGreaterThan) {
      appendRange(activeBooleanQuery,activeLogicalClause,
          propertyClause,literal,null,false,false);
      
    } else if (propertyClause instanceof PropertyIsGreaterThanOrEqualTo) {
      appendRange(activeBooleanQuery,activeLogicalClause,
          propertyClause,literal,null,true,false);
      
    } else if (propertyClause instanceof PropertyIsLessThan) {
      appendRange(activeBooleanQuery,activeLogicalClause,
          propertyClause,null,literal,false,false); 
      
    } else if (propertyClause instanceof PropertyIsLessThanOrEqualTo) {
      appendRange(activeBooleanQuery,activeLogicalClause,
          propertyClause,null,literal,false,true);  
      
    } else if (propertyClause instanceof PropertyIsNotEqualTo) {     
      appendRange(activeBooleanQuery,new LogicalClause.LogicalNot(), 
          propertyClause,literal,literal,true,true); 
      
    } else if (propertyClause instanceof PropertyIsNull) {
      appendNullCheck(activeBooleanQuery,fieldName);
      
    } else {
      String sErr = "Unrecognized property clause type: ";
      throw new DiscoveryException(sErr+propertyClause.getClass().getName());
    }

  }
  
  /**
   * Adapts a property clause requiring the parsing of an expression
   * to the Lucene model.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param propertyClause the property clause to adapt
   * @throws DiscoveryException if an invalid clause is encountered
   * @throws ParseException if a Lucene query parsing exception occurs
   */
  private void handleTermsClause(BooleanQuery activeBooleanQuery, 
                                 LogicalClause activeLogicalClause,
                                 PropertyIsLike propertyClause) 
    throws DiscoveryException, ParseException {
    Analyzer analyzer = getQueryAdapter().getIndexAdapter().newAnalyzer();
    
    String[] fields = null;
    if (storeable instanceof AnyTextProperty) {
      AnyTextProperty anyText = (AnyTextProperty)storeable;
      fields = anyText.getFieldNames();
    } else {
      String fieldName;
      if (termsField != null) {
        fieldName = termsField.getName();
      } else {
        fieldName = comparisonField.getName();
      }
      fields = new String[]{fieldName};
    }
    
    // make the parser
    LuceneConfig lcfg = getQueryAdapter().getIndexAdapter().getLuceneConfig();
    Map<String, IParserProxy> proxies = lcfg.getParserProxies();
    final TermResolver streamer = new TermResolver(proxies);
    final QueryProvider queryProvider = 
        new QueryProvider(fields, lcfg.getUseConstantScoreQuery(), getQueryAdapter(), getMeanings());
    QueryParser parser = createQueryParser(fields, analyzer, streamer, queryProvider);
    if (discoverable.getMeaning().getAllowLeadingWildcard()) {
      parser.setAllowLeadingWildcard(true);
    }
    String sMsg = "Applying parser: "+parser.getClass().getName()+
      "\n  to fields: "+fields+ "\n  queryExpression: "+expressionToQuery;
    LOGGER.finer(sMsg);
    
    // parse the query expression (auto-escape if an exception occurs)
    try {
      Query query = parser.parse(expressionToQuery);
      appendQuery(activeBooleanQuery,activeLogicalClause,query); 
    } catch (ParseException pe) {
      Query query = parser.parse(QueryParser.escape(expressionToQuery));
      appendQuery(activeBooleanQuery,activeLogicalClause,query); 
    }
    this.getQueryAdapter().setHasScoredExpression(true);
      
  }

  /**
   * Creates query parser.
   * @param fields array of fields
   * @param analyzer analyzer
   * @param streamer streamer
   * @param queryProvider query provider
   * @return query parser
   */
  private QueryParser createQueryParser(String [] fields, Analyzer analyzer, TermResolver streamer, QueryProvider queryProvider) {
    return ((fields != null) && (fields.length == 1))?
      new ExtQueryParser(fields[0], analyzer, streamer, queryProvider):
      new ExtMultiFieldQueryParser(fields, analyzer, streamer, queryProvider);
  }

  /**
   * Gets property meanings.
   * @return property meanings
   */
  private PropertyMeanings getMeanings() throws DiscoveryException {
    RequestContext context = this.getQueryAdapter().getIndexAdapter().getRequestContext();
    return context.getCatalogConfiguration().getConfiguredSchemas().getPropertyMeanings();
  }

  /**
   * Ensure that there is a not-tokenized field that can be used for
   * non-term comparisons, then set appropriate query values. 
   * @param propertyClause the active property clause
   * @throws DiscoveryException if the property cannot be determined
   */
  private void prepareComparisonField(PropertyClause propertyClause) 
    throws DiscoveryException {
    // TODO what about geometry is null comparison??
    comparisonField = storeable.getComparisonField();
    if (comparisonField == null) {
      String sErr = "Storeable.name \""+storeable.getName()+"\" ";
      sErr += "has no associated comparison field. ";
      sErr += propertyClause.getClass().getSimpleName()+" cannot be executed.";
      throw new DiscoveryException(sErr);
    }
  }
   
  /**
   * Ensure that there is a tokenized field that can be used for
   * term comparisons, then sets the query expression.
   * @param propertyClause the active property clause
   * @throws DiscoveryException if the property cannot be determined
   */
  private void prepareTermsField(PropertyIsLike propertyClause) 
    throws DiscoveryException {
    expressionToQuery = propertyClause.getLiteral();
    if (!(storeable instanceof AnyTextProperty)) {
      termsField = storeable.getTermsField();
      if (termsField == null) {
        comparisonField = storeable.getComparisonField();
        if (comparisonField == null) {
          String sErr = "Storeable.name \""+storeable.getName()+"\" ";
          sErr += "has no associated terms or comparison field. ";
          sErr += propertyClause.getClass().getSimpleName()+" cannot be executed.";
          throw new DiscoveryException(sErr);
        }
      }
    }

    // TODO the expression may need to be escaped,
    // we also need to check for wild cards 
  }
  
}
