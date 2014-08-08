/*
 * Copyright 2014 Esri, Inc..
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

package com.esri.gpt.catalog.publication;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.server.csw.client.NullReferenceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Processor capable of drilling down WMS or WMTS service and create matedata for each operational layer.
 */
public class WMSProcessor extends ResourceProcessor {
  private final String resourceUrl;
  
  public WMSProcessor(ProcessingContext context, String resourceUrl) {
    super(context);
    this.resourceUrl = Val.chkStr(resourceUrl);
  }
  
  /**
   * Invokes processing against the resource.
   * @throws Exception if an exception occurs
   */
  @Override
  public void process() throws Exception {
    for (Publishable p: extractPublishables(new NativeImpl())) {
      this.publishMetadata(p.getSourceUri().asString(), p.getContent());
    }
  }

  @Override
  public Query createQuery(IterationContext context, Criteria criteria) {
    Query query = new Query() {
      @Override
      public Result execute() {
        return new CommonResult(extractPublishables(new NativeImpl()));
      }
    };
    return query;
  }

  @Override
  public Native getNativeResource(IterationContext context) {
    return new NativeImpl();
  }
  
  public String getContent(LayerAdaptor layer) throws XPathExpressionException {
    return "<?xml version='1.0' encoding='UTF-8'?>"
      + "<rdf:RDF xmlns:dct='http://purl.org/dc/terms/' xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:dc='http://purl.org/dc/elements/1.1/' xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' xmlns:exslt='http://exslt.org/common' xmlns:rim='urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0' xmlns:dcmiBox='http://dublincore.org/documents/2000/07/11/dcmi-box/' xmlns:ows='http://www.opengis.net/ows'>"
      + "<rdf:Description rdf:about='rdf_about'>"
      + "<dc:identifier>" + Val.escapeXml(layer.getName()) + "</dc:identifier>"
      + "<dc:title>" + Val.escapeXml(layer.getTitle()) + "</dc:title>"
      + "<dct:abstract>" + Val.escapeXml(layer.getAbstract()) + "</dct:abstract>"
      + (layer.getSpatial().length!=4? "": 
         "<ows:WGS84BoundingBox>" + 
         "<ows:LowerCorner>" +layer.getSpatial()[0]+" "+layer.getSpatial()[1]+ "</ows:LowerCorner>" +
         "<ows:UpperCorner>" +layer.getSpatial()[2]+" "+layer.getSpatial()[3]+ "</ows:UpperCorner>" +
         "</ows:WGS84BoundingBox>"
      )
      + "<dct:references>" + Val.escapeXml(resourceUrl) + "</dct:references>"
      + "</rdf:Description>"
      + "</rdf:RDF>";

  }
  
  private class LayerAdaptor {
    private final XPath newXPath;
    private final Node ndLayer;
    private final Double [] spatial;

    public LayerAdaptor(XPath newXPath, Node ndLayer) {
      this.newXPath = newXPath;
      this.ndLayer = ndLayer;
      
      ArrayList<Double> sp = new ArrayList<Double>();
      try {
        Node LatLonBoundingBox = (Node)newXPath.evaluate("LatLonBoundingBox", ndLayer, XPathConstants.NODE);
        if (LatLonBoundingBox!=null) {
          Double minx = (Double)newXPath.evaluate("@minx", LatLonBoundingBox, XPathConstants.NUMBER);
          if (minx!=null) sp.add(minx);
          Double miny = (Double)newXPath.evaluate("@miny", LatLonBoundingBox, XPathConstants.NUMBER);
          if (miny!=null) sp.add(miny);
          Double maxx = (Double)newXPath.evaluate("@maxx", LatLonBoundingBox, XPathConstants.NUMBER);
          if (maxx!=null) sp.add(maxx);
          Double maxy = (Double)newXPath.evaluate("@maxy", LatLonBoundingBox, XPathConstants.NUMBER);
          if (maxy!=null) sp.add(maxy);
        } else {
          Node BoundingBox = (Node)newXPath.evaluate("BoundingBox[@CRS='CRS:84']", ndLayer, XPathConstants.NODE);
          if (BoundingBox!=null) {
            Double minx = (Double)newXPath.evaluate("@minx", BoundingBox, XPathConstants.NUMBER);
            if (minx!=null) sp.add(minx);
            Double miny = (Double)newXPath.evaluate("@miny", BoundingBox, XPathConstants.NUMBER);
            if (miny!=null) sp.add(miny);
            Double maxx = (Double)newXPath.evaluate("@maxx", BoundingBox, XPathConstants.NUMBER);
            if (maxx!=null) sp.add(maxx);
            Double maxy = (Double)newXPath.evaluate("@maxy", BoundingBox, XPathConstants.NUMBER);
            if (maxy!=null) sp.add(maxy);
          } else {
            BoundingBox = (Node)newXPath.evaluate("BoundingBox[@CRS='EPSG:4326']", ndLayer, XPathConstants.NODE);
            if (BoundingBox!=null) {
              Double minx = (Double)newXPath.evaluate("@miny", BoundingBox, XPathConstants.NUMBER);
              if (minx!=null) sp.add(minx);
              Double miny = (Double)newXPath.evaluate("@minx", BoundingBox, XPathConstants.NUMBER);
              if (miny!=null) sp.add(miny);
              Double maxx = (Double)newXPath.evaluate("@maxy", BoundingBox, XPathConstants.NUMBER);
              if (maxx!=null) sp.add(maxx);
              Double maxy = (Double)newXPath.evaluate("@maxx", BoundingBox, XPathConstants.NUMBER);
              if (maxy!=null) sp.add(maxy);
            } else {
              Node EX_GeographicBoundingBox = (Node)newXPath.evaluate("EX_GeographicBoundingBox", ndLayer, XPathConstants.NODE);
              if (EX_GeographicBoundingBox!=null) {
                Double minx = (Double)newXPath.evaluate("westBoundLongitude", EX_GeographicBoundingBox, XPathConstants.NUMBER);
                if (minx!=null) sp.add(minx);
                Double miny = (Double)newXPath.evaluate("southBoundLatitude", EX_GeographicBoundingBox, XPathConstants.NUMBER);
                if (miny!=null) sp.add(miny);
                Double maxx = (Double)newXPath.evaluate("eastBoundLongitude", EX_GeographicBoundingBox, XPathConstants.NUMBER);
                if (maxx!=null) sp.add(maxx);
                Double maxy = (Double)newXPath.evaluate("northBoundLatitude", EX_GeographicBoundingBox, XPathConstants.NUMBER);
                if (maxy!=null) sp.add(maxy);
              }
            }
          }
        }
      } catch (XPathExpressionException ex) {}
      this.spatial = sp.toArray(new Double[sp.size()]);
    }
    
