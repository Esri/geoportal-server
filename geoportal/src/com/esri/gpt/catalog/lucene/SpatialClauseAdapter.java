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
import com.esri.gpt.catalog.discovery.SpatialClause;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Logger;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.function.ValueSourceQuery;

/**
 * Adapts a catalog discovery SpatialClause to the Lucene model.
 */
public class SpatialClauseAdapter extends DiscoveryClauseAdapter {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(SpatialClauseAdapter.class.getName());
  
  /** instance variables ====================================================== */
  
  /** The input envelope. */
  private Envelope envelope;
    
  /** The field names. */
  private String docMinX = "envelope.minx";
  private String docMinY = "envelope.miny";
  private String docMaxX = "envelope.maxx";
  private String docMaxY = "envelope.maxy";
  private String docMinXLeft = "envelope.minx";
  private String docMaxXRight = "envelope.maxx";
  private String docXDL = "envelope.xdl";
  
  /** The query values */
  private boolean qryCrossedDateline = false;
  private double  qryMinX;
  private double  qryMinY;
  private double  qryMaxX;
  private double  qryMaxY;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated query adapter.
   * @param queryAdapter the query adapter
   */
  protected SpatialClauseAdapter(LuceneQueryAdapter queryAdapter) {
    super(queryAdapter);
  }
  
  /** methods ================================================================= */
 
