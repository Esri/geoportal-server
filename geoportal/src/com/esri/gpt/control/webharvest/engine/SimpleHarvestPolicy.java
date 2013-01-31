/*
 * Copyright 2012 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Simple harvest policy.
 */
public class SimpleHarvestPolicy extends HarvestPolicy {
  public static final String SIMPLE_POLICY_FORCE_FREQUENCY = "webharvester.policy.simple.frequency";
  private static Integer forceFrequency;

  @Override
  public boolean getForceFullHarvest(HrRecord record) {
    int nForceFrequency = getForceFrequency();
    if (nForceFrequency>0) {
      if (nForceFrequency==1) {
        LOG.info("{HARVEST POLICY] Attempting to enforce 'Full harvest' for the record: "+record.getUuid());
        return true;
      }
      
      ConnectionBroker connectionBroker = new ConnectionBroker();
      PreparedStatement st = null;
      ResultSet rs = null;
      try {
        ManagedConnection managedConnection = connectionBroker.returnConnection("");
        Connection connection = managedConnection.getJdbcConnection();
        st = connection.prepareStatement("SELECT COUNT(*) JOB_TYPE FROM GPT_HARVESTING_JOBS_COMPLETED WHERE HARVEST_ID=?");
        st.setString(1, record.getUuid());
        
        rs = st.executeQuery();
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count % nForceFrequency != 0) {
            return false;
          }
        }
        
        LOG.info("{HARVEST POLICY] Attempting to enforce 'Full harvest' for the record: "+record.getUuid());
        return true;
      } catch (SQLException ex) {
        LOG.log(Level.SEVERE, "[HARVEST POLICY] Error evaluating policy for the record: "+record.getUuid(), ex);
      } finally {
        if (rs!=null) {
          try {
            rs.close();
          } catch (SQLException ex) {}
        }
        if (st!=null) {
          try {
            st.close();
          } catch (SQLException ex) {}
        }
        connectionBroker.closeAll();
      }
    }
    return false;
  }
  
  protected int getForceFrequency() {
    if (forceFrequency==null) {
      String strForceFrequency = Val.chkStr(getParameters().getValue(SIMPLE_POLICY_FORCE_FREQUENCY));
      int nForceFrequency = Val.chkInt(strForceFrequency, 0);
      forceFrequency = Math.max(nForceFrequency, 0);
    }
    return forceFrequency;
  }
}
