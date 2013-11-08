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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpCredentials;
import com.esri.gpt.agp.client.AgpTokenCriteria;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISInfo;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.control.webharvest.engine.Ags2AgpExecutor;
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Executor;
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
 * Ags2Agp protocol.
 */
public class HarvestProtocolAgs2Agp extends AbstractHTTPHarvestProtocol {
  
  private StringAttributeMap attrMap = new StringAttributeMap();

  /**
   * name of the protocol
   */
  public static final String NAME = "AGS2AGP";
  /**
   * flags to carry over
   */
  private long flags;

  public ArcGISInfo getSource() {
    StringAttributeMap attrs = getAttributeMap();
    return new ArcGISInfo(
            attrs.getValue("ags-src-restUrl"),
            attrs.getValue("ags-src-soapUrl"),
            attrs.getValue("ags-src-userName"),
            attrs.getValue("ags-src-userPassword"));
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
            attrs.getValue("ags-dest-u"), attrs.getValue("ags-dest-p")));
    con2.getTokenCriteria().setReferer(getReferrer());
    destination.setConnection(con2);
    destination.setDestinationOwner(attrs.getValue("ags-dest-o"));
    destination.setDestinationFolderID(attrs.getValue("ags-dest-f"));

    return destination;
  }
  
  /**
   * Gets host URL.
   * @return host URL
   */
  public String getHostUrl() {
    StringAttributeMap attrs = getAttributeMap();
    return getSourceHost() + "?user=" +attrs.getValue("ags-src-userName") +
            "&dest=http://" + getDestinationHost() + "/" + attrs.getValue("agp-dest-o")  + "/" + attrs.getValue("agp-dest-f") + "&destuser=" + attrs.getValue("agp-dest-u");
  }
  
  /**
   * Gets source host.
   * @return host name
   */
  public String getSourceHost() {
    return getAttributeMap().getValue("ags-src-restUrl");
  }

  /**
   * Gets destination host.
   * @return host name
   */
  public String getDestinationHost() {
    return getAttributeMap().getValue("ags-dest-h");
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

  @Override
  public void ping(String url) throws Exception {
    super.ping(getSourceHost()); //To change body of generated methods, choose Tools | Templates.
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
    return new Ags2AgpExecutorImpl(dataProcessor, unit, worker);
  }


  /**
   * Agp2Agp executor implementation.
   */
  private static class Ags2AgpExecutorImpl extends Ags2AgpExecutor {
    private IWorker worker;

    public Ags2AgpExecutorImpl(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker) {
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
