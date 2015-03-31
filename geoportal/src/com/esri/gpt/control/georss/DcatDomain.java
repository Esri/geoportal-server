/*
 * Copyright 2015 Esri, Inc..
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
package com.esri.gpt.control.georss;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Dcat domain of the field.
 */
public class DcatDomain extends ArrayList<DcatDomain.DcatMapping> {
  private static final String COPY_TAG = "*";
  private static final Logger L = Logger.getLogger(DcatDomain.class.getCanonicalName());
  
  /**
   * Adds domain mapping.
   * @param from pattern of the input value
   * @param value output value
   */
  public void addMapping(String from, String value) {
    try {
      Pattern fromPattern = Pattern.compile(from,Pattern.CASE_INSENSITIVE);
      if (COPY_TAG.equals(value)) {
        add(new DcatMapping(fromPattern));
      } else {
        add(new DcatMapping(fromPattern, value));
      }
    } catch (Exception ex) {
      L.log(Level.WARNING, "Invalid DCAT domain pattern: "+from, ex);
    }
  }
  
  /**
   * Translates input value into an appropriate domain value.
   * @param value input value
   * @param defValue default value
   * @return 
   */
  public String translate(String value, String defValue) {
    for (DcatDomain.DcatMapping mapping: this) {
      if (mapping.matches(value)) {
        return mapping.getValue(value);
      }
    }
    return defValue;
  }
  
  /**
   * Translates input value into an appropriate domain value.
   * <p>
   * If translation not found, input value is returned.
   * </p>
   * @param value input value
   * @return 
   */
  public String translate(String value) {
    return translate(value,value);
  }
  
  /**
   * Domain mapping.
   * <p>
   * Association between pattern applied on the input value and output value.
   * </p>
   */
  public static class DcatMapping {
    private final Pattern pattern;
    private final IDcatGetter valueGetter;

    /**
     * Creates instance of the mapping.
     * @param pattern pattern
     * @param valueGetter value getter used to get value if input value matches pattern
     */
    public DcatMapping(Pattern pattern, IDcatGetter valueGetter) {
      this.pattern = pattern;
      this.valueGetter = valueGetter!=null? valueGetter: new DcatCopyGetter();
    }

    /**
     * Creates instance of the mapping.
     * @param pattern pattern
     * @param value value to be returned if input value matches pattern
     */
    public DcatMapping(Pattern pattern, String value) {
      this.pattern = pattern;
      this.valueGetter = new DcatFixedGetter(value);
    }

    /**
     * Creates instance of the mapping.
     * <p>
     * If input value matches pattern, is simply being passed by.
     * </p>
     * @param pattern pattern
     */
    public DcatMapping(Pattern pattern) {
      this.pattern = pattern;
      this.valueGetter = new DcatCopyGetter();
    }

    /**
     * Gets value.
     * @param input input value
     * @return output value
     */
    public String getValue(String input) {
      return valueGetter.get(input);
    }
    
    /**
     * Checks if input value matches pattern.
     * @param input input value
     * @return <code>true</code> if input value matches pattern
     */
    public boolean matches(String input) {
      return pattern.matcher(input).matches();
    }
  }
  
  /**
   * Value getter.
   */
  public static interface IDcatGetter {
    /**
     * Gets value.
     * @param input input value
     * @return output value
     */
    String get(String input);
  }
  
  /**
   * Fixed value getter.
   * <p>
   * Always returns a fixed value.
   * </p>
   */
  public static class DcatFixedGetter implements IDcatGetter {
    private final String value;

    /**
     * Creates instance of the getter.
     * @param value fixed value
     */
    public DcatFixedGetter(String value) {
      this.value = value;
    }

    @Override
    public String get(String input) {
      return value;
    }
  }
  
  /**
   * Copy getter.
   * <p>
   * It always copies input value into output value.
   * </p>
   */
  public static class DcatCopyGetter implements IDcatGetter {

    @Override
    public String get(String input) {
      return input;
    }
    
  }
}
