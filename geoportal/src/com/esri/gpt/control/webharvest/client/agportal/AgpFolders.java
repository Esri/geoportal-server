/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.control.webharvest.client.agportal;

import com.esri.gpt.catalog.arcgis.agportal.client.SearchClient;
import com.esri.gpt.catalog.arcgis.agportal.client.SearchClient.SearchResult;
import com.esri.gpt.catalog.arcgis.agportal.itemInfo.ESRI_ItemInformation;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * ArcGIS Portal folders.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class AgpFolders implements Iterable<Resource> {

  /** logger */
  private static final Logger LOGGER = Logger.getLogger(AgpFolders.class.getCanonicalName());
  /** iteration context */
  private IterationContext context;
  /** service info */
  private AgpInfo info;
  /** query criteria */
  private Criteria criteria;

  /**
   * Creates new instance of folders.
   * @param context iteration context
   * @param info service info
   * @param criteria query criteria
   */
  public AgpFolders(IterationContext context, AgpInfo info, Criteria criteria) {
    if (context == null) {
      throw new IllegalArgumentException("No context provided.");
    }
    if (info == null) {
      throw new IllegalArgumentException("No info provided.");
    }
    this.context = context;
    this.info = info;
    this.criteria = criteria;
  }

  @Override
  public Iterator<Resource> iterator() {
    return new AgpFolderIterator();
  }

  /**
   * OAI folders iterator.
   */
  private class AgpFolderIterator extends ReadOnlyIterator<Resource> {

    SearchResult sr = null;
    /** resumption Id */
    private int resumptionId = 1;
    /** next records */
    private Iterable<Resource> nextAgpRecords = null;
    /** records counter */
    private int recs;
    /** no more records*/
    private boolean noMore;

    @Override
    public boolean hasNext() {
      if (!noMore && nextAgpRecords == null) {
        if (resumptionId > 0) {
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
      final Iterable<Resource> records = nextAgpRecords;
      nextAgpRecords = null;
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
     * @throws JSONException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    private void advanceToNextRecords() throws IOException {
      LOGGER.finer("Advancing to the next group of records.");

      final String startDcTemplate = "<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dct=\"http://purl.org/dc/terms/\" xmlns:dcmiBox=\"http://dublincore.org/documents/2000/07/11/dcmi-box/\">";
      final String endDcTemplate = "</rdf:Description></rdf:RDF>";

      if (sr==null) {
        SearchClient sc = new SearchClient(info.getUrl());
        sr = sc.listAll();
      } else {
        sr = sr.next();
      }
      
      if (sr!=null) {
        ArrayList<Resource> resources = new ArrayList<Resource>();
        for (final ESRI_ItemInformation ii : sr.getItems()) {
          final StringBuilder document = new StringBuilder();
          final Appender app = new Appender(document);
          recs++;
          boolean maxReached = criteria != null && criteria.getMaxRecords() != null && recs > criteria.getMaxRecords();
          if (maxReached) {
            noMore = true;
            break;
          }
          Date modifiedDate = ii.getModifiedDate();
          
          if (criteria!=null && criteria.getFromDate()!=null && modifiedDate!=null && criteria.getFromDate().before(modifiedDate)) {
            continue;
          }
          Publishable publishable = new CommonPublishable() {

            private String itemInfoUrl = info.getUrl().replaceAll("/$", "") + "/content/items/" + ii.getId() + "/info/iteminfo.xml";
            private StringUri uri = new StringUri(formatUuid(ii.getId()));

            @Override
            public SourceUri getSourceUri() {
              return uri;
            }

            @Override
            public String getContent() throws IOException {
              try {
                document.append(startDcTemplate);
                document.append("<rdf:Description rdf:about=\"").append(itemInfoUrl).append("\">");
                
                if (!app.append("dc:title", ii.getTitle())) {
                  app.append("dc:title", ii.getName());
                }
                if (ii.getModifiedDate()!=null) {
                  app.append("dc:date", new IsoDateFormat().format(ii.getModifiedDate()));
                }
                app.append("dc:identifier", ii.getId());
                if (!app.append("dc:description", ii.getDescription())) {
                  app.append("dc:description", ii.getSnippet());
                }
                app.append("dct:references", ii.getUrl());
                app.append("dct:references", itemInfoUrl);
                app.append("dc:creator", ii.getOwner());
                app.append("dct:accessRights", ii.getAccess());
                app.append("dc:language", ii.getCulture());
                app.append("dc:type", ii.getTypeKeywords());
                app.append("dc:type", ii.getTags());
                app.append("dc:subject", ii.getType());
                if (ii.getExtent()!=null && ii.getExtent().isValid()) {
                  Envelope e = ii.getExtent();
                  document.append("<ows:WGS84BoundingBox>");
                  document.append("<ows:LowerCorner>");
                  document.append(Double.toString(e.getMinX())).append(" ").append(Double.toString(e.getMinY()));
                  document.append("</ows:LowerCorner>");
                  document.append("<ows:UpperCorner>");
                  document.append(Double.toString(e.getMaxX())).append(" ").append(Double.toString(e.getMaxY()));
                  document.append("</ows:UpperCorner>");
                  document.append("</ows:WGS84BoundingBox>");
                }

                document.append(endDcTemplate);

                return document.toString();
              } catch (Exception e) {
                throw new IOException("Error occured while retrieving metadata, cause : " + e.getMessage());
              }
            }
          };
          resources.add(publishable);
        }
        nextAgpRecords = resources;
      } else {
        noMore = true;
      }
    }
  }
  
  private String formatUuid(String uuid) {
    uuid = Val.chkStr(uuid);
    String value = uuid.replaceAll("[^0-9a-fA-F]", "");
    if (value.length()==32) {
      StringBuilder sb = new StringBuilder();
      sb.append(value.substring(0, 8));
      sb.append("-");
      sb.append(value.substring(8, 12));
      sb.append("-");
      sb.append(value.substring(12, 16));
      sb.append("-");
      sb.append(value.substring(16, 20));
      sb.append("-");
      sb.append(value.substring(20));
      value = "{"+sb.toString()+"}";
    } else {
      value = uuid;
    }
    return value;
  }
  
  private class Appender {
    private StringBuilder sb;
    
    public Appender(final StringBuilder sb) {
      this.sb = sb;
    }
    
    public boolean append(String tagName, String value) {
      tagName = Val.chkStr(tagName);
      if (tagName.length()==0) return false;
      value = Val.chkStr(value);
      if (value.length()==0 || value.equalsIgnoreCase("null")) return false;
      
      sb.append("<").append(tagName).append(">");
      sb.append(Val.escapeXml(value));
      sb.append("</").append(tagName).append(">");
      
      return true;
    }
    
    public void append(String tagName, List<String> values) {
      for (String v : values) {
        append(tagName, v);
      }
    }
  }
}
