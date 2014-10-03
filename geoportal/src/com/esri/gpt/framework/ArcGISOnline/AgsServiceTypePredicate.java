/*
 * Copyright 2014 Esri, Inc..
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

package com.esri.gpt.framework.ArcGISOnline;

import java.util.Comparator;

/**
 * AGS service type predicate.
 */
public abstract class AgsServiceTypePredicate implements ITypePredicate {
  private Comparator<String> comparator;
  private String typeName;
  protected abstract String getTypeName();
  
  public AgsServiceTypePredicate() {
    this.comparator = new EndStringComparator();
  }
  
  public AgsServiceTypePredicate(String typeName) {
    this.comparator = new EndStringComparator();
    this.typeName = typeName;
  }
  
  public AgsServiceTypePredicate(Comparator<String> comparator) {
    this.comparator = comparator!=null? comparator: new EndStringComparator();
  }
  
  public AgsServiceTypePredicate(Comparator<String> comparator, String typeName) {
    this.comparator = comparator!=null? comparator: new EndStringComparator();
    this.typeName = typeName;
  }

  @Override
  public boolean matches(String url) {
    return this.comparator.compare(url,getRealTypeName())==0;
  }
  
  protected String getRealTypeName() {
    return typeName!=null? typeName: getTypeName().replace("Service", "Server").replaceAll("\\s+", "");
  }

  public static class EndStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
      return o1.toLowerCase().endsWith(o2.toLowerCase())? 0: 1;
    }
    
  }
}
