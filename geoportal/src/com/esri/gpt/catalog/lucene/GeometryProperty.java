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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

/**
 * Represents the bounding envelope associated with a document.
 */
public class GeometryProperty extends Storeable {
  
  /** class variables ========================================================= */
    
  /** The Logger */
  private static Logger LOGGER = Logger.getLogger(GeometryProperty.class.getName());
        
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name.
   * @param name the property name
   */
  public GeometryProperty(String name) {
    super(name);
    setComparisonField(new DoubleField("envelope.area",8));
  }
      
  /** methods ================================================================= */
  
  /**
   * Appends underlying fields to a document prior to writing the 
   * document to the index.
   * @param document the Lucene document
   */
  @Override
  public void appendForWrite(Document document) {
    Object value = null;
    if ((getValues() != null) && (getValues().length == 1)) {
      value = getValues()[0];
    }
    if ((value != null) && (value instanceof Envelope)) {
      Envelope envelope = (Envelope)value;
      if ((envelope != null) && !envelope.isEmpty()) {
        
        // initialize values
        ArrayList<Fieldable> alFields = new ArrayList<Fieldable>();
        double dMinX = envelope.getMinX();
        double dMinY = envelope.getMinY();
        double dMaxX = envelope.getMaxX();
        double dMaxY = envelope.getMaxY();
        double dArea = Math.abs(dMaxX - dMinX) * Math.abs(dMaxY - dMinY);
        boolean bCrossedDateLine = (dMinX > dMaxX);
        if (bCrossedDateLine) {
          dArea = Math.abs(dMaxX + 360.0 - dMinX) * Math.abs(dMaxY - dMinY);
        } 
        String sFull = dMinX+";"+dMinY+";"+dMaxX+";"+dMaxY+";"+dArea+";"+bCrossedDateLine;
        
        // initialize fields
        NumericField fldMinX = new NumericField("envelope.minx",Field.Store.YES,true);
        NumericField fldMinY = new NumericField("envelope.miny",Field.Store.YES,true);
        NumericField fldMaxX = new NumericField("envelope.maxx",Field.Store.YES,true);
        NumericField fldMaxY = new NumericField("envelope.maxy",Field.Store.YES,true);
        NumericField fldArea = new NumericField("envelope.area",Field.Store.YES,true);
        Field fldXDL = new Field("envelope.xdl",""+bCrossedDateLine,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);
        Field fldFull = new Field("envelope.full",sFull,Field.Store.YES,Field.Index.NOT_ANALYZED,Field.TermVector.NO);;

        fldMinX.setDoubleValue(dMinX);
        fldMinY.setDoubleValue(dMinY);
        fldMaxX.setDoubleValue(dMaxX);
        fldMaxY.setDoubleValue(dMaxY);
        fldArea.setDoubleValue(dArea);
        alFields.add(fldMinX);
        alFields.add(fldMinY);
        alFields.add(fldMaxX);
        alFields.add(fldMaxY);
        alFields.add(fldArea);
        alFields.add(fldXDL);
        alFields.add(fldFull);
        
        // add the fields
        for (Fieldable fld: alFields) {
          if (LOGGER.isLoggable(Level.FINER)) {
            StringBuffer sb = new StringBuffer();
            sb.append("Appending field:\n ");
            sb.append(" name=\"").append(fld.name()).append("\"");
            sb.append(" storageOption=\"").append(Field.Store.YES).append("\"");
            sb.append("\n  storeValue=\"").append(fld.stringValue()).append("\"");
            LOGGER.finer(sb.toString());
          }
          document.add(fld);
        }
        
      }
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
   * Reads the envelope associated with a document.
   * @param document the Lucene document
   * @return the envelope (null if none)
   */
  protected Envelope readEnvelope(Document document) {
    String sMinX = document.get("envelope.minx");
    String sMinY = document.get("envelope.miny");
    String sMaxX = document.get("envelope.maxx");
    String sMaxY = document.get("envelope.maxy");
    if ((sMinX != null) && (sMinY != null) && (sMaxX != null) && (sMaxY != null)) {
      Envelope envelope = new Envelope();
      envelope.put(sMinX,sMinY,sMaxX,sMaxY);
      if (!envelope.isEmpty()) {
        return envelope;
      }
    }
    return null;
  }
    
}
