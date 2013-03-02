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
package com.esri.gpt.catalog.arcims;

import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Query request.
 */
public class QueryRequest extends HarvestRequest {

// class variables =============================================================
/** date format */
private static final SimpleDateFormat DATE_FORMAT =
  new SimpleDateFormat("yyyy-MM-dd");

// instance variables ==========================================================
/** maximum records to query; 0 - no limit. */
private int _maxRec;
/** Update date 'after' */
private Date _updatedAfterDate;
/** Update date 'before' */
private Date _updatedBeforeDate;
/** selected uuids */
private Set<String> _uuids = new TreeSet<String>();
// constructors ================================================================
/**
 * Creates instance of the request.
 */
public QueryRequest() {
  super();
}

/**
 * Creates instance of the request.
 * @param credentials credentials
 */
public QueryRequest(UsernamePasswordCredentials credentials) {
  super(credentials);
}
// properties ==================================================================
/**
 * Gets maximum number of records to query.
 * @return maximum number of records to query, or <code>0</code> if no such a 
 * limit
 */
public int getMaxRec() {
  return _maxRec;
}

/**
 * Sets maximum number of records to query.
 * @param maxRec maximum number of records to query, or <code>0</code> if no 
 * such a limit
 */
public void setMaxRec(int maxRec) {
  _maxRec = Math.max(0, maxRec);
}

/**
 * Gets update date 'after'.
 * @return date or <code>null</code> if no limit set
 */
public Date getUpdatedAfterDate() {
  return _updatedAfterDate;
}

/**
 * Sets update date 'after'.
 * @param updatedAfterDate date or <code>null</code> if no limit
 */
public void setUpdatedAfterDate(Date updatedAfterDate) {
  _updatedAfterDate = updatedAfterDate;
}

/**
 * Gets update date 'before'.
 * @return date or <code>null</code> if no limit set
 */
public Date getUpdatedBeforeDate() {
  return _updatedBeforeDate;
}

/**
 * Sets update date 'before'.
 * @param updatedBeforeDate date or <code>null</code> if no limit
 */
public void setUpdatedBeforeDate(Date updatedBeforeDate) {
  _updatedBeforeDate = updatedBeforeDate;
}

/**
 * Gets selected uuids.
 * @return collection of uuids
 */
public Set<String> getUuids() {
  return _uuids;
}
// methods =====================================================================
/**
 * Executes query.
 * @throws ImsServiceException if error executing query
 */
public void execute()
  throws ImsServiceException {

  _uuids.clear();

  StringBuilder sb = new StringBuilder();
  sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sb.append("\r\n<ARCXML version=\"1.1\">");
  sb.append("\r\n<REQUEST>");
  sb.append("\r\n<GET_METADATA>");
  sb.append("\r\n<SEARCH_METADATA fulloutput=\"false\"");
  if (getMaxRec() > 0) {
    sb.append(" maxresults=\"");
    sb.append(Integer.toString(getMaxRec()));
    sb.append("\"");
  }
  sb.append(">");

  if (getUpdatedAfterDate() != null || getUpdatedAfterDate() != null) {
    sb.append("\r\n<SEARCH_METADATA");

    if (getUpdatedAfterDate() != null) {
      sb.append(" after=\"");
      sb.append(DATE_FORMAT.format(getUpdatedAfterDate()));
      sb.append("\"");
    }

    if (getUpdatedBeforeDate() != null) {
      sb.append(" before=\"");
      sb.append(DATE_FORMAT.format(getUpdatedBeforeDate()));
      sb.append("\"");
    }
    sb.append("/>");
  }
  sb.append("\r\n</SEARCH_METADATA>");
  sb.append("\r\n</GET_METADATA>");
  sb.append("\r\n</REQUEST>");
  sb.append("\r\n</ARCXML>");

  setAxlRequest(sb.toString());

  executeRequest();

  if (wasActionOK()) {
    try {
      Document document =
        DomUtil.makeDomFromString(getAxlResponse(), false);


      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xPath.evaluate(
        "ARCXML/RESPONSE/METADATA/METADATA_DATASET/@docid",
        document, XPathConstants.NODESET);

      for (int i=0; i<nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        String docid = Val.chkStr(node.getNodeValue());
        if (docid.length() > 0) {
          _uuids.add(docid);
        }
      }

    } catch (XPathExpressionException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (ParserConfigurationException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (SAXException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (IOException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    }
  }

}
}
