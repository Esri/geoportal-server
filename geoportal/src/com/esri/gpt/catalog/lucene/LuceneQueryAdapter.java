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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.catalog.discovery.Discoverables;
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.DiscoveredRecords;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.discovery.DiscoveryFilter;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.DiscoveryQueryAdapter;
import com.esri.gpt.catalog.discovery.DiscoveryResult;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyMeaningType;
import com.esri.gpt.catalog.discovery.Sortable;
import com.esri.gpt.catalog.discovery.Sortables;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.QueryOptions;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

/**
 * Adapts a catalog discovery query to the Lucene model.
 */
public class LuceneQueryAdapter extends DiscoveryQueryAdapter {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(LuceneQueryAdapter.class.getName());
  
  /** If the request filter is empty, we'll query all records if the
   *  request max record number is <= this threshold.  
   */
  private static int QUERYALL_THRESHOLD = 10000;
  
  /** Log a warning if the number of records to process is >= this value. */
  private static int TOOMANY_WARNING_THRESHOLD = 1000;

  /** instance variables ====================================================== */
  private boolean            hasScoredExpression = false;
  private LuceneIndexAdapter indexAdpter;
  private int                maxDoc = -1;

  /** constructors ============================================================ */

  /** Default constructor. */
  public LuceneQueryAdapter() {}

  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not a scored expression exists within the query.
   * <p/>This flag supports relevance sorting.
   * @return true if a scored expression exists
   */
  protected boolean getHasScoredExpression() {
    return hasScoredExpression;
  }
  /**
   * Sets the flag indicating whether or not a scored expression exists within the query.
   * <p/>This flag supports relevance sorting.
   * @param hasScoredExpression true if a scored expression exists
   */
  protected void setHasScoredExpression(boolean hasScoredExpression) {
    this.hasScoredExpression = hasScoredExpression;
  }

  /**
   * Gets the index adapter.
   * @return the index adapter
   */
  protected LuceneIndexAdapter getIndexAdapter() {
    return this.indexAdpter;
  }
  
  /**
   * Gets the maxDoc() count returned by the Lucene IndexSearcher.
   * @return the index adapter
   */
  protected int getMaxDoc() {
    return this.maxDoc;
  }

  /** methods ================================================================= */
  
  /**
   * Executes a query for metadata documents.
   * @param context the active request context
   * @param discoveryQuery the query to execute
   * @throws DiscoveryException if an exception occurs
   */
  public void execute(RequestContext context,
                      DiscoveryQuery discoveryQuery) 
    throws DiscoveryException {
    LOGGER.finer("Executing DiscoveryQuery...");
    this.indexAdpter = new LuceneIndexAdapter(context);
 
    try {
      executeQuery(discoveryQuery);
    } catch (ParseException e) {
      String sMsg = "Error querying documents:\n "+Val.chkStr(e.getMessage());
      throw new DiscoveryException(sMsg,e);
    } catch (CorruptIndexException e) {
      String sMsg = "Error querying documents:\n "+Val.chkStr(e.getMessage());
      throw new DiscoveryException(sMsg,e);
    } catch (IOException e) {
      String sMsg = "Error querying documents:\n "+Val.chkStr(e.getMessage());
      throw new DiscoveryException(sMsg,e);
    }
  }
  
