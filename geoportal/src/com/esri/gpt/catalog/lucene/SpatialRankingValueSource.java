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
import com.esri.gpt.framework.geometry.Envelope;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.function.DocValues;
import org.apache.lucene.search.function.ValueSource;

/**
 * 
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 * <p/>
 * 
 * The algorithm is implemented as envelope on envelope overlays rather than
 * complex polygon on complex polygon overlays.
 * 
 * <p/>
 * Spatial relevance scoring algorithm:
 * 
 * <br/>  queryArea = the area of the input query envelope
 * <br/>  targetArea = the area of the target envelope (per Lucene document)
 * <br/>  intersectionArea = the area of the intersection for the query/target envelopes
 * <br/>  queryPower = the weighting power associated with the query envelope (default = 1.0)
 * <br/>  targetPower =  the weighting power associated with the target envelope (default = 1.0)
 *   
 * <br/>  queryPower and targetPower are initialized from gpt.xml configuration parameters:
 * <br/>  /gptConfig/catalog/parameter@key="spatialRelevance.queryPower" and
 * <br/>  /gptConfig/catalog/parameter@key="spatialRelevance.queryPower"  
 *      
 * <br/>  queryRatio  = intersectionArea / queryArea;
 * <br/>  targetRatio = intersectionArea / targetArea;
 * <br/>  queryFactor  = Math.pow(queryRatio,queryPower);
 * <br/>  targetFactor = Math.pow(targetRatio,targetPower);
 * <br/>  score = queryFactor * targetFactor;
 *      
 */
public class SpatialRankingValueSource extends ValueSource {
  
  /** class variables ========================================================= */
  
  /** The class name hash code. */
  private static final int HCODE = SpatialRankingValueSource.class.hashCode();

  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(SpatialRankingValueSource.class.getName());
  
  /** instance variables ====================================================== */
 
  /** Properties associated with the query envelope */
  private double  qryArea;
  private double  qryPower = 2.0;
  private double  qryMinX;
  private double  qryMinY;
  private double  qryMaxX;
  private double  qryMaxY;
  private boolean qryCrossedDateline = false;
  
  /** Properties associated with the target envelope (the document's envelope) */
  private String tgtField = "envelope.full";
  private double tgtPower = 0.5;

  /** constructors ============================================================ */
  
