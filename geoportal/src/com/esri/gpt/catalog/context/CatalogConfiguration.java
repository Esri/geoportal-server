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
package com.esri.gpt.catalog.context;

import java.util.logging.Level;

import com.esri.gpt.catalog.arcims.ImsCatalog;
import com.esri.gpt.catalog.discovery.DiscoveryQueryAdapter;
import com.esri.gpt.catalog.lucene.LuceneConfig;
import com.esri.gpt.catalog.schema.SchemaFactory;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.search.browse.TocCollection;
import com.esri.gpt.control.search.browse.TocFactory;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Catalog configuration information.
 */
public class CatalogConfiguration extends Configuration {
  
// class variables =============================================================

// instance variables ==========================================================
private Schemas            _configuredSchemas = null;
private ImsCatalog         _imsCatalog;
private LuceneConfig       _luceneConfig = new LuceneConfig();
private String             _mvsTablePrefix = "MVS_";
private StringAttributeMap _parameters = new StringAttributeMap();
private SearchConfig       _searchConfig;
private String             _tablePrefix = "GPT_";
private TocCollection      _tocCollection = null;
private DcatSchemas 			 _dcatSchemas = new DcatSchemas();

private int                connectionTimeOut = HttpClientRequest.DEFAULT_CONNECTION_TIMEOUT;
private int                responseTimeOut   = HttpClientRequest.DEFAULT_RESPONSE_TIMEOUT;      

// constructors ================================================================
/** Default constructor. */
public CatalogConfiguration() {
  super();
  setArcImsCatalog(new ImsCatalog());
  setSearchConfig(new SearchConfig());
  getConfiguredTocs();
}

// properties ==================================================================

/**
 * Gets the configured ArcIMS catalog.
 * @return the configured catalog
 */
public ImsCatalog getArcImsCatalog() {
  return _imsCatalog;
}

/**
 * Sets the configured ArcIMS catalog.
 * @param catalog the configured catalog
 */
private void setArcImsCatalog(ImsCatalog catalog) {
  _imsCatalog = catalog;
}

/**
 * Gets the configured schemas.  Prevents queuing by not using synchronized and
 * only calls the getConfiguredSchemasHelper when necessary
 * 
 * @return the configured schemas
 */
public Schemas getConfiguredSchemas() {
  if (_configuredSchemas != null) {
    return _configuredSchemas;
  }
  return getConfiguredSchemasHelper();
}

/**
 * Loads the configured schemas
 * 
 * @return the configured schemas
 */
private synchronized Schemas getConfiguredSchemasHelper() {
  if (_configuredSchemas != null) {
    return _configuredSchemas;
  } else {
    Schemas schemas;
    try {
      SchemaFactory factory = new SchemaFactory();
      schemas = factory.loadSchemas();
      String cacheOpt = Val.chkStr(this.getParameters().getValue("cacheSchemaDefinitions"));
      if (!cacheOpt.equalsIgnoreCase("false")) {
        _configuredSchemas = schemas;
      }
      return schemas;
    } catch (Exception e) {
      e.printStackTrace(System.err);
      LogUtil.getLogger().log(Level.SEVERE, "Unable to configure schemas:", e);
      return new Schemas();
    }
  }
}

/**
 * Loads the configured tocs
 * 
 * @return the configured tocs
 */
public synchronized TocCollection getConfiguredTocs() {
  if (_tocCollection != null) {
    return _tocCollection;
  } else {
  	TocCollection tocs;
    try {
      TocFactory factory = new TocFactory();
      tocs = factory.loadTocConfig();
      String cacheOpt = Val.chkStr(this.getParameters().getValue("cacheTocDefinitions"));
      if (!cacheOpt.equalsIgnoreCase("false")) {
      	_tocCollection = tocs;
      }
      return tocs;
    } catch (Exception e) {
      e.printStackTrace(System.err);
      LogUtil.getLogger().log(Level.SEVERE, "Unable to load toc configuration:", e);
      return new TocCollection();
    }
  }
}

/**
 * Gets the harvesting history table name.
 * @return the harvesting history table name
 */
public String getHarvestingHistoryTableName() {
  return getTablePrefix() + "HARVESTING_HISTORY";
}

/**
 * Gets pending harvesting jobs table name.
 * This is a table holding jobs definition to be processed or being processed
 * at the time.
 * @return pending harvesting jobs table name
 */
public String getHarvestingJobsPendingTableName() {
  return getTablePrefix() + "HARVESTING_JOBS_PENDING";
}

/**
 * Gets completed harvesting jobs table name.
 * This is a table holding historical data about already processed jobs.
 * @return completed harvesting jobs table name
 */
public String getHarvestingJobsCompletedTableName() {
  return getTablePrefix() + "HARVESTING_JOBS_COMPLETED";
}

/**
 * Gets the Lucene configuration.
 * @return the Lucene configuration
 */
public LuceneConfig getLuceneConfig() {
  return _luceneConfig;
}

/**
 * Gets resource table name.
 * @return resource table name
 */
public String getResourceTableName() {
  return getTablePrefix() + "RESOURCE";
}

/**
 * Gets resource data table name.
 * @return resource data table name
 */
public String getResourceDataTableName() {
  return getTablePrefix() + "RESOURCE_DATA";
}

/**
 * Gets the prefix used for MVS table names.
 * @return the prefix
 */
public String getMvsTablePrefix() {
  return _mvsTablePrefix;
}

/**
 * Sets the prefix used for MVS table names.
 * @param prefix the prefix
 */
public void setMvsTablePrefix(String prefix) {
  _mvsTablePrefix = Val.chkStr(prefix);
  if (_tablePrefix.length() == 0) {
    _mvsTablePrefix = "MVS_";
  }
}

/**
 * Gets the map generally configured paramaters.
 * @return the configured parameters (from <parameter> tag)
 */
public StringAttributeMap getParameters() {
  return _parameters;
}

//StringAttributeMap

/**
 * Gets the search config.
 * @return the search config (possibly null)
 */
public SearchConfig getSearchConfig() {
  return _searchConfig;
}

/**
 * Sets the search config.
 * @param searchConfig the new search config
 */
public void setSearchConfig(SearchConfig searchConfig) {
  this._searchConfig = searchConfig;
}

/**
 * Gets the name of the saved search table.
 * @return the save search table name
 */
public String getSearchTableName() {
  return getTablePrefix() + "SEARCH";
}

/**
 * Gets the prefix used for table names.
 * @return the prefix
 */
public String getTablePrefix() {
  return _tablePrefix;
}

/**
 * Sets the prefix used for table names.
 * @param prefix the prefix
 */
public void setTablePrefix(String prefix) {
  _tablePrefix = Val.chkStr(prefix);
  if (_tablePrefix.length() == 0) {
    _tablePrefix = "GPT_";
  }
}

/**
 * Gets the name of the user table.
 * @return the user table name
 */
public String getUserTableName() {
  return getTablePrefix() + "USER";
}

/**
 * Gets the connection time out in milliseconds.
 * 
 * @return the connection time out (always >= 0)
 */
public int getConnectionTimeOutMs() {
  if(connectionTimeOut < 0) {
    connectionTimeOut = 0;
  }
  return connectionTimeOut;
}

/**
 * Sets the connection time out in milliseconds.
 * 
 * @param connectionTimeOut the new connection time out
 */
public void setConnectionTimeMs(int connectionTimeOut) {
  this.connectionTimeOut = connectionTimeOut;
}

/**
 * Gets the response time out in milliseconds
 * 
 * @return the response time out (always >= 0)
 */
public int getResponseTimeOutMs() {
  if(responseTimeOut < 0) {
    responseTimeOut = 0;
  }
  return responseTimeOut;
}

/**
 * Sets the response time out in milliseconds.
 * 
 * @param responseTimeOut the new response time out
 */
public void setResponseTimeOutMs(int responseTimeOut) {
  this.responseTimeOut = responseTimeOut;
}

/**
 * Gets the dcat schemas
 * @return the _dcatSchemas
 */
public DcatSchemas getDcatSchemas() {
	return _dcatSchemas;
}

// methods =====================================================================
/**
 * Makes a catalog index adapter.
 * @param context the request context
 * @return the catalog index adapter (null if none)
 */
public CatalogIndexAdapter makeCatalogIndexAdapter(RequestContext context) {
  if (this.getLuceneConfig().getIndexLocation().length() > 0) {
    return new com.esri.gpt.catalog.lucene.LuceneIndexAdapter(context);
  } else {
    return null;
  }
}
   

/**
 * Instantiates a new discovery query adapter.
 * <p/>
 * By default, a new instance of 
 * com.esri.gpt.catalog.lucene.LuceneQueryAdapter is returned.
 * <p/>
 * This can be overridden by the configuration parameter:
 * /gptConfig/catalog/parameter@key="discoveryQueryAdapter"
 * @return the discovery query adapter
 */
public DiscoveryQueryAdapter newDiscoveryQueryAdapter() {
  String className = Val.chkStr(getParameters().getValue("discoveryQueryAdapter"));
  if (className.length() == 0) {
    className = com.esri.gpt.catalog.lucene.LuceneQueryAdapter.class.getName();
  }
  try {
    Class<?> cls = Class.forName(className);
    Object obj = cls.newInstance();
    if (obj instanceof DiscoveryQueryAdapter) {
      return (DiscoveryQueryAdapter)obj;
    } else {
      String sMsg = "The configured discoveryQueryAdapter parameter is invalid: "+className;
      throw new ConfigurationException(sMsg);
    }
  } catch (ConfigurationException t) {
    throw t;
  } catch (Throwable t) {
    String sMsg = "Error instantiating discovery query adapter: "+className;
    throw new ConfigurationException(sMsg,t);
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" gptTablePrefix=").append(getTablePrefix()).append("\n");
  sb.append(" mvsTablePrefix=").append(getMvsTablePrefix()).append("\n");
  sb.append(" userTableName=").append(getUserTableName()).append("\n");
  sb.append(" searchTableName=").append(getSearchTableName()).append("\n");
  sb.append(" resourceTableName=").append(getResourceTableName()).append("\n");
  sb.append(" resourceDataTableName=").append(getResourceDataTableName()).append("\n");
  sb.append(" harvestingHistoryTableName=").append(
    getHarvestingHistoryTableName()).append("\n");
  sb.append(" harvestingJobsPendingTableName=").append(
    getHarvestingJobsPendingTableName()).append("\n");
  sb.append(" harvestingJobsCompletedTableName=").append(
    getHarvestingJobsCompletedTableName()).append("\n");

  sb.append(getArcImsCatalog()).append("\n");

  sb.append(this.getSearchConfig()).append("\n");
  sb.append(this.getLuceneConfig()).append("\n");

  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