  /**
   * Adapts a catalog discovery SpatialClause to the Lucene model.
   * @param activeBooleanQuery the active Lucene boolean query
   * @param activeLogicalClause the active discovery logical clause
   * @param spatialClause the spatial clause to adapt
   * @throws DiscoveryException if an invalid clause is encountered
   */
  protected void adaptSpatialClause(BooleanQuery activeBooleanQuery, 
                                    LogicalClause activeLogicalClause,
                                    SpatialClause spatialClause) 
    throws DiscoveryException {
    LOGGER.finer("Adapting SpatialClause...\n"+spatialClause);
    
    // determine the discoverable target, set names
    String sErr;
    Discoverable discoverable = spatialClause.getTarget();
    if (discoverable == null) {
      sErr = "The SpatialClause.target is null.";
      throw new DiscoveryException(sErr);
    }
    if (discoverable.getStorable() == null) {
      sErr = "The SpatialClause.target.storeable is null.";
      throw new DiscoveryException(sErr);
    } else {
      Storeable storeable = (Storeable)discoverable.getStorable();
      if (!(storeable instanceof GeometryProperty)) {
        sErr = "The SpatialClause.target.storeable is not a GeometryProperty.";
        throw new DiscoveryException(sErr);
      }
    }
        
    // check the envelope
    envelope = spatialClause.getBoundingEnvelope();
    if ((envelope == null) || envelope.isEmpty()) {
      sErr = "The SpatialClause.boundingEnvelope is empty.";
      throw new DiscoveryException(sErr);
    }
    
    // initialize the values of the input query envelope
    qryMinX = envelope.getMinX();
    qryMinY = envelope.getMinY();
    qryMaxX = envelope.getMaxX();
    qryMaxY = envelope.getMaxY();
    if (qryMinX > qryMaxX) {
      qryCrossedDateline = true;
    }
    
    // determine spatialRelevance parameters 
    // (original defaults were queryPower=2.0, targetPower=0.5)
    RequestContext rc = this.getQueryAdapter().getIndexAdapter().getRequestContext();
    StringAttributeMap params = rc.getCatalogConfiguration().getParameters();
    double queryPower = Val.chkDbl(params.getValue("spatialRelevance.queryPower"),1.0);
    double targetPower = Val.chkDbl(params.getValue("spatialRelevance.targetPower"),1.0);
    String rankingOption = Val.chkStr(params.getValue("spatialRelevance.ranking.enabled"));
    int rankingMaxDoc = Val.chkInt(params.getValue("spatialRelevance.ranking.maxDoc"),50000);
    boolean bUseSpatialRanking = false;
    if (rankingOption.equalsIgnoreCase("true")) {
      bUseSpatialRanking = true;
    } else if (rankingOption.equalsIgnoreCase("false")) {
      bUseSpatialRanking = false;
    } else {
   
      // default spatialRelevance.ranking.enabled option is "auto"
      if (this.getQueryAdapter() != null) {
        int maxDoc = this.getQueryAdapter().getMaxDoc();
        if ((maxDoc > 0) && (maxDoc <= rankingMaxDoc)) {
          bUseSpatialRanking = true;
        }
      }
    }
    
    // Handle each operation - Beyond, Crosses, DWithin and Touches are not implemented
    
    if (bUseSpatialRanking) {
      Query spatialQuery = null;
      if (spatialClause instanceof SpatialClause.GeometryBBOXIntersects) {
        spatialQuery = this.makeIntersects();
      } else if (spatialClause instanceof SpatialClause.GeometryContains) {
        spatialQuery = this.makeContains();
      } else if (spatialClause instanceof SpatialClause.GeometryIntersects) {
        spatialQuery = this.makeIntersects();
      } else if (spatialClause instanceof SpatialClause.GeometryIsDisjointTo) {
        bUseSpatialRanking = false;
      } else if (spatialClause instanceof SpatialClause.GeometryIsEqualTo) {
        bUseSpatialRanking = false;
      } else if (spatialClause instanceof SpatialClause.GeometryIsWithin) {
        spatialQuery = this.makeWithin();
      } else if (spatialClause instanceof SpatialClause.GeometryOverlaps) {
        spatialQuery = this.makeIntersects();
      } else {
        sErr = "Unrecognized spatial clause type: ";
        throw new DiscoveryException(sErr+spatialClause.getClass().getName());
      }
      
      if (bUseSpatialRanking) {
        SpatialRankingValueSource srvs = new SpatialRankingValueSource(envelope,queryPower,targetPower);
        Query spatialRankingQuery = new ValueSourceQuery(srvs);
        BooleanQuery bq = new BooleanQuery();
        bq.add(spatialQuery,BooleanClause.Occur.MUST);
        bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
        appendQuery(activeBooleanQuery,activeLogicalClause,bq);
        this.getQueryAdapter().setHasScoredExpression(true);
      }
    }

    if (!bUseSpatialRanking) {
      if (spatialClause instanceof SpatialClause.GeometryBBOXIntersects) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeIntersects());
      } else if (spatialClause instanceof SpatialClause.GeometryContains) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeContains());
      } else if (spatialClause instanceof SpatialClause.GeometryIntersects) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeIntersects());
      } else if (spatialClause instanceof SpatialClause.GeometryIsDisjointTo) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeDisjoint());
      } else if (spatialClause instanceof SpatialClause.GeometryIsEqualTo) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeEquals());
      } else if (spatialClause instanceof SpatialClause.GeometryIsWithin) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeWithin());  
      } else if (spatialClause instanceof SpatialClause.GeometryOverlaps) {
        appendQuery(activeBooleanQuery,activeLogicalClause,makeIntersects());
      } else {
        sErr = "Unrecognized spatial clause type: ";
        throw new DiscoveryException(sErr+spatialClause.getClass().getName());
      }
    }
        
  }
 
  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  private Query makeContains() {
    
    /*
    // the original contains query does not work for envelopes that cross the date line
    // docMinX <= qryMinX, docMinY <= qryMinY, docMaxX >= qryMaxX, docMaxY >= qryMaxY
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,null,qryMinX,false,true);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,null,qryMinY,false,true);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,qryMaxX,null,true,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,qryMaxY,null,true,false);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
    */
    
    // general case
    // docMinX <= qryMinX AND docMinY <= qryMinY AND docMaxX >= qryMaxX AND docMaxY >= qryMaxY
    
    // Y conditions
    // docMinY <= qryMinY AND docMaxY >= qryMaxY
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,null,qryMinY,false,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,qryMaxY,null,true,false);
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.MUST);
    
    // X conditions
    Query xConditions = null;
    
    // queries that do not cross the date line
    if (!qryCrossedDateline) {
      
      // X Conditions for documents that do not cross the date line,
      // documents that contain the min X and max X of the query envelope, 
      // docMinX <= qryMinX AND docMaxX >= qryMaxX    
      Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,null,qryMinX,false,true);
      Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,qryMaxX,null,true,false);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qMinMax);
      
      // X Conditions for documents that cross the date line,
      // the left portion of the document contains the min X of the query
      // OR the right portion of the document contains the max X of the query,
      // docMinXLeft <= qryMinX OR docMaxXRight >= qryMaxX
      Query qXDLLeft = NumericRangeQuery.newDoubleRange(docMinXLeft,null,qryMinX,false,true);
      Query qXDLRight = NumericRangeQuery.newDoubleRange(docMaxXRight,qryMaxX,null,true,false);
      Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.SHOULD);
      Query qXDL = this.makeXDL(true,qXDLLeftRight);
      
      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
      
    // queries that cross the date line
    } else {
      
      // No need to search for documents that do not cross the date line
      
      // X Conditions for documents that cross the date line,
      // the left portion of the document contains the min X of the query
      // AND the right portion of the document contains the max X of the query,
      // docMinXLeft <= qryMinX AND docMaxXRight >= qryMaxX
      Query qXDLLeft = NumericRangeQuery.newDoubleRange(docMinXLeft,null,qryMinX,false,true);
      Query qXDLRight = NumericRangeQuery.newDoubleRange(docMaxXRight,qryMaxX,null,true,false);
      Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.MUST);
      Query qXDL = this.makeXDL(true,qXDLLeftRight);
       
      xConditions = qXDL;
    }
    
    // both X and Y conditions must occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.MUST);
    return xyConditions;
  }
  
  /**
   * Constructs a query to retrieve documents that are disjoint to the input envelope.
   * @return the spatial query
   */
  private Query makeDisjoint() {
    
    /*
    // the original disjoint query does not work for envelopes that cross the date line
    // docMinX > qryMaxX OR docMaxX < qryMinX OR docMinY > qryMaxY OR docMaxY < qryMinY
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,qryMaxX,null,false,false);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMinX,false,false);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,qryMaxY,null,false,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,qryMinY,false,false);    
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.SHOULD);
    bq.add(qMinY,BooleanClause.Occur.SHOULD);
    bq.add(qMaxX,BooleanClause.Occur.SHOULD);
    bq.add(qMaxY,BooleanClause.Occur.SHOULD);
    */
    
    // general case
    // docMinX > qryMaxX OR docMaxX < qryMinX OR docMinY > qryMaxY OR docMaxY < qryMinY
    
    // Y conditions
    // docMinY > qryMaxY OR docMaxY < qryMinY
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,qryMaxY,null,false,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,qryMinY,false,false); 
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.SHOULD);
    
    // X conditions
    Query xConditions = null;
    
    // queries that do not cross the date line
    if (!qryCrossedDateline) {
      
      // X Conditions for documents that do not cross the date line,
      // docMinX > qryMaxX OR docMaxX < qryMinX 
      Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,qryMaxX,null,false,false);
      Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMinX,false,false);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.SHOULD);
      Query qNonXDL = this.makeXDL(false,qMinMax);

      // X Conditions for documents that cross the date line,
      // both the left and right portions of the document must be disjoint to the query
      // (docMinXLeft > qryMaxX OR docMaxXLeft < qryMinX) AND
      // (docMinXRight > qryMaxX OR docMaxXRight < qryMinX) 
      // where: docMaxXLeft = 180.0, docMinXRight = -180.0
      // (docMaxXLeft  < qryMinX) equates to (180.0  < qryMinX) and is ignored
      // (docMinXRight > qryMaxX) equates to (-180.0 > qryMaxX) and is ignored
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(docMinXLeft,qryMaxX,null,false,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(docMaxXRight,null,qryMinX,false,false);
      Query qLeftRight = this.makeQuery(new Query[]{qMinXLeft,qMaxXRight},BooleanClause.Occur.MUST);
      Query qXDL = this.makeXDL(true,qLeftRight);
      
      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
      
    // queries that cross the date line
    } else {
      
      // X Conditions for documents that do not cross the date line,
      // the document must be disjoint to both the left and right query portions
      // (docMinX > qryMaxXLeft OR docMaxX < qryMinX) AND (docMinX > qryMaxX OR docMaxX < qryMinXLeft) 
      // where: qryMaxXLeft = 180.0, qryMinXLeft = -180.0
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(docMinX,180.0,null,false,false);
      Query qMaxXLeft = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMinX,false,false);
      Query qMinXRight = NumericRangeQuery.newDoubleRange(docMinX,qryMaxX,null,false,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(docMaxX,null,-180.0,false,false);
      Query qLeft = this.makeQuery(new Query[]{qMinXLeft,qMaxXLeft},BooleanClause.Occur.SHOULD);
      Query qRight = this.makeQuery(new Query[]{qMinXRight,qMaxXRight},BooleanClause.Occur.SHOULD);
      Query qLeftRight = this.makeQuery(new Query[]{qLeft,qRight},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qLeftRight);
      
      // No need to search for documents that do not cross the date line
      
      xConditions = qNonXDL;
    }
        
    // either X or Y conditions should occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.SHOULD);
    return xyConditions;
  }
  
  /**
   * Constructs a query to retrieve documents that equal the input envelope.
   * @return the spatial query
   */
  private Query makeEquals() {
    
    // docMinX = qryMinX AND docMinY = qryMinY AND docMaxX = qryMaxX AND docMaxY = qryMaxY
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,qryMinX,qryMinX,true,true);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,qryMinY,qryMinY,true,true);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,qryMaxX,qryMaxX,true,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,qryMaxY,qryMaxY,true,true);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
  }
  
  /**
   * Constructs a query to retrieve documents that intersect the input envelope.
   * @return the spatial query
   */
  private Query makeIntersects() { 
    
    // the original intersects query does not work for envelopes that cross the date line,
    // switch to a NOT Disjoint query
        
    // MUST_NOT causes a problem when it's the only clause type within a BooleanQuery,
    // to get round it we add all documents as a SHOULD
    
    // there must be an envelope, it must not be disjoint
    Query qDisjoint = makeDisjoint();
    Query qIsNonXDL = this.makeXDL(false);
    Query qIsXDL = this.makeXDL(true);
    Query qHasEnv = this.makeQuery(new Query[]{qIsNonXDL,qIsXDL},BooleanClause.Occur.SHOULD);
    BooleanQuery qNotDisjoint = new BooleanQuery();
    qNotDisjoint.add(qHasEnv,BooleanClause.Occur.MUST);
    qNotDisjoint.add(qDisjoint,BooleanClause.Occur.MUST_NOT);
    
    //Query qDisjoint = makeDisjoint();
    //BooleanQuery qNotDisjoint = new BooleanQuery();
    //qNotDisjoint.add(new MatchAllDocsQuery(),BooleanClause.Occur.SHOULD);
    //qNotDisjoint.add(qDisjoint,BooleanClause.Occur.MUST_NOT);
    return qNotDisjoint;
  }
  
  /**
   * Makes a boolean query based upon a collection of queries and a logical operator.
   * @param queries the query collection
   * @param occur the logical operator
   * @return the query
   */
  private BooleanQuery makeQuery(Query[] queries, BooleanClause.Occur occur) {
    BooleanQuery bq = new BooleanQuery();
    for (Query query: queries) {
      bq.add(query,occur);
    }
    return bq;
  }
  
  /**
   * Constructs a query to retrieve documents are fully within the input envelope.
   * @return the spatial query
   */
  private Query makeWithin() {
    
    /*
    // the original within query does not work for envelopes that cross the date line
    // docMinX >= qryMinX AND docMinY >= qryMinY AND docMaxX <= qryMaxX AND docMaxY <= qryMaxY
    Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,qryMinX,null,true,false);
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,qryMinY,null,true,false);
    Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMaxX,false,true);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,qryMaxY,false,true);
    BooleanQuery bq = new BooleanQuery();
    bq.add(qMinX,BooleanClause.Occur.MUST);
    bq.add(qMinY,BooleanClause.Occur.MUST);
    bq.add(qMaxX,BooleanClause.Occur.MUST);
    bq.add(qMaxY,BooleanClause.Occur.MUST);
    return bq;
    */
    
    // general case
    // docMinX >= qryMinX AND docMinY >= qryMinY AND docMaxX <= qryMaxX AND docMaxY <= qryMaxY
    
    // Y conditions
    // docMinY >= qryMinY AND docMaxY <= qryMaxY
    Query qMinY = NumericRangeQuery.newDoubleRange(docMinY,qryMinY,null,true,false);
    Query qMaxY = NumericRangeQuery.newDoubleRange(docMaxY,null,qryMaxY,false,true);
    Query yConditions = this.makeQuery(new Query[]{qMinY,qMaxY},BooleanClause.Occur.MUST);
    
    // X conditions
    Query xConditions = null;
    
    // X Conditions for documents that cross the date line,
    // the left portion of the document must be within the left portion of the query,
    // AND the right portion of the document must be within the right portion of the query
    // docMinXLeft >= qryMinX AND docMaxXLeft <= 180.0 
    // AND docMinXRight >= -180.0 AND docMaxXRight <= qryMaxX
    Query qXDLLeft = NumericRangeQuery.newDoubleRange(docMinXLeft,qryMinX,null,true,false);
    Query qXDLRight = NumericRangeQuery.newDoubleRange(docMaxXRight,null,qryMaxX,false,true);
    Query qXDLLeftRight = this.makeQuery(new Query[]{qXDLLeft,qXDLRight},BooleanClause.Occur.MUST);
    Query qXDL  = this.makeXDL(true,qXDLLeftRight);
    
    // queries that do not cross the date line
    if (!qryCrossedDateline) {
     
      // X Conditions for documents that do not cross the date line,
      // docMinX >= qryMinX AND docMaxX <= qryMaxX
      Query qMinX = NumericRangeQuery.newDoubleRange(docMinX,qryMinX,null,true,false);
      Query qMaxX = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMaxX,false,true);
      Query qMinMax = this.makeQuery(new Query[]{qMinX,qMaxX},BooleanClause.Occur.MUST);
      Query qNonXDL = this.makeXDL(false,qMinMax);
      
      // apply the non-XDL or XDL X conditions
      if ((qryMinX <= -180.0) && qryMaxX >= 180.0) {
        xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
      } else {
        xConditions = qNonXDL;
      }
      
    // queries that cross the date line
    } else {
      
      // X Conditions for documents that do not cross the date line
      
      // the document should be within the left portion of the query
      // docMinX >= qryMinX AND docMaxX <= 180.0
      Query qMinXLeft = NumericRangeQuery.newDoubleRange(docMinX,qryMinX,null,true,false);
      Query qMaxXLeft = NumericRangeQuery.newDoubleRange(docMaxX,null,180.0,false,true);
      Query qLeft = this.makeQuery(new Query[]{qMinXLeft,qMaxXLeft},BooleanClause.Occur.MUST);
      
      // the document should be within the right portion of the query
      // docMinX >= -180.0 AND docMaxX <= qryMaxX
      Query qMinXRight = NumericRangeQuery.newDoubleRange(docMinX,-180.0,null,true,false);
      Query qMaxXRight = NumericRangeQuery.newDoubleRange(docMaxX,null,qryMaxX,false,true);
      Query qRight = this.makeQuery(new Query[]{qMinXRight,qMaxXRight},BooleanClause.Occur.MUST);
      
      // either left or right conditions should occur,
      // apply the left and right conditions to documents that do not cross the date line
      Query qLeftRight = this.makeQuery(new Query[]{qLeft,qRight},BooleanClause.Occur.SHOULD);
      Query qNonXDL = this.makeXDL(false,qLeftRight);
      
      // apply the non-XDL and XDL conditions
      xConditions = this.makeQuery(new Query[]{qNonXDL,qXDL},BooleanClause.Occur.SHOULD);
    }
    
    // both X and Y conditions must occur
    Query xyConditions = this.makeQuery(new Query[]{xConditions,yConditions},BooleanClause.Occur.MUST);
    return xyConditions;
  }
  
  /**
   * Constructs a query to retrieve documents that do or do not cross the date line. 
   * @param crossedDateLine <code>true</true> for documents that cross the date line
   * @return the query
   */
  private Query makeXDL(boolean crossedDateLine) {
    return new TermQuery(new Term(docXDL,""+crossedDateLine));
  }
  
  /**
   * Constructs a query to retrieve documents that do or do not cross the date line
   * and match the supplied spatial query. 
   * @param crossedDateLine <code>true</true> for documents that cross the date line
   * @param query the spatial query
   * @return the query
   */
  private Query makeXDL(boolean crossedDateLine, Query query) {
    BooleanQuery bq = new BooleanQuery();
    bq.add(this.makeXDL(crossedDateLine),BooleanClause.Occur.MUST);
    bq.add(query,BooleanClause.Occur.MUST);
    return bq;
  }  
    
}
