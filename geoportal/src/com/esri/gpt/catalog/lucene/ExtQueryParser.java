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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

/**
 * Extended query parser.
 */
/**package*/class ExtQueryParser extends QueryParser {

  /** term streamer */
  private TermResolver reslover;
  /** query provider */
  private QueryProvider queryProvider;
  
  /**
   * Creates instance of the extended query parser.
   * @param f field name
   * @param a analyzer
   */
  public ExtQueryParser(String f, Analyzer a, TermResolver streamer, QueryProvider queryProvider) {
    super(org.apache.lucene.util.Version.LUCENE_30, f, a);
    if (streamer==null) throw new IllegalArgumentException("No any resolver provided.");
    if (queryProvider==null) throw new IllegalArgumentException("No any query provider provided.");
    this.reslover = streamer;
    this.queryProvider = queryProvider;
  }
  
  @Override
  public Query parse(String query) throws ParseException {
    return super.parse(reslover.resolve(query));
  }
  
  @Override
  protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    Query query = queryProvider.getFieldQuery(field, queryText, slop);
    return query!=null? query: super.getFieldQuery(field, queryText, slop);
  }
  
  @Override
  protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException {
    Query query = queryProvider.getFuzzyQuery(field, termStr, minSimilarity);
    return query!=null? query: super.getFuzzyQuery(field, termStr, minSimilarity);
  }
  
  @Override
  protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException {
    Query query = queryProvider.getRangeQuery(field, part1, part2, inclusive);
    return query!=null? query: super.getRangeQuery(field, part1, part2, inclusive);
  }
  
  @Override
  protected Query getWildcardQuery(String field, String termStr) throws ParseException {
    Query query = queryProvider.getWildcardQuery(field, termStr);
    return query!=null? query: super.getWildcardQuery(field, termStr);
  }
  
  @Override
  protected Query getPrefixQuery(String field, String termStr) throws ParseException {
    Query query = queryProvider.getPrefixQuery(field, termStr);
    return query!=null? query: super.getPrefixQuery(field, termStr);
  }

}
