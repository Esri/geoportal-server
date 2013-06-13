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
import com.esri.gpt.catalog.schema.indexable.tp.TpInterval;
import com.esri.gpt.catalog.schema.indexable.tp.TpIntervals;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

/**
 * Represents the time periods associated with a document.
 */
public class TimeperiodProperty extends Storeable {
  
  /** class variables ========================================================= */
    
  /** The Logger */
  private static Logger LOGGER = Logger.getLogger(TimeperiodProperty.class.getName());
  
  /** instance variables ====================================================== */
  private String intervalMetaFieldName;
  private String multiplicityFieldName;
  private String summaryMetaFieldName;
  private int    precisionStep = 4; 
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the property name
   */
  public TimeperiodProperty(String name) {
    super(name);
    this.intervalMetaFieldName = this.getName()+".imeta";
    this.multiplicityFieldName = this.getName()+".num";
    this.summaryMetaFieldName = this.getName()+".meta";
  }
      
  /** methods ================================================================= */
  
  /**
   * Appends underlying fields to a document prior to writing the 
   * document to the index.
   * @param document the Lucene document
   */
  @Override
  public void appendForWrite(Document document) {
    
    // get the analyzed time period intervals
    boolean bWasInvalid = false;
    TpIntervals intervals = null;
    Object value = null;
    if ((getValues() != null) && (getValues().length == 1)) {
      value = getValues()[0];
      if (value instanceof TpIntervals) {
        intervals = (TpIntervals)value;
      }
    }
    if (intervals == null) {
      intervals = new TpIntervals();
      bWasInvalid = true;
    }
    if (intervals.size() > 1) intervals.sort();

    long nNow = System.currentTimeMillis();
    long nLower = Long.MAX_VALUE;
    long nUpper = Long.MIN_VALUE;
    int nInterval = 0;
    boolean bHasUnknown = false;
    boolean bHasNow = false;
    boolean bHasLowerNow = false;
    boolean bHasUpperNow = false;
    boolean bHasDeterminate = false;
    boolean bLastWasUpperNow = false;
    TpInterval firstInterval = null;
    List<String> lIMetaTerms = new ArrayList<String>();
    List<String> lSMetaTerms = new ArrayList<String>();
    ArrayList<Fieldable> lFields = new ArrayList<Fieldable>();
    
    int nIntervals = intervals.size();
    int nLastInterval = (nIntervals - 1);
    for (int i=0;i<nIntervals;i++) {
      boolean bLastInterval = (i == nLastInterval);
      TpInterval interval = intervals.get(i);      
      String sCurInd = Val.chkStr(interval.getIndeterminate());
      
      if (sCurInd.equals("unknown")) {
        bHasUnknown = true;
      } else {
        
        nInterval++;
        String sLowerField = this.getLowerFieldName(nInterval);
        String sUpperField = this.getUpperFieldName(nInterval); 
        long nCurLower = interval.getLower().longValue();
        long nCurUpper = interval.getUpper().longValue();
        lFields.add(this.makeBoundaryField(sLowerField,nCurLower));
        lFields.add(this.makeBoundaryField(sUpperField,nCurUpper));
        if (firstInterval == null) {
          firstInterval = interval;
        }
        
        if (sCurInd.equals("now")) {
          bHasNow = true;
          lIMetaTerms.add(this.getMetaValue("now",nInterval));
        } else if (sCurInd.equals("now.lower")) {
          bHasLowerNow = true;
          lIMetaTerms.add(this.getMetaValue("now.l",nInterval));
          if (nCurUpper > nUpper) {
            nUpper = nCurUpper;
          }
        } else if (sCurInd.equals("now.upper")) {
          bHasUpperNow = true;
          if (bLastInterval) bLastWasUpperNow = true;
          lIMetaTerms.add(this.getMetaValue("now.u",nInterval));
          if (nCurLower < nLower) {
            nLower = nCurLower;
          }
        } else {
          bHasDeterminate = true;
          lIMetaTerms.add(this.getMetaValue("determinate",nInterval));
          if (nCurLower < nLower) {
            nLower = nCurLower;
          }
          if (nCurUpper > nUpper) {
            nUpper = nCurUpper;
          }
        }
      }
    }  
    
    int nMultiplicity = lFields.size() / 2;
    boolean bIsUnknown = bHasUnknown && (nMultiplicity == 0);
    boolean bIsDeterminate = bHasDeterminate && !bHasUnknown && 
                             !bHasNow && !bHasLowerNow && !bHasUpperNow;
    boolean bIs1Determinate = bIsDeterminate && (nMultiplicity == 1);
    boolean bIsIndeterminate = bHasNow || bHasLowerNow || bHasUpperNow;
    boolean bIs1Indeterminate = bIsIndeterminate && (nMultiplicity == 1);
    boolean bWasEmpty = !bWasInvalid && (nIntervals == 0);
    if (bIsDeterminate)    lSMetaTerms.add("isDeterminate");
    if (bIs1Determinate)   lSMetaTerms.add("is1Determinate");
    if (bIsIndeterminate)  lSMetaTerms.add("isIndeterminate");
    if (bIs1Indeterminate) lSMetaTerms.add("is1Indeterminate");
    if (bIsUnknown)        lSMetaTerms.add("isUnknown");
    if (bHasDeterminate)   lSMetaTerms.add("hasDeterminate");
    if (bHasUnknown)       lSMetaTerms.add("hasUnknown");
    if (bHasNow)           lSMetaTerms.add("hasNow");
    if (bHasLowerNow)      lSMetaTerms.add("hasLowerNow");
    if (bHasUpperNow)      lSMetaTerms.add("hasUpperNow");
    if (bWasInvalid)       lSMetaTerms.add("wasInvalid");
    if (bWasEmpty)         lSMetaTerms.add("wasEmpty");
    
    // make the summary interval (for the document minLower->maxUpper)
    nInterval = 0;
    String sLowerField = this.getLowerFieldName(nInterval);
    String sUpperField = this.getUpperFieldName(nInterval); 
    if (bIsDeterminate) {
      String sMeta = "determinate";
      lFields.add(0,this.makeBoundaryField(sUpperField,nUpper));
      lFields.add(0,this.makeBoundaryField(sLowerField,nLower));
      lIMetaTerms.add(0,this.getMetaValue(sMeta,nInterval));
    } else if (nMultiplicity == 1) {
      TpInterval interval = firstInterval;
      String sCurInd = Val.chkStr(interval.getIndeterminate());
      String sMeta = null;
      if (sCurInd.equals("now")) {
        sMeta = "now";
      } else if (sCurInd.equals("now.lower")) {
        sMeta = "now.l";
      } else if (sCurInd.equals("now.upper")) {
        sMeta = "now.u";
      }
      if (sMeta != null) {
        lFields.add(0,this.makeBoundaryField(sUpperField,nUpper));
        lFields.add(0,this.makeBoundaryField(sLowerField,nLower));
        lIMetaTerms.add(0,this.getMetaValue(sMeta,nInterval));
      }
    } else if (nMultiplicity > 1) {
                  
      // check for a sequential set of intervals ending with now
      // e.g. 2008-08-01..2009-08-31 , 2009-09-01..2010-04-15 , 2010-04-16..now
      boolean bUseUpperNowSummary = false;
      if (!bHasUnknown && !bHasNow && !bHasLowerNow && 
           bHasUpperNow && bLastWasUpperNow) {
        if ((nLower != Long.MIN_VALUE) && (nLower != Long.MAX_VALUE)) {
          if ((nUpper != Long.MIN_VALUE) && (nUpper != Long.MAX_VALUE)) {
            if (nUpper < nNow) {
              bUseUpperNowSummary = true;
            }
          }
        }
      }
      
      if (!bUseUpperNowSummary) {
        // not sure how to generate the summary interval here,
        // without a summary the document cannot fall "within"
      } else {
        lFields.add(0,this.makeBoundaryField(sUpperField,Long.MAX_VALUE));
        lFields.add(0,this.makeBoundaryField(sLowerField,nLower));
        lIMetaTerms.add(0,this.getMetaValue("now.u",nInterval));
      }
    }
    
    // add the multiplicity field
    boolean bIndexMultiplicity = true;
    if (bIndexMultiplicity) {
      String sName = this.multiplicityFieldName;
      NumericField f = new NumericField(sName,this.precisionStep,Field.Store.YES,true);
      f.setLongValue(nMultiplicity);
      lFields.add(0,f);
    }
    
    // add the interval meta terms field
    if (lIMetaTerms.size() > 0) {
      String sName = this.intervalMetaFieldName;
      String sValue = "meta-terms";
      Field f = new Field(sName,sValue,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.NO);
      String[] aMetaTerms = lIMetaTerms.toArray(new String[lIMetaTerms.size()]);
      f.setTokenStream(new MetaTermsTokenStream(aMetaTerms));
      lFields.add(0,f);
    }
    
    // add the summary meta terms field
    if (lSMetaTerms.size() > 0) {
      String sName = this.summaryMetaFieldName;
      String sValue = "meta-terms";
      Field f = new Field(sName,sValue,Field.Store.YES,Field.Index.ANALYZED,Field.TermVector.NO);
      String[] aMetaTerms = lSMetaTerms.toArray(new String[lSMetaTerms.size()]);
      f.setTokenStream(new MetaTermsTokenStream(aMetaTerms));
      lFields.add(0,f);
    }
    
    // add the interval fields
    for (Fieldable fld: lFields) {
      if (LOGGER.isLoggable(Level.FINER)) {
        StringBuffer sb = new StringBuffer();
        sb.append("Appending field:\n ");
        sb.append(" name=\"").append(fld.name()).append("\"");
        sb.append(" storageOption=\"").append(fld.isStored()).append("\"");
        sb.append("\n  storeValue=\"").append(fld.stringValue()).append("\"");
        LOGGER.finer(sb.toString());
      }
      document.add(fld);
    }
  }
  
