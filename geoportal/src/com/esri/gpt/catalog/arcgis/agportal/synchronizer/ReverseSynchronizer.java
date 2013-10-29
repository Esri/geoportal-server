/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.catalog.arcgis.agportal.synchronizer;

import com.esri.gpt.catalog.arcgis.agportal.itemInfo.ESRI_ItemInformation;
import com.esri.gpt.catalog.arcgis.agportal.publication.EndPoint;
import com.esri.gpt.catalog.arcgis.agportal.publication.ItemInfoLuceneAdapter;
import com.esri.gpt.catalog.arcgis.agportal.publication.PublicationRequest;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.scheduler.IScheduledTask;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reverse ArcGIS portal synchronizer.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class ReverseSynchronizer implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
  private static final Logger LOGGER = Logger.getLogger(ReverseSynchronizer.class.getName());
  /** instance variables ====================================================== */
  private StringAttributeMap parameters;

  /** constructors  =========================================================== */
  /** Default constructor. */
  public ReverseSynchronizer() {
  }

  /** properties  ============================================================= */
  /**
   * Sets the configuration parameters for the task.
   * @param parameters the configuration parameters
   */
  @Override
  public void setParameters(StringAttributeMap parameters) {
    this.parameters = parameters;
  }

  /** methods ================================================================= */
  /**
   * Run the synchronization process.
   */
  @Override
  public void run() {

    try {
      
      Timestamp ts = new Timestamp(new Date().getTime());
      LOGGER.info("Starting reverse sycnhronization.");
      
      RequestContext context = RequestContext.extract(null);
      PublicationRequest request = createPublicationRequest(context);
      publishAnythingWithUrl(context, request);

      setLastSynchronizationDate(ts);

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error in reverse synchronization.", e);
    } finally {
      LOGGER.info("Reverse sycnhronization completed.");
    }
  }

  private void publishAnythingWithUrl(RequestContext context, PublicationRequest request) throws SQLException {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      StringBuilder sbSql = new StringBuilder();

      sbSql.append("SELECT DOCUUID, HOST_URL FROM ");
      sbSql.append(getResourceTableName(context));
      sbSql.append(" WHERE HOST_URL IS NOT NULL AND (APPROVALSTATUS='approved' OR APPROVALSTATUS='reviewed') ");
      sbSql.append(" AND UPDATEDATE >= ?");

      ManagedConnection mc = context.getConnectionBroker().returnConnection("");
      Connection con = mc.getJdbcConnection();
      st = con.prepareStatement(sbSql.toString());
      st.setTimestamp(1, getLastSynchronizationDate());

      rs = st.executeQuery();

      while (rs.next()) {
        String hostUrl = rs.getString("HOST_URL");
        if (assertHostUrl(hostUrl)) {
          String docuuid = rs.getString("DOCUUID");
          publishUuid(context, request, docuuid);
        }
      }
    } finally {
      SQLclose(rs);
      SQLclose(st);
    }
  }

  private void SQLclose(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException ex) {
      }
    }
  }

  private void SQLclose(Statement st) {
    if (st != null) {
      try {
        st.close();
      } catch (SQLException ex) {
      }
    }
  }

  private String getDateStore() {
    return getParameter("datestore");
  }

  private void publishUuid(RequestContext context, PublicationRequest request, String uuid) {
    try {
      ItemInfoLuceneAdapter iiAdapter = new ItemInfoLuceneAdapter();
      ESRI_ItemInformation ii = iiAdapter.makeItemInfoByUuid(context, null, uuid);
      if (ii != null) {
        request.publish(ii);
        LOGGER.log(Level.FINE, "Record: {0} has been published.", uuid);
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Record: {0} has NOT been published. {1}", new Object[]{uuid, ex.getMessage()});
    }
  }

  /**
   * Gets resource table name.
   * @param context request context
   * @return resource table name
   */
  private String getResourceTableName(RequestContext context) {
    return context.getApplicationConfiguration().getCatalogConfiguration().getResourceTableName();
  }

  /**
   * Creates publication request.
   * @param context request context
   * @return publication request
   * @throws Exception if creating publication request fails
   */
  private PublicationRequest createPublicationRequest(RequestContext context) throws Exception {
    return new PublicationRequest(context, EndPoint.extract(context), getCredentialProvider(), getParameter("referer"));
  }

  /**
   * Gets last synchronization date.
   * @return last synchronization date
   */
  private Timestamp getLastSynchronizationDate() {
    File file = new File(getDateStore());
    if (file.exists()) {
      FileInputStream fis = null;
      ObjectInputStream is = null;
      try {
        fis = new FileInputStream(file);
        is = new ObjectInputStream(fis);
        Timestamp ts = (Timestamp) is.readObject();
        return ts;
      } catch (Exception ex) {
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
          }
        }
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
          }
        }
      }
    }
    return new Timestamp(0);
  }

  /**
   * Sets last synchronization date.
   * @param ts last synchronization date
   */
  private void setLastSynchronizationDate(Timestamp ts) {
    File file = new File(getDateStore());
    FileOutputStream fis = null;
    ObjectOutputStream is = null;
    try {
      fis = new FileOutputStream(file);
      is = new ObjectOutputStream(fis);
      is.writeObject(ts);
    } catch (Exception ex) {
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
        }
      }
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Creates credential provider.
   * Credential provider is created based on the following thread parameters: 
   * "username", and "password.
   * @return credential provider.
   */
  private CredentialProvider getCredentialProvider() {
    String username = getParameter("username");
    String password = getParameter("password");
    if (username.length() > 0 && password.length() > 0) {
      return new CredentialProvider(username, password);
    }
    return null;
  }

  /**
   * Gets thread parameter.
   * @param name parameter name
   * @return parameter value
   */
  private String getParameter(String name) {
    return parameters != null ? Val.chkStr(parameters.getValue(name)) : "";
  }

  /**
   * Checks if given URL has a chance to be published in ArcGIS portal
   * @param hostUrl url
   * @return <code>true</code> if url has a chance to be published in ArcGIS portal
   */
  private boolean assertHostUrl(String hostUrl) {
    hostUrl = Val.chkStr(hostUrl);
    if (hostUrl.contains(".mpk") || hostUrl.contains(".eaz") || hostUrl.contains(".nmf")
        || hostUrl.contains(".esriaddin") || hostUrl.contains(".lpk")
        || hostUrl.contains(".zip") || hostUrl.contains(".ncfg")
        || hostUrl.contains(".wmpk") || (hostUrl.contains("/rest/services/") /*&& hostUrl.endsWith("Server")*/)) {
      return true;
    }
    return false;
  }
}
