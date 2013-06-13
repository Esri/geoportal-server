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
package com.esri.gpt.control.georss;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 * Sitemap writer.
 */
public class SitemapWriter implements FeedWriter {
  
  /** ISO 8601 data formatter. */
  private static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  /** instance variables ====================================================== */
  private String             baseUrl = "";
  private RequestContext     context;
  private String             changefreq = "weekly";
  private String             documentUrlPattern = "/rest/document/{0}?f=html";
  private boolean            hadMax = false;
  private boolean            hadStart = false;
  private MessageBroker      messageBroker;
  private String             namespaceUri = "http://www.sitemaps.org/schemas/sitemap/0.9";
  private PrintWriter        printWriter;
  private String             priority = "";
  private RestQuery          query;
  private HttpServletRequest request;
  private String             subFormat;
  private int                urlsPerIndexFile = 1000;
  private int                urlsPerSitemapFile = 1000;

  /** constructors ============================================================ */
  
  /**
   * Creates instance of the writer.
   * @param request HHTP request
   * @param context request context
   * @param printWriter underlying writer
   * @param messageBroker message broker
   * @param query original query
   */
  public SitemapWriter(HttpServletRequest request, RequestContext context, 
      PrintWriter printWriter, MessageBroker messageBroker, RestQuery query) {
    this.request = request;
    this.context = context;
    this.printWriter = printWriter;
    this.messageBroker = messageBroker;
    this.query = query;
    this.initialize(request,context);
  }
  
  /** methods ================================================================= */
  
  /**
   * Initializes the request. 
   * @param request the HTTP request
   * @param context the request context
   */
  private void initialize(HttpServletRequest request, RequestContext context) {

    String sTmp;
    int nTmp = 0;
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    
    Enumeration<String> paramNames = request.getParameterNames();
    if (paramNames != null) {
      while (paramNames.hasMoreElements()) {
        String paramName = Val.chkStr(paramNames.nextElement());
        if (paramName.equalsIgnoreCase("start")) {
          this.hadStart = true;
        } else if (paramName.equalsIgnoreCase("max")) {
          this.hadMax = true;
        } else if (paramName.equalsIgnoreCase("f")) {
          sTmp = Val.chkStr(this.request.getParameter(paramName));
          if (sTmp.toLowerCase().startsWith("sitemap.")) {
            this.subFormat = Val.chkStr(sTmp.substring(8));
          }
        }
      }
    }
    
    String baseContextPath = RequestContext.resolveBaseContextPath(request);
    this.baseUrl = baseContextPath;
    
    sTmp = Val.chkStr(params.getValue("sitemap.baseUrl"));
    if (sTmp.length() > 0) {
      this.baseUrl = sTmp;
    } else {
      this.baseUrl += "/sitemap";
    }
    if (this.baseUrl.endsWith("&")) {
      this.baseUrl = this.baseUrl.substring(0,this.baseUrl.length() - 1);
    }
    if (this.baseUrl.endsWith("?")) {
      this.baseUrl = this.baseUrl.substring(0,this.baseUrl.length() - 1);
    }
    
    if ((this.subFormat != null) && (this.subFormat.length() > 0)) {
      sTmp = Val.chkStr(params.getValue("sitemap.documentUrlPattern."+this.subFormat));
      if (sTmp.length() == 0) {
        sTmp = Val.chkStr(params.getValue("sitemap.documentUrlPattern"));
      }
    } else {
      sTmp = Val.chkStr(params.getValue("sitemap.documentUrlPattern"));
    }
    if (sTmp.length() > 0) this.documentUrlPattern = sTmp;
    if (!this.documentUrlPattern.startsWith("http")) {
      if (!this.documentUrlPattern.startsWith("/")) {
        this.documentUrlPattern = "/"+this.documentUrlPattern;
      }
      this.documentUrlPattern = baseContextPath+this.documentUrlPattern;
    }
    
    nTmp = Val.chkInt(params.getValue("sitemap.urlsPerIndexFile"),0);
    if ((nTmp > 0) && (nTmp < 1000)) this.urlsPerIndexFile = nTmp;
    
    nTmp = Val.chkInt(params.getValue("sitemap.urlsPerSitemapFile"),0);
    if ((nTmp > 0) && (nTmp <= 50000)) this.urlsPerSitemapFile = nTmp;
    if (!this.hadStart && !this.hadMax) {
      this.query.getFilter().setMaxRecords(this.urlsPerSitemapFile);
    }
    
    sTmp = Val.chkStr(params.getValue("sitemap.namespaceUri"));
    if (sTmp.length() > 0) this.namespaceUri = sTmp;
    
    sTmp = Val.chkStr(params.getValue("sitemap.changefreq"));
    if (sTmp.length() > 0) this.changefreq = sTmp; 
    
    sTmp = Val.chkStr(params.getValue("sitemap.priority"));
    if (sTmp.length() > 0) this.priority = sTmp; 
    
    // error check
    String errPfx = "gpt.xml: gptConfig/catalog/parameter/";
    if (this.documentUrlPattern.indexOf("{0}") == -1) {
      throw new ConfigurationException(errPfx+"@key=sitemap.documentUrlPattern must contain {0}");
    }
  }
  
