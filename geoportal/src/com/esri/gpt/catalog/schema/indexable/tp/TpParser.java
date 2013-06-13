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
package com.esri.gpt.catalog.schema.indexable.tp;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.util.Val;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parses the time period descriptors associated with a document.
 */
public class TpParser {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TpParser.class.getName());
  
  /** instance variables ====================================================== */
  private boolean allowAfterAndBefore = false;
  private boolean allowNow = true;
  private boolean allowNowLower = true;
  private boolean allowNowUpper = true;
  private boolean allowOpenEndedRange = true;
  private boolean allowUnknown = true;
  private boolean allowWarnings = false;
  private String  documentName;
  private boolean hasMaxExceededWarning = false;
  private int     maxIntervalsPerDocument = 50;
  private int     numInvalid = 0;
  private boolean wasConfigured = false;
  
  private TpIntervals      intervals;
  private List<TpPosition> valid = new ArrayList<TpPosition>();  
  private List<String>     warnings = new ArrayList<String>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TpParser() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the document name (used for logging only).
   * @return the document name
   */
  public String getDocumentName() {
    return this.documentName;
  }
  /**
   * Sets the document name (used for logging only).
   * @param documentName the document name
   */
  public void setDocumentName(String documentName) {
    this.documentName = documentName;
  }
  
  /**
   * Gets the parsed interval collection.
   * @return the intervals
   */
  public TpIntervals getIntervals() {
    return this.intervals;
  }
  
  /** methods ================================================================= */
  
  /**
   * Add a warning message.
   * @param position the position containing the warning
   */
  private void addWarning(TpPosition position) {
    this.addWarning(position.getDescriptor(),position.getWarning());
  }
  
  /**
   * Add a warning message.
   * @param descriptor the input descriptor
   * @param message the message
   */
  private void addWarning(String descriptor, String message) {
    this.warnings.add("For: "+descriptor+"\r\n"+message);
  }
  
  /**
   * Checks for and applies a now based indeterminate.
   * @param position the active position
   * @param indeterminate the indeterminate (now or present)
   */
  private void checkNow(TpPosition position, String indeterminate) {
    String sType = Val.chkStr(position.getType());
    String sInd = Val.chkStr(indeterminate);
    String sWarn = null;
    if (sInd.equals("now") || sInd.equals("present")) {
      if (sType.equalsIgnoreCase("beginPosition")) {
        if (this.allowNowLower) {
          position.setInterval(new TpInterval(Long.MIN_VALUE,null));
          position.getInterval().setIndeterminate("now.lower");
          position.setIsValid(true);
        } else {
          sWarn = "Indeterminate is not supported: "+sInd;
        }
      } else if (sType.equalsIgnoreCase("endPosition")) {
        if (this.allowNowUpper) {
          position.setInterval(new TpInterval(null,Long.MAX_VALUE));
          position.getInterval().setIndeterminate("now.upper");
          position.setIsValid(true);
        } else {
          sWarn = "Indeterminate is not supported: "+sInd;
        }
      } else {
        if (this.allowNow) {
          position.setInterval(new TpInterval(Long.MIN_VALUE,Long.MAX_VALUE));
          position.getInterval().setIndeterminate("now");
          position.setIsValid(true);  
        } else {
          sWarn = "Indeterminate is not supported: "+sInd;
        }
      }
    }
    if (sWarn != null) {
      position.setWarning(sWarn); 
    }
  }
  
  /**
   * Configure the parser.
   */
  private void configure() {
    if (this.wasConfigured) return;
    this.wasConfigured = true;
    StringAttributeMap params = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
    int n;
    String s;
    
    s = Val.chkStr(params.getValue("timeperiod.allowAfterAndBefore"));
    if (s.equalsIgnoreCase("true")) {
      this.allowAfterAndBefore = true;
    }
    s = Val.chkStr(params.getValue("timeperiod.allowOpenEndedRange"));
    if (s.equalsIgnoreCase("false")) {
      this.allowOpenEndedRange = false;
    }
    n = Val.chkInt(params.getValue("timeperiod.maxIntervalsPerDocument"),-1);
    if (n >= 0) {
      this.maxIntervalsPerDocument = n;
    }
  }
  
  /**
   * Convert an FDGC date-time descriptor to ISO-8601 format.
   * @param fgdcDateTime the FDGC date-time descriptor
   * @return the ISO-8601 string (null if not valid)
   */
  private String fgdcToIsoDateString(String fgdcDateTime) {
    String sIsoDate = null;
    String sDate = Val.chkStr(fgdcDateTime);
    if (sDate.indexOf("T") != -1) {
      if (sDate.endsWith(".fgdctime.")) {
        sDate = sDate.substring(0,sDate.length()-10);
      }
      return sDate;
    }
    
    String sTime = "";
    String sTimeZone = "";
    String sDateZone = "";
    boolean bDate = false;
    boolean bTime = false;
    boolean bTimeZone = false;
    
    int nTimeSeparator = sDate.toLowerCase().indexOf(".fgdctime.");
    if (nTimeSeparator != -1) {
      sTime = Val.chkStr(sDate.substring(nTimeSeparator+10));
      sDate = Val.chkStr(sDate.substring(0,nTimeSeparator));
      sTime = sTime.replaceAll(":","");
      sTime = sTime.replaceAll("\\.","");
      if (sTime.equalsIgnoreCase("unknown")) {
        sTime = "";
      }
    }
    sDate = sDate.replaceAll("-","");
    String lc = sDate.toLowerCase();
    if (lc.equals("unknown") || lc.equals("present")) {
      return sDate;
    }
      
    if ((sIsoDate == null) || (sIsoDate.length() == 0)) {
      if (sTime.length() == 0) {
        if (sDate.indexOf("T") != -1) {
          sIsoDate = sDate;
        } else if (sDate.toLowerCase().endsWith("z")) {
          sDateZone = "Z";
          sDate = Val.chkStr(sDate.substring(0,sDate.length()-1));
        }
      } 
    }
      
    if ((sIsoDate == null) || (sIsoDate.length() == 0)) {
      if (sTime.length() > 0) {
        if (sDate.toLowerCase().endsWith("z")) {
          sDate = Val.chkStr(sDate.substring(0,sDate.length()-1));
        }
        if (sTime.toLowerCase().endsWith("z")) {
          sTimeZone = "Z";
          sTime = Val.chkStr(sTime.substring(0,sTime.length()-1));
          bTimeZone = true;
        } else {
          int n = sTime.indexOf("+");
          if (n == -1) n = sTime.indexOf("-");
          if (n != -1) {
            sTimeZone = Val.chkStr(sTime.substring(n));
            sTime = Val.chkStr(sTime.substring(0,n));
            if (sTimeZone.length() == 5) {
              sTimeZone = sTimeZone.substring(0,3)+":"+sTimeZone.substring(3,5);
              bTimeZone = true;
            }
          } 
        }
      }
    }
      
    if ((sIsoDate == null) || (sIsoDate.length() == 0)) {
      if (sDate.length() == 4) {
        bDate = true;
      } else if (sDate.length() == 6) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6);
        bDate = true;
      } else if (sDate.length() == 8) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6)+"-"+sDate.substring(6,8);
        bDate = true;
        if (sTime.length() == 2) {
          sTime = sTime+":00:00";
          bTime = true;
        } else if (sTime.length() == 4) { 
          sTime = sTime.substring(0,2)+":"+sTime.substring(2,4)+":00";
          bTime = true;
        } else if (sTime.length() == 6) { 
          sTime = sTime.substring(0,2)+":"+sTime.substring(2,4)+":"+sTime.substring(4,6);
          bTime = true;
        } else if (sTime.length() > 6) { 
          sTime = sTime.substring(0,2)+":"+sTime.substring(2,4)+":"+sTime.substring(4,6)+"."+sTime.substring(6);
          bTime = true;
        }
      }
    
      if (bDate) {
        sIsoDate = sDate;
        if (bTime) {
          sIsoDate = sIsoDate+"T"+sTime;
          if (bTimeZone) {
            sIsoDate = sIsoDate+sTimeZone;
          }
        } else {
          sIsoDate = sIsoDate+sDateZone;
        }
      }
    }
    
    return sIsoDate;
  }
  
  /**
   * True if parser warnings were encountered.
   * @return true if parser warnings were encountered
   */
  public boolean hasWarnings() {
    return (this.warnings.size() > 0);
  }
  
  /**
   * Parses a time period descriptor string.
   * @param descriptor the descriptor
   */
  public void parseDescriptor(String descriptor) {
    LOGGER.finest("Parsing descriptor: "+descriptor); 
    descriptor = Val.chkStr(descriptor);
    String lc = descriptor.toLowerCase();
    if (this.hasMaxExceededWarning) return;
    if (!this.wasConfigured) this.configure();
    
    // false hits
    if (lc.equals("tp.position.") ||
        lc.equals("tp.position..indeterminate.") ||
        lc.equals("tp.begin..indeterminate..end..indeterminate.")) {
      LOGGER.finest("Ignoring false timeperiod descrtptor hit: "+descriptor);
      return;
    } else if (lc.equals("tp.position..fgdctime.") ||
               lc.equals("tp.begin..fgdctime..end..fgdctime.")) {
      LOGGER.finest("Ignoring false timeperiod descrtptor hit: "+descriptor);
      return;
    }
    
    if (lc.startsWith("tp.position.")) {
      String posDescriptor = Val.chkStr(descriptor.substring(12));
      TpPosition position = new TpPosition();
      position.setType("position");
      position.setDescriptor(descriptor);
      position.setPositionDescriptor(posDescriptor);
      this.parsePosition(position);
      if (position.getIsValid()) {
        this.valid.add(position);
      } else {
        String sInd = Val.chkStr(position.getIndeterminate());
        if (this.allowUnknown && sInd.equals("unknown")) {
          position.setInterval(new TpInterval(
              Long.MIN_VALUE,Long.MAX_VALUE,"unknown"));
          position.setIsValid(true);
          this.valid.add(position);
        } else {
          this.addWarning(position);
          this.numInvalid++;
        }
      }
    
    } else if (lc.startsWith("tp.begin.")) {
      String sBegin = "";
      String sEnd = "";
      String sRange = Val.chkStr(descriptor.substring(9));
      int n = sRange.indexOf(".end.");
      if (n != -1) {
        sBegin = Val.chkStr(sRange.substring(0,n));
        sEnd = Val.chkStr(sRange.substring(n+5));
      } else {
        sBegin = sRange;
      }
      
      TpPosition begin = new TpPosition();
      begin.setType("beginPosition");
      begin.setDescriptor(descriptor);
      begin.setPositionDescriptor(sBegin);
      this.parsePosition(begin);
      
      TpPosition end = new TpPosition();
      end.setType("endPosition");
      end.setDescriptor(descriptor);
      end.setPositionDescriptor(sEnd);
      this.parsePosition(end);
      
      if (begin.getIsValid() && end.getIsValid()) {
        String sInd = null;
        String sWarn = null;
        String sBeginInd = Val.chkStr(begin.getInterval().getIndeterminate());
        String sEndInd = Val.chkStr(end.getInterval().getIndeterminate());
        
        TpPosition position = new TpPosition();
        position.setType("position");
        position.setDescriptor(descriptor);
        
        if (sBeginInd.equals("now.lower") && (sEndInd.equals("now.upper"))) {
          sInd = "now";
          if (this.allowNow) {
            position.setInterval(new TpInterval(Long.MIN_VALUE,Long.MAX_VALUE));
          } else {
            sWarn = "The begin position and end position are both: now.";
          }
        } else if (sBeginInd.equals("now.lower")) {
          sInd = "now.lower";
          long nUpper = end.getInterval().getUpper().longValue();
          position.setInterval(new TpInterval(Long.MIN_VALUE,nUpper));
        } else if (sEndInd.equals("now.upper")) {
          sInd = "now.upper";
          long nLower = begin.getInterval().getLower().longValue();
          position.setInterval(new TpInterval(nLower,Long.MAX_VALUE));
        } else {
          long nLower = begin.getInterval().getLower().longValue();
          long nUpper = end.getInterval().getUpper().longValue();
          if (nUpper < nLower) {
            sWarn = "The end position is earlier than the begin position.";
          } else {
            position.setInterval(new TpInterval(nLower,nUpper));
          }
        }
        if (sWarn != null) {
          this.addWarning(descriptor,sWarn);
          this.numInvalid++;
        } else {
          position.getInterval().setIndeterminate(sInd);
          position.setIsValid(true);
          this.valid.add(position);
        }
        
      } else if (begin.getIsValid()) {
        String sInd = Val.chkStr(end.getIndeterminate());
        boolean bOpen = (sEnd.length() == 0) || sInd.equals("unknown");
        if (bOpen) {
          sInd = Val.chkStr(begin.getInterval().getIndeterminate());
          if (sInd.equals("now.lower")) {
            if (this.allowNow) {
              begin.setInterval(new TpInterval(
                  Long.MIN_VALUE,Long.MAX_VALUE,"now"));
            } else {
              bOpen = false;
            }
          } 
        }
        if (bOpen && this.allowOpenEndedRange) {
          this.valid.add(begin);
        } else {
          this.addWarning(end);
          this.numInvalid++;
        }
        
      } else if (end.getIsValid()) {
        String sInd = Val.chkStr(begin.getIndeterminate());
        boolean bOpen = (sBegin.length() == 0) || sInd.equals("unknown");
        if (bOpen) {
          sInd = Val.chkStr(end.getInterval().getIndeterminate());
          if (sInd.equals("now.upper")) {
            if (this.allowNow) {
              end.setInterval(new TpInterval(
                  Long.MIN_VALUE,Long.MAX_VALUE,"now"));
            } else {
              bOpen = false;
            }
          }
        }
        if (bOpen && this.allowOpenEndedRange) {
          this.valid.add(end);
        } else {
          this.addWarning(begin);
          this.numInvalid++;
        }
        
      } else {
        
        String sBeginInd = Val.chkStr(begin.getIndeterminate());
        String sEndInd = Val.chkStr(begin.getIndeterminate());
        if (this.allowUnknown && sBeginInd.equals("unknown") && sEndInd.equals("unknown")) {
          TpPosition position = new TpPosition();
          position.setType("position");
          position.setDescriptor(descriptor);
          position.setInterval(new TpInterval(
              Long.MIN_VALUE,Long.MAX_VALUE,"unknown"));
          position.setIsValid(true);
          this.valid.add(position);
        } else {
          String sWarn = begin.getWarning()+"\r\n"+end.getWarning();
          this.addWarning(descriptor,sWarn);
          this.numInvalid++;
        }
      }
      
    } else {
      this.addWarning(descriptor,"Unrecognized input.");
      this.numInvalid++;
    }
    
    if (!this.hasMaxExceededWarning) {
      if (this.valid.size() > this.maxIntervalsPerDocument) {
        String s = "The maximum number of time period intervals per document was exceeded.";
        s += " timeperiod.maxIntervalsPerDocument="+this.maxIntervalsPerDocument;
        this.addWarning(descriptor,s);
      }
    }
  }
    
  /**
   * Parses a declared time period position.
   * @param position the position.
   * @param isEndOfRange
   */
  private void parsePosition(TpPosition position) {
    String sDescriptor = Val.chkStr(position.getPositionDescriptor());
    String sDate = "";  
    
    // check for an indeterminate within the input string
    int n = sDescriptor.toLowerCase().indexOf(".indeterminate.");
    if (n == -1) {
      sDate = sDescriptor;
    } else {
      sDate = Val.chkStr(sDescriptor.substring(0,n));
      String sInd = Val.chkStr(sDescriptor.substring(n+15).toLowerCase());
      if (sInd.length() > 0) {
        String sWarn = null;
        position.setIndeterminate(sInd);
        if (sInd.equals("after") || sInd.equals("before")) {
          if (!this.allowAfterAndBefore) {
            sWarn = "Indeterminate is not supported: "+sInd;
          }
        } else if (sInd.equals("now") || sInd.equals("present")) {
          this.checkNow(position,sInd);
          if (position.getIsValid() || (position.getWarning() != null)) {
            return;
          }
        } else if (sInd.equals("unknown")) {
          sWarn = "Indeterminate is not supported: "+sInd;
        } else {
          sWarn = "Indeterminate is not recognized: "+sInd;
        }
        if (sWarn != null) {
          position.setWarning(sWarn); 
          return;
        }
      }
    }
    
    // check of an FGDC date/time
    n = sDate.toLowerCase().indexOf(".fgdctime.");
    if (n != -1) {
      sDate = Val.chkStr(fgdcToIsoDateString(sDate));
    }
    
    // check for a date string
    position.setDateDescriptor(sDate);
    if (sDate.length() == 0) {
      position.setWarning("Invalid date: null");
      return;
    }
    
    // check for an indeterminate within the date value
    if (position.getIndeterminate() == null) {
      String sInd = sDate.toLowerCase();
      String sWarn = null;
      if (sInd.equals("after") || sInd.equals("before")) {
        sWarn = "Indeterminate is invalid in this usage context: "+sDate;
      } else if (sInd.equals("now") || sInd.equals("present")) {
        this.checkNow(position,sInd);
        if (position.getIsValid() || (position.getWarning() != null)) {
          return;
        }
      } else if (sInd.equals("unknown")) {
        sWarn = "Indeterminate is not supported: "+sInd;
      }
      if (sWarn != null) {
        position.setIndeterminate(sInd);
        position.setWarning(sWarn); 
        return;
      }
    }
    
    // determine the boundaries of the interval
    try {
      String sInd = Val.chkStr(position.getIndeterminate()).toLowerCase();
      Calendar cLower = TpUtil.parseIsoDateTime(sDate);
      Calendar cUpper = new GregorianCalendar();
      cUpper.setTimeInMillis(cLower.getTimeInMillis());
      TpUtil.advanceToUpperBoundary(cUpper,sDate);
      long nLower = cLower.getTimeInMillis();
      long nUpper = cUpper.getTimeInMillis();
      if (sInd.equals("after")) {
        nLower = nUpper + 1;
        nUpper = Long.MAX_VALUE;
      } else if (sInd.equals("before")) {
        nUpper = nLower - 1;
        nLower = Long.MIN_VALUE;
      } 
      position.setInterval(new TpInterval(nLower,nUpper));
      position.setIsValid(true);
    } catch (Exception e) {
      String sErr = "Error parsing datetime string: "+sDate+"\r\n"+e.toString();
      LOGGER.finer(sErr);
      position.setWarning("Invalid date: "+sDate);     
    }
  }
  
  /**
   * Processes the result for the document.
   */
  public void processResult() {
    if (this.hasWarnings() && !this.allowWarnings) return;

    TpIntervals intervals = new TpIntervals();
    for (TpPosition position: this.valid) {
      Long nLower = position.getInterval().getLower();
      Long nUpper = position.getInterval().getUpper();
      String sIndeterminate = position.getInterval().getIndeterminate();
      assert (nLower != null);
      assert (nUpper != null);
      TpInterval interval = new TpInterval(nLower,nUpper,sIndeterminate);
      intervals.add(interval);
    }
    this.intervals = intervals;
  }
  
  /**
   * Return warnings.
   * @return the warnings (null if none)
   */
  public String warningsToString() {
    if (this.warnings.size() > 0) {      
      StringBuilder sb = new StringBuilder();
      sb.append("TimePeriodAnalyzer Warnings");
      String sName = Val.chkStr(this.documentName);
      if (sName.length() > 0) {
        sb.append(" (").append(sName).append(")");
      }
      sb.append("\r\nThe following values will not be indexed:");
      boolean bFirst = true;
      for (String s: this.warnings) {
        if (bFirst) {
          bFirst = false;
          sb.append("\r\n");
        }
        sb.append("\r\n").append(s).append("\r\n");
      }
      return sb.toString();
    } 
    return null;
  }
  
  @Override
  public String toString() {
    String pfx = "\r\n  ";
    
    StringBuilder sbP = new StringBuilder();
    if ((this.valid == null) || (this.valid.size() == 0)) {
      sbP.append(pfx).append("valid: {}");
    } else {
      String pfx2 = pfx+"  ";
      sbP.append(pfx).append("valid: {");
      for (TpPosition position: this.valid) {
        sbP.append(pfx2).append("position: {");
        position.echo(sbP,3);
        sbP.append(pfx2).append("}");
      }
      sbP.append(pfx).append("}");
    }
            
    StringBuilder sb = new StringBuilder();
    sb.append("\r\nTpParser: {");
    sb.append(pfx).append("documentName: ").append(this.documentName);
    sb.append(pfx).append("allowAfterAndBefore: ").append(this.allowAfterAndBefore);
    sb.append(pfx).append("allowNow: ").append(this.allowNow);
    sb.append(pfx).append("allowNowLower: ").append(this.allowNowLower);
    sb.append(pfx).append("allowNowUpper: ").append(this.allowNowUpper);
    sb.append(pfx).append("allowOpenEndedRange: ").append(this.allowOpenEndedRange);
    sb.append(pfx).append("allowUnknown: ").append(this.allowUnknown);
    sb.append(pfx).append("allowWarnings: ").append(this.allowWarnings);
    sb.append(pfx).append("maxIntervalsPerDocument: ").append(this.maxIntervalsPerDocument);
    sb.append(pfx).append("numInvalid: ").append(this.numInvalid);
    sb.append(sbP);
    sb.append("\r\n").append("}");
    return sb.toString();
  }
  
}
