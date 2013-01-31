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
package com.esri.gpt.server.assertion.handler;
import com.esri.gpt.server.assertion.components.AsnAssertionRenderer;
import com.esri.gpt.server.assertion.components.AsnAssertionSet;
import com.esri.gpt.server.assertion.components.AsnAuthorizer;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnProperty;
import com.esri.gpt.server.assertion.components.AsnValueType;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.assertion.index.Assertion;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 * Handles assertions associated with comment queries.
 */
public class AsnCommentHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnCommentHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handles an assertion operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
        
    // initialize
    AsnOperation operation = context.getOperation();
    AsnAuthorizer authorizer = context.getAuthorizer();
    String subjectPfx = operation.getSubject().getURNPrefix();
    String predicate = operation.getPredicate().getURN();
    
    // query comments
    if (subjectPfx.endsWith(":assertionid") && 
        predicate.equals("urn:esri:geoportal:comment:query")) {
      Assertion assertion = this.getIndexAdapter().loadAssertionById(context,true);
      authorizer.authorizeQuery(context);
      AsnAssertionRenderer renderer = new AsnAssertionRenderer();
      AsnProperty prop = renderer.makeProperty(context,assertion);
      context.getOperationResponse().generateResponse(context,prop);
        
    } else if (subjectPfx.endsWith(":resourceid") && 
        predicate.equals("urn:esri:geoportal:comment:query")) {   
      authorizer.authorizeQuery(context);
      this.query(context);
        
    } else {
      throw new AsnInvalidOperationException();
    }
  }
  
  /**
   * Queries comments.
   * @param context the assertion operation context
   * @throws Exception if an exception occurs
   */
  private void query(AsnContext context) throws Exception { 
    
    // initialize
    AsnOperation operation = context.getOperation();
    AsnAssertionSet asnSet = operation.getAssertionSet();
    AsnValueType vType = asnSet.getValueType();
    String subject = operation.getSubject().getURN();
    String predicate = vType.getRdfPredicate();    
    
    // build a query to match all occurrences of the subject/predicate pair
    BooleanQuery query = new BooleanQuery();
    Query qSubject = new TermQuery(new Term(AsnConstants.FIELD_RDF_SUBJECT,subject));
    Query qPredicate = new TermQuery(new Term(AsnConstants.FIELD_RDF_PREDICATE,predicate));
    query.add(qSubject,BooleanClause.Occur.MUST);
    query.add(qPredicate,BooleanClause.Occur.MUST);
    
    // sort on descending timestamp
    String tsField = AsnConstants.FIELD_SYS_TIMESTAMP;
    Sort sortOption = new Sort(new SortField(tsField,SortField.STRING,true));
    
    // determine the start and end positions
    int startRecord = context.getRequestOptions().getStartRecord() - 1;
    int maxRecords = context.getRequestOptions().getMaxRecords();
    if (startRecord < 0) startRecord = 0;
    int recordsPerPage = maxRecords;
    if (recordsPerPage <= 0) recordsPerPage = 1;
    int hitsToReturn = startRecord + recordsPerPage;
    int nextRecord = 0;
    int numDocs = 0;
    
    IndexReader reader = null;
    IndexSearcher searcher = null;
    try {
      
      // make the reader and searcher, execute the search
      reader = this.getIndexAdapter().makeIndexReader();
      searcher = new IndexSearcher(reader);
      TopDocs topDocs = searcher.search(query,null,hitsToReturn,sortOption);
      ScoreDoc[] scoreDocs = null;
      int totalHits = topDocs.totalHits;
      if (maxRecords > 0) {
        scoreDocs = topDocs.scoreDocs;
        if ((scoreDocs != null) && (scoreDocs.length) > 0) {
          numDocs = scoreDocs.length;
          if (totalHits > numDocs) {
            nextRecord = numDocs + 1;
          }
        }
      }
      
      // root property for the response
      String rootSubject = subject;
      String roorPredicate = operation.getPredicate().getURN()+"response";
      AsnProperty rootProp = new AsnProperty(rootSubject,roorPredicate,null);
      
      // hit count and next record
      String queryPfx = asnSet.getURNPrefix()+":query";
      rootProp.getChildren().add(new AsnProperty(null,queryPfx+":hits",""+totalHits));
      if (nextRecord > 0) {
        rootProp.getChildren().add(new AsnProperty(null,queryPfx+":nextRecord",""+nextRecord));
      }

      // canCreate capability for the active user
      String canCreatePred = asnSet.getURNPrefix()+":activeUser:canCreate";
      String canCreateVal = ""+context.getAuthorizer().canCreate(context,asnSet.getAuthPolicy());
      rootProp.getChildren().add(new AsnProperty(null,canCreatePred,canCreateVal));
      
      // process the documents, generate the response
      AsnAssertionRenderer renderer = new AsnAssertionRenderer();
      for (int i=startRecord; i<numDocs; i++) {
        Document document = reader.document(scoreDocs[i].doc);
        Assertion assertion = asnSet.newAssertion(context,false);
        assertion.load(document);
        rootProp.getChildren().add(renderer.makeProperty(context,assertion));
      }
      context.getOperationResponse().generateResponse(context,rootProp.getChildren());
      
    } finally {
      this.getIndexAdapter().closeReader(reader);
      this.getIndexAdapter().closeSearcher(searcher);
    } 
    
  }
}