  /**
   * Executes a query against a Lucene index.
   * @param discoveryQuery the query to execute
   */
  protected void executeQuery(DiscoveryQuery discoveryQuery) 
    throws DiscoveryException, ParseException, CorruptIndexException, IOException {
   
    IndexSearcher searcher = null;
    try {
      
      // initialize
      searcher = getIndexAdapter().newSearcher();
      this.maxDoc = searcher.maxDoc();
      boolean bExecuteQuery = true;
      boolean bProcessHits = true;
      RequestContext reqContext = this.getIndexAdapter().getRequestContext();
      BooleanQuery rootQuery = new BooleanQuery();
      DiscoveryFilter discoveryFilter = discoveryQuery.getFilter();
      DiscoveryResult discoveryResult = discoveryQuery.getResult();
      Discoverables returnables = discoveryQuery.getReturnables();
      if ((returnables == null) || (returnables.size() == 0) ||
          (discoveryFilter.getMaxRecords() <= 0)) {
        bProcessHits = false;
      }
      
      // CSW query provider options
      boolean isDublinCoreResponse = true;
      boolean isBriefResponse = false;
      boolean isSummaryResponse = false;
      QueryOptions cswQueryOptions = (QueryOptions)reqContext.getObjectMap().get(
          "com.esri.gpt.server.csw.provider.components.QueryOptions");
      
      // build the query (if no query was supplied, we'll query everything)
      LogicalClauseAdapter logicalAdapter = new LogicalClauseAdapter(this);
      LogicalClause rootClause = discoveryFilter.getRootClause();
      if ((rootClause == null) || (rootClause.getClauses().size() == 0)) {
        if (discoveryFilter.getMaxRecords() <= QUERYALL_THRESHOLD) {
          LOGGER.finer("No filter was supplied, querying all...");
          logicalAdapter.appendSelectAll(rootQuery);
        } else {
          LOGGER.finer("No filter was supplied, query will not be executed.");
          bExecuteQuery = false;
        }
      } else {
        logicalAdapter.adaptLogicalClause(rootQuery,rootClause);
        if ((rootQuery.clauses() == null) && (rootQuery.clauses().size() > 0)) {
          bExecuteQuery = false;
        }
      }
      if (!bExecuteQuery) return;
     
        
      // execute the query and process the hits if required
      
      // set the sort option
      Sort sortOption = null;
      if (bProcessHits && (searcher.maxDoc() > 0)) {
        sortOption = makeSortOption(discoveryQuery);
      }
      
      // filters
      Filter filter = null;
      
      // make the access control filter
      MetadataAcl acl = new MetadataAcl(reqContext);
      AuthenticationStatus auth = reqContext.getUser().getAuthenticationStatus();
      boolean bAdmin = auth.getAuthenticatedRoles().hasRole("gptAdministrator");
      if (!bAdmin && !acl.isPolicyUnrestricted()) {
        String[] aclValues = acl.makeUserAcl();
        filter = new AclFilter(Storeables.FIELD_ACL,aclValues);
      }
      
      // isPartOf filter
      filter = IsPartOfFilter.make(reqContext,filter);
      
      // make the schema filter
      if (cswQueryOptions != null) {
        String schemaName = Val.chkStr(cswQueryOptions.getSchemaFilter());
        if (schemaName.length() > 0) {
          filter = new SchemaFilter(schemaName,filter);
          isDublinCoreResponse = cswQueryOptions.isDublinCoreResponse();
          if (!isDublinCoreResponse) {
            String elementSetType = Val.chkStr(cswQueryOptions.getElementSetType());
            if (elementSetType.equalsIgnoreCase("brief")) {
              isBriefResponse = true;
            } else if (elementSetType.equalsIgnoreCase("summary")) {
              isSummaryResponse = true;
            }
          }
        }
      }
      
      
      // determine the start/end positions
      int startRecord = discoveryFilter.getStartRecord() - 1;
      int maxRecords = discoveryFilter.getMaxRecords();
      if (startRecord < 0) startRecord = 0;
      int recordsPerPage = maxRecords;
      if (recordsPerPage <= 0) recordsPerPage = 1;
      int hitsToReturn = startRecord + recordsPerPage;
      int nextRecord = 0;
      int numDocs = 0;
      
      // execute the query 
      LOGGER.finer("Executing Lucene Query:\n"+rootQuery);
      TopDocs topDocs = null;
      if (filter != null) {
        if (sortOption != null) {
          topDocs = searcher.search(rootQuery,filter,hitsToReturn,sortOption);
        } else {
          topDocs = searcher.search(rootQuery,filter,hitsToReturn);
        }
      } else {
        if (sortOption != null) {
          topDocs = searcher.search(rootQuery,filter,hitsToReturn,sortOption);
        } else {
          topDocs = searcher.search(rootQuery,hitsToReturn);
        }
      }
      
      // determine the hit count
      int totalHits = topDocs.totalHits;
      ScoreDoc[] scoreDocs = topDocs.scoreDocs;
      if ((scoreDocs != null) && (scoreDocs.length) > 0) {
        numDocs = scoreDocs.length;
        if (totalHits > numDocs) {
          nextRecord = numDocs + 1;
        }
      }
      discoveryResult.setNumberOfHits(totalHits);
      LOGGER.finer("Total query hits: "+totalHits);
      
      if (startRecord > (totalHits - 1)) bProcessHits = false;      
      if (maxRecords <= 0) bProcessHits = false;
      int nTotal = numDocs - startRecord;
      if (!bProcessHits) return;
        
      // warn if many records were requested
      if (nTotal >= TOOMANY_WARNING_THRESHOLD) {
        LOGGER.warning("A request to process "+nTotal+
            " discovery records was recieved and will be exceuted.\n"+discoveryQuery.toString());
      }
               
      // process the hits, build the results
      LOGGER.finer("Processing "+nTotal+" records from: "+(startRecord+1)+" to: "+numDocs);
      Storeable storeable;
      DiscoveredRecords records = discoveryResult.getRecords();
      IndexReader reader = searcher.getIndexReader();
      for (int i=startRecord; i<numDocs; i++) {
        ScoreDoc scoreDoc = scoreDocs[i];
        Document document = reader.document(scoreDoc.doc);
        DiscoveredRecord record = new DiscoveredRecord();
        
        // Dublin Core based responses
        if (isDublinCoreResponse) {
          for (Discoverable target: returnables) {
            ArrayList<Object> values = new ArrayList<Object>();
            storeable = (Storeable)target.getStorable();
            
            if (storeable instanceof AnyTextProperty) {
              values = null;
              
            } else if (storeable instanceof GeometryProperty) {
              GeometryProperty geom = (GeometryProperty)storeable;
              values.add(geom.readEnvelope(document));
              
            } else if (target.getMeaning().getMeaningType().equals(PropertyMeaningType.XMLURL)) {
              String uuid = document.get(Storeables.FIELD_UUID);
              uuid = URLEncoder.encode(uuid,"UTF-8");
              values.add("?getxml="+uuid);
              
            } else {           
              DatastoreField retrievalField = storeable.getRetrievalField();
              Field[] fields = document.getFields(retrievalField.getName());
              if (fields != null) {
                for (Field f: fields) {
                  Object value = retrievalField.makeValueToReturn(f.stringValue());
                  values.add(value);
                }
              }
            }
            
            if (values != null) {
              Object [] oValues = null;
              if (values.size() >= 0) oValues = values.toArray();
              record.addField(target,oValues);
            }
          }
        
        // non Dublin Core based responses
        } else {
          String responseXml = null;
          if (isBriefResponse && (responseXml == null)) {
            Field field = document.getField(Storeables.FIELD_XML_BRIEF);
            if (field != null) {
              responseXml = field.stringValue();
            }
          } else if (isSummaryResponse && (responseXml == null)) {
            Field field = document.getField(Storeables.FIELD_XML_SUMMARY);
            if (field != null) {
              responseXml = field.stringValue();
            }
          } else if (responseXml == null) {
            Field field = document.getField(Storeables.FIELD_XML);
            if (field != null) {
              responseXml = field.stringValue();
            }           
          }
          record.setResponseXml(responseXml);
        }
        onRecord(record, document);
        records.add(record);
      }
      int nPopulated = records.size();
      LOGGER.finer("Populated "+nPopulated+" records.");
      
    } finally {
      getIndexAdapter().closeSearcher(searcher);
    }
  }
  