  /**
   * Appends underlying fields to a document prior to writing the 
   * document to the index.
   * @param document the Lucene document
   * @param value the input value to write
   */
  @Override
  public void appendForWrite(Document document, Object value) {
    // Not applicable
  }
  
  /**
   * Makes the lower boundary field name associated with an interval index.
   * <br/>Interval 0 is the summary interval for the document.
   * @param interval the interval index
   * @return the name
   */
  private String getLowerFieldName(int interval) {
    //if (interval == 0) return this.getName()+".l.d";
    //else return this.getName()+".l."+interval;
    return this.getName()+".l."+interval;
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
    return this.getName()+".u."+interval;
    //String s = this.getName()+".bnd.u";
    //if (interval == 0) return s;
    //else return s+"."+interval;
    //if (interval == 0) return this.getName()+".u.d";
    //else return this.getName()+".u."+interval;
  }
  
  /**
   * Makes an interval boundary field.
   * @param name the field name
   * @param value the field value
   * @return the field
   */
  private NumericField makeBoundaryField(String name, Long value) {
    NumericField f = new NumericField(name,this.precisionStep,Field.Store.YES,true);
    f.setLongValue(value);
    return f;
  }
  
  /**
   * A stream to provide the meta terms for a time period.
   */
  private class MetaTermsTokenStream extends TokenStream  {
    private int           currentIndex = -1;
    private TermAttribute termAttribute = addAttribute(TermAttribute.class);
    private int           termCount = 0;
    private String[]      terms;
    
    public MetaTermsTokenStream(String[] terms) {
      this.terms = terms;
      this.termCount = this.terms.length;
    }

    @Override
    public boolean incrementToken() throws IOException {
      this.currentIndex++;
      if (this.currentIndex < this.termCount) {
        String sTerm = this.terms[this.currentIndex];
        this.termAttribute.setTermBuffer(sTerm.toLowerCase());
        return true;
      }
      return false;
    }

    @Override
    public void reset() throws IOException {
      this.currentIndex = -1;
    }
  }
   
}
