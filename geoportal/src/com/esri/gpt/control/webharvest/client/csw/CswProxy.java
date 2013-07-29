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
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.util.ResourceXml;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;

/**
 * CSW service proxy.
 */
class CswProxy {
/** logger */
private static final Logger LOGGER = Logger.getLogger(CswProxy.class.getCanonicalName());
/** service info */
private CswInfo info;
/** CSW catalog */
private CswCatalog catalog;
/** CSW search request */
private CswSearchRequest request;

/**
 * Creates instance of the proxy.
 * @param info service info
 * @param catalog CSW catalog
 */
public CswProxy(CswInfo info, CswCatalog catalog) {
  if (info==null) throw new IllegalArgumentException("No info provided.");
  if (catalog==null) throw new IllegalArgumentException("No catalog provided.");
  this.info = info;
  this.catalog = catalog;
  this.request = new CswSearchRequest();
  
  CswSearchCriteria cswCriteria = new CswSearchCriteria();
  cswCriteria.setSearchText("");
  CswSearchResponse cswResponse = new CswSearchResponse();

  this.request.setCatalog(catalog);
  this.request.setCriteria(cswCriteria);
  this.request.setCswSearchResponse(cswResponse);
}

/**
 * Gets CSW catalog.
 * @return CSW catalog
 */
public CswCatalog getCatalog() {
  return catalog;
}

public String read(String sourceUri) throws IOException, NullReferenceException {
  LOGGER.finer("Reading metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
  sourceUri = Val.chkStr(sourceUri);
  String fullMetadata = "";
  try {
    request.getMetadataByID(sourceUri);

    Iterator itr = request.getResponse().getRecords().iterator();
    if (itr.hasNext()) {
      com.esri.gpt.server.csw.client.CswRecord rec = (com.esri.gpt.server.csw.client.CswRecord) itr.next();
      fullMetadata = rec.getFullMetadata();
    }
    String mdText = Val.chkStr(fullMetadata);
    LOGGER.finer("Received metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
    LOGGER.finest(mdText);
    return mdText;
//  } catch (NullReferenceException ex) {
//    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (InvalidOperationException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (TransformerException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  }
}

/**
 * Gets native resource.
 * @return native resource
 */
public Native getNativeResource() {
  return new NativeImpl();
}

@Override
public String toString() {
  return info.toString();
}

/**
 * Destroys proxy.
 */
public void destroy() {
  info.destroy();
}
/**
 * Native implementation.
 */
private class NativeImpl extends CommonPublishable implements Native {

    @Override
  public SourceUri getSourceUri() {
    return new UrlUri(info.getUrl());
  }

    @Override
  public String getContent() throws IOException {
    ResourceXml resourceXml = new ResourceXml();
    return resourceXml.makeResourceXmlFromResponse(info.getUrl());
  }
}
}
