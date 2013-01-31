/*
 * Copyright 2011 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.client.thredds;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.waf.DestroyableResource;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.NodeListAdapter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * THREDDS folder
 */
class TFolder implements DestroyableResource {

  /** logger */
  protected static final Logger LOGGER = Logger.getLogger(TFolder.class.getCanonicalName());
  protected IterationContext context;
  protected TInfo info;
  protected TProxy proxy;
  protected Criteria criteria;
  private Set<String> processedFolders;

  /**
   * Creates instance of the WAF folder.
   * @param context iteration context
   * @param info WAF info
   * @param proxy WAF proxy
   * @param url folder URL
   * @param criteria search criteria
   */
  public TFolder(IterationContext context, TInfo info, TProxy proxy, Criteria criteria, Set<String> processedFolders) {
    this.context = context;
    this.info = info;
    this.proxy = proxy;
    this.criteria = criteria;
    this.processedFolders = processedFolders;
  }

  @Override
  public void destroy() {
  }

  @Override
  public Iterable<Resource> getNodes() {
    return new Iterable<Resource>() {

      @Override
      public Iterator<Resource> iterator() {
        return new TFolderIterator();
      }
    };
  }

  /**
   * Folder iterator.
   */
  private class TFolderIterator extends ReadOnlyIterator<Resource> {

    private Iterator<Resource> iterator;
    private boolean noMore;

    @Override
    public boolean hasNext() {
      if (!noMore && iterator == null) {
        loadFolderContent();
      }
      boolean hasMore = !noMore && iterator != null ? iterator.hasNext() : false;
      if (!hasMore) {
        noMore = true;
        iterator = null;
      }
      return hasMore;
    }

    @Override
    public Resource next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more resources.");
      }
      return iterator.next();
    }

    /**
     * Loads folder content.
     */
    private void loadFolderContent() {
      try {
        LOGGER.log(Level.FINER, "Loading folder content of {0}", info.getUrl());

        HttpClientRequest cr = new HttpClientRequest();
        cr.setUrl(info.getUrl());
        XmlHandler sh = new XmlHandler(false);
        cr.setContentHandler(sh);
        cr.setBatchHttpClient(info.getBatchHttpClient());
        cr.execute();

        iterator = parseResponse(sh.getDocument()).iterator();

        LOGGER.log(Level.FINER, "Loading folder content of {0} completed.", info.getUrl());
      } catch (Exception ex) {
        noMore = true;
        iterator = null;
        context.onIterationException(ex);
      }
    }
  }
  
  private String encodeUrl(String str) {
    try {
      return URLEncoder.encode(str, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return str;
    }
  }
  
  private List<Resource> parseResponse(Document doc) throws XPathException, IOException {
    ArrayList<Resource> resources = new ArrayList<Resource>();
    
    XPath xPath = XPathFactory.newInstance().newXPath();
    Node ndCatalog = (Node)xPath.evaluate("/catalog", doc, XPathConstants.NODE);
    
    URL infoUrl = new URL(info.getUrl());
    
    String iso = Val.chkStr((String) xPath.evaluate("//service[@serviceType='ISO']/@base", ndCatalog, XPathConstants.STRING));
    if (!iso.isEmpty()) {
      // creating TFiles only if iso exist
      URL baseUrl = new URL(infoUrl.toExternalForm());
      baseUrl = new URL(baseUrl, iso);
    
      NodeList ndDatasets = (NodeList)xPath.evaluate("//dataset[string-length(normalize-space(@urlPath))>0]", ndCatalog, XPathConstants.NODESET);
      for (Node ndDataset: new NodeListAdapter(ndDatasets)) {
        String url = (String)xPath.evaluate("@urlPath",ndDataset,XPathConstants.STRING);
        String ID = (String)xPath.evaluate("@ID",ndDataset,XPathConstants.STRING);
        if (!url.isEmpty()) {
          URL datasetUrl = new URL(baseUrl, url);
          TFile datasetFile = new TFile(proxy, datasetUrl.toExternalForm()+"?catalog="+encodeUrl(info.getUrl())+"&dataset="+ID);
          resources.add(datasetFile);
        }
      }
    }

    NodeList ndCatalogRefs = (NodeList)xPath.evaluate("//catalogRef/@href", ndCatalog, XPathConstants.NODESET);
    for (Node ndCatalogRef: new NodeListAdapter(ndCatalogRefs)) {
      String url = Val.chkStr(ndCatalogRef.getNodeValue());
      URL catalogUrl = new URL(infoUrl, url);
      String catalogUrlExternal = catalogUrl.toExternalForm();
      if (!processedFolders.contains(catalogUrlExternal)) {
        processedFolders.add(catalogUrlExternal);
        TInfo catalogInfo = new TInfo(catalogUrl.toExternalForm());
        TProxy catalogProxy = new TProxy(catalogInfo, criteria);
        TFolder catalogFolder = new TFolder(context, catalogInfo, catalogProxy, criteria, processedFolders);
        resources.add(catalogFolder);
      }
    }
    
    return resources;
  }
}
