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
package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.DiscoveryFilter;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.DiscoveryQueryAdapter;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyClause;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.components.CswConstants;
import com.esri.gpt.server.csw.components.IQueryEvaluator;
import com.esri.gpt.server.csw.components.OperationContext;
import com.esri.gpt.server.csw.components.OriginalXmlProvider;
import com.esri.gpt.server.csw.components.OwsException;
import com.esri.gpt.server.csw.components.QueryOptions;
import com.esri.gpt.server.csw.components.TransactionOptions;

import java.util.logging.Logger;

/**
 * Evaluates a CSW query.
 */
public class QueryEvaluator extends DiscoveryAdapter implements IQueryEvaluator {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(QueryEvaluator.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public QueryEvaluator(OperationContext context) {
    super(context);
  }
  
  /** methods ================================================================= */
  
  /**
   * Builds and evaluates an ID based query.
   * @param context the operation context
   * @param ids the IDs to query
   * @throws Exception if a processing exception occurs
   */
  public void evaluateIdQuery(OperationContext context, String[] ids) 
    throws Exception {
    
    // initialize
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    
    // the outputSchema may have to be used to filter the query
    // response generator is 
    //   outputSchema+ElementSetName+ElementSetName@typeNames+Query@typeNames based
    /*
    String elementSetType = Val.chkStr(qOptions.getElementSetType());
    String outputSchema = Val.chkStr(qOptions.getOutputSchema());
    StringSet elementSetTypeNames = qOptions.getElementSetTypeNames();
    boolean isBrief = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Brief);
    boolean isSummary = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Summary);
    boolean isFull = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Full);
    */
    
    // ensure that there are IDs to query
    String locator = "Id";
    if (ids == null) {
      String msg = "The Id parameter was missing.";
      throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
    } else if (ids.length == 0) {
      String msg = "No Valid IDs were supplied.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } 
      
    // determine the queryables
    
    // determine the discoverable
    Discoverable discoverable = this.getDiscoveryContext().findDiscoverable("Id");
    if (discoverable == null) {
      String msg = "The Id queryable is not supported.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    }
    
    // build the discovery filter
    query.getFilter().setRootClause(new LogicalClause.LogicalOr());
    for (String id: ids) {
      id = Val.chkStr(id);
      if (id.length() == 0) {
        String msg = "A supplied ID was empty.";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
      } else {
        PropertyClause propertyClause = new PropertyClause.PropertyIsEqualTo();
        propertyClause.setTarget(discoverable);
        propertyClause.setLiteral(id);
        query.getFilter().getRootClause().getClauses().add(propertyClause);
      }
    }
    int nIds = query.getFilter().getRootClause().getClauses().size();
    qOptions.setStartRecord(1);
    qOptions.setMaxRecords(nIds);
    
    // sdi.suite SmartEditor
    if ((nIds == 1) && !qOptions.isDublinCoreResponse()) {
      String schemaName = Val.chkStr(qOptions.getSchemaFilter());
      if (schemaName.equalsIgnoreCase("http://www.isotc211.org/2005/gmd")) {
        TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
        if ((tOptions.getPublicationMethod() != null) && (tOptions.getPublicationMethod().length() > 0)) {
          if (tOptions.getPublicationMethod().equals("seditor")) {
            OriginalXmlProvider oxp = new OriginalXmlProvider();
            String origXml = oxp.provideOriginalXml(context,ids[0]);
            if ((origXml != null) && (origXml.length() > 0)) {
              DiscoveredRecord record = new DiscoveredRecord();
              record.setResponseXml(origXml);
              query.getResult().setNumberOfHits(1);
              query.getResult().getRecords().add(record);
              return;
            }
          }
        }
      }
    }
 
    // execute the query
    this.evaluateQuery(context);
  }
  
  /**
   * Evaluates the query.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void evaluateQuery(OperationContext context) throws Exception {
    
    // initialize
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    DiscoveryFilter filter = query.getFilter();
    filter.setStartRecord(qOptions.getStartRecord());
    String resultType = Val.chkStr(qOptions.getResultType());
    if (resultType.equalsIgnoreCase(CswConstants.ResultType_Hits)) {
      filter.setMaxRecords(0);
    } else {
      filter.setMaxRecords(qOptions.getMaxRecords());
    }
    filter.setMaxRecords(qOptions.getMaxRecords());
    this.getDiscoveryContext().setReturnables(context);
    context.getRequestContext().getObjectMap().put(
        "com.esri.gpt.server.csw.provider.components.QueryOptions",qOptions);
    
    // evaluate
    LOGGER.finer("Executing discovery query:\n"+query.toString());
    DiscoveryQueryAdapter dqa = 
      context.getRequestContext().getCatalogConfiguration().newDiscoveryQueryAdapter();
    dqa.execute(context.getRequestContext(),query);
    LOGGER.finer("Discovery query result:\n"+query.getResult().toString());
  }
 
}
