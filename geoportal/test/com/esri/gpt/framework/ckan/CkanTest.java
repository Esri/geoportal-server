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

import com.esri.gpt.control.webharvest.client.ckan.CkanConfig;
import com.esri.gpt.control.webharvest.client.ckan.CkanCrawler;
import com.esri.gpt.control.webharvest.client.ckan.CkanIterator;
import com.esri.gpt.framework.robots.BotsMode;
import java.net.URL;
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
  public void ckanIteratorTest() throws Exception {
    {
      int num = iterate("http://demo.ckan.org/api/3/action", 20);
      assertTrue(num >= 20);
    }
    {
      int num = iterate("http://catalog.data.gov/api/3/action", 20);
      assertTrue(num >= 20);
    }
  }
  
  @Test
  public void ckanCrawlerTest() throws Exception {
    {
      int num = crawl("http://demo.ckan.org/api/3/action", 20);
      assertTrue(num >= 20);
    }
    {
      int num = crawl("http://catalog.data.gov/api/3/action", 20);
      assertTrue(num >= 20);
    }
  }
  
  private class CallbackImpl implements CkanCrawler.Callback {
    private final int max;
    private int counter;

    public CallbackImpl(int max) {
      this.max = max;
    }

    public int getCounter() {
      return counter;
    }
    
    @Override
    public boolean onPackage(CkanPackage pkg) {
      try {
        CkanTest.this.onPackage(pkg);
        counter++;
        return (++counter) < max;
      } catch (Exception ex) {
        return false;
      }
    }
    
  }

  private int crawl(String baseUrl, int max) throws Exception {
    CallbackImpl callback = new CallbackImpl(max);
    CkanCrawler crawler = new CkanCrawler(new CkanConfig(BotsMode.never, "GeoportalServer", null, false));
    crawler.crawl(new URL(baseUrl), BotsMode.never, callback);
    return callback.getCounter();
  }
  
  private int iterate(String baseUrl, int max) throws Exception {
    int counter = 0;
    CkanIterator itr = new CkanIterator(new CkanConfig(BotsMode.never, "GeoportalServer", null, false), new URL(baseUrl));
    for (CkanPackage pkg : itr) {
      if ((++counter) >= max) {
        break;
      }
      onPackage(pkg);
    }
    return counter;
  }
}
