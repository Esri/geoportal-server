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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.search.BooleanQuery;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.arcims.ImsService;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.lucene.LuceneIndexObserver;
import com.esri.gpt.catalog.lucene.LuceneIndexObserverInfo;
import com.esri.gpt.catalog.lucene.ParserAdaptorInfo;
import com.esri.gpt.catalog.lucene.ParserAdaptorInfos;
import com.esri.gpt.catalog.search.MapViewerConfigs;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.control.download.DownloadConfiguration;
import com.esri.gpt.control.download.ItemInfo;
import com.esri.gpt.control.georss.DcatField;
import com.esri.gpt.control.georss.DcatFields;
import com.esri.gpt.control.georss.DcatSchemas;
import com.esri.gpt.control.webharvest.engine.DataProcessorFactory;
import com.esri.gpt.control.webharvest.engine.HarvesterConfiguration;
import com.esri.gpt.control.webharvest.engine.LocalDataProcessorFactory;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactories;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.ProtocolInitializer;
import com.esri.gpt.control.webharvest.validator.IValidatorFactory;
import com.esri.gpt.control.webharvest.validator.ValidatorFactory;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.mail.MailConfiguration;
import com.esri.gpt.framework.scheduler.ThreadSchedulerConfiguration;
import com.esri.gpt.framework.security.codec.PC1_Encryptor;
import com.esri.gpt.framework.security.credentials.ProxyAuthenticator;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.IdentitySupport;
import com.esri.gpt.framework.security.identity.SingleSignOnMechanism;
import com.esri.gpt.framework.security.identity.ldap.LdapConfiguration;
import com.esri.gpt.framework.security.identity.ldap.LdapConnectionProperties;
import com.esri.gpt.framework.security.identity.ldap.LdapGroupProperties;
import com.esri.gpt.framework.security.identity.ldap.LdapUserProperties;
import com.esri.gpt.framework.security.identity.open.OpenProvider;
import com.esri.gpt.framework.security.metadata.MetadataAccessPolicy;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.UserAttribute;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.sql.DatabaseReference;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;

/**
 * Application configuration loader.
 * <p>
 * Loads the primary configuration for an application based upon the XML content
 * defined by the primary configuration source.
 * 
 * @see ApplicationConfiguration
 */
public class ApplicationConfigurationLoader {

  
/** Main XML configuration file location. */
private static final String MAIN_FILE = "gpt/config/gpt.xml";

private static final String MAIN_FILE_DEV = "gpt/config/gpt_dev.xml";

/** Default constructor. */
public ApplicationConfigurationLoader() {
}

// properties ==================================================================
/**
 * Gets the logger.
 * 
 * @return the logger
 */
private Logger getLogger() {
  return LogUtil.getLogger();
}

// methods =====================================================================
/**
 * Starts the configuration loading process.
 * 
 * @param appConfig
 *          the primary application configuration
 * @throws Exception
 */
public void load(ApplicationConfiguration appConfig) throws Exception {

  // load the dom
	String sConfigFile = null;
	Document dom = null;
	try {
		sConfigFile = MAIN_FILE_DEV;
		dom = DomUtil.makeDomFromResourcePath(sConfigFile, false);
		getLogger().log(Level.FINE, "Loaded configuration file: {0}", sConfigFile);
	} catch (Throwable e) {
    // Dev config not found
	}
	if (dom == null) {
		sConfigFile = MAIN_FILE;
		getLogger().log(Level.FINE, "Loading configuration file: {0}", sConfigFile);
		dom = DomUtil.makeDomFromResourcePath(sConfigFile, false);
	}
  XPath xpath = XPathFactory.newInstance().newXPath();


  try {
    Node root = (Node) xpath.evaluate("/gptConfig", dom, XPathConstants.NODE);
    appConfig.setVersion(xpath.evaluate("@version", root));

    // load configurations
    loadDatabase(appConfig, dom, root);
    loadIdentity(appConfig, dom, root);
    loadMail(appConfig, dom, root);
    loadInteractiveMap(appConfig, dom, root);
    loadCatalog(appConfig, dom, root);
    loadScheduler(appConfig, dom, root);
    loadDownloadData(appConfig, dom, root);
    loadHarvesterConfiguration(appConfig, dom, root);
    loadProtocolFactories(appConfig, dom, root);

    // forward proxy authentication
    Node ndProxyAuth = (Node) xpath.evaluate("forwardProxyAuth", root, XPathConstants.NODE);
    if (ndProxyAuth != null) {
      String sUser = xpath.evaluate("@username", ndProxyAuth);
      String sPwd = xpath.evaluate("@password", ndProxyAuth);
      boolean bEncrypted = Val.chkBool(xpath.evaluate("@encrypted", ndProxyAuth), false);
      boolean bSetSystemProperties = Val.chkBool(xpath.evaluate("@setSystemProperties", ndProxyAuth), true);
      if (bEncrypted) {
        try {
          String sDecrypted = PC1_Encryptor.decrypt(sPwd);
          sPwd = sDecrypted;
        } catch (Exception e) {
          this.getLogger().log(Level.SEVERE, "The forwardProxyAuth password failed to decrypt.", e);
        }
      }
      if ((sUser != null) && (sUser.length() > 0) && (sPwd != null) && (sPwd.length() > 0)) {
        ProxyAuthenticator.setDefault(sUser, sPwd);

        // set system properties
        if (bSetSystemProperties) {
          try {
            if (System.getProperty("http.proxyUser") == null) {
              System.setProperty("http.proxyUser", sUser);
              System.setProperty("http.proxyPassword", sPwd);
            }
            if (System.getProperty("https.proxyUser") == null) {
              System.setProperty("https.proxyUser", sUser);
              System.setProperty("https.proxyPassword", sPwd);
            }
          } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Error setting system properties for forward proxy authentication.", e);
          }
        }
      }
    }

  } catch (XPathExpressionException e) {
    e.printStackTrace(System.err);
  }

  StringAttributeMap params = appConfig.getCatalogConfiguration().getParameters();
  String param = Val.chkStr(params.getValue("catalog.echoConfigOnStartup"));
  boolean bEchoConfig = !param.equalsIgnoreCase("false");
  if (bEchoConfig) {
  	getLogger().info(appConfig.toString());
  }
}

/**
 * Loads the catalog configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws Exception 
 */
