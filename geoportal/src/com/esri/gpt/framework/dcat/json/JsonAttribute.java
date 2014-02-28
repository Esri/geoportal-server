/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.dcat.json;

/**
 * Raw DCAT attribute.
 */
public class JsonAttribute {
  private TYPE type;
  private getter getter;
  
  /**
   * Creates instance of the string attribute.
   * @param value value
   */
  public JsonAttribute(String value) {
    this.type = TYPE.STRING;
    this.getter = new FromStringGetter(value);
  }
  
  /**
   * Creates instance of the double attribute.
   * @param value value
   */
  public JsonAttribute(Double value) {
    this.type = TYPE.NUMBER;
    this.getter = new FromNumberGetter(value);
  }
  
  /**
   * Creates instance of the boolean attribute.
   * @param value value
   */
  public JsonAttribute(Boolean value) {
    this.type = TYPE.BOOLEAN;
    this.getter = new FromBooleanGetter(value);
  }
  
  /**
   * Gets value as number.
   * @return number
   */
  public Double getNumber() {
    return getter.getNumericValue();
  }
  
  /**
   * Gets value as string.
   * @return string
   */
  public String getString() {
    return getter.getStringValue();
  }
  
  /**
   * Gets value as boolean.
   * @return boolean
   */
  public Boolean getBoolean() {
    return getter.getBooleanValue();
  }
  
  /**
   * Gets original value type.
   * @return type
   */
  public TYPE getType() {
    return type;
  }
  
  
  private interface getter {
    String getStringValue();
    Double getNumericValue();
    Boolean getBooleanValue();
  }
  
  private class FromBooleanGetter implements getter {
    private Boolean booleanValue;
    
    public FromBooleanGetter(Boolean booleanValue) {
      this.booleanValue = booleanValue;
    }

    @Override
    public String getStringValue() {
      return booleanValue.toString();
    }

    @Override
    public Double getNumericValue() {
      return Double.NaN;
    }

    @Override
    public Boolean getBooleanValue() {
      return booleanValue;
    }
    
    @Override
    public String toString() {
      return booleanValue.toString();
    }
  }
  
  private class FromStringGetter implements getter {
    private String stringValue;
    
    public FromStringGetter(String stringValue) {
      this.stringValue = stringValue;
    }

    @Override
    public String getStringValue() {
      return stringValue;
    }

    @Override
    public Double getNumericValue() {
      try {
        return Double.parseDouble(stringValue);
      } catch (NumberFormatException ex) {
        return Double.NaN;
      }
    }
    
    @Override
    public String toString() {
      return "\"" + stringValue + "\"";
    }

    @Override
    public Boolean getBooleanValue() {
      return Boolean.parseBoolean(stringValue);
    }
  }
  
  private class FromNumberGetter implements getter {
    private Double numericValue;
    
    public FromNumberGetter(Double numericValue) {
      this.numericValue = numericValue;
    }

    @Override
    public String getStringValue() {
      return numericValue.toString();
    }

    @Override
    public Double getNumericValue() {
      return numericValue;
    }
    
    @Override
    public String toString() {
      long longValue = numericValue.longValue();
      double doubleValue = numericValue.doubleValue();
      return longValue<doubleValue? numericValue.toString(): ""+longValue;
    }

    @Override
    public Boolean getBooleanValue() {
      return Boolean.FALSE;
    }
    
  }
  
  @Override
  public String toString() {
    return getter.toString();
  }
  
  /**
   * Type.
   */
  public static enum TYPE {
    /** string */
    STRING,
    /** number */
    NUMBER,
    /** boolean */
    BOOLEAN
  }
}
