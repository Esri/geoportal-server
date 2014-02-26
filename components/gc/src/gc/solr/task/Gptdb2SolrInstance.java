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
import gc.base.sql.SqlConnectionBroker;
import gc.base.util.ValueUtil;
import gc.base.xml.DomUtil;
import gc.base.xml.XsltReference;
import gc.base.xmltypes.XmlInterrogationInfo;
import gc.base.xmltypes.XmlType;
import gc.base.xmltypes.XmlTypes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Gptdb2SolrInstance {
	
	private String gptInstanceId;
	private String gptInstanceUrl;
    private boolean gptIncludeCollections;
	private String solrCollectionUrl;
	
	private String sqlDriver;
	private String sqlUrl;
	private String sqlUsername;
	private String sqlPassword;
	
	public Gptdb2SolrInstance() {}
	
	public String getGptInstanceId() {
		return this.gptInstanceId;
	}
	public void setGptInstanceId(String val) {
		this.gptInstanceId = val;
	}
	
	public String getGptInstanceUrl() {
		return this.gptInstanceUrl;
	}
	public void setGptInstanceUrl(String val) {
		this.gptInstanceUrl = val;
	}
	
	public String getSolrCollectionUrl() {
		return this.solrCollectionUrl;
	}
	public void setSolrCollectionUrl(String val) {
		this.solrCollectionUrl = val;
	}
	
	public Connection makeSqlConnection() {
		return SqlConnectionBroker.makeConnection(sqlDriver,sqlUrl,sqlUsername,sqlPassword);
	}

    public boolean isGptIncludeCollections() {
      return gptIncludeCollections;
    }

    public void setGptIncludeCollections(boolean gptIncludeCollections) {
      this.gptIncludeCollections = gptIncludeCollections;
    }
	
	private void putSqlConInfo(String driver, String url, String username, String password) {
		this.sqlDriver = driver;
		this.sqlUrl = url;
		this.sqlUsername = username;
		this.sqlPassword = password;
	}
	
	public static Gptdb2SolrInstance createFromConfigNode(Node instanceIdNode) 
			throws XPathExpressionException {
		Gptdb2SolrInstance instance = new Gptdb2SolrInstance();
  	XPath xpath = XPathFactory.newInstance().newXPath();
  	Node ndI = instanceIdNode;
  	
  	instance.setGptInstanceId(ValueUtil.trim(
  			xpath.evaluate("@value",ndI)));
  	instance.setGptInstanceUrl(ValueUtil.trim(
    		xpath.evaluate("property[@name='gpt.instance.url']/@value",ndI)));
  	instance.setGptIncludeCollections("true".equalsIgnoreCase(ValueUtil.trim(
    		xpath.evaluate("property[@name='gpt.include.collections']/@value",ndI))));
  	instance.setSolrCollectionUrl(ValueUtil.trim(
    		xpath.evaluate("property[@name='solr.collection.url']/@value",ndI)));
  	
  	Node ndD = (Node)xpath.evaluate("property[@name='sql.database']",ndI,XPathConstants.NODE);
    String driver = xpath.evaluate("property[@name='driver']/@value",ndD);
    String url = xpath.evaluate("property[@name='url']/@value",ndD);
    String username = xpath.evaluate("property[@name='username']/@value",ndD);
    String password = xpath.evaluate("property[@name='password']/@value",ndD);
    instance.putSqlConInfo(driver,url,username,password);
    
    return instance;
	}
	
  public static List<Gptdb2SolrInstance> createInstancesFromConfig() throws Exception {
		String configPath = "gc-config/gptdb2solr.xml";
		Document dom = DomUtil.makeDomFromResourcePath(configPath,true);
		List<Gptdb2SolrInstance> instances = new ArrayList<Gptdb2SolrInstance>(); 
  	XPath xpath = XPathFactory.newInstance().newXPath();
	  NodeList nl = (NodeList)xpath.evaluate("//property[@name='gptdb2solr.instance.id']",
	  		dom,XPathConstants.NODESET);
	  for (int i=0; i<nl.getLength();i++) {
	  	Gptdb2SolrInstance instance = Gptdb2SolrInstance.createFromConfigNode(nl.item(i));
	  	if (instance != null) instances.add(instance);
	  }
	  return instances;
	}

}
