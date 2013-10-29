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
import com.esri.gpt.catalog.discovery.DiscoveredRecords;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.lucene.LuceneQueryAdapter;
import com.esri.gpt.catalog.search.OpenSearchProperties;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.util.AbstractList;

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
  public IFeedRecords doSearch(HttpServletRequest request, HttpServletResponse response, RequestContext context, RestQuery query) throws Exception {
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

    FeedLinkBuilder linkBuilder = new FeedLinkBuilder(context, RequestContext.resolveBaseContextPath(request), msgBroker);
    for (IFeedRecord record : discoveredRecordsAdapter) {
      linkBuilder.build(record);
    }

    return discoveredRecordsAdapter;
    /*
    MessageBroker msgBroker = new FacesContextBroker(request, response).extractMessageBroker();
    FeedLinkBuilder linkBuilder = new FeedLinkBuilder(RequestContext.resolveBaseContextPath(request), msgBroker);
    
    DcatRecordsAdapter discoveredRecordsAdapter = new DcatRecordsAdapter(msgBroker, linkBuilder, context, query);

    return discoveredRecordsAdapter;
    */
  }
    
  /**
   * DCAT records adapter.
   */
  public static class DcatRecordsAdapter extends AbstractList<IFeedRecord> implements IFeedRecords {
    private static List<FieldMeta> fieldMetaList = new ArrayList<FieldMeta>();
    
    private MessageBroker msgBroker;
    private FeedLinkBuilder linkBuilder;
    private RequestContext context;
    private RestQuery query;
    
    /* page buffer */
    private ArrayList<IFeedRecord> feedRecords = new ArrayList<IFeedRecord>();
    private boolean initialized;

    /**
     * Creates instance of the adapter.
     * @param msgBroker message broker
     * @param linkBuilder link builder
     * @param context request context
     * @param query query
     */
    public DcatRecordsAdapter(MessageBroker msgBroker, FeedLinkBuilder linkBuilder, RequestContext context, RestQuery query) {
      this.msgBroker = msgBroker;
      this.linkBuilder = linkBuilder;
      this.context = context;
      this.query = query;
    }

    @Override
    public IFeedRecord get(int index) {
      try {
        // assure initialization
        initialize();
        
        // get currently loaded page number
        int currentPage = getCurrentPage();
        // get page for the requested record
        int pageNumber = getPageForRecord(index);
        
        // if current page is different than requested, load a correct page
        if (currentPage!=pageNumber) {
          loadPage(pageNumber);
        }
        
        // calculate offset of the record within the current page
        int offset = index - (query.getFilter().getStartRecord()-1);
        
        // get record
        IFeedRecord record = feedRecords.get(offset);
        
        return record;
      } catch (DiscoveryException ex) {
        throw new IndexOutOfBoundsException(ex.getMessage());
      }
    }

    @Override
    public int size() {
      try {
        initialize();
        return query.getResult().getNumberOfHits();
      } catch (DiscoveryException ex) {
        return 0;
      }
    }

    /**
     * First time initialization.
     * @throws DiscoveryException if initialization fails 
     */
    private void initialize() throws DiscoveryException {
      if (!initialized) {
        loadPage(0);
        initialized = true;
      }
    }
    
    /**
     * Loads page of information.
     * @param pageNumber page number
     * @throws DiscoveryException if loading page fails
     */
    private void loadPage(int pageNumber) throws DiscoveryException {
      // clear buffers
      feedRecords.clear();
      query.getResult().getRecords().clear();

      // prepare and execute query
      int startRecord = getStartRecordOfPage(pageNumber);
      query.getFilter().setStartRecord(startRecord);
      LuceneQueryAdapterImpl lqa = new LuceneQueryAdapterImpl();
      lqa.execute(context, query);
      ResourceIdentifier resourceIdentifier = ResourceIdentifier.newIdentifier(context);
      
      // traverse through discovered records and build IFeedRecord for each of them (as DiscoveredRecordAdapter)
      DiscoveredRecords records = query.getResult().getRecords();
      for (DiscoveredRecord dr: records) {
        DiscoveredRecordAdapter record = new DiscoveredRecordAdapter(resourceIdentifier,dr);
        Map<String, IFeedAttribute> attrs = new HashMap<String, IFeedAttribute>();
        Map<String,List<String>> data = lqa.getMapping().get(dr);
        if (data!=null) {
          for (Map.Entry<String,List<String>> e: data.entrySet()) {
            List<IFeedAttribute> l = new ArrayList<IFeedAttribute>();
            for (String s: e.getValue()) {
              l.add(IFeedAttribute.Factory.create(s,256));
            }
            attrs.put(e.getKey(), IFeedAttribute.Factory.create(l));
          }
          record.getData(IFeedRecord.STD_COLLECTION_INDEX).putAll(attrs);
        }
        linkBuilder.build(record);
        feedRecords.add(record);
      }
    }
    
    /**
     * Gets first record of a page.
     * @param pageNumber page number (0-based index)
     * @return number of the first record of the page (1-based index)
     */
    private int getStartRecordOfPage(int pageNumber) {
      int maxRecords = query.getFilter().getMaxRecords();
      return pageNumber*maxRecords + 1;
    }
    
    /**
     * Gets current page.
     * @return current page (0-based index)
     */
    private int getCurrentPage() {
      int startRecord = query.getFilter().getStartRecord();
      return getPageForRecord(startRecord-1);
    }
    
    /**
     * Gets page for the record.
     * @param recordNumber record number (1-based index)
     * @return page number (0-based index)
     */
    private int getPageForRecord(int recordNumber) {
      int maxRecords = query.getFilter().getMaxRecords();
      return recordNumber/maxRecords;
    }
    
    /**
     * Gets number of pages.
     * @return number of pages
     */
    private int getPagesCount() {
      int numberOfHits = query.getResult().getNumberOfHits();
      int maxRecords = query.getFilter().getMaxRecords();
      return numberOfHits>0? numberOfHits/maxRecords+1: 0;
    }

    @Override
    public OpenSearchProperties getOpenSearchProperties() {
      OpenSearchProperties osProps = new OpenSearchProperties();
      osProps.setShortName(msgBroker.retrieveMessage("catalog.openSearch.shortName"));
      osProps.setNumberOfHits(query.getResult().getNumberOfHits());
      osProps.setStartRecord(query.getFilter().getStartRecord());
      osProps.setRecordsPerPage(query.getResult().getNumberOfHits());
      return osProps;
    }

    @Override
    public List<FieldMeta> getMetaData() {
      return fieldMetaList;
    }
  }

  /**
   * Specific Lucene query adapter.
   */
  private static class LuceneQueryAdapterImpl extends LuceneQueryAdapter {
    private Map<DiscoveredRecord, Map<String, List<String>>> mapping = new HashMap<DiscoveredRecord, Map<String, List<String>>>();
    
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

    /**
     * Gets mapping.
     * @return mapping
     */
    public Map<DiscoveredRecord, Map<String, List<String>>> getMapping() {
      return mapping;
    }
    
  }
}
