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
import java.io.IOException;

import gc.base.task.TaskContext;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class DocPublisher {

	private int         commitWithinMillis = 5000;
	private TaskContext context;
	private String      solrCollectionUrl;
	private int         numThreads = 1;
	private int         queueSize = 1;
	private SolrServer  updateServer;
	
	public DocPublisher(TaskContext context, String solrCollectionUrl) {
		this.context = context;
		this.solrCollectionUrl = solrCollectionUrl;
	}
	
	public DocPublisher(TaskContext context, String solrCollectionUrl, 
			int commitWithinMillis) {
		this.context = context;
		this.solrCollectionUrl = solrCollectionUrl;
		this.commitWithinMillis = commitWithinMillis;
	}
	
	public DocPublisher(TaskContext context, String solrCollectionUrl,
			int commitWithinMillis, int queueSize, int numThreads) {
		this.context = context;
		this.solrCollectionUrl = solrCollectionUrl;
		this.commitWithinMillis = commitWithinMillis;
		this.queueSize = queueSize;
		this.numThreads = numThreads;
	}
	
	public void commit() throws IOException, SolrServerException {
		this.updateServer.commit();
	}
	
	public void updateDoc(SolrInputDocument doc) throws SolrServerException, IOException {
		if (this.commitWithinMillis <= 0) {
			this.updateServer.add(doc);
		} else {
			this.updateServer.add(doc,this.commitWithinMillis);
		}
	}
	
	public void shutdown() {
		this.updateServer.shutdown();
		//System.err.println(".......... after docPublisher.shutdown() ..........");
	}
	
	public void startup() {
		if (this.updateServer == null) {
		  //this.updateServer = new ConcurrentUpdateSolrServer(solrCollectionUrl,queueSize,numThreads);
		  if ((queueSize == 1) && (numThreads == 1)) {
		  	this.updateServer = new HttpSolrServer(solrCollectionUrl);
		  } else {
			  this.updateServer = new CUServer(solrCollectionUrl,queueSize,numThreads);
		  }
		}
	}
	
	public SolrServer getUpdateServer() {
		return this.updateServer;
	}
	
}
