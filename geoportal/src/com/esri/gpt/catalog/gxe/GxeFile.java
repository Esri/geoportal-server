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
package com.esri.gpt.catalog.gxe;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A component XML file associated with a Geoportal XML editor definition.
 */
public class GxeFile {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GxeFile.class.getName());
  
  /** instance variables ====================================================== */
  private List<URI> baseLocations = new ArrayList<URI>();
  private boolean   isRoot = false;
  private String    location;
  private GxeFile   parent;
  private URL       referencedUrl;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GxeFile() {}
  
  /**
   * Constructs with a parent file and reference location.
   * @param parent the parent file from which this external reference was made
   * @param location the file location
   */
  public GxeFile(GxeFile parent, String location) {
    this.setParent(parent);
    this.setLocation(location);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the base locations.
   * @return the base locations
   */
  public List<URI> getBaseLocations() {
    return this.baseLocations;
  }
  
  /**
   * Sets the base locations.
   * @param locations the base locations
   */
  public void setBaseLocations(List<URI> locations) {
    this.baseLocations = locations;
  }
  
  /**
   * Indicates whether or not the file is the definition root.
   * @return true if this is the root
   */
  public boolean getIsRoot() {
    return this.isRoot;
  }
  
  /**
   * Indicates whether or not the file is the definition root.
   * @param isRoot true if this is the root
   */
  public void setIsRoot(boolean isRoot) {
    this.isRoot = isRoot;
  }
  
  /**
   * Gets the location.
   * @return the location
   */
  public String getLocation() {
    return this.location;
  }
  
  /**
   * Sets the location.
   * @param location the location
   */
  public void setLocation(String location) {
    this.location = location;
  }
  
  /**
   * Gets the parent.
   * @return the parent
   */
  public GxeFile getParent() {
    return this.parent;
  }
  /**
   * Sets the parent.
   * @param parent the parent
   */
  public void setParent(GxeFile parent) {
    this.parent = parent;
  }
  
  /**
   * Gets the URL that was referenced while loading the DOM.
   * @return the referenced URL
   */
  public URL getReferencedUrl() {
    return this.referencedUrl;
  }
  
  /** methods ================================================================= */
  
  /**
   * Finds the root node within a document.
   * @param dom the document
   * @return the root node
   */
  public Node findRoot(Document dom) {
    NodeList nl = dom.getChildNodes(); 
    for (int i=0; i<nl.getLength(); i++) {
      Node nd = nl.item(i);
      if (nd.getNodeType() == Node.ELEMENT_NODE) { 
        return nd; 
      }
    }
    return null;
  }
  
  /**
   * Establishes the base location inheritance precedence.
   * @param dom the active XML document.
   * @throws Exception if an exception occurs
   */
  private void inheritBaseLocations(Document dom) throws Exception {
    String ns = GxeContext.URI_GXE;
    Node root = this.findRoot(dom);
    Node nd = root.getAttributes().getNamedItemNS(ns,"extends");
    if (nd != null) {
      String loc = Val.chkStr(nd.getNodeValue());
      if (loc.length() > 0) {
        URL locUrl = Thread.currentThread().getContextClassLoader().getResource(loc);
        if (locUrl != null) {
          InputSource src = new InputSource(locUrl.toExternalForm());
          Document domExtends = DomUtil.makeDomFromSource(src,true);
          String baseLoc = locUrl.toExternalForm();
          int nIdx = baseLoc.lastIndexOf("/");
          if (nIdx != -1) baseLoc = baseLoc.substring(0,nIdx);
          this.getBaseLocations().add(new URI(baseLoc+"/"));
          this.inheritBaseLocations(domExtends);
        }
      }
    }
  }
  
  /**
   * Loads the XML document object model (org.w3c.dom.Document).
   * @throws Exception if an exception occurs
   */
  public Document loadDom()  {
    Document dom = null;
    try { 
      URL url = this.makeUrl();
      InputSource src = new InputSource(url.toExternalForm());
      dom = DomUtil.makeDomFromSource(src,true);
      if (this.getIsRoot()) {
        this.setBaseLocations(new ArrayList<URI>());
        String baseLoc = url.toExternalForm();
        int nIdx = baseLoc.lastIndexOf("/");
        if (nIdx != -1) baseLoc = baseLoc.substring(0,nIdx);
        this.getBaseLocations().add(new URI(baseLoc+"/"));
        this.inheritBaseLocations(dom);
      }
    } catch (Exception e) {
      String msg = "Error loading XML file: "+Val.chkStr(this.getLocation());
      msg += ", "+Val.chkStr(e.toString());
      LOGGER.log(Level.CONFIG,msg,e);
      throw new ConfigurationException(msg,e);
    }
    return dom;
  }
  
  /**
   * Returns the URL for the file.
   * @return the associated url
   * @throws IOException if the URL cannot be generated
   */
  public URL makeUrl() throws Exception {
    
    // return the reference if it was already established
    if (this.referencedUrl != null) return this.referencedUrl;
    
    String loc = Val.chkStr(this.getLocation());
    URL locUrl = null;
    
    // determine the base location URIs
    URI rootUri = null;
    List<URI> baseUris = null;
    GxeFile parentFile = this;
    while (parentFile != null) {
      if (parentFile.isRoot) {
        baseUris = parentFile.getBaseLocations();
        break;
      }
      parentFile = parentFile.getParent();
    }
    boolean hasBaseUris = (baseUris != null) && (baseUris.size() > 0);
    if (hasBaseUris) rootUri = baseUris.get(0);
    
    if (locUrl == null) {
      locUrl = Thread.currentThread().getContextClassLoader().getResource(loc);
    }
    
    if ((locUrl == null) && !loc.startsWith("$base")) {
      if ((this.getParent() != null) && (this.getParent().getReferencedUrl() != null)) {
        try {
          URL parentUrl = this.getParent().getReferencedUrl();
          URI parentUri = parentUrl.toURI();
          URI thisUri = parentUri.resolve(loc);
          File thisFile = new File(thisUri);
          locUrl = thisUri.toURL();
          if (!thisFile.exists() && hasBaseUris && (rootUri != null)) {
            URI relUri = rootUri.relativize(thisUri);
            int n = baseUris.size();
            for (int i=0; i<n; i++) {
              URI testUri = baseUris.get(i).resolve(relUri);
              // this won't work for http based URIs
              if ((new File(testUri)).exists()) {
                locUrl = testUri.toURL();
                break;
              }
            }
          }
        } catch (URISyntaxException e) {
          LOGGER.log(Level.CONFIG,"While attemting to load: "+loc+", error: "+e.toString(),e);
        }
      }
    }
    
    if ((locUrl == null) && loc.startsWith("$base")) {
      String relLoc = Val.chkStr(loc.substring(5));
      if (relLoc.startsWith("/")) relLoc = Val.chkStr(relLoc.substring(1));
      if ((relLoc.length() > 0) && hasBaseUris) {
        try {
          URI relUri = new URI(relLoc);
          int n = baseUris.size();
          for (int i=0; i<n; i++) {
            URI testUri = baseUris.get(i).resolve(relUri);
            // this won't work for http based URIs
            if ((new File(testUri)).exists()) {
              locUrl = testUri.toURL();
              break;
            }
          }
        } catch (URISyntaxException e) {
          LOGGER.log(Level.CONFIG,"While attemting to load: "+loc+", error: "+e.toString(),e);
        }        
      }
    }
    
    // throw an exception if the URL was not generated, otherwise save and return the reference
    if (locUrl == null) {
      throw new IOException("Unable to create resource URL for path: "+loc);
    } else {
      this.referencedUrl = locUrl;
    }
    return this.referencedUrl;
  }
  
}
