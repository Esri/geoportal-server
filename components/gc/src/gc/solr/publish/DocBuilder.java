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
import gc.base.task.TaskContext;
import gc.base.task.TaskStats;
import gc.base.xml.DomUtil;
import gc.base.xml.XsltReference;
import gc.base.xml.XsltTemplate;
import gc.base.xml.XsltTemplates;
import gc.base.xmltypes.XmlInterrogator;
import gc.base.xmltypes.XmlType;
import gc.base.xmltypes.XmlTypes;

import java.util.Date;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;

public class DocBuilder {
	
	public DocBuilder() {}
	
	public void addField(SolrInputDocument doc, String name, String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() > 0) {
				doc.addField(name,value);
			}
		}
	}
	
	public void append(TaskContext context, SolrInputDocument doc, 
			DocInfo info, boolean isStart, boolean isEnd) {
		
		if (isStart) {
			// id fields
		  this.setField(doc,FieldConstants.Id,info.Id);
		  this.setField(doc,FieldConstants.Id_Table,info.Id_Table);
		
	    // metadata URL
		  this.setField(doc,FieldConstants.Url_Metadata,info.Url_Metadata);
	  }
		if (!isEnd) return;
		
		// metadata type fields
		this.setField(doc,FieldConstants.MetadataType_Key,info.MetadataType_Key);
		this.setField(doc,FieldConstants.MetadataType_Identifier,info.MetadataType_Identifier);
		this.setField(doc,FieldConstants.MetadataType_Indexables_Version,info.MetadataType_Indexables_Version);

		// owner fields
		//this.setField(doc,FieldConstants.Owner_Dn,info.Owner_Dn);
		this.setField(doc,FieldConstants.Owner_Username,info.Owner_Username);
		
	  // src fields (harvesting source)
		setField(doc,FieldConstants.Src_Item_Http_ContentType,info.Src_Item_Http_ContentType);
		setField(doc,FieldConstants.Src_Item_Http_ForeignStamp,info.Src_Item_Http_ForeignStamp);
		setField(doc,FieldConstants.Src_Item_Http_LastModified,info.Src_Item_Http_LastModified);
		setField(doc,FieldConstants.Src_Item_LastModified,info.Src_Item_LastModified);
		setField(doc,FieldConstants.Src_Item_Uri,info.Src_Item_Uri);
		setField(doc,FieldConstants.Src_Item_Url,info.Src_Item_Url);
		setField(doc,FieldConstants.Src_Site_Id,info.Src_Site_Id);
	    setField(doc,FieldConstants.Src_Site_Name,info.Src_Site_Name);
		setField(doc,FieldConstants.Src_Site_Protocol,info.Src_Site_Protocol);
		setField(doc,FieldConstants.Src_Site_Url,info.Src_Site_Url);
		
	  // sync fields
		setField(doc,FieldConstants.Sync_Foreign_Id,info.Sync_Foreign_Id);
		setField(doc,FieldConstants.Sync_Foreign_InstanceId,info.Sync_Foreign_InstanceId);
	  setField(doc,FieldConstants.Sync_Foreign_InstanceUrl,info.Sync_Foreign_InstanceUrl);
		setField(doc,FieldConstants.Sync_Foreign_Stamp,info.Sync_Foreign_Stamp);
		setField(doc,FieldConstants.Sync_Type,info.Sync_Type);
		
		// task fields
		this.setField(doc,FieldConstants.Task_Id,context.getTaskID());
		this.setField(doc,FieldConstants.Task_IndexDate,new Date(System.currentTimeMillis()));
	}
	
	public void prepare(TaskContext context, XmlTypes xmlTypes, 
			SolrInputDocument doc, DocInfo info) throws Exception {
		TaskStats stats = context.getStats();
		String tn = context.getTaskName()+".prepare";
		
		// make the metadata document
		long t1 = System.currentTimeMillis();
		Document dom = DomUtil.makeDom(info.Xml_Metadata,true);
		long t2 = System.currentTimeMillis();
		stats.incrementTime(tn+".makeXmlDom",t2-t1);
		
		// interrogate the metadata document
		XmlInterrogator interrogator = new XmlInterrogator();
		XmlType xmlType = interrogator.interrogate(xmlTypes,dom);
		XsltReference xsltReference = xmlType.getToSolrXslt();
		XsltTemplate xsltTemplate = XsltTemplates.getCompiledTemplate(xsltReference.getSrc());
		info.MetadataType_Key = xmlType.getKey();
		info.MetadataType_Identifier = xmlType.getIdentifier();
		info.MetadataType_Indexables_Version = xsltReference.getVersion();
		long t3 = System.currentTimeMillis();
		stats.incrementTime(tn+".interrogateXmlType",t3-t2);
		
		// append fields at the top of the document
		this.append(context,doc,info,true,false);
		
	  // transform the metadata document to a Solr document
		// (this appends fields from the transform)
		SolrDocSaxHandler saxHandler = new SolrDocSaxHandler(doc);
		DOMSource source = new DOMSource(dom);
		SAXResult result = new SAXResult(saxHandler);
		xsltTemplate.transform(source,result,null);
		long t4 = System.currentTimeMillis();
		stats.incrementTime(tn+".transformToSolrDoc",t4-t3);
		
		// append remaining fields
		this.append(context,doc,info,false,true);
		
	}
	
	public void setField(SolrInputDocument doc, String name, Object value) {
		if (value != null) {
			doc.setField(name,value);
		}
	}
	
	public void setField(SolrInputDocument doc, String name, String value) {
		if (value != null) {
			value = value.trim();
			if (value.length() > 0) {
				doc.setField(name,value);
			}
		}
	}

}