private void loadCatalog(ApplicationConfiguration appConfig, Document dom,
    Node root) throws Exception {
  XPath xpath = XPathFactory.newInstance().newXPath();

  // catalog configuration
  Node ndCat = (Node) xpath.evaluate("catalog", root, XPathConstants.NODE);
  if (ndCat != null) {
    CatalogConfiguration cfg = appConfig.getCatalogConfiguration();
    cfg.getParameters().clear();
    ImsService publish = cfg.getArcImsCatalog().getPublishService();
    ImsService browse = cfg.getArcImsCatalog().getBrowseService();
    cfg.setTablePrefix(Val.chkStr(xpath.evaluate("@gptTablePrefix", ndCat),"GPT_"));
    cfg.setMvsTablePrefix(Val.chkStr(xpath.evaluate("@mvsTablePrefix", ndCat),"MVS_"));

    publish.setServerUrl(xpath.evaluate("@metadataServerUrl", ndCat));
    publish.setServiceName(Val.chkStr(xpath.evaluate("@metadataServerPublishService",ndCat), "GPT_Publish_Metadata"));
    publish.setTimeoutMillisecs(Val.chkInt(xpath.evaluate("@metadataServerTimeoutMillisecs", ndCat), 0));

    browse.setServerUrl(publish.getServerUrl());
    browse.setServiceName(Val.chkStr(xpath.evaluate("@metadataServerBrowseService", ndCat),"GPT_Browse_Metadata"));
    browse.setTimeoutMillisecs(publish.getTimeoutMillisecs());

    // additional parameters
    populateParameters(cfg.getParameters(), ndCat);
    
    //load dcat fields
    if(cfg.getParameters().containsKey("dcat.mappings")){
    	loadDcatMappings(cfg.getDcatSchemas(),cfg.getParameters().get("dcat.mappings").getValue());
    }
    
    // parse http timeouts
    String connectionTimeout = cfg.getParameters().getValue("httpClientRequest.connectionTimeout");
    String responseTimeout   = cfg.getParameters().getValue("httpClientRequest.responseTimeout");
    
    // set http timeouts
    cfg.setConnectionTimeMs((int)parsePeriod(connectionTimeout, HttpClientRequest.DEFAULT_CONNECTION_TIMEOUT).getValue());
    cfg.setResponseTimeOutMs((int)parsePeriod(responseTimeout , HttpClientRequest.DEFAULT_RESPONSE_TIMEOUT).getValue());
  }

  // search configuration
  Node ndSearch = (Node) xpath.evaluate("catalog/search", root,
      XPathConstants.NODE);
  SearchConfig sCfg = appConfig.getCatalogConfiguration().getSearchConfig();
  sCfg.setSearchConfigNode(ndSearch);
  if (ndSearch != null) {
    sCfg.setResultsReviewsShown(
        Val.chkStr(xpath.evaluate("@searchResultsReviewsShown", ndSearch)));
    sCfg.setResultsPerPage(xpath.evaluate("@searchResultsPerPage", ndSearch));
    sCfg.setMaxSavedSearches(xpath.evaluate("@maxSavedSearches", ndSearch));
    sCfg.setCswProfile(
        Val.chkStr(xpath.evaluate("@cswServletUrlProfile", ndSearch),"urn:ogc:CSW:2.0.2:HTTP:OGCCORE:ESRI:GPT"));
    sCfg.setSearchUri(xpath.evaluate("@cswServletUrl", ndSearch));
    sCfg.setTimeOut(xpath.evaluate("@searchTimeoutMillisecs", ndSearch));
    sCfg.setDistributedSearchMaxSelectedSites(
        Val.chkStr(xpath.evaluate("@distributedSearchMaxSelectedSites", 
            ndSearch)));
    sCfg.setDistributedSearchTimeoutMillisecs(
        Val.chkStr(xpath.evaluate("@distributedSearchTimeoutMillisecs", 
            ndSearch)));
        
    sCfg.setAllowExternalSearch(Val.chkBool(xpath.evaluate(
        "@allowExternalSiteSearch", ndSearch), false));
    sCfg.setAllowTemporalSearch(Val.chkBool(xpath.evaluate("@allowTemporalSearch",ndSearch),false));
    sCfg.setJsfSuffix(Val.chkStr(xpath.evaluate(
        "@jsfSuffix", ndSearch)));
    sCfg.setGptToCswXsltPath(xpath.evaluate("@gpt2cswXslt", ndSearch));
    sCfg.setMapViewerUrl(Val.chkStr(xpath.evaluate("@mapViewerUrl", ndSearch),""));
    sCfg.validate();
  }


  NodeList nodes = (NodeList) xpath.evaluate(
      "catalog/search/repositories/repository",
      root, XPathConstants.NODESET);
  NodeList nodeList = nodes;
  LinkedHashMap<String, Map<String, String>> sFactory =
      new LinkedHashMap<String, Map<String, String>>();
  Map<String, String> attributes =
      new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
  /*attributes.put("key", "local");
  attributes.put("class", "com.esri.gpt.catalog.search.SearchEngineLocal");
  attributes.put("resourceKey", "catalog.search.searchSite.defaultsite");
  attributes.put("labelResourceKey", "catalog.search.searchSite.defaultsite");
  attributes.put("abstractResourceKey", "catalog.search.searchSite.defaultsite.abstract");
  sFactory.put("local", attributes);*/
  for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
    ndSearch = nodeList.item(i);
    attributes =
        new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    NamedNodeMap nnm = ndSearch.getAttributes();
    for (int j = 0; nnm != null && j < nnm.getLength(); j++) {
      Node nd = nnm.item(j);
      String key = Val.chkStr(nd.getNodeName());
      String value = Val.chkStr(nd.getNodeValue());
      attributes.put(key, value);
      if (key.equalsIgnoreCase("RESOURCEKEY")) {
        attributes.put("RESOURCEKEY", value);
      }
      if (key.equalsIgnoreCase("labelResourceKey")) {
        attributes.put("labelResourceKey", value);
      }
      if (key.equalsIgnoreCase("abstractResourceKey")) {
        attributes.put("abstractResourceKey", value);
      }
    }
    NodeList params = (NodeList) xpath.evaluate("parameter",
        ndSearch, XPathConstants.NODESET);
    for (int k = 0; params != null && k < params.getLength(); k++) {
      String key = xpath.evaluate("@key", params.item(k));
      String value = xpath.evaluate("@value", params.item(k));
      attributes.put(Val.chkStr(key), Val.chkStr(value));
    }

    String key = Val.chkStr(xpath.evaluate("@key", ndSearch));
    sFactory.put(key, attributes);

  }
  sCfg.setSearchFactoryRepos(sFactory);

  // Mapviewer
  ArrayList<MapViewerConfigs> mapViewerConfigs =
      new ArrayList<MapViewerConfigs>();
  nodes = (NodeList) xpath.evaluate("catalog/mapViewer/instance", root,
      XPathConstants.NODESET);
  for (int j = 0; nodes != null && j < nodes.getLength(); j++) {
    MapViewerConfigs mvConfigs = new MapViewerConfigs();
    Node nd = nodes.item(j);
    mvConfigs.setClassName(Val.chkStr(xpath.evaluate("@className", nd),"com.esri.gpt.catalog.search.MapViewerFlex"));
    mvConfigs.setUrl(xpath.evaluate("@url", nd));
    NodeList pNodeList = (NodeList) xpath.evaluate("parameter", nd, XPathConstants.NODESET);
    for (int k = 0; pNodeList != null && k < pNodeList.getLength(); k++) {
      String key = xpath.evaluate("@key", pNodeList.item(k));
      String value = xpath.evaluate("@value", pNodeList.item(k));
      if (key != null || value != null) {
        mvConfigs.addParameter(key, value);
      }
    }
    mapViewerConfigs.add(mvConfigs);

  }
  sCfg.setMapViewerInstances(mapViewerConfigs);

  // Lucene configuration
  Node ndLucene = (Node) xpath.evaluate("catalog/lucene", root, XPathConstants.NODE);
  if (ndLucene != null) {
    CatalogConfiguration cfg = appConfig.getCatalogConfiguration();
    cfg.getLuceneConfig().setIndexLocation(
        xpath.evaluate("@indexLocation", ndLucene));
    cfg.getLuceneConfig().setWriteLockTimeout(
        Val.chkInt(xpath.evaluate("@writeLockTimeout", ndLucene), -1));
    cfg.getLuceneConfig().setUseNativeFSLockFactory(
        Val.chkStr(xpath.evaluate("@useNativeFSLockFactory", ndLucene)).equalsIgnoreCase("true"));
    cfg.getLuceneConfig().setAnalyzerClassName(
        xpath.evaluate("@analyzerClassName", ndLucene));
    cfg.getLuceneConfig().setUseConstantScoreQuery(
        Val.chkBool(xpath.evaluate("@useConstantScoreQuery", ndLucene), false));
    cfg.getLuceneConfig().setMaxClauseCount(
        Val.chkInt(xpath.evaluate("@maxClauseCount", ndLucene), BooleanQuery.getMaxClauseCount()));

    ParserAdaptorInfos infos = new ParserAdaptorInfos();
    NodeList ndLstProxies = (NodeList) xpath.evaluate("adaptor", ndLucene,
        XPathConstants.NODESET);
    for (int i = 0; i < ndLstProxies.getLength(); i++) {
      Node ndProxy = ndLstProxies.item(i);
      String proxyName = xpath.evaluate("@name", ndProxy);
      String proxyClassName = xpath.evaluate("@className", ndProxy);

      ParserAdaptorInfo info = new ParserAdaptorInfo();
      info.setName(proxyName);
      info.setClassName(proxyClassName);

      NodeList ndListProps = (NodeList) xpath.evaluate("attribute", ndProxy,
          XPathConstants.NODESET);
      for (int p = 0; p < ndListProps.getLength(); p++) {
        Node ndProp = ndListProps.item(p);
        String key = xpath.evaluate("@key", ndProp);
        String value = xpath.evaluate("@value", ndProp);
        info.getAttributes().set(key, value);
      }

      infos.add(info);
    }

    cfg.getLuceneConfig().setParserProxies(infos.createParserProxies());
    
    NodeList ndObservers = (NodeList) xpath.evaluate("observer", ndLucene, XPathConstants.NODESET);
    for (Node ndObserver: new NodeListAdapter(ndObservers)) {
      LuceneIndexObserverInfo info = new LuceneIndexObserverInfo();
      info.setClassName(Val.chkStr(xpath.evaluate("@className", ndObserver)));
      NodeList ndListProps = (NodeList) xpath.evaluate("attribute", ndObserver, XPathConstants.NODESET);
      for (Node ndAttribute: new NodeListAdapter(ndListProps)) {
        String key = xpath.evaluate("@key", ndAttribute);
        String value = xpath.evaluate("@value", ndAttribute);
        info.getAttributes().set(key, value);
      }
      LuceneIndexObserver observer = info.createObserver();
      if (observer!=null) {
        cfg.getLuceneConfig().getObservers().add(observer);
      }
    }
    
  }

  loadMetadataAccessPolicyConfiguration(appConfig, root);

  StringAttributeMap params = appConfig.getCatalogConfiguration().getParameters();
  String param = Val.chkStr(params.getValue("catalog.loadSchemasOnStartup"));
  boolean bLoadSchemas = param.equalsIgnoreCase("true");
  if (bLoadSchemas) {
  	appConfig.getCatalogConfiguration().getConfiguredSchemas();
  }
}