  /**
   * Converts a Timestamp to ISO-8601 format.
   * @param timestamp the timestamp
   * @return the formatted restlt
   */
  private String toIso8601(Timestamp timestamp) {
    String sTimestamp = "";
    if (timestamp != null) {
      sTimestamp = ISO8601FORMAT.format(timestamp);
      sTimestamp = sTimestamp.substring(0,sTimestamp.length()-2)+":"+sTimestamp.substring(sTimestamp.length()-2);
    }
    return sTimestamp;
  }

  /**
   * Writes the response.
   * @param records the response records
   */
  public void write(IFeedRecords records) {
    boolean respondWithIndex = false;
    int hits = records.getOpenSearchProperties().getNumberOfHits();
    if (!hadStart && !hadMax) {
      if ((hits > 0) && (hits > this.urlsPerSitemapFile)) {
        respondWithIndex = true;
      }
    }
    
    // generate an index based or document based sitemap response
    if (respondWithIndex) {
      this.writeSitemapIndex(hits);
    } else {
      this.writeSitemapRecords(records);
    }
    
  }
  
  /**
   * Writes response as a sitemap index file.
   * @param recordCount the number of records
   */
  private void writeSitemapIndex(int recordCount) {
    //LOGGER.info("Writing sitemap index file response, recordCount="+recordCount);
    printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    printWriter.println("<sitemapindex xmlns=\""+this.namespaceUri+"\">");
    
    //String requestUri = context.resolveBaseContextPath(arg0)
    String queryString = Val.chkStr(this.request.getQueryString());
    
    String url;
    int nWritten = 0;
    for (int start = 1; start <= recordCount; start += this.urlsPerSitemapFile) {
      url = this.baseUrl;
      if (queryString.length() > 0) {
        url = this.baseUrl+"?"+queryString;
        if (!url.endsWith("&")) {
          url += "&";
        }
      } else {
        url += "?";
      }
      url += "start="+start+"&max="+this.urlsPerSitemapFile;
      String modified = this.toIso8601(new Timestamp(System.currentTimeMillis()));
      printWriter.println("<sitemap>");
      writeTag(printWriter,"loc",url);
      writeTag(printWriter,"lastmod",modified);
      printWriter.println("</sitemap>");
      printWriter.flush();
      nWritten++;
      if (nWritten >= this.urlsPerIndexFile) break;
      //if (Thread.interrupted()) {break;}
    }
    printWriter.println("</sitemapindex>");
  }

  /**
   * Writes the response as a sitemap urlset.
   * @param records the response records
   */
  private void writeSitemapRecords(IFeedRecords records) {
    printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    printWriter.println("<urlset xmlns=\""+this.namespaceUri+"\">");
    int nWritten = 0;
    for (IFeedRecord r : records) {
      
      // determine the document URL
      String uuid = Val.chkStr(r.getUuid());
      String url = "";
      try {url = this.documentUrlPattern.replace("{0}",URLEncoder.encode(uuid,"UTF-8"));} 
      catch (UnsupportedEncodingException uee) {}
      
      // determine the modification timestamp
      Timestamp tsMod = null;
      String modified = "";
      if (r.getModfiedDate() != null) {
        tsMod = new Timestamp(r.getModfiedDate().getTime());
        modified = this.toIso8601(tsMod);
      }      
      
      // write the URL element
      printWriter.println("<url>");
      writeTag(printWriter,"loc",url);
      if ((modified != null) && (modified.length() > 0)) {
        writeTag(printWriter,"lastmod",modified);
      }
      writeTag(printWriter,"changefreq",this.changefreq);
      writeTag(printWriter,"priority",this.priority);
      printWriter.println("</url>");
      
      printWriter.flush();
      nWritten++;
      if (nWritten >= this.urlsPerSitemapFile) break;
    }
    printWriter.println("</urlset>");
  }

  /**
   * Writes an XML tag with text node content to the response.
   * @param out the HTTP response writer
   * @param tag the name of the tag
   * @param content the text node content
   */
  private void writeTag(PrintWriter out, String tag, String content) {
    if ((content != null) && (content.length() > 0)) {
      out.println("<"+tag+">"+Val.escapeXml(content)+"</"+tag+">");
    }
  }

}
