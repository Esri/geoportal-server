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
package com.esri.gpt.server.csw.provider30.local;
import com.esri.gpt.catalog.discovery.DcElement;
import com.esri.gpt.catalog.discovery.DiscoveredRecord;
import com.esri.gpt.catalog.discovery.DiscoveredRecords;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.DiscoveryResult;
import com.esri.gpt.catalog.discovery.PropertyMeaningType;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyValueType;
import com.esri.gpt.catalog.discovery.Returnable;
import com.esri.gpt.catalog.search.ResourceIdentifier;
import com.esri.gpt.control.georss.AtomEntry;
import com.esri.gpt.control.georss.DiscoveredRecordAdapter;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.server.csw.components.CswConstants;
import com.esri.gpt.server.csw.components.CswNamespaces;
import com.esri.gpt.server.csw.components.IOriginalXmlProvider;
import com.esri.gpt.server.csw.components.IResponseGenerator;
import com.esri.gpt.server.csw.components.OperationContext;
import com.esri.gpt.server.csw.components.OperationResponse;
import com.esri.gpt.server.csw.components.OwsException;
import com.esri.gpt.server.csw.components.QueryOptions;
import com.esri.gpt.server.csw.components.ServiceProperties;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Generates a CSW query response.
 * <p>
 * Applies to the GetRecordById and GetRecords operation response.
 */
public class QueryResponse extends DiscoveryAdapter implements IResponseGenerator { 
  
