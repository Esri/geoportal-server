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
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

/**
 * A filter that forces the query to restrict to one or more item collections.
 */
@SuppressWarnings("serial")
public class IsPartOfFilter extends Filter {

  /** instance variables ====================================================== */
  private Filter   chained;
  private String   fieldName;
  private long     timeMillis = 0;
  private String[] values;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated field name, value array and a chained filter.
   * @param fieldName the field name
   * @param values set of values
   * @param chained a chained filter
   */
  public IsPartOfFilter(String fieldName, String[] values, Filter chained) {
    this.fieldName = fieldName;
    this.values = values;
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
    OpenBitSet bitSet = this.queryValues(reader,this.fieldName,this.values);
    if (this.chained != null) {
      bitSet.and((OpenBitSet)this.chained.getDocIdSet(reader));
    }
    setTimeMillis(System.currentTimeMillis() - t1);
    return bitSet;
  }
  
  /**
   * Makes the filter based upon the application configuration and active 
   * request context.
   * @param context the request filter
   * @param chained a chained filter
   * @return the filter (null if not applicable)
   */
  public static Filter make(RequestContext context, Filter chained) {
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    String s = Val.chkStr(params.getValue("lucene.force.isPartOf"));
    if (s.length() > 0) {
      if (s.indexOf(",") == -1) {
        String[] values = new String[]{s};
        return new IsPartOfFilter("isPartOf",values,chained);
      } else {
        String[] tokens = s.split(",");
        ArrayList<String> al = new ArrayList<String>();
        for (String token: tokens) {
          String s2 = Val.chkStr(token);
          if (s2.length() > 0) al.add(s2);
        }
        if (al.size() > 0) {
          String[] values = al.toArray(new String[0]);
          return new IsPartOfFilter("isPartOf",values,chained);
        }
      }
    }
    return chained;
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
  
