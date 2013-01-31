/*
 * Copyright 2011 Esri.
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
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import java.util.NoSuchElementException;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Ftp file iterator
 */
class FtpFileIterator extends ReadOnlyIterator<Resource> {
  private IterationContext iterationContext;
  private FtpClientRequest ftpClient;
  private Criteria criteria;
  private FTPFile[] files;
  private String folder;
  private int index = -1;
  private Resource nextResource;

  /**
   * Creates instance of the iterator.
   * @param iterationContext iteration context
   * @param url server url
   * @param folder folder
   */
  public FtpFileIterator(IterationContext iterationContext, FtpClientRequest ftpClient, Criteria criteria, FTPFile[] files, String folder) {
    this.iterationContext = iterationContext;
    this.ftpClient = ftpClient;
    this.criteria = criteria;
    this.files = files!=null? files: new FTPFile[]{};
    this.folder = Val.chkStr(folder);
  }

  @Override
  public boolean hasNext() {
    if (nextResource!=null) {
      return true;
    }
    if (index+1>=getFiles().length) {
      return false;
    }
    nextResource = newResource(getFiles()[++index]);
    if (nextResource!=null) {
      return true;
    }
    return hasNext();
  }

  @Override
  public Resource next() {
    if (nextResource==null) {
      throw new NoSuchElementException("No more elements.");
    }
    Resource r = nextResource;
    nextResource = null;
    if (r instanceof FtpFile) {
      try {
        FtpFile f = (FtpFile)r;
        f.getContent();
      } catch (Exception ex) {
        iterationContext.onIterationException(ex);
      }
    }
    return r;
  }

  /**
   * Gets files.
   * @return files
   */
  public final FTPFile[] getFiles() {
    return files;
  }
  
  /**
   * Gets request.
   * @return request
   */
  public final FtpClientRequest getFtpClient() {
    return ftpClient;
  }

  /**
   * Creates new resource.
   * @param file file
   * @return resource
   */
  protected Resource newResource(FTPFile file) {
    if (file.isDirectory()) {
      return new FtpFolder(iterationContext, getFtpClient(), criteria, folder + "/" + file.getName());
    } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
      if (criteria==null || criteria.getFromDate()==null || file.getTimestamp()==null
          || (criteria.getFromDate()!=null && file.getTimestamp()!=null && file.getTimestamp().after(criteria.getFromDate()))) {
        return new FtpFile(iterationContext, getFtpClient(), folder + "/" + file.getName());
      }
    }
    return null;
  }
  
}
