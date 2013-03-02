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
 * Provides statistics associated with a single term.
 * <p/>
 * Statistics are unavailable until collected.
 * <p/>
 * Statistics provided are:
 * <ul>
 *   <li>the number of documents considered during statistics collection</li>
 *   <li>the number of documents that contain the term</li>
 *   <li>the total frequency for this term across all considered fields</li>
 *   <li>the list of frequencies per field for this term</li>
 * </ul>
 */
class SingleTermStats extends Collectable {

  /** instance variables ====================================================== */
  private FrequencyAccumulator fieldAccumulator = new FrequencyAccumulator();
  private long                 numberOfDocsWithTerm = 0;  
  private String               text;
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a supplied text string.
   * @param text the term text
   */
  public SingleTermStats(String text) {
    super();
    this.text = text;
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the term text.
   * @return the term text
   */
  private String getText() {
    return this.text;
  }
  
  /**
   * Gets the number of documents containing this term.
   * @return the number of documents
   */
  private long getNumberOfDocsWithTerm() {
    return this.numberOfDocsWithTerm;
  }
  
  /**
   * Gets the list of frequencies per field for this term.
   * <br/>Each member will be named by field and counted by term frequency.
   * @return the term frequencies
   */
  private List<NamedFrequency> getFieldFrequencies() {
    return this.fieldAccumulator.getFrequencies();
  }
  
  /**
   * Gets the total frequency for this term across all considered fields.
   * @return the total frequency
   */
  private long getTotalFrequency() {
    return this.fieldAccumulator.getTotalFrequency();
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
      OpenBitSet docsWithTermBitSet = new OpenBitSet(reader.maxDoc());
      boolean isUnfiltered = (documentFilterBitSet == null);

      // return if there are no stats to collect
      String[] fieldNames = request.getCollectableFieldNames(reader);
      if (this.determineNumberOfDocsConsidered(reader,documentFilterBitSet) <= 0) {
        return;
      } else if ((fieldNames == null) || (fieldNames.length == 0)) {
        return;
      }
      
      //Map<String,Long,>
      
      // accumlate term frequencies per field
      termDocs = reader.termDocs();
      for (String fieldName: fieldNames) {   
        termEnum = reader.terms(new Term(fieldName,this.text));
        do {
          Term term = termEnum.term();
          if (term != null && term.field().equals(fieldName)) {
            if (!term.text().equals(this.text)) {
              break;
            }
            
            termDocs.seek(term);
            long count = 0;
            while (termDocs.next()) {
              int docId = termDocs.doc();
              boolean bSet = isUnfiltered || documentFilterBitSet.fastGet(docId);
              if (bSet) {
                docsWithTermBitSet.fastSet(docId);
                count++;
                //this.fieldAccumulator.add(fieldName,termDocs.freq());
              }
            }
            this.fieldAccumulator.add(fieldName,count);
            
          } else {
            break;
          }
        } while (termEnum.next());
        termEnum.close();
        termEnum = null;
       
      }
      
      // sort
      this.numberOfDocsWithTerm = docsWithTermBitSet.cardinality();
      if (Val.chkStr(request.getSortBy()).equalsIgnoreCase("name")) {
        this.fieldAccumulator.sortByName();
      } else {
        this.fieldAccumulator.sortByFrequency();
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
    String encTerm = URLEncoder.encode(this.getText(),"UTF-8");
    String escTerm = Val.escapeXmlForBrowser(this.getText());
    String callbackUrl = baseUrl+"/terms?term="+encTerm;
    
    /*
    writer.println("timeMillis="+this.getTimeMillis());
    writer.println("term="+this.getText());
    writer.println("numberOfDocsConsidered="+this.getNumberOfDocsConsidered());
    writer.println("numberOfDocsWithTerm="+this.getNumberOfDocsWithTerm());
    writer.println("totalFrequency="+getTotalFrequency());
    
    writer.println("....................");
    List<NamedFrequency> frequencies = this.getFieldFrequencies();
    for (NamedFrequency frequency: frequencies) {
      writer.println("frequency="+frequency.getFrequency()+", field="+frequency.getName());
    }
    */
    
    if (request.getResponseFormat().equalsIgnoreCase("json")) {
      writer.println("{");
      writer.println("  \"term\": \""+Val.escapeStrForJson(this.getText())+"\",");
      writer.println("  \"documentsIndexed\": "+this.getNumberOfDocsConsidered()+",");
      writer.println("  \"numberOfDocsWithTerm\": "+this.getNumberOfDocsWithTerm()+","); 
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
      writer.println("<strong>Term:</strong>&nbsp;"+Val.escapeXmlForBrowser(this.getText()));
      writer.println("<br/><strong>Documents containing term:</strong>&nbsp;"+this.getNumberOfDocsWithTerm());
      writer.println("<br/><strong>Documents indexed:</strong>&nbsp;"+this.getNumberOfDocsConsidered());
      writer.println("<br/><strong>Number of fields listed:</strong>&nbsp;"+this.getFieldFrequencies().size());
      writer.println("</p>");
      
      // statistics table
      writer.println("<table border='1'>");
      writer.println("<thead><tr>");
      writer.println("<th><a href=\""+callbackUrl+"&amp;sortBy=name\">Field</a></th>");
      writer.println("<th><a href=\""+callbackUrl+"\">Documents</a></th>");
      writer.println("</tr></thead>");
      writer.println("<tbody>");
      List<NamedFrequency> frequencies = this.getFieldFrequencies();
      for (NamedFrequency frequency: frequencies) {
        writer.print("<tr>");
        
        String encField = URLEncoder.encode(frequency.getName(),"UTF-8");
        String escField = Val.escapeXmlForBrowser(encField);
        String href1 = baseUrl+"/fields?field="+escField;
        writer.print("<td>");
        writer.print("<a href=\""+href1+"\">");
        writer.print(Val.escapeXmlForBrowser(frequency.getName()));
        writer.print("</a></td>");
        
  
        String href2 = baseQueryUrl+"?f=html&searchText="+escField+":"+escTerm;
        writer.print("<td>");
        writer.print("<a href=\""+href2+"\">");
        writer.print(frequency.getFrequency());
        writer.print("</a></td>");
        
        writer.println("</tr>");
        writer.flush();
      }
      writer.println("</tbody>");
      writer.println("</table>");
      writer.flush();
    }
  }
  
}
