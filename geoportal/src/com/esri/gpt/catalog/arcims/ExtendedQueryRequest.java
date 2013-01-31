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

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
 * Extended Arc IMS query request. Provides capability to specify additional
 * search/query parameters: text, bounding box, bounding box bounds.
 */
public class ExtendedQueryRequest extends QueryRequest {
/** date format */
private static final SimpleDateFormat DATE_FORMAT =
  new SimpleDateFormat("yyyy-MM-dd");

/** full text */
private String [] fullText = new String[]{};
/** bounding box */
private Envelope bbox;
/** spatial operator */
private SpatialOperator spatialOperator = SpatialOperator.anywhere;
/** folders */
private TreeSet<String> folders = new TreeSet<String>();

/**
 * Creates instance of the request.
 */
public ExtendedQueryRequest() {
  super();
}

/**
 * Creates instance of the request.
 * @param credentials credentials
 */
public ExtendedQueryRequest(UsernamePasswordCredentials credentials) {
  super(credentials);
}

/**
 * Sets full text to search.
 * @param fullText full text to search
 */
public void setFullText(String [] fullText) {
  this.fullText = fullText!=null? fullText: new String[]{};
}

/**
 * Gets full text to search.
 * @return full text to search
 */
public String [] getFullText() {
  return fullText;
}

/**
 * Sets bounding box.
 * @param bbox bounding box
 */
public void setBBox(Envelope bbox) {
  this.bbox = bbox;
}

/**
 * Gets bounding box.
 * @return bounding box
 */
public Envelope getBBox() {
  return bbox;
}

/**
 * Sets spatial operator.
 * @param spatialOperator spatial operator
 */
public void setSpatialOperator(SpatialOperator spatialOperator) {
  this.spatialOperator = spatialOperator!=null? spatialOperator: SpatialOperator.anywhere;
}

/**
 * Gets spatial operator.
 * @return spatial operator
 */
public SpatialOperator getSpatialOperator() {
  return spatialOperator;
}

/**
 * Gets folders.
 * @return folders
 */
private TreeSet<String> getFolders() {
  return folders;
}

/**
 * Makes AXL request.
 * @return AXL request
 */
private String makeAxlRequest() {
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

  if (!getFolders().isEmpty()) {
    String folderUuid = getFolders().first();
    getFolders().remove(folderUuid);
    sb.append("\r\n<SEARCH_METADATA><SUBSET type=\"children\" docid=\"" +folderUuid+ "\"/></SEARCH_METADATA>");
  }

  if (getBBox()!=null && getSpatialOperator()!=SpatialOperator.anywhere) {
    sb.append("\r\n<ENVELOPE");
    sb.append(" minx=\"" +getBBox().getMinX()+ "\"");
    sb.append(" miny=\"" +getBBox().getMinY()+ "\"");
    sb.append(" maxx=\"" +getBBox().getMaxX()+ "\"");
    sb.append(" maxy=\"" +getBBox().getMaxY()+ "\"");
    sb.append(" spatialoperator=\"" +getSpatialOperator()+ "\"");
    sb.append("/>");
  } else {
    sb.append("\r\n<ENVELOPE");
    sb.append(" minx=\"-180\"");
    sb.append(" miny=\"-90\"");
    sb.append(" maxx=\"180\"");
    sb.append(" maxy=\"90\"");
    sb.append(" spatialoperator=\"overlaps\"");
    sb.append("/>");
  }
  
  if (getFullText()!=null && getFullText().length>0) {
    for (String word : getFullText()) {
      word = Val.chkStr(word);
      if (word.length()>0) {
        sb.append("\r\n<FULLTEXT word=\"" +word+ "\"/>");
      }
    }
  }

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

  return sb.toString();
}

/**
 * Executes query.
 * @throws ImsServiceException if error executing query
 */
  @Override
public void execute() throws ImsServiceException {

  getUuids().clear();
  getFolders().clear();

  do {
    setAxlRequest(makeAxlRequest());

    executeRequest();

    if (wasActionOK()) {
      try {
        Document document =
          DomUtil.makeDomFromString(getAxlResponse(), false);


        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.evaluate(
          "ARCXML/RESPONSE/METADATA/METADATA_DATASET",
          document, XPathConstants.NODESET);

        for (int i=0; i<nodeList.getLength(); i++) {
          if (Thread.currentThread().isInterrupted()) break;
          if (getMaxRec()>0 && getUuids().size()>=getMaxRec()) break;
          Node node = nodeList.item(i);
          String docid   = (String) xPath.evaluate("@docid", node, XPathConstants.STRING);
          String name    = (String) xPath.evaluate("@name", node, XPathConstants.STRING);
          boolean folder = Val.chkBool((String) xPath.evaluate("@folder", node, XPathConstants.STRING), false);
          if (!folder)
            getUuids().add(docid);
          else
            getFolders().add(docid);
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
  } while (!getFolders().isEmpty() && (getMaxRec()<=0 || getUuids().size()<getMaxRec()));

}

/**
 * Spatial operator.
 */
public static enum SpatialOperator {
  /** anywhere in the World */
  anywhere,
  /** may overlap specified bounding box */
  overlaps,
  /** has to completly contain within specified bounding box */
  within
}
}
