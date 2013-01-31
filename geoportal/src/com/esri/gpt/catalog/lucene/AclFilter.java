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
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * A filter that provides access control at a document level.
 */
public class AclFilter extends Filter {

  /** instance variables ====================================================== */
  private String   fieldName;
  private long     timeMillis = 0;
  private String[] userPrincipals;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a field name and a list of principal strings associated
   * with the current user.
   * @param fieldName - the ACL field name
   * @param userPrincipals the user principal strings
   */
  public AclFilter(String fieldName, String[] userPrincipals) {
    this.fieldName = fieldName;
    this.userPrincipals = userPrincipals;
  }
  
  /** properties ============================================================== */
  
  /** 
   * Gets the ACL field name.
   * @return the field name
   */
  public String getFieldName() {
    return this.fieldName;
  }
  
  /** 
   * Gets the execution time.
   * @return the execution time (in milliseconds)
   */
  public long getTimeMillis() {
    return this.timeMillis;
  }
  /** 
   * Sets the execution time.
   * @param millis the execution time (in milliseconds)
   */
  protected void setTimeMillis(long millis) {
    this.timeMillis = millis;
  }
  
  /** 
   * Gets the array of ACL principals associated with the active user.
   * @return the array of principals
   */
  public String[] getUserPrincipals() {
    return this.userPrincipals;
  }
  
  /** methods ================================================================= */
  
  /**
   * Applies the filter and returns a DocIdSet of matching documents.
   * @param reader the index reader
   * @return the DocIdSet (documents that are visible to the supplied user principals)
   * @throws IOException if an exception is encountered while reading the index
   */
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    long t1 = System.currentTimeMillis();
    OpenBitSet bitSet = this.queryValues(reader,getFieldName(),this.getUserPrincipals());
    OpenBitSet publicBitSet = queryNulls(reader,getFieldName());
    bitSet.or(publicBitSet);
    setTimeMillis(System.currentTimeMillis() - t1);
    return bitSet;
  }
  
  /**
   * Queries for documents that have no values associated with the field.
   * @param reader the index reader
   * @return the OpenBitSet (documents with no values set to true)
   * @throws IOException if an exception is encountered while reading the index
   */
  private OpenBitSet queryNulls(IndexReader reader, String field) throws IOException {
    int nBits = reader.maxDoc();
    OpenBitSet bitSet = new OpenBitSet(nBits);
    TermEnum termEnum = null;
    TermDocs termDocs = null;
    if ((field != null) && (field.trim().length() > 0)) {
      try {
        
        // find all documents that have a term for the field, then flip the bit set
        termEnum = reader.terms(new Term(field));
        termDocs = reader.termDocs();
        do {
          Term term = termEnum.term();
          if ((term != null) && term.field().equals(field)) {
            termDocs.seek(term);
            while (termDocs.next()) {
              bitSet.fastSet(termDocs.doc());
            }
          }
        } while (termEnum.next());
                
        bitSet.flip(0,nBits);
        if (reader.hasDeletions()) {
          for (int i=0;i<nBits;i++) {
            if (bitSet.get(i) && reader.isDeleted(i)) {
              bitSet.fastFlip(i);
            }
          }
        }
        
      } finally {
        try {if (termEnum != null) termEnum.close();} catch (Exception ef) {}
        try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      }
    }
    return bitSet;
  }
  
  /**
   * Queries for documents that match one or more of the supplied values.
   * @param reader the index reader
   * @return the OpenBitSet (documents with matches are set to true)
   * @throws IOException if an exception is encountered while reading the index
   */
  private OpenBitSet queryValues(IndexReader reader, String field, String[] values) throws IOException {
    OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
    if ((values != null) && (values.length > 0)) {
      TermDocs termDocs = null;
      try {      
        Term baseTerm = new Term(field);
        termDocs = reader.termDocs();
        for (String value: values) {
          termDocs.seek(baseTerm.createTerm(value.trim().toLowerCase()));
          while (termDocs.next()) {
            bitSet.set(termDocs.doc());
          }
        }
      } finally {
        try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      }
    }
    return bitSet;
  }

}
  