  /**
   * Called before a record is being added to the collection
   * @param record discovered record
   * @param document Lucene document
   */
  protected void onRecord(DiscoveredRecord record, Document document) {
    // TODO: override to customize behavior
  }
  
  /**
   * Makes the sort option for the query.
   * @param discoveryQuery the active query
   */
  private Sort makeSortOption(DiscoveryQuery discoveryQuery) {
    Sort sortOption = null;
    Sortables sortables = discoveryQuery.getSortables();
    ArrayList<SortField> sortFields = new ArrayList<SortField>();
    if ((sortables != null) && (sortables.size() > 0)) {
      Storeable storable;
      for (Sortable sortable: sortables) {
        storable = (Storeable)sortable.getStorable();
        if (storable != null) {
          DatastoreField comparisonField = storable.getComparisonField();
          if (comparisonField != null) {
            int sortFieldType = comparisonField.sortFieldType();
            LOGGER.finer("Sorting on "+comparisonField.getName()+" "+sortable.getDirection());
            if (sortable.getDirection().equals(Sortable.SortDirection.ASC)) {
              sortFields.add(new SortField(comparisonField.getName(),sortFieldType,false));
            } else {
              sortFields.add(new SortField(comparisonField.getName(),sortFieldType,true));
            }
          }
        }
      }
    }
    
    // if sort fields have not been supplied and the query does not contain 
    // a scored expression then sort by descending date
    if (sortFields.size() == 0) {
      if (!this.getHasScoredExpression()) {
        String sModifiedDate = Storeables.FIELD_DATEMODIFIED;
        sortFields.add(new SortField(sModifiedDate,SortField.LONG,true));
        LOGGER.finer("Auto-sorting on "+sModifiedDate+" DESC");
      } else {
        LOGGER.finer("Sorting on relevance.");
      }
    }
    
    if (sortFields.size() > 0) {
      sortOption = new Sort(sortFields.toArray(new SortField[0]));
    }
    return sortOption;
  }

}
