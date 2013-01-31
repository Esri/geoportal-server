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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;

/**
 * Provides utilities supporting request validation.
 */
public class ValidationHelper {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public ValidationHelper() {}
  
  /** methods ================================================================= */
  
  /**
   * Negotiates a requested parameter value.
   * @param supported the supported values
   * @param locator the OwsException locator
   * @param parsed the list of parsed values
   * @param required <code>true</code if this parameter value is required
   * @return the negotiated value
   * @throws OwsException if validation fails
   */
  public String negotiateValue(ISupportedValues supported,
                               String locator, 
                               String[] parsed, 
                               boolean required) 
    throws OwsException {
    if (parsed == null) {
      if (required) {
        String msg = "The parameter value was missing.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      }
    } else if (parsed.length == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      if (supported != null) {
        for (String value: parsed) {
          value = Val.chkStr(value);
          if (value.length() == 0) {
            String msg = "The parameter value was empty.";
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
          } else {
            String validValue = supported.getSupportedValue(value);
            if (validValue != null) {
              return validValue;
            }
          }
        }
      }
      String owsCode = OwsException.OWSCODE_InvalidParameterValue;
      String lcLocator = Val.chkStr(locator).toLowerCase();
      if (lcLocator.endsWith("acceptversions") || lcLocator.endsWith("ows:version")) {
        owsCode = OwsException.OWSCODE_VersionNegotiationFailed;
      } else if (lcLocator.endsWith("acceptformats") || lcLocator.endsWith("ows:outputformat")) {
        
        // The response to a GetCapabilities request that includes an unsupported 
        // AcceptFormats parameter value must include the default XML representation 
        // of the capabilities document.
        return null;
      }
      String msg = "None of the supplied parameter values are supported.";
      throw new OwsException(owsCode,locator,msg);
    }
    return null;
  }
  
  /**
   * Validates a requested parameter value.
   * @param locator the OwsException locator
   * @param parsed the list of parsed values
   * @param required <code>true</code if this parameter value is required
   * @return the valid value
   * @throws OwsException if validation fails
   */
  public String validateValue(String locator, 
                              String[] parsed, 
                              boolean required) 
    throws OwsException {
    if (parsed == null) {
      if (required) {
        String msg = "The parameter value was missing.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      }
    } else if (parsed.length == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else if (parsed.length > 1) {
      String msg = "More than one parameter value was supplied.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      String value = Val.chkStr(parsed[0]);
      if (value.length() == 0) {
        String msg = "The parameter value was empty.";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
      } else {
        return value;
      }
    }
    return null;
  }
  
  /**
   * Validates a requested parameter value.
   * @param supported the supported values
   * @param locator the OwsException locator
   * @param parsed the list of parsed values
   * @param required <code>true</code if this parameter value is required
   * @return the valid value
   * @throws OwsException if validation fails
   */
  public String validateValue(ISupportedValues supported,
                              String locator, 
                              String[] parsed, 
                              boolean required) 
    throws OwsException {
    if (parsed == null) {
      if (required) {
        String msg = "The parameter value was missing.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      }
    } else if (parsed.length == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else if (parsed.length > 1) {
      String msg = "More than one parameter value was supplied.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      String value = Val.chkStr(parsed[0]);
      if (value.length() == 0) {
        String msg = "The parameter value was empty.";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
      } else if (supported == null) {
        String msg = "This parameter value is not supported: "+value;
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
      } else {
        String validValue = supported.getSupportedValue(value);
        if (validValue == null) {
          String msg = "This parameter value is not supported: "+value;
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
        } else {
          return validValue;
        }        
      }
    }
    return null;
  }
    
  /**
   * Populates a collection of requested parameter values.
   * @param locator the OwsException locator
   * @param parsed the parsed values
   * @param required <code>true</code if this parameter value is required
   * @return the valid values
   * @throws OwsException if validation fails
   */
  public StringSet validateValues(String locator, 
                                  String[] parsed, 
                                  boolean required) 
    throws OwsException {
    StringSet requested = new StringSet();
    if (parsed == null) {
      if (required) {
        String msg = "The parameter value was missing.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      }
    } else if (parsed.length == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      int count = 0;
      for (String value: parsed) {
        value = Val.chkStr(value);
        if (value.length() == 0) {
          String msg = "The parameter value was empty.";
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
        } else {
          requested.add(value);
          count++;
        }
      }
      if (required && (count == 0)) {
        String msg = "No valid values were supplied.";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
      }
    }
    return requested;
  }
  
  /**
   * Validates a collection of requested parameter values.
   * @param supported the supported values
   * @param locator the OwsException locator
   * @param parsed the parsed values
   * @param required <code>true</code if this parameter value is required
   * @return the valid values
   * @throws OwsException if validation fails
   */
  public StringSet validateValues(ISupportedValues supported,
                                  String locator, 
                                  String[] parsed, 
                                  boolean required) 
    throws OwsException {
    StringSet requested = new StringSet();
    if (parsed == null) {
      if (required) {
        String msg = "The parameter value was missing.";
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
      }
    } else if (parsed.length == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      for (String value: parsed) {
        value = Val.chkStr(value);
        if (value.length() == 0) {
          String msg = "The parameter value was empty.";
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
        } else if (supported == null) {
          String msg = "This parameter value is not supported: "+value;
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
        } else {
          String validValue = supported.getSupportedValue(value);
          if (validValue == null) {
            String msg = "This parameter value is not supported: "+value;
            throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
          } else {
            requested.add(validValue);
          }
        }
      }
    }
    return requested;
  }
    
}
