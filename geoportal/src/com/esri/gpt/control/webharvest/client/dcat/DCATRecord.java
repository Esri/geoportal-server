/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.control.webharvest.client.dcat;

import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.resource.common.UuidUri;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * DCAT record.
 */
class DCATRecord extends CommonPublishable {
  private static final Pattern pattern = Pattern.compile("/rest/document\\?id=\\{[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}\\}$", Pattern.CASE_INSENSITIVE);

  private SourceUri uri;
  private DCATProxy proxy;
  private DCATProxy.Content content;
  private IOException storedException;
  private String encodedUrl;

  /**
   * Creates instance of the record.
   * @param proxy proxy
   * @param url record URL
   */
  public DCATRecord(DCATProxy proxy, String url) {
    this.proxy = proxy;
    this.uri = createUri(url);
    this.encodedUrl = encode(url);
  }

  @Override
  public SourceUri getSourceUri() {
    return uri;
  }
  
  /**
   * Creates URI.
   * @param url URL of the record
   * @return source URI
   */
  private SourceUri createUri(String url) {
    try {
      String decoded = URIUtil.decode(url, "UTF-8");
      // consider Geoportal Server URI; if URL matches pattern, create UUID-based
      // URI.
      if (decoded.length()>=38 && pattern.matcher(decoded).find()) {
        decoded = decoded.substring(decoded.length()-38);
        return new UuidUri(decoded);
      } else {
        return new UrlUri(url);
      }
    } catch (IOException ex) {
      return new UrlUri(url);
    }
  }

  @Override
  public String getContent() throws IOException {
    // rethrow stored exception which occured during getUpdateDate()
    if (storedException != null) {
      throw storedException;
    }
    if (content == null) {
      content = proxy.readContent(encodedUrl);
    }
    String metadata = "";
    if (content != null) {
      metadata = Val.chkStr(content.getText());
      content = null;
    }
    return metadata;
  }

  @Override
  public Date getUpdateDate() {
    try {
      if (content == null) {
        content = proxy.readContent(encodedUrl);
      }
    } catch (IOException ex) {
      // store that exception here because getUpdateDate can not throw any exception by interface
      // instead getContent() would rethrow this exception
      storedException = ex;
    }
    return content != null ? content.getLastModifedDate() : null;
  }

  /**
   * Encodes URL.
   * @param url URL to encode
   * @return encoded URL
   */
  private String encode(String url) {
    url = Val.chkStr(url);
    try {
      return URIUtil.encodePathQuery(URIUtil.decode(url, "UTF-8"), "UTF-8");
    } catch (URIException ex) {
      return url;
    }
  }
}
