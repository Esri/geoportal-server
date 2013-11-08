/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gc.gpt.db;

import gc.base.sql.SqlQInfo;
import gc.base.sql.SqlQuery;
import gc.base.sql.SqlRowHandler;
import gc.base.task.TaskContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * GPT collections.
 */
public class GptCollections extends GptRecord {

  private static Map<String,String> collections;
  
  private Set<String> shortNames = new HashSet<String>();

  /**
   * Builds the object based upon a supplied document uuid.
   *
   * @param context the task context
   * @param con the database connection
   * @param docuuid the document uuid
   * @throws Exception if an exception occurs
   */
  public void querySqlDB(TaskContext context, Connection con, String docuuid) throws Exception {
    init(context, con);
    
    SqlQInfo info = getSqlQInfo();
    info.setTableSuffix("COLLECTION_MEMBER");
    info.setWhere("DOCUUID=?");
    List<Object> bindings = new ArrayList<Object>();
    bindings.add(docuuid);
    info.setQueryBindings(bindings);
    SqlQuery q = new SqlQuery();
    q.query(context, con, info, new SqlRowHandler() {
      @Override
      public void handleSqlRow(TaskContext context, Connection con, ResultSet rs, long rowNum) throws Exception {
        readFields(rs);
      }
    });
  }

  @Override
  public void readFields(ResultSet rs) throws SQLException {
    String coluuid = rs.getString("COLUUID");
    String shortName = collections.get(coluuid);
    if (shortName!=null) {
      shortNames.add(shortName);
    }
  }
  
  public List<String> getShortNames() {
    return new ArrayList<String>(shortNames);
  }
  
  private void init(TaskContext context, Connection con) throws Exception {
    if (collections==null) {
      collections = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
      
      SqlQInfo info = getSqlQInfo();
      info.setTableSuffix("COLLECTION");
      SqlQuery q = new SqlQuery();
      q.query(context, con, info, new SqlRowHandler() {
        @Override
        public void handleSqlRow(TaskContext context, Connection con, ResultSet rs, long rowNum) throws Exception {
          String coluuid = rs.getString("COLUUID");
          String shortName = rs.getString("SHORTNAME");
          collections.put(coluuid, shortName);
        }
      });
    }
  }
}
