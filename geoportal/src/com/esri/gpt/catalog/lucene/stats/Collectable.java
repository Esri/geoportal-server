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
package com.esri.gpt.catalog.lucene.stats;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.OpenBitSet;

/**
 * Super-class for a collectable set of statistics.
 */
abstract class Collectable {
  
  /** instance variables ====================================================== */
  private long    numberOfDocsConsidered = 0;
  private boolean sortByFequency = true;
  private long    timeMillis = 0;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public Collectable() {}
 
  /** properties  ============================================================= */
  
  /**
   * Gets the number of documents considered during stats collection.
   * @return the number of documents considered
   */
  public long getNumberOfDocsConsidered() {
    return this.numberOfDocsConsidered;
  }
  /**
   * Sets the number of documents considered during stats collection.
   * @param count the number of documents considered
   */
  protected void setNumberOfDocsConsidered(long count) {
    this.numberOfDocsConsidered = count;
  }
  
  /** 
   * Gets the flag indicating whether or not results should be sorted by frequency.
   * @return true if results should be sorted by frequency
   */
  protected boolean getSortByFrequency() {
    return this.sortByFequency;
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
  
  /** methods ================================================================= */
  
  /**
   * Executes the collection of statistics.
   * @param request the active statistics request
   * @param reader the index reader
   * @throws IOException if an error occurs while communicating with the index
   */
  public abstract void collectStats(StatsRequest request, IndexReader reader) throws IOException;
  
  /**
   * Determines the number of documents considered during stats collection.
   * <br/>If the document filter bitset is not null, the count will be based upon it's cardinality.
   * <br/>Otherwise the count will be based upon the number of docs returned by the reader.
   * @param reader the index reader
   * @param documentFilterBitSet the bitset represing the subset of documents being cosidered
   * @return the total number of documents
   */
  protected long determineNumberOfDocsConsidered(IndexReader reader, OpenBitSet documentFilterBitSet) {
    if (documentFilterBitSet != null) {
      this.setNumberOfDocsConsidered(documentFilterBitSet.cardinality());
    } else {
      this.setNumberOfDocsConsidered(reader.numDocs());
    }
    return this.getNumberOfDocsConsidered();
  }
  
}
