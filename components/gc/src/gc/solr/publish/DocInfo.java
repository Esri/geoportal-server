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
import java.util.Date;

public class DocInfo {
		
	public String Id;
	public String Id_Table;
	
	public String MetadataType_Key;
	public String MetadataType_Identifier;
	public String MetadataType_Indexables_Version;

	public String Owner_Dn;
	public String Owner_Username;
	
	public String Src_Item_Http_ContentType;
	public String Src_Item_Http_ForeignStamp;
	public String Src_Item_Http_LastModified;
	public Date   Src_Item_LastModified;
	public String Src_Item_Uri;
	public String Src_Item_Url;
	public String Src_Site_Id;
	public String Src_Site_Name;
	public String Src_Site_Protocol;
	public String Src_Site_Url;
	
	public String Sync_Foreign_Id;
	public String Sync_Foreign_InstanceId;
	public String Sync_Foreign_InstanceUrl;
	public String Sync_Foreign_Stamp;
	public String Sync_Type;
	
	public String Url_Metadata;
	
	public String Xml_Metadata;
	
	public DocInfo() {}

}
