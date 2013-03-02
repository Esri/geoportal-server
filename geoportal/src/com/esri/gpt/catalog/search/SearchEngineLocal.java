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
package com.esri.gpt.catalog.search;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.search.DcList;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswRecord;
import com.esri.gpt.server.csw.client.CswRecords;
import com.esri.gpt.server.csw.client.Utils;
import com.esri.gpt.server.csw.provider.components.IOriginalXmlProvider;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OperationResponse;
import com.esri.gpt.server.csw.provider.components.RequestHandler;
import com.esri.gpt.server.csw.provider.local.ProviderFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * CSW based search engine that executes against the local GPT catalog.
 */
public class SearchEngineLocal extends SearchEngineCSW {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static final Logger LOGGER = Logger.getLogger(SearchEngineLocal.class.getName());
  
  /** The ID that can be associated with the search factory hint. */
  public static final String ID = "local";
  
  /** instance variables ====================================================== */
//  private RequestContext requestContext;
  
  /** constructors ============================================================ */
  
  private SearchEngineLocal() {
    
  }
  
  public SearchEngineLocal(RequestContext context) {
    super(context);
    //this.requestContext = context;
  }
  
  
  // methods =================================================================.
  /* 
   * Ignoring init
   * 
   * @throws SearchException the search exception
   */
  @Override
  public void init() throws SearchException {
    // We do not want to contact the urls
  }
  
  /**
   * Sends a CSW GetRecordByID request to CSW service.
   * @param uuid the document UUID
   * @return the resultant record
   * @throws SearchException the search exception
   */
  @Override
  protected CswRecord getMetadata(String uuid) throws SearchException {  
    String cswResponse = "";
    CswRecord record = null;
    CswRecords records = null;
    
    // send the GetRecordsById request
    try {
      GetRecordsGenerator generator = new GetRecordsGenerator(this.getRequestContext());
      String cswRequest = generator.generateCswByIdRequest(uuid);
      
      RequestHandler handler = ProviderFactory.newHandler(this.getRequestContext());
      OperationResponse resp = handler.handleXML(cswRequest);
      cswResponse = resp.getResponseXml();
      
      records = this.parseResponse(cswResponse);
    } catch (DiscoveryException e) {
      throw new SearchException("Error quering GetRecordById: "+e.getMessage(),e);
    } catch (Exception e) {
      throw new SearchException("Error generating GetRecordById: "+e.getMessage(),e);
    }

    // get the first record
    if (records != null) {
      Iterator iter = records.iterator();
      if (iter.hasNext()) {
        Object obj = iter.next();
        if(obj instanceof CswRecord ) {
          record = (CswRecord)obj;
        } 
      }
    }
    
    // parse the GetRecordsById response
    if (record != null) { 
      record.setId(uuid);
      try {
        getCswProfile().readCSWGetMetadataByIDResponseLocal(cswResponse, record);
      } catch (Exception e) {
        throw new SearchException("Error parsing GetRecordById: "+e.getMessage(),e);
      }
      if (record != null) {
        
        // read the full metadata XML (acl was already processed by above CSW request)
        String fullMetadataXml = "";       
        try {
          
          RequestHandler handler = ProviderFactory.newHandler(this.getRequestContext());
          OperationContext ctx = handler.getOperationContext();
          IOriginalXmlProvider oxp = ctx.getProviderFactory().makeOriginalXmlProvider(ctx);
          fullMetadataXml = oxp.provideOriginalXml(ctx,uuid);
          
        } catch (Exception e) {
          throw new SearchException("Error accessing full metadata xml for: "+uuid);
        }
        record.setFullMetadata(fullMetadataXml);
        if (fullMetadataXml.length() == 0) {
          record = null;
        } 
      }
    }
    
    if (record == null) {
      throw new SearchException("No associated record was located for: "+uuid);
    }
    return record;
  }
  
