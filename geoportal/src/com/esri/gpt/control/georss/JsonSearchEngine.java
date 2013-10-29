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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.catalog.lucene.LuceneQueryAdapter;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * JSON search engine.
   * <p/>
   * Uses <i>json.engine.className</i> parameter from <b>gpt.xml</b> to create instance.
   * Creates default instance if parameter is empty.
   * <p/>
   * Two parameters: <i>json.meta.lucene</i> and <i>json.meta.catalog</i> from gpt.xml
   * configuration file determines whether certain family of output parameters 
   * is allowed to be print to the output or not. Default: <code>true</code>.
 * @see ExtJsonFeedWriter
 */
public abstract class JsonSearchEngine {

  /**
   * Logger.
   */
  private static final Logger LOG = Logger.getLogger(JsonSearchEngine.class.getCanonicalName());
  
  /**
   * Default search engine.
   */
  private static final JsonSearchEngine defaultEngine = new JsonSearchEngine() {
    @Override
    public IFeedRecords search(HttpServletRequest request, HttpServletResponse response, RequestContext context, RestQuery query) throws Exception {
      return doSearch(request, response, context, query);
    }
  };

  /**
   * Creates instance of the search engine. 
   * @return instance of {@link JsonSearchEngine}
   */
  public static JsonSearchEngine createInstance() {
    String className = getConfigParam("json.engine.className");
    if (className.isEmpty()) {
      return defaultEngine;
    } else {
      try {
        Class engineClass = Class.forName(className);
        return (JsonSearchEngine) engineClass.newInstance();
      } catch (Exception ex) {
        LOG.log(Level.INFO, "Error creating JSON search engine: " + className +". Using default JSON search engine instead.", ex);
        return defaultEngine;
      }
    }
  }

  /**
   * Performs search operation.
   *
   * @param request HTTP servlet request
   * @param response HTTP servlet response
   * @param context request context
   * @param query query
   * @return records
   * @throws Exception if searching fails
   */
  public abstract IFeedRecords search(HttpServletRequest request, HttpServletResponse response, RequestContext context, RestQuery query) throws Exception;

  /**
   * Loads standard field meta info.
   * @param fields list of fields
   */
  protected void loadStdFieldMeta(List<IFeedRecords.FieldMeta> fields) {
    fields.add(new IFeedRecords.FieldMeta("objectid", "esriFieldTypeOID", "OBJECTID"));
    fields.add(new IFeedRecords.FieldMeta("title", "esriFieldTypeString", "Title", 256));
    fields.add(new IFeedRecords.FieldMeta("id", "esriFieldTypeGUID", "UUID", 38));
    fields.add(new IFeedRecords.FieldMeta("updated", "esriFieldTypeDate", "Updated", 20));
    fields.add(new IFeedRecords.FieldMeta("contentType", "esriFieldTypeString", "Content Type", 64));
    fields.add(new IFeedRecords.FieldMeta("summary", "esriFieldTypeString", "Summary", 256));
    fields.add(new IFeedRecords.FieldMeta("shape", "esriFieldTypeGeometry", "Shape"));
  }

  /**
   * Performs search operation.
   * @param request HTTP servlet request
   * @param response HTTP servlet response
   * @param context request context
   * @param query query
   * @return records
   * @throws Exception if searching fails
   */
  protected IFeedRecords doSearch(HttpServletRequest request, HttpServletResponse response, RequestContext context, RestQuery query) throws Exception {
    MessageBroker msgBroker = new FacesContextBroker(request, response).extractMessageBroker();
    final Map<DiscoveredRecord, Map<String, List<String>>> mapping = new HashMap<DiscoveredRecord, Map<String, List<String>>>();

    List<IFeedRecords.FieldMeta> fields = new ArrayList<IFeedRecords.FieldMeta>();
    loadStdFieldMeta(fields);

    int startRecord = query.getFilter().getStartRecord();
    boolean returnIdsOnly = Val.chkBool(Val.chkStr(request.getParameter("returnIdsOnly")), false);
    if (returnIdsOnly) {
      startRecord = 1;
      query.getFilter().setMaxRecords(1);
      LuceneQueryAdapter tmp = new LuceneQueryAdapter();
      tmp.execute(context, query);
      query.getFilter().setMaxRecords(query.getResult().getNumberOfHits());
    }

    query.getFilter().setStartRecord(startRecord);
    LuceneQueryAdapter lqa = new LuceneQueryAdapter() {
      @Override
      protected void onRecord(DiscoveredRecord record, Document document) {
        Map<String, List<String>> fieldMap = new HashMap<String, List<String>>();
        for (Fieldable field : document.getFields()) {
          String name = field.name();
          List<String> fieldValues = fieldMap.get(name);
          if (fieldValues == null) {
            fieldValues = new ArrayList<String>();
            fieldMap.put(name, fieldValues);
          }
          fieldValues.add(field.stringValue());
        }
        mapping.put(record, fieldMap);
      }
    };
    lqa.execute(context, query);
    startRecord += query.getFilter().getMaxRecords();

    loadLuceneMeta(context, fields);

    OpenSearchProperties osProps = new OpenSearchProperties();
    osProps.setShortName(msgBroker.retrieveMessage("catalog.openSearch.shortName"));
    osProps.setNumberOfHits(query.getResult().getNumberOfHits());
    osProps.setStartRecord(query.getFilter().getStartRecord());
    osProps.setRecordsPerPage(query.getFilter().getMaxRecords());

    ResourceIdentifier resourceIdentifier = ResourceIdentifier.newIdentifier(context);
    DiscoveredRecordsAdapter discoveredRecordsAdapter =
            new DiscoveredRecordsAdapter(resourceIdentifier, osProps, fields, query.getResult().getRecords(), mapping);

    loadCatalog(context, discoveredRecordsAdapter);

    FeedLinkBuilder linkBuilder = new FeedLinkBuilder(context, RequestContext.resolveBaseContextPath(request), msgBroker);
    for (IFeedRecord record : discoveredRecordsAdapter) {
      linkBuilder.build(record);
    }

    return discoveredRecordsAdapter;
  }

