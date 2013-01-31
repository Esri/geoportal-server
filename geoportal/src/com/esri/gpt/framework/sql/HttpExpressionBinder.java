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
package com.esri.gpt.framework.sql;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Aids in the process of binding HTTP request parameters to SQL expressions.
 */
public class HttpExpressionBinder extends ExpressionBinder {
  
  /** class variables ========================================================= */
  
  /** Parameter type: "Double" */
  public static final String PARAMETERTYPE_DOUBLE = "Double";
  
  /** Parameter type: "Integer" */
  public static final String PARAMETERTYPE_INTEGER = "Integer";
  
  /** Parameter type: "String" */
  public static final String PARAMETERTYPE_STRING = "String";
  
  /** instance variables ====================================================== */
  private Map<String,String[]> parameters = new HashMap<String,String[]>(); 
  private HttpServletRequest   request;
  
  /** constructors ============================================================ */
  
  /**
   * Constructor
   * @param request the HTTP servlet rquest
   */
  @SuppressWarnings(value = "unchecked")
  public HttpExpressionBinder(HttpServletRequest request) {
    this.request = request;
    
    // marshal the servlet parameter map into a map with low case keys
    Enumeration enNames = request.getParameterNames();
    while (enNames.hasMoreElements()) {
      String key = (String)enNames.nextElement();
      parameters.put(key.toLowerCase(),request.getParameterValues(key));
    }
  }
  
  /** methods ================================================================= */
      
  /**
   * Gets the first object value associated with a parameter name.
   * <br/>The parameterType is used to convert HTTP key-value pair strings to an
   * object of the supplied parameter type.
   * @param parameter the parameter name
   * @param parameterType the parameter type (HttpExpressionBinder.PARAMETERTYPE_*)
   * @return the converted object value (null if none)
   */
  private Object getObjectValue(String parameter, String parameterType) {
    String value = Val.chkStr(getParameter(parameter));
    if (value.length() > 0) {
      return this.makeObjectValue(value,parameterType);
    }  else {
      return null;
    }
  }
  
  /**
   * Gets the list of object values associated with a parameter name.
   * <br/>The parameterType is used to convert HTTP key-value pair strings to an
   * object of the supplied parameter type.
   * @param parameter the parameter name
   * @paaram delimiter a delimiter to tokenize values (can be null)
   * @param parameterType the parameter type (HttpExpressionBinder.PARAMETERTYPE_*)
   * @return the converted object values (empty list if none were located)
   */
  private List<Object> getObjectValues(String parameter, String delimiter, String parameterType) {
    String[] values = this.parameters.get(parameter.toLowerCase());
    List<Object> oValues = new ArrayList<Object>();
    
    if (values != null) {
      for (String value: values) {
        value = Val.chkStr(value);
        if (value.length() > 0) {
          if ((delimiter != null) && (delimiter.length() > 0)) {
            String[] tokens = Val.tokenize(value,delimiter);
            for (String token: tokens) {
              String sToken = Val.chkStr(token);
              if (sToken.length() > 0) {
                oValues.add(this.makeObjectValue(sToken,parameterType));
              }
            }
          } else {
            oValues.add(this.makeObjectValue(value,parameterType));
          }
        }
      }
    }    
    
    return oValues;
  }
  
  /**
   * Gets the first value associated with a parameter name.
   * @param parameter the parameter name
   * @return the parameter value (null if none)
   */
  private String getParameter(String parameter) {
    String[] values = this.getParameterValues(parameter);
    if (values.length > 0) {
      return values[0];
    } else {
      return null;
    }
  }
    
  /**
   * Gets the array of values associated with a parameter name.
   * @param parameter the parameter name
   * @return the parameter values array (empty array if none were located)
   */
  private String[] getParameterValues(String parameter) {
    String[] values = this.parameters.get(parameter.toLowerCase());
    if (values != null) {
      return values;
    } else {
      return new String[0];
    }
  }
    
