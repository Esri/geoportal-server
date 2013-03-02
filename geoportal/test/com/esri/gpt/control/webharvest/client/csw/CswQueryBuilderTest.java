/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolCsw;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class CswQueryBuilderTest {
private static CswQueryBuilder instance = null;

public CswQueryBuilderTest() {
}

@BeforeClass
public static void setUpClass() throws Exception {
  HarvestProtocolCsw protocol = new HarvestProtocolCsw();
  protocol.setProfile("urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT");
  IterationContext context = new IterationContext() {
    public void onIterationException(Exception ex) {
    }
  };
  instance  = new CswQueryBuilder(context, protocol, "http://geoss.esri.com/geoportal/csw/discovery?service=CSW&request=getCapabilities");
}

@Test
public void testNewQuery() {
      System.out.println("newQuery");
      CommonCriteria crt = new CommonCriteria();
      crt.setMaxRecords(5);
      Query query = instance.newQuery(crt);
      assertNotNull(query);
      Result result = query.execute();
      assertNotNull(result);
      Iterable<Resource> resources = result.getResources();
      assertNotNull(resources);
      int count = 0;
      for (Resource resource : resources) {
        count++;
      }
      assertTrue(count<=crt.getMaxRecords());
}
}
