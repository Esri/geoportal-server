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
package com.esri.gpt.catalog.gxe;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * Simple test module.
 */
public class GxeTest {
  
  /**
   * Main unit test method.
   * @param args startup arguments
   */
  public static void main(String[] args) {
    RequestContext rc = null;
    try {
      //rc = RequestContext.extract(null);
      
      System.err.println(".................");
           
      GxeDefinition definition = new GxeDefinition();
      definition.setFileLocation("gpt/gxe/iso/iso19139/iso19139-dataset-editor.xml");
      
      GxeContext context = new GxeContext();
      context.setMessageBroker(new MessageBroker());
      context.getMessageBroker().setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
      
      GxeLoader loader = new GxeLoader();
      loader.loadDefinition(context,definition);
      
      GxeJsonSerializer serializer = new GxeJsonSerializer();
      String json = serializer.asJson(context,definition);
      System.err.println(json);
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    } finally {
      if (rc != null) rc.onExecutionPhaseCompleted();
    }
  }
}
