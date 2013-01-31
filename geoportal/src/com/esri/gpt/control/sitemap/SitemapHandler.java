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
package com.esri.gpt.control.sitemap;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.util.Val;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the generation of sitemap files based upon the content of the metadata catalog.
 */
public class SitemapHandler extends BaseDao {

  /** class variables ========================================================= */
  
  /** ISO 8601 data formatter. */
  private static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  
  /** The Logger. */
	private static final Logger LOGGER = Logger.getLogger(SitemapHandler.class.getName());
		
	/** Standard XML header. */
	private static String XMLHEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	/** instance variables ====================================================== */
	private String baseUrl = "";
	private String changefreq = "weekly";
	private String documentUrlPattern = "/rest/document/{0}?f=html";
	private String namespaceUri = "http://www.sitemaps.org/schemas/sitemap/0.9";
	private String priority = "";
	private int    startRecord = -1;
	private int    urlsPerIndexFile = 1000;
	private int    urlsPerSitemapFile = 40000;

	/** constructors ============================================================ */

	/** Default constructor. */
	protected SitemapHandler() {}
			
	/** methods ================================================================= */
	
  /**
   * Counts the number of records that are publically available for the sitemap.
   * @return the record count
   * @throws SQLException if a database related exception occurs
   */
  private int countRecords() throws SQLException {
    PreparedStatement st = null;
    try {
      Connection con = this.returnConnection().getJdbcConnection();
      String sql = this.makeQuery(true);
      this.logExpression(sql);
      st = con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      closeStatement(st);
    }
    return 0;
  }
	
  /**
   * Handles a sitemap request. 
   * @param request the HTTP request
   * @param response the HTTP response
   * @param context the request context
   * @throws Exception if an exception occurs
   */
  protected void handle(HttpServletRequest request, 
                        HttpServletResponse response, 
                        RequestContext context)
    throws Exception {
    PrintWriter out = null;
    try {
      initialize(request,response,context);
      out = response.getWriter();
  	  if (this.startRecord > 0) {
  	    this.writeSitemapFile(out);
  	  } else {
  	    int recordCount = this.countRecords();
  	    if (recordCount <= this.urlsPerSitemapFile) {
  	      this.writeSitemapFile(out);
  	    } else {
  	      this.writeIndexFile(out,recordCount);
  	    }
  	  }
    } finally {
      try {if (out != null) out.close();} catch (Exception ef) {}
    }
	}
  
  /**
   * Initializes the request. 
   * @param request the HTTP request
   * @param response the HTTP response
   * @param context the request context
   */
  private void initialize(HttpServletRequest request,
      HttpServletResponse response, RequestContext context) {
    
    String sTmp;
    int nTmp = 0;
    this.setRequestContext(context);
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    
    String baseContextPath = RequestContext.resolveBaseContextPath(request);
    this.baseUrl = baseContextPath+"/sitemap";
    this.startRecord = Val.chkInt(request.getParameter("startRecord"),-1);
    response.setContentType("text/xml; charset=UTF-8");
    
    
    sTmp = Val.chkStr(params.getValue("sitemap.baseUrl"));
    if (sTmp.length() > 0) this.baseUrl = sTmp;
    if (this.baseUrl.endsWith("&")) {
      this.baseUrl = this.baseUrl.substring(0,this.baseUrl.length() - 1);
    }
    if (this.baseUrl.endsWith("?")) {
      this.baseUrl = this.baseUrl.substring(0,this.baseUrl.length() - 1);
    }
    
    sTmp = Val.chkStr(params.getValue("sitemap.documentUrlPattern"));
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
    if ((nTmp > 0) && (nTmp < 50000)) this.urlsPerSitemapFile = nTmp;
    
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
   * Builds the SQL query string.
   * @param countOnly if true a COUNT(*) query is returned
   * @return the SQL query string
   */
	private String makeQuery(boolean countOnly) {
    String resourceTable = this.getRequestContext().getCatalogConfiguration().getResourceTableName();
    StringBuilder sql = new StringBuilder();
    if (countOnly) {
      sql.append("SELECT COUNT(*) FROM ");
    } else {
      sql.append("SELECT DOCUUID,UPDATEDATE FROM ");
    }
    sql.append(resourceTable);
    sql.append(" WHERE ");
    sql.append(" AND ((APPROVALSTATUS = 'approved') OR (APPROVALSTATUS = 'reviewed'))");
    sql.append(" AND (ACL IS NULL)");
    return sql.toString();
	}
	
	/**
   * Writes the sitemap index file response.
   * @param out the HTTP response writer
   * @param recordCount the number of sitemap records
   */
  private void writeIndexFile(PrintWriter out, int recordCount) {
    LOGGER.log(Level.INFO, "Writing sitemap index file response, recordCount={0}", recordCount);
    out.println(SitemapHandler.XMLHEADER);
    out.println("<sitemapindex xmlns=\""+this.namespaceUri+"\">");
    
    String url;
    int nWritten = 0;
    for (int start = 1; start <= recordCount; start += this.urlsPerSitemapFile) {
      if (this.baseUrl.indexOf("?") == -1) {
        url = this.baseUrl+"?startRecord="+start;
      } else {
        url = this.baseUrl+"&startRecord="+start;
      }
      String modified = this.toIso8601(new Timestamp(System.currentTimeMillis()));
      out.println("<sitemap>");
      writeTag(out,"loc",url);
      writeTag(out,"lastmod",modified);
      out.println("</sitemap>");
      out.flush();
      nWritten++;
      if (nWritten >= this.urlsPerIndexFile) break;
      if (Thread.interrupted()) {break;}
    }
    
    out.println("</sitemapindex>");
  }
  
  /**
   * Writes a sitemap file response.
   * @param out the HTTP response writer
   * @throws SQLException if a database exception occurs 
   */
  private void writeSitemapFile(PrintWriter out) throws SQLException {
    if (this.startRecord > 0) {
      LOGGER.log(Level.INFO, "Writing sitemap file response, startRecord={0}", this.startRecord);
    } else {
      LOGGER.info("Writing sitemap file response.");
    }
  	PreparedStatement st = null;
  	
  	try {
      out.println(SitemapHandler.XMLHEADER);
      out.println("<urlset xmlns=\""+this.namespaceUri+"\">");
      
      // construct the query
      int nRecord = 0;
      int nWritten = 0;
      String sql = this.makeQuery(false);
      
      // execute thq query
      Connection con = this.returnConnection().getJdbcConnection();
      this.logExpression(sql);
      st = con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        nRecord++;
        if (nRecord >= this.startRecord) {
          String uuid = rs.getString(1);
          
          // determine the document URL
          String url = "";
          try {url = this.documentUrlPattern.replace("{0}",URLEncoder.encode(uuid,"UTF-8"));} 
          catch (UnsupportedEncodingException uee) {}
          
          // determine the modification timestamp
          Timestamp tsMod = rs.getTimestamp(2);
          String modified = this.toIso8601(tsMod);
        
          // write the URL element
          out.println("<url>");
          writeTag(out,"loc",url);
          writeTag(out,"lastmod",modified);
          writeTag(out,"changefreq",this.changefreq);
          writeTag(out,"priority",this.priority);
          out.println("</url>");
          out.flush();
          nWritten++;
          if (nWritten >= this.urlsPerSitemapFile) break;
          if (Thread.interrupted()) break;
        }  
      }
        
    } finally {
      closeStatement(st);
      try {out.println("</urlset>");} catch (Exception ef) {}
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
