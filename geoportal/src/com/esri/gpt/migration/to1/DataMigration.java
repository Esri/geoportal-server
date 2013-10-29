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
package com.esri.gpt.migration.to1;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.sql.DatabaseReference;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * 
 * A class to migrate data from Geoportal 9.3.x to 10 database.
 */
public class DataMigration {

	// class variables
	private static Logger LOGGER = Logger
			.getLogger(DataMigration.class.getName());

	// instance variables
	private int userCount = 0;

	/**
	 * Invokes data migration for different database tables
	 * 
	 * @param context
	 *          the RequestContext
	 * @param out
	 *          the PrintWriter
	 * @throws Exception
	 */
	public void migrateData(RequestContext context, PrintWriter out)
			throws Exception {
		DatabaseReference fromDbRef = null;
		Connection fromConn = null;
		try {
			ServletRequest request = context.getServletRequest();
			fromDbRef = new DatabaseReference();
			fromDbRef.setReferenceName("gpt");
			fromDbRef.setDirectDriverClassName(chkStr(request
					.getParameter("jdbcDriver")));
			fromDbRef.setDirectUrl(chkStr(request.getParameter("jdbcUrl")));
			fromDbRef.setDirectUsername(chkStr(request.getParameter("dbUserName")));
			fromDbRef.setDirectPassword(chkStr(request.getParameter("dbPassword")));
			
			fromDbRef.setIsJndiBased(false);

			HashMap<String, Object> parameters = new HashMap<String, Object>();
			
			String dbSchema = chkStr(request.getParameter("dbSchemaName"));
			String tablePrefix = chkStr(request.getParameter("tablePrefix"));
			if(dbSchema.length() > 0){
				tablePrefix = dbSchema + "." + tablePrefix;
			}
			parameters.put("fromTablePrefix",tablePrefix);			
			parameters.put("version",
					chkStr(request.getParameter("geoportalVersion")));
			parameters.put("metadataServer", chkStr(request
					.getParameter("useMetadataServer")));
			parameters.put("toTablePrefix", context.getCatalogConfiguration()
					.getTablePrefix());
			parameters.put("metaDataTableName", chkStr(request
					.getParameter("metaDataTableName")));
			parameters.put("geoportalUserName", chkStr(request
					.getParameter("geoportalUserName")));
			parameters.put("geoportalPassword", chkStr(request
					.getParameter("geoportalPassword")));
			parameters.put("serviceUrl", chkStr(request.getParameter("serviceUrl")));
			parameters.put("context", context);


			ManagedConnection mc = returnConnection(context);
			Connection toConn = mc.getJdbcConnection();
			boolean autoCommit = toConn.getAutoCommit();
			toConn.setAutoCommit(false);
			fromConn = fromDbRef.openConnection();
			/*
			 * IClobMutator toCM = mc.getClobMutator(); parameters.put("toCM", toCM);
			 */

			String dbType = findDbType(fromConn);
			boolean fromClob = isClobData(fromConn);

			parameters.put("toConn", toConn);
			parameters.put("fromConn", fromConn);

			parameters.put("fromClob", fromClob);
			parameters.put("dbType", dbType);

			out
					.write("<img  style=\"z-index:1; background: #450200 url(../skins/themes/red/images/banner.jpg) no-repeat scroll 0pt 0px;height: 65px;position: relative;width:100%;\"/>");
			out
					.write("<div style=\"z-index:2; position: absolute;top: 10px;left: 10px;font-size: 2em;font-family: 'Trebuchet MS',Helvetica,Arial,Geneva,sans-serif;color: #FFF;text-decoration: none !important;\">Geoportal 10 - Data Migration</div>");

			out.write("<br>========== Migration Status =========");
			out.write("<br>Data Migration Started at ="
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar
							.getInstance().getTime()));

			out.write("<br>Migrating user table data...");
			migrateUsersData(parameters, out);
			out.write("<br>" + userCount + " records migrated from user table");

			out.write("<br>Migrating Harvesting_History table data...");
			int harvestingHistoryCount = migrateHarvestHistoryData(parameters, out);
			out.write("<br>" + harvestingHistoryCount
					+ " records migrated from harvesting_history table");

			out.write("<br>Migrating Search table data...");
			int searchCount = migrateSearchData(parameters, out);
			out.write("<br>" + searchCount + " records migrated from search table");

			out.write("<br>Migrating Harvesting table data...");
			int harvestingCount = migrateHarvestData(parameters, out);
			out.write("<br>" + harvestingCount
					+ " records migrated from harvesting table");

			out.write("<br>Migrating admin and Metadata table data...");
			int adminCount = migrateMetadata(parameters, out);
			out.write("<br>" + adminCount
					+ " records migrated from admin and metadata table");

			out.write("<br>========== Migration Summary =========");
			out.write("<br>Total metadata record migrated ="
					+ (adminCount + harvestingCount));
			out.write("<br>Total search table record migrated =" + searchCount);
			out.write("<br>Total user table record migrated =" + userCount);
			out.write("<br>Data Migration Completed at ="
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar
							.getInstance().getTime()));

