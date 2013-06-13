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
import com.esri.gpt.catalog.schema.indexable.tp.TpUtil;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Adapts a timeperiod based PropertyClause to the Lucene model.
 */
public class TimeperiodClauseAdapter extends PropertyClauseAdapter {
    
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TimeperiodClauseAdapter.class.getName());
  
  /** instance variables ====================================================== */
  private String    baseFieldName;
  private boolean   inclusive = true;
  private int       maxIntervalFieldName;
  private String    intervalMetaFieldName;
  private String    multiplicityFieldName;
  private Long      now = System.currentTimeMillis();
  private int       precisionStep = 4; 
  private String    summaryMetaFieldName;
  
  private boolean   queryIntersectsNow;
  private boolean   queryIsAfterNow;
  private boolean   queryIsBeforeNow;
  private Long      queryLower;
  private Long      queryUpper;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated query adapter.
   * @param queryAdapter the query adapter
   */
  protected TimeperiodClauseAdapter(LuceneQueryAdapter queryAdapter) {
    super(queryAdapter);
  }
  
  /** properties ============================================================== */
  
  /**
   * Determines if range boundaries inclusive or exclusive.
   * <br/>[] inclusive -> intersects
   * <br/>{} exclusive -> within
   * @return true if inclusive
   */
  public boolean getInclusive() {
    return this.inclusive;
  }
  /**
   * Determines if range boundaries inclusive or exclusive.
   * <br/>[] inclusive -> intersects
   * <br/>{} exclusive -> within
   * @param inclusive true if inclusive
   */
  public void setInclusive(boolean inclusive) {
    this.inclusive = inclusive;
  }
    
  /** methods ================================================================= */
 
  /**
   * Adapts a timeperiod based PropertyClause to the Lucene model.
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
    LOGGER.finer("Adapting timeperiod PropertyClause...\n"+propertyClause);
    
    // determine the discoverable target, set the underlying storable
    Discoverable discoverable = propertyClause.getTarget();
    if (discoverable == null) {
      String sErr = "The PropertyClause.target is null.";
      throw new DiscoveryException(sErr);
    }
    if (discoverable.getStorable() == null) {
      String sErr = "The PropertyClause.target.storeable is null.";
      throw new DiscoveryException(sErr);
    } else {
      Storeable storeable = (Storeable)discoverable.getStorable();
      this.baseFieldName = storeable.getName();
      if (this.baseFieldName.endsWith(".intersects")) {
        this.inclusive = true;
        this.baseFieldName = this.baseFieldName.substring(0,this.baseFieldName.length()-11);
      } else if (this.baseFieldName.endsWith(".within")) {
        this.inclusive = false;
        this.baseFieldName = this.baseFieldName.substring(0,this.baseFieldName.length()-7);
      }
      this.intervalMetaFieldName = this.baseFieldName+".imeta";
      this.multiplicityFieldName = this.baseFieldName+".num";
      this.summaryMetaFieldName = this.baseFieldName+".meta";
    }
    
    // initialize values
    boolean bInclusive = this.inclusive;
    String sLiteral = Val.chkStr(propertyClause.getLiteral());
    String sLower = "";
    String sUpper = "";
    String sErr = null;
    String sErrSfx = " is not supported for timeperiod fields,"+
                     " use PropertyIsBetween.";
    
    if (propertyClause instanceof PropertyIsBetween) {
      PropertyIsBetween between = (PropertyIsBetween)propertyClause;
      sLower = Val.chkStr(between.getLowerBoundary());
      sUpper = Val.chkStr(between.getUpperBoundary());
      this.queryLower = this.parseDateTime(sLower,false);
      this.queryUpper = this.parseDateTime(sUpper,true);
      
    } else if ((propertyClause instanceof PropertyIsEqualTo)  ||
               (propertyClause instanceof PropertyIsNotEqualTo)) {
      Query q = null;
      sLower = Val.chkStr(sLiteral);
      sUpper = Val.chkStr(sLiteral);
      this.queryLower = this.parseDateTime(sLower,false);
      if (this.queryLower == null) {
       sErr = "Timeperiod literal cannot be null for PropertyIsEqualTo/PropertyIsNotEqualTo";
      } else {
       this.queryUpper = this.parseDateTime(sUpper,true);
       if (propertyClause instanceof PropertyIsEqualTo) {
         q = this.makeEquals();
       } else {
         q = this.makeNotEquals();
       }
       appendQuery(activeBooleanQuery,activeLogicalClause,q);
       return;
      }
      
    } else if (propertyClause instanceof PropertyIsGreaterThan) {
      bInclusive = false; // use within logic
      sLower = sLiteral;
      this.queryLower = this.parseDateTime(sLower,false);
      if (this.queryLower != null) {
        this.queryLower = new Long(this.queryLower.longValue() + 1);
      }
      
    } else if (propertyClause instanceof PropertyIsGreaterThanOrEqualTo) {
      bInclusive = false; // use within logic
      sLower = sLiteral;
      this.queryLower = this.parseDateTime(sLower,false);
      
    } else if (propertyClause instanceof PropertyIsLessThan) {
      bInclusive = false; // use within logic
      sUpper = sLiteral;
      this.queryUpper = this.parseDateTime(sUpper,false);
      if (this.queryUpper != null) {
        this.queryUpper = new Long(this.queryUpper.longValue() - 1);
      }
      
    } else if (propertyClause instanceof PropertyIsLessThanOrEqualTo) {
      bInclusive = false; // use within logic
      sUpper = sLiteral;
      this.queryUpper = this.parseDateTime(sUpper,true);
      
    } else if (propertyClause instanceof PropertyIsNull) {
      appendQuery(activeBooleanQuery,activeLogicalClause,this.makeNull());
      return;
      
    } else if (propertyClause instanceof PropertyIsLike) {  
      sErr = "PropertyIsLike"+sErrSfx;
      
    } else {
      sErr = "Unrecognized property clause type: "+propertyClause.getClass().getName();
    }
    if (sErr != null) {
      throw new DiscoveryException(sErr);
    }
    
    // check for upper < lower
    if ((this.queryLower != null) && (this.queryUpper != null)) {
      if (this.queryUpper.longValue() < this.queryLower.longValue()) {
        appendQuery(activeBooleanQuery,activeLogicalClause,new BooleanQuery());
        return;
      }
    }
    
    // could implement a timeperiod relevance ranking here
    if ((this.queryLower != null) && (this.queryUpper != null)) {}
    
    if (bInclusive) {
      this.determineRelationshipWithNow();
      this.determineMaxIntervalFieldName();
      appendQuery(activeBooleanQuery,activeLogicalClause,makeIntersects());
    } else {
      this.determineRelationshipWithNow();
      appendQuery(activeBooleanQuery,activeLogicalClause,makeWithin());
    }
  }
  
  /**
   * Determine the index for the highest interval field within the Lucene index.
   * <br/>e.g. timeperiod.l.7
   * <br/>If the the document with the most intervals has 7, then 7 is the max.
   * @throws DiscoveryException if there is a problem accessing the index
   */
  private void determineMaxIntervalFieldName() throws DiscoveryException {
    IndexSearcher searcher = null;
    try {
      searcher = this.getQueryAdapter().getIndexAdapter().newSearcher();
      IndexReader reader = searcher.getIndexReader();
      Collection<String> names = reader.getFieldNames(FieldOption.ALL);
      String sPfx = this.baseFieldName.toLowerCase()+".l.";
      int nBeginSubstring = sPfx.length();
      int nMax = -1;
      for (String name: names) {
        String lc = name.toLowerCase();
        if (lc.startsWith(sPfx)) {
          LOGGER.finest("Found boundary field: "+name);
          String s = lc.substring(nBeginSubstring);
          try {
            int n = Integer.valueOf(s);
            if (n > nMax) {
              nMax = n;
            }
          } catch (NumberFormatException nfe) {}
        }
      }
      LOGGER.finest("MaxBndFieldIndex: "+nMax);
      this.maxIntervalFieldName = nMax;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE,"Index issue.",e);
      throw new DiscoveryException(e.toString(),e);
    } finally {
      this.getQueryAdapter().getIndexAdapter().closeSearcher(searcher);
    }
  }
  
  /**
   * Determines if the query bounds are before after or intersecting with now.
   */
  private void determineRelationshipWithNow() {
    long nNow = this.now;
    if ((this.queryLower != null) && 
        (this.queryLower.longValue() > nNow)) {
      this.queryIsAfterNow = true;
    } else if ((this.queryUpper != null) &&
        (this.queryUpper.longValue() < nNow)) {
      this.queryIsBeforeNow = true;
    } else {
      this.queryIntersectsNow = true;
    }
  }
  
  /**
   * Makes the lower boundary field name associated with an interval index.
   * <br/>Interval 0 is the summary interval for the document.
   * @param interval the interval index
   * @return the name
   */
  private String getLowerFieldName(int interval) {
    //if (interval == 0) return this.baseFieldName+".l.d";
    //else return this.baseFieldName+".l."+interval;
    return this.baseFieldName+".l."+interval;
  }
  
  /**
   * Makes the meta value associated with an interval index.
   * <br/>Interval 0 is the summary interval for the document.
   * @param type the value predicate
   * @param interval the interval index
   * @return the name
   */
  private String getMetaValue(String type, int interval) {
    //if (interval == 0) return type+".d";
    //else return type+"."+interval;
    return type+"."+interval;
    
  }

  /**
   * Makes the upper boundary field name associated with an interval index.
   * <br/>Interval 0 is the summary interval for the document.
   * @param interval the interval index
   * @return the name
   */
  private String getUpperFieldName(int interval) {
    //if (interval == 0) return this.baseFieldName+".u.d";
    //else return this.baseFieldName+".u."+interval;
    return this.baseFieldName+".u."+interval;
  }
  
  /**
   * Constructs a query for documents that are equal to the 
   * input time period.
   * @return the query
   */
  private Query makeEquals() {
    
    /**
     * one determinate and boundaries are equal
     */
    int nStep = this.precisionStep;
    String fSMeta = this.summaryMetaFieldName;
    String fLower = this.getLowerFieldName(0);
    String fUpper = this.getUpperFieldName(0);
    
    String sMeta = "is1determinate";
    Query qIs1Determinate = new TermQuery(new Term(fSMeta,sMeta));
    Query qDocLowerEq = NumericRangeQuery.newLongRange(
          fLower,nStep,queryLower,queryLower,true,true);
    Query qDocUpperEq = NumericRangeQuery.newLongRange(
          fUpper,nStep,queryUpper,queryUpper,true,true);
    
    BooleanQuery bq = new BooleanQuery();
    bq.add(qIs1Determinate,BooleanClause.Occur.MUST);
    bq.add(qDocLowerEq,BooleanClause.Occur.MUST);
    bq.add(qDocUpperEq,BooleanClause.Occur.MUST);
    return bq;
  }
  
  /**
   * Constructs a query for documents that intersect the input time period.
   * @return the query
   */
  private Query makeIntersects() {
    BooleanQuery bq = new BooleanQuery();
    for (int i=1;i<=this.maxIntervalFieldName;i++) {
      Query q = this.makeIntersectsInterval(i);
      bq.add(q,BooleanClause.Occur.SHOULD);
    }
    return bq;
  }
  
  /**
   * Constructs a query for a document interval that intersects 
   * the input time period.
   * @param interval the field name index for the interval
   * @return the query
   */
  private Query makeIntersectsInterval(int interval) {
    /*
      Intersects:
      
           docMinIn:    fMin >= qMin AND fMin <= qMax
        OR docMaxIn:    fMax >= qMin AND fMax <= qMax
        OR docContains: fMin <= qMin AND fMax >= qMax
    */
    
    int nStep = this.precisionStep;
    String fMeta = this.intervalMetaFieldName;
    String fLower = this.getLowerFieldName(interval);
    String fUpper = this.getUpperFieldName(interval);
    
    Query qDocLowerIn = NumericRangeQuery.newLongRange(
          fLower,nStep,queryLower,queryUpper,true,true);
    Query qDocUpperIn = NumericRangeQuery.newLongRange(
          fUpper,nStep,queryLower,queryUpper,true,true);
    
    BooleanQuery qDocContains = new BooleanQuery();
    Query qLowerBeforeL = NumericRangeQuery.newLongRange(
        fLower,nStep,null,queryLower,true,true);
    Query qLowerBeforeU = NumericRangeQuery.newLongRange(
        fLower,nStep,null,queryUpper,true,true);
    Query qUpperAfterL = NumericRangeQuery.newLongRange(
        fUpper,nStep,queryLower,null,true,true);
    Query qUpperAfterU = NumericRangeQuery.newLongRange(
        fUpper,nStep,queryUpper,null,true,true);
    qDocContains.add(qLowerBeforeL,BooleanClause.Occur.MUST);
    qDocContains.add(qLowerBeforeU,BooleanClause.Occur.MUST);
    qDocContains.add(qUpperAfterL,BooleanClause.Occur.MUST);
    qDocContains.add(qUpperAfterU,BooleanClause.Occur.MUST);
    
    BooleanQuery qIntervalIn = new BooleanQuery();
    qIntervalIn.add(qDocLowerIn,BooleanClause.Occur.SHOULD);
    qIntervalIn.add(qDocUpperIn,BooleanClause.Occur.SHOULD);
    qIntervalIn.add(qDocContains,BooleanClause.Occur.SHOULD);
    
    String sMeta = this.getMetaValue("determinate",interval);
    Query qIsDeterminate = new TermQuery(new Term(fMeta,sMeta));
    
    BooleanQuery bqDeterminate = new BooleanQuery();
    bqDeterminate.add(qIsDeterminate,BooleanClause.Occur.MUST);
    bqDeterminate.add(qIntervalIn,BooleanClause.Occur.MUST);
        
    // intervals that intersect now
    BooleanQuery bqNow = new BooleanQuery();
    if (this.queryIntersectsNow) {
      // any interval with the following meta terms intersects:
      // now.i now.l.i now.u.i where i is the interval index (1 based)
      String s1 = this.getMetaValue("now",interval);
      String s2 = this.getMetaValue("now.l",interval);
      String s3 = this.getMetaValue("now.u",interval);
      Query q1 = new TermQuery(new Term(fMeta,s1));
      Query q2 = new TermQuery(new Term(fMeta,s2));
      Query q3 = new TermQuery(new Term(fMeta,s3));
      bqNow.add(q1,BooleanClause.Occur.SHOULD);
      bqNow.add(q2,BooleanClause.Occur.SHOULD);
      bqNow.add(q3,BooleanClause.Occur.SHOULD);      
    } else if (this.queryIsBeforeNow) {
      // meta term now.u.i and fLower must be <= queryUpper
      String s1 = this.getMetaValue("now.u",interval);
      Query q1 = new TermQuery(new Term(fMeta,s1));
      Query q2 = NumericRangeQuery.newLongRange(
                 fLower,nStep,null,queryUpper,true,true);
      bqNow.add(q1,BooleanClause.Occur.MUST);
      bqNow.add(q2,BooleanClause.Occur.MUST);
    } else if (this.queryIsAfterNow) {
      // meta term now.l.i and fUpper must be >= queryLower
      String s1 = this.getMetaValue("now.l",interval);
      Query q1 = new TermQuery(new Term(fMeta,s1));
      Query q2 = NumericRangeQuery.newLongRange(
                 fUpper,nStep,queryLower,null,true,true);
      bqNow.add(q1,BooleanClause.Occur.MUST);
      bqNow.add(q2,BooleanClause.Occur.MUST);
    }

    BooleanQuery bq = new BooleanQuery();
    bq.add(bqDeterminate,BooleanClause.Occur.SHOULD);
    bq.add(bqNow,BooleanClause.Occur.SHOULD);
    
    return bq;
  }
  
  /**
   * Constructs a query for documents that are not
   * equal to the input time period.
   * @return the query
   */
  private Query makeNotEquals() {    
    Query qEquals = this.makeEquals();
    BooleanQuery qNotEquals  = new BooleanQuery();
    qNotEquals.add(new MatchAllDocsQuery(),BooleanClause.Occur.SHOULD);
    qNotEquals.add(qEquals,BooleanClause.Occur.MUST_NOT);
    return qNotEquals;
  }
  
  /**
   * Constructs a query for documents that have a null time period.
   * @return the query
   */
  private Query makeNull() {
    int nStep = this.precisionStep;
    Query qHasIntervals = NumericRangeQuery.newLongRange(
        this.multiplicityFieldName,nStep,1L,null,true,true);
    BooleanQuery qNull  = new BooleanQuery();
    qNull.add(new MatchAllDocsQuery(),BooleanClause.Occur.SHOULD);
    qNull.add(qHasIntervals,BooleanClause.Occur.MUST_NOT);
    return qNull;
  }
  
  /**
   * Constructs a query for documents that are within the input time period.
   * @return the query
   */
  private Query makeWithin() {
    BooleanQuery bq = new BooleanQuery();
    Query q = this.makeWithinInterval(0);
    bq.add(q,BooleanClause.Occur.MUST);
    return bq;
  }
  
  /**
   * Constructs a query for a document interval that is within 
   * the input time period.
   * @param interval the field name index for the interval
   * @return the query
   */
  private Query makeWithinInterval(int interval) {
    
    // Within: docMin >= qryMin AND docMax <= qryMax
        
    int nStep = this.precisionStep;
    String fMeta = this.intervalMetaFieldName;
    String fLower = this.getLowerFieldName(interval);
    String fUpper = this.getUpperFieldName(interval);
    
    Query qDocLowerWithin = NumericRangeQuery.newLongRange(
          fLower,nStep,queryLower,queryUpper,true,true);
    Query qDocUpperWithin = NumericRangeQuery.newLongRange(
          fUpper,nStep,queryLower,queryUpper,true,true);
    
    BooleanQuery qIntervalWithin = new BooleanQuery();
    qIntervalWithin.add(qDocLowerWithin,BooleanClause.Occur.MUST);
    qIntervalWithin.add(qDocUpperWithin,BooleanClause.Occur.MUST);
    
    String sMeta = this.getMetaValue("determinate",interval);
    Query qIsDeterminate = new TermQuery(new Term(fMeta,sMeta));
    
    BooleanQuery bqDeterminate = new BooleanQuery();
    bqDeterminate.add(qIsDeterminate,BooleanClause.Occur.MUST);
    bqDeterminate.add(qIntervalWithin,BooleanClause.Occur.MUST);
    
    // intervals that intersect now
    BooleanQuery bqNow = null;
    
    if (this.queryIntersectsNow) {

      // meta term now.i and is within
      String s1 = this.getMetaValue("now",interval);
      Query q1 = new TermQuery(new Term(fMeta,s1));
      
      // meta term now.l.i and fUpper must be <= queryUpper
      String s2 = this.getMetaValue("now.l",interval);
      Query qM2 = new TermQuery(new Term(fMeta,s2));
      Query qI2 = NumericRangeQuery.newLongRange(
                  fUpper,nStep,null,queryUpper,true,true);
      BooleanQuery q2 = new BooleanQuery();
      q2.add(qM2,BooleanClause.Occur.MUST);
      q2.add(qI2,BooleanClause.Occur.MUST);
      
      // meta term now.u.i and fLower must be >= queryLower
      String s3 = this.getMetaValue("now.u",interval);
      Query qM3 = new TermQuery(new Term(fMeta,s3));
      Query qI3 = NumericRangeQuery.newLongRange(
                  fLower,nStep,queryLower,null,true,true); 
      BooleanQuery q3 = new BooleanQuery();
      q3.add(qM3,BooleanClause.Occur.MUST);
      q3.add(qI3,BooleanClause.Occur.MUST);
      
      bqNow = new BooleanQuery();
      bqNow.add(q1,BooleanClause.Occur.SHOULD);
      bqNow.add(q2,BooleanClause.Occur.SHOULD);
      bqNow.add(q3,BooleanClause.Occur.SHOULD);      
    } else if (this.queryIsBeforeNow) {
      // not within
    } else if (this.queryIsAfterNow) {
      // not within
    }
    
    if (bqNow == null) {
      return bqDeterminate;
    } else {
      BooleanQuery bq = new BooleanQuery();
      bq.add(bqDeterminate,BooleanClause.Occur.SHOULD);
      bq.add(bqNow,BooleanClause.Occur.SHOULD);
      return bq;
    }
  }  
  
  /**
   * Parses a date/time string.
   * @param dateTime the date/time
   * @param isUpper true if this is an upper boundary
   * @return the corresponding time in millis
   * @throws IllegalArgumentException if the input does not conform
   */
  private Long parseDateTime(String dateTime, boolean isUpper) {
    dateTime = Val.chkStr(dateTime);
    String lc = dateTime.toLowerCase();
    if (lc.equals("*")) {
      return null;
    } else if (lc.equals("now") || lc.equals("present")) {
      return new Long(this.now);
    } else if (lc.equals("unknown")) { 
      return null;
    } else {
      
      Calendar calendar = null;
      String s = dateTime;
      if (s.startsWith("-")) s = s.substring(1);
      if (s.length() >= "1000000000".length()) {
        boolean bChkMillis = true;
        char[] ca = s.toCharArray();
        for (char c: ca) {
          if (!Character.isDigit(c)) {
            bChkMillis = false;
            break;
          }
        }
        if (bChkMillis) {
          try {
            long l = Long.valueOf(dateTime);
            calendar = new GregorianCalendar();
            calendar.setTimeInMillis(l);
          } catch (NumberFormatException nfe) {
            calendar = null;
          }
        }
      }
      if (calendar == null) {
        calendar = TpUtil.parseIsoDateTime(dateTime);
      }
      if (isUpper) {
        TpUtil.advanceToUpperBoundary(calendar,dateTime);
      }
      if (LOGGER.isLoggable(Level.FINER)) {
        String sMsg = dateTime+" -> "+calendar.getTimeInMillis()+" "+
                      TpUtil.printIsoDateTime(calendar);
        LOGGER.finer(sMsg);
      }
      return new Long(calendar.getTimeInMillis());
    }
  }
  
}
