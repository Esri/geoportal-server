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
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Ftp file.
 */
class FtpFile extends CommonPublishable {

  private IterationContext iterationContext;
  private FtpClientRequest ftpClient;
  private String path;
  private String content;

  /**
   * Creates instance of the file.
   * @param iterationContext iteration context
   * @param request request
   * @param url server url
   * @param path path
   */
  public FtpFile(IterationContext iterationContext, FtpClientRequest ftpClient, String path) {
    this.iterationContext = iterationContext;
    this.ftpClient = ftpClient;
    this.path = Val.chkStr(path);
  }

  @Override
  public SourceUri getSourceUri() {
    return new StringUri(getFtpClient().getServer() + path);
  }

  @Override
  public String getContent() throws IOException, TransformerException, SAXException {
    if (content==null) {
      try {
        content = getFtpClient().readTextFile(path);
      } catch (IOException ex) {
        content = "";
        throw ex;
      }
    }
    return content;
  }
  
  /**
   * Gets request.
   * @return request
   */
  public final FtpClientRequest getFtpClient() {
    return ftpClient;
  }
}
