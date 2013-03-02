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
package com.esri.gpt.server.assertion.handler;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnAssertionSet;
import com.esri.gpt.server.assertion.components.AsnAuthorizer;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnOperation;
import com.esri.gpt.server.assertion.components.AsnProperty;
import com.esri.gpt.server.assertion.components.AsnValueType;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.assertion.index.Assertion;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * Handles assertions associated with rating queries.
 */
public class AsnRatingHandler extends AsnOperationHandler {
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnRatingHandler() {}
  
  /** methods ================================================================= */
  
  /**
   * Handles an assertion operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public void handle(AsnContext context) throws Exception {
        
    // initialize
    AsnOperation operation = context.getOperation();
    AsnAuthorizer authorizer = context.getAuthorizer();
    String predicate = operation.getPredicate().getURN();
    
    // query ratings for a resource
    if (predicate.equals("urn:esri:geoportal:rating:query")) {
      if (!operation.getSubject().isType(AsnConstants.SUBJECT_PREFIX_RESOURCEID)) {
        throw new AsnInvalidOperationException();
      }
      authorizer.authorizeQuery(context);
      this.query(context);
    }
  }
  
  /**
   * Summarizes the ratings for a resource.
   * @param context the assertion operation context
   * @throws Exception if an exception occurs
   */
  private void query(AsnContext context) throws Exception {
    
    // initialize
    AsnOperation operation = context.getOperation();
    AsnAssertionSet asnSet = operation.getAssertionSet();
    AsnValueType vType = asnSet.getValueType();
    String subject = operation.getSubject().getURN();
    String predicate = vType.getRdfPredicate(); 
    String valueField = vType.getRdfValueField();
    String upValue = "urn:esri:geoportal:rating:value:up";
    String downValue = "urn:esri:geoportal:rating:value:down";
    
    IndexReader reader = null;
    IndexSearcher searcher = null;
    try {
      
      // make the reader and searcher
      reader = this.getIndexAdapter().makeIndexReader();
      searcher = new IndexSearcher(reader);
      
      // count up votes
     long nUp = this.getIndexAdapter().count(
          context,searcher,valueField,subject,predicate,upValue);
      
      // count down votes
      long nDown = this.getIndexAdapter().count(
          context,searcher,valueField,subject,predicate,downValue);
      
      // root property for the response
      String rootSubject = subject;
      String roorPredicate = operation.getPredicate().getURN()+"response";
      AsnProperty rootProp = new AsnProperty(rootSubject,roorPredicate,null);
        
      // up, down and total counts
      rootProp.getChildren().add(new AsnProperty(null,upValue+":count",""+nUp));
      rootProp.getChildren().add(new AsnProperty(null,downValue+":count",""+nDown));
      rootProp.getChildren().add( new AsnProperty(null,asnSet.getURNPrefix()+":count",""+(nUp+nDown)));
      
      // canCreate capability for the active user
      String canCreatePred = asnSet.getURNPrefix()+":activeUser:canCreate";
      String canCreateVal = ""+context.getAuthorizer().canCreate(context,asnSet.getAuthPolicy());
      rootProp.getChildren().add(new AsnProperty(null,canCreatePred,canCreateVal));
      
      // user's previous rating
      Assertion previous = this.getIndexAdapter().loadPreviousUserAssertion(context,searcher);
      if (previous != null) {
        String prevSubj = Val.chkStr(previous.getSystemPart().getAssertionId());
        prevSubj = asnSet.getAssertionIdPrefix()+":"+prevSubj;
        String predPred = asnSet.getURNPrefix()+":activeUser:previousValue";
        String prevVal = previous.getRdfPart().getValue();
        rootProp.getChildren().add(new AsnProperty(prevSubj,predPred,prevVal));
      }
      
      // generate the response
      context.getOperationResponse().generateResponse(context,rootProp.getChildren());      
    } finally {
      this.getIndexAdapter().closeReader(reader);
      this.getIndexAdapter().closeSearcher(searcher);
    }
  }

}
