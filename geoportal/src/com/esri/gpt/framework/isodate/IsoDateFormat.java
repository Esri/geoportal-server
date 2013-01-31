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

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * ISO Date format.
 */
public class IsoDateFormat extends Format {

  private Format format = Format.extended;

  public IsoDateFormat() {}

  public IsoDateFormat(Format format) {
    this.format = format;
  }

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    if (!(obj instanceof Date)) throw new IllegalArgumentException("Expected object to format is not a date object.");
    IsoDateTimeFormater formater = new IsoDateTimeFormater() {
      protected void appendDash(StringBuilder sb) {
        if (format==Format.extended) sb.append("-");
      }
      protected void appendColon(StringBuilder sb) {
        if (format==Format.extended) sb.append(":");
      }
    };
    pos.setBeginIndex(toAppendTo.length());
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.setTime((Date) obj);
    String dateStr = formater.format(cal);
    toAppendTo.append(dateStr);
    pos.setEndIndex(toAppendTo.length());
    return toAppendTo;
  }

  @Override
  public Date parseObject(String source, ParsePosition pos) {
    IsoDateTimeParser parser = new IsoDateTimeParser();
    try {
      return parser.parse(source).getTime();
    } catch (ParseException ex) {
      if (pos!=null)
        pos.setErrorIndex(ex.getErrorOffset());
    } finally {
      if (pos!=null)
        pos.setIndex(parser.getIndex());
    }
    return null;
  }

  @Override
  public Date parseObject(String source) throws ParseException {
    return (Date) this.parseObject(source, null);
  }

  /**
   * Types of ISO DATE formats.
   */
  public static enum Format {
    basic, extended
  }
}
