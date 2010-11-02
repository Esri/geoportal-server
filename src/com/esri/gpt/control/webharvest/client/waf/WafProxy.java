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

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * WAF proxy.
 */
class WafProxy {
/** logger */
private static final Logger LOGGER = Logger.getLogger(WafProxy.class.getCanonicalName());
/** service info */
private WafInfo info;

/**
 * Creates instance of the proxy.
 * @param info service info
 */
public WafProxy(WafInfo info) {
  if (info==null) throw new IllegalArgumentException("No info provided.");
  this.info = info;
}

public String read(String sourceUri) throws IOException {
  LOGGER.finer("Reading metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
  sourceUri = Val.chkStr(sourceUri).replaceAll("\\{", "%7B").replaceAll("\\}", "%7D");
  HttpClientRequest cr = new HttpClientRequest();
  cr.setUrl(sourceUri);
  StringHandler sh = new StringHandler();
  cr.setContentHandler(sh);
  cr.setCredentialProvider(info.newCredentialProvider());
  cr.execute();
  String mdText = sh.getContent();
  LOGGER.finer("Received metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
  LOGGER.finest(mdText);
  return mdText;
}

@Override
public String toString() {
  return info.toString();
}
}
