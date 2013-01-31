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
package com.esri.gpt.framework.util;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Serves as a proxy for the specification of a date.
 * <p>
 * The primary purpose is to support input date strings in standard
 * Java yyyy-MM-dd format.
 */
public class DateProxy implements Serializable {
  
// class variables =============================================================
/** default time format used by class **/
public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd";
  
// instance variables ==========================================================
private int       _day = 0;
private String    _date = "";
private String    _formattedInputDate = "";
private Timestamp _fromTimestamp = null;
private Timestamp _fromTimestampExcl = null;
private String    _fullFormat = "";
private int       _month = 0;
private Timestamp _toTimestamp = null;
private Timestamp _toTimestampExcl = null;
private int       _year = 0;

// constructors ================================================================

/** Default constructor. */
public DateProxy() {}

// properties ==================================================================

/**
 * Sets the day.
 * @param day the day
 */
private void setDay(String day) {
  _day = Val.chkInt(day,0);
}

/**
 * Gets the input date string.
 * @return the input date string
 */
public String getDate() {
  return _date;
}
/**
 * Sets the input date string.
 * @param date the input date string
 */
public void setDate(String date) {
  _date = Val.chkStr(date);
  normalize();
  if (getIsValid()) {
    _date = _formattedInputDate;
  }
}

/**
 * Determines if the date is valid.
 * @return true if the date is valid
 */
public boolean getIsValid() {
  return (_fromTimestamp != null);
}

/**
 * Sets the month.
 * @param month the month
 */
private void setMonth(String month) {
  _month = Val.chkInt(month,0);
}

/**
 * Determines if the input date string was invalid.
 * <p>
 * The input string is only considered invalid if it had non-zero
 * length and failed to eveluate to a date.
 * @return true if the input date string was invalid
 */
public boolean getWasInputStringInvalid() {
  return (!getIsValid() && (_date.length() > 0));
}

/**
 * Sets the year.
 * @param year the year
 */
private void setYear(String year) {
  _year = Val.chkInt(year,0);
}

// methods =====================================================================

/**
 * Returns a timestamp applicable for the from part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp asFromTimestamp() {
  return _fromTimestamp;
}

/**
 * Returns an exclusive timestamp applicable for the from part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp asFromTimestampExcl() {
  return _fromTimestampExcl;
}

/**
 * Returns a timestamp applicable for the to part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp asToTimestamp() {
  return _toTimestamp;
}

/**
 * Returns an exclusive timestamp applicable for the to part of a date range query.
 * @return the timestamp (null in unspecified)
 */
public Timestamp asToTimestampExcl() {
  return _toTimestampExcl;
}

/**
 * Formats a date as "yyyy-MM-dd".
 * @param date the date to format
 * @return date represented as string
 */
public static String formatDate(Timestamp date) {
  SimpleDateFormat format = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
  return format.format(date);
}

/**
 * Formats a timestamp as "yyyy-MM-dd'T'HH:mm:ssZ".
 * @param timestamp the timestamp to format
 * @return the formatted result
 */
public static String formatIso8601Timestamp(Timestamp timestamp) {
  String sTimestamp = "";
  if (timestamp != null) {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    sTimestamp = fmt.format(timestamp);
    sTimestamp = sTimestamp.substring(0,sTimestamp.length()-2)
                 + ":" + sTimestamp.substring(sTimestamp.length()-2);
  }
  return sTimestamp;
}

/**
 * Normalizes the input date string into parts.
 */
private void normalize() {
  resetParts();
  String sDate = Val.chkStr(_date).replaceAll("/","-");
  if (sDate.endsWith("Z")) sDate = sDate.substring(0,sDate.length()-1);
  if (sDate.length() > 0) {
    
    // insert the "-" delimiter
    if (sDate.indexOf("-") == -1) {
      if (sDate.length() == 8) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6)+
               "-"+sDate.substring(6,8);
      } else if (sDate.length() == 6) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6);
      } else if (sDate.length() == 4) {
        sDate = sDate.substring(0,4);
      }
    }
    
    // tokenize the parts
    String[] aDate = Val.tokenize(sDate,"-");
    if (aDate.length == 3) {
      setYear(aDate[0]);
      setMonth(aDate[1]);
      setDay(aDate[2]);
    } else if (aDate.length == 2) {
      setYear(aDate[0]);
      setMonth(aDate[1]);
    } else if (aDate.length == 1) {
      setYear(aDate[0]);
    } 
    
    // make the yyyy-MM-dd string
    String yyyyMMdd = "";
    if (_year > 0) {
      String sYear = ""+_year;
      if (sYear.length() == 1) {
        sYear = "200"+sYear;
      } else if (sYear.length() == 2) {
        sYear = "20"+sYear;
      }
      while (sYear.length() < 4) sYear = "0"+sYear;
      yyyyMMdd = sYear;
      if (_month > 0) {
        String sMonth = ""+_month;
        while (sMonth.length() < 2) sMonth = "0"+sMonth;
        yyyyMMdd += "-"+sMonth;
        if (_day > 0) {
          String sDay = ""+_day;
          while (sDay.length() < 2) sDay = "0"+sDay;
          yyyyMMdd += "-"+sDay;
        } else {
          yyyyMMdd += "-01";
        }
      } else {
        yyyyMMdd += "-01-01";
      }
    }

    // set the timestamps and formated date strings 
    if (yyyyMMdd.length() > 0) {
      try {
        
        // set the from and to timestamps
        _fromTimestamp = new Timestamp(Date.valueOf(yyyyMMdd).getTime());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(_fromTimestamp.getTime());
        if (_month <= 0) {
          calendar.add(Calendar.YEAR,1);
         } else if (_day <= 0) {
          calendar.add(Calendar.MONTH,1);
        } else {
          calendar.add(Calendar.DAY_OF_MONTH,1);
        }
        _fromTimestampExcl = new Timestamp(calendar.getTimeInMillis());
        calendar.add(Calendar.SECOND,-1);
        _toTimestamp = new Timestamp(calendar.getTimeInMillis());
        if (_month <= 0) {
          calendar.add(Calendar.YEAR,-1);
         } else if (_day <= 0) {
          calendar.add(Calendar.MONTH,-1);
        } else {
          calendar.add(Calendar.DAY_OF_MONTH,-1);
        }
        _toTimestampExcl = new Timestamp(calendar.getTimeInMillis());
        
        // set the full format,
        // set the formatted input data (according to the parts supplied)
        _fullFormat = DateProxy.formatDate(_fromTimestamp);
        _formattedInputDate = _fullFormat;
        if (_month <= 0) {
          _formattedInputDate = _formattedInputDate.substring(0,4);
        } else if (_day <= 0) {
          _formattedInputDate = _formattedInputDate.substring(0,7);
        } 

      } catch (Exception e) {
        resetParts();
      }
    }
  }
}
  
