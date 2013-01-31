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
import java.util.Iterator;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP root file iterable.
 */
class FtpRootFileIterable extends FtpFileIterable {

  /**
   * Creates instance of the object.
   * @param iterationContext iteration context
   * @param ftpClient ftp client
   * @param criteria criteria
   * @param files files
   */
  public FtpRootFileIterable(IterationContext iterationContext, FtpClientRequest ftpClient, Criteria criteria, FTPFile[] files) {
    super(iterationContext, ftpClient, criteria, files, ftpClient.getRootFolder());
  }

  @Override
  public Iterator<Resource> iterator() {
    return new FtpFileIterator(iterationContext, getFtpClient(), criteria, getFiles(), folder);
  }
}
