/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.server.csw.provider3.local;

import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveryFilter;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyClause;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.components.IQueryParser;
import com.esri.gpt.server.csw.components.OperationContext;
import com.esri.gpt.server.csw.components.OwsException;
import com.esri.gpt.server.csw.components.QueryOptions;
import java.util.logging.Logger;

/**
 * Query parser parses "q" parameter.
 */
public class QueryParser extends DiscoveryAdapter implements IQueryParser {
      
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(QueryParser.class.getName());
  
  /** instance variables ====================================================== */
  private OperationContext opContext;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public QueryParser(OperationContext context) {
    super(context);
    this.opContext = context;
  }
  
  /** methods ================================================================= */

  @Override
  public void parseQuery(OperationContext context, String[] keywords) 
    throws OwsException {
      if (keywords!=null && keywords.length>0) {
        QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
        DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
        DiscoveryFilter filter = query.getFilter();
        if (filter.getRootClause()==null) {
          filter.setRootClause(new LogicalClause.LogicalAnd());
        }
        LogicalClause rootClause = filter.getRootClause();
        Discoverable anytext = this.getDiscoveryContext().findDiscoverable("anytext");
        
        if (anytext!=null) {
            for (String keyword: keywords) {
                PropertyClause propertyIsLike = new PropertyClause.PropertyIsLike();
                propertyIsLike.setTarget(anytext);
                propertyIsLike.setLiteral(Val.chkStr(keyword));
                rootClause.getClauses().add(propertyIsLike);
            }
        }
      }
  }

}
