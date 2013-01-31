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
package com.esri.gpt.control.rest.repositories;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Provides utility functions associated with registered CSW reposirories.
 */
public class CswRepository  {
  
  /** class variables ============================================================ */
  
  /** Cached templates */
  private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
  
  /** XSLT for transforming CSW capabilities to HTML: "gpt/metadata/ogc/csw-to-html.xslt" */
  public static final String XSLT_CSW_TO_HTML = "gpt/metadata/ogc/csw-to-html.xslt";
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public CswRepository() {}
  
  /** methods ================================================================= */
  
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   * @throws SAXException if a SAX parsing exception occurs
   */
  private synchronized XsltTemplate getCompiledTemplate(String xsltPath)
    throws TransformerException {
    String sKey = xsltPath;
    XsltTemplate template = XSLTTEMPLATES.get(sKey);
    if (template == null) {
      template = XsltTemplate.makeTemplate(xsltPath);
      XSLTTEMPLATES.put(sKey,template);
    }
    return template;
  }
  
  /**
   * Returns the URL associated with a registered CSW repository.
   * @param context request context
   * @param rid the remote CSW repository id
   * @return the registered URL for the remote repository
   * @throws SQLException if an exception occurs
   */
  public String queryCswUrl(RequestContext context, String rid) throws SQLException {
    PreparedStatement st = null;
    ManagedConnection mcon = null;
    rid = Val.chkStr(rid);
    if (rid.length() > 0) {
      try {
        int nId = -1;
        String field = "UUID";
        try {
          nId = Integer.parseInt(rid);
          field = "ID";     
        } catch (NumberFormatException nfe) {}
        
        String table = context.getCatalogConfiguration().getResourceTableName();
        String sql = "SELECT PROTOCOL_TYPE,HOST_URL FROM "+table+" WHERE "+field+"=?";
        mcon = context.getConnectionBroker().returnConnection("");
        st = mcon.getJdbcConnection().prepareStatement(sql);
        if (field.equalsIgnoreCase("ID")) {
          st.setInt(1,nId);
        } else {
          st.setString(1,rid);
        }
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
          if (Val.chkStr(rs.getString(1)).equalsIgnoreCase("CSW")) {
            return rs.getString(2);
          }
        }
      } finally {
        try {if (st != null) st.close();} catch (Exception ef) {}
        context.getConnectionBroker().closeConnection(mcon);
      }
    }
    return null;
  }
  
  /**
   * Transforms the response of a CSW GetCapabilities request to an HTML
   * suitable for display within the GPt searach page.
   * <br/>gpt/metadata/ogc/csw-to-html.xslt
   * @param url the CSW GetCapabilities URL
   * @return the HTML representation
   * @throws TransformerException if an exception occurs
   */
  public String transformToHtml(String url) throws TransformerException {
    XsltTemplate template = getCompiledTemplate(CswRepository.XSLT_CSW_TO_HTML);
    String xml = template.transform(XmlIoUtil.readXml(url));
    return xml;
  }

}
