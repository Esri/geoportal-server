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

import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;

public class CUServer extends ConcurrentUpdateSolrServer {
	private static final long serialVersionUID = 1L;
	
	public CUServer(String solrServerUrl, int queueSize, int threadCount) {
		super(solrServerUrl, queueSize, threadCount);
	}

	@Override
	public void handleError(Throwable ex) {
		super.handleError(ex);
		System.err.println("********* CUServer Exception ***********");
		ex.printStackTrace();
	}
	
}
