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
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.OpenBitSet;

/**
 * Provides statistics associated with all indexed fields.
 * <p/>
 * Statistics are unavailable until collected.
 * <p/>
 * Statistics provided are:
 * <ul>
 *   <li>the number of documents considered during statistics collection</li>
 *   <li>the number of fields considered</li>
 *    <li>the list of document frequencies per field</li>
 * </ul>
 */
class GlobalFieldStats extends Collectable {

  /** instance variables ====================================================== */
  private FrequencyAccumulator fieldAccumulator = new FrequencyAccumulator(); 
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a supplied term text.
   */
  public GlobalFieldStats() {
    super();
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the number of fields considered.
   * @return the number of fields
   */
  private int getFieldCount() {
    return this.fieldAccumulator.getFrequencies().size();
  }
  
  /**
   * Gets the list of document frequencies per term.
   * <br/>Each member will be named by field and counted by document frequency.
   * @return the field frequencies
   */
  private List<NamedFrequency> getFieldFrequencies() {
    return this.fieldAccumulator.getFrequencies();
  }
  
  /** methods ================================================================= */
  
  /**
   * Executes the collection of statistics.
   * @param request the active statistics request
   * @param reader the index reader
   * @throws IOException if an error occurs while communicating with the index
   */
  public void collectStats(StatsRequest request, IndexReader reader) throws IOException {
    long t1 = System.currentTimeMillis();
    TermEnum termEnum = null;
    TermDocs termDocs = null;
    try {
      OpenBitSet documentFilterBitSet = request.getDocumentFilterBitSet();
      boolean isUnfiltered = (documentFilterBitSet == null);

      // return if there are no stats to collect
      String[] fieldNames = request.getCollectableFieldNames(reader);
      if (this.determineNumberOfDocsConsidered(reader,documentFilterBitSet) <= 0) {
        return;
      } else if ((fieldNames == null) || (fieldNames.length == 0)) {
        return;
      }
      
      // accumulate field frequencies per document
      termDocs = reader.termDocs();
      for (String fieldName: fieldNames) {   
        termEnum = reader.terms(new Term(fieldName));
        OpenBitSet docsWithFieldBitSet = new OpenBitSet(reader.maxDoc());
        do {
          Term term = termEnum.term();
          if (term != null && term.field().equals(fieldName)) {

            termDocs.seek(term);
            while (termDocs.next()) {
              int docId = termDocs.doc();
              boolean bSet = isUnfiltered || documentFilterBitSet.fastGet(docId);
              if (bSet) {
                docsWithFieldBitSet.fastSet(docId);
              }
            }
            
          } else {
            break;
          }
        } while (termEnum.next());
        termEnum.close();
        termEnum = null;
        
        if (docsWithFieldBitSet.cardinality() > 0) {
          this.fieldAccumulator.add(fieldName,docsWithFieldBitSet.cardinality());
        }
      }
      
      // sort
      if (this.getSortByFrequency()) {
        this.fieldAccumulator.sortByFrequency();
      } else {
        this.fieldAccumulator.sortByName();
      }
      
    } finally {
      try {if (termEnum != null) termEnum.close();} catch (Exception ef) {}
      try {if (termDocs != null) termDocs.close();} catch (Exception ef) {}
      this.setTimeMillis(System.currentTimeMillis() - t1);
    }   
    
    // print
    if (request.getResponseWriter() != null) {
      this.print(request);
    }
  }
  
  /**
   * Prints collected statistics.
   * @param request the active statistics request
   */
  private void print(StatsRequest request) throws IOException{
    PrintWriter writer = request.getResponseWriter();
    String baseUrl = request.getBaseStatsUrl();
    
    /*
    writer.println("timeMillis="+this.getTimeMillis());
    writer.println("numberOfDocsConsidered="+this.getNumberOfDocsConsidered());
    writer.println("totalNumberOfFields="+this.getFieldFrequencies().size());
        
    writer.println("....................");
    List<NamedFrequency> docCounts = this.getFieldFrequencies();
    for (NamedFrequency frequency: docCounts) {
      writer.println("fieldName="+frequency.getName()+", numberOfDocsWithField="+frequency.getFrequency());
    }
    */
    //writer.println("<h4>");
    //writer.println("</h4>");
    
    if (request.getResponseFormat().equalsIgnoreCase("json")) {
      writer.println("{");
      writer.println("  \"documentsIndexed\": "+this.getNumberOfDocsConsidered()+",");
      writer.println("  \"fields\": [");
      List<NamedFrequency> frequencies = this.getFieldFrequencies();
      int count = 0;
      int size = frequencies.size();
      for (NamedFrequency frequency: frequencies) {
        count++;
        writer.print("    {");
        writer.print("\"name\": \""+Val.escapeStrForJson(frequency.getName())+"\"");
        writer.print(", \"documents\": "+frequency.getFrequency());
        if (count < size) {
          writer.println("},");
        } else {
          writer.println("}");
        }
      }
      writer.println("  ]");
      writer.println("}");
      writer.flush();
      
    } else {
    
      // navigation
      writer.println("<p><a href=\""+baseUrl+"/fields\">Fields</a>");
      writer.println("&nbsp;&nbsp;<a href=\""+baseUrl+"/terms\">Terms</a></p>");
      
      // summary
      writer.println("<p>");
      writer.println("<strong>Indexed fields:</strong>&nbsp;"+this.getFieldFrequencies().size());
      writer.println("<br/><strong>Indexed documents:</strong>&nbsp;"+this.getNumberOfDocsConsidered());
      writer.println("</p>");
      
      // statistics table
      writer.println("<table border='1'>"); 
      writer.println("<thead><tr><th>Field</th><th>Documents</th></tr></thead>");
      writer.println("<tbody>");
      List<NamedFrequency> frequencies = this.getFieldFrequencies();
      for (NamedFrequency frequency: frequencies) {
        writer.print("<tr>");
        writer.print("<td>");
        String href = baseUrl+"/fields?field="+URLEncoder.encode(frequency.getName(),"UTF-8");
        writer.print("<a href=\""+href+"\">");
        writer.print(Val.escapeXmlForBrowser(frequency.getName()));
        writer.print("</a></td>");
        writer.print("<td>");
        writer.print(frequency.getFrequency());
        writer.print("</td>");      
        writer.println("</tr>");
        writer.flush();
      }
      writer.println("</tbody>");
      writer.println("</table>");
      writer.flush();
    }
  }
  
}
