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
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * A filter that limits the response to a specific schema name.
 * <p/>e.g. http://www.isotc211.org/2005/gmd
 */
@SuppressWarnings("serial")
public class SchemaFilter extends Filter {

  /** instance variables ====================================================== */
  private Filter chained;
  private String fieldName;
  private long   timeMillis = 0;
  private String value;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated schema name.
   * @param schemaName the schema name
   * @param chained a chained filter
   */
  public SchemaFilter(String schemaName, Filter chained) {
    this.fieldName = Storeables.FIELD_SCHEMA;
    this.value = schemaName;
    this.chained = chained;
  }
  
  /** properties ============================================================== */
  
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
  
  /** methods ================================================================= */
  
  /**
   * Applies the filter and returns a DocIdSet of matching documents.
   * @param reader the index reader
   * @return the DocIdSet (documents that are visible to the supplied user principals)
   * @throws IOException if an exception is encountered while reading the index
   */
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    long t1 = System.currentTimeMillis();
    OpenBitSet bitSet = this.queryValue(reader,this.fieldName,this.value);
    if (this.chained != null) {
      bitSet.and((OpenBitSet)this.chained.getDocIdSet(reader));
    }
    setTimeMillis(System.currentTimeMillis() - t1);
    return bitSet;
  }
  
  /**
   * Queries for documents that match the supplied value.
   * @param reader the index reader
   * @return the OpenBitSet (documents with matches are set to true)
   * @throws IOException if an exception is encountered while reading the index
   */
  private OpenBitSet queryValue(IndexReader reader, String field, String value) throws IOException {
    OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
    if ((value != null) && (value.length() > 0)) {
      TermDocs termDocs = null;
      try {      
        Term term = new Term(field,value);
        termDocs = reader.termDocs();
        termDocs.seek(term);
        while (termDocs.next()) {
          bitSet.set(termDocs.doc());
        }
      } finally {
        try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      }
    }
    return bitSet;
  }

}
  