			if (toConn != null) {
				toConn.setAutoCommit(autoCommit);
			}
		} finally {
			fromDbRef.closeConnection(fromConn);
		}
	}

	/**
	 * Migrates user data
	 * 
	 * @param the
	 *          parameters map
	 * @param the
	 *          print writer
	 * @return the number of records migrated
	 */
	private void migrateUsersData(HashMap<String, Object> parameters,
			PrintWriter out) throws Exception {
		PreparedStatement st = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		Connection toConn = (Connection) parameters.get("toConn");
		Connection fromConn = (Connection) parameters.get("fromConn");
		String toTablePrefix = (String) parameters.get("toTablePrefix");
		String fromTablePrefix = (String) parameters.get("fromTablePrefix");	
		int total = 0;
		try {
			total = countMigrationRecords(fromConn, fromTablePrefix, "user");

			StringBuffer query = new StringBuffer();
			query.append("select * from ").append(fromTablePrefix).append("user");

			st = fromConn.prepareStatement(query.toString());
			rst = st.executeQuery();

			/* iterate through results */
			while (rst.next()) {
				String dn = rst.getString("dn");
				if (!exists(toConn, dn, toTablePrefix + "user", "dn", "String")) {
					stmt = toConn.prepareStatement(" insert into " + toTablePrefix
							+ "user (dn,username) values (?,?)");
					stmt.setString(1, rst.getString("dn"));
					stmt.setString(2, rst.getString("username"));
					stmt.execute();
					toConn.commit();
					closeStatement(stmt);
					stmt = null;
					userCount += 1;
					writeStatusDiv(out, userCount, total, "user");
				}
			}
		} catch (Exception e) {
			out.write("<BR>Error occured while migrating data to " + toTablePrefix
					+ "user table. " + e.getMessage());
			LOGGER.severe(e.getMessage());
		} finally {
			closeResultSet(rst);
			closeStatement(st);
			st = null;
			closeStatement(stmt);
		}
	}

	/**
	 * Migrates search data
	 * 
	 * @param the
	 *          parameters map
	 * @param the
	 *          print writer
	 * @return the number of records migrated
	 */
	private int migrateSearchData(HashMap<String, Object> parameters,
			PrintWriter out) throws Exception {
		PreparedStatement st = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		int searchCount = 0;
		int total = 0;
		Connection toConn = (Connection) parameters.get("toConn");
		Connection fromConn = (Connection) parameters.get("fromConn");
		String toTablePrefix = (String) parameters.get("toTablePrefix");
		String fromTablePrefix = (String) parameters.get("fromTablePrefix");
		boolean isFromClob = (Boolean) parameters.get("fromClob");
		try {

			total = countMigrationRecords(fromConn, fromTablePrefix, "search");

			StringBuffer query = new StringBuffer();
			query.append("select * from ").append(fromTablePrefix).append("search");

			st = fromConn.prepareStatement(query.toString());
			rst = st.executeQuery();

			/* iterate through results */
			while (rst.next()) {

				String uuid = rst.getString("uuid");
				if (!exists(toConn, uuid, toTablePrefix + "search", "uuid", "String")) {
					stmt = toConn.prepareStatement(" insert into " + toTablePrefix
							+ "search (uuid,name,userid,criteria) values (?,?,?,?)");

					stmt.setString(1, uuid);
					stmt.setString(2, rst.getString("name"));
					stmt.setInt(3, getNewUserId(rst.getInt("userid"), fromTablePrefix,
							fromConn, toTablePrefix, toConn));

					if (isFromClob) {
						stmt.setString(4, mutateClob(rst, "criteria"));
					} else {
						stmt.setString(4, rst.getString("criteria"));
					}

					stmt.execute();
					toConn.commit();
					closeStatement(stmt);
					stmt = null;
					searchCount += 1;
					writeStatusDiv(out, searchCount, total, "search");
				}
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			out.write("<BR>Error occured while migrating data to " + toTablePrefix
					+ "search table. " + e.getMessage());
			e.printStackTrace();
		} finally {
			closeResultSet(rst);
			closeStatement(st);
			closeStatement(stmt);
		}
		return searchCount;
	}
	/**
	 * Migrates harvesting data
	 * 
	 * @param the
	 *          parameters map
	 * @param the
	 *          print writer
	 * @return the number of records migrated
	 */
	private int migrateHarvestData(HashMap<String, Object> parameters,
			PrintWriter out) throws Exception {

		PreparedStatement st = null;
		PreparedStatement stmt = null;
		PreparedStatement pstmt = null;
		PreparedStatement selectIdStmt = null;
		ResultSet rst = null;
		int harvestingCount = 0;
		int total = 0;
		/*HttpServletRequest request = (HttpServletRequest) ((RequestContext) parameters
				.get("context")).getServletRequest();
		// String ctxPath = request.getContextPath();
		int port = request.getServerPort();
		String serverName = request.getServerName();
		String csw2zEndPoint = "http://"
				+ serverName
				+ ":"
				+ port
				+ "/csw2z/discovery?request=GetCapabilities&service=CSW&version=2.0.2&x-target=";*/
		Connection toConn = (Connection) parameters.get("toConn");
		Connection fromConn = (Connection) parameters.get("fromConn");
		String toTablePrefix = (String) parameters.get("toTablePrefix");
		String fromTablePrefix = (String) parameters.get("fromTablePrefix");
		String uuid = "";
		try {
			total = countMigrationRecords(fromConn, fromTablePrefix, "harvesting");

			StringBuffer query = new StringBuffer();
			query
					.append("select uuid,name,userid,update_date,input_date,host_url,protocol_type,protocol,frequency,send_notification from "
							+ fromTablePrefix + "harvesting");

			st = fromConn.prepareStatement(query.toString());
			rst = st.executeQuery();

			/* iterate through results */
			while (rst.next()) {
				uuid = rst.getString("uuid");

				if (!exists(toConn, uuid, toTablePrefix + "resource", "docuuid",
						"String")) {

					pstmt = toConn
							.prepareStatement("insert into "
									+ toTablePrefix
									+ "resource (docuuid,title,owner,updatedate,inputdate,host_url,protocol_type,protocol,frequency,send_notification,approvalstatus,pubmethod) values (?,?,?,?,?,?,?,?,?,?,?,?)");

					stmt = toConn.prepareStatement("insert into " + toTablePrefix
							+ "resource_data (docuuid,id,xml) values(?,?,?)");

					pstmt.setString(1, uuid);
					pstmt.setString(2, rst.getString("name"));
					pstmt.setInt(3, getNewUserId(rst.getInt("userid"), fromTablePrefix,
							fromConn, toTablePrefix, toConn));
					pstmt.setDate(4, rst.getDate("update_date"));
					pstmt.setDate(5, rst.getDate("input_date"));

					String hostUrl = rst.getString("host_url");
					String protocolType = rst.getString("protocol_type");
					String protocol = rst.getString("protocol");
					String newProtocolType = protocolType;
					if (newProtocolType != null
							&& newProtocolType.equalsIgnoreCase("z3950")) {
						continue;
						/*newProtocolType = "csw";
						String s[] = z2cswProtocol(protocol);
						if (s != null && s.length == 3) {
							hostUrl = csw2zEndPoint + hostUrl + ":" + s[0] + "/" + s[1];
							protocol = s[2];
						}*/
					}

					pstmt.setString(6, hostUrl);
					pstmt.setString(7, newProtocolType);
					pstmt.setString(8, protocol);

					pstmt.setString(9, rst.getString("frequency"));
					pstmt.setString(10, rst.getString("send_notification"));
					pstmt.setString(11, "approved");
					pstmt.setString(12, "Registration");
					pstmt.execute();

					closeStatement(pstmt);
					pstmt = null;

					// selects id value (a sequence) from resource table for
					// current
					// record
					selectIdStmt = toConn.prepareStatement("select id from "
							+ toTablePrefix + "resource where docuuid=?");
					selectIdStmt.setString(1, uuid);
					ResultSet rst2 = selectIdStmt.executeQuery();
					if (rst2.next()) {
						stmt.setInt(2, rst2.getInt("id"));
					}
					closeStatement(selectIdStmt);
					selectIdStmt = null;

					stmt.setString(1, uuid);
					stmt.setString(3, makeResourceXml(rst.getString("name"), hostUrl,
							protocolType));

					stmt.execute();

					closeStatement(stmt);
					closeStatement(pstmt);
					stmt = null;
					pstmt = null;
					toConn.commit();
					harvestingCount += 1;
					writeStatusDiv(out, harvestingCount, total, "resource");
				}
			}

		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			out.write("<BR>Error occured while migrating data to " + toTablePrefix
					+ "harvesting table. " + e.getMessage());
			e.printStackTrace();
		} finally {
			closeResultSet(rst);
			closeStatement(st);
			st = null;
			closeStatement(stmt);
			closeStatement(pstmt);
			closeStatement(selectIdStmt);
		}
		return harvestingCount;
	}

	/**
	 * Migrates harvest history data
	 * 
	 * @param the
	 *          parameters map
	 * @param the
	 *          print writer
	 * @return the number of records migrated
	 */
	private int migrateHarvestHistoryData(HashMap<String, Object> parameters,
			PrintWriter out) throws Exception {
		PreparedStatement st = null;
		PreparedStatement stmt = null;
		ResultSet rst = null;
		Connection toConn = (Connection) parameters.get("toConn");
		Connection fromConn = (Connection) parameters.get("fromConn");
		String toTablePrefix = (String) parameters.get("toTablePrefix");
		String fromTablePrefix = (String) parameters.get("fromTablePrefix");
		String uuid = "";
		int harvestingHistoryCount = 0;
		int total = 0;
		boolean isFromClob = (Boolean) parameters.get("fromClob");
		try {

			total = countMigrationRecords(fromConn, fromTablePrefix,
					"harvesting_history");

			StringBuffer query = new StringBuffer();
			query.append("select * from ").append(fromTablePrefix).append(
					"harvesting_history");

			st = fromConn.prepareStatement(query.toString());
			rst = st.executeQuery();

			/* iterate through results */
			while (rst.next()) {
				uuid = rst.getString("uuid");
				if (!exists(toConn, uuid, toTablePrefix + "harvesting_history", "uuid",
						"String")) {

					stmt = toConn
							.prepareStatement(" insert into "
									+ toTablePrefix
									+ "harvesting_history (uuid,harvest_id,harvest_date,harvested_count,validated_count,published_count,harvest_report) values (?,?,?,?,?,?,?)");

					stmt.setString(1, uuid);
					stmt.setString(2, rst.getString("harvest_id"));
					stmt.setDate(3, rst.getDate("harvest_date"));
					stmt.setInt(4, rst.getInt("harvested_count"));
					stmt.setInt(5, rst.getInt("validated_count"));
					stmt.setInt(6, rst.getInt("published_count"));

					if (isFromClob) {
						stmt.setString(7, mutateClob(rst, "harvest_report"));
					} else {
						stmt.setString(7, rst.getString("harvest_report"));
					}

					stmt.execute();
					toConn.commit();
					closeStatement(stmt);
					stmt = null;
					harvestingHistoryCount += 1;
					writeStatusDiv(out, harvestingHistoryCount, total,
							"harvesting_history");
				}
			}

		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			out.write("<BR>Error occured while migrating data to " + toTablePrefix
					+ "harvesting_history table. " + e.getMessage());
		} finally {
			closeResultSet(rst);
			closeStatement(st);
			closeStatement(stmt);
		}
		return harvestingHistoryCount;
	}

	/**
	 * Migrates admin and metadata table data
	 * 
	 * @param the
	 *          parameters map
	 * @param the
	 *          print writer
	 * @return the number of records migrated
	 */
	private int migrateMetadata(HashMap<String, Object> parameters,
			PrintWriter out) throws Exception {
		PreparedStatement fromPst = null;
		PreparedStatement selectZ3950 = null;
		ResultSet fromRs = null;
		ResultSet z3950Rs = null;
		int adminCount = 0;
		int total = 0;
		String docuuid = "";
		Connection toConn = (Connection) parameters.get("toConn");
		Connection fromConn = (Connection) parameters.get("fromConn");
		String toTablePrefix = (String) parameters.get("toTablePrefix");
		String fromTablePrefix = (String) parameters.get("fromTablePrefix");
		String version = (String) parameters.get("version");
		String metadataTableName = (String) parameters.get("metaDataTableName");
		boolean mds = ((String) parameters.get("metadataServer")).trim().endsWith(
				"Yes") ? true : false;
		boolean isFromClob = (Boolean) parameters.get("fromClob");
		try {
			total = countMigrationRecords(fromConn, fromTablePrefix,
					metadataTableName);

			StringBuffer query = new StringBuffer();
			query
					.append("select m.docuuid, m.datasetname, m.owner, m.updatedate, a.approvalstatus,a.pubmethod,a.siteuuid,a.sourceuri,a.fileidentifier");

			if (!version.trim().equals("93"))
				query.append(",a.acl");

			query.append(" from ").append(fromTablePrefix).append(metadataTableName)
					.append(" m,").append(fromTablePrefix).append(
							"admin a where m.docuuid = a.docuuid");

			fromPst = fromConn.prepareStatement(query.toString());
			fromRs = fromPst.executeQuery();
			
			selectZ3950 = fromConn.prepareStatement("select host_url,name from " + fromTablePrefix + "harvesting where protocol_type='z3950'",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			z3950Rs = selectZ3950.executeQuery();

			/* iterate through results */
			while (fromRs.next()) {
	
				PreparedStatement stmt = null;
				PreparedStatement pstmt = null;
				PreparedStatement selectIdStmt = null;
				try {
					boolean isFromZ3950 = false;
					String sourceUri = fromRs.getString("sourceUri");
					z3950Rs.beforeFirst();
					while(z3950Rs.next()){
						String hostUrl = z3950Rs.getString("host_url");
						String name = z3950Rs.getString("name");
						if(sourceUri.contains(hostUrl) && sourceUri.contains(name)){
							isFromZ3950 = true;
							break;
						}
					}
					
					if(isFromZ3950) continue;
					
					docuuid = fromRs.getString("docuuid");
					if (!exists(toConn, docuuid, toTablePrefix + "resource", "docuuid",
							"String")) {

						if (!version.trim().equals("93")) {
							pstmt = toConn
									.prepareStatement("insert into "
											+ toTablePrefix
											+ "resource (docuuid,title,owner,updatedate,approvalstatus,pubmethod,siteuuid,sourceuri,fileidentifier,acl) values (?,?,?,?,?,?,?,?,?,?)");
						} else {
							pstmt = toConn
									.prepareStatement("insert into "
											+ toTablePrefix
											+ "resource (docuuid,title,owner,updatedate,approvalstatus,pubmethod,siteuuid,sourceuri,fileidentifier) values (?,?,?,?,?,?,?,?,?)");
						}

						stmt = toConn.prepareStatement("insert into " + toTablePrefix
								+ "resource_data (docuuid,xml,thumbnail,id) values(?,?,?,?)");

						pstmt.setString(1, docuuid);
						pstmt.setString(2, fromRs.getString("datasetname"));

						int owner = fromRs.getInt("owner");
						String xml = "";
						if (mds) {
							xml = fetchMetadata(parameters, pstmt, docuuid, owner);
						} else {
							owner = getNewUserId(owner, fromTablePrefix, fromConn,
									toTablePrefix, toConn);
							pstmt.setInt(3, owner);

							PreparedStatement getXmlStmt = null;
							try {
								getXmlStmt = fromConn.prepareStatement("select xml from "
										+ fromTablePrefix + metadataTableName + " where docuuid=?");
								getXmlStmt.setString(1, docuuid);
								ResultSet getXmlRs = getXmlStmt.executeQuery();
								if (getXmlRs.next()) {
									if (isFromClob) {
										xml = mutateClob(getXmlRs, "xml");
									} else {
										xml = getXmlRs.getString("xml");
									}
								}
							} finally {
								closeStatement(getXmlStmt);
								getXmlStmt = null;
							}

							Timestamp ts = fromRs.getTimestamp("updatedate");
							pstmt.setTimestamp(4, new Timestamp(ts.getTime()));
						}

						pstmt.setString(5, fromRs.getString("approvalStatus"));
						pstmt.setString(6, fromRs.getString("pubmethod"));
						pstmt.setString(7, fromRs.getString("siteUuid"));
						pstmt.setString(8, sourceUri);
						pstmt.setString(9, fromRs.getString("fileIdentifier"));

						if (!version.trim().equals("93")) {
							pstmt.setString(10, fromRs.getString("acl"));
						}

						pstmt.executeUpdate();
						closeStatement(pstmt);
						pstmt = null;

						// selects id value (a sequence) from resource table for
						// current
						// record
						selectIdStmt = toConn.prepareStatement("select id from "
								+ toTablePrefix + "resource where docuuid=?");
						selectIdStmt.setString(1, docuuid);
						ResultSet rst2 = selectIdStmt.executeQuery();
						if (rst2.next()) {
							stmt.setInt(4, rst2.getInt("id"));
						}
						closeStatement(selectIdStmt);
						selectIdStmt = null;

						stmt.setString(1, docuuid);
						stmt.setString(2, xml);

						PreparedStatement getThbnlStmt = null;
						try {
							getThbnlStmt = fromConn.prepareStatement("select thumbnail from "
									+ fromTablePrefix + metadataTableName + " where docuuid=?");
							getThbnlStmt.setString(1, docuuid);
							ResultSet getThbnlRs = getThbnlStmt.executeQuery();
							if (getThbnlRs.next()) {
								stmt.setBytes(3, getThbnlRs.getBytes("thumbnail"));
							}
						} finally {
							closeStatement(getThbnlStmt);
							getThbnlStmt = null;
						}

						stmt.executeUpdate();
						toConn.commit();
						closeStatement(stmt);
						stmt = null;
						adminCount += 1;
						writeStatusDiv(out, adminCount, total, "resource");
					}
				} finally {
					closeStatement(stmt);
					closeStatement(pstmt);
					closeStatement(selectIdStmt);
					if(z3950Rs != null){
						z3950Rs.beforeFirst();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			out.write("<BR>Error occured while migrating data to " + toTablePrefix
					+ "resource and " + toTablePrefix + "resource_data table. "
					+ e.getMessage());
		} finally {
			closeStatement(fromPst);
			closeStatement(selectZ3950);
		}
		return adminCount;
	}

	/**
	 * Fetch metadata xml from Metadata server
	 * 
	 * @param parameters parameters
	 * @param pstmt prepared statement
	 * @param docuuid document UUID
	 * @param owner owner id
	 * @return metadata document
	 * @throws Exception if fetching metadata fails
	 */
	private String fetchMetadata(HashMap<String, Object> parameters,
			PreparedStatement pstmt, String docuuid, int owner)
			throws Exception {

		PreparedStatement selectIdStmt = null;
		String xml = "";
		int userid = -1;
		try {
			Connection toConn = (Connection) parameters.get("toConn");
			Connection fromConn = (Connection) parameters.get("fromConn");
			String toTablePrefix = (String) parameters.get("toTablePrefix");
			String fromTablePrefix = (String) parameters.get("fromTablePrefix");
			String metadataTableName = (String) parameters.get("metaDataTableName");

			String serviceUrl = (String) parameters.get("serviceUrl");
			String geoportalUserName = (String) parameters.get("geoportalUserName");
			String geoportalPassword = (String) parameters.get("geoportalPassword");
			RemoteGetDocumentRequest arcxmlRequest = new RemoteGetDocumentRequest(
					serviceUrl, geoportalUserName, geoportalPassword);
			arcxmlRequest.executeGet(docuuid);
			xml = arcxmlRequest.getXml();
			selectIdStmt = fromConn.prepareStatement("select username from "
					+ toTablePrefix + metadataTableName + "u where userid=?");
			selectIdStmt.setInt(1, owner);
			ResultSet metau = selectIdStmt.executeQuery();
			if (metau.next()) {
				String userName = metau.getString("username");

				PreparedStatement selectStmt = toConn.prepareStatement("select * from "
						+ toTablePrefix + "user where userName=?");
				selectStmt.setString(1, userName);
				ResultSet user = selectStmt.executeQuery();
				if (user.next()) {
					userid = user.getInt("userid");
					userid = getNewUserId(userid, fromTablePrefix, fromConn,
							toTablePrefix, toConn);
					if (!exists(toConn, userName, toTablePrefix + "user", "username",
							"String")) {
						userCount += 1;

						PreparedStatement userInsert = toConn
								.prepareStatement(" insert into " + toTablePrefix
										+ "user (dn,username) values (?,?,?)");
						userInsert.setString(1, user.getString("dn"));
						userInsert.setString(2, user.getString("username"));
						userInsert.execute();
						toConn.commit();
						closeStatement(userInsert);
						userInsert = null;
					}
				}
				closeStatement(selectStmt);
				selectStmt = null;

				if(userid == -1) throw new Exception("Userid match not found in GPT_" + metadataTableName + "u table for owner: " + owner);
				
				pstmt.setInt(3, userid);
				pstmt.setTimestamp(4, arcxmlRequest.getUpdateDate());
			}
		} finally {
			closeStatement(selectIdStmt);
			selectIdStmt = null;
		}
		return xml;
	}

	/**
	 * Gets new userid from migrated table for the matching username and dn
	 * 
	 * @param oldUserId
	 * @param fromTablePrefix
	 * @param fromConn
	 * @param toTablePrefix
	 * @param toConn
	 * @return new user id
	 * @throws SQLException
	 */
	private int getNewUserId(int oldUserId, String fromTablePrefix,
			Connection fromConn, String toTablePrefix, Connection toConn)
			throws SQLException {
		PreparedStatement fromStmt = null;
		PreparedStatement toStmt = null;
		ResultSet fromRst = null;
		ResultSet toRst = null;
		try {
			fromStmt = fromConn.prepareStatement("select username,dn from "
					+ fromTablePrefix + "user where userid=?");
			fromStmt.setInt(1, oldUserId);
			fromRst = fromStmt.executeQuery();
			if (fromRst.next()) {
				String userName = fromRst.getString("username");
				String dn = fromRst.getString("dn");
				closeStatement(fromStmt);
				fromStmt = null;
				toStmt = toConn.prepareStatement("select userid from " + toTablePrefix
						+ "user where username =? and dn =?");
				toStmt.setString(1, userName);
				toStmt.setString(2, dn);
				toRst = toStmt.executeQuery();
				if (toRst.next()) {
					return toRst.getInt("userid");
				}
			}
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		} finally {
			closeStatement(fromStmt);
			closeStatement(toStmt);
		}
		return -1;
	}

	/**
	 * Mutates clob data
	 * @param rs the result set
	 * @param fieldName the field name
	 * @return the data string
	 * @throws SQLException
	 */
	private String mutateClob(ResultSet rs, String fieldName) throws SQLException {
		Clob clob = rs.getClob(fieldName);
		return clob != null ? clob.getSubString(1, (int) clob.length()) : null;
	}
	/**
	 * Counts number of records to be migrated
	 * @param fromConn
	 * @param fromTablePrefix
	 * @param tableName
	 * @return count of migration records
	 * @throws SQLException
	 */
	private int countMigrationRecords(Connection fromConn,
			String fromTablePrefix, String tableName) throws SQLException {
		int total = 0;
		String sql = "select count(*) from " + fromTablePrefix + tableName;
		PreparedStatement fromPstCount = null;
		ResultSet fromRsCount = null;
		try {
			fromPstCount = fromConn.prepareStatement(sql);
			fromRsCount = fromPstCount.executeQuery();
			if (fromRsCount.next()) {
				total = fromRsCount.getInt(1);
			}
		} finally {
			closeStatement(fromPstCount);
		}
		return total;
	}
	/**
	 * Writes migration progress div
	 * 
	 * @param out
	 *          the PrintWriter
	 * @param cnt
	 *          the count of records
	 * @param total
	 *          the total count
	 * @param table
	 *          name
	 */
	private void writeStatusDiv(PrintWriter out, double cnt, double total,
			String table) {
		out
				.write("<div name=\"status\" id=\"status\" style=\"; position:absolute; bottom: 20; background-color:yellow;\">Number of records migrated to "
						+ table
						+ " ="
						+ cnt
						+ " / "
						+ total
						+ "("
						+ customFormat("###.#", (cnt / total) * 100) + "%)</div>");
	}

	/**
	 * Formats display of migration status
	 * 
	 * @param pattern
	 * @param value
	 * @return custom format
	 */
	private String customFormat(String pattern, double value) {
		DecimalFormat myFormatter = new DecimalFormat(pattern);
		String output = myFormatter.format(value);
		return output;
	}

	/**
	 * Makes resource xml for resource endpoints
	 * 
	 * @param title
	 * @param resourceUrl
	 * @param protocolType
	 * @return resourceXml
	 * @throws IOException
	 */
	private String makeResourceXml(String title, String resourceUrl,
			String protocolType) {
		String response = "";
		if (protocolType != null && protocolType.trim().equalsIgnoreCase("csw")) {
			HttpClientRequest client = new HttpClientRequest();
			client.setUrl(resourceUrl);
			client.setConnectionTimeMs(5000);
			client.setResponseTimeOutMs(10000);
			try {
				response = Val.chkStr(client.readResponseAsCharacters());
				if (client.getResponseInfo().getContentType().toLowerCase().contains(
						"xml")
						&& response.length() > 0) {
					return response;
				}
			} catch (IOException e) {
			} // build dc metadata
		}
		return response;
		/*StringBuffer xml = new StringBuffer();
		xml
				.append(
						"<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dcmiBox=\"http://dublincore.org/documents/2000/07/11/dcmi-box/\">")
				.append(
						"<rdf:Description rdf:about=\"" + Val.escapeXml(resourceUrl)
								+ "\">").append("<dc:title>").append(title).append(
						"</dc:title>").append("<dct:references>").append(
						Val.escapeXml(resourceUrl)).append(
						"</dct:references></rdf:Description></rdf:RDF>");
		return xml.toString();*/
	}

	/**
	 * Check if record already exists in target database
	 * 
	 * @param conn
	 *          the Connection
	 * @param value
	 *          the field value
	 * @param tableName
	 *          the table name
	 * @param field
	 *          the field name
	 * @param type
	 *          the field type
	 * @return true if exists
	 * @throws SQLException
	 */
	private boolean exists(Connection conn, String value, String tableName,
			String field, String type) throws SQLException {
		PreparedStatement selectIdStmt = null;
		try {
			selectIdStmt = conn.prepareStatement("select " + field + " from "
					+ tableName + " where " + field + "=?");
			if (type.toLowerCase().equalsIgnoreCase("string"))
				selectIdStmt.setString(1, value);
			else
				selectIdStmt.setInt(1, Integer.parseInt(value));

			ResultSet rst2 = selectIdStmt.executeQuery();

			if (rst2.next()) {
				return true;
			}
		} finally {
			closeStatement(selectIdStmt);
		}
		return false;
	}

	/**
	 * Converts z3950 protocol information to csw resource endpoint
	 * 
	 * @param protocol
	 * @return csw parameters
	 * @throws Exception
	 */
	private String[] z2cswProtocol(String protocol) throws Exception {
		String[] parts = new String[3];
		Document dom = DomUtil.makeDomFromString(protocol, false);
		NodeList port = dom.getElementsByTagName("port");
		if (port != null && port.getLength() == 1)
			parts[0] = port.item(0).getTextContent();
		NodeList db = dom.getElementsByTagName("database");
		if (db != null && db.getLength() == 1)
			parts[1] = db.item(0).getTextContent();
		parts[2] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><protocol type=\"CSW\"><username></username><password></password><profile>urn:ogc:CSW:2.0.2:HTTP:FGDC</profile></protocol>";
		return parts;
	}

	/**
	 * Checks if data field is clob or string
	 * 
	 * @param conn
	 *          the Connection
	 * @return true if clob data type exists
	 * @throws SQLException
	 */
	private boolean isClobData(Connection conn) throws SQLException {
		String database = conn.getMetaData().getDatabaseProductName().toLowerCase();

		if (database.contains("postgresql") || database.contains("microsoft")) {
			return false;
		}

		if (database.contains("oracle") || database.contains("db2")) {
			return true;
		}
		return true;
	}

	/**
	 * Checks if data field is clob or string
	 * 
	 * @param conn
	 * @return the database type
	 * @throws SQLException
	 */
	private String findDbType(Connection conn) throws SQLException {
		String database = conn.getMetaData().getDatabaseProductName().toLowerCase();

		if (database.contains("postgresql")) {
			return "pg";
		} else if (database.contains("microsoft")) {
			return "mssql";
		} else if (database.contains("oracle")) {
			return "orcl";
		}
		return "orcl";
	}

	/**
	 * Closes a statement.
	 * 
	 * @param st
	 *          the JDBC Statement to close
	 */
	private void closeStatement(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (Throwable t) {
				LogUtil.getLogger().log(Level.SEVERE, "Error closing statement.", t);
			}
		}
	}

	/**
	 * Closes result set.
	 * 
	 * @param rs
	 *          result set to close
	 */
	private void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Throwable t) {
				LogUtil.getLogger().log(Level.SEVERE, "Error closing result set.", t);
			}
		}
	}

	// Checks a string value.
	private String chkStr(String s) {
		if (s == null)
			return "";
		else
			return s.trim();
	}

	/**
	 * Returns a managed connection to the default database.
	 * 
	 * @throws SQLException
	 *           if an exception occurs while establishing the connection
	 */
	private ManagedConnection returnConnection(RequestContext context)
			throws SQLException {
		return context.getConnectionBroker().returnConnection("");
	}

}