  /**
   * Makes an object from a parameter value.
   * <br/>The parameterType is used to convert HTTP key-value pair strings to an
   * object of the supplied parameter type.
   * @param parameterValue the parameter value
   * @param parameterType the parameter type (HttpExpressionBinder.PARAMETERTYPE_*)
   * @return the converted object value
   */
  private Object makeObjectValue(String parameterValue, String parameterType) {
    Object value = null;
    try {
      if (parameterType.equalsIgnoreCase(HttpExpressionBinder.PARAMETERTYPE_DOUBLE)) {
        value = new Double(parameterValue);
      } else if (parameterType.equalsIgnoreCase(HttpExpressionBinder.PARAMETERTYPE_INTEGER)) {
        value = new Integer(parameterValue);
      } else if (parameterType.equalsIgnoreCase(HttpExpressionBinder.PARAMETERTYPE_STRING)) {
        value = new String(parameterValue);
      } else {
        value = "";
      }
    } catch (NumberFormatException nfe) {
      value = "";
    }
    return value;
  }
  
  /**
   * Parses the HTTP resquest, binding a parameter value to an SQL field.
   * @param parameter the HTTP request parameter name
   * @param field the SQL field name
   * @param operator the SQL operator examples: =, !=, >, >=, <, <=, LIKE, ...
   * @param parameterType the parameter type (HttpExpressionBinder.PARAMETERTYPE_*)
   */
  public void parse(String parameter, String field, String operator, String parameterType) { 
    Object binding = this.getObjectValue(parameter,parameterType);
    if (binding != null) {
      this.addBinding(field,operator,binding);
    }
  }
  
  /**
   * Parses the HTTP resquest, binding parameter values to an SQL field.
   * <br/>This method considers multiple valuers per parameter name.
   * <br/>The values collection is connected by logical "OR" operators.
   * @param parameter the HTTP request parameter name
   * @param field the SQL field name
   * @param operator the SQL operator examples: =, !=, >, >=, <, <=, LIKE, ...
   * @param delimiter a delimiter to tokenize values (can be null)
   * @param parameterType the parameter type (HttpExpressionBinder.PARAMETERTYPE_*)
   */
  public void parse(String parameter, String field, String operator, String delimiter, String parameterType) { 
    List<Object> bindings = this.getObjectValues(parameter,delimiter,parameterType);
    if (bindings != null) {
      this.addBindings(field,operator,bindings.toArray());
    }
  }
  
  /**
   * Parses the HTTP resquest, binding a parameter value to an SQL field.
   * <br/>This method is useful for VARCHAR type SQL fields.
   * @param parameter the HTTP request parameter name
   * @param field the SQL field name
   * @param forceUpper force an upper case comparison
   * @param forceLike for an SQL LIKE operator 
   */
  public void parse(String parameter, String field, boolean forceUpper, boolean forceLike) {
    String value = Val.chkStr(getParameter(parameter));
    if (value.length() > 0) {
      this.addBinding(field,value,forceUpper,forceLike);
    }    
  }
  
  /**
   * Parses the HTTP resquest, binding parameter values to an SQL field.
   * <br/>This method considers multiple valuers per parameter name.
   * <br/>The values collection is connected by logical "OR" operators.
   * <br/>This method is useful for VARCHAR type SQL fields.
   * @param parameter the HTTP request parameter name
   * @param field the SQL field name
   * @param delimiter a delimiter to tokenize values (can be null)
   * @param forceUpper force an upper case comparison
   * @param forceLike for an SQL LIKE operator 
   */
  public void parse(String parameter, String field, String delimiter, boolean forceUpper, boolean forceLike) {
    String parameterType = HttpExpressionBinder.PARAMETERTYPE_STRING;
    List<Object> bindings = this.getObjectValues(parameter,delimiter,parameterType);
    if (bindings != null) {
      this.addBindings(field,bindings.toArray(new String[0]),forceUpper,forceLike);
    } 
  }
   
}
