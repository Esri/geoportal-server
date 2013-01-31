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
package com.esri.gpt.server.assertion.index;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnAssertionSet;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnValueType;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * Handles read/write operations against a Lucene index of assertions.
 */
public class AsnIndexAdapter extends AsnBaseIndexAdapter {
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnIndexAdapter() {
    super();
  }
  
  /** methods ================================================================= */
    
  /**
   * Returns a hit count for a subject, predicate and value combination.
   * <br/>This will only work correctly for values that have not been analyzed 
   * prior to indexing.
   * @param context the assertion operation context
   * @param searcher the index searcher
   * @param valueField the value field name
   * @param subject the subject
   * @param predicate the predicate
   * @param value the value
   * @return the cardinality
   * @throws CorruptIndexException if the index is corrupt
   * @throws IOException if an I/O exception occurs
   */
  public long count(AsnContext context, 
                    IndexSearcher searcher,
                    String valueField,
                    String subject, 
                    String predicate, 
                    String value) 
    throws CorruptIndexException, IOException {
    
    // build a query to match the subject/predicate/value triple
    BooleanQuery query = new BooleanQuery();
    Query qSubject = new TermQuery(new Term(AsnConstants.FIELD_RDF_SUBJECT,subject));
    Query qPredicate = new TermQuery(new Term(AsnConstants.FIELD_RDF_PREDICATE,predicate));
    Query qValue = new TermQuery(new Term(valueField,value));
    query.add(qSubject,BooleanClause.Occur.MUST);
    query.add(qPredicate,BooleanClause.Occur.MUST);
    query.add(qValue,BooleanClause.Occur.MUST);
    
    // execute the search, return the hits
    TopDocs topDocs = searcher.search(query,1);
    return topDocs.totalHits;
  }
  
