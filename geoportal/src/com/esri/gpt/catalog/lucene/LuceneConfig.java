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
import com.esri.gpt.framework.util.Val;
import java.util.Map;
import org.apache.lucene.search.BooleanQuery;

/**
 * Maintains configuration information associated with a Lucene based index.
 */
public class LuceneConfig {
    
  /** instance variables ====================================================== */
  private String                   analyzerClassName = "";
  private String                   indexLocation = "";
  private Map<String,IParserProxy> parserProxies;
  private boolean                  useNativeFSLockFactory = false;
  private int                      writeLockTimeout = 0;
  private boolean                  useConstantScoreQuery;
  private LuceneIndexObserverArray observers = new LuceneIndexObserverArray();

  /** constructors ============================================================ */

  /** Default constructor. */
  public LuceneConfig() {}

  /** properties ============================================================== */
  
  /**
   * Gets the class name for the analyzer.
   * @return the analyzer class name
   */
  public String getAnalyzerClassName() {
    return this.analyzerClassName;
  }
  /**
   * Sets the class name for the analyzer.
   * @param className the analyzer class name
   */
  public void setAnalyzerClassName(String className) {
    this.analyzerClassName = Val.chkStr(className);
  }

  /**
   * Gets the location of the folder that will hold the Lucene index for the catalog.
   * @return the catalog index location
   */
  public String getIndexLocation() {
    return this.indexLocation;
  }
  /**
   * Sets the location of the folder that will hold the Lucene index for the catalog.
   * @param path the catalog index location
   */
  public void setIndexLocation(String path) {
    this.indexLocation = Val.chkStr(path);
  }
  
  /**
   * Gets parser proxies.
   * @return collection (map) of parser proxies
   */
  public Map<String,IParserProxy> getParserProxies() {
    return parserProxies;
  }
  /**
   * Sets parser proxies.
   * @param parserProxies collection (map) of parser proxies
   */
  public void setParserProxies(Map<String,IParserProxy> parserProxies) {
    this.parserProxies = parserProxies;
  }
  
  /**
   * Gets the flag indicating if a NativeFSLockFactory should be used.
   * @return true if a NativeFSLockFactory should be used, otherwise use a SimpleFSLockFactory
   */
  public boolean getUseNativeFSLockFactory() {
    return this.useNativeFSLockFactory;
  }
  /**
   * Sets the flag indicating if a NativeFSLockFactory should be used.
   * @param useNative true if a NativeFSLockFactory should be used, otherwise use a SimpleFSLockFactory 
   */
  public void setUseNativeFSLockFactory(boolean useNative) {
    this.useNativeFSLockFactory = useNative;
  }
  
  /**
   * Gets the write lock timeout in milli-seconds.
   * @return the write lock timeout 
   */
  public int getWriteLockTimeout() {
    return this.writeLockTimeout;
  }
  /**
   * Sets the write lock timeout in milli-seconds.
   * <br/>If the timout is less than zero, the timeout will be set to 60000.
   * @param millis the write lock timeout  the write lock timeout 
   */
  public void setWriteLockTimeout(int millis) {
    this.writeLockTimeout = millis;
    if (this.writeLockTimeout < 0) this.writeLockTimeout = 60000;
    org.apache.lucene.index.IndexWriter.setDefaultWriteLockTimeout(this.writeLockTimeout);
  }

  
  /**
   * Gets flag indicating if use constant score query instead of prefix query
   * @return <code>true</code> to use constant score query
   */
  public boolean getUseConstantScoreQuery() {
    return useConstantScoreQuery;
  }

  /**
   * Sets flag indicating if use constant score query instead of prefix query
   * @param useConstantScoreQuery <code>true</code> to use constant score query
   */
  public void setUseConstantScoreQuery(boolean useConstantScoreQuery) {
    this.useConstantScoreQuery = useConstantScoreQuery;
  }
  
  /**
   * Gets maximum number of clauses within boolean query.
   * @return maximum number of clauses within boolean query
   */
  public int getMaxClauseCount() {
    return BooleanQuery.getMaxClauseCount();
  }

  /**
   * Sets maximum number of clauses within boolean query.
   * @param maxClauseCount maximum number of clauses within boolean query
   */
  public void setMaxClauseCount(int maxClauseCount) {
    BooleanQuery.setMaxClauseCount(maxClauseCount);
  }
  
  /**
   * Gets observers.
   * @return observers
   */
  public LuceneIndexObserverArray getObservers() {
    return observers;
  }
  
  /**
   * Sets observers.
   * @param observers observers.
   */
  public void setObservers(LuceneIndexObserverArray observers) {
    this.observers = observers!=null? observers: new LuceneIndexObserverArray();
  }
  
  /** methods ================================================================= */
  
  /**
   * Returns the string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
    sb.append(" indexLocation=").append(getIndexLocation()).append("\n");
    sb.append(" writeLockTimeout=").append(getWriteLockTimeout()).append("\n");
    sb.append(" useNativeFSLockFactory=").append(getUseNativeFSLockFactory()).append("\n");
    sb.append(" analyzerClassName=").append(getAnalyzerClassName()).append("\n");
    sb.append(" parserProxies=").append(parserProxies!=null? parserProxies: "[]").append("\n");
    sb.append(" useConstantScoreQuery=").append(getUseConstantScoreQuery()).append("\n");
    sb.append(" maxClauseCount=").append(getMaxClauseCount()).append("\n");
    sb.append(") ===== end ").append(getClass().getName());
    return sb.toString();
  }
  
}
