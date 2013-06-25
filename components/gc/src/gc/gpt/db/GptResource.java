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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a GPT resource record (SQL database).
 */
public class GptResource extends GptRecord {
	
	/** instance variables */
  
	// primary key
	public String    key;
	
	// meta related
	public String    baseType; // hs hd ld (harvesting site, harvested document, local document)
	public boolean   isHarvestingSite = false;
	public boolean   isHarvestedDocument = false;
	public boolean   isLocalDocument = false;
	
	// identification related
	public String    docuuid;
	public String    fileidentifier;
	//public Integer   id;
	public String    sourceuri;
	public String    siteuuid;
	public String    title;
	
	// ownership related
	public String    acl;
	public Integer   owner;
	
	// status related
	public String    approvalstatus;
	public String    pubmethod;
	
	// timestamp related
	public Timestamp inputdate;
	public Timestamp updatedate;
	
	// harvesting site related
	public String    findable;
	public String    frequency;
	public String    host_url;
	public Timestamp lastsyncdate;
	public String    protocol;
	public String    protocol_type;
	public String    searchable;
	public String    send_notification;
	public String    synchronizable;
	
	/** Default constructor. */
	public GptResource() {
		super();
		SqlQInfo info = getSqlQInfo();
		info.setTableSuffix("RESOURCE");
		info.setFields(new String[]{"*"});
		info.setOrderBy(null);
		info.setWhere(null);
	}
	
	/**
	 * Determines the base type.
	 */
	private void determineBaseType() {
		isHarvestingSite = false;
		isHarvestedDocument = false;
		isLocalDocument = false;
		if ((protocol != null) && (protocol.length() > 0)) {
			isHarvestingSite = true;
			baseType = "hs";
		} else if ((siteuuid != null) && (siteuuid.length() > 0)) {
			isHarvestedDocument = true;
			baseType = "hd";
		} else {
			isLocalDocument = true;
			baseType = "ld";
		}
	}
	
	@Override
	public void readFields(ResultSet rs) throws SQLException {
		int n; Timestamp ts;
		key = rs.getString("DOCUUID");
		
		docuuid = rs.getString("DOCUUID");
		fileidentifier = rs.getString("FILEIDENTIFIER");
		//n = rs.getInt("ID"); 
		//id = rs.wasNull() ? null : new Integer(n);
		sourceuri = rs.getString("SOURCEURI");
		siteuuid = rs.getString("SITEUUID");
		title = rs.getString("TITLE");
		
		acl = rs.getString("ACL");
		n = rs.getInt("OWNER"); 
		owner = rs.wasNull() ? null : new Integer(n);
		
		approvalstatus = rs.getString("APPROVALSTATUS");
		pubmethod = rs.getString("PUBMETHOD");
		
		ts = rs.getTimestamp("INPUTDATE"); 
		inputdate = rs.wasNull() ? null : ts;
		ts = rs.getTimestamp("UPDATEDATE"); 
		updatedate = rs.wasNull() ? null : ts;
		
		findable = rs.getString("FINDABLE");
		frequency = rs.getString("FREQUENCY");
		host_url = rs.getString("HOST_URL");
		ts = rs.getTimestamp("LASTSYNCDATE"); 
		lastsyncdate = rs.wasNull() ? null : ts;
		protocol = rs.getString("PROTOCOL");
		protocol_type = rs.getString("PROTOCOL_TYPE");
		searchable = rs.getString("SEARCHABLE");
		send_notification = rs.getString("SEND_NOTIFICATION");
		synchronizable = rs.getString("SYNCHRONIZABLE");
		
		determineBaseType();
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
		fileidentifier = DataIoUtil.readString(in);
		//id = DataIoUtil.readInteger(in);
		sourceuri = DataIoUtil.readString(in);
		siteuuid = DataIoUtil.readString(in);
		title = DataIoUtil.readString(in);
		
		acl = DataIoUtil.readString(in);
		owner = DataIoUtil.readInteger(in);
		
		approvalstatus = DataIoUtil.readString(in);
		pubmethod = DataIoUtil.readString(in);
		
		inputdate = DataIoUtil.readTimestamp(in);
		updatedate = DataIoUtil.readTimestamp(in);
		
		findable = DataIoUtil.readString(in);
		frequency = DataIoUtil.readString(in);
		host_url = DataIoUtil.readString(in);
		lastsyncdate = DataIoUtil.readTimestamp(in);
		protocol = DataIoUtil.readString(in);
		protocol_type = DataIoUtil.readString(in);
		searchable = DataIoUtil.readString(in);
		send_notification = DataIoUtil.readString(in);
		synchronizable = DataIoUtil.readString(in);
		
		determineBaseType();
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		DataIoUtil.writeString(out,key);
		
		DataIoUtil.writeString(out,docuuid);
		DataIoUtil.writeString(out,fileidentifier);
		//DataIoUtil.writeInteger(out,id);
		DataIoUtil.writeString(out,sourceuri);
		DataIoUtil.writeString(out,siteuuid);
		DataIoUtil.writeString(out,title);
		
		DataIoUtil.writeString(out,acl);
		DataIoUtil.writeInteger(out,owner);
		
		DataIoUtil.writeString(out,approvalstatus);
		DataIoUtil.writeString(out,pubmethod);
		
		DataIoUtil.writeTimestamp(out,inputdate);
		DataIoUtil.writeTimestamp(out,updatedate);
		
		DataIoUtil.writeString(out,findable);
		DataIoUtil.writeString(out,frequency);
		DataIoUtil.writeString(out,host_url);
		DataIoUtil.writeTimestamp(out,lastsyncdate);
		DataIoUtil.writeString(out,protocol);
		DataIoUtil.writeString(out,protocol_type);
		DataIoUtil.writeString(out,searchable);
		DataIoUtil.writeString(out,send_notification);
		DataIoUtil.writeString(out,synchronizable);
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
