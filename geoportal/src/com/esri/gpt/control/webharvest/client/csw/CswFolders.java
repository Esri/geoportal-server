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
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.server.csw.client.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * CSW folders.
 */
class CswFolders implements Iterable<Resource> {
/** logger */
private static final Logger LOGGER = Logger.getLogger(CswFolders.class.getCanonicalName());
/** iteration context */
private IterationContext context;
/** service proxy */
private CswProxy proxy;
/** query criteria */
private Criteria criteria;

/**
 * Creates instance of the folders.
 * @param context iteration context
 * @param proxy service proxy
 * @param criteria query criteria
 */
public CswFolders(IterationContext context, CswProxy proxy, Criteria criteria) {
  if (context==null) throw new IllegalArgumentException("No context provided.");
  if (proxy==null) throw new IllegalArgumentException("No proxy provided.");
  this.context = context;
  this.proxy = proxy;
  this.criteria = criteria;
}

@Override
public Iterator<Resource> iterator() {
  return new CswFolderIterator();
}

/**
 * Folders iterator.
 */
private class CswFolderIterator extends ReadOnlyIterator<Resource> {
/** index of the next start record */
private int nextStartRecord = 1;
/** next collection of records */
private ArrayList<Resource> nextCswrecords = null;
/** no more records*/
private boolean noMore;

@Override
public boolean hasNext() {
  if (!noMore && nextCswrecords == null) {
    if (criteria!=null && criteria.getMaxRecords()!=null && nextStartRecord>criteria.getMaxRecords()) {
      noMore = true;
    } else {
      try {
        advanceToNextRecords();
      } catch (IOException ex) {
        noMore = true;
        context.onIterationException(ex);
      }
    }
  }
  return !noMore;
}

@Override
public Resource next() {
  if (!hasNext()) {
    throw new NoSuchElementException();
  }
  final Iterable<Resource> records = nextCswrecords;
  nextCswrecords = null;
  return new Resource() {
    @Override
    public Iterable<Resource> getNodes() {
      return records;
    }
  };
}

/**
 * Advances to the next portion of records.
 * @throws IOException if advancing fails
 */
private void advanceToNextRecords() throws IOException {
  LOGGER.finer("Advancing to the next group of records.");
  try {
    nextCswrecords = null;

    // submit CSW request
    IsoDateFormat IDF = new IsoDateFormat();
    CswSearchCriteria cswCriteria = new CswSearchCriteria();
    cswCriteria.setMaxRecords(criteria!=null && criteria.getMaxRecords()!=null? criteria.getMaxRecords(): 20);
    cswCriteria.setStartPosition(nextStartRecord);
    cswCriteria.setSearchText(criteria!=null && criteria.getSearchText()!=null? criteria.getSearchText(): "");
    cswCriteria.setEnvelope(getEnvelope());
    CswSearchResponse cswResponse = new CswSearchResponse();
    CswSearchRequest request = new CswSearchRequest();
    request.setCatalog(proxy.getCatalog());
    request.setCriteria(cswCriteria);
    request.setCswSearchResponse(cswResponse);
    request.search();

    // get response
    CswRecords cswRecords = cswResponse.getRecords();
    nextStartRecord += cswRecords.getSize();

    ArrayList<Resource> resources = new ArrayList<Resource>();
    if (cswRecords.getSize()>0) {
      // process response
      for (Iterator i = cswRecords.iterator(); i.hasNext();) {
        com.esri.gpt.server.csw.client.CswRecord record = (com.esri.gpt.server.csw.client.CswRecord) i.next();
        resources.add(new CswPublishableAdapter(proxy,record));
      }
    } else {
      noMore = true;
    }

    // save processed response
    nextCswrecords = resources;

  } catch (NullReferenceException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (XPathExpressionException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (ParserConfigurationException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (SAXException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (TransformerException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  }
}
  }

/**
 * Gets envelope from the criteria.
 * @return envelope or <code>null</code> if no envelope in criteria
 */
private Envelope getEnvelope() {
  if (criteria!=null && criteria.getBBox()!=null) {
    Envelope env = new Envelope();
    env.setMinX(criteria.getBBox().getMinX());
    env.setMinY(criteria.getBBox().getMinY());
    env.setMaxX(criteria.getBBox().getMaxX());
    env.setMaxY(criteria.getBBox().getMaxY());
    return env;
  }
  return null;
}

}
