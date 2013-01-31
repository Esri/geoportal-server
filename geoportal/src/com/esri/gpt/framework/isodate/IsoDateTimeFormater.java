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

import java.util.Calendar;
import java.util.TimeZone;

/**
 * ISO Date and Time formater.
 */
abstract class IsoDateTimeFormater {

  /**
   * Formats date into the string.
   * @param cal date to format
   * @return string representation of the ISO date and time
   */
  public String format(Calendar cal) {

    StringBuilder sb = new StringBuilder();

    if (cal.isSet(Calendar.YEAR)) {
      sb.append(pad(Integer.toString(cal.get(Calendar.YEAR)),4));
      if (cal.isSet(Calendar.MONTH)) {
        appendDash(sb);
        sb.append(pad(Integer.toString(cal.get(Calendar.MONTH)),2));
        if (cal.isSet(Calendar.DAY_OF_MONTH)) {
          appendDash(sb);
          sb.append(pad(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)),2));
        }
      }
      if (cal.isSet(Calendar.HOUR_OF_DAY)) {
        sb.append("T");
        sb.append(pad(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)),2));
        if (cal.isSet(Calendar.MINUTE)) {
          appendColon(sb);
          sb.append(pad(Integer.toString(cal.get(Calendar.MINUTE)),2));
          if (cal.isSet(Calendar.SECOND)) {
            appendColon(sb);
            sb.append(pad(Integer.toString(cal.get(Calendar.SECOND)),2));
          }
        }
      }
      TimeZone tz = cal.getTimeZone();
      int minOffset = tz.getRawOffset()/1000/60;
      if (minOffset==0) {
        sb.append("Z");
      } else {
        int hOffset = Math.abs(minOffset/60);
        int mOffset = Math.abs(minOffset % 60);
        sb.append(minOffset>0? "+": "-");
        sb.append(pad(Integer.toString(hOffset),2));
        appendColon(sb);
        sb.append(pad(Integer.toString(mOffset),2));
      }
    }

    return sb.toString();
  }

  /**
   * Appends dash.
   * @param sb string builder
   */
  protected abstract void appendDash(StringBuilder sb);

  /**
   * Appends colon.
   * @param sb string builder
   */
  protected abstract void appendColon(StringBuilder sb);

  /**
   * Pads a string to the certain size.
   * @param input input string
   * @param size size
   * @return padded string
   */
  private String pad(String input, int size) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<size-input.length(); i++) {
      sb.append("0");
    }
    sb.append(input);
    return sb.toString();
  }
}
