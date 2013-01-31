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
package com.esri.gpt.catalog.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchFilterTemporal.  Filter Bean class holding date information.
 */
@SuppressWarnings("serial")
public class SearchFilterTemporal implements ISearchFilter, 
  ISearchFilterTemporal {

// class variables =============================================================

/** Class logger **/
private static final Logger LOG = 
  Logger.getLogger(SearchFilterTemporal.class.getCanonicalName());

/**
 * The Enum SelectedTimePeriod.
 */
public static enum SelectedTimePeriod {

/** The any time. */
any,

/** The period Before and after. */ 
beforeAndOrAfterPeriod,

}

/** The default time format. */
public static String DEFAULT_TIME_FORMAT = DateProxy.DEFAULT_TIME_FORMAT;

/** object for the default time format */
private static SimpleDateFormat DEFAULT_TIME_FORMAT_OBJ = 
  new SimpleDateFormat(DEFAULT_TIME_FORMAT );

/**
 * The Enum SaveParams.
 */
private static enum SaveParams {

/** The selected modified time. */
selectedModifiedTime,

/** The modified date to. */
modifiedDateTo,

/** The modified date from. */
modifiedDateFrom,

/** The modified date after. 
modifiedDateAfter,*/

}

// instance variables ==========================================================
/** The date modified to. */
private String dateModfiedTo;

/** The date modified from. */
private String dateModifiedFrom;

/** The selected modified date option. */
private String selectedModifiedDateOption;

// constructor =================================================================
/**
 * Instantiates a new search filter temporal.
 */
public SearchFilterTemporal() {
  reset();
}
// properties ==================================================================
/**
 * Gets the date modified to.
 * 
 * @return the date modified to (trimmed, never null)
 */
public String getDateModifiedTo() {
  return Val.chkStr(dateModfiedTo);
}

/**
 * Sets the date modified to.
 * 
 * @param dateModfiedTo the new date modified to
 */
public void setDateModifiedTo(String dateModfiedTo) {
  this.dateModfiedTo = dateModfiedTo;
}

/**
 * Gets the date modified to as Date object.
 * 
 * @return the date modified to (possibly null)
 */
public Date getDateModifiedToAsDate() {
  String dateTo = this.getDateModifiedTo();
  if ("".equals(dateTo)) {
    return null;
  }
  try {
    DateProxy dateProxy = new DateProxy();
    dateProxy.setDate(dateTo);
    this.setDateModifiedTo(
        DEFAULT_TIME_FORMAT_OBJ.format(dateProxy.asToTimestamp()));
    return dateProxy.asToTimestamp();
  } catch (Exception e) {
    LOG.log(Level.WARNING, "to date = " + dateTo + "could not convert to"
        + " timestamp object", e);
  }
  return null;
}

/**
 * Gets the date modified from.
 * 
 * @return the date modified from (trimmed, never null)
 */
public String getDateModifiedFrom() {
 
  return Val.chkStr(dateModifiedFrom);
}

/**
 * Gets the date modified From as Date object.
 * 
 * @return the date modified to (possibly null)
 */
public Date getDateModifiedFromAsDate() {
  String dateFrom = this.getDateModifiedFrom();
  if("".equals(dateFrom)) {
    return null;
  }
  try {
    DateProxy dateProxy = new DateProxy();
    dateProxy.setDate(dateFrom);
    this.setDateModifiedFrom(
        DEFAULT_TIME_FORMAT_OBJ.format(dateProxy.asFromTimestamp()));
    return dateProxy.asToTimestamp();
  } catch (Exception e) {
    LOG.log(Level.WARNING, "from date = " + dateFrom + "could not convert to"
        + " timestamp object", e);
  }
  return null;
   
}

/**
 * Sets the date modified from.
 * 
 * @param dateModifiedFrom the new date modified from
 */
public void setDateModifiedFrom(String dateModifiedFrom) {
  this.dateModifiedFrom = dateModifiedFrom;
}

/**
 * Gets the selected modified date option.
 * 
 * @return the selected modified date option (trimmed, never null or empty, 
 * default = "any")
 */
public String getSelectedModifiedDateOption() {
  if(selectedModifiedDateOption == null || 
      "".equals(selectedModifiedDateOption)) {
    selectedModifiedDateOption = SelectedTimePeriod.any.toString();
  }
  return Val.chkStr(selectedModifiedDateOption);
}

/**
 * Sets the selected modified date option.
 * 
 * @param selectedModifiedDateOption the new selected modified date option
 */
public void setSelectedModifiedDateOption(String selectedModifiedDateOption) {
  this.selectedModifiedDateOption = Val.chkStr(selectedModifiedDateOption);
}

// methods =====================================================================
/**
 * Search parameters
 * @return (never null)
 */
public SearchParameterMap getParams() {
  return this.getParams(new SimpleDateFormat(DEFAULT_TIME_FORMAT));
}

/**
 * Checks if is equals.
 * 
 * @param obj the obj
 * 
 * @return true, if checks if is equals
 */
public boolean isEquals(Object obj) {
  if(!(obj instanceof SearchFilterTemporal)) {
    return false;
  }
  SearchFilterTemporal foreignObject =(SearchFilterTemporal) obj;
  return this.getParams().equals(foreignObject.getParams());
}

/**
 * Checks if an object argument is the same as <b>this.
 * @param obj
 * @return true or false if object is equal to <b>this</b> this or not
 */
@Override
public boolean equals(Object obj) {
  return isEquals(obj);
}

/**
 * Reset this object
 */
public void reset() {
  this.setDateModifiedTo(null);
  this.setDateModifiedFrom(null);
  this.setSelectedModifiedDateOption(null);
  
}

/**
 * Sets the params.
 * 
 * @param parameterMap the parameter map
 * 
 * @throws SearchException the search exception
 */
public void setParams(SearchParameterMap parameterMap) throws SearchException {
  
  if (parameterMap == null) {
    return;
  }
  SearchParameterMap.Value value = parameterMap
      .get(SaveParams.selectedModifiedTime.name());
  this.setSelectedModifiedDateOption(value.getParamValue());

  try {
    value = parameterMap.get(SaveParams.modifiedDateFrom.name());
    if (value != null) {
      String format = value.getInfo();
      if (format != null && !"".equals(format)) {
        SimpleDateFormat sdfFormat = new SimpleDateFormat(format);
        Date date = sdfFormat.parse(value.getParamValue());
        this.setDateModifiedFrom(DEFAULT_TIME_FORMAT_OBJ.format(date));
      }
    }
  } catch (Exception e) {
    this.setDateModifiedFrom(null);
    LOG.log(Level.WARNING,
        "Could not get modified date from while deserializing" + e.getMessage());
  }
  
  try {
    value = parameterMap.get(SaveParams.modifiedDateTo.name());
    if (value != null) {
      String format = value.getInfo();
      if (format != null && !"".equals(format)) {
        SimpleDateFormat sdfFormat = new SimpleDateFormat(format);
        Date date = sdfFormat.parse(value.getParamValue());
        this.setDateModifiedTo(DEFAULT_TIME_FORMAT_OBJ.format(date));
      }
    }
  } catch (Exception e) {
    this.setDateModifiedTo(null);
    LOG.log(Level.WARNING,
        "Could not get modified date to while deserializing " + e.getMessage());
  }
}

/**
 * Validate.
 * 
 * @throws SearchException the search exception
 */
public void validate() throws SearchException {
  
  // Since this is called during business logic execution
  // Lets do some correction
  if(this.getDateModifiedFromAsDate() == null){
    this.setDateModifiedFrom(null);
  }
  if(this.getDateModifiedToAsDate() == null) {
    this.setDateModifiedTo(null);
  }
  
  if(this.getDateModifiedToAsDate() ==  null && 
      this.getDateModifiedFromAsDate() == null) {
      this.setSelectedModifiedDateOption(SelectedTimePeriod.any.name());
  } else if(this.getDateModifiedFromAsDate() != null && 
      this.getDateModifiedToAsDate() != null) {
    if(this.getDateModifiedFromAsDate().after(this.getDateModifiedToAsDate())) {
      String tmp = this.getDateModifiedFrom();
      this.setDateModifiedFrom(this.getDateModifiedTo());
      this.setDateModifiedTo(tmp);
    }
    
  }
  
 
  
  
}

/**
 * Gets the params.
 * 
 * @param format the format (if null, then default is used)
 * 
 * @return the params (never null)
 */
public SearchParameterMap getParams(SimpleDateFormat format) {
  
  try {
    validate();
  } catch (SearchException e) {
   LOG.log(Level.WARNING,"", e);
  }
  SearchParameterMap map = new SearchParameterMap();
  map.put(SaveParams.selectedModifiedTime.name(), 
           map.new Value(this.getSelectedModifiedDateOption()));
  
  Date fromDate = this.getDateModifiedFromAsDate();
  if(fromDate != null  && format != null) {
    map.put(SaveParams.modifiedDateFrom.name(), 
        map.new Value(format.format(fromDate), format.toPattern()));
  }
  
  Date toDate = this.getDateModifiedToAsDate();
  if(toDate != null && format != null) {
    map.put(SaveParams.modifiedDateTo.name(), 
        map.new Value(format.format(toDate), format.toPattern()));
  }
  
  return map;
}

/**
 * String representation of class
 * @return string representation
 */
@Override
public String toString(){
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
    this.getParams().toString()
    + "\n===========================}";
}






}
