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
package com.esri.gpt.control.webharvest.client.atom;

import java.util.logging.Logger;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAtom;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * Atom query builder.
 */
public class AtomQueryBuilder implements QueryBuilder {

/** logger */
private static final Logger LOGGER = Logger.getLogger(AtomQuery.class.getCanonicalName());
/** capabilities */
private static final Capabilities capabilities = new AtomCommonCapabilities();
/** iteration context */
private IterationContext context;
/** service info */
private BaseAtomInfo info;

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param url url
 * @throws Exception 
 */
public AtomQueryBuilder(IterationContext context, HarvestProtocolAtom protocol, String url) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
    this.context = context;
    try{
    	String atomInfoProcessorClassName = protocol.getAtomType();
    	if (atomInfoProcessorClassName.length() == 0) {
		    String[] parts = url.split("atomInfoProcessorClassName=");			
				if (parts != null && parts.length >= 2) {
					atomInfoProcessorClassName = Val.chkStr(parts[1]);
					int idx = atomInfoProcessorClassName.indexOf("&");
					if (idx == -1) {
						atomInfoProcessorClassName = atomInfoProcessorClassName.substring(0);
					} else {
						atomInfoProcessorClassName = atomInfoProcessorClassName.substring(0, idx);
					}
				}
    	}
			if (atomInfoProcessorClassName.length() == 0) {
				atomInfoProcessorClassName = "com.esri.gpt.control.webharvest.client.atom.OpenSearchAtomInfoProcessor";
			}
			Class<?> clsAdapter;
			clsAdapter = Class.forName(atomInfoProcessorClassName);
			Object atomInfoProcessorObj = clsAdapter.newInstance();
			if (atomInfoProcessorObj instanceof IAtomInfoProcessor) {
				IAtomInfoProcessor atomInfoProcessor = ((IAtomInfoProcessor) atomInfoProcessorObj);
				atomInfoProcessor.preInitialize();
				this.info = atomInfoProcessor.initializeAtomInfo(protocol,url);				
				atomInfoProcessor.postCreate(this.info);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

public Capabilities getCapabilities() {
  return capabilities;
}

public Query newQuery(Criteria crt) {
  AtomProxy proxy = new AtomProxy(info);
  Query q = new AtomQuery(context, info, proxy, crt);
  LOGGER.finer("Query created: " + q);
  return q;
}

public Native getNativeResource() {
   AtomProxy proxy = new AtomProxy(info);
   return proxy.getNativeResource();
}

/**
 * Atom capabilities.
 */
private static class AtomCommonCapabilities extends CommonCapabilities {

@Override
public boolean canQueryFromDate() {
  return true;
}

@Override
public boolean canQueryToDate() {
  return true;
}

@Override
public boolean canQueryMaxRecords() {
  return true;
}
}
}
