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
package com.esri.gpt.framework.search;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineCSW;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.request.Criteria;
import com.esri.gpt.framework.request.QueryResult;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.request.Records;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswRecord;
import com.esri.gpt.server.csw.client.CswRuntimeException;
import com.esri.gpt.server.csw.client.CswSearchCriteria;
import com.esri.gpt.server.csw.client.InvalidOperationException;
import com.esri.gpt.server.csw.client.NullReferenceException;
import com.esri.gpt.server.csw.client.Utils;


/**
 * Search profile.
 * @param <C> criteria type
 * @param <RD> record type
 * @param <RS> collection of records type
 * @param <QR> query result type
 */
public abstract class SearchXslProfile<C extends Criteria, RD extends Record,
  RS extends Records<RD>, QR extends QueryResult<RS>> {

// class variables =============================================================
/** The class logger *. */
private static Logger LOG = Logger.getLogger(CswProfile.class
    .getCanonicalName());

private final static String SCHEME_METADATA_DOCUMENT = 
  "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document";

/**
 * Type of search output format.
 */
public static enum FORMAT_SEARCH_TO_XSL {MINIMAL_LEGACY_CSWCLIENT, 
  FULL_NATIVE_GPTXML, DETAILED_GPT_CSW202};

private static final String XSL_PARAM_HITS_ONLY = "searchQueryDoHitsOnly";
// instance variables ==========================================================
/** The description. */
private String description;

/** The filter_extentsearch. */ 
private boolean filter_extentsearch;

/** The filter_livedatamap. */
private boolean filter_livedatamap;

/** indicates whether endpoint is harvestable */
private boolean harvestable = true;

/** The id. */
private String id;

/** The kvp. */
private String kvp;

/** The metadataxslt. */
private String metadataxslt;

/** The metadata xslt obj. */
private XsltTemplate metadataXsltObj; 

/** The name. */
private String name;

/** The requestxslt. */
private String requestxslt;

/** The request xslt obj. */
private XsltTemplate requestXsltObj; 

/** The responsexslt. */
private String responsexslt;

/** The response xslt obj. */
private XsltTemplate responseXsltObj; 

/** XSLT factory*. */
private TransformerFactory factory;

/** The supports spatial query. */
private boolean supportsSpatialQuery;

/** The supports content type query. */
private boolean supportsContentTypeQuery;

/** The supports spatial boundary. */
private boolean supportsSpatialBoundary;

//TODO: Put this in the profile, find out which profiles accept this
private boolean supportsFullMetadataAtSearch;

/** The format request to xsl. */
private FORMAT_SEARCH_TO_XSL formatRequestToXsl;



// constructors ================================================================
/**
 * Instantiates a new csw profile.
 */
public SearchXslProfile() {
}

/**
 * The Constructor.
 * 
 * @param sid
 *          the sid
 * @param sname
 *          the sname
 * @param sdescription
 *          the sdescription
 */
public SearchXslProfile(String sid, String sname, String sdescription) {
}

/**
 * The Constructor.
 * 
 * @param livedatamap
 *          the livedatamap
 * @param extentsearch
 *          the extentsearch
 * @param id
 *          the id
 * @param name
 *          the name
 * @param description
 *          the description
 * @param kvp
 *          the kvp
 * @param requestxslt
 *          the requestxslt
 * @param responsexslt
 *          the responsexslt
 * @param metadataxslt
 *          the metadataxslt
 */
public SearchXslProfile(String id, String name, String description, String kvp,
    String requestxslt, String responsexslt, String metadataxslt,
    boolean livedatamap, boolean extentsearch) {
  this.id = id;
  this.name = name;
  this.description = description;
  this.kvp = kvp;
  this.requestxslt = requestxslt;
  this.responsexslt = responsexslt;
  this.metadataxslt = metadataxslt;
  this.filter_livedatamap = livedatamap;
  this.filter_extentsearch = extentsearch;

}

// properties ==================================================================

/**
 * Checks if is supports full metadata at search.
 * 
 * @return true, if is supports full metadata at search
 */
public boolean isSupportsFullMetadataAtSearch() {
  return supportsFullMetadataAtSearch;
}



/**
 * Checks if is harvestable.
 * 
 * @return true, if is harvestable
 */
public boolean isHarvestable() {
  return harvestable;
}

/**
 * Sets the harvestable.
 * 
 * @param harvestable the new harvestable
 */
public void setHarvestable(boolean harvestable) {
  this.harvestable = harvestable;
}

/**
 * Sets the supports full metadata at search.
 * 
 * @param supportsFullMetadataAtSearch the new supports full metadata at search
 */
public void setSupportsFullMetadataAtSearch(boolean supportsFullMetadataAtSearch) {
  this.supportsFullMetadataAtSearch = supportsFullMetadataAtSearch;
}

/**
 * Checks if is supports spatial query.
 * 
 * @return true, if is supports spatial query
 */
public boolean isSupportsSpatialQuery() {
  return supportsSpatialQuery;
}

/**
 * Sets the supports spatial query.
 * 
 * @param supportsSpatialQuery
 *          the new supports spatial query
 */
public void setSupportsSpatialQuery(boolean supportsSpatialQuery) {
  this.supportsSpatialQuery = supportsSpatialQuery;
}

/**
 * Checks if is supports content type query.
 * 
 * @return true, if is supports content type query
 */
public boolean isSupportsContentTypeQuery() {
  return supportsContentTypeQuery;
}

/**
 * Sets the supports content type query.
 * 
 * @param supportsContentTypeQuery
 *          the new supports content type query
 */
public void setSupportsContentTypeQuery(boolean supportsContentTypeQuery) {
  this.supportsContentTypeQuery = supportsContentTypeQuery;
}

/**
 * Checks if is supports spatial boundary.
 * 
 * @return true, if is supports spatial boundary
 */
public boolean isSupportsSpatialBoundary() {
  return supportsSpatialBoundary;
}

/**
 * Sets the supports spatial boundary.
 * 
 * @param supportsSpatialBoundary
 *          the new supports spatial boundary
 */
public void setSupportsSpatialBoundary(boolean supportsSpatialBoundary) {
  this.supportsSpatialBoundary = supportsSpatialBoundary;
}

// methods =====================================================================

/**
 * Read get metadata by id response.
 * 
 * @param response the response
 * @param record the record
 * @throws TransformerException the transformer exception
 */
public abstract void readGetMetadataByIDResponse(String response, RD record) 
  throws TransformerException ;

/**
 * Read get records response.  Puts SearchXslRecord into search
 * Result param.
 * 
 * @param responseString the response string
 * @param searchResult the search results (will be filled with SearchXslRecords)
 * @throws TransformerException the transformer exception
 * @throws ParserConfigurationException the parser configuration exception
 * @throws SAXException the sAX exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws XPathExpressionException the x path expression exception
 */
@SuppressWarnings("unchecked")
public void readGetRecordsResponse(String responseString,
    QueryResult<RS> searchResult) throws TransformerException,
    ParserConfigurationException, SAXException, IOException,
    XPathExpressionException {
  

  LOG.finer("Transforming response to searchxslrecord native " +
  		"response. response = " + responseString);
  String response = this.getResponsexsltobj().transform(responseString);
  RS recordList = searchResult.getRecords();
  
  LOG.finer("CSW Response to CSWClient Native = " + response);
  String RECORD_TAG = "Record";
  // create xml document object
  // XML parser load doc specified by filename
  DocumentBuilder builder = DocumentBuilderFactory.newInstance()
    .newDocumentBuilder();
  Document doc = builder.parse(new InputSource(new StringReader(response)));
  checkForExceptions(doc);
  
  // Get a list of nodes of which root is Profile tag
  NodeList recordNodes = doc.getElementsByTagName(RECORD_TAG);
  // create CswRecord and convert xml document node to each record

  // Get the maximum number of records
  XPath xpath = XPathFactory.newInstance().newXPath();
  @SuppressWarnings("unused")
  NodeList maxRecordNodes = doc.getElementsByTagName("SearchResults");
 
  Node searchResultNode = doc.getDocumentElement();
  NamedNodeMap attributes = searchResultNode.getAttributes();
  Node node = attributes.getNamedItem("maxRecords");
  if(node != null ) {
    int maxNum = Utils.chkInt(node.getNodeValue(), Integer.MIN_VALUE);
    if(maxNum >= 0) {
      recordList.setMaximumQueryHits(maxNum);
      searchResult.setMaxQueryHits(maxNum);
     }
  }
  int nLen = recordNodes.getLength();
  for (int i = 0; i < nLen; i++) {

    LOG.finer("Going through a record node");
    Node currNode = recordNodes.item(i);
    CswRecord record = new CswRecord();

    double maxX = 180;
    double maxY = 90;
    double minX = -180;
    double minY = -90;
    String lowerCorner = "";
    String upperCorner = "";
    record.setDefaultEnvelope(true);

    NodeList nlChildren = currNode.getChildNodes();
    int nChildren = nlChildren.getLength();
    for (int nChild = 0; nChild < nChildren; nChild++) {
      Node ndChild = nlChildren.item(nChild);
      String nodeName = ndChild.getNodeName();
      String nodeValue = ndChild.getTextContent();
      
      if (nodeName.equals("ID")) {
        record.setId(nodeValue);
      } else if (nodeName.equals("Title")) {
        record.setTitle(nodeValue);
      } else if (nodeName.equals("Abstract")) {
        record.setAbstractData(nodeValue);
      } else if (nodeName.equals("ModifiedDate")) {
        record.setModifiedDate(nodeValue);
      } else if (nodeName.equals("MaxX")) {
        maxX = Utils.chkDbl(nodeValue,maxX);
        record.setDefaultEnvelope(
            Utils.chkDbl(nodeValue,Integer.MIN_VALUE) == Integer.MIN_VALUE); 
      } else if (nodeName.equals("MaxY")) {
        maxY = Utils.chkDbl(nodeValue,maxY);
      } else if (nodeName.equals("MinX")) {
        minX = Utils.chkDbl(nodeValue,minX);
      } else if (nodeName.equals("MinY")) {
        minY = Utils.chkDbl(nodeValue,minY);
      } else if (nodeName.equals("LowerCorner")) {
        lowerCorner = Utils.chkStr(nodeValue);
      } else if (nodeName.equals("UpperCorner")) {
        upperCorner = Utils.chkStr(nodeValue);        
      } else if (nodeName.equals("References")) {
        record.setReference(nodeValue);
      } else if (nodeName.equals("Types")) {
        record.setTypes(nodeValue);
      } else if (nodeName.equals("ModifiedDate")) {
        record.setTypes(nodeValue);
      }
    }
    
    if (!"".equals(lowerCorner)) {
      String lowerCornerPts[] = lowerCorner.split(" ");
      String upperCornerPts[] = upperCorner.split(" ");
      if (lowerCornerPts != null && lowerCornerPts.length >= 2) {
        minY = Utils.chkDbl(lowerCornerPts[1], minY);
        minX = Utils.chkDbl(lowerCornerPts[0], minX);
        record.setDefaultEnvelope(
            Utils.chkDbl(lowerCornerPts[1], Integer.MIN_VALUE) 
              == Integer.MIN_VALUE); 
      }
      if (upperCornerPts != null && upperCornerPts.length >= 2) {
        maxY = Utils.chkDbl(upperCornerPts[1], maxY);
        maxX = Utils.chkDbl(upperCornerPts[0], maxX);
      }
    }
    record.setEnvelope(new Envelope(minX, minY, maxX, maxY));
             
    // Attempting to see if this is a livedata record
    record.setLiveDataOrMap(false);
    Iterator<DcList.Value> iter = record.getTypes().iterator();
    while (iter.hasNext()) {
      DcList.Value value = iter.next();
      if (value.getValue().equalsIgnoreCase("livedata")) {
        record.setLiveDataOrMap(true);
      } 
    
    }
       
    // Links
    NodeList nodeList = ((Element)currNode).getElementsByTagName("Link");
    for(int j = 0; j < nodeList.getLength(); j++) {
      Node linkNode = nodeList.item(j);
      NamedNodeMap attrLinkNode = linkNode.getAttributes();
      String gptLinkTag = null;
      if(attrLinkNode.getNamedItem("gptLinkTag")!= null) {
        gptLinkTag = attrLinkNode.getNamedItem("gptLinkTag").getNodeValue();
      }
      String show = null;
      if(attrLinkNode.getNamedItem("show")!= null) {
        show = attrLinkNode.getNamedItem("show").getNodeValue();
      }
      String url = null;
      if(linkNode!= null) {
        url = linkNode.getTextContent();
      }
      String label = null;
      if(attrLinkNode.getNamedItem("label")!= null) {
        label = attrLinkNode.getNamedItem("label").getNodeValue();
      }
      
      if(label != null && !"".equals(label)) {
        record.getLinks().addCustomLink(label, url);
      } else if(gptLinkTag != null && !"".equals(gptLinkTag)){
        record.getLinks().addDefaultLinkOptions(gptLinkTag, 
            Val.chkBool(show, true));
      } else {
        continue;
      }
      
    }
    recordList.add((RD)record);
  }
    
  
  
}


/**
 * Support content type query.
 * 
 * @return true, if successful
 */
public boolean SupportContentTypeQuery() {
  return isSupportsContentTypeQuery();
}

public boolean SupportSpatialBoundary() {
  return isSupportsSpatialBoundary();
}

/**
 * Support spatial query.
 * 
 * @return true, if successful
 */
public boolean SupportSpatialQuery() {
  return isSupportsSpatialQuery();
}

public String getDescription() {
  return description;
}

/**
 * Sets the description.
 * 
 * @param description
 *          the new description
 */
public void setDescription(String description) {
  this.description = description;
}

/**
 * Checks if is filter_extentsearch.
 * 
 * @return true, if is filter_extentsearch
 */
public boolean isFilter_extentsearch() {
  return filter_extentsearch;
}

/**
 * Sets the filter_extentsearch.
 * 
 * @param filter_extentsearch
 *          the new filter_extentsearch
 */
public void setFilter_extentsearch(boolean filter_extentsearch) {
  this.filter_extentsearch = filter_extentsearch;
}

/**
 * Checks if is filter_livedatamap.
 * 
 * @return true, if is filter_livedatamap
 */
public boolean isFilter_livedatamap() {
  return filter_livedatamap;
}

/**
 * Sets the filter_livedatamap.
 * 
 * @param filter_livedatamap
 *          the new filter_livedatamap
 */
public void setFilter_livedatamap(boolean filter_livedatamap) {
  this.filter_livedatamap = filter_livedatamap;
}

/**
 * Gets the id.
 * 
 * @return the id
 */
public String getId() {
  return id;
}

/**
 * Sets the id.
 * 
 * @param id
 *          the new id
 */
public void setId(String id) {
  this.id = id;
}

/**
 * Gets the kvp.
 * 
 * @return the kvp
 */
public String getKvp() {
  return kvp;
}

/**
 * Sets the kvp.
 * 
 * @param kvp
 *          the new kvp
 */
public void setKvp(String kvp) {
  this.kvp = kvp;
}

/**
 * Gets the metadataxslt.
 * 
 * @return the metadataxslt
 */
public String getMetadataxslt() {
  return metadataxslt;
}

/**
 * Sets the metadataxslt.
 * 
 * @param metadataxslt
 *          the new metadataxslt
 */
public void setMetadataxslt(String metadataxslt) {
  this.metadataxslt = metadataxslt;
}

/**
 * Gets the metadataxsltobj.
 * 
 * @return the metadataxsltobj
 */
public XsltTemplate getMetadataxsltobj() {
  return metadataXsltObj;
}

/**
 * Sets the metadataxsltobj.
 * 
 * @param metadataxsltobj
 *          the new metadataxsltobj
 */
public void setMetadataxsltobj(XsltTemplate metadataxsltobj) {
  this.metadataXsltObj = metadataxsltobj;
}

/**
 * Gets the name.
 * 
 * @return the name
 */
public String getName() {
  return name;
}

/**
 * Sets the name.
 * 
 * @param name
 *          the new name
 */
public void setName(String name) {
  this.name = name;
}

/**
 * Gets the requestxslt.
 * 
 * @return the requestxslt
 */
public String getRequestxslt() {
  return requestxslt;
}

/**
 * Sets the requestxslt.
 * 
 * @param requestxslt
 *          the new requestxslt
 */
public void setRequestxslt(String requestxslt) {
  this.requestxslt = requestxslt;
}

/**
 * Gets the requestxsltobj.
 * 
 * @return the requestxsltobj
 * @throws TransformerConfigurationException on xslt template creation error
 * @throws IOException Signals that an I/O exception has occurred.
 */
public XsltTemplate getRequestxsltobj() 
  throws TransformerConfigurationException, IOException {
  
  if (requestXsltObj == null) {
    String file = this.getRequestxslt();
    try {
      this.setRequestxsltobj(XsltTemplate.makeFromResourcePath(file));
    } catch (IOException e) {
      try {
        this.setRequestxsltobj(XsltTemplate.makeFromResourcePath(file));
      } catch (IOException f) {
        throw f;
      }
    }
  }
  
  return this.requestXsltObj;
}

/**
 * Sets the requestxsltobj.
 * 
 * @param requestxsltobj
 *          the new requestxsltobj
 */
public void setRequestxsltobj(XsltTemplate requestxsltobj) {
  this.requestXsltObj = requestxsltobj;
}

/**
 * Gets the responsexslt.
 * 
 * @return the responsexslt
 */
public String getResponsexslt() {
  return responsexslt;
}

/**
 * Sets the response XSLT.
 * 
 * @param responsexslt
 *          the new response XSLT
 */
public void setResponsexslt(String responsexslt) {
  this.responsexslt = responsexslt;
}

/**
 * Gets the response XSLT object.
 * 
 * @return the response XSLT object
 * @throws CSWException
 */
public XsltTemplate getResponsexsltobj() throws 
  TransformerConfigurationException, IOException {
  if (responseXsltObj == null) {
    String file = this.getResponsexslt();
    try {
      this.setResponsexsltobj(XsltTemplate.makeFromResourcePath(file));
    } catch (IOException e) {
      try {
        this.setResponsexsltobj(XsltTemplate.makeFromResourcePath(file));
      } catch (IOException f) {
        throw f;
      }
    }
  }
  return responseXsltObj;
}

/**
 * Gets the response XSLT object.
 * 
 * @return the response XSLT object
 * @throws CSWException
 */
public XsltTemplate getMetadataXsltObj() throws TransformerConfigurationException {
  if (this.metadataXsltObj == null) {
    String file = this.getMetadataxslt();
    //InputStream inStream = this.getClass().getResourceAsStream(file);
    //if (inStream == null) {
    //  inStream = this.getClass().getResourceAsStream("/" + file);
    //  file = "/" + file;
    //}
    //inStream = null;
    try {
      this.setMetadataxsltobj(XsltTemplate.makeFromResourcePath(file));
    } catch (TransformerConfigurationException e) {
      LOG.severe("CSW Client: Could not get xslt template " + file);
      throw e;
    } catch (IOException e) {
      LOG.severe("CSW Client: Could not get xslt template " + file);
      throw new TransformerConfigurationException("CSW Client: Could not get xslt template " + file,e);
    }
  }
  return this.metadataXsltObj;
}

/**
 * Sets the response XSLT Object.
 * 
 * @param responsexsltobj
 *          the new response XSLT Object
 */
public void setResponsexsltobj(XsltTemplate responsexsltobj) {
  this.responseXsltObj = responsexsltobj;
}

/**
 * Gets the transformer factory.
 * 
 * @return the factory
 */
public TransformerFactory getFactory() {
  if (factory == null) {
    return TransformerFactory.newInstance();
  }
  return factory;
}

/**
 * Transformer convinient method.
 * 
 * @param xml the xml
 * @param transformer the transformer
 * @return the string
 * @throws TransformerException the transformer exception
 */
protected String transform(String xml, Transformer transformer) 
 throws TransformerException {
  StreamSource source = new StreamSource(new StringReader(xml));
  StringWriter writer = new StringWriter();
  StreamResult result = new StreamResult(writer);
  
  transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
  transformer.setOutputProperty(OutputKeys.INDENT,"yes");
  transformer.transform(source,result);
  
  return result.getWriter().toString();
}

/**
 * Check response if there is any exception produced from the server.
 * 
 * @param doc the doc
 * @exception CswRuntimeException if exception found in response
 */
private void checkForExceptions(Document doc) {
  String exceptionTag = "exception";
  StringBuffer exception = null;
  NodeList recordNodes = doc.getElementsByTagName(exceptionTag);
  
  if(recordNodes == null || recordNodes.getLength() < 1){
    return;
  }
  exception = new StringBuffer();
  XPath xpath = XPathFactory.newInstance().newXPath();
  for (int i = 0; i < recordNodes.getLength(); i++) {
    
   
    try {
      exception.append(
          Utils.chkStr(xpath.evaluate("exceptionText", recordNodes.item(i))));
    } catch (XPathExpressionException e) {
      exception.append(e.getMessage());
      LOG.log(Level.WARNING, "Problem while parsing execption recieved", e);
      e.printStackTrace();
    }
  
  }
  if(exception != null) {
    LOG.severe("Search returned exception. Creating exception report "
        + exception.toString());
    throw new CswRuntimeException(exception.toString());
  }
 
}

/**
 * Read intermidiate get record by id.  GetRecords does only uses the getMetadata
 * xslt to get an intermidiate reference to the actual xml
 * 
 * @param response the response
 * @param record the record
 * @throws TransformerConfigurationException the transformer configuration exception
 * @throws TransformerException the transformer exception
 */
protected void readIntermidiateGetRecordById(String response, 
    SearchXslRecord record) 
  throws TransformerConfigurationException, TransformerException 
{
  String metadataxslt = this.getMetadataxslt();
  if (metadataxslt == null || metadataxslt.equals("")) {
    record.setFullMetadata(Utils.chkStr(response));
  } else {
 
    LOG.finer("Transforming GetRecordByID intermidiate xml to GetRecordById " +
        "Native");
    
    String result = this.getMetadataXsltObj().transform(Val.chkStr(response));
        
    String xmlUrl = null;
    String dctReferences = result;
    LOG.finer("Native GetRecordBYID from transform = " + dctReferences);
    DcList lstDctReferences = new DcList(dctReferences);
    
    Iterator<DcList.Value> iter = lstDctReferences.iterator();
    while(iter.hasNext()) {
      DcList.Value value = iter.next();
      if(value.getValue().toLowerCase().endsWith(".xml") 
          || value.getScheme().equals(SCHEME_METADATA_DOCUMENT)) {
        xmlUrl = value.getValue();
      }
    }
    record.setReferences(lstDctReferences);
    LOG.finer("URL to view full metadata document found = " + xmlUrl);
    record.setMetadataResourceURL(xmlUrl);
  }
}

/**
 * Gets the record by id.
 * 
 * <ul>
 *   <li>Url constructed</li>
 *   <li>XML is gotten from url</li>
 *   <li>XML put through the getRecordsResponse xsl to make a cswRecord</li> 
 *   <li>if metadataxsl exists, use it to get the use responstr an intermidiate
 * response and get the associated reference</li>
 *   <li>Fills the cswrecord with the responseStr and later with the intermidiate
 * xml reference response if there is one
 *   </li>
 * <ul>
 *   
 * 
 * 
 * @param responseStr the respons str
 * @param uuid the uuid
 * @param requestUrl the request url
 * @return the record by id
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws TransformerException the transformer exception
 * @throws NullReferenceException the null reference exception
 * @throws InvalidOperationException the invalid operation exception
 */
@SuppressWarnings("unchecked")
public SearchXslRecord getRecordById(String responseStr, String uuid, 
    String requestUrl) 
  throws IOException, TransformerException,
    NullReferenceException, InvalidOperationException, SearchException {
  SearchXslRecord record = null;

        
    responseStr = Val.chkStr(responseStr);    
    
    try {
      LOG.finer("GetRecordById: Making csw record object using g" +
          "etRecordsResponse operation");
    
      // making cswObject by going through getrecord response xslt
      Records<SearchXslRecord> recordList = new Records<SearchXslRecord>();
      QueryResult<RS> results = new QueryResult<RS>();
      this.readGetRecordsResponse(responseStr, results);
   
      Iterator<SearchXslRecord> iter = recordList.iterator();
      if(iter.hasNext()) {
         record = iter.next(); 
        
      } else  {
        LOG.log(Level.WARNING, 
            "Could not get csw metadata of metadata document");
      }
    } catch (ParserConfigurationException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + e.getMessage());
    } catch (SAXException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + e.getMessage());
    } catch (XPathExpressionException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + e.getMessage());
    }
    record.setId(uuid);
    LOG.finer("GetRecordByID: Transforming intermidiate xml to populate xml " +
        "into csw Record");
    this.readIntermidiateGetRecordById(responseStr, record);
    if (record == null) {
      throw new NullReferenceException("Record not populated.");
    }
    // check if full metadata or resourceURL has been returned
    boolean hasFullMetadata = !(record.getFullMetadata() == null || record
        .getFullMetadata() == "");
    boolean hasResourceUrl = !(record.getMetadataResourceURL() == null || record
        .getMetadataResourceURL() == ""); // TODO: CHECK THE COMPARISONS!
    
    if(!hasResourceUrl && requestUrl != null ) {
      record.setMetadataResourceURL(requestUrl);
      hasResourceUrl = true;
    }

    if (!hasFullMetadata && !hasResourceUrl) {
      throw new SearchException("Neither full metadata nor metadata"
          + " resource URL was found for the CSW record.");
    } else if (hasResourceUrl) {
      // need to load metadata from resource URL
      URL url = null;
      Exception ex = null;
      try {
        url = new URL(record.getMetadataResourceURL());

        HttpClientRequest clientRequest = HttpClientRequest.newRequest(
            HttpClientRequest.MethodName.GET, url.toExternalForm());
        // clientRequest.setConnectionTimeOut(getConnectionTimeout());
        //clientRequest.setResponseTimeOut(getResponseTimeout());
        clientRequest.execute();
        String response = clientRequest.readResponseAsCharacters();
        LOG.finer("Response from get Metadata url = " + url.toExternalForm()
            +"\n response = \n"+ response);
        record.setFullMetadata(response);
      } catch (MalformedURLException e) {
        ex = e;
      } catch (IOException e) {
        ex = e;
      }
      if(ex != null) {
        throw new SearchException("Could not get metadata id url = " + url, ex);
      }

      
    }

 
  return record;

}

