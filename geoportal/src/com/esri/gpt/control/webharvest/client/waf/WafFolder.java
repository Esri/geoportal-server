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

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single WAF folder.
 */
abstract class WafFolder implements DestroyableResource {

/** logger */
protected static final Logger LOGGER = Logger.getLogger(WafFolder.class.getCanonicalName());
protected IterationContext context;
protected WafInfo info;
protected WafProxy proxy;
protected String url;
protected Criteria criteria;

/**
 * Creates instance of the WAF folder.
 * @param context iteration context
 * @param info WAF info
 * @param proxy WAF proxy
 * @param url folder URL
 * @param criteria search criteria
 */
public WafFolder(IterationContext context, WafInfo info, WafProxy proxy, String url, Criteria criteria) {
  this.context = context;
  this.info = info;
  this.proxy = proxy;
  this.url = Val.chkStr(url);
  this.criteria = criteria;
}

@Override
public void destroy() {
}

@Override
public Iterable<Resource> getNodes() {
  return new Iterable<Resource>() {

  @Override
  public Iterator<Resource> iterator() {
    return new WafFolderIterator();
  }
  };
}

/**
 * Parses WAF response.
 * @param response response
 * @return collection of resources found in the response
 * @throws IOException if unable to parse response
 */
protected abstract Collection<Resource> parseResonse(String response) throws IOException;

/**
 * Folder iterator.
 */
private class WafFolderIterator extends ReadOnlyIterator<Resource> {

private Iterator<Resource> iterator;
private boolean noMore;

@Override
public boolean hasNext() {
  if (!noMore && iterator == null) {
    loadFolderContent();
  }
  boolean hasMore =  !noMore && iterator != null ? iterator.hasNext() : false;
  if (!hasMore) {
    noMore = true;
    iterator = null;
  }
  return hasMore;
}

@Override
public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException("No more resources.");
  }
  return iterator.next();
}

/**
 * Loads folder content.
 */
private void loadFolderContent() {
  try {
    LOGGER.log(Level.FINER, "Loading folder content of {0}", url);
    
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(url);
    StringHandler sh = new StringHandler();
    cr.setContentHandler(sh);
    cr.setCredentialProvider(info.newCredentialProvider());
    cr.setBatchHttpClient(info.getBatchHttpClient());
    cr.execute();
    
    iterator = parseResonse(sh.getContent()).iterator();

    LOGGER.log(Level.FINER, "Loading folder content of {0} completed.", url);
  } catch (Exception ex) {
    noMore = true;
    iterator = null;
    context.onIterationException(ex);
  }
}
}
}
