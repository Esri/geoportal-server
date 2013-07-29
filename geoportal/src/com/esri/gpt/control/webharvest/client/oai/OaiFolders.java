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
package com.esri.gpt.control.webharvest.client.oai;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.NodeListAdapter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * OAI folders.
 */
class OaiFolders implements Iterable<Resource> {

/** logger */
private static final Logger LOGGER = Logger.getLogger(OaiFolders.class.getCanonicalName());
/** Date format. */
private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
/** iteration context */
private IterationContext context;
/** service info */
private OaiInfo info;
/** service proxy */
private OaiProxy proxy;
/** query criteria */
private Criteria criteria;

/**
 * Creates new instance of folders.
 * @param context iteration context
 * @param info service info
 * @param proxy service proxy
 * @param criteria query criteria
 */
public OaiFolders(IterationContext context, OaiInfo info, OaiProxy proxy, Criteria criteria) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  if (info == null)
    throw new IllegalArgumentException("No info provided.");
  if (proxy == null)
    throw new IllegalArgumentException("No proxy provided.");
  this.context = context;
  this.info = info;
  this.proxy = proxy;
  this.criteria = criteria;
}

@Override
public Iterator<Resource> iterator() {
  return new OaiFolderIterator();
}

/**
 * OAI folders iterator.
 */
private class OaiFolderIterator extends ReadOnlyIterator<Resource> {

/** resumption token */
private String resumptionToken = null;
/** next records */
private Iterable<Resource> nextOaiRecords = null;
/** records counter */
private int recs;
/** no more records*/
private boolean noMore;

@Override
public boolean hasNext() {
  if (!noMore && nextOaiRecords == null) {
    if (resumptionToken == null || resumptionToken.length() > 0) {
      try {
        advanceToNextRecords();
      } catch (IOException ex) {
        noMore = true;
        context.onIterationException(ex);
      }
    } else {
      noMore = true;
    }
  }
  return !noMore;
}

@Override
public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException();
  }
  final Iterable<Resource> records = nextOaiRecords;
  nextOaiRecords = null;
  return new Resource() {
    @Override
    public Iterable<Resource> getNodes() {
      return records;
    }
  };
}

/**
 * Advances to the next set of records.
 * @throws IOException if advancing fails
 */
private void advanceToNextRecords() throws IOException {
  LOGGER.finer("Advancing to the next group of records.");
  try {
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(info.newListIdsUrl(resumptionToken, criteria.getFromDate(), criteria.getToDate()));

    XmlHandler sh = new XmlHandler(false);
    cr.setContentHandler(sh);
    cr.setCredentialProvider(info.newCredentialProvider());
    cr.execute();
    Document doc = sh.getDocument();

    XPath xPath = XPathFactory.newInstance().newXPath();
    NodeList nodeList = (NodeList) xPath.evaluate("/OAI-PMH/ListIdentifiers/header", doc, XPathConstants.NODESET);

    boolean maxReached = false;
    ArrayList<Resource> resources = new ArrayList<Resource>();
    for (Node nd : new NodeListAdapter(nodeList)) {
      recs++;
      maxReached = criteria!=null && criteria.getMaxRecords()!=null && recs>criteria.getMaxRecords();
      if (maxReached) break;
      final String id = (String) xPath.evaluate("identifier/text()", nd, XPathConstants.STRING);
      Publishable publishable = new CommonPublishable() {
        private StringUri uri = new StringUri(id);
        
        @Override
        public SourceUri getSourceUri() {
          return uri;
        }

        @Override
        public String getContent() throws IOException {
          return proxy.read(id);
        }
      };
      resources.add(publishable);
    }

    nextOaiRecords = resources;

    if (!maxReached) {
      resumptionToken = Val.chkStr((String) xPath.evaluate(
          "/OAI-PMH/ListIdentifiers/resumptionToken/text()",
          doc, XPathConstants.STRING));
    } else {
      resumptionToken = ""; // this is to stop advancing
    }

  } catch (XPathExpressionException ex) {
    throw new IOException("Error accessing metadata. Cause: " + ex.getMessage());
  }
}
}
}