/**
 * Gets the format request to xsl.
 * 
 * @return the format request to xsl (never null, default MINIMAL_LEGACY)
 */
public FORMAT_SEARCH_TO_XSL getFormatRequestToXsl() {
  if(this.formatRequestToXsl == null) {
    this.formatRequestToXsl = FORMAT_SEARCH_TO_XSL.MINIMAL_LEGACY_CSWCLIENT;
  }
  return formatRequestToXsl;
}

/**
 * Sets the format request to xsl.
 * 
 * @param formatRequestToXsl the new format request to xsl
 */
public void setFormatRequestToXsl(FORMAT_SEARCH_TO_XSL formatRequestToXsl) {
  this.formatRequestToXsl = formatRequestToXsl;
}


/**
 * Generate get records request.
 * 
 * @param criteria the criteria
 * @param xslParams the xsl params
 * @param hitsOnly Indicates whether this is a hits only request
 * @return the string
 * @throws TransformerException the transformer exception
 * @throws ParserConfigurationException the parser configuration exception
 * @throws SAXException the sAX exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws SearchException the search exception
 * @throws XPathExpressionException the x path expression exception
 */
public String generateGetRecordsRequest(SearchCriteria criteria,
    Map<String, String> xslParams, boolean hitsOnly)
    throws TransformerException, ParserConfigurationException, SAXException,
    IOException, SearchException, XPathExpressionException {

  if(xslParams == null) {
    xslParams = new HashMap<String, String>();
  }
  xslParams.put(XSL_PARAM_HITS_ONLY, String.valueOf(hitsOnly));
  String internalRequestXml = null;
  FORMAT_SEARCH_TO_XSL formatRequestToXsl = this.getFormatRequestToXsl();
  if (formatRequestToXsl == FORMAT_SEARCH_TO_XSL.FULL_NATIVE_GPTXML) {
    internalRequestXml = criteria.toDom2();

  } else if (formatRequestToXsl == FORMAT_SEARCH_TO_XSL.DETAILED_GPT_CSW202) {
    internalRequestXml = SearchEngineCSW.transformGptToCswRequest(criteria,
        SearchEngineCSW.SEARCH_OPERATION.doSearch);
  } else {
    // Default use the MINIMAL XML
    CswSearchCriteria cswSearchCriteria = SearchEngineCSW
        .marshallGptToCswClientCriteria2(criteria);
    internalRequestXml = cswSearchCriteria.toXml();
  }

  LOG.finer("Generated internal XML requst input  to request xsl "
      + internalRequestXml);
  internalRequestXml = getRequestxsltobj().transform(internalRequestXml,
      xslParams);
  LOG.finer("Transform output from Request xsl " + internalRequestXml);

  return internalRequestXml;
}

}