/**
 * Resets the date parts.
 */
private void resetParts() {
  _day = 0;
  _month = 0;
  _year = 0;
  _fromTimestamp = null;
  _toTimestamp = null;
  _fullFormat = "";
  _formattedInputDate = "";
}

/**
 * Subtract number of days from current date
 * @param daysToSubtract number of days to Subtract
 * @return the Subtracted timestamp
 */
public static Timestamp subtractDays(int daysToSubtract) {
  GregorianCalendar cal = new GregorianCalendar();	    
  cal.setTimeInMillis(System.currentTimeMillis());
  cal.add(Calendar.DAY_OF_MONTH, (-1 * daysToSubtract));
  return new Timestamp(cal.getTimeInMillis()); 
}

/**
 * Returns a string representation this object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" date=").append(_date).append("\n");
  sb.append(" isValid=").append(getIsValid()).append("\n");
  sb.append(" formattedInputDate=").append(_formattedInputDate).append("\n");
  sb.append(" fullFormat=").append(_fullFormat).append("\n");
  sb.append(" year=").append(_year);
  sb.append(" month=").append(_month);
  sb.append(" day=").append(_day).append("\n");
  sb.append(" fromTimestamp=").append(_fromTimestamp).append("\n");
  sb.append(" toTimestamp=").append(_toTimestamp).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}


}
