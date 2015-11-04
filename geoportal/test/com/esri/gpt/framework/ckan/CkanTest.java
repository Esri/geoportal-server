/*
 * Copyright 2015 pete5162.
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
import com.esri.gpt.framework.robots.BotsMode;
import com.esri.gpt.framework.robots.BotsUtils;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.httpclient.HttpMethodBase;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pete5162
 */
public class CkanTest {

  @Test
  public void ckanTest() throws Exception {
    String baseUrl = "http://demo.ckan.org/api/3/action";
    //String baseUrl = "http://catalog.data.gov/api/3/action";
    
    CkanPackageList packageList = readPackageList(new URL(baseUrl+"/package_list"));
    assertNotNull(packageList);
    
    for (String packageId: packageList.getPackagesIds()) {
      CkanPackage pkg = readPackage(new URL(baseUrl+"/package_show?id="+packageId));
      assertNotNull(pkg);
      System.out.println(pkg);
      for (CkanResource rsc : pkg.getResources()) {
        System.out.println("   "+rsc);
      }
    }
 }
  
  private CkanPackageList readPackageList(URL packageListUrl) throws IOException, JSONException {
    Bots bots = null; //BotsUtils.readBots(BotsMode.always, packageListUrl);
    HttpCrawlRequest request = new HttpCrawlRequest(bots) {
      @Override
      protected HttpMethodBase createMethod() throws IOException {
        HttpMethodBase method = super.createMethod();
        method.addRequestHeader("User-Agent", "GeoportalServer");
        return method;
      }
    };
    request.setUrl(packageListUrl.toExternalForm());
    StringHandler handler = new StringHandler();
    request.setContentHandler(handler);
    request.execute();
    return CkanParser.parsePackageList(handler.getContent());
  }
  
  private CkanPackage readPackage(URL packageUrl)  throws IOException, JSONException {
    Bots bots = null; //BotsUtils.readBots(BotsMode.always, packageUrl);
    HttpCrawlRequest request = new HttpCrawlRequest(bots) {
      @Override
      protected HttpMethodBase createMethod() throws IOException {
        HttpMethodBase method = super.createMethod();
        method.addRequestHeader("User-Agent", "GeoportalServer");
        return method;
      }
    };
    request.setUrl(packageUrl.toExternalForm());
    StringHandler handler = new StringHandler();
    request.setContentHandler(handler);
    request.execute();
    return CkanParser.parsePackage(handler.getContent());
  }
}
