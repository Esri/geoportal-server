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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolCsw;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.server.csw.client.CswCatalog;
import com.esri.gpt.server.csw.client.CswProfiles;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * CSW query builder.
 */
public class CswQueryBuilder implements QueryBuilder {

/** logger */
private static final Logger LOGGER = Logger.getLogger(CswQueryBuilder.class.getCanonicalName());
/** service capabilities */
private static final Capabilities capabilities = new CswCommonCapabilities();
/** CSW profiles */
private static CswProfiles cswProfiles;
/** iteration context */
private IterationContext context;
/** service info */
private CswInfo info;
/** service proxy */
private CswCatalog catalog;

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param url url
 */
public CswQueryBuilder(IterationContext context, HarvestProtocolCsw protocol, String url) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  if (cswProfiles==null) {
    try {
      cswProfiles = new CswProfiles();
      cswProfiles.loadProfilefromConfig();
    } catch (Exception ex) {
      throw new IllegalArgumentException("Unable to obtain profiles.", ex);
    }
  }
  this.context = context;
  if (cswProfiles!=null) {
    this.info = new CswInfo(cswProfiles, url, protocol.getProfile(), protocol.getUserName(), protocol.getUserPassword());
    try {
      this.catalog = this.info.newCatalog();
    } catch (IOException ex) {
      throw new IllegalArgumentException("Unable to create catalog.", ex);
    }
  }
}

public Capabilities getCapabilities() {
  return capabilities;
}

public Query newQuery(Criteria crt) {
  CswProxy proxy = new CswProxy(info, catalog);
  Query q = new CswQuery(context, proxy, crt);
  LOGGER.finer("Query created: " + q);
  return q;
}

public Native getNativeResource() {
  CswProxy proxy = new CswProxy(info, catalog);
  return proxy.getNativeResource();
}

/**
 * CSW capabilities.
 */
private static class CswCommonCapabilities extends CommonCapabilities {

@Override
public boolean canQueryBBox() {
  return true;
}

@Override
public boolean canQueryBBoxOption() {
  return true;
}

@Override
public boolean canQueryContentType() {
  return true;
}

@Override
public boolean canQueryDataCategory() {
  return true;
}

@Override
public boolean canQueryFromDate() {
  return true;
}

@Override
public boolean canQueryMaxRecords() {
  return true;
}

@Override
public boolean canQuerySearchText() {
  return true;
}

@Override
public boolean canQuerySortOption() {
  return true;
}

@Override
public boolean canQueryToDate() {
  return true;
}
}
}
