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
define(["dojo/_base/declare","dojo/_base/lang"], 
function(declare,lang) {
  
  var oThisClass = declare("g.solr.base.QComponent", null, {
    
    activeQClauses: null,
    solrClient: null,
    
    appendQParameters: function(qRequest) {},
    
    connectTitleBarPane: function(titleBarPane) {},
    
    ensureSolrClient: function() {
      if (!this.solrClient) {
        this.solrClient = window.solrClient;
      }
    },

    // responseObject is the Solr JSON response
    handleQResponse: function(responseObject) {},
    
    onQClauseRemoved: function(qClause) {},
    
    pushQClause: function(qClause,bFetch) {
      if (this.activeQClauses == null) {
        this.activeQClauses = new Array();
      }
      this.activeQClauses.push(qClause);
      if (bFetch) {
        if (this.solrClient != null) {
          this.solrClient.fetch();
        }
      }
    }
    
  });
  
  return oThisClass;
});