/**
 * Load configured dcat mappings 
 * @param dcatSchemas the configured dcat schemas
 * @param dcatMappings the path to dcat mapping file
 * @throws IOException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 */
private void loadDcatMappings(DcatSchemas dcatSchemas, String dcatMappings) throws Exception {
	getLogger().log(Level.FINE, "Loading dcat mapping file: {0}", dcatMappings);
	Document dom = DomUtil.makeDomFromResourcePath(dcatMappings, false);
	XPath xpath = XPathFactory.newInstance().newXPath();
	Node dcat = (Node) xpath.evaluate("/dcat", dom, XPathConstants.NODE);
	NodeList fields = (NodeList) xpath.evaluate("fields", dcat, XPathConstants.NODESET);
	if(fields != null){		
		for (int j = 0; j < fields.getLength(); j++) {	  
	  	Node fld = fields.item(j);
	  	DcatFields dcatFields = new DcatFields();
	  	String schema = xpath.evaluate("@schema", fld);
	  	NodeList flds = (NodeList) xpath.evaluate("field", fld, XPathConstants.NODESET);
	  	for (int i = 0; i < flds.getLength(); i++) {
	  		Node field = flds.item(i);
	  		DcatField df = new DcatField();
	  		String name = xpath.evaluate("@name", field);
	  		df.setName(name);
	  		df.setType(xpath.evaluate("@type", field));
	  		df.setIndex(xpath.evaluate("@index", field));
	  		df.setDateFormat(xpath.evaluate("@dateFormat", field));
	  		String max = Val.chkStr(xpath.evaluate("@maxChars", field));
	  		String required = Val.chkStr(xpath.evaluate("@required", field));
	  		if(required.length() > 0){
	  			df.setRequired(Boolean.parseBoolean(required));
	  		}
	  		if(max.length() > 0){
	  			df.setMaxChars(Integer.parseInt(max));
	  		}
	  		df.setDelimiter(xpath.evaluate("@delimiter", field));
	  		dcatFields.add(df);
	  	}
	  	dcatSchemas.put(schema, dcatFields);
	  }
	}
}

/**
 * Loads the acsess policies.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws Exception
 * @throws Exception
 */
private void loadMetadataAccessPolicyConfiguration(
    ApplicationConfiguration appConfig, Node root) throws Exception {
  XPath xpath = XPathFactory.newInstance().newXPath();
  MetadataAccessPolicy aclCfg = appConfig.getMetadataAccessPolicy();
  Node ndPolicy = (Node) xpath.evaluate("catalog/metadataAccessPolicy", root,
      XPathConstants.NODE);
  if (ndPolicy != null) {
    String type = xpath.evaluate("@type", ndPolicy);
    aclCfg.setAccessPolicyType(type);
    String accessToGroupDN = xpath.evaluate("@protectedGroupDN", ndPolicy);
    if (accessToGroupDN != null && accessToGroupDN.trim().length() > 0) {
      aclCfg.setAccessToGroupDN(accessToGroupDN);
    }
  }
}

