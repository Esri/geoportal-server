/*
 * Copyright 2015 Esri.
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
package com.esri.gpt.framework.ckan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * CKAN parser.
 */
public class CkanParser {
  /**
   * Parse package list.
   * @param packageList package list as string
   * @return package list
   * @throws JSONException if error parsing JSON
   */
  public static CkanPackageList parsePackageList(String packageList) throws JSONException {
    return makePackageList(new JSONObject(packageList).getJSONArray("result"));
    //return new CkanPackageListAdaptor(new JSONObject(packageList).getJSONArray("result"));
  }
  
  /**
   * Parse package list.
   * @param pkg package list as array
   * @return package list
   * @throws JSONException if error parsing JSON
   */
  public static CkanPackageList makePackageList(JSONArray pkg) throws JSONException {
    return new CkanPackageListAdaptor(pkg);
  }
  
  /**
   * Parse package.
   * @param pkg package as string
   * @return package
   * @throws JSONException if error parsing JSON
   */
  public static CkanPackage parsePackage(String pkg) throws JSONException {
    return makePackage(new JSONObject(pkg));
  }
  
  /**
   * Parse package.
   * @param pkg package as object
   * @return package
   * @throws JSONException if error parsing JSON
   */
  public static CkanPackage makePackage(JSONObject pkg) throws JSONException {
    return new CkanPackageAdaptor(pkg);
  }
}
