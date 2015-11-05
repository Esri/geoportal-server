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
package com.esri.gpt.framework.ckan;

import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.crawl.HttpCrawlRequest;
import com.esri.gpt.framework.robots.Bots;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.httpclient.HttpMethodBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * CKAN Test.
 */
public class CkanTest {

  protected void onPackage(CkanPackage pkg) throws Exception {
    assertNotNull(pkg);
    System.out.println("" + pkg);
    for (CkanResource res : pkg.getResources()) {
      onResource(res);
    }
  }

  protected void onResource(CkanResource res) {
    System.out.println("  " + res);
  }

  @Test
  public void ckanTest() throws Exception {
    //String baseUrl = "http://demo.ckan.org/api/3/action";
    //String baseUrl = "http://catalog.data.gov/api/3/action";
    runTest("http://demo.ckan.org/api/3/action", 20);
  }

  public void runTest(String baseUrl, int max) throws Exception {
    int counter = 0;
    JSONObject packageList = readJsonData(new URL(baseUrl + "/package_list"));
    if (packageList.has("result") && packageList.optBoolean("success", false)) {
      if (packageList.get("result") instanceof JSONArray) {
        // result of /package_list
        JSONArray idsArray = packageList.getJSONArray("result");
        for (int i = 0; i < idsArray.length(); i++) {
          String packageId = idsArray.getString(i);
          JSONObject pkgObject = readJsonData(new URL(baseUrl + "/package_show?id=" + packageId));
          if (pkgObject.has("result") && pkgObject.optBoolean("success", false) && pkgObject.get("result") instanceof JSONObject) {
            CkanPackage pkg = new CkanPackageAdaptor(pkgObject.getJSONObject("result"));
            onPackage(pkg);
            if (max>0 && (++counter)>=max) {
              return;
            }
          }
        }
      } else if (packageList.get("result") instanceof JSONObject) {
        // result of /package_search
        JSONObject result = packageList.getJSONObject("result");
        if (result.has("results") && result.get("results") instanceof JSONArray) {
          long count = result.getLong("count");
          long start = 0;
          do {
            JSONArray pkgs = result.getJSONArray("results");
            for (int i = 0; i < pkgs.length(); i++) {
              JSONObject pkgObject = pkgs.getJSONObject(i);
              CkanPackage pkg = new CkanPackageAdaptor(pkgObject);
              onPackage(pkg);
              if (max>0 && (++counter)>=max) {
                return;
              }
            }
            start = start + pkgs.length();
            if (start < count) {
              JSONObject searchData = readJsonData(new URL(baseUrl + "/package_search?start=" + start));
              if (!(searchData.has("result") && searchData.get("result") instanceof JSONObject)) {
                break;
              }
              result = searchData.getJSONObject("result");
              if (!(result.has("results") && result.get("results") instanceof JSONArray)) {
                break;
              }
            }
          } while (start < count);
        }
      }
    }
  }

  private JSONObject readJsonData(URL url) throws IOException, JSONException {
    Bots bots = null; //BotsUtils.readBots(BotsMode.always, packageListUrl);
    HttpCrawlRequest request = new HttpCrawlRequest(bots) {
      @Override
      protected HttpMethodBase createMethod() throws IOException {
        HttpMethodBase method = super.createMethod();
        method.addRequestHeader("User-Agent", "GeoportalServer");
        return method;
      }
    };
    request.setUrl(url.toExternalForm());
    StringHandler handler = new StringHandler();
    request.setContentHandler(handler);
    request.execute();
    return new JSONObject(handler.getContent());
  }
}
