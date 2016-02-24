/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.gpt.control.webharvest.extensions.localfolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to handle list of strings
 * @author Esri, Inc.
 */
public class StringListUtil {
  /**
   * Gets head of the list.
   * @param lst source list
   * @param to end index
   * @return head 
   */
  public static List<String> head(List<String> lst, int to) {
    return lst.subList(0, Math.max(0,Math.min(to, lst.size())));
  }
  /**
   * Gets tail of the list
   * @param lst source list
   * @param from start index
   * @return 
   */
  public static List<String> tail(List<String> lst, int from) {
    return lst.subList(Math.max(0,Math.min(from, lst.size())), lst.size());
  }
  
  /**
   * Checks if list ends with the specific suffix
   * @param lst source list 
   * @param suffix suffix
   * @return <code>true</code> if list ends with suffix
   */
  private static boolean endsWith(List<String> lst, List<String> suffix) {
    int idx = lst.size() - suffix.size();
    
    if (idx<0) {
      return false;
    }
    
    for (int i=0; i<suffix.size(); i++) {
      if (!lst.get(idx+i).equals(suffix.get(i))) {
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Joins two lists in such a way that common end of the first list and common 
   * start with the second list are merged.
   * @param lst1 first list
   * @param lst2 second list
   * @return merged list
   */
  public static List<String> merge(List<String> lst1, List<String> lst2) {
    int rootStart = lst1.size()>lst2.size()? lst1.size()-lst2.size(): 0;
    int suffixEnd = lst2.size()>lst1.size()? lst1.size(): lst2.size();
    boolean ends = false;
    
    while (rootStart<lst1.size() && suffixEnd>=0) {
      ends = endsWith(lst1,head(lst2,suffixEnd));
      if (ends) break;
      rootStart++;
      suffixEnd--;
    }
    
    ArrayList<String> acc = new ArrayList<String>(lst1);
    if (ends) {
      acc.addAll(tail(lst2,suffixEnd));
    } else {
      acc.addAll(lst2);
    }
    
    return acc;
  }
}
