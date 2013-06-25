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
package gc.solr.publish;

public class FieldConstants {
			
	public static final String Id = "id";
	public static final String Id_Table = "id.table_s";
	
	public static final String Links = "links";
	
	public static final String MetadataType_Key = "sys.metadatatype.key_s";
	public static final String MetadataType_Identifier = "sys.metadatatype.identifier_s";
	public static final String MetadataType_Indexables_Version = "sys.metadatatype.indexables.version_s";
	
	public static final String Owner_Dn = "sys.owner.dn_s";
	public static final String Owner_Username = "sys.owner.username_s";
	
	public static final String Src_Item_Http_ContentType = "sys.src.http.contenttype_ss";
	public static final String Src_Item_Http_ForeignStamp = "sys.src.item.http.foreignstamp_s";
	public static final String Src_Item_Http_LastModified = "sys.src.item.http.lastmodified_s";
	public static final String Src_Item_LastModified = "sys.src.item.lastmodified_tdt";
	public static final String Src_Item_Uri = "sys.src.item.uri_s";
	public static final String Src_Item_Url = "sys.src.item.url_s";
	
	public static final String Src_Site_Id = "sys.src.site.id_s";
	public static final String Src_Site_Name = "sys.src.site.name_s";
	public static final String Src_Site_Protocol = "sys.src.site.protocol_s";
	public static final String Src_Site_Url = "sys.src.site_url_s";
	
	public static final String Sync_Foreign_Id = "sys.sync.foreign.id_s";
	public static final String Sync_Foreign_InstanceId = "sys.sync.foreign.instance.id_s";
	public static final String Sync_Foreign_InstanceUrl = "sys.sync.foreign.instance.url_s";
	public static final String Sync_Foreign_Stamp = "sys.sync.foreign.stamp_s";
  public static final String Sync_Type = "sys.sync.type_s";
	
	public static final String Sys_Tags = "sys.tags_ss";
	
	public static final String Task_Id = "sys.task.id_s";
	public static final String Task_IndexDate = "sys.task.indexdate_tdt";
	
	public static final String Url_Metadata = "url.metadata_s";
	
	public static final String Val_Id_Table_DocIndex = "table.docindex";
	public static final String Val_Src_Site_Protocol_Waf = "src.site.protocol.waf";
	public static final String Val_Sync_Type_Gptdb2Solr = "sync.type.gptdb2solr";

}
