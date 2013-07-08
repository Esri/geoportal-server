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
import java.util.Calendar;

/**
 * Timeperiod utilities.
 */
public class TpUtil {
   
  /** constructors ============================================================ */
 
  /** Default constructor. */
  public TpUtil() {}
  
  /** methods ================================================================= */
  
  /**
   * Advances a calendar to the end of date interval.
   * <p/>
   * Example: 
   * <br/>A calendar has been previously set to the beginning 
   * millisecond of 2010-04
   * <br/>Calling this method will advance the calendar to the 
   * final millisecond of 2010-04
   * @param calendar the calendar to advance
   * @param date the date string associated with the current calendar time
   */
  public static void advanceToUpperBoundary(Calendar calendar, String date) {
    if (date.indexOf("T") == -1) {
      int nAddType = -1;
      String[] parts = date.split("-");
      int nParts = parts.length;
      for (String s: parts) {
        if ((s.indexOf(":") != -1) && (s.indexOf("+") == -1)) {
          nParts--;
        }
      }
      if (nParts == 1) {
        nAddType = Calendar.YEAR;
      } else if (nParts == 2) {
        nAddType = Calendar.MONTH;
      } else if (nParts == 3) { 
        nAddType = Calendar.DAY_OF_MONTH;
      }
      if (nAddType > 0) {
        calendar.add(nAddType,1);
        // TODO: Does removing a millisecond cause an issue?
        calendar.add(Calendar.MILLISECOND,-1);
      }
    }
  }
  
  /**
   * Parses an ISO 8601 date string.
   * <br/>JAXB is used as the parser to provide a suitable ISO 8601 proxy.
   * <br/>See XML Schema Part 2: Datatypes for xsd:date.
   * @param lexicalDate the lexical representation of the date
   * @return a Calendar representing the input
   * @throws IllegalArgumentException if the input does not conform
   * @throws NullPointerException if the input is null
   */
  public static Calendar parseIsoDate(String lexicalDate) {
    return javax.xml.bind.DatatypeConverter.parseDate(lexicalDate);
  }
  
  /**
   * Parses an ISO 8601 date-time string.
   * <br/>JAXB is used as the parser to provide a suitable ISO 8601 proxy.
   * <br/>See XML Schema Part 2: Datatypes for xsd:dateTime.
   * @param lexicalDateTime the lexical representation of the date-time
   * @return a Calendar representing the input
   * @throws IllegalArgumentException if the input does not conform
   * @throws NullPointerException if the input is null
   */
  public static Calendar parseIsoDateTime(String lexicalDateTime) {
    
    // TODO:
    // if a timezone is not specified within the date-time string, 
    // the local time zone is assumed, this isn't such a good idea,
    // e.g 2012-06-13  vs 2012-06-13Z
    return javax.xml.bind.DatatypeConverter.parseDateTime(lexicalDateTime);
  }
  
  /**
   * Converts a Calendar value into an ISO 8601 date string.
   * <br/>JAXB is used to format the string. 
   * @param calendar the calendar value
   * @return the formatted string 
   */
  public static String printIsoDate(Calendar calendar) {
    return javax.xml.bind.DatatypeConverter.printDate(calendar);
  }
  
  /**
   * Converts a Calendar value into an ISO 8601 date-time string.
   * <br/>JAXB is used to format the string. 
   * @param calendar the calendar value
   * @return the formatted string 
   */
  public static String printIsoDateTime(Calendar calendar) {
    return javax.xml.bind.DatatypeConverter.printDateTime(calendar);
  }

}