  /**
   * Specific parse for sitemap only response.
   * @param cswResponse the CSW response
   * @return the resultant records
   * @throws SearchException the search exception
   */
  private CswRecords parseResponseForSitemap(String cswResponse) throws SearchException {
    CswRecords cswRecords = new CswRecords();
    try {
      //System.err.println(cswResponse);
      
      InputSource src = new InputSource(new StringReader(Val.chkStr(cswResponse)));
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      org.w3c.dom.Document dom = builder.parse(src);
      
      String cswNamespace = "http://www.opengis.net/cat/csw/2.0.2";
      String dcNamespace = "http://purl.org/dc/elements/1.1/";
      String dctNamespace = "http://purl.org/dc/terms/";
      String idScheme = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID";
      
      NodeList resultNodes = dom.getElementsByTagNameNS(cswNamespace,"SearchResults");
      if (resultNodes.getLength() == 1) {
        Node searchResultsNode = resultNodes.item(0);
        if (searchResultsNode != null) {
          Node ndHits = searchResultsNode.getAttributes().getNamedItem("numberOfRecordsMatched");
          //Node ndHits = searchResultsNode.getAttributes().getNamedItemNS(cswNamespace,"numberOfRecordsMatched");
          if (ndHits != null) {
            int nHits = Val.chkInt(Val.chkStr(ndHits.getNodeValue()),-1);
            cswRecords.setMaximumQueryHits(nHits);
          }
        }
      }      
      
      NodeList recordNodes = dom.getElementsByTagNameNS(cswNamespace,"Record");
      int nLen = recordNodes.getLength();
      for (int i=0; i<nLen; i++) {
        CswRecord record = new CswRecord();
        String id = "";
        String modified = "";
        Node recordNode = recordNodes.item(i);
        NodeList nlChildren = recordNode.getChildNodes();
        int nChildren = nlChildren.getLength();
        for (int nChild=0; nChild<nChildren; nChild++) {
          Node ndChild = nlChildren.item(nChild);
          String namespace = ndChild.getNamespaceURI();
          String localname = ndChild.getLocalName();
          
          if ((namespace != null) && (localname != null)) {
            if (namespace.equals(dcNamespace) && localname.equals("identifier")) {
              String scheme = "";
              Node ndScheme = ndChild.getAttributes().getNamedItem("scheme");
              if (ndScheme != null) {
                scheme = Val.chkStr(ndScheme.getNodeValue());
              }
              if (scheme.equals(idScheme)) {
                id = Val.chkStr(ndChild.getTextContent());
              }
            } else if (namespace.equals(dctNamespace) && localname.equals("modified")) {
              modified = Val.chkStr(ndChild.getTextContent());
            }
          }
          
        }
        record.setId(id);
        //System.err.println("id="+id+" modified="+modified);
        if (modified.length() > 0) {
          record.setModifiedDate(modified);
        }
        cswRecords.add(record);
      }
      
    } catch (Exception e) {
      throw new SearchException(e);
    } 
    return cswRecords;
  }

    
  /**
   * Sends a CSW GetRecords request to the local CSW service.
   * @param cswRequest the CSW XML request
   * @return the resultant records
   * @throws SearchException the search exception
   */
  @Override
  protected CswRecords sendRequest(String cswRequest) throws SearchException {
    try {
      LOGGER.log(Level.FINER, "Executing local CSW 2.0.2 Discovery request:\n{0}", cswRequest);
      
      
      boolean isSitemapRequest = false;
      Object obj = this.getRequestContext().getObjectMap().get("com.esri.gpt.catalog.search.isSitemapRequest");
      if ((obj != null) && (obj instanceof String)) {
        isSitemapRequest = ((String)obj).equalsIgnoreCase("true");
      }
      
      RequestHandler handler = ProviderFactory.newHandler(this.getRequestContext());
      OperationResponse resp = handler.handleXML(cswRequest);
      String cswResponse = resp.getResponseXml();
      
      LOGGER.log(Level.FINER, "cswResponse:\n{0}", cswResponse);
      
      //return this.parseResponse(cswResponse);
      
      CswRecords parsedRecords = null;
      if (!isSitemapRequest) {
        parsedRecords = this.parseResponse(cswResponse);
      } else {
        parsedRecords = this.parseResponseForSitemap(cswResponse);
      }
      
      return parsedRecords;
      
    } catch (Exception e) {
      throw new SearchException("Error quering GetRecords: "+e.getMessage(),e);
    }
  }
  
  /**
   * Gets the abstract associated with the key
   * 
   * @return the abstract
   * @throws SearchException
   */
  @Override
  public String getKeyAbstract() throws SearchException {
    
    Map<String, String> map = this.getFactoryAttributes();
    String absKey = null;
    if(map != null) {
       absKey = map.get("abstractResourceKey");
    } 
    if(absKey == null || "".equals(absKey.trim())) {
      absKey  = "catalog.search.searchSite.defaultsite.abstract";
    }
    MessageBroker bundle = new MessageBroker();
    bundle.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
    return bundle.retrieveMessage(absKey);
  }
  
  /** 
   * 
   * Creates instances
   *@param rids
   *@return Map with engine
   */
  @Override
  public Map<String, Object> createInstances(StringSet rids) {
    Map<String, Object> mapRid2Engine = new HashMap<String, Object>();
    for(String rid:rids) {
      ASearchEngine engine = new SearchEngineLocal();
      try {
        engine.setKey(rid);
        mapRid2Engine.put(rid, engine);
      } catch (SearchException e) {
        mapRid2Engine.put(rid,"Error while intializing id " + rid + " " 
            + e.getMessage());
        LOGGER.log(Level.WARNING,"Error while intializing id " + rid,e);
      }
      
    }
    return mapRid2Engine;
  }

}
