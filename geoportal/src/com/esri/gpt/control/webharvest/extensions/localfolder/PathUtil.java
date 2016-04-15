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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Path util.
 */
public class PathUtil {
  
  /**
   * Splits path by separator
   * @param p path
   * @return list of elements
   */
  public static List<String> splitPath(String p) {
    ArrayList<String> path = new ArrayList<String>();
    for (String t: p.split("/|\\\\")) {
      if (t!=null && !t.trim().isEmpty()) {
        path.add(sanitizeFileName(t.trim()));
      }
    }
    return path;
  }
  
  /**
   * Splits path by separator.
   * @param u URL
   * @return list of elements
   */
  public static  List<String> splitPath(URL u) {
    return splitPath(u.getPath());
  }
  
  /**
   * Splits path by separator.
   * @param f file
   * @return list of elements
   */
  public static List<String> splitPath(File f) {
    LinkedList<String> path = new LinkedList<String>();
    while (f!=null) {
      String name = f.getParentFile()!=null? f.getName(): f.getPath();
      path.push(name);
      f = f.getParentFile();
    }
    return path;
  }
  
  /**
   * Sanitizes file name.
   * @param name arbitrary file name
   * @return safe file name
   */
  public static String sanitizeFileName(String name) {
    return name.replaceAll("[^a-zA-Z0-9\\._{}-]+", "_");
  }
}
