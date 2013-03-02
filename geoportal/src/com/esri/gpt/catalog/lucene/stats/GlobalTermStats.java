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
 * Provides statistics associated with all terms.
 * <p/>
 * Statistics are unavailable until collected.
 * <p/>
 * Statistics provided are:
 * <ul>
 *   <li>the number of documents considered during statistics collection</li>
 *   <li>the list of frequencies per term across all considered documents</li>
 * </ul>
 */
class GlobalTermStats extends Collectable {

  /** instance variables ====================================================== */
  private int                  maxRecords = 100;
  private int                  minFrequency = 10;
  private FrequencyAccumulator termAccumulator = new FrequencyAccumulator();
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a supplied text string.
   */
  public GlobalTermStats() {
    super();
  }
  
  /** properties  ============================================================= */
    
  /**
   * Gets the list of frequencies per term across all considered documents.
   * <br/>Each member will be named by term and counted by term frequency.
   * @return the term frequencies
   */
  private List<NamedFrequency> getTermFrequencies() {
    return this.termAccumulator.getFrequencies();
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
      
      // accumulate term frequencies
      
      termDocs = reader.termDocs();
      for (String fieldName: fieldNames) {   
        termEnum = reader.terms(new Term(fieldName));
        do {
          Term term = termEnum.term();
          if (term != null && term.field().equals(fieldName)) {
               
            termDocs.seek(term);
            long count = 0;
            while (termDocs.next()) {
              int docId = termDocs.doc();
              boolean bSet = isUnfiltered || documentFilterBitSet.get(docId);
              if (bSet) {
                count++;
                //this.termAccumulator.add(term.text(),termDocs.freq());
              }
            }
            this.termAccumulator.add(term.text(),count);
            
          } else {
            break;
          }
        } while (termEnum.next());
        termEnum.close();
        termEnum = null;
        
      }
      
      // purge based on min frequence and min records
      
      // sort
      if (Val.chkStr(request.getSortBy()).equalsIgnoreCase("name")) {
        this.termAccumulator.sortByName();
      } else {
        this.termAccumulator.sortByFrequency();
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
  private void print(StatsRequest request) throws IOException {
    PrintWriter writer = request.getResponseWriter();
    String baseUrl = request.getBaseStatsUrl();
    String baseQueryUrl = request.getBaseQueryUrl();
    String callbackUrl = baseUrl+"/terms";
    
    /*
    writer.println("timeMillis="+this.getTimeMillis());
    writer.println("numberOfDocsConsidered="+this.getNumberOfDocsConsidered());
    writer.println("....................");
    List<NamedFrequency> frequencies = this.getTermFrequencies();
    for (NamedFrequency frequency: frequencies) {
      if (frequency.getFrequency() >= this.minTermFrequency) {
        writer.println("frequency="+frequency.getFrequency()+", term="+frequency.getName());
      }
    }
    */
    
    
    if (request.getResponseFormat().equalsIgnoreCase("json")) {
      writer.println("{");
      writer.println("  \"documentsIndexed\": "+this.getNumberOfDocsConsidered()+",");
      writer.println("  \"minFrequencyConsidered\": "+this.minFrequency+",");
      writer.println("  \"maxTermsConsidered\": "+this.maxRecords+",");      
      writer.println("  \"terms\": [");
      List<NamedFrequency> frequencies = this.getTermFrequencies();
      int count = 0;
      int size = frequencies.size();
      for (NamedFrequency frequency: frequencies) {
        count++;
        boolean isLast = (count >= size);
        if ((this.maxRecords > 0) && (count >= this.maxRecords)) {
          isLast = true;
        }
        
        writer.print("    {");
        writer.print("\"name\": \""+Val.escapeStrForJson(frequency.getName())+"\"");
        writer.print(", \"documents\": "+frequency.getFrequency());
        if (!isLast) {
          writer.println("},");
        } else {
          writer.println("}");
          break;
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
      writer.println("<strong>Terms</strong>");
      writer.println("<br/><strong>Documents indexed:</strong>&nbsp;"+this.getNumberOfDocsConsidered());
      writer.println("<br/><strong>Number of terms listed:</strong>&nbsp;"+this.getTermFrequencies().size());
      writer.println("<br/><strong>Minimum frequency considered:</strong>&nbsp;"+this.minFrequency);
      writer.println("<br/><strong>Maximum terms considered:</strong>&nbsp;"+this.maxRecords);
      writer.println("</p>");
      
      // statistics table
      writer.println("<table border='1'>");
      writer.println("<thead><tr>");
      writer.println("<th><a href=\""+callbackUrl+"?sortBy=name\">Term</a></th>");
      writer.println("<th><a href=\""+callbackUrl+"\">Documents</a></th>");
      writer.println("</tr></thead>");
      writer.println("<tbody>");
      List<NamedFrequency> frequencies = this.getTermFrequencies();
      long count = 0;
      for (NamedFrequency frequency: frequencies) {
        if (frequency.getFrequency() >= this.minFrequency) {
          
          count++;
          if ((this.maxRecords > 0) && (count > this.maxRecords)) {
            break;
          }
          writer.print("<tr>");
          
          String encTerm = URLEncoder.encode(frequency.getName(),"UTF-8");
          String escTerm = Val.escapeXmlForBrowser(frequency.getName());
          String href1 = baseUrl+"/terms?term="+escTerm;
          writer.print("<td>");
          writer.print("<a href=\""+href1+"\">");
          writer.print(Val.escapeXmlForBrowser(frequency.getName()));
          writer.print("</a></td>");
          
          String href2 = baseQueryUrl+"?f=html&searchText="+escTerm;
          writer.print("<td style=\"text-align:right;\">");
          writer.print("<a href=\""+href2+"\">");
          writer.print(frequency.getFrequency());
          writer.print("</a></td>");
              
          writer.println("</tr>");
          //writer.flush();
        }
      }
      writer.println("</tbody>");
      writer.println("</table>");
      writer.flush();
    }
  }
  
}
