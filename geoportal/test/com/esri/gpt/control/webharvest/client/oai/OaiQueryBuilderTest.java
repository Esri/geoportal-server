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
package com.esri.gpt.control.webharvest.client.oai;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolOai;
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
public class OaiQueryBuilderTest {
private static OaiQueryBuilder instance = null;

public OaiQueryBuilderTest() {
}

@BeforeClass
public static void setUpClass() throws Exception {
  HarvestProtocolOai protocol = new HarvestProtocolOai();
  protocol.setPrefix("oai_dc");
  IterationContext context = new IterationContext() {
    public void onIterationException(Exception ex) {
    }
  };
  instance  = new OaiQueryBuilder(context, protocol, "http://alcme.oclc.org/oaicat/OAIHandler");
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
