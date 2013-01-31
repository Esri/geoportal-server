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
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.OpenBitSet;

/**
 * Provides statistics associated with a single field.
 * <p/>
 * Statistics are unavailable until collected.
 * <p/>
 * Statistics provided are:
 * <ul>
 *   <li>the number of documents considered during statistics collection</li>
 *   <li>the number of documents that contain the field</li>
 *   <li>the total number of terms indexed for this field across all documents</li>
 *   <li>the list of frequencies per term for this field</li>
 * </ul>
 */
public class SingleFieldStats extends Collectable {

  /** instance variables ====================================================== */
  private String               fieldName;
  private int                  maxRecords = 100;
  private int                  maxFrequency = -1;
  private int                  minFrequency = 1;
  private long                 numberOfDocsWithField = 0;
  private FrequencyAccumulator termAccumulator = new FrequencyAccumulator(); 
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a supplied field name.
   * @param fieldName the field name
   */
  public SingleFieldStats(String fieldName) {
    super();
    this.fieldName = fieldName;
  }
  
  /**
   * Constructs with a supplied field name and min/max thresholds.
   * @param fieldName the field name
   * @param maxRecords the maximum number of records to return
   * @param minFrequency the minimum term frequency to consider
   * @param maxFrequency the maximum term frequency to consider
   */
  public SingleFieldStats(String fieldName, int maxRecords, int minFrequency, int maxFrequency) {
    super();
    this.fieldName = fieldName;
    if (maxRecords >= 0) {
      if (maxRecords > 10000) maxRecords = 10000;
      this.maxRecords = maxRecords;
    }
    if (minFrequency > 0) {
      this.minFrequency = minFrequency;
    }
    if (maxFrequency > 0) {
      this.maxFrequency = maxFrequency;
    }
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the field name.
   * @return the field name
   */
  private String getFieldName() {
    return this.fieldName;
  }
  
  /**
   * Gets the number of documents containing this field.
   * @return the number of documents
   */
  private long getNumberOfDocsWithField() {
    return this.numberOfDocsWithField;
  }
  
  /**
   * Gets the list of frequencies per term for this field.
   * <br/>Each member will be named by term and counted by term frequency.
   * @return the term frequencies
   */
  private List<NamedFrequency> getTermFrequencies() {
    return this.termAccumulator.getFrequencies();
  }
  
  /**
   * Gets the total number of terms indexed for this field across all documents.
   * @return the total number of terms
   */
  private long getTotalNumberOfTerms() {
    return this.termAccumulator.getTotalFrequency();
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
      OpenBitSet docsWithFieldBitSet = new OpenBitSet(reader.maxDoc());
      boolean isUnfiltered = (documentFilterBitSet == null);
      boolean checkMaxFreq = (this.maxFrequency > 0);
      boolean checkMinFreq = (this.minFrequency > 0);

      // return if there are no stats to collect
      if (this.determineNumberOfDocsConsidered(reader,documentFilterBitSet) <= 0) {
        return;
      } else if (!request.isFieldCollectable(this.fieldName)){
        return;
      }
      
      boolean checkTermDocs = true;
      if (isUnfiltered) {
        MetadataAcl acl = new MetadataAcl(request.getRequestContext());
        if (acl.isPolicyUnrestricted()) {
          if (this.getNumberOfDocsConsidered() > 25000) {
            checkTermDocs = false;
          }
        }
      }
      
      // accumulate term frequencies per field
      termEnum = reader.terms(new Term(this.fieldName));
      termDocs = reader.termDocs();
      do {
        Term term = termEnum.term();
        if (term != null && term.field().equals(this.fieldName)) {
          
          if (checkTermDocs) {
            termDocs.seek(term);
            long count = 0;
            while (termDocs.next()) {
              int docId = termDocs.doc();              
              boolean bSet = isUnfiltered || documentFilterBitSet.fastGet(docId);
              if (bSet) {
                docsWithFieldBitSet.fastSet(docId);
                count++;
              }
            }
            if ((!checkMaxFreq || (count <= this.maxFrequency)) &&
                (!checkMinFreq || (count >= this.minFrequency))) {
              this.termAccumulator.add(term.text(),count);
            }
            
          } else {
            long count = termEnum.docFreq();
            if ((!checkMaxFreq || (count <= this.maxFrequency)) &&
                (!checkMinFreq || (count >= this.minFrequency))) {
              this.termAccumulator.add(term.text(),count);
            }
          }
          
        } else {
          break;
        }
      } while (termEnum.next());
      
      // sort
      this.numberOfDocsWithField = docsWithFieldBitSet.cardinality();
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
    String callbackUrl = baseUrl+"/fields?field="+this.fieldName;
    
    int max = this.maxRecords;
    int numToReturn = this.getTermFrequencies().size();
    if ((this.maxRecords >= 0) && (this.maxRecords < numToReturn)) {
      numToReturn = max;
    }
    String sMaxFreq = "none";
    if (this.maxFrequency > 0) {
      sMaxFreq = ""+this.maxFrequency;
    }
     
    /*
    writer.println("timeMillis="+this.getTimeMillis());
    writer.println("numberOfDocsConsidered="+this.getNumberOfDocsConsidered());
    writer.println("fieldName="+this.getFieldName());
    writer.println("numberOfDocsWithField="+this.getNumberOfDocsWithField());
    writer.println("totalNumberOfTerms="+getTotalNumberOfTerms());
    
    writer.println("....................");
    List<NamedFrequency> frequencies = this.getTermFrequencies();
    for (NamedFrequency frequency: frequencies) {
      writer.println("frequency="+frequency.getFrequency()+", term="+frequency.getName());
    }
    */
    
    if (request.getResponseFormat().equalsIgnoreCase("json")) {
      writer.println("{");
      writer.println("  \"field\": \""+Val.escapeStrForJson(this.fieldName)+"\",");
      writer.println("  \"documentsIndexed\": "+this.getNumberOfDocsConsidered()+",");
      //writer.println("  \"numberOfDocsWithField\": "+this.getNumberOfDocsWithField()+",");
      writer.println("  \"totalNumberOfTerms\": "+this.getTermFrequencies().size()+","); 
      writer.println("  \"numberOfTermsListed\": "+numToReturn+","); 
      writer.println("  \"minFrequencyConsidered\": "+this.minFrequency+",");
      writer.println("  \"maxFrequencyConsidered\": "+this.maxFrequency+",");
     
      writer.println("  \"terms\": [");
      List<NamedFrequency> frequencies = this.getTermFrequencies();
      int count = 0;
      if (numToReturn > 0) {
        for (NamedFrequency frequency: frequencies) {
          count++;
          boolean isLast = (count >= numToReturn);        
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
      }
      writer.println("  ]");
      writer.println("}");
      writer.flush();
      
    } else {
    
      // navigation
      writer.println("<p><a href=\""+baseUrl+"/fields\">Fields</a>");
      //writer.println("&nbsp;&nbsp;<a href=\""+baseUrl+"/terms\">Terms</a></p>");
      
      // summary
      writer.println("<p>");
      writer.println("<strong>Field:</strong>&nbsp;"+Val.escapeXmlForBrowser(this.fieldName));
      writer.println("<br/><strong>Documents indexed:</strong>&nbsp;"+this.getNumberOfDocsConsidered());
      //writer.println("<br/><strong>Documents containing field:</strong>&nbsp;"+this.getNumberOfDocsWithField());
      writer.println("<br/><strong>Total number of terms:</strong>&nbsp;"+this.getTermFrequencies().size());
      writer.println("<br/><strong>Number of terms listed:</strong>&nbsp;"+numToReturn);
      writer.println("<br/><strong>Minimum frequency considered:</strong>&nbsp;"+this.minFrequency);
      writer.println("<br/><strong>Maximum frequency considered:</strong>&nbsp;"+sMaxFreq);
      writer.println("</p>");
      
      // statistics table
      if (numToReturn > 0) {
        writer.println("<table border='1'>");
        writer.println("<thead><tr>");
        writer.println("<th><a href=\""+callbackUrl+"&amp;sortBy=name\">Term</a></th>");
        writer.println("<th><a href=\""+callbackUrl+"\">Documents</a></th>");
        writer.println("</tr></thead>");
        writer.println("<tbody>");
        List<NamedFrequency> frequencies = this.getTermFrequencies();
        int count = 0;
        for (NamedFrequency frequency: frequencies) {
          count++;
          boolean isLast = (count >= numToReturn);
          writer.print("<tr>");
          
          String encTerm = URLEncoder.encode(frequency.getName(),"UTF-8");
          //String escTerm = Val.escapeXmlForBrowser(frequency.getName());
          String escTerm = Val.escapeXmlForBrowser(encTerm);
          String href1 = baseUrl+"/terms?term="+escTerm;
          writer.print("<td>");
          //writer.print("<a href=\""+href1+"\">");
          writer.print(Val.escapeXmlForBrowser(frequency.getName()));
          //writer.print("</a>");
          writer.print("</td>");
          
          String q = this.fieldName+":"+QueryParser.escape(frequency.getName());
          String href2 = baseQueryUrl+"?f=html&searchText="+URLEncoder.encode(q,"UTF-8");
          writer.print("<td style=\"text-align:right;\">");
          writer.print("<a href=\""+Val.escapeXmlForBrowser(href2)+"\">");
          writer.print(frequency.getFrequency());
          writer.print("</a></td>");
              
          writer.println("</tr>");
          //writer.flush();
          if (isLast) break;
        }
      }
      writer.println("</tbody>");
      writer.println("</table>");
      writer.flush();
    }
  }
  
}
