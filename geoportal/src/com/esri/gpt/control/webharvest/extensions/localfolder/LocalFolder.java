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

import static com.esri.gpt.control.webharvest.extensions.localfolder.PathUtil.sanitizeFileName;
import static com.esri.gpt.control.webharvest.extensions.localfolder.PathUtil.splitPath;
import static com.esri.gpt.control.webharvest.extensions.localfolder.StringListUtil.head;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.util.UuidUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Esri, Inc.
 */
public class LocalFolder {

  private final File destinationFolder;
  private final List<String> subFolder;

  public LocalFolder(File rootFolder, URL hostUrl) {
    destinationFolder = new File(rootFolder,hostUrl.getHost());
    subFolder = splitPath(hostUrl.getPath());
  }
  
  public void storeData(SourceUri sourceUri, String content) throws IOException {
      File f = generateFileName(sourceUri);
      f.getParentFile().mkdirs();
      if (!f.getName().contains(".")) {
        f = new File(f.getParentFile(),f.getName()+".xml");
      }
      FileOutputStream output = null;
      ByteArrayInputStream input = null;
      try {
        output = new FileOutputStream(f);
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
          stock = Arrays.asList(new String[0]);
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
        File f = new File(sUri);
        for (String t: StringListUtil.merge(subFolder,splitPath(f))) {
          fileName = new File(fileName,t);
        }
        return fileName;
      }
    }
  }
  
}
