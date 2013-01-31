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
package com.esri.gpt.control.webharvest.client.arcims;

import com.esri.gpt.catalog.arcims.ExtendedQueryRequest;
import com.esri.gpt.catalog.arcims.ExtendedQueryRequest.SpatialOperator;
import com.esri.gpt.catalog.arcims.HarvestMetadataRequest;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.search.ISearchFilterSpatialObj.OptionsBounds;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * ArcIMS service proxy.
 */
class ArcImsProxy {
/** logger */
private static final Logger LOGGER = Logger.getLogger(ArcImsProxy.class.getCanonicalName());
/** service info */
private ArcImsInfo info;

/**
 * Creates instance of the proxy.
 * @param info service info
 */
public ArcImsProxy(ArcImsInfo info) {
  if (info==null) throw new IllegalArgumentException("No info provided.");
  this.info = info;
}

/**
 * Gets query request based on the criteria.
 * @param criteria criteria
 * @return query request
 */
public ExtendedQueryRequest getQueryRequest(Criteria criteria) {
    ExtendedQueryRequest queryRequest = new ExtendedQueryRequest();
    queryRequest.setService(info.newService());
    queryRequest.setCredentials(info.newCredentials());
    queryRequest.setBBox(criteria!=null? criteria.getBBox(): null);
    queryRequest.setSpatialOperator(criteria!=null? convertBBoxOptions(criteria.getBBoxOption()): null);
    queryRequest.setFullText(criteria!=null && criteria.getSearchText()!=null? criteria.getSearchText().split(" "): null);
    queryRequest.setMaxRec(criteria!=null && criteria.getMaxRecords()!=null? criteria.getMaxRecords(): 0);
    return queryRequest;
}

public String read(String sourceUri) throws IOException {
  LOGGER.finer("Reading metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
  try {
    HarvestMetadataRequest harvestMetadataRequest = new HarvestMetadataRequest();
    harvestMetadataRequest.setService(info.newService());
    harvestMetadataRequest.setCredentials(info.newCredentials());
    harvestMetadataRequest.executeHarvest(sourceUri);

    String md = harvestMetadataRequest.getMetadata();
    if (md.length()==0) {
      LOGGER.finer("Reading metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this+"; Received no METADATA response.");
      return "";
    }

    // this section 'fixes' metadata dataset information; it happens, that some
    // of the elements are having invalid XML characters and that would cause
    // parsing to fail.
    Pattern pattern = Pattern.compile("\"[^\"]+\"");
    Matcher matcher = pattern.matcher(md);
    int start = 0;
    ArrayList<int[]> sections = new ArrayList<int[]>();
    while (matcher.find(start)) {
      int [] section = new int[]{ matcher.start()+1, matcher.end()-1 };
      sections.add(section);
      start = matcher.end()+1;
    }
    for (int i = sections.size()-1; i>=0; i--) {
      int [] section = sections.get(i);
      String text = md.substring(section[0], section[1]);
      text = Val.escapeXml(text);
      md  = md.substring(0,section[0]) + text + md.substring(section[1], md.length());
    }

    Document doc = DomUtil.makeDomFromString(md, false);

    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();

    String url = (String) xPath.evaluate("/METADATA/METADATA_DATASET/@url", doc, XPathConstants.STRING);
    LOGGER.finer("Reading metadata of id: \"" +sourceUri+ "\" through proxy: "+this+"; Received no metadata URL within METADATA response.");
    if (url.length()==0) return "";

    url = Val.chkStr(url).replaceAll("\\{", "%7B").replaceAll("\\}", "%7D");
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(url);

    StringHandler sh = new StringHandler();
    cr.setContentHandler(sh);

    cr.execute();

    String mdText = sh.getContent();
    LOGGER.finer("Received metadata of id: \"" +sourceUri+ "\" through proxy: "+this);
    LOGGER.finest(mdText);
    return mdText;

  } catch (XPathExpressionException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (SAXException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (ParserConfigurationException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (ImsServiceException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  }
}

@Override
public String toString() {
  return info.toString();
}


/**
 * Converts bounding box options bounds to spatial operator.
 * @param options bounding box options bounds
 * @return spatial operator
 */
private SpatialOperator convertBBoxOptions(OptionsBounds options) {
  if (options!=null) {
    switch (options) {
      case useGeogExtent: return SpatialOperator.overlaps;
      case dataWithinExtent: return SpatialOperator.within;
      default: return SpatialOperator.anywhere;
    }
  } else {
    return SpatialOperator.anywhere;
  }
}

}
