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

import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.DataProcessorFactory;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.control.webharvest.engine.Suspender;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 * Local folder data processor factory.
 * <p>
 * It is used to collect harvested metadata into the local folder.
 * <p>
 * Usage in gpt.xml:
 * <code><pre>
    &lt;webharvester&gt;
		&lt;dataProcessorFactory enabled="true" 
                 className="com.esri.gpt.control.webharvest.extensions.localfolder.LocalFolderDataProcessorFactory" 
                 name="localfolder" 
                 rootFolder="c:\data"/&gt;
    &lt;/webharvester&gt;
 * </pre></code>
 */
public class LocalFolderDataProcessorFactory implements DataProcessorFactory {

  private static final Logger LOG = Logger.getLogger(LocalFolderDataProcessorFactory.class.getName());
  private String name = "localfolder";
  private File rootFolder;

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void init(Node ndConfig) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      String sRootFolder = Val.chkStr((String) xpath.evaluate("@rootFolder", ndConfig, XPathConstants.STRING));
      rootFolder = new File(sRootFolder);
    } catch (XPathExpressionException ex) {
      LOG.log(Level.SEVERE, "Missing root folder", ex);
    }
  }

  @Override
  public DataProcessor newProcessor(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener) {
    return null;
  }

  @Override
  public DataProcessor newProcessor(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener, Suspender suspender) {
    if (rootFolder!=null) {
      return new LocalFolderDataProcessor(name, rootFolder, messageBroker, baseContextPath, listener, suspender);
    } else {
      return null;
    }
  }
  
}
