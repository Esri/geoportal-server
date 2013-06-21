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
package gc.gpt.db;
import gc.base.sql.SqlQInfo;
import gc.base.sql.SqlQuery;
import gc.base.sql.SqlRowHandler;
import gc.base.task.TaskContext;
import gc.base.util.DataIoUtil;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GPT resource XML record (SQL database).
 */
public class GptResourceXml extends GptRecord {
	
	/** instance variables */
	public String key;
	public String docuuid;
	public String xml;
	
	/** Default constructor. */
	public GptResourceXml() {
		super();
		SqlQInfo info = this.getSqlQInfo();
		info.setTableSuffix("RESOURCE_DATA");
		info.setFields(new String[]{"DOCUUID,XML"});
		info.setOrderBy(null);
		info.setWhere(null);
		info.setFetchSize(1);
	}
	
	@Override
	public void readFields(ResultSet rs) throws SQLException {
		key = rs.getString("DOCUUID");
		docuuid = rs.getString("DOCUUID");
		
		// TODO: clob mutator how to determine the data base 
		
		//ResultSetMetaData rsmd = rs.getMetaData();
		//int ct = rsmd.getColumnType(2);
		//String cn = rsmd.getColumnName(2);
		//System.err.println("************************ "+cn+" "+ct);
		//boolean useClob = (rs.getMetaData().getColumnType(2) == java.sql.Types.CLOB);
		
		boolean useClob = true;
		if (useClob) {
			xml = null;
			try {
			  Clob clob = rs.getClob("XML");
			  if (clob != null) {
			  	int n = (int)clob.length();
			  	if (n > 0) xml = clob.getSubString(1,n);
			  }
			} catch (Exception e) {
				xml = rs.getString("XML");
			}
		} else {
		  xml = rs.getString("XML");
		}
	}
	
	@Override
	public void write(PreparedStatement pst) throws SQLException {
		String msg = "write(PreparedStatement) is not supported.";
		throw new UnsupportedOperationException(msg);
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		key = DataIoUtil.readString(in);
		docuuid = DataIoUtil.readString(in);
		xml = DataIoUtil.readString(in);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		DataIoUtil.writeString(out,key);
		DataIoUtil.writeString(out,docuuid);
		DataIoUtil.writeString(out,xml);
	}
	
	/**
	 * Builds the object based upon a supplied document uuid.
	 * @param context the task context
	 * @param con the database connection
	 * @param docuuid the document uuid
	 * @throws Exception if an exception occurs
	 */
	public void querySqlDB(TaskContext context, Connection con, String docuuid) throws Exception {
		key = null;
		xml = null;
		SqlQInfo info = getSqlQInfo();
		info.setWhere("DOCUUID=?");
		List<Object> bindings = new ArrayList<Object>();
		bindings.add(docuuid);
		info.setQueryBindings(bindings);
		SqlQuery q = new SqlQuery();
		q.query(context,con,info, new SqlRowHandler() {
			@Override
			public void handleSqlRow(TaskContext context, Connection con, ResultSet rs,
					long rowNum) throws Exception {
				readFields(rs);
			}
		});
	}

}
