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
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * A filter associated with a field and value.
 */
@SuppressWarnings("serial")
public class AsnFilter extends Filter {
  
  /** class variables ========================================================= */
  
  /** Chained and operand = "and" */
  public static final String CHAINED_OPERAND_AND = "and";
  
  /** Chained and not operand = "andnot" */
  public static final String CHAINED_OPERAND_ANDNOT = "andnot";
  
  /** Chained or operand = "or" */
  public static final String CHAINED_OPERAND_OR = "or";

  /** instance variables ====================================================== */
  private AsnFilter[] chain;
  private String      chainedOperand;
  private String      fieldName;
  private long        timeMillis = 0;
  private String      value;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated field and value.
   * @param fieldName the field name
   * @param value the field name
   */
  public AsnFilter(String fieldName,String value) {
    this.fieldName = fieldName;
    this.value = value;
  }
  
  /**
   * Constructs with an associated field and value.
   * @param chain chain
   * @param chainedOperand operand
   */
  public AsnFilter(AsnFilter[] chain, String chainedOperand) {
    this.chain = chain;
    this.chainedOperand = chainedOperand;
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
   * @return the DocIdSet the matching documents
   * @throws IOException if an exception is encountered while reading the index
   */
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    long t1 = System.currentTimeMillis();
    OpenBitSet bitSet = null;
    if (this.chain == null) {
      bitSet = this.queryValue(reader);
    } else {
      String operand = Val.chkStr(this.chainedOperand);
      bitSet = new OpenBitSet(reader.maxDoc());
      for (int i=0; i<this.chain.length; i++) {
        AsnFilter asnFilter = this.chain[i];
        if (i == 0) {
          bitSet = (OpenBitSet)asnFilter.getDocIdSet(reader);
        } else {
          OpenBitSet subBitSet = (OpenBitSet)asnFilter.getDocIdSet(reader);
          if (operand.equalsIgnoreCase(AsnFilter.CHAINED_OPERAND_AND)) {
            if (bitSet.cardinality() > 0) {
              bitSet.and(subBitSet);
            }
          } else if (operand.equalsIgnoreCase(AsnFilter.CHAINED_OPERAND_OR)) {
            bitSet.or(subBitSet);            
          } else if (operand.equalsIgnoreCase(AsnFilter.CHAINED_OPERAND_ANDNOT)) {
            if (bitSet.cardinality() > 0) {
              bitSet.andNot(subBitSet);
            }
          } else {
            if (bitSet.cardinality() > 0) {
              bitSet.and(subBitSet);
            }
          }
        }
      }
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
  private OpenBitSet queryValue(IndexReader reader) throws IOException {
    OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
    if ((this.value != null) && (this.value.length() > 0)) {
      TermDocs termDocs = null;
      try {      
        Term term = new Term(this.fieldName,this.value);
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
  
