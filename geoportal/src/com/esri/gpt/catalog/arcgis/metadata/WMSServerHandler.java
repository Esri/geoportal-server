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
package com.esri.gpt.catalog.arcgis.metadata;

import com.esri.arcgisws.EnvelopeN;
import com.esri.arcgisws.ServiceDescription;
import com.esri.gpt.catalog.arcgis.metadata.ServiceInfo.LayerInfo;
import com.esri.gpt.control.georss.GeometryService;
import com.esri.gpt.framework.geometry.Envelope;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the collection of metadata for an ArcGIS WMS service (WMSServer).
 */
public class WMSServerHandler extends OGCServerHandler {
  private static final Logger LOGGER = Logger.getLogger(WMSServerHandler.class.getCanonicalName());
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public WMSServerHandler() {
    super("WMS");
  } 

  @Override
  public ServiceInfo createServiceInfo(ServiceInfo parentInfo, ServiceDescription desc, String currentRestUrl, String currentSoapUrl) {
    ServiceInfo serviceInfo = super.createServiceInfo(parentInfo, desc, currentRestUrl, currentSoapUrl);
    String thumbnailUrl = createThumbnailUrl(serviceInfo);
    serviceInfo.setThumbnailUrl(thumbnailUrl);
    String textInfo = createTextInfo(serviceInfo);
    serviceInfo.setText(textInfo);
    return serviceInfo;
  }
  
  /**
   * Creates text info.
   * @param parentInfo parent info
   * @param serviceInfo service info
   * @return text info
   */
  private String createTextInfo(ServiceInfo serviceInfo) {
    StringBuilder ti = new StringBuilder();
    
    ti.append("{");
    
    ti.append("\"title\":\"").append(serviceInfo.getName()).append("\",");
    ti.append("\"url\":\"").append(serviceInfo.getSoapUrl()).append("\",");
    ti.append("\"mapUrl\":\"").append(serviceInfo.getSoapUrl()).append("?\",");
    ti.append("\"version\":\"").append("1.3.0").append("\",");

    ti.append("\"layers\":[");
    
    StringBuilder lb = new StringBuilder();
    for (LayerInfo li : serviceInfo.getLayersInfo()) {
      if (lb.length()>0) {
        lb.append(",");
      }
      lb.append("{");
      lb.append("\"name\":\"").append(li.getName()).append("\",");
      lb.append("\"title\":\"").append(li.getTitle()).append("\"");
      lb.append("}");
    }
    ti.append(lb);
    
    ti.append("],");

    ti.append("\"copyright\":\"").append(serviceInfo.getCopyright()).append("\",");
    
    ti.append("\"spatialReferences\":[");
    ti.append("4326");
    if (serviceInfo.getEnvelope() instanceof EnvelopeN) {
      EnvelopeN e = (EnvelopeN) serviceInfo.getEnvelope();
      if (e.getSpatialReference()!=null && e.getSpatialReference().getWKID()!=null && e.getSpatialReference().getWKID()!=4326) {
        ti.append(",").append(e.getSpatialReference().getWKID().toString());
      }
    }
    ti.append("],");
    
    ti.append("\"format\":").append("null");
    
    ti.append("}");
    
    return ti.toString();
  }
  
  /**
   * Creates thumbnail URL.
   * @param serviceInfo service info
   * @return thumbnail URL
   */
  private String createThumbnailUrl(ServiceInfo serviceInfo) {
    if (serviceInfo.getEnvelope() instanceof EnvelopeN) {
      EnvelopeN e = (EnvelopeN) serviceInfo.getEnvelope();
      Envelope envelope = new Envelope(e.getXMin(), e.getYMin(), e.getXMax(), e.getYMax());
      envelope.setWkid(e.getSpatialReference()!=null && e.getSpatialReference().getWKID()!=null? e.getSpatialReference().getWKID().toString(): "4326");
      
      GeometryService gs = GeometryService.createDefaultInstance();
      try {
        List<Envelope> envelopes = gs.project(Arrays.asList(new Envelope[]{envelope}), "4326");
        if (!envelopes.isEmpty()) {
          envelope = envelopes.get(0);
          
          StringBuilder thumbnailURL = new StringBuilder();
          thumbnailURL.append(serviceInfo.getSoapUrl());
          thumbnailURL.append("?SERVICE=WMS&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=TRUE&STYLES=&VERSION=1.3.0");
          thumbnailURL.append("&layers=");
          
          StringBuilder liSB = new StringBuilder();
          for (LayerInfo li : serviceInfo.getLayersInfo()) {
            if (liSB.length()>0) {
              liSB.append(",");
            }
            liSB.append(li.getName());
          }
            thumbnailURL.append(liSB);
          
          thumbnailURL.append("&WIDTH=200&HEIGHT=133&CRS=EPSG:4326");

          StringBuilder bboxSB = new StringBuilder();
          bboxSB.append(envelope.getMinY()).append(",").append(envelope.getMinX()).append(",").append(envelope.getMaxY()).append(",").append(envelope.getMaxX());
          
          thumbnailURL.append("&BBOX=").append(bboxSB);
          
          return thumbnailURL.toString();
        }
      } catch (Exception ex) {
        LOGGER.log(Level.WARNING, "Unable to create thumbnail URL for WMS service.", ex);
      }
    }
    
    return "";
  }
  
}