  /**
   * Loads Lucene index metadata.
   * @param context request context
   * @param fields list of fields
   * @throws CatalogIndexException if accessing index fails
   */
  protected void loadLuceneMeta(RequestContext context, List<IFeedRecords.FieldMeta> fields) throws CatalogIndexException {
    if (!isLuceneMetaAllowed()) {
      return;
    }

    LuceneIndexAdapter indexAdapter = new LuceneIndexAdapter(context);
    IndexSearcher searcher = null;
    try {
      searcher = indexAdapter.newSearcher();
      IndexReader indexReader = searcher.getIndexReader();
      for (String fieldName : indexReader.getFieldNames(IndexReader.FieldOption.ALL)) {
        fields.add(new IFeedRecords.FieldMeta(IFeedRecord.STD_COLLECTION_INDEX + "." + fieldName, "esriFieldTypeString", fieldName));
      }
    } catch (Exception e) {
      String sMsg = "Error accessing index:\n " + Val.chkStr(e.getMessage());
      throw new CatalogIndexException(sMsg, e);
    } finally {
      indexAdapter.closeSearcher(searcher);
    }
  }

  /**
   * Loads catalog meta.
   * @param context request context
   * @param records records
   * @throws SQLException if accessing catalog database fails
   */
  protected void loadCatalog(RequestContext context, IFeedRecords records) throws SQLException {
    if (!isCatalogMetaAllowed()) {
      return;
    }

    ConnectionBroker connectionBroker = context.getConnectionBroker();
    ManagedConnection managedConnection = connectionBroker.returnConnection("");
    Connection conn = managedConnection.getJdbcConnection();

    boolean firstRecord = true;

    for (IFeedRecord record : records) {
      PreparedStatement st = null;
      ResultSet rs = null;
      try {
        st = conn.prepareStatement("SELECT * FROM GPT_RESOURCE WHERE DOCUUID=?");
        st.setString(1, record.getUuid());

        rs = st.executeQuery();
        if (rs.next()) {
          ResultSetMetaData metaData = rs.getMetaData();
          for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String name = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            IFeedAttribute attribute = IFeedAttribute.Factory.create(value, metaData.getColumnDisplaySize(i));
            if (firstRecord) {
              records.getMetaData().add(new IFeedRecords.FieldMeta(IFeedRecord.STD_COLLECTION_CATALOG + "." + name, attribute.getEsriType(), name, attribute.getLength()));
            }
            record.getData(IFeedRecord.STD_COLLECTION_CATALOG).put(name, attribute);
          }
        }
        firstRecord = false;
      } finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException e) {
          }
        }
        if (st != null) {
          try {
            st.close();
          } catch (SQLException e) {
          }
        }
      }
    }
  }

  /**
   * Gets gpt.xml configuration parameter.
   * @param paramName parameter name
   * @return parameter value
   */
  protected static String getConfigParam(String paramName) {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    return Val.chkStr(appCfg.getCatalogConfiguration().getParameters().getValue(paramName));
  }

  /**
   * Checks if Lucene data is allowed.
   * Reads "json.meta.lucene" configuration parameter.
   * @return <code>true</code> if data is allowed
   */
  protected boolean isLuceneMetaAllowed() {
    String sMeta = getConfigParam("json.meta.lucene");
    return Val.chkBool(sMeta, true);
  }

  /**
   * Checks if catalog data is allowed.
   * Reads "json.meta.catalog" configuration parameter.
   * @return <code>true</code> if data is allowed
   */
  protected boolean isCatalogMetaAllowed() {
    String sMeta = getConfigParam("json.meta.catalog");
    return Val.chkBool(sMeta, true);
  }
}
