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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Esri, Inc.
 */
public class StringListUtil {
  public static String [] makeArray(String...values) {
    return values;
  }
  public static List<String> makeList(String...values) {
    return Arrays.asList(values);
  }
  public static String toString(List<String> lst) {
    StringBuilder sb = new StringBuilder();
    for (String s: lst) {
      sb.append(sb.length()>0?",":"").append(s);
    }
    return "[" + sb + "]";
  }
  public static List<String> head(List<String> lst, int to) {
    return lst.subList(0, Math.max(0,Math.min(to, lst.size())));
  }
  public static List<String> tail(List<String> lst, int from) {
    return lst.subList(Math.max(0,Math.min(from, lst.size())), lst.size());
  }
  public static List<String> last(List<String> lst, int length) {
    return lst.subList(Math.max(0,Math.min(lst.size(),lst.size()-length)), lst.size());
  }
  
  public static boolean endsWith(List<String> root, List<String> suffix) {
    int idx = root.size() - suffix.size();
    
    if (idx<0) {
      return false;
    }
    
    for (int i=0; i<suffix.size(); i++) {
      if (!root.get(idx+i).equals(suffix.get(i))) {
        return false;
      }
    }
    
    return true;
  }
  
  public static List<String> joinRest(List<String> root, List<String> suffix) {
    int rootStart = root.size()>suffix.size()? root.size()-suffix.size(): 0;
    int suffixEnd = suffix.size()>root.size()? root.size(): suffix.size();
    boolean ends = false;
    
    while (rootStart<root.size() && suffixEnd>=0) {
      ends = endsWith(root,head(suffix,suffixEnd));
      if (ends) break;
      rootStart++;
      suffixEnd--;
    }
    
    ArrayList<String> acc = new ArrayList<String>(root);
    if (ends) {
      acc.addAll(tail(suffix,suffixEnd));
    } else {
      acc.addAll(suffix);
    }
    
    return acc;
  }
}