/**
 * Loads the database references.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadDatabase(ApplicationConfiguration appConfig, Document dom,
    Node root) throws XPathExpressionException {
  XPath xpath = XPathFactory.newInstance().newXPath();
  Node ndDb = (Node) xpath.evaluate("databaseReference", root,
      XPathConstants.NODE);
  if (ndDb != null) {
    DatabaseReference dbRef = new DatabaseReference();
    dbRef.setReferenceName(xpath.evaluate("@name", ndDb));
    dbRef.setJndiName(xpath.evaluate("@jndiName", ndDb));
    dbRef.setDirectDriverClassName(xpath.evaluate("@driver", ndDb));
    dbRef.setDirectUrl(xpath.evaluate("@url", ndDb));
    dbRef.setDirectUsername(xpath.evaluate("@username", ndDb));
    dbRef.setDirectPassword(xpath.evaluate("@password", ndDb));
    dbRef.getTags().add("default");
    appConfig.getDatabaseReferences().add(dbRef, true);
  }
}

/**
 * Loads the identify configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadIdentity(ApplicationConfiguration appConfig, Document dom,
    Node root) throws XPathExpressionException {

  // prepare
  XPath xpath = XPathFactory.newInstance().newXPath();
  IdentityConfiguration idConfig = appConfig.getIdentityConfiguration();
  LdapConfiguration ldapConfig = idConfig.getLdapConfiguration();
  Node ndIdentity = (Node) xpath.evaluate("identity", root, XPathConstants.NODE);
  if (ndIdentity == null) {
    return;
  }

  // primary parameters
  String sName = Val.chkStr(xpath.evaluate("@name", ndIdentity));
  String sRealm = Val.chkStr(xpath.evaluate("@realm", ndIdentity));
  String sEncKey = Val.chkStr(xpath.evaluate("@encKey", ndIdentity));
  String sAdapterClass = Val.chkStr(xpath.evaluate("@adapterClassName", ndIdentity));
  if (sName.length() == 0) {
    sName = "Identity Configuration";
  }
  if (sRealm.length() == 0) {
    sRealm = "Geoportal";
  }
  if (sAdapterClass.length() == 0) {
    sAdapterClass = "com.esri.gpt.framework.security.identity.ldap.LdapIdentityAdapter";
  }
  idConfig.setName(sName);
  idConfig.setRealm(sRealm);
  idConfig.setEncKey(sEncKey);

  // determine the adapter
  Node ndSimple = (Node) xpath.evaluate("simpleAdapter", ndIdentity, XPathConstants.NODE);
  Node ndLdap = (Node) xpath.evaluate("ldapAdapter", ndIdentity, XPathConstants.NODE);
  if (ndSimple != null) {
    ndLdap = null;
    sAdapterClass = "com.esri.gpt.framework.security.identity.local.SimpleIdentityAdapter";
  }
  idConfig.setAdapterClassName(sAdapterClass);

  // simple adapter configuration
  if (ndSimple != null) {

    // account
    Node ndAccount = (Node) xpath.evaluate("account", ndSimple, XPathConstants.NODE);
    if (ndAccount != null) {
      String sUser = xpath.evaluate("@username", ndAccount);
      String sPwd = xpath.evaluate("@password", ndAccount);
      String sDN = "cn=" + sUser + ",ou=simpleadapter";
      boolean bEncrypted = Val.chkBool(xpath.evaluate("@encrypted", ndAccount),
          false);
      if (bEncrypted) {
        try {
          String sDecrypted = PC1_Encryptor.decrypt(sPwd);
          sPwd = sDecrypted;
        } catch (Exception e) {
          this.getLogger().log(Level.SEVERE,
              "The simple account password failed to decrypt.", e);
        }
      }
      UsernamePasswordCredentials creds = new UsernamePasswordCredentials( sUser, sPwd);
      creds.setDistinguishedName(sDN);
      idConfig.setCatalogAdminDN(creds.getDistinguishedName());
      idConfig.getSimpleConfiguration().setServiceAccountCredentials(creds);
    }

    // roles
    Node ndRoles = (Node) xpath.evaluate("roles", ndSimple, XPathConstants.NODE);
    if (ndRoles != null) {
      Roles roles = idConfig.getConfiguredRoles();
      NodeList nlRoles = (NodeList) xpath.evaluate("role", ndRoles, XPathConstants.NODESET);
      for (int i = 0; i < nlRoles.getLength(); i++) {
        Node ndRole = nlRoles.item(i);
        Role role = new Role();
        role.setKey(xpath.evaluate("@key", ndRole));
        roles.add(role);
      }
      for (Role role : roles.values()) {
        role.buildFullRoleSet(roles);
      }
    }

  }

  // LDAP adapter configuration
  if (ndLdap != null) {

    // connection properties & service account
    Node ndCon = (Node) xpath.evaluate("ldapConnectionProperties", ndLdap,
        XPathConstants.NODE);
    if (ndCon != null) {
      LdapConnectionProperties props = ldapConfig.getConnectionProperties();
      props.setProviderUrl(xpath.evaluate("@providerURL", ndCon));
      props.setInitialContextFactoryName(xpath.evaluate(
          "@initialContextFactoryName", ndCon));
      props.setSecurityAuthenticationLevel(xpath.evaluate(
          "@securityAuthentication", ndCon));
      props.setSecurityProtocol(xpath.evaluate("@securityProtocol", ndCon));

      Node ndService = (Node) xpath.evaluate("ldapServiceAccount", ndCon, XPathConstants.NODE);
      if (ndService != null) {
        String sUser = xpath.evaluate("@securityPrincipal", ndService);
        String sPwd = xpath.evaluate("@securityCredentials", ndService);
        boolean bEncrypted = Val.chkBool(xpath.evaluate("@encrypted", ndService), false);
        if (bEncrypted) {
          try {
            String sDecrypted = PC1_Encryptor.decrypt(sPwd);
            sPwd = sDecrypted;
          } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "The securityCredentials failed to decrypt.", e);
          }
        }
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(sUser, sPwd);
        props.setServiceAccountCredentials(creds);
        idConfig.setCatalogAdminDN(xpath.evaluate("@catalogAdminDN", ndService));
      }
    }

    // single sign-on mechanism
    Node ndSSO = (Node) xpath.evaluate("singleSignOn", ndLdap,
        XPathConstants.NODE);
    if (ndSSO != null) {
      SingleSignOnMechanism sso = idConfig.getSingleSignOnMechanism();
      sso.setActive(Val.chkBool(xpath.evaluate("@active", ndSSO), false));
      sso.setCredentialLocation(xpath.evaluate("@credentialLocation", ndSSO));
      sso.setAnonymousValue(xpath.evaluate("@anonymousValue", ndSSO));
    }

    // self care support
    Node ndSupport = (Node) xpath.evaluate("selfCareSupport", ndLdap,
        XPathConstants.NODE);
    if (ndSupport != null) {
      IdentitySupport support = idConfig.getSupportedFunctions();
      support.setSupportsLogin(Val.chkBool(xpath.evaluate("@supportsLogin",
          ndSupport), true));
      support.setSupportsLogout(Val.chkBool(xpath.evaluate("@supportsLogout",
          ndSupport), true));
      support.setSupportsUserRegistration(Val.chkBool(xpath.evaluate(
          "@supportsUserRegistration", ndSupport), false));
      support.setSupportsUserProfileManagement(Val.chkBool(xpath.evaluate(
          "@supportsUserProfileManagement", ndSupport), false));
      support.setSupportsPasswordChange(Val.chkBool(xpath.evaluate(
          "@supportsPasswordChange", ndSupport), false));
      support.setSupportsPasswordRecovery(Val.chkBool(xpath.evaluate(
          "@supportsPasswordRecovery", ndSupport), false));
    }

    // roles
    Node ndRoles = (Node) xpath.evaluate("roles", ndLdap, XPathConstants.NODE);
    if (ndRoles != null) {
      Roles roles = idConfig.getConfiguredRoles();
      String sRegUserKey = Val.chkStr(xpath.evaluate("@registeredUserRoleKey",
          ndRoles));
      if (sRegUserKey.length() == 0) {
        sRegUserKey = "gptRegisteredUser";
      }
      roles.setAuthenticatedUserRequiresRole(Val.chkBool(xpath.evaluate(
          "@authenticatedUserRequiresRole", ndRoles), true));
      roles.setRegisteredUserRoleKey(sRegUserKey);
                 
      NodeList nlRoles = (NodeList) xpath.evaluate("role", ndRoles,
          XPathConstants.NODESET);
      for (int i = 0; i < nlRoles.getLength(); i++) {
        Node ndRole = nlRoles.item(i);
        Role role = new Role();
        role.setKey(xpath.evaluate("@key", ndRole));
        role.setInherits(xpath.evaluate("@inherits", ndRole));
        role.setResKey(xpath.evaluate("@resKey", ndRole));
        role.setManage(Val.chkBool(xpath.evaluate("@manage", ndRole),true));
        role.setForbidden(Val.chkBool(xpath.evaluate("@forbidden", ndRole),false));
        role.setDistinguishedName(xpath.evaluate("@groupDN", ndRole));
        roles.add(role);
      }
      for (Role role : roles.values()) {
        role.buildFullRoleSet(roles);
      }
    }

    // user properties
    Node ndUser = (Node) xpath.evaluate("users", ndLdap, XPathConstants.NODE);
    if (ndUser != null) {
      LdapUserProperties props = ldapConfig.getUserProperties();
      props.setUserDisplayNameAttribute(xpath.evaluate("@displayNameAttribute",
          ndUser));
      props.setPasswordEncryptionAlgorithm(xpath.evaluate(
          "@passwordEncryptionAlgorithm", ndUser));
      props.setUserDNPattern(xpath.evaluate("@newUserDNPattern", ndUser));
      props.setUsernameSearchPattern(xpath.evaluate("@usernameSearchPattern",
          ndUser));
      
      props.setUserRequestsSearchPattern(xpath.evaluate("@newUserRequestSearchPattern",
              ndUser));
      
      props.setUserSearchDIT(xpath.evaluate("@searchDIT", ndUser));
      NodeList nlObj = (NodeList) xpath.evaluate(
          "requiredObjectClasses/objectClass/@name", ndUser,
          XPathConstants.NODESET);
      for (int i = 0; i < nlObj.getLength(); i++) {
        props.addUserObjectClass(nlObj.item(i).getNodeValue());
      }
    }

    // user profile parameters
    UserAttributeMap uaMap = idConfig.getUserAttributeMap();
    NodeList nlUserAttr = (NodeList) xpath.evaluate(
        "users/userAttributeMap/attribute", ndLdap, XPathConstants.NODESET);
    for (int i = 0; i < nlUserAttr.getLength(); i++) {
      UserAttribute attr = new UserAttribute();
      attr.setKey(xpath.evaluate("@key", nlUserAttr.item(i)));
      attr.setLdapName(xpath.evaluate("@ldapName", nlUserAttr.item(i)));

      // TODO: need to do a better check to filter out badly defined
      // parameters
      boolean bIsLdap = (idConfig.getAdapterClassName().indexOf("Ldap") != -1);
      if (bIsLdap && (attr.getLdapName().length() > 0)) {
        uaMap.add(attr);
      }
    }
    ldapConfig.getUserProperties().getUserProfileMapping().configureFromUserAttributes(uaMap);

    // group properties
    Node ndGroup = (Node) xpath.evaluate("groups", ndLdap, XPathConstants.NODE);
    if (ndGroup != null) {
      LdapGroupProperties props = ldapConfig.getGroupProperties();
      props.setGroupDisplayNameAttribute(xpath.evaluate(
          "@displayNameAttribute", ndGroup));
      props.setGroupDynamicMemberAttribute(xpath.evaluate(
          "@dynamicMemberOfGroupsAttribute", ndGroup));
      props.setGroupDynamicMembersAttribute(xpath.evaluate(
          "@dynamicMembersAttribute", ndGroup));
      props.setGroupMemberAttribute(xpath.evaluate("@memberAttribute", ndGroup));
      props.setGroupMemberSearchPattern(xpath.evaluate("@memberSearchPattern",
          ndGroup));
      props.setGroupNameSearchPattern(xpath.evaluate("@nameSearchPattern",
          ndGroup));
      props.setGroupSearchDIT(xpath.evaluate("@searchDIT", ndGroup));
    }

    // metadata management groups
    NodeList nlMmg = (NodeList) xpath.evaluate("groups/metadataManagementGroup",ndLdap,XPathConstants.NODESET);
    for (int i = 0; i < nlMmg.getLength(); i++) {
      Node ndMmg = nlMmg.item(i);
      Group group = new Group();
      group.setDistinguishedName(xpath.evaluate("@groupDN", ndMmg));
      group.setKey(group.getDistinguishedName());
      group.setName(xpath.evaluate("@name", ndMmg));
      idConfig.getMetadataManagementGroups().add(group);
    }

  }
  
  // open providers
  NodeList nlOpenProviders = (NodeList)xpath.evaluate("openProviders/openProvider",ndIdentity,XPathConstants.NODESET);
  for (int i=0; i<nlOpenProviders.getLength(); i++) {
    try {
      OpenProvider op = new OpenProvider();
      op.processConfgurationNode(idConfig.getOpenProviders(),nlOpenProviders.item(i));
    } catch (Exception e) {
      this.getLogger().log(Level.SEVERE,"Error while configuring openProvider.",e);
    }
  }
}

/**
 * Loads the interactive map configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadInteractiveMap(ApplicationConfiguration appConfig,
    Document dom, Node root) throws XPathExpressionException {
  XPath xpath = XPathFactory.newInstance().newXPath();
  appConfig.getInteractiveMap().setJsapiUrl(
      xpath.evaluate("interactiveMap/@jsapiUrl", root));
  appConfig.getInteractiveMap().setMapServiceUrl(
      xpath.evaluate("interactiveMap/@mapServiceUrl", root));
  appConfig.getInteractiveMap().setMapServiceType(
      xpath.evaluate("interactiveMap/@mapServiceType", root));
  appConfig.getInteractiveMap().setGeometryServiceUrl(
      xpath.evaluate("interactiveMap/@geometryServiceUrl", root));
  appConfig.getInteractiveMap().setLocatorUrl(
      xpath.evaluate("interactiveMap/@locatorUrl", root));
  appConfig.getInteractiveMap().setLocatorSingleFieldParameter(
      xpath.evaluate("interactiveMap/@locatorSingleFieldParameter", root));
  appConfig.getInteractiveMap().setMapVisibleLayers(
      xpath.evaluate("interactiveMap/@mapVisibleLayers", root));
  appConfig.getInteractiveMap().setMapInitialExtent(
      xpath.evaluate("interactiveMap/@mapInitialExtent", root));
}

/**
 * Loads the mail configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadMail(ApplicationConfiguration appConfig, Document dom,
    Node root) throws XPathExpressionException {
  XPath xpath = XPathFactory.newInstance().newXPath();
  MailConfiguration mcfg = appConfig.getMailConfiguration();
  mcfg.put(
      xpath.evaluate("mail/@smtpHost", root),
      xpath.evaluate("mail/@smtpPort", root),
      xpath.evaluate("mail/@siteEmailAddress", root),
      xpath.evaluate("mail/@siteEmailAddress", root));
  mcfg.setEmailAddressRegexp(xpath.evaluate("mail/@emailAddressRegexp", root));

  Node ndAuth = (Node) xpath.evaluate("mail/smtpAuth", root, XPathConstants.NODE);
  if (ndAuth != null) {
    String sUser = Val.chkStr(xpath.evaluate("@username", ndAuth));
    String sPwd = xpath.evaluate("@password", ndAuth);
    boolean bEncrypted = Val.chkBool(xpath.evaluate("@encrypted", ndAuth), false);
    if (bEncrypted) {
      try {
        String sDecrypted = PC1_Encryptor.decrypt(sPwd);
        sPwd = sDecrypted;
      } catch (Exception e) {
        this.getLogger().log(Level.SEVERE, "The smptAuth password failed to decrypt.", e);
      }
    }
    if ((sUser != null) && (sUser.length() > 0) && (sPwd != null) && (sPwd.length() > 0)) {
      mcfg.setPasswordAuthentication(new javax.mail.PasswordAuthentication(sUser, sPwd));
    }
  }
}

/**
 * Loads the scheduler configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadScheduler(ApplicationConfiguration appConfig, Document dom,
    Node root) throws XPathExpressionException {

  XPath xpath = XPathFactory.newInstance().newXPath();
  ThreadSchedulerConfiguration tsConfig = appConfig.getThreadSchedulerConfiguration();
  Node ndScheduler = (Node) xpath.evaluate("scheduler", root,
      XPathConstants.NODE);
  if (ndScheduler == null) {
    return;
  }

  // primary parameters
  tsConfig.setActive(Val.chkBool(xpath.evaluate("@active", ndScheduler), false));
  tsConfig.setCorePoolSize(Val.chkInt(xpath.evaluate("@corePoolSize",
      ndScheduler), 0));

  // threads
  NodeList nlThreads = (NodeList) xpath.evaluate("thread", ndScheduler,
      XPathConstants.NODESET);
  for (int i = 0; i < nlThreads.getLength(); i++) {
    Node ndThread = nlThreads.item(i);
    String sClass = Val.chkStr(xpath.evaluate("@class", ndThread));
    String sDelay = Val.chkStr(xpath.evaluate("@delay", ndThread));
    String sPeriod = Val.chkStr(xpath.evaluate("@period", ndThread));
    String sAt = Val.chkStr(xpath.evaluate("@at", ndThread));

    // read parameters
    StringAttributeMap parameters = new StringAttributeMap();
    populateParameters(parameters, ndThread);

    // add definition
    tsConfig.addDefinition(sClass, sDelay, sPeriod, sAt, parameters);
  }
}

/**
 * Loads the download data configuration.
 * 
 * @param appConfig
 *          the primary application configuration
 * @param dom
 *          the configuration document
 * @param root
 *          the root node for the document
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void loadDownloadData(ApplicationConfiguration appConfig, Document dom,
    Node root) throws XPathExpressionException {

  // download configuration
  XPath xpath = XPathFactory.newInstance().newXPath();
  Node ndDownload = (Node) xpath.evaluate("downloadData", root,
      XPathConstants.NODE);
  if (ndDownload != null) {
    DownloadConfiguration cfg = appConfig.getDownloadDataConfiguration();
    cfg.setTaskUrl(xpath.evaluate("@taskUrl", ndDownload));
    cfg.setMapServiceUrl(xpath.evaluate("@mapServiceUrl", ndDownload));
    cfg.setMapServiceType(xpath.evaluate("@mapServiceType", ndDownload));
    cfg.setMapInitialExtent(xpath.evaluate("@mapInitialExtent", ndDownload));

    // load projections
    NodeList ndProjections = (NodeList) xpath.evaluate(
        "projections/projection", ndDownload, XPathConstants.NODESET);
    for (int i = 0; i < ndProjections.getLength(); i++) {
      Node node = ndProjections.item(i);
      String key = xpath.evaluate("@key", node);
      String alias = xpath.evaluate("@alias", node);
      String resKey = xpath.evaluate("@resKey", node);
      ItemInfo ii = new ItemInfo(key, alias, resKey);
      cfg.getProjectionInfo().add(ii);
    }

    // load output format
    NodeList ndFormats = (NodeList) xpath.evaluate("formats/format",
        ndDownload, XPathConstants.NODESET);
    for (int i = 0; i < ndFormats.getLength(); i++) {
      Node node = ndFormats.item(i);
      String key = xpath.evaluate("@key", node);
      String alias = xpath.evaluate("@alias", node);
      String resKey = xpath.evaluate("@resKey", node);
      ItemInfo ii = new ItemInfo(key, alias, resKey);
      cfg.getOutputFormatInfo().add(ii);
    }

    // load feature formats
    NodeList ndFeatureFormats = (NodeList) xpath.evaluate("features/feature",
        ndDownload, XPathConstants.NODESET);
    for (int i = 0; i < ndFeatureFormats.getLength(); i++) {
      Node node = ndFeatureFormats.item(i);
      String key = xpath.evaluate("@key", node);
      String alias = xpath.evaluate("@alias", node);
      String resKey = xpath.evaluate("@resKey", node);
      ItemInfo ii = new ItemInfo(key, alias, resKey);
      cfg.getFeatureFormatInfo().add(ii);
    }

    // load raster formats
    NodeList ndRasterFormats = (NodeList) xpath.evaluate("rasters/raster",
        ndDownload, XPathConstants.NODESET);
    for (int i = 0; i < ndRasterFormats.getLength(); i++) {
      Node node = ndRasterFormats.item(i);
      String key = xpath.evaluate("@key", node);
      String alias = xpath.evaluate("@alias", node);
      String resKey = xpath.evaluate("@resKey", node);
      ItemInfo ii = new ItemInfo(key, alias, resKey);
      cfg.getRasterFormatInfo().add(ii);
    }
  }
}

/**
 * Loads harvester configuration.
 * @param appConfig application configuration
 */
