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
  
  public String translate(String value) {
    for (DcatDomain.DcatMapping mapping: this) {
      if (mapping.matches(value)) {
        return mapping.getValue(value);
      }
    }
    return value;
  }
  
  public static class DcatMapping {
    private final Pattern pattern;
    private final IDcatGetter valueGetter;

    public DcatMapping(Pattern pattern, IDcatGetter valueGetter) {
      this.pattern = pattern;
      this.valueGetter = valueGetter!=null? valueGetter: new DcatCopyGetter();
    }

    public DcatMapping(Pattern pattern, String value) {
      this.pattern = pattern;
      this.valueGetter = new DcatFixedGetter(value);
    }

    public DcatMapping(Pattern pattern) {
      this.pattern = pattern;
      this.valueGetter = new DcatCopyGetter();
    }

    public String getValue(String input) {
      return valueGetter.get(input);
    }
    
    public boolean matches(String input) {
      return pattern.matcher(input).matches();
    }
  }
  
  public static interface IDcatGetter {
    String get(String input);
  }
  
  public static class DcatFixedGetter implements IDcatGetter {
    private final String value;

    public DcatFixedGetter(String value) {
      this.value = value;
    }

    @Override
    public String get(String input) {
      return value;
    }
  }
  
  public static class DcatCopyGetter implements IDcatGetter {

    @Override
    public String get(String input) {
      return input;
    }
    
  }
}