  /** class variables ========================================================= */
    /** The DAT e_ forma t_ pattern. */
    static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    /** The TIM e_ forma t_ pattern. */
    static final String TIME_FORMAT_PATTERN = "kk:mm:ss";
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(QueryResponse.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public QueryResponse(OperationContext context) {
    super(context);
  }

    /** methods ================================================================= */

    /**
     * Creates and appends elements associated with a returnable property to a
     * record element.
     * @param context the operation context
     * @param record the parent element that will hold the fields (a Record)
     * @param returnable the returnable property
     */
    protected void appendDiscoveredField(OperationContext context, Element record, Returnable returnable) {
        // initialize
        ServiceProperties svcProps = context.getServiceProperties();
        String httpContextPath = Val.chkStr(svcProps.getHttpContextPath());
        String cswBaseUrl = Val.chkStr(svcProps.getCswBaseURL());
        OperationResponse opResponse = context.getOperationResponse();
        Document responseDom = opResponse.getResponseDom();
        PropertyMeaning meaning = returnable.getMeaning();
        PropertyMeaningType meaningType = meaning.getMeaningType();
        Object[] values = returnable.getValues();
        DcElement dcElement = meaning.getDcElement();
        if ((dcElement == null) || dcElement.getElementName().startsWith("!")) {
            return;
        }
        
        // TODO create an empty element if the values are null?
        // return if the values are null
        if (values == null) {
            //Element elField = dom.createElement(returnable.getClientName());
            //elField.appendChild(dom.createTextNode(""));
            //record.appendChild(elField);
            return;
        }
        
        // add an element for each value found
        for (Object oValue: values) {
            if (oValue != null) {
                if (meaning.getValueType().equals(PropertyValueType.GEOMETRY)) {
                    if (oValue instanceof Envelope) {
                        
                        // TODO include multiple envelope types in the response
                        Envelope env = (Envelope)oValue;
                        String sLower = env.getMinX()+" "+env.getMinY();
                        String sUpper = env.getMaxX()+" "+env.getMaxY();
                        
                        Element elField = responseDom.createElement("ows:WGS84BoundingBox");
                        Element elLower = responseDom.createElement("ows:LowerCorner");
                        Element elUpper = responseDom.createElement("ows:UpperCorner");
                        elLower.appendChild(responseDom.createTextNode(sLower));
                        elUpper.appendChild(responseDom.createTextNode(sUpper));
                        elField.appendChild(elLower);
                        elField.appendChild(elUpper);
                        record.appendChild(elField);
                        
                        elField = responseDom.createElement("ows:BoundingBox");
                        elLower = responseDom.createElement("ows:LowerCorner");
                        elUpper = responseDom.createElement("ows:UpperCorner");
                        elLower.appendChild(responseDom.createTextNode(sLower));
                        elUpper.appendChild(responseDom.createTextNode(sUpper));
                        elField.appendChild(elLower);
                        elField.appendChild(elUpper);
                        record.appendChild(elField);
                    }
                    
                } else {
                    
                    String sValue = oValue.toString();
                    if (oValue instanceof Timestamp) {
                        if (meaningType.equals(PropertyMeaningType.DATEMODIFIED)) {
                            sValue = opResponse.toIso8601((Timestamp)oValue);
                        } else {
                            sValue = opResponse.toIso8601Date((Timestamp)oValue);
                        }
                    }
                    if (meaningType.equals(PropertyMeaningType.XMLURL)) {
                        if ((sValue != null) && sValue.startsWith("?getxml=")) {
                            sValue = cswBaseUrl+sValue;
                        }
                    } else if (meaningType.equals(PropertyMeaningType.THUMBNAILURL)) {
                        if ((sValue != null) && sValue.startsWith("/thumbnail?uuid")) {
                            sValue = httpContextPath+sValue;
                        }
                    }
                    if ((sValue != null) && (dcElement != null) && (dcElement.getElementName().length() > 0)) {
                        String elName = dcElement.getElementName().replaceAll("~","");
                        Element elField = responseDom.createElement(elName);
                        elField.appendChild(responseDom.createTextNode(sValue));
                        if (dcElement.getScheme().length() > 0) {
                            elField.setAttribute("scheme",dcElement.getScheme());
                            
                            // don't return unknown content types
                            if (sValue.equalsIgnoreCase("unknown")) {
                                if (dcElement.getScheme().toLowerCase().endsWith("contenttype")) {
                                    elField = null;
                                }
                            }
                        }
                        if (elField != null) {
                            record.appendChild(elField);
                        }
                    }
                    
                }
            }
        }
    }
  
  /**
   * Creates and appends the discovered record elements to the XML document.
   * <br/>Applies to csw:GetRecordByIdResponse and csw:GetRecordsResponse.
   * @param context the operation context
   * @param parent the parent element that will hold the records
   */
  protected void appendDiscoveredRecords(OperationContext context, Element parent) 
    throws Exception {
    
    // determine the record element's namespace URI, namespace prefix, 
    // local name and qualified name
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    boolean isDublinCore = qOptions.isDublinCoreResponse();
    
    String elementSetType = Val.chkStr(qOptions.getElementSetType());
    String outputSchema = Val.chkStr(qOptions.getOutputSchema());
    StringSet elementSetTypeNames = qOptions.getElementSetTypeNames();
    boolean isBrief = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Brief);
    boolean isSummary = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Summary);
    boolean isFull = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Full);
    
    // Dublin Core based responses
    if (isDublinCore) {
      OperationResponse opResponse = context.getOperationResponse();
      Document responseDom = opResponse.getResponseDom();
      String recNamespacePfx = "csw";
      String recNamespaceUri = CswNamespaces.CSW_30.URI_CSW();
      String recLocalName = "Record";
      StringSet elementNames = qOptions.getElementNames();
      boolean hasElementNames = (elementNames != null) && (elementNames.size() > 0);
      if (!hasElementNames) {
        if (isBrief) {
          recLocalName = "BriefRecord";
        } else if (isSummary) {
          recLocalName = "SummaryRecord";
        }
      }
      String recQName = recNamespacePfx+":"+recLocalName;
      
      // append each record
      DiscoveredRecords records = query.getResult().getRecords();
      for (DiscoveredRecord record: records) {
        Element elRecord = responseDom.createElementNS(recNamespaceUri,recQName);
        for (Returnable returnable: sortFields(filterFields(record.getFields(),new RejectFileIdentifiersPredicate()))) {
          this.appendDiscoveredField(context,elRecord,returnable);
        }
        parent.appendChild(elRecord);
      }
    
    // non Dublin Core based responses
    } else {
      
      IOriginalXmlProvider oxp = context.getProviderFactory().makeOriginalXmlProvider(context);
      DiscoveredRecords records = query.getResult().getRecords();
      for (DiscoveredRecord record: records) {
        String responseXml = Val.chkStr(record.getResponseXml());
        if (responseXml.length() > 0) {
          Document dom = DomUtil.makeDomFromString(responseXml,true);
          NodeList nl = dom.getChildNodes(); 
          for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
              Node ndXml = nl.item(i);
              Node ndImported = parent.getOwnerDocument().importNode(ndXml,true);
              parent.appendChild(ndImported);
              break;
            }
          }
        } else {
          //TODO throw exception here?
        }        
      }
      
    }
  }
    
  /**
   * Creates and appends the csw:SearchResults element to the XML document.
   * <br/>Applies to csw:GetRecordsResponse.
   * <br/>Discovered records are also created and appended.
   * @param parent the parent element that will hold the results (typically the root)
   */
  protected void appendSearchResultsElement(OperationContext context, Element parent) 
    throws Exception{
    
    // determine records counts
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    OperationResponse opResponse = context.getOperationResponse();
    
    Document responseDom = opResponse.getResponseDom();
    DiscoveryResult result = query.getResult();
    int numberOfRecordsMatched = result.getNumberOfHits();
    int numberOfRecordsReturned = 0;
    int nextRecord = 0;
    String rtname = qOptions.getResultType();
    boolean isHits = rtname.equalsIgnoreCase(CswConstants.ResultType_Hits);
    if (isHits || (query.getFilter().getMaxRecords() <= 0)) {
      if (numberOfRecordsMatched > 0) {
        nextRecord = 1;
      }
    } else {
      numberOfRecordsReturned = result.getRecords().size();
      if (numberOfRecordsReturned > 0) {
        if (numberOfRecordsReturned < numberOfRecordsMatched) {
          nextRecord = query.getFilter().getStartRecord() + numberOfRecordsReturned;
          if (nextRecord > numberOfRecordsMatched) nextRecord = 0;
        }
      }
    } 
      
    // the following attributes are not currently populated:
    //   resultSetId expires
    
    // create and add the status element
    String sTimestamp = opResponse.toIso8601(new Timestamp(System.currentTimeMillis()));
    Element elStatus = responseDom.createElementNS(CswNamespaces.CSW_30.URI_CSW(),"csw:SearchStatus");
    elStatus.setAttribute("timestamp",sTimestamp);
    parent.appendChild(elStatus);
    
    // create and add the results element
    Element elResults = responseDom.createElementNS(CswNamespaces.CSW_30.URI_CSW(),"csw:SearchResults");
    elResults.setAttribute("numberOfRecordsMatched",""+numberOfRecordsMatched);
    elResults.setAttribute("numberOfRecordsReturned",""+numberOfRecordsReturned);
    if (nextRecord >= 0) {
      elResults.setAttribute("nextRecord",""+nextRecord);
    }
    StringSet elementNames = qOptions.getElementNames();
    boolean hasElementNames = (elementNames != null) && (elementNames.size() > 0);
    if (!hasElementNames) {
      String type = qOptions.getElementSetType();
      elResults.setAttribute("elementSet",type);
    }
    if (qOptions.isDublinCoreResponse()) {
      elResults.setAttribute("recordSchema",CswNamespaces.CSW_30.URI_CSW());
    } else {
      elResults.setAttribute("recordSchema",qOptions.getOutputSchema());
    }
    parent.appendChild(elResults);
    
    // append the discovered records
    if (!isHits) {
      this.appendDiscoveredRecords(context,elResults);
    } 
  }
    
  /**
   * Creates and appends the Atom elements to the XML document.
   * <br/>Applies to csw:GetRecordsResponse.
   * <br/>Discovered records are also created and appended.
   * @param root the parent element that will hold the results (typically the root)
   */
  protected void appendAtomResultsElement(OperationContext context) 
    throws Exception{
    String atomns = "http://www.w3.org/2005/Atom";

    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    OperationResponse opResponse = context.getOperationResponse();
    
    Document responseDom = opResponse.getResponseDom();
    
    Element elFeed = responseDom.createElement("feed");
    elFeed.setAttribute("xmlns", atomns);
    elFeed.setAttribute("xmlns:georss", "http://www.georss.org/georss");
    elFeed.setAttribute("xmlns:opensearch", "http://a9.com/-/spec/opensearch/1.1/");
    elFeed.setAttribute("xmlns:dc", CswNamespaces.URI_DC);
    responseDom.appendChild(elFeed);
    
    Element elTitle = responseDom.createElementNS(atomns, "title");
    elTitle.appendChild(responseDom.createTextNode("Title"));
    elFeed.appendChild(elTitle);
    
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);
    Date now = new Date();
    String published = dateFormat.format(now) + "T" + timeFormat.format(now) + "Z";
    
    Element elPublished = responseDom.createElementNS(atomns, "updated");
    elPublished.appendChild(responseDom.createTextNode(published));
    elFeed.appendChild(elPublished);
    
    Element elId = responseDom.createElementNS(atomns, "id");
    elId.appendChild(responseDom.createTextNode("urn:uuid:"+UuidUtil.makeUuid()));
    elFeed.appendChild(elId);
    
    DiscoveredRecords records = query.getResult().getRecords();
    for (DiscoveredRecord record: records) {
        AtomEntry atomEntry = new AtomEntry();
        DiscoveredRecordAdapter discoveredRecordAdapter = new DiscoveredRecordAdapter(new ResourceIdentifier(), record);
        atomEntry.setData(discoveredRecordAdapter);
        atomEntry.AppendTo(elFeed);
    }
    
    /*
    // determine records counts
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    
    Document responseDom = opResponse.getResponseDom();
    DiscoveryResult result = query.getResult();
    int numberOfRecordsMatched = result.getNumberOfHits();
    int numberOfRecordsReturned = 0;
    int nextRecord = 0;
    String rtname = qOptions.getResultType();
    boolean isHits = rtname.equalsIgnoreCase(CswConstants.ResultType_Hits);
    if (isHits || (query.getFilter().getMaxRecords() <= 0)) {
      if (numberOfRecordsMatched > 0) {
        nextRecord = 1;
      }
    } else {
      numberOfRecordsReturned = result.getRecords().size();
      if (numberOfRecordsReturned > 0) {
        if (numberOfRecordsReturned < numberOfRecordsMatched) {
          nextRecord = query.getFilter().getStartRecord() + numberOfRecordsReturned;
          if (nextRecord > numberOfRecordsMatched) nextRecord = 0;
        }
      }
    } 
      
    // the following attributes are not currently populated:
    //   resultSetId expires
    
    // create and add the status element
    String sTimestamp = opResponse.toIso8601(new Timestamp(System.currentTimeMillis()));
    Element elStatus = responseDom.createElementNS(CswNamespaces.CSW_30.URI_CSW(),"csw:SearchStatus");
    elStatus.setAttribute("timestamp",sTimestamp);
    parent.appendChild(elStatus);
    
    // create and add the results element
    Element elResults = responseDom.createElementNS(CswNamespaces.CSW_30.URI_CSW(),"csw:SearchResults");
    elResults.setAttribute("numberOfRecordsMatched",""+numberOfRecordsMatched);
    elResults.setAttribute("numberOfRecordsReturned",""+numberOfRecordsReturned);
    if (nextRecord >= 0) {
      elResults.setAttribute("nextRecord",""+nextRecord);
    }
    StringSet elementNames = qOptions.getElementNames();
    boolean hasElementNames = (elementNames != null) && (elementNames.size() > 0);
    if (!hasElementNames) {
      String type = qOptions.getElementSetType();
      elResults.setAttribute("elementSet",type);
    }
    if (qOptions.isDublinCoreResponse()) {
      elResults.setAttribute("recordSchema",CswNamespaces.CSW_30.URI_CSW());
    } else {
      elResults.setAttribute("recordSchema",qOptions.getOutputSchema());
    }
    parent.appendChild(elResults);
    
    // append the discovered records
    if (!isHits) {
      this.appendDiscoveredRecords(context,elResults);
    } 
    */
  }
  
  /**
   * Generates the response.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void generateResponse(OperationContext context) throws Exception { 
    String opName = Val.chkStr(context.getOperationName());
    
    if (opName.equalsIgnoreCase(CswConstants.Operation_GetRecordById)) {
      LOGGER.finer("Generating GetRecordByIdResponse...");
      OperationResponse opResponse = context.getOperationResponse();
      Document root = this.newResponseDom(context);
      this.appendDiscoveredRecords(context,root);
      opResponse.setResponseXml(XmlIoUtil.domToString(opResponse.getResponseDom()));
      
    } else if (opName.equalsIgnoreCase(CswConstants.Operation_GetRecords)) {
      LOGGER.finer("Generating GetRecordsResponse...");
      OperationResponse opResponse = context.getOperationResponse();
      if (!context.isAtomResponse()) {
        Element root = this.newResponseDom(context,"GetRecordsResponse");
        this.appendSearchResultsElement(context,root);
      } else {
        this.newResponseDom(context);
        this.appendAtomResultsElement(context);
      }
      opResponse.setResponseXml(XmlIoUtil.domToString(opResponse.getResponseDom()));
    }
  }
  
  public Document newResponseDom(OperationContext context) throws Exception {
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    if (qOptions.isDublinCoreResponse()) {
      Document dom = context.getOperationResponse().newResponseDom();
      context.getOperationResponse().setResponseDom(dom);
      return dom;
    } else {
      Document dom = DomUtil.newDocument();
      context.getOperationResponse().setResponseDom(dom);
      return dom;
    }
  }
  
  /**
   * Creates a new XML document for response construction.
   * @param rootName the name of the root element
   * @return the root element
   * @throws Exception if a processing exception occurs
   */
  public Element newResponseDom(OperationContext context,String rootName) throws Exception {
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    if (qOptions.isDublinCoreResponse()) {
      return context.getOperationResponse().newResponseDom(rootName);
    } else {
      Document dom = DomUtil.newDocument();
      if (!rootName.startsWith("csw:")) {
        rootName = "csw:"+rootName;
      }
      Element root = dom.createElementNS(CswNamespaces.CSW_30.URI_CSW(),rootName);
      root.setAttribute("xmlns:csw",CswNamespaces.CSW_30.URI_CSW());
      dom.appendChild(root);
      context.getOperationResponse().setResponseDom(dom);
      return root;
    }
  }

    protected void appendDiscoveredRecords(OperationContext context, Document root) throws Exception {
    
    // determine the record element's namespace URI, namespace prefix, 
    // local name and qualified name
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
    boolean isDublinCore = context.isDublinCoreResponse();
    boolean isAtom = context.isAtomResponse();
    
    String elementSetType = Val.chkStr(qOptions.getElementSetType());
    String outputSchema = Val.chkStr(qOptions.getOutputSchema());
    StringSet elementSetTypeNames = qOptions.getElementSetTypeNames();
    boolean isBrief = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Brief);
    boolean isSummary = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Summary);
    boolean isFull = elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Full);
    
    // Dublin Core based responses
    if (isAtom) {
        CharArrayWriter writer = new CharArrayWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        AtomEntry atomEntry = new AtomEntry();
        
        DiscoveredRecords records = query.getResult().getRecords();
        if (records.size()!=1) {
            context.getOperationResponse().setResponseCode(HttpServletResponse.SC_NOT_FOUND);
            throw new OwsException(OwsException.OWSCODE_UnexpectedStatus,"record",records.isEmpty()? "no records found": "to many records found");
        }
        
        DiscoveredRecordAdapter discoveredRecordAdapter = new DiscoveredRecordAdapter(new ResourceIdentifier(), records.get(0));
        atomEntry.setData(discoveredRecordAdapter);
        atomEntry.WriteNsTo(writer);

        String xml = writer.toString();
        Document dom = DomUtil.makeDomFromString(xml,true);
        NodeList nl = dom.getChildNodes(); 
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
              Node ndXml = nl.item(i);
              Node ndImported = root.importNode(ndXml,true);
              if (ndImported instanceof Element) {
                context.getOperationResponse().setNSAttributes((Element) ndImported);
                Element dcIdentifier = root.createElement("dc:identifier");
                Text textNode = root.createTextNode(discoveredRecordAdapter.getUuid());
                dcIdentifier.appendChild(textNode);
                ndImported.appendChild(dcIdentifier);
              }
              root.appendChild(ndImported);
              xml = XmlIoUtil.domToString(root);
              break;
            }
        }
        
    } else if (isDublinCore) {
      OperationResponse opResponse = context.getOperationResponse();
      Document responseDom = opResponse.getResponseDom();
      String recNamespacePfx = "csw";
      String recNamespaceUri = CswNamespaces.CSW_30.URI_CSW();
      String recLocalName = "Record";
      StringSet elementNames = qOptions.getElementNames();
      boolean hasElementNames = (elementNames != null) && (elementNames.size() > 0);
      if (!hasElementNames) {
        if (isBrief) {
          recLocalName = "BriefRecord";
        } else if (isSummary) {
          recLocalName = "SummaryRecord";
        }
      }
      String recQName = recNamespacePfx+":"+recLocalName;
      
      // append each record
      DiscoveredRecords records = query.getResult().getRecords();
      if (records.size()!=1) {
          context.getOperationResponse().setResponseCode(HttpServletResponse.SC_NOT_FOUND);
          throw new OwsException(OwsException.OWSCODE_UnexpectedStatus,"record",records.isEmpty()? "no records found": "to many records found");
      }
      
      for (DiscoveredRecord record: records) {
        Element elRecord = responseDom.createElementNS(recNamespaceUri,recQName);
        for (Returnable returnable: sortFields(record.getFields())) {
          this.appendDiscoveredField(context,elRecord,returnable);
        }
        context.getOperationResponse().setNSAttributes(elRecord);
        root.appendChild(elRecord);
      }
    } else {
        // non Dublin Core based responses
      
      IOriginalXmlProvider oxp = context.getProviderFactory().makeOriginalXmlProvider(context);
      DiscoveredRecords records = query.getResult().getRecords();
      if (records.size()!=1) {
          context.getOperationResponse().setResponseCode(HttpServletResponse.SC_NOT_FOUND);
          throw new OwsException(OwsException.OWSCODE_UnexpectedStatus,"record",records.isEmpty()? "no records found": "to many records found");
      }
      for (DiscoveredRecord record: records) {
        String responseXml = Val.chkStr(record.getResponseXml());
        if (responseXml.length() > 0) {
          Document dom = DomUtil.makeDomFromString(responseXml,true);
          NodeList nl = dom.getChildNodes(); 
          for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
              Node ndXml = nl.item(i);
              Node ndImported = root.importNode(ndXml,true);
              if (ndImported instanceof Element) {
                context.getOperationResponse().setNSAttributes((Element) ndImported);
              }
              root.appendChild(ndImported);
              break;
            }
          }
        } else {
          //TODO throw exception here?
        }        
      }
      
    }
    }
    
    /**
     * Filters fields.
     * @param colFields collection of fields
     * @param predicate predicate used to filter fields
     * @return 
     */
    private Collection<Returnable> filterFields(Collection<Returnable> colFields, IPredicate<Returnable> predicate) {
        ArrayList<Returnable> arrFields = new ArrayList<Returnable>();
        for (Returnable returnable: colFields) {
            if (predicate==null || predicate.accept(returnable)) {
                arrFields.add(returnable);
            }
        }
        return arrFields;
    }
    
    /**
     * Sorts fields.
     * <p/>
     * The goal of sorting is to push any geometry field at the end while all
     * other fields should stay in the same position
     * @param colFields collection of filters to sort.
     * @return sorted fields
     */
    private Collection<Returnable> sortFields(Collection<Returnable> colFields) {
        ArrayList<Returnable> arrFields = new ArrayList<Returnable>(colFields);
        Collections.sort(arrFields, new Comparator<Returnable>() {
            @Override
            public int compare(Returnable o1, Returnable o2) {
                boolean g1 = isGeometry(o1);
                boolean g2 = isGeometry(o2);
                
                if (g1==g2) return 0;
                if (g1) return 1;
                if (g2) return -1;
                
                return 0;
            }
            
            private boolean isGeometry(Returnable r) {
                PropertyMeaning meaning = r.getMeaning();
                return meaning.getValueType().equals(PropertyValueType.GEOMETRY);
            }
        });
        return arrFields;
    }
  
    /**
     * Filter fredicate.
     * 
     * @param <T> 
     */
    private static interface IPredicate<T> {
        /**
         * Decides if the element should be accepted or rejected during filtering.
         * @param element element to interrogate
         * @return <code>true</code> if element should be accepted
         */
        boolean accept(T element);
    }
    
    /**
     * Predicate to reject any file identifiers.
     */
    private static class RejectFileIdentifiersPredicate implements IPredicate<Returnable> {

        @Override
        public boolean accept(Returnable element) {
            return element.getMeaning().getMeaningType()!=PropertyMeaningType.FILEIDENTIFIER;
        }
        
    }
}