private void loadHarvesterConfiguration(ApplicationConfiguration appConfig, Document dom, Node root) throws XPathExpressionException {
  StringAttributeMap parameters = appConfig.getCatalogConfiguration().getParameters();
  HarvesterConfiguration cfg = appConfig.getHarvesterConfiguration();

  String active = Val.chkStr(parameters.getValue("webharvester.active"));
  String suspended = Val.chkStr(parameters.getValue("webharvester.suspended"));
  String queueEnabled = Val.chkStr(parameters.getValue("webharvester.queueEnabled"));
  String poolsize = Val.chkStr(parameters.getValue("webharvester.poolSize"));
  String autoselectfrequency = Val.chkStr(parameters.getValue("webharvester.autoSelectFrequency"));
  String watchdogfrequency = Val.chkStr(parameters.getValue("webharvester.watchDogFrequency"));
  String basecontextpath = Val.chkStr(parameters.getValue("webharvester.baseContextPath"));
  String maxRepRecords = Val.chkStr(parameters.getValue("webharvester.maxRepRecords"));
  String maxRepErrors = Val.chkStr(parameters.getValue("webharvester.maxRepErrors"));
  String resourceAutoApprove = Val.chkStr(parameters.getValue("webharvester.resource.autoApprove"));

  Logger logger = getLogger();

  if (Val.chkBool(active, true)) {
    cfg.setActive(true);
    cfg.setQueueEnabled(true);
  } else {
    cfg.setActive(false);
    cfg.setQueueEnabled(false);
  }

  if (Val.chkBool(suspended, false)) {
    cfg.setSuspended(true);
  } else {
    cfg.setSuspended(false);
  }

  if (queueEnabled.length()>0) {
    cfg.setQueueEnabled(Val.chkBool(queueEnabled, cfg.getQueueEnabled()) || cfg.getActive());
  }

  if (poolsize.length() > 0) {
    try {
      int num = Integer.parseInt(poolsize);
      if (num <= 0) {
        logger.info("[SYNCHRONIZER] Parameter \"webharvester.poolSize\" less or equal to zero. No harvestig will be performed.");
      }
      cfg.setPoolSize(num);
    } catch (NumberFormatException ex) {
      logger.log(Level.INFO, "[SYNCHRONIZER] Invalid \"webharvester.poolSize\" parameter. Default {0} will be used instead.", HarvesterConfiguration.DEFAULT_POOL_SIZE);
      cfg.setPoolSize(HarvesterConfiguration.DEFAULT_POOL_SIZE);
    }
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.poolSize\" parameter. Default {0} will be used instead.", HarvesterConfiguration.DEFAULT_POOL_SIZE);
    cfg.setPoolSize(HarvesterConfiguration.DEFAULT_POOL_SIZE);
  }

  if (autoselectfrequency.length() > 0) {
    try {
      TimePeriod tp = TimePeriod.parseValue(autoselectfrequency);
      cfg.setAutoSelectFrequency(tp);
    } catch (NumberFormatException ex) {
      logger.log(Level.INFO, "[SYNCHRONIZER] Invalid \"webharvester.autoSelectFrequency\" parameter. Default {0} will be used instead.", HarvesterConfiguration.AUTOSELECT_FREQUENCY);
      cfg.setAutoSelectFrequency(new TimePeriod(HarvesterConfiguration.AUTOSELECT_FREQUENCY));
    }
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.autoSelectFrequency\" parameter. Default {0} will be used instead.", HarvesterConfiguration.AUTOSELECT_FREQUENCY);
    cfg.setAutoSelectFrequency(new TimePeriod(HarvesterConfiguration.AUTOSELECT_FREQUENCY));
  }

  if (watchdogfrequency.length() > 0) {
    try {
      TimePeriod tp = TimePeriod.parseValue(watchdogfrequency);
      cfg.setWatchDogFrequency(tp);
    } catch (NumberFormatException ex) {
      logger.log(Level.INFO, "[SYNCHRONIZER] Invalid \"webharvester.watchDogFrequency\" parameter. Default {0} will be used instead.", HarvesterConfiguration.WATCHDOG_FREQUENCY);
      cfg.setWatchDogFrequency(new TimePeriod(HarvesterConfiguration.WATCHDOG_FREQUENCY));
    }
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.watchDogFrequency\" parameter. Default {0} will be used instead.", HarvesterConfiguration.WATCHDOG_FREQUENCY);
    cfg.setWatchDogFrequency(new TimePeriod(HarvesterConfiguration.WATCHDOG_FREQUENCY));
  }

  if (basecontextpath.length() > 0) {
    cfg.setBaseContextPath(basecontextpath);
  } else {
    String reverseProxyPath = Val.chkStr(parameters.getValue("reverseProxy.baseContextPath"));
    if (reverseProxyPath.length() > 0) {
      logger.info("[SYNCHRONIZER] Missing \"webharvester.baseContextPath\" parameter. Value of \"reverseProxy.baseContextPath\" will be used instead.");
    } else {
      logger.info("[SYNCHRONIZER] Missing \"webharvester.baseContextPath\" parameter. Harvest notification messages will be sent without information about harvest report.");
    }
  }

  if (maxRepRecords.length() > 0) {
    try {
      long num = Long.parseLong(maxRepRecords);
      if (num < 0) {
        logger.info("[SYNCHRONIZER] Parameter \"webharvester.maxRepRecords\" less than zero. No limits will be set.");
      }
      cfg.setMaxRepRecords(num);
    } catch (NumberFormatException ex) {
      logger.log(Level.INFO, "[SYNCHRONIZER] Invalid \"webharvester.maxRepRecords\" parameter. Default {0} will be used instead.", HarvesterConfiguration.MAX_REP_RECORDS);
      cfg.setMaxRepRecords(HarvesterConfiguration.MAX_REP_RECORDS);
    }
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.maxRepRecords\" parameter. Default {0} will be used instead.", HarvesterConfiguration.MAX_REP_RECORDS);
    cfg.setMaxRepRecords(HarvesterConfiguration.MAX_REP_RECORDS);
  }


  if (maxRepErrors.length() > 0) {
    try {
      long num = Long.parseLong(maxRepErrors);
      if (num < 0) {
        logger.info("[SYNCHRONIZER] Parameter \"webharvester.maxRepErrors\" less than zero. No limits will be set.");
      }
      cfg.setMaxRepErrors(num);
    } catch (NumberFormatException ex) {
      logger.log(Level.INFO, "[SYNCHRONIZER] Invalid \"webharvester.maxRepErrors\" parameter. Default {0} will be used instead.", HarvesterConfiguration.MAX_REP_ERRORS);
      cfg.setMaxRepErrors(HarvesterConfiguration.MAX_REP_ERRORS);
    }
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.maxRepErrors\" parameter. Default {0} will be used instead.", HarvesterConfiguration.MAX_REP_ERRORS);
    cfg.setMaxRepErrors(HarvesterConfiguration.MAX_REP_ERRORS);
  }

  if (resourceAutoApprove.length() > 0) {
    boolean bool = Val.chkBool(resourceAutoApprove,HarvesterConfiguration.RESOURCE_AUTOAPPROVE);
    cfg.setResourceAutoApprove(bool);
  } else {
    logger.log(Level.INFO, "[SYNCHRONIZER] Missing \"webharvester.resource.autoApprove\" parameter. Default {0} will be used instead.", HarvesterConfiguration.RESOURCE_AUTOAPPROVE);
    cfg.setResourceAutoApprove(HarvesterConfiguration.RESOURCE_AUTOAPPROVE);
  }
  
  // load data processor factories
  XPath xpath = XPathFactory.newInstance().newXPath();
  
  // add local data processor factory by default
  cfg.getDataProcessorFactories().add(new LocalDataProcessorFactory());

  // get root of webharvester configuration
  Node ndWebHarvester = (Node) xpath.evaluate("webharvester", root, XPathConstants.NODE);
  if (ndWebHarvester!=null) {
    // create and initialize data processor for each netry in configuration
    NodeList ndDataProcessorFactories = (NodeList) xpath.evaluate("dataProcessorFactory", ndWebHarvester, XPathConstants.NODESET);
    for (Node ndDataProcessorFactory : new NodeListAdapter(ndDataProcessorFactories)) {
      String className = Val.chkStr((String) xpath.evaluate("@className", ndDataProcessorFactory, XPathConstants.STRING));
      String name = Val.chkStr((String) xpath.evaluate("@name", ndDataProcessorFactory, XPathConstants.STRING));
      boolean enabled = Val.chkBool(Val.chkStr((String) xpath.evaluate("@enabled", ndDataProcessorFactory, XPathConstants.STRING)), true);
      if (enabled) {
        try {
          Class factoryClass = Class.forName(className);
          DataProcessorFactory processorFactory = (DataProcessorFactory) factoryClass.newInstance();
          processorFactory.setName(name);
          processorFactory.init(ndDataProcessorFactory);
          cfg.getDataProcessorFactories().add(processorFactory);
        } catch (Exception ex) {
          getLogger().log(Level.SEVERE, "Error creating processor factory: "+className, ex);
        }
      } else {
        if (LocalDataProcessorFactory.class.getCanonicalName().equals(className)) {
          removeDataProcessorFactory(cfg.getDataProcessorFactories(), className);
        }
      }
    }
  }
}

