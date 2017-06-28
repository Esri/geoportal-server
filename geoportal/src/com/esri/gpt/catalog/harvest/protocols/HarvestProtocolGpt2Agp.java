/*
 * Copyright 2015 pete5162.
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
import com.esri.gpt.agp.client.AgpTokenCriteria;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.agp.sync.GptSource;
import static com.esri.gpt.catalog.harvest.protocols.HarvestProtocolAgs2Agp.NAME;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Executor;
import com.esri.gpt.control.webharvest.engine.Gpt2AgpExecutor;
import com.esri.gpt.control.webharvest.engine.IWorker;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author pete5162
 */
public class HarvestProtocolGpt2Agp extends AbstractHTTPHarvestProtocol {
  
  private StringAttributeMap attrMap = new StringAttributeMap();

  /**
   * name of the protocol
   */
  public static final String NAME = "GPT2AGP";
  /**
   * flags to carry over
   */
  private long flags;

  public GptSource getSource() {
    return new GptSource();
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
            attrs.getValue("gpt-dest-u"), attrs.getValue("gpt-dest-p")));
    con2.getTokenCriteria().setReferer(getReferrer());
    destination.setConnection(con2);
    destination.setDestinationOwner(attrs.getValue("gpt-dest-o"));
    destination.setDestinationFolderID(attrs.getValue("gpt-dest-f"));

    return destination;
  }

  /**
   * Gets destination host.
   * @return host name
   */
  public String getDestinationHost() {
    return getAttributeMap().getValue("gpt-dest-h");
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
    // In case of Gpt2Agp protocol, query builder will not be used; however
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

  @Override
  public Executor newExecutor(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
    return new HarvestProtocolGpt2Agp.Gpt2AgpExecutorImpl(dataProcessor, unit, worker);
  }

  /**
   * Agp2Agp executor implementation.
   */
  private static class Gpt2AgpExecutorImpl extends Gpt2AgpExecutor {
    private IWorker worker;

    public Gpt2AgpExecutorImpl(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
      super(dataProcessor, unit);
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
