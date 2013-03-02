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
package com.esri.gpt.control.view;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.util.UuidUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the display of local metadata thumbnail images.
 * <p>
 * Most thumbnails are remote urls and are accessed through standard HTTP. <br/>
 * The images for some metadata thumbnails are stored within the DBMS. These
 * typically come from metadata that is being created/edited through ArcCatalog.
 * These images are retrieved by the browser through this Servlet. A sample url
 * to access this servlet:<br/> &lt;img
 * src="/GPT9/thumbnail?uuid={6153A394-70A9-4311-A6DC-582F2F9758FF}"/&gt;
 */
public class ThumbnailServlet extends BaseServlet {

  /** class variables ========================================================= */

  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(ThumbnailServlet.class.getName());
  
  /** methods ================================================================= */
  
  /**
   * Executes the request.
   * @param request the HTTP request
   * @param response the HTTP response
   * @throws Exception if an exception occurs
   */
  protected void execute(HttpServletRequest request,
      HttpServletResponse response, RequestContext context) throws Exception {
  
    // determine the guid for the thumbnail
    String uuid = UuidUtil.addCurlies(request.getParameter("uuid"));
  
    Connection con = null;
    PreparedStatement st = null;
    InputStream bis = null;
    ServletOutputStream sos = null;
    try {
  
      // open the binary input stream for the thumbnail image (blob)
      if (uuid.length() > 0) {
        
        // ensure access to the document
        boolean bCheckAccess = true;
        if (bCheckAccess) {
          MetadataAcl acl = new MetadataAcl(context);
          boolean bHasAccess = acl.hasReadAccess(context.getUser(),uuid);
          if (!bHasAccess) {
            throw new NotAuthorizedException("Access denied");
          }
        }
  
        con = context.getConnectionBroker().returnConnection("").getJdbcConnection();
        String sTbl = context.getCatalogConfiguration().getResourceDataTableName();
        String sSql = "SELECT THUMBNAIL FROM " + sTbl + " WHERE DOCUUID=?";
        st = con.prepareStatement(sSql);
        st.setString(1,uuid);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
        	String database = con.getMetaData().getDatabaseProductName().toLowerCase();
      		if (database.contains("postgresql")){
      			bis = rs.getBinaryStream("thumbnail");
      		}
      		else{
	          Blob blob = rs.getBlob(1);
	          if (blob != null) {
	            bis = blob.getBinaryStream();
	          }
      		}
        }
      }
  
      // write the image to the response output stream
      if (bis != null) {
        response.setHeader("Content-Disposition:","attachment;filename=thumbnail");
        byte[] aBuffer = new byte[4096];
        sos = response.getOutputStream();
        while (true) {
          int nBytes = bis.read(aBuffer, 0, aBuffer.length);
          if (nBytes <= 0) break;
          sos.write(aBuffer, 0, nBytes);
        }
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
        
    } finally {
      try {if (st  != null) st.close();}  catch (Exception ef) {}
      try {if (con != null) con.close();} catch (Exception ef) {}
      try {if (bis != null) bis.close();} catch (Exception ef) {}
      try {if (sos != null) sos.flush();} catch (Exception ef) {LOGGER.severe(ef.getMessage());}
    }
  }

}