private void removeDataProcessorFactory(List<DataProcessorFactory> factories, String factoryClassName) {
  for (DataProcessorFactory factory : factories) {
    if (factory.getClass().getCanonicalName().equals(factoryClassName)) {
      factories.remove(factory);
      break;
    }
  }
}

/**
 * Loads protocol factories.
 * @param appConfig protocol factories
 */
private void loadProtocolFactories(ApplicationConfiguration appConfig, Document dom, Node root) throws XPathExpressionException {
  ProtocolFactories factories = appConfig.getProtocolFactories();
  XPath xpath = XPathFactory.newInstance().newXPath();

  Node ndProtocols = (Node) xpath.evaluate("protocols", root, XPathConstants.NODE);
  if (ndProtocols!=null) {
    // check 'default' attribute of the 'protcols' node; if present and true than initialize default factories
    boolean defaultValue = Val.chkBool((String) xpath.evaluate("@default", ndProtocols, XPathConstants.STRING),false);
    if (defaultValue) {
        factories.initDefault();
    }
    
    // initilaize explicit protocol factories
    NodeList lstProtocol = (NodeList) xpath.evaluate("protocol", ndProtocols, XPathConstants.NODESET);
    for (Node ndProto : new NodeListAdapter(lstProtocol)) {
      String factoryClass = (String) xpath.evaluate("@factoryClass", ndProto, XPathConstants.STRING);
      try {
        Class fc = Class.forName(factoryClass);
        ProtocolFactory factory = (ProtocolFactory) fc.newInstance();
        ProtocolInitializer.init(factory, ndProto);
        String resourceKey = Val.chkStr((String) xpath.evaluate("@resourceKey", ndProto, XPathConstants.STRING));
        factories.put(factory.getName(), factory, resourceKey);
      } catch (Exception ex) {
        getLogger().log(Level.WARNING, "Error loading protocol: "+factoryClass, ex);
      }
      
      String validatorFactoryClass = Val.chkStr((String) xpath.evaluate("validator/@factoryClass", ndProto, XPathConstants.STRING));
      if (!validatorFactoryClass.isEmpty()) {
        try {
          Class fc = Class.forName(validatorFactoryClass);
          IValidatorFactory factory = (IValidatorFactory) fc.newInstance();
          ValidatorFactory.register(factory);
        } catch (Exception ex) {
          getLogger().log(Level.WARNING, "Error loading protocol validator factory: "+validatorFactoryClass, ex);
        }
      }
    }
  } else {
    factories.initDefault();
  }
}

