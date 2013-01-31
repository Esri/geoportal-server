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
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * WAF file.
 */
class WafFile extends CommonPublishable {
private UrlUri uri;
private WafProxy proxy;
private WafProxy.Content content;
private IOException storedException;
private String encodedUrl;

public WafFile(WafProxy proxy, String url) {
  this.proxy = proxy;
  this.uri = new UrlUri(url);
  this.encodedUrl = encode(url);
}

@Override
public SourceUri getSourceUri() {
  return uri;
}

@Override
public String getContent() throws IOException {
  // rethrow stored exception which occured during getUpdateDate()
  if (storedException!=null) {
    throw storedException;
  }
  if (content==null) {
    content = proxy.readContent(encodedUrl);
  }
  String metadata = "";
  if (content!=null) {
    metadata = Val.chkStr(content.getText());
    content = null;
  }
  return metadata;
}

@Override
public Date getUpdateDate() {
  try {
    if (content==null) {
      content = proxy.readContent(encodedUrl);
    }
  } catch (IOException ex) {
    // store that exception here because getUpdateDate can not throw any exception by interface
    // instead getContent() would rethrow this exception
    storedException = ex;
  }
  return content!=null? content.getLastModifedDate(): null;
}

private String encode(String url) {
  url = Val.chkStr(url);
  try {
    return URIUtil.encodePathQuery(URIUtil.decode(url, "UTF-8"), "UTF-8");
  } catch (URIException ex) {
    return url;
  }
}
}
