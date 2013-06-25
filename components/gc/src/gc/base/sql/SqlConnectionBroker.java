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
package gc.base.sql;

import gc.base.task.TaskContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * TODO: needs implementation
 */
public class SqlConnectionBroker {
	
	public Connection getConnection(TaskContext context) {
		return openConnection();
	}
	
	private static Connection openConnection() {
    //return ConMgr.openConnection("com.mysql.jdbc.Driver",
    //    "jdbc:mysql://gpt13:3306/geoportal","geoportal","geoportalpwd");
    
    //return ConMgr.openConnection("com.mysql.jdbc.Driver",
    //    "jdbc:mysql://prats:3306/geodata","root","mysql");
    
    //return ConMgr.openConnection("org.postgresql.Driver",
    //    "jdbc:postgresql://pandzelvm:5432/geoportal","geoportal","geoportalpwd");

    //return ConMgr.openConnection("com.mysql.jdbc.Driver",
    //    "jdbc:mysql://datagov:3306/geoportal","geoportal","geoportalpwd"); 
    
    //return openConnection("com.mysql.jdbc.Driver",
    //    "jdbc:mysql://datagov:3306/geoportal?useUnicode=true&characterEncoding=utf8",
    //    "geoportal","geoportalpwd");  
    
    return openConnection("com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost:3306/geoportal?useUnicode=true&characterEncoding=utf8",
        "root","adminadmin");  
  }
	
	private static Connection openConnection(String driver, String url, 
      String username, String password) {
    try {
      Class.forName(driver);
      return DriverManager.getConnection(url,username,password);
    } catch (ClassNotFoundException e) {
      String msg = "Unable to access JDBC driver: "+driver;
      throw new RuntimeException(msg,e);
    } catch (SQLException e) {
      String msg = "Unable to make JDBC connection: "+url;
      throw new RuntimeException(msg,e);
    }
  }
	
	public static Connection makeConnection(String driver, String url, 
      String username, String password) {
    try {
      Class.forName(driver);
      return DriverManager.getConnection(url,username,password);
    } catch (ClassNotFoundException e) {
      String msg = "Unable to access JDBC driver: "+driver;
      throw new RuntimeException(msg,e);
    } catch (SQLException e) {
      String msg = "Unable to make JDBC connection: "+url;
      throw new RuntimeException(msg,e);
    }
  }

}