/**
 * Reads the parameters under a parent node.
 * 
 * @param parameters
 *          the parameters to populate
 * @param parent
 *          the parent node containing the parameters to read
 * @throws XPathExpressionException
 *           indicates a programming error, bad XPath
 */
private void populateParameters(StringAttributeMap parameters, Node parent)
    throws XPathExpressionException {
  XPath xpath = XPathFactory.newInstance().newXPath();
  NodeList nlParameters = (NodeList) xpath.evaluate("parameter", parent,
      XPathConstants.NODESET);
  for (int i = 0; i < nlParameters.getLength(); i++) {
    Node ndParameter = nlParameters.item(i);
    String sKey = Val.chkStr(xpath.evaluate("@key", ndParameter));
    String sValue = Val.chkStr(xpath.evaluate("@value", ndParameter));
    if (sKey.length() > 0) {
      parameters.add(new StringAttribute(sKey, sValue));
    }
  }
}
  
/**
 * Safely parses time period giving default value if time period can not be parsed.
 * @param periodDef period definition to parse
 * @param defaultValue default value if period definition cannot be parsed
 * @return time period
 */
private TimePeriod parsePeriod(String periodDef, long defaultValue) {
  try {
    periodDef = Val.chkStr(periodDef);
    if (periodDef.isEmpty()) {
      return new TimePeriod(defaultValue);
    }
    return TimePeriod.parseValue(periodDef);
  } catch (IllegalArgumentException ex) {
    return new TimePeriod(defaultValue);
  }
}
}
