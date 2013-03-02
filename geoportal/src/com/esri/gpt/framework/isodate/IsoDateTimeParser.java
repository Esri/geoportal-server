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
package com.esri.gpt.framework.isodate;

import com.esri.gpt.framework.util.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * ISO Date and Time parser.
 */
class IsoDateTimeParser {
  
  private IsoDateContext isoDateContext = new IsoDateContext();

  public int getIndex() {
    return isoDateContext.getIndex();
  }

  /**
   * Parses date and time.
   * @param strDateTime date and time to parse
   * @return calendar
   * @throws ParseException if unable to parse given date and time
   */
  public Calendar parse(String strDateTime) throws ParseException {
    isoDateContext = new IsoDateContext();

    strDateTime = Val.chkStr(strDateTime);
    Calendar cal = Calendar.getInstance();
    cal.clear();
    IsoDateInputStream input = new IsoDateInputStream(strDateTime, isoDateContext);
    IsoDateReader r = new IsoDateReader(input);

    try {
      // read year
      String year = "";
      int nSign = 1;

      if (r.peek(1).matches("[+-]")) {
        nSign = r.read(1).equals("+")? 1: -1;
        year = r.read(5);
        if (year.length()!=5) throw new ParseException(strDateTime,getIndex());
      } else {
        year = r.read(2);
        if (year.length()!=2) throw new ParseException(strDateTime,getIndex());
        if (r.peek(2).length()==2 && !r.peek(1).toUpperCase().equals("T")) {
          year += r.read(2);
        }
      }

      if (year.length()==2) {
        // if length is exactly 2, the date is a century
        cal.set(Calendar.YEAR, 100*Integer.parseInt(year));
      } else if (year.length()==4) {
        // year has exactly 4 characters
        cal.set(Calendar.YEAR, Integer.parseInt(year));
      } else if (year.length()==5) {
        // year has exactly 5 characters; use sign also
        cal.set(Calendar.YEAR, nSign*Integer.parseInt(year));
      } else {
        throw new ParseException(strDateTime,getIndex());
      }

      // read month, week, day
      String month = "";
      String week = "";
      String day = "";

      if (r.hasMore()) {
        if (r.peek(1).toUpperCase().equals("W")) {
          r.read(1);
          week = r.read(2);
          cal.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(week));
          if (!r.peek(1).toUpperCase().equals("T") && r.peek(1).length()==1) {
            day = r.read(1);
            cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, Integer.parseInt(day));
          }
        } else if (!r.peek(1).toUpperCase().equals("T")) {
          if (r.peek(4).length()==4 && !r.peek(4).toUpperCase().contains("T")) {
            month = r.read(2);
            day = r.read(2);
            cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
          } else if (r.peek(3).length()==3 && !r.peek(3).toUpperCase().contains("T")) {
            week = r.read(3);
            cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(week));
          } else if (r.peek(2).length()==2 && !r.peek(2).toUpperCase().contains("T")) {
            month = r.read(2);
            cal.set(Calendar.MONTH, Integer.parseInt(month)-1);
          } else {
            throw new ParseException(strDateTime,getIndex());
          }
        }
      }

      if (r.peek(1).toUpperCase().equals("T")) {
        r.read(1);

        TimeZoneFlag timeZoneFlag = new TimeZoneFlag();
        
        // parse time
        if (r.peek(2).length()==2) {
          cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(r.read(2)));
          parseTimeZone(cal, timeZoneFlag, strDateTime, r);
        }

        if (r.peek(2).length()==2) {
          cal.set(Calendar.MINUTE, Integer.parseInt(r.read(2)));
          parseTimeZone(cal, timeZoneFlag, strDateTime, r);
        }

        if (r.peek(2).length()==2) {
          cal.set(Calendar.SECOND, Integer.parseInt(r.read(2)));
          parseTimeZone(cal, timeZoneFlag, strDateTime, r);
        }
      }

      if (r.hasMore()) {
        throw new ParseException(strDateTime,getIndex());
      }
    } catch (NumberFormatException ex) {
      throw new ParseException(strDateTime, Math.max(0, getIndex()));
    } catch (IOException ex) {
      throw new ParseException(strDateTime, Math.max(0, getIndex()));
    } finally {
      try { r.close(); } catch (IOException ex) {}
      try { input.close(); } catch (IOException ex) {}
    }

    return cal;
  }


  private void parseTimeZone(Calendar cal, TimeZoneFlag timeZoneFlag, String strDateTime, IsoDateReader r) throws ParseException, IOException {
    r.filterOff();
    if (r.peek(1).toUpperCase().equals("Z")) {
      r.filterOn();
      r.read(1);
      if (timeZoneFlag.flag) throw new ParseException(strDateTime, Math.max(0, getIndex()));
      TimeZone tz = TimeZone.getTimeZone("GMT 00:00");
      cal.setTimeZone(tz);
      timeZoneFlag.flag = true;
    } else if (r.peek(1).matches("[+-]")) {
      r.filterOn();
      if (r.peek(4).length()==4) {
        String strTimeShift = r.read(4);
        if (timeZoneFlag.flag) throw new ParseException(strDateTime, Math.max(0, getIndex()));
        TimeZone tz = TimeZone.getTimeZone("GMT "+strTimeShift.substring(0, 2)+":"+strTimeShift.substring(2,4));
        cal.setTimeZone(tz);
        timeZoneFlag.flag = true;
      } else if (r.peek(2).length()==2) {
        String strTimeShift = r.read(2);
        if (timeZoneFlag.flag) throw new ParseException(strDateTime, Math.max(0, getIndex()));
        TimeZone tz = TimeZone.getTimeZone("GMT "+strTimeShift+":00");
        cal.setTimeZone(tz);
        timeZoneFlag.flag = true;
      }
    }
    r.filterOn();
  }

  private static class TimeZoneFlag {
    public boolean flag;
  }
}
