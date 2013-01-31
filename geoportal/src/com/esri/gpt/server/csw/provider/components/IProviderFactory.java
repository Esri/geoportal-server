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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.context.RequestContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Interface for instantiating components associated with the execution of a 
 * requested CSW operation.
 */
public interface IProviderFactory {

  /** methods ================================================================= */
  
  /**
   * Makes a parser for a csw:CqlText expression.
   * @param context the operation context
   * @param version the CSW constraint version
   * @return the CQL parser
   * @throws OwsException if the method is unsupported
   */
  public ICqlParser makeCqlParser(OperationContext context, String version)
    throws OwsException;
  
  /**
   * Makes a parser for an ogc:Filter.
   * @param context the operation context
   * @param version the CSW constraint version
   * @return the filter parser
   * @throws OwsException if the method is unsupported
   */
  public IFilterParser makeFilterParser(OperationContext context, String version)
    throws OwsException;
    
  /**
   * Makes an operation provider for a given operation name.
   * @param context the operation context
   * @param operationName the operation name
   * @return the operation provider
   * @throws OwsException if the method is unsupported
   */
  public IOperationProvider makeOperationProvider(OperationContext context, 
                                                  String operationName)
    throws OwsException;
  
  /**
   * Makes a provider for documents in their original XML schema.
   * @param context the operation context
   * @return the original XML provider
   * @throws OwsException if the method is unsupported
   */
  public IOriginalXmlProvider makeOriginalXmlProvider(OperationContext context)
    throws OwsException;
  
  /**
   * Makes an evaluator for a CSW query.
   * @param context the operation context
   * @return the query evaluator
   * @throws OwsException if the method is unsupported
   */
  public IQueryEvaluator makeQueryEvaluator(OperationContext context)
    throws OwsException;
  
  /**
   * Makes a CSW request handler.
   * @param request the HTTP servlet request
   * @param requestContext the active request context
   * @param cswSubContextPath the HTTP sub-context path associated with the CSW service
   * @param resourceFilePrefix the path prefix for XML/XSLT resource files
   * @return the request handler
   */
  public RequestHandler makeRequestHandler(HttpServletRequest request,
                                           RequestContext requestContext,
                                           String cswSubContextPath,
                                           String resourceFilePrefix);
  /**
   * Makes an appropriate CSW operation response generator.
   * @param context the operation context
   * @return the response generator
   * @throws OwsException if the method is unsupported
   */
  public IResponseGenerator makeResponseGenerator(OperationContext context)
    throws OwsException;
  
  /**
   * Makes a parser for an ogc:SortBy clause.
   * @param context the operation context
   * @return the sortBy parser
   * @throws OwsException if the method is unsupported
   */
  public ISortByParser makeSortByParser(OperationContext context)
    throws OwsException;
  
}