    public String getName() {
      try {
        return Val.chkStr((String) newXPath.evaluate("Name", ndLayer, XPathConstants.STRING));
      } catch (XPathExpressionException ex) {
        return "";
      }
    }
    
    public String getTitle() {
      try {
        return Val.chkStr((String) newXPath.evaluate("Title", ndLayer, XPathConstants.STRING));
      } catch (XPathExpressionException ex) {
        return "";
      }
    }

    
    public String getAbstract() {
      try {
        return Val.chkStr((String) newXPath.evaluate("Abstract", ndLayer, XPathConstants.STRING));
      } catch (XPathExpressionException ex) {
        return "";
      }
    }
    
    public Double[] getSpatial() {
      return spatial;
    }
    
  }
  
  private Publishable makeLayerPublishable(XPath newXPath, Node ndLayer) throws XPathExpressionException {
    Publishable publishable = null;
    
    LayerAdaptor layer = new LayerAdaptor(newXPath, ndLayer);
    
    final String name = layer.getName();
    if (!name.isEmpty()) {
      final String content = getContent(layer);
      publishable = new CommonPublishable() {

        @Override
        public SourceUri getSourceUri() {
          return new StringUri(resourceUrl+"&layer="+name);
        }

        @Override
        public String getContent() throws IOException, TransformerException, SAXException, NullReferenceException {
          return content;
        }
      };
    }
    
    return publishable;
  }
  
  private List<Publishable> processLayerNode(XPath newXPath, Node ndLayer) throws XPathExpressionException {
    List<Publishable> publishables = new ArrayList<Publishable>();
    
    Publishable publishable = makeLayerPublishable(newXPath, ndLayer);
    if (publishable!=null) {
      publishables.add(publishable);
    }
    
    NodeList ndLayers = (NodeList) newXPath.evaluate("Layer", ndLayer, XPathConstants.NODESET);
    for (Node ndSubLayer: new NodeListAdapter(ndLayers)) {
      publishables.addAll(processLayerNode(newXPath, ndSubLayer));
    }
    
    return publishables;
  }
  
  private List<Publishable> extractPublishables(NativeImpl nat) {
    List<Publishable> publishables = new ArrayList<Publishable>();
    
    publishables.add(nat);
    
    try {
      Document doc = nat.getDocument();
      
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath newXPath = xPathFactory.newXPath();
      
      NodeList ndLayers = (NodeList) newXPath.evaluate("//WMT_MS_Capabilities/Capability/Layer", doc, XPathConstants.NODESET);
      for (Node ndLayer: new NodeListAdapter(ndLayers)) {
        publishables.addAll(processLayerNode(newXPath, ndLayer));
      }
      
      ndLayers = (NodeList) newXPath.evaluate("//WMS_Capabilities/Capability/Layer", doc, XPathConstants.NODESET);
      for (Node ndLayer: new NodeListAdapter(ndLayers)) {
        publishables.addAll(processLayerNode(newXPath, ndLayer));
      }
      
    } catch (XPathExpressionException ex) {
      Logger.getLogger(WMSProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(WMSProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    return publishables;
  }

  private String readXml() throws IOException {
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(resourceUrl);
    StringHandler sh = new StringHandler();
    cr.setContentHandler(sh);
    cr.execute();
    String xml = sh.getContent();
    return xml;
  }

  private Document readDoc() throws IOException {
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(resourceUrl);
    XmlHandler sh = new XmlHandler(false);
    cr.setContentHandler(sh);
    cr.execute();
    Document doc = sh.getDocument();
    return doc;
  }

  private class NativeImpl extends CommonPublishable implements Native {
    private final UrlUri uri = new UrlUri(resourceUrl);

    @Override
    public SourceUri getSourceUri() {
      return uri;
    }

    @Override
    public String getContent() throws IOException {
       return readXml();
    }
    
    public Document getDocument() throws IOException {
      return readDoc();
    }

  };
}
