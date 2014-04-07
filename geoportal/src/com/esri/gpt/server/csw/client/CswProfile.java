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
package com.esri.gpt.server.csw.client;

import com.esri.gpt.framework.search.DcList;
import com.esri.gpt.framework.search.SearchXslProfile;
import com.esri.gpt.framework.util.ResourceXml;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

 

/**
 * The Class CswProfile.  Hold the class statically if you intend to use
 * the XSLT templates.
 * 
 */
public class CswProfile extends 
  SearchXslProfile<CswSearchCriteria, CswRecord, CswRecords, CswResult> {
	
// class variables =============================================================
	/** The class logger *. */
private static Logger LOG = Logger.getLogger(CswProfile.class
	                                    .getCanonicalName());	
private static final Pattern XML_TEST_PATTERN = Pattern.compile("^\\p{Space}*(<!--(.|\\p{Space})*?-->\\p{Space}*)+<\\?xml");	
// instance variables ==========================================================


/** The filter_extentsearch. */
private boolean            filter_extentsearch;

/** The filter_livedatamap. */
private boolean            filter_livedatamap;

/** Used to get element with the metadata document during getElementBy Id. */
private final static String SCHEME_METADATA_DOCUMENT = 
  "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document";

// constructors ================================================================
/**
 * Instantiates a new csw profile.
 */
public CswProfile() {
}

/**
 * The Constructor.
 * 
 * @param sid the sid
 * @param sname the sname
 * @param sdescription the sdescription
 */
public CswProfile(String sid, String sname, String sdescription) {
}

/**
 * The Constructor.
 * 
 * @param id the id
 * @param name the name
 * @param description the description
 * @param kvp the kvp
 * @param requestxslt the requestxslt
 * @param responsexslt the responsexslt
 * @param metadataxslt the metadataxslt
 * @param livedatamap the livedatamap
 * @param extentsearch the extentsearch
 */
public CswProfile(String id, String name, String description, String kvp,
    String requestxslt, String responsexslt, String metadataxslt,
    boolean livedatamap, boolean extentsearch) {
  this.setId(id);
  this.setName(name);
  this.setDescription(description);
  this.setKvp(kvp);
  this.setMetadataxslt(metadataxslt);
  this.setResponsexslt(responsexslt);
  this.setRequestxslt(requestxslt);
  this.filter_livedatamap = livedatamap;
  this.filter_extentsearch = extentsearch;

}

// properties ==================================================================

// methods =====================================================================

/**
 * Generate a CSW request String to get metadata by ID.
 * The CSW request String is built. The request is String is build based
 * on the baseurl and record id
 * 
 * @param baseURL the base URL
 * @param recordId the record id
 * 
 * @return The request String
 * 
 *
 */
public String generateCSWGetMetadataByIDRequestURL(String baseURL,
    String recordId) {
  String kvp = this.getKvp();
  recordId = Utils.chkStr(recordId);
  try {
    recordId = URLEncoder.encode(recordId, "UTF-8");
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Could not encode record Id " + recordId, e);
  }
  StringBuffer requeststring = new StringBuffer();
  requeststring.append(baseURL);
  if (baseURL.endsWith("?")){
    requeststring.append(Utils.chkStr(kvp));
  }
  else if (baseURL.contains("?")) {
    requeststring = new StringBuffer();
    requeststring.append(baseURL.substring(0, baseURL.indexOf("?") + 1) + Utils.chkStr(kvp));
  } else if(kvp.contains("{0}") &&
      !kvp.contains("=")) {
    requeststring.append(kvp);
    // taking out need for question mark
  }
  else {
    requeststring.append("?" + Utils.chkStr(kvp));
  }

  if(requeststring.toString().contains("{0}")) {
    String tmp = requeststring.toString().replace("{0}", recordId);
    requeststring = new StringBuffer(tmp);
  } else  {
    requeststring.append("&ID=" + recordId);
  }
  return requeststring.toString();


}


/**
 * Generate a CSW request String.
 * First, create a simple common form of request xml
 * Then, transform the request xml into a real request xml using profile specific xslt
 * 
 * The CSW request String is built. The request is String is build based
 * on the request xslt.
 * 
 * @param search the search
 * 
 * @return The request String
 * 
 * @throws TransformerException the transformer exception
 * @throws IOException Signals that an I/O exception has occurred.
 * 
 *  
 */
public String generateCSWGetRecordsRequest(CswSearchCriteria search)
    throws TransformerException, IOException {
  // Build xml
  String request = "<?xml version='1.0' encoding='UTF-8' ?>";
  request += "<GetRecords>" + "<StartPosition>" + search.getStartPosition()
      + "</StartPosition>";
  request += "<MaxRecords>" + search.getMaxRecords() + "</MaxRecords>";
  request += "<KeyWord>" + this.XmlEscape(search.getSearchText())
      + "</KeyWord>";
  request += ("<LiveDataMap>" + search.isLiveDataAndMapsOnly() + "</LiveDataMap>");
  if (search.getEnvelope() != null) {
    request += ("<Envelope>");
    request += "<MinX>" + search.getEnvelope().getMinX() + "</MinX>";
    request += "<MinY>" + search.getEnvelope().getMinY() + "</MinY>";
    request += "<MaxX>" + search.getEnvelope().getMaxX() + "</MaxX>";
    request += "<MaxY>" + search.getEnvelope().getMaxY() + "</MaxY>";
    request += "</Envelope>";
    request += "<RecordsFullyWithinEnvelope>"+ search.isEnvelopeContains() +"</RecordsFullyWithinEnvelope>";
    request += "<RecordsIntersectWithEnvelope>"+ search.isEnvelopeIntersects() +"</RecordsIntersectWithEnvelope>";
  }
  request += "</GetRecords>";
  

  LOG.fine("Internal CSW Request = \n"+ request);

  //Get an XSL Transformer object
  String requestStr = this.getRequestxsltobj().transform(request);
  
  return requestStr;
}

/**
 * Read a CSW metadata response for search engine local.  
 * Will populate record referenceList and record metadataResourceUrl
 * The CSW metadata response is read. The CSw record is updated with the
 * metadata
 * 
 * @param response the response
 * @param record the record
 * 
 * @throws TransformerException the transformer exception
 * 
 */
public void readCSWGetMetadataByIDResponseLocal(String response, CswRecord record)
    throws TransformerException {
  String metadataxslt = this.getMetadataxslt();
  if (metadataxslt == null || metadataxslt.equals("")) {
    record.setFullMetadata(Utils.chkStr(response));
  } else {
 
    LOG.finer("Transforming GetRecordByID intermidiate xml to GetRecordById " +
    		"Native");
    
    String result = this.getMetadataXsltObj().transform(Utils.chkStr(response));
        
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
 * Read a CSW metadata response.  Will populate record referenceList and record metadataResourceUrl
 * The CSW metadata response is read. The CSw record is updated with the
 * metadata
 * 
 * @param recordByIdResponse the response
 * @param record the record
 * 
 * @throws TransformerException the transformer exception
 * @throws IOException Exception while reading 
 * 
 */
public void readCSWGetMetadataByIDResponse(CswClient cswClient, String recordByIdResponse, CswRecord record)
    throws TransformerException, IOException {
  String metadataxslt = this.getMetadataxslt();
  if (metadataxslt == null || metadataxslt.equals("")) {
    record.setFullMetadata(Utils.chkStr(recordByIdResponse));
  } else {
 
    LOG.finer("Transforming GetRecordByID intermidiate xml to GetRecordById " +
    		"Native");
    
    String sRecordByIdXslt = this.getMetadataXsltObj()
      .transform(Utils.chkStr(recordByIdResponse));
        
    String xmlUrl = null;
    String dctReferences = sRecordByIdXslt;
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
    // T.M.  Adds so that xslt for ouput xml for transform Metadata can be
    // used
    String indirectUrlXml = null;
    if(!Val.chkStr(record.getMetadataResourceURL()).equals("")) {
      InputStream istRealDoc = cswClient.submitHttpRequest("GET", 
          Utils.chkStr(record.getMetadataResourceURL()), "", "", "");
      indirectUrlXml = Val.chkStr(Utils.getInputString2(istRealDoc));
    }
    
    if(!Val.chkStr(indirectUrlXml).equals("")) {
      // Indirect xml
      record.setFullMetadata(indirectUrlXml);
    } else if(!Val.chkStr(sRecordByIdXslt).equals("")) {
    	
      try {
    	  // Check if it is an  xml document
    	  XmlIoUtil.transform(sRecordByIdXslt);
    	  record.setFullMetadata(sRecordByIdXslt);
      } catch(Exception e) {
        ResourceXml resourceXml = new ResourceXml();
        String fullMetadata = resourceXml.makeResourceFromCswResponse(recordByIdResponse, record.getId());
        record.setFullMetadata(fullMetadata); 
      }
    } else {
      // The get record by id
      record.setFullMetadata(recordByIdResponse);
    } 
  }

}


/**
 * Parse a CSW response.
 * The CSW response is parsed and the records collection is populated with
 * the result.The reponse is parsed based on the response xslt.
 * 
 * @param src the src
 * @param recordList the record list
 * 
 * @throws TransformerException the transformer exception
 * @throws ParserConfigurationException the parser configuration exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws SAXException the SAX exception
 * @throws XPathExpressionException the x path expression exception
 */
public void readCSWGetRecordsResponse(String src, CswRecords recordList)
    throws TransformerException, SAXException, IOException,
    ParserConfigurationException, XPathExpressionException {
    CswResult result = new CswResult();
    super.readGetRecordsResponse(src, result);
    recordList.addAll(result.getRecords());
}


/**
 * Support content type query.
 * 
 * @return true, if successful
 */
@Override
public boolean SupportContentTypeQuery() {
  return isSupportsContentTypeQuery();
}

/* (non-Javadoc)
 * @see com.esri.gpt.framework.search.SearchXslProfile#SupportSpatialBoundary()
 */
@Override
public boolean SupportSpatialBoundary() {
  return isSupportsSpatialBoundary();
}

/**
 * Support spatial query.
 * 
 * @return true, if successful
 */
@Override
public boolean SupportSpatialQuery() {
  return isSupportsSpatialQuery();
}

/**
 * replace special xml character.
 * Encode special characters (such as &, ", <, >, ') to percent values.
 * </remarks>
 * <param name="data
 * 
 * @param data the data
 * 
 * @return the string
 * 
 *  
 */
private String XmlEscape(String data) {
  data = data.replace("&", "&amp;");
  data = data.replace("<", "&lt;");
  data = data.replace(">", "&gt;");
  data = data.replace("\"", "&quot;");
  data = data.replace("'", "&apos;");

  return data;
}

/**
 * Checks if is filter_extentsearch.
 * 
 * @return true, if is filter_extentsearch
 */
@Override
public boolean isFilter_extentsearch() {
  return filter_extentsearch;
}

/**
 * Sets the filter_extentsearch.
 * 
 * @param filter_extentsearch the new filter_extentsearch
 */
@Override
public void setFilter_extentsearch(boolean filter_extentsearch) {
  this.filter_extentsearch = filter_extentsearch;
}

/**
 * Checks if is filter_livedatamap.
 * 
 * @return true, if is filter_livedatamap
 */
@Override
public boolean isFilter_livedatamap() {
  return filter_livedatamap;
}

/**
 * Sets the filter_livedatamap.
 * 
 * @param filter_livedatamap the new filter_livedatamap
 */
@Override
public void setFilter_livedatamap(boolean filter_livedatamap) {
  this.filter_livedatamap = filter_livedatamap;
}

/**
 * Read get metadata by id response.
 * 
 * @param response the response
 * @param record the record
 * @throws TransformerException the transformer exception
 * @see com.esri.gpt.framework.search.SearchXslProfile#readGetMetadataByIDResponse(java.lang.String, com.esri.gpt.framework.request.Record)
 */
@Override
public void readGetMetadataByIDResponse(String response, CswRecord record)
    throws TransformerException {
  // TODO Auto-generated method stub
  
}

}
