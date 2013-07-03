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
package gc.solr.task;
import gc.base.sql.SqlQuery;
import gc.base.sql.SqlRowHandler;
import gc.base.task.Task;
import gc.base.task.TaskContext;
import gc.base.task.TaskStats;
import gc.base.util.UuidUtil;
import gc.base.xmltypes.XmlTypes;
import gc.gpt.db.GptResource;
import gc.gpt.db.GptResourceXml;
import gc.gpt.db.GptUser;
import gc.solr.publish.DocBuilder;
import gc.solr.publish.DocInfo;
import gc.solr.publish.DocPublisher;
import gc.solr.publish.FieldConstants;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class Gptdb2SolrTask extends Task implements SqlRowHandler {
	
	/*
	 TODO
	   - collections deletes GPT_
	 */

	private boolean      approvedOnly = true;
	private boolean      docsOnly = true;
	private boolean      emptyAclOnly = false;
	private boolean      checkForDeletes = true;
	
	private DocPublisher docPublisher;
	private String       foreignInstanceId;
	private String       foreignInstanceUrl;
	private SolrServer   queryServer;
	private String       solrCollectionUrl;
	private String       syncType = FieldConstants.Val_Sync_Type_Gptdb2Solr;
	private XmlTypes     xmlTypes;
	private Gptdb2SolrInstance gptdb2SolrInstance;
	
	private Map<String,String> okIds = new HashMap<String,String>();
	private List<String> delIds = new ArrayList<String>();
	private int maxIdsInMap = 1000000;
	
	
	public Gptdb2SolrTask(TaskContext context, 
			XmlTypes xmlTypes, Gptdb2SolrInstance gptdb2SolrInstance) {
		super(context);
		this.xmlTypes = xmlTypes;
		this.gptdb2SolrInstance = gptdb2SolrInstance;
		this.foreignInstanceId = this.gptdb2SolrInstance.getGptInstanceId();
		this.foreignInstanceUrl = this.gptdb2SolrInstance.getGptInstanceUrl();
		this.solrCollectionUrl = this.gptdb2SolrInstance.getSolrCollectionUrl();
	}

	@Override
	protected void executeTask() throws Exception {
		TaskContext context = this.getContext();
		Connection con = null;
		try {
			// TODO: dbconnection, gptinstance name, gcinstancename
			queryServer = new HttpSolrServer(solrCollectionUrl);
			docPublisher = new DocPublisher(context,solrCollectionUrl,5000,10,1);
			this.docPublisher.startup();
			
			// TODO remove this
			//deleteDocs();
			//if (true) return;
			
			okIds = null;
			con = gptdb2SolrInstance.makeSqlConnection();
			GptResource r = new GptResource();
			SqlQuery q = new SqlQuery();
			q.query(context,con,r.getSqlQInfo(),this);
			//this.walkSolrDocs();
			
		} finally {
      try {if (con != null) con.close();} 
      catch (Exception ef) {ef.printStackTrace();}
			try {
				if (queryServer != null) {
				  queryServer.shutdown();
				}
			} finally {
				if (docPublisher != null) {
			    try {
			    	docPublisher.commit();
			    } finally {
			    	docPublisher.shutdown();
			    }
				}
			}
		}
	}

	@Override
	public void handleSqlRow(TaskContext context, Connection con, 
			ResultSet rs, long rowNum) throws Exception {
		try {
			this._handleSqlRow(context,con,rs,rowNum);
		} catch (Exception e) {
			TaskStats stats = context.getStats();
			String tn = context.getTaskName()+".sync";
			stats.incrementCount(tn+".exceptions");
			System.err.println(e.toString());
			//e.printStackTrace(System.err);
		}
	}
	
	private void _handleSqlRow(TaskContext context, Connection con, 
			ResultSet rs, long rowNum) throws Exception {
		TaskStats stats = context.getStats();
		String tn = context.getTaskName()+".sync";
		GptResource resource = new GptResource();
		resource.readFields(rs);

		String[] result = queryDoc(resource);
		String id = result[0];
		String fsMatched = result[1];
		if (fsMatched != null) {
			if ((okIds != null) && (okIds.size() <= this.maxIdsInMap)) {
				okIds.put(id,"");
			} else if (okIds != null) {
				okIds = null;
			}
			stats.incrementCount(tn+".noChange");
		} else {
			
			String s;
			boolean bContinue = true;
			if (bContinue && this.approvedOnly) {
				s = resource.approvalstatus;
				if (s == null) s = "";
				if (!s.equals("approved") && !s.equals("posted")) {
					stats.incrementCount(tn+".ignore.notApproved");
					bContinue = false;
				}
			}
			if (bContinue && this.emptyAclOnly) {
				s = resource.acl;
				if (s == null) s = "";
				if (s.trim().length() > 0) {
					stats.incrementCount(tn+".ignore.nonEmptyAcl");
					bContinue = false;
				}
			}
			if (bContinue && this.docsOnly) {
				if (resource.isHarvestingSite) {
					stats.incrementCount(tn+".ignore.harvestingSite");
					bContinue = false;
				}
			}
			
			GptResourceXml resourceXml = new GptResourceXml();
			if (bContinue) {
				resourceXml.querySqlDB(context,con,resource.docuuid);
				s = resourceXml.xml;
				if ((s == null) || (s.length() == 0)) {
					stats.incrementCount(tn+".ignore.noResourceXml");
					bContinue = false;
				}
			}
			
			if (bContinue) {
				if (id == null) {
				  stats.incrementCount(tn+".insertRequired");
				} else {
					stats.incrementCount(tn+".updateRequired");
				}
				
				GptUser user = new GptUser();
				user.querySqlDB(context,con,resource.owner);
				
				GptResource parentSite = null;
				if (resource.isHarvestedDocument) {
					parentSite = new GptResource();
					parentSite.querySqlDB(context,con,resource.siteuuid);
				}
	
				SolrInputDocument doc = makeDoc(id,resource,user,resourceXml,parentSite);
				//System.err.println(doc);
				updateDoc(doc);
				stats.incrementCount(tn+".solr.sent");
			}
			
			if (!bContinue && this.checkForDeletes) {
				if (id != null) {
					this.docPublisher.getUpdateServer().deleteById(id);
					stats.incrementCount(tn+".solr.sentForDelete");
				}
			}
		}
	}
	
	private SolrInputDocument makeDoc(String id, GptResource resource, GptUser user, 
			GptResourceXml resourceXml, GptResource parentSite) throws Exception {
		
		/*
	   - Collections? Acls?
	   - Parent site info in not within the foreign stamp,
	     if changed the item will not be updated
	   - Owner needs a realm?
	   - errors from ConcurrentUpdateSolrServer?
	   - store the XML? link to the XML?
	   - gpt fields?
	   - tags?
	 */
		
		TaskContext context = getContext();
		DocBuilder builder = new DocBuilder();
		DocInfo info = new DocInfo();
		SolrInputDocument doc = new SolrInputDocument();
		String s;
		
		if (id == null) id = UuidUtil.normalizeGptUuid(resource.docuuid);
	  info.Id = id;
	  info.Id_Table = FieldConstants.Val_Id_Table_DocIndex;
	  
		//info.Owner_Dn = user.dn;
		info.Owner_Username = user.username;
		
		String sItemUrl = null;
		s = resource.sourceuri;
		if ((s != null) && (s.startsWith("http:") || s.startsWith("https:") || 
				 s.startsWith("ftp:") || s.startsWith("ftps:"))) {
			sItemUrl = resource.sourceuri;
		}
	  info.Src_Item_Http_ContentType = null;
	  info.Src_Item_Http_ForeignStamp = null;
	  info.Src_Item_Http_LastModified = null;
	  info.Src_Item_LastModified = resource.updatedate;
		info.Src_Item_Uri = resource.sourceuri;
		info.Src_Item_Url = sItemUrl;
		if (parentSite != null) {
		  info.Src_Site_Id = resource.siteuuid;
		  //info.Src_Site_Name = parentSite.title;
		  info.Src_Site_Protocol = parentSite.protocol_type;
		  info.Src_Site_Url = parentSite.host_url;
		}
		
		String fs = makeForeignStamp(resource);
		info.Sync_Foreign_Id = resource.docuuid;
		info.Sync_Foreign_InstanceId = this.foreignInstanceId;
		info.Sync_Foreign_InstanceUrl = this.foreignInstanceUrl;
		info.Sync_Foreign_Stamp = fs;
		info.Sync_Type = this.syncType;
		
		if (!resource.isHarvestingSite) {
			String sMetadataUrl = null;
			s = this.foreignInstanceUrl;
			if ((s != null) && (s.length() > 0) && (!s.contains("?"))) {
				if (!s.endsWith("/")) s += "/";
				sMetadataUrl = s+"rest/document?id="+URLEncoder.encode(resource.docuuid,"UTF-8");
			}
			info.Url_Metadata = sMetadataUrl;
		}
		info.Xml_Metadata = resourceXml.xml;
		
	  builder.prepare(context, xmlTypes, doc, info);
		if (!this.approvedOnly) {
		  builder.setField(doc,"gpt.doc.approvalstatus_s",resource.approvalstatus);
		}
	  //System.err.println(doc);
		
		if ((okIds != null) && (okIds.size() <= this.maxIdsInMap)) {
			okIds.put(id,"");
		} else if (okIds != null) {
			okIds = null;
		}
		
	  return doc;
	}
	
	private String makeForeignStamp(GptResource resource) {
		String acl = resource.acl;
		if (acl == null) acl = "";
		String fs = resource.approvalstatus+
                ("."+resource.owner)+
                ("."+acl)+
                ("."+resource.updatedate.getTime());
    return fs;
	}
	
	private String[] queryDoc(GptResource resource) 
			throws SolrServerException, IOException {
		String[] result = new String[]{null,null};
		String fldId = FieldConstants.Id;
		String fldForeignStamp = FieldConstants.Sync_Foreign_Stamp;
		
		/* TODO can same id from different GPTs cause a problem? */
		
		String k = UuidUtil.normalizeGptUuid(resource.docuuid);
		String fs = makeForeignStamp(resource);
		
		String fl = fldId+","+fldForeignStamp;
		String q = fldId+":"+k;
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q",q);
		params.set("fl",fl);
		
		QueryResponse response = queryServer.query(params);
		SolrDocumentList docs = response.getResults();
		if (docs != null) {
			int nDocs = docs.size();
			if (nDocs == 1) {
				SolrDocument doc = docs.get(0);
				String sId = (String)doc.getFieldValue(fldId);
				String sFs = (String)doc.getFieldValue(fldForeignStamp);
				result[0] = sId;
				if (sFs.equals(fs)) {
					result[1] = "fsMatched";
				} 
			} else if (nDocs > 1) {
				// TODO: exception here?, fix the problem?
			} 
		}
		return result;
	}
	
	private void updateDoc(SolrInputDocument doc) throws SolrServerException, IOException {
		this.docPublisher.updateDoc(doc);
	}
	
	private void deleteDocs() throws SolrServerException, IOException {
		String q = FieldConstants.Id_Table+":"+FieldConstants.Val_Id_Table_DocIndex;
		q += " AND "+FieldConstants.Sync_Type+":"+this.syncType;
		q += " AND "+FieldConstants.Sync_Foreign_InstanceId+":"+this.foreignInstanceId;
		//q = "*:*";
		this.docPublisher.getUpdateServer().deleteByQuery(q);
		this.docPublisher.commit();
	}
	
	private void walkSolrDocs() throws SolrServerException, IOException {
		if (!checkForDeletes) return;
		if ((okIds == null) || (okIds.size() == 0)) return;
		
		TaskContext context = this.getContext();
		TaskStats stats = context.getStats();
		String tn = context.getTaskName()+".walkSolrDocs";
		stats.setString(tn,"...");
		
		String fl = FieldConstants.Id;
		String q = FieldConstants.Id_Table+":"+FieldConstants.Val_Id_Table_DocIndex;
		q += " AND "+FieldConstants.Sync_Type+":"+this.syncType;
		q += " AND "+FieldConstants.Sync_Foreign_InstanceId+":"+this.foreignInstanceId;
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q",q);
		params.set("fl",fl);
		stats.setString(tn+".q",q);
		
		boolean bContinue = true;
		long nDeepTotal = 0;
		long nFetched = 0;
		long nHits = 0;
		int nDocs = 0;
		long nStart = 0;
		int nRows = 1000;
		long nNextStart = 0;
		while (bContinue) {
			bContinue = false;
			params.set("start",""+nStart);
			params.set("rows",""+nRows);
			QueryResponse response = queryServer.query(params);
			SolrDocumentList docs = response.getResults();
			if (docs != null) {
				nHits = docs.getNumFound();
				nDocs = docs.size();
				nNextStart = nStart+nDocs;
				if ((nDocs > 0) && (nNextStart < nHits)) {
					bContinue = true;
				}
				for (int i=0;i<nDocs;i++) {
					SolrDocument doc = docs.get(i);
					String id = (String)doc.getFieldValue(FieldConstants.Id);
					nFetched++;
					stats.incrementCount(tn+".fetched");
					if (okIds.get(id) != null) {
						stats.incrementCount(tn+".idOk");
					} else {
						stats.incrementCount(tn+".idRequiresDelete");
						if ((delIds != null) && (delIds.size() <= this.maxIdsInMap)) {
							delIds.add(id);
						} else if (delIds != null) {
							delIds = null;
							bContinue = false;
							break;
						}
					}
					if ((nDeepTotal > 0) && (nFetched >= nDeepTotal)) {
						bContinue = false;
						break;
					}
				}
				nStart = nNextStart;
			}
		}
		
		if ((delIds != null) && (delIds.size() > 0)) {
			//stats.incrementCount(context.getTaskName()+".solr.sentForDelete",delIds.size());
			//this.docPublisher.getUpdateServer().deleteById(delIds);
	  }
		
	}

}
