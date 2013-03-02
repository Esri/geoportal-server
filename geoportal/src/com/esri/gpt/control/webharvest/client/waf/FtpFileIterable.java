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
import com.esri.gpt.framework.util.Val;
import java.util.Iterator;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP file iterable.
 */
class FtpFileIterable implements Iterable<Resource> {

  protected IterationContext iterationContext;
  protected FtpClientRequest ftpClient;
  protected Criteria criteria; 
  private FTPFile[] files;
  protected String folder;

  private Iterator<Resource> iterator;
  
  /**
   * Creates instance of the iterable.
   * @param iterationContext iteration context
   * @param url server url
   * @param folder folder
   */
  public FtpFileIterable(IterationContext iterationContext, FtpClientRequest ftpClient, Criteria criteria, FTPFile[] files, String folder) {
    this.iterationContext = iterationContext;
    this.ftpClient = ftpClient;
    this.criteria = criteria;
    this.files = files!=null? files: new FTPFile[]{};
    this.folder = Val.chkStr(folder);
  }

  @Override
  public Iterator<Resource> iterator() {
    if (iterator==null) {
      iterator = new FtpFileIterator(iterationContext, getFtpClient(), criteria, files, folder);
    }
    return iterator;
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
}
