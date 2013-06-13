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
package com.esri.gpt.control.webharvest.client.atom;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.schema.NamespaceContextImpl;
import com.esri.gpt.catalog.schema.Namespaces;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.http.HttpClientException;
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
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * Atom folders.
 */
class AtomFolders implements Iterable<Resource> {

/** logger */
private static final Logger LOGGER = Logger.getLogger(AtomFolders.class.getCanonicalName());
/** Date format. */
private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
/** iteration context */
private IterationContext context;
/** service info */
private BaseAtomInfo info;
/** service proxy */
private AtomProxy proxy;
/** query criteria */
private Criteria criteria;


/**
 * Creates new instance of folders.
 * @param context iteration context
 * @param info service info
 * @param proxy service proxy
 * @param criteria query criteria
 */
public AtomFolders(IterationContext context, BaseAtomInfo info, AtomProxy proxy, Criteria criteria) {
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

public Iterator<Resource> iterator() {
  return new AtomFolderIterator();
}

/**
 * Atom folders iterator.
 */
private class AtomFolderIterator extends ReadOnlyIterator<Resource> {
/** totalResults */
private int totalResults= -1;
/** startIndex */
private int startIndex=1;
/** itemsPerPage */
private int itemsPerPage=10;
/** next records */
private Iterable<Resource> nextAtomRecords = null;
/** records counter */
private int recs;
/** no more records*/
private boolean noMore;
/** no more records*/
private boolean firstTime = true;

public boolean hasNext() {
	totalResults = info.getTotalResults();
  if (!noMore && nextAtomRecords == null) {
    if ((totalResults >= startIndex) || (firstTime && totalResults == -1)) {
      try {    	  
        advanceToNextRecords();
        if(totalResults == -1){
        	firstTime = false;
        }else{
        	startIndex += itemsPerPage;
        }
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

public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException();
  }
  final Iterable<Resource> records = nextAtomRecords;
  nextAtomRecords = null;
  return new Resource() {
    public Iterable<Resource> getNodes() {
      return records;
    }
  };
}

/**
 * Makes ATOM name spaces.
 * @return the ATOM name spaces
 */
private Namespaces makeNamespaces() {
  Namespaces namespaces = new Namespaces();
  namespaces.add("atom","http://www.w3.org/2005/Atom");
  return namespaces;
}
/**
 * Makes a context for Atom name spaces.
 * @return the name space context
 */
private NamespaceContext makeNamespaceContext() {
  return new NamespaceContextImpl(makeNamespaces());
}

/**
 * Advances to the next set of records.
 * @throws IOException if advancing fails
 */
private void advanceToNextRecords() throws IOException {
  LOGGER.finer("Advancing to the next group of records.");
  try {
    HttpClientRequest cr = new HttpClientRequest();	
    cr.setUrl(info.newUrl(startIndex, itemsPerPage));
    XmlHandler sh = new XmlHandler(true);
    cr.setContentHandler(sh);
    cr.setCredentialProvider(info.newCredentialProvider());
    try{
    	cr.execute();
    }catch (HttpClientException hcex){
    	if(hcex.getHttpStatusCode() == 404){    		
    		cr.setUrl(info.newUrl(-1, -1));
    	    sh = new XmlHandler(true);
    	    cr.setContentHandler(sh);
    	    cr.setCredentialProvider(info.newCredentialProvider());
    	    cr.execute();
    	    noMore = true;
    	}
    }
    Document doc = sh.getDocument();
    XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(makeNamespaceContext());
    NodeList entries = (NodeList) xPath.evaluate("/atom:feed/atom:entry", doc, XPathConstants.NODESET);

    String entryProcessorClassName = info.getEntryProcessorClassName();
  	if (entryProcessorClassName.length() == 0) {
	    String[] parts = info.getUrl().split("entryProcessorClassName=");			
			if (parts != null && parts.length >= 2) {
				entryProcessorClassName = Val.chkStr(parts[1]);
				int idx = entryProcessorClassName.indexOf("&");
				if (idx == -1) {
					entryProcessorClassName = entryProcessorClassName.substring(0);
				} else {
					entryProcessorClassName = entryProcessorClassName.substring(0, idx);
				}
			}
  	}
		if (entryProcessorClassName.length() == 0) {
			entryProcessorClassName = "com.esri.gpt.control.webharvest.client.atom.SimpleEntryProcessor";
		}
		final String finalEntryProcessorClassName = entryProcessorClassName;		
    boolean maxReached = false;
    ArrayList<Resource> resources = new ArrayList<Resource>();
    for (final Node entry : new NodeListAdapter(entries)) {    	
      recs++;
      maxReached = criteria!=null && criteria.getMaxRecords()!=null && recs>criteria.getMaxRecords();
      if (maxReached) break;
      
      final String id = (String) xPath.evaluate("atom:id/text()", entry, XPathConstants.STRING);
      
      Publishable publishable = new CommonPublishable() {
        private StringUri uri = new StringUri(id);
        
        public SourceUri getSourceUri() {
          return uri;
        }

        public String getContent() throws IOException, SAXException {
        	Class<?> clsAdapter = null;
      		try {
						clsAdapter = Class.forName(finalEntryProcessorClassName);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
      		Object entryProcessorObj = null;
      		try {
      			entryProcessorObj = clsAdapter.newInstance();
      		} catch (InstantiationException e) {
      			// TODO Auto-generated catch block
      			e.printStackTrace();
      		} catch (IllegalAccessException e) {
      			// TODO Auto-generated catch block
      			e.printStackTrace();
      		}
      		if (entryProcessorObj instanceof IEntryProcessor) {
      			IEntryProcessor entryProcessor = ((IEntryProcessor) entryProcessorObj);			      	
        		return entryProcessor.extractMetadata(info,entry);
        	}
        	return ""; 
        }
      };
      resources.add(publishable);
    }

    nextAtomRecords = resources;

    if (maxReached) {	 
    	noMore = true;
    }

  } catch (XPathExpressionException ex) {
    throw new IOException("Error accessing metadata. Cause: " + ex.getMessage());
  }
}
}
}