  /**
   * Deletes an assertion.
   * @param context the assertion operation context
   * @param assertionId the assertion ID to delete
   * @throws CorruptIndexException if the index is corrupt
   * @throws LockObtainFailedException if a write lock cannot be obtained
   * @throws IOException if an I/O exception occurs
   */
  public void delete(AsnContext context, String assertionId) 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = null;
    try {
      assertionId = Val.chkStr(assertionId);
      if (assertionId.length() > 0) {
        Term idTerm = new Term(AsnConstants.FIELD_SYS_ASSERTIONID,assertionId);
        writer = this.makeIndexWriter(this.newAnalyzer(context));
        writer.deleteDocuments(idTerm);
      }
    } finally {
      this.closeWriter(writer);
    }
  }
  
  /**
   * Indexes an assertion.
   * @param context the assertion operation context
   * @param assertion the assertion to index
   * @throws CorruptIndexException if the index is corrupt
   * @throws LockObtainFailedException if a write lock cannot be obtained
   * @throws IOException if an I/O exception occurs
   */
  public void index(AsnContext context, Assertion assertion) 
    throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = null;
    try {
      Document document = assertion.makeWritableDocument(context);
      String assertionId = assertion.getSystemPart().getAssertionId();
      Term idTerm = new Term(AsnConstants.FIELD_SYS_ASSERTIONID,assertionId);
      writer = this.makeIndexWriter(this.newAnalyzer(context));
      writer.updateDocument(idTerm,document);
    } finally {
      this.closeWriter(writer);
    }
  }
  
  /**
   * Loads an assertion based upon the subject id of the active operation.
   * @param context the assertion operation context
   * @param mustExist <code>true</code> true if the assertion must exist
   * @return the assertion (null if not found)
   * @throws CorruptIndexException if the index is corrupt
   * @throws IOException if an I/O exception occurs
   * @throws AsnInvalidOperationException if mustExist and the assertion was not found
   */
  public Assertion loadAssertionById(AsnContext context, boolean mustExist) 
    throws CorruptIndexException, IOException, AsnInvalidOperationException {
    TermDocs termDocs = null;
    IndexReader reader = null;
    AsnOperation operation = context.getOperation();
    try {
      String assertionId = Val.chkStr(operation.getSubject().getValuePart());
      if (assertionId.length() > 0) {
        reader = this.makeIndexReader();
        termDocs = reader.termDocs();
        termDocs.seek(new Term(AsnConstants.FIELD_SYS_ASSERTIONID,assertionId));
        if (termDocs.next()) {
          Document document = reader.document(termDocs.doc());
          Assertion assertion = operation.getAssertionSet().newAssertion(context,false);
          assertion.load(document);
          return assertion;
        }
      }
    } finally {
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      this.closeReader(reader);
    }
    if (mustExist) {
      String msg = "This assertion id was not found - "+operation.getSubject().getURN();
      throw new AsnInvalidOperationException(msg);
    }
    return null;
  }
  
  /**
   * Loads the assertion previously cast for the active subject, predicate and user.
   * @param context the assertion operation context
   * @return the previously cast assertion (can be null)
   * @throws Exception if an exception occurs
   */
  public Assertion loadPreviousUserAssertion(AsnContext context) throws Exception {
    AsnOperation operation = context.getOperation();
    String userKey = Val.chkStr(operation.getUserPart().getKey());
    String username = Val.chkStr(operation.getUserPart().getName());
    boolean isAnonymous = username.equalsIgnoreCase(AsnConstants.ANONYMOUS_USERNAME);
    if (!isAnonymous && (userKey.length() > 0)) {        
      IndexReader reader = null;
      IndexSearcher searcher = null;
      try {
        reader = this.makeIndexReader();
        searcher = new IndexSearcher(reader); 
        this.loadPreviousUserAssertion(context,searcher);
      } finally {
        this.closeReader(reader);
        this.closeSearcher(searcher);
      } 
    }
    return null;
  }
  
  /**
   * Loads the assertion previously cast for the active subject, predicate and user.
   * @param context the assertion operation context
   * @param searcher the index searcher
   * @return the previously cast assertion (can be null)
   * @throws Exception if an exception occurs
   */
  public Assertion loadPreviousUserAssertion(AsnContext context, IndexSearcher searcher) 
    throws Exception {
    AsnOperation operation = context.getOperation();
    String userKey = Val.chkStr(operation.getUserPart().getKey());
    String username = Val.chkStr(operation.getUserPart().getName());
    boolean isAnonymous = username.equalsIgnoreCase(AsnConstants.ANONYMOUS_USERNAME);
    if (!isAnonymous && (userKey.length() > 0)) {  
      AsnAssertionSet asnSet = operation.getAssertionSet();
      AsnValueType vType = asnSet.getValueType();
      String subject = operation.getSubject().getURN();
      String predicate = vType.getRdfPredicate();
      
      // build a query to match the subject/predicate/user triple
      BooleanQuery query = new BooleanQuery();
      Query qSubject = new TermQuery(new Term(AsnConstants.FIELD_RDF_SUBJECT,subject));
      Query qPredicate = new TermQuery(new Term(AsnConstants.FIELD_RDF_PREDICATE,predicate));
      Query qUserKey = new TermQuery(new Term(AsnConstants.FIELD_USER_KEY,userKey));
      query.add(qSubject,BooleanClause.Occur.MUST);
      query.add(qPredicate,BooleanClause.Occur.MUST);
      query.add(qUserKey,BooleanClause.Occur.MUST);
        
      // make the reader and searcher, execute the search, return the previous assertion
      TopDocs topDocs = searcher.search(query,1);
      ScoreDoc[] scoreDocs = topDocs.scoreDocs;
      if ((scoreDocs != null) && (scoreDocs.length) > 0) {
        Document document = searcher.getIndexReader().document(scoreDocs[0].doc);
        Assertion assertion = asnSet.newAssertion(context,false);
        assertion.load(document);
        return assertion;
      }
    }
    return null;
  }
    
}
