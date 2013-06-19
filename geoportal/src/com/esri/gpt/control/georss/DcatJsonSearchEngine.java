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
package com.esri.gpt.control.georss;

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

import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.lucene.LuceneQueryAdapter;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * Performs search to generate search results for dcat response.
 */
public abstract class DcatJsonSearchEngine extends JsonSearchEngine {
	/**
   * Logger.
   */
  private static final Logger LOG = Logger.getLogger(DcatJsonSearchEngine.class.getCanonicalName());
  
  /**
   * Default search engine.
   */
  private static final DcatJsonSearchEngine defaultEngine = new DcatJsonSearchEngine() {
    @Override
    public IFeedRecords search(HttpServletRequest request, HttpServletResponse response, RequestContext context, RestQuery query) throws Exception {
    	if(query.getFilter().getMaxRecords() > 10000){
    		query.getFilter().setMaxRecords(10000);
    	}
    	return doSearch(request, response, context, query);
    }
  };

	/**
   * Creates instance of the search engine. 
   * @return instance of {@link JsonSearchEngine}
   */
  public static DcatJsonSearchEngine createInstance() {
    String className = getConfigParam("dcat.json.engine.className");
    if (className.isEmpty()) {
      return defaultEngine;
    } else {
      try {
        Class engineClass = Class.forName(className);
        return (DcatJsonSearchEngine) engineClass.newInstance();
      } catch (Exception ex) {
        LOG.log(Level.INFO, "Error creating JSON search engine: " + className +". Using default JSON search engine instead.", ex);
        return defaultEngine;
      }
    }
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
	@Override
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

    DiscoveredRecordsAdapter discoveredRecordsAdapter =
            new DiscoveredRecordsAdapter(osProps, fields, query.getResult().getRecords(), mapping);

    FeedLinkBuilder linkBuilder = new FeedLinkBuilder(RequestContext.resolveBaseContextPath(request), msgBroker);
    for (IFeedRecord record : discoveredRecordsAdapter) {
      linkBuilder.build(record);
    }

    return discoveredRecordsAdapter;
  }

}
