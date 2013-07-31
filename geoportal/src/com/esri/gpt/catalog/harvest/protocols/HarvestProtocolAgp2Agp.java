/*
 * Copyright 2012 Esri.
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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpCredentials;
import com.esri.gpt.agp.client.AgpSearchCriteria;
import com.esri.gpt.agp.client.AgpTokenCriteria;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.agp.sync.AgpSource;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.control.webharvest.engine.Agp2AgpExecutor;
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Executor;
import com.esri.gpt.control.webharvest.engine.IWorker;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Agp2Agp protocol
 */
public class HarvestProtocolAgp2Agp extends AbstractHTTPHarvestProtocol {
  public static final long DEFAULT_MAX_ITEMS_AGP2AGP = 2000;
  public static final String DEFAULT_MAX_ITEMS_AGP2AGP_KEY = "webharvester.agp2agp.maxItems";
  
  private StringAttributeMap attrMap = new StringAttributeMap();

  /**
   * name of the protocol
   */
  public static final String NAME = "AGP2AGP";
  /**
   * flags to carry over
   */
  private long flags;
  /**
   * flag indicating to stop on error.
   */
  private boolean stopOnError = true;
  
  /**
   * Creates instance of the protocol
   * @param stopOnError <code>true</code> to stop harvesting on error
   */
  public HarvestProtocolAgp2Agp(boolean stopOnError) {
    this.stopOnError = stopOnError;
  }
  
  /**
   * Checks if stop harvesting on error.
   * @return <code>true</code> to stop harvesting on error
   */
  public boolean getStopOnError() {
    return stopOnError;
  }
  
  /**
   * Gets source.
   * @return source
   */
  public AgpSource getSource() {
    StringAttributeMap attrs = getAttributeMap();

    AgpSource source = new AgpSource();
    AgpConnection con1 = new AgpConnection();
    HostContextPair pair = HostContextPair.makeHostContextPair(getSourceHost());
    con1.setHost(pair.getHost());
    con1.setWebContext(pair.getContext());
    con1.setTokenCriteria(new AgpTokenCriteria());
    con1.getTokenCriteria().setCredentials(new AgpCredentials(
            attrs.getValue("src-u"), attrs.getValue("src-p")));
    con1.getTokenCriteria().setReferer(getReferrer());
    source.setConnection(con1);
    AgpSearchCriteria searchCriteria = new AgpSearchCriteria();
    searchCriteria.setSortField("title");
    searchCriteria.setQ(attrs.getValue("src-q"));
    searchCriteria.setDeepTotal(Long.valueOf(attrs.getValue("src-m")));
    source.setSearchCriteria(searchCriteria);

    return source;
  }

  /**
   * Gets destination.
   * @return destination
   */
  public AgpDestination getDestination() {
    StringAttributeMap attrs = getAttributeMap();
    
    AgpDestination destination = new AgpDestination();
    AgpConnection con2 = new AgpConnection();
    HostContextPair pair = HostContextPair.makeHostContextPair(getDestinationHost());
    con2.setHost(pair.getHost());
    con2.setWebContext(pair.getContext());
    con2.setTokenCriteria(new AgpTokenCriteria());
    con2.getTokenCriteria().setCredentials(new AgpCredentials(
            attrs.getValue("dest-u"), attrs.getValue("dest-p")));
    con2.getTokenCriteria().setReferer(getReferrer());
    destination.setConnection(con2);
    destination.setDestinationOwner(attrs.getValue("dest-o"));
    destination.setDestinationFolderID(attrs.getValue("dest-f"));

    return destination;
  }
  
  /**
   * Gets host URL.
   * @return host URL
   */
  public String getHostUrl() {
    StringAttributeMap attrs = getAttributeMap();
    return "http://" + getSourceHost() + "?q=" + attrs.getValue("src-q") + "&user=" +attrs.getValue("src-u")+ "&max=" + attrs.getValue("src-m") +
            "&dest=http://" + getDestinationHost() + "/" + attrs.getValue("dest-o")  + "/" + attrs.getValue("dest-f") + "&destuser=" + attrs.getValue("dest-u");
  }
  
  /**
   * Gets source host.
   * @return host name
   */
  public String getSourceHost() {
    return getAttributeMap().getValue("src-h");
  }

  /**
   * Gets destination host.
   * @return host name
   */
  public String getDestinationHost() {
    return getAttributeMap().getValue("dest-h");
  }
  
  @Override
  public StringAttributeMap getAttributeMap() {
    return attrMap;
  }

  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    attrMap = attributeMap;
  }

  @Override
  public long getFlags() {
    return flags;
  }

  @Override
  public void setFlags(long flags) {
    this.flags = flags;
  }

  @Override
  public String getKind() {
    return NAME;
  }

  @Override
  public ProtocolType getType() {
    return null;
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    // In case of Agp2Agp protocol, query builder will not be used; however
    // for the flow requirements, some query builder has to be created.
    return new QueryBuilder() {

      @Override
      public Capabilities getCapabilities() {
        return new CommonCapabilities();
      }

      @Override
      public Query newQuery(Criteria crt) {
        return null;
      }

      @Override
      public Native getNativeResource() {
        return null;
      }
    };
  }

  /**
   * Gets referrer.
   * @return referrer
   */
  protected String getReferrer() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException ex) {
      return "";
    }
  }

/**
 * Gets AGP2AGP max items.
 * @return return AGP2AGP max items
 */
public static Long getAgp2AgpMaxItems() {
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  
  String sMaxItems = appCfg.getCatalogConfiguration().getParameters().getValue(DEFAULT_MAX_ITEMS_AGP2AGP_KEY);
  
  return Val.chkLong(sMaxItems, DEFAULT_MAX_ITEMS_AGP2AGP);
}

  @Override
  public Executor newExecutor(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
    return new Agp2AgpExecutorImpl(dataProcessor, unit, worker, getStopOnError());
  }


  /**
   * Agp2Agp executor implementation.
   */
  private static class Agp2AgpExecutorImpl extends Agp2AgpExecutor {
    private IWorker worker;

    public Agp2AgpExecutorImpl(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker, boolean stopOnError) {
      super(dataProcessor, unit, stopOnError);
      this.worker = worker;
    }

    @Override
    protected boolean isActive() {
      return worker.isActive();
    }

    @Override
    protected boolean isShutdown() {
      return worker.isShutdown();
    }

    @Override
    protected boolean isSuspended() {
      return worker.isSuspended();
    }
  }

}
