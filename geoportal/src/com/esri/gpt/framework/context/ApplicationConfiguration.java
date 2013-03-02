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
package com.esri.gpt.framework.context;

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.control.download.DownloadConfiguration;
import com.esri.gpt.control.webharvest.engine.HarvesterConfiguration;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactories;
import com.esri.gpt.framework.ArcGIS.InteractiveMap;
import com.esri.gpt.framework.mail.MailConfiguration;
import com.esri.gpt.framework.scheduler.ThreadSchedulerConfiguration;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.UnconfiguredIdentityAdapter;
import com.esri.gpt.framework.sql.DatabaseReferences;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.security.metadata.MetadataAccessPolicy;

import java.util.logging.Level;

/**
 * Represents the primary configuration for an application.
 * <p>   
 * This configuration is scoped at the application level. Writes
 * should occur during initial loading only. Writing during normal request
 * execution will affect all threads, it's not safe.
 */
public class ApplicationConfiguration extends Configuration {

// class variables =============================================================
// instance variables ==========================================================
private CatalogConfiguration _catalogConfiguration;
private DatabaseReferences _databaseReferences;
private IdentityConfiguration _identityConfiguration;
private InteractiveMap _interactiveMap = new InteractiveMap();
private MailConfiguration _mailConfiguration = new MailConfiguration();
private ThreadSchedulerConfiguration _threadSchedulerConfiguration;
private DownloadConfiguration _downloadDataConfiguration;
private MetadataAccessPolicy _metadataAccessPolicy;
private String _version = "";
private HarvesterConfiguration _harvesterConfiguration = new HarvesterConfiguration();
private ProtocolFactories _protocolFactories = new ProtocolFactories();

// constructors ================================================================
/** Default constructor. */
public ApplicationConfiguration() {
  super();
  setDatabaseReferences(new DatabaseReferences()); 
  setIdentityConfiguration(new IdentityConfiguration());
  setCatalogConfiguration(new CatalogConfiguration());
  setMailConfiguration(new MailConfiguration());
  setThreadSchedulerConfiguration(new ThreadSchedulerConfiguration());
  setDownloadDataConfiguration(new DownloadConfiguration());
  setMetadataAccessPolicy(new MetadataAccessPolicy());
}

// properties ==================================================================
/**
 * Gets the metadata access level configuration associated with this application.
 * @return the catalog configuration
 */
public MetadataAccessPolicy getMetadataAccessPolicy() {
  return _metadataAccessPolicy;
}

/**
 * Sets the metadata access level configuration associated with this application.
 * @param metadataAccessPolicy access policy
 */
public void setMetadataAccessPolicy(
    MetadataAccessPolicy metadataAccessPolicy) {
  this._metadataAccessPolicy = metadataAccessPolicy;
}

/**
 * Gets the metadata catalog configuration associated with this application.
 * @return the catalog configuration
 */
public CatalogConfiguration getCatalogConfiguration() {
  return _catalogConfiguration;
}

/**
 * Sets the metadata catalog configuration associated with this application.
 * @param configuration the catalog configuration
 */
private void setCatalogConfiguration(CatalogConfiguration configuration) {
  _catalogConfiguration = configuration;
  if (_catalogConfiguration == null) {
    _catalogConfiguration = new CatalogConfiguration();
  }
}

/**
 * Gets the database references associated with this application.
 * @return the database references
 */
public DatabaseReferences getDatabaseReferences() {
  return _databaseReferences;
}

/**
 * Sets the database references associated with this application.
 * @param references the database references
 */
private void setDatabaseReferences(DatabaseReferences references) {
  _databaseReferences = references;
  if (_databaseReferences == null) {
    _databaseReferences = new DatabaseReferences();
  }
}

/**
 * Gets the identity configuration associated with this application.
 * @return the identity configuration
 */
public IdentityConfiguration getIdentityConfiguration() {
  return _identityConfiguration;
}

/**
 * Sets the identity configuration associated with this application.
 * @param configuration the identity configuration
 */
private void setIdentityConfiguration(IdentityConfiguration configuration) {
  _identityConfiguration = configuration;
  if (_identityConfiguration == null) {
    _identityConfiguration = new IdentityConfiguration();
  }
}

/**
 * Gets the interactive map configuration (for ArcGIS Server Javascript API).
 * @return the interactive map configuration
 */
public InteractiveMap getInteractiveMap() {
  return _interactiveMap;
}

/**
 * Gets the mail configuration associated with this application.
 * @return the mail configuration
 */
public MailConfiguration getMailConfiguration() {
  return _mailConfiguration;
}

/**
 * Sets the mail configuration associated with this application.
 * @param configuration the mail configuration
 */
private void setMailConfiguration(MailConfiguration configuration) {
  _mailConfiguration = configuration;
  if (_mailConfiguration == null) {
    _mailConfiguration = new MailConfiguration();
  }
}

/**
 * Gets thread scheduler configuration.
 * @return thread scheduler configuration
 */
public ThreadSchedulerConfiguration getThreadSchedulerConfiguration() {
  return _threadSchedulerConfiguration;
}

/**
 * Sets thread scheduler configuration.
 * @param configuration thread scheduler configuration
 */
private void setThreadSchedulerConfiguration(
    ThreadSchedulerConfiguration configuration) {
  _threadSchedulerConfiguration = configuration;
  if (_threadSchedulerConfiguration == null) {
    _threadSchedulerConfiguration = new ThreadSchedulerConfiguration();
  }
}

/**
 * Gets data download configuration.
 * @return data download configuration
 */
public DownloadConfiguration getDownloadDataConfiguration() {
  return _downloadDataConfiguration;
}

/**
 * Sets data download configuration.
 * @param downloadConfiguration data download configuration
 */
public void setDownloadDataConfiguration(
    DownloadConfiguration downloadConfiguration) {
  _downloadDataConfiguration = downloadConfiguration != null ? downloadConfiguration
      : new DownloadConfiguration();
}

/**
 * Gets harvester configuration.
 * @return harvester configuration
 */
public HarvesterConfiguration getHarvesterConfiguration() {
  return this._harvesterConfiguration;
}

/**
 * Sets harvester configuration.
 * @param harvesterConfiguration harvester configuration
 */
public void setHarvesterConfiguration(HarvesterConfiguration harvesterConfiguration) {
  this._harvesterConfiguration = harvesterConfiguration != null ? harvesterConfiguration : new HarvesterConfiguration();
}

/**
 * Gets protocol factories.
 * @return protocol factories
 */
public ProtocolFactories getProtocolFactories() {
  return _protocolFactories;
}

/**
 * Sets protocol factories.
 * @param protocolFactories protocol factories
 */
public void setProtocolFactories(ProtocolFactories protocolFactories) {
  this._protocolFactories = protocolFactories!=null? protocolFactories: new ProtocolFactories();
}

/**
 * Gets the version.
 * @return the version
 */
public String getVersion() {
  return _version;
}

/**
 * Sets the version.
 * @param version the version
 */
protected void setVersion(String version) {
  _version = Val.chkStr(version);
}

// methods =====================================================================
/**
 * Instantiates a new Identity adapter.
 * @return the new identity adapter
 */
protected IdentityAdapter newIdentityAdapter() {
  IdentityAdapter identityAdapter = null;
  try {
    String adapterClassName = getIdentityConfiguration().getAdapterClassName();
    if (adapterClassName.length() == 0) {
      throw new ConfigurationException(
          "The identity adapter class name was not properly configured.");
    } else {
      Class<?> clsAdapter = Class.forName(adapterClassName);
      Object objAdapter = clsAdapter.newInstance();
      if (objAdapter instanceof IdentityAdapter) {
        identityAdapter = (IdentityAdapter) objAdapter;
        identityAdapter.setApplicationConfiguration(this);
      } else {
        throw new ConfigurationException(
            "The identity adapter class name is invalid: " + adapterClassName);
      }
    }
  } catch (Throwable t) {
    identityAdapter = new UnconfiguredIdentityAdapter();
    identityAdapter.setApplicationConfiguration(this);
    LogUtil.getLogger().log(Level.SEVERE,
        "Unable to create a new IdenityAdapter.", t);
  }
  return identityAdapter;
}

/**
 * Returns a string representation of this object.
 * @return the string
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();
  sb.append(getClass().getName()).append(" (\n");
  sb.append(" version=\"").append(getVersion()).append("\"\n");
  sb.append(getDatabaseReferences()).append("\n");
  sb.append(getMailConfiguration()).append("\n");
  sb.append(getInteractiveMap()).append("\n");
  sb.append(getCatalogConfiguration()).append("\n");
  sb.append(getIdentityConfiguration()).append("\n");
  sb.append(getThreadSchedulerConfiguration()).append("\n");
  sb.append(getDownloadDataConfiguration()).append("\n");
  sb.append(getMetadataAccessPolicy()).append("\n");
  sb.append(getHarvesterConfiguration()).append("\n");
  sb.append(getProtocolFactories()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
