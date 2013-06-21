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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GPT user record (SQL database).
 */
public class GptUser extends GptRecord {
	
	/** instance variables */
	public String  key;
	public Integer userid;
	public String  username;
	public String  dn;
	
	/** Default constructor. */
	public GptUser() {
		super();
		SqlQInfo info = getSqlQInfo();
		info.setTableSuffix("USER");
		info.setFields(new String[]{"*"});
		info.setOrderBy(null);
		info.setWhere(null);
	}
	
	@Override
	public void readFields(ResultSet rs) throws SQLException {
		key = rs.getString("USERNAME");
		int n = rs.getInt("USERID"); 
		userid = rs.wasNull() ? null : new Integer(n);
		username = rs.getString("USERNAME");
		dn = rs.getString("DN");
	}
	
	@Override
	public void write(PreparedStatement pst) throws SQLException {
		String msg = "write(PreparedStatement) is not supported.";
		throw new UnsupportedOperationException(msg);
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		key = DataIoUtil.readString(in);
		userid = DataIoUtil.readInteger(in);
		username = DataIoUtil.readString(in);
		dn = DataIoUtil.readString(in);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		DataIoUtil.writeString(out,key);
		DataIoUtil.writeInteger(out,userid);
		DataIoUtil.writeString(out,username);
		DataIoUtil.writeString(out,dn);
	}
	
	/**
	 * Builds the object based upon a supplied user id.
	 * @param context the task context
	 * @param con the database connection
	 * @param userid the user id
	 * @throws Exception if an exception occurs
	 */
	public void querySqlDB(TaskContext context, Connection con, int userid) throws Exception {
		key = null;
		SqlQInfo info = getSqlQInfo();
		info.setWhere("USERID=?");
		List<Object> bindings = new ArrayList<Object>();
		bindings.add(new Integer(userid));
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
