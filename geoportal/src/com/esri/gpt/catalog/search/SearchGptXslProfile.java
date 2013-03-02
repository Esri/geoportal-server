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

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.gpt.framework.request.Criteria;
import com.esri.gpt.framework.request.QueryResult;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.request.Records;
import com.esri.gpt.framework.search.SearchXslProfile;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * The Class SearchGptProfile.
 */
public class SearchGptXslProfile extends 
  SearchXslProfile<SearchCriteria, SearchResultRecord, SearchResultRecords, 
  SearchResult> {

// class variables  ============================================================
/**
 * Defines format should be used to output to the xsl
 */

/** Class logger */
public Logger LOG = Logger.getLogger(
    SearchGptXslProfile.class.getCanonicalName());

/** The xslt param search url. */
public static String XSLT_PARAM_SEARCH_URL = "searchUrl";

// instance variables ==========================================================
/** The class instance. */
private String classInstance;

/** The key. */
private String key;

/** The id. */
private String id;

/**
 * Gets the class instance.
 * 
 * @return the class instance
 */
public String getClassInstance() {
  return classInstance;
}

/**
 * Sets the class instance.
 * 
 * @param classInstance the new class instance
 */
public void setClassInstance(String classInstance) {
  this.classInstance = classInstance;
}


/**
 * Gets the key.
 * 
 * @return the key
 */
public String getKey() {
  return key;
}

/**
 * Sets the key.
 * 
 * @param key the new key
 */
public void setKey(String key) {
  this.key = key;
}

/* (non-Javadoc)
 * @see com.esri.framework.search.SearchProfile#getId()
 */
@Override
public String getId() {
  return id;
}

/* (non-Javadoc)
 * @see com.esri.framework.search.SearchProfile#setId(java.lang.String)
 */
@Override
public void setId(String id) {
  this.id = id;
}


@Override
public void readGetMetadataByIDResponse(String response,
    SearchResultRecord record) throws TransformerException {
  // TODO Auto-generated method stub
  
}

}
