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

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.control.webharvest.engine.Suspender;
import static com.esri.gpt.control.webharvest.extensions.localfolder.PathUtil.sanitizeFileName;
import static com.esri.gpt.control.webharvest.extensions.localfolder.StringListUtil.head;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.util.UuidUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.commons.io.IOUtils;
import static com.esri.gpt.control.webharvest.extensions.localfolder.PathUtil.splitPath;
import java.util.ArrayList;

/**
 * Local folder data processor.
 */
public class LocalFolderDataProcessor implements DataProcessor {

  private static final Logger LOG = Logger.getLogger(LocalFolderDataProcessor.class.getName());
  private final String name;
  private final File rootFolder;
  private final MessageBroker messageBroker;
  private final String baseContextPath;
  private final Harvester.Listener listener;
  private final Suspender suspender;
  
  private File destinationFolder;
  private List<String> subFolder;

  /**
   * Creates instance of the processor.
   * @param name name of the processor
   * @param rootFolder root folder
   * @param messageBroker message broker
   * @param baseContextPath base context path
   * @param listener listener (optional)
   * @param suspender suspender (optional)
   */
  public LocalFolderDataProcessor(String name, File rootFolder, MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener, Suspender suspender) {
    this.name = name;
    this.rootFolder = rootFolder;
    this.messageBroker = messageBroker;
    this.baseContextPath = baseContextPath;
    this.listener = listener;
    this.suspender = suspender;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void onEnd(ExecutionUnit unit, boolean success) {
  }

  @Override
  public void onIterationException(ExecutionUnit unit, Exception ex) {
  }

  @Override
  public void onMetadata(ExecutionUnit unit, Publishable record) throws IOException, SQLException, CatalogIndexException, TransformerConfigurationException {
    if (destinationFolder!=null) {
      File f = generateFileName(record.getSourceUri());
      f.getParentFile().mkdirs();
      if (!f.getName().contains(".")) {
        f = new File(f.getParentFile(),f.getName()+".xml");
      }
      FileOutputStream output = null;
      ByteArrayInputStream input = null;
      try {
        output = new FileOutputStream(f);
        String content = record.getContent();
        input = new ByteArrayInputStream(content.getBytes("UTF-8"));
        IOUtils.copy(input, output);
      } catch (Exception ex) {
        throw new IOException("Error reading content.", ex);
      } finally {
        if (input!=null) {
          IOUtils.closeQuietly(input);
        }
        if (output!=null) {
          IOUtils.closeQuietly(output);
        }
      }
    }
  }

  @Override
  public void onStart(ExecutionUnit unit) {
    try {
      URL hostUrl = new URL(unit.getRepository().getHostUrl());
      destinationFolder = rootFolder!=null? new File(rootFolder,hostUrl.getHost()): null;
      subFolder = splitPath(hostUrl.getPath());
    } catch (MalformedURLException ex) {
      LOG.log(Level.SEVERE, "Error starting harvesting", ex);
    }
  }
  
  private File generateFileName(SourceUri uri) {
    String sUri = uri.asString();
    
    File fileName = destinationFolder;
    try {
      List<String> stock;
      
      URL u = new URL(sUri);
      List<String> path = splitPath(u);
      if (path.size()>0) {
        if (path.size()>1) {
          stock = StringListUtil.merge(subFolder,head(path, path.size()-1));
          stock.add(path.get(path.size()-1));
        } else {
          stock = new ArrayList<String>();
          stock.addAll(subFolder);
          stock.addAll(path);
        }
      } else {
        stock = subFolder;
      }
      
      for (String t: stock) {
        fileName = new File(fileName,t);
      }
      return fileName;
    } catch (MalformedURLException ex) {
      if (UuidUtil.isUuid(sUri)) {
        fileName = new File(fileName,sanitizeFileName(sUri)+".xml");
        return fileName;
      } else {
        File f = new File(sanitizeFileName(sUri)+".xml");
        for (String t: StringListUtil.merge(subFolder,splitPath(f))) {
          fileName = new File(fileName,t);
        }
        return fileName;
      }
    }
  }
}