  /**
   * Constructor.
   * @param queryEnvelope the query envelope
   * @param queryPower the query power (scoring algorithm)
   * @param targetPower the target power (scoring algorithm)
   */
  public SpatialRankingValueSource(Envelope queryEnvelope, double queryPower, double targetPower) {
    
    // initialize
    this.qryPower = queryPower;
    this.tgtPower = targetPower;
    this.qryMinX = queryEnvelope.getMinX();
    this.qryMinY = queryEnvelope.getMinY();
    this.qryMaxX = queryEnvelope.getMaxX();
    this.qryMaxY = queryEnvelope.getMaxY();
    
    if (this.qryMinX > this.qryMaxX) {
      this.qryCrossedDateline = true;
      this.qryArea = Math.abs(qryMaxX + 360.0 - qryMinX) * Math.abs(qryMaxY - qryMinY);
    } else {
      this.qryArea = Math.abs(qryMaxX - qryMinX) * Math.abs(qryMaxY - qryMinY);
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Calculates the spatial ranking score.
   * @param doc the document id
   * @param delimitedEnvelope the envelope.full field value
   * @return the score
   */
  private float calculateScore(int doc, String delimitedEnvelope) {
    double score = 0;
    if (delimitedEnvelope== null) return (float)score;
    
    String[] tokens = delimitedEnvelope.split(";");
    if (tokens.length == 6) {
      double tgtMinX = Double.valueOf(tokens[0]);
      double tgtMinY = Double.valueOf(tokens[1]);
      double tgtMaxX = Double.valueOf(tokens[2]);
      double tgtMaxY = Double.valueOf(tokens[3]);
      double tgtArea = Double.valueOf(tokens[4]);
      boolean tgtCrossedDateline = Boolean.valueOf(tokens[5]);
      
      // determine the area of the target and the area of intersection, 
      if ((this.qryArea > 0) && (tgtArea > 0)) {
        double top    = Math.min(this.qryMaxY,tgtMaxY);
        double bottom = Math.max(this.qryMinY,tgtMinY);
        double height = top - bottom;
        double width  = 0;
        
        // queries that do not cross the date line
        if (!this.qryCrossedDateline) {
          
          // documents that do not cross the date line
          if (!tgtCrossedDateline) {
            double left  = Math.max(this.qryMinX,tgtMinX);
            double right = Math.min(this.qryMaxX,tgtMaxX);
            width = right - left;
            
          // documents that cross the date line
          } else {
            double tgtWestLeft  = Math.max(this.qryMinX,tgtMinX);
            double tgtWestRight = Math.min(this.qryMaxX,180.0);
            double tgtWestWidth = tgtWestRight - tgtWestLeft;
            if (tgtWestWidth > 0) {
              width = tgtWestWidth;
            } else {
              double tgtEastLeft  = Math.max(this.qryMinX,-180.0);
              double tgtEastRight = Math.min(this.qryMaxX,tgtMaxX);
              double tgtEastWidth = tgtEastRight - tgtEastLeft;
              if (tgtEastWidth > 0) {
                width = tgtEastWidth;
              }
            }
          }
          
        // queries that cross the date line
        } else {
          
          // documents that do not cross the date line
          if (!tgtCrossedDateline) {
            
            double qryWestLeft  = Math.max(this.qryMinX,tgtMinX);
            double qryWestRight = Math.min(tgtMaxX,180.0);
            double qryWestWidth = qryWestRight - qryWestLeft;
            if (qryWestWidth > 0) {
              width = qryWestWidth;
            } else {
              double qryEastLeft  = Math.max(tgtMinX,-180.0);
              double qryEastRight = Math.min(this.qryMaxX,tgtMaxX);
              double qryEastWidth = qryEastRight - qryEastLeft;
              if (qryEastWidth > 0) {
                width = qryEastWidth;
              }
            }
            
          // documents that cross the date line
          } else {
            double left  = Math.max(this.qryMinX,tgtMinX);
            double right = Math.min(this.qryMaxX,tgtMaxX);
            width = right + 360.0 - left;
          }
        }
        
        // calculate the score
        if ((width > 0) && (height > 0)) { 
          double intersectionArea = width * height;          
          double queryRatio  = intersectionArea / this.qryArea;
          double targetRatio = intersectionArea / tgtArea;
          double queryFactor  = Math.pow(queryRatio,this.qryPower);
          double targetFactor = Math.pow(targetRatio,this.tgtPower);
          score = queryFactor * targetFactor * 10000.0;
                    
          boolean bLog = false;
          if (bLog && LOGGER.isLoggable(Level.FINER)) {
            StringBuffer sb = new StringBuffer();
            sb.append("\nscore="+score);
            sb.append("\n  queryEnv="+this.qryMinX+","+this.qryMinY+","+this.qryMaxX+","+this.qryMaxY);
            sb.append(" targetEnv="+tgtMinX+","+tgtMinY+","+tgtMaxX+","+tgtMaxY);
            sb.append("\n  intersectionArea="+intersectionArea);
            sb.append(" queryArea="+this.qryArea+" targetArea="+tgtArea);
            sb.append("\n  queryRatio="+queryRatio+" targetRatio="+targetRatio);
            sb.append("\n  queryFactor="+queryFactor+" targetFactor="+targetFactor);
            sb.append(" (queryPower="+this.qryPower+" targetPower="+this.tgtPower+")");
            LOGGER.finer(sb.toString());
          }
        }
      }

    }
    
    return (float)score;
  }

  /**
   * Returns the ValueSource description.
   * @return the description
   */
  @Override
  public String description() {
    return "SpatialRankingValueSource("+this.tgtField+")";
  }
  
  /**
   * Determines if this ValueSource is equal to another.
   * @param o the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object o) {
    if (o.getClass() !=  SpatialRankingValueSource.class) return false;
    SpatialRankingValueSource other = (SpatialRankingValueSource)o;
    return this.getDelimiterQueryParameters().equals(other.getDelimiterQueryParameters());
  }
  
  /**
   * Returns the delimited query parameters.
   * <br/>Applies to ValueSource.equals and ValueSource.hashCode.
   * @return the delimited parameters
   */
  public String getDelimiterQueryParameters() {
    return this.qryMinX+";"+this.qryMinY+";"+this.qryMaxX+";"+this.qryMaxY+";"+
           this.qryPower+";"+this.tgtPower;
  }

  /**
   * Returns the DocValues used by the function query. 
   * @param reader the index reader
   * @return the values
   */
  @Override
  public DocValues getValues(IndexReader reader) throws IOException {
    final String[] arr = FieldCache.DEFAULT.getStrings(reader,this.tgtField);
    
    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        return calculateScore(doc,arr[doc]);
      }
      @Override
      public String toString(int doc) {
        return description()+"="+floatVal(doc);
      }
      Object getInnerArray() {
        return arr;
      }
    };
    
  }
  
  /**
   * Returns the ValueSource hash code.
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return HCODE+this.getDelimiterQueryParameters().hashCode();
  }

}
