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
package com.esri.gpt.control.livedata;

import com.esri.gpt.control.livedata.selector.HttpRequestDefinition;
import com.esri.gpt.control.livedata.selector.IHttpResponseListener;
import com.esri.gpt.control.livedata.selector.IRegistry;
import com.esri.gpt.control.livedata.selector.ISetter;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.ResponseInfo;
import com.esri.gpt.framework.util.Val;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * WMS renderer factory.
 */
/*packge*/ class WMSRendererFactory extends MapBasedRendererFactory {

  /** get capabilities request */
  private static String GET_CAPABILITIES_REQUEST = "service=WMS&request=GetCapabilities";
  /** context path */
  private String contextPath = "";
  /** proxy URL */
  private String proxyUrl = "";

  @Override
  public boolean isDefinitive() {
    return true;
  }

  @Override
  public void register(IRegistry reg, final ISetter setter, final String url) {
    int qmark = url.lastIndexOf("?");
    final String serviceUrl = qmark >= 0 ? url.substring(0, qmark) : url;
    final String queryString = qmark >= 0 ? url.substring(qmark + 1) : "";

    Query query = new Query(queryString);
    if (query.containsKey("service") && !query.get("service").equalsIgnoreCase("WMS"))
      return;
    query = new Query(GET_CAPABILITIES_REQUEST).mixin(query);

    final String getCapabilitiesUrl = serviceUrl + "?" + query;

    reg.register(new HttpRequestDefinition(getCapabilitiesUrl), new IHttpResponseListener() {

      public void onResponse(ResponseInfo info, String strContent, Document docContent) {
        if (docContent != null) {
          try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            Node ndWmsCapabilities = (Node) xPath.evaluate("/WMS_Capabilities", docContent, XPathConstants.NODE);
            if (ndWmsCapabilities == null) {
              ndWmsCapabilities = (Node) xPath.evaluate("/WMT_MS_Capabilities", docContent, XPathConstants.NODE);
            }

            if (ndWmsCapabilities != null) {
              final Envelope extent = readExtent(xPath, ndWmsCapabilities);
              setter.set(new WMSRenderer() {

                @Override
                protected Envelope getExtent() {
                  return extent;
                }

                @Override
                protected String getUrl() {
                  return getCapabilitiesUrl;
                }

                @Override
                protected String getProxyUrl() {
                  return contextPath + proxyUrl;
                }

                @Override
                protected int getMapHeightAdjustment() {
                  return getProperties().getMapHeightAdjustment();
                }
              });
            }

          } catch (Exception ex) {
          }
        }
      }
    });
  }

  /**
   * Creates instance of the factory.
   * @param properties properties
   * @param contextPath context path
   * @param proxyUrl proxy URL
   */
  public WMSRendererFactory(ILiveDataProperties properties, String contextPath, String proxyUrl) {
    super(properties);
    this.contextPath = Val.chkStr(contextPath);
    this.proxyUrl = Val.chkStr(proxyUrl);
  }

  /**
   * Reads extent from the capabilities node.
   * @param xPath xpath
   * @param ndWmsCapabilities capabilities node
   * @return envelope or <code>null</code> if envelope can not be created
   * @throws javax.xml.xpath.XPathExpressionException if using XPath fails
   */
  private Envelope readExtent(XPath xPath, Node ndWmsCapabilities) throws XPathExpressionException {
    String wkid = "4326";
    Node EX_GeographicBoundingBox = (Node) xPath.evaluate("Capability/Layer/EX_GeographicBoundingBox", ndWmsCapabilities, XPathConstants.NODE);
    if (EX_GeographicBoundingBox != null) {
      return extractExtent(xPath, EX_GeographicBoundingBox, new String[]{"westBoundLongitude", "southBoundLatitude", "eastBoundLongitude", "northBoundLatitude"}, wkid);
    } else {
      NodeList nodes = (NodeList) xPath.evaluate("//EX_GeographicBoundingBox", ndWmsCapabilities, XPathConstants.NODESET);
      if (nodes.getLength() > 0) {
        Envelope envelope = new Envelope();
        envelope.setWkid(wkid);
        for (int i = 0; i < nodes.getLength(); i++) {
          Node node = nodes.item(i);
          Envelope e = extractExtent(xPath, node, new String[]{"westBoundLongitude", "southBoundLatitude", "eastBoundLongitude", "northBoundLatitude"}, wkid);
          if (e != null) {
            envelope.merge(e);
          }
        }
        if (envelope.hasSize())
          return envelope;
      }
    }

    Node LatLonBoundingBox = (Node) xPath.evaluate("Capability/Layer/LatLonBoundingBox", ndWmsCapabilities, XPathConstants.NODE);
    if (LatLonBoundingBox != null) {
      return extractExtent(xPath, LatLonBoundingBox, new String[]{"@minx", "@miny", "@maxx", "@maxy"}, wkid);
    }

    return null;
  }

  private Envelope extractExtent(XPath xPath, Node node, String[] names, String wkid) throws XPathExpressionException {
    if (node != null && names != null && names.length == 4) {
      String[] values = new String[4];
      for (int i = 0; i < 4; i++) {
        values[i] = Val.chkStr((String) xPath.evaluate(names[i], node, XPathConstants.STRING));
      }
      return makeExtent(values[0], values[1], values[2], values[3], wkid);
    }
    return null;
  }

  /**
   * Creates envelope from string reprezentations of coordinates.
   * @param sMinX minx
   * @param sMinY miny
   * @param sMaxX maxx
   * @param sMaxY maxy
   * @param wkid wkid
   * @return envelope or <code>null</code> if envelope can not be created
   */
  private Envelope makeExtent(String sMinX, String sMinY, String sMaxX, String sMaxY, String wkid) {
    if (sMinX.length() > 0 && sMaxX.length() > 0 && sMinY.length() > 0 && sMaxY.length() > 0) {
      double minx = Val.chkDbl(sMinX, -180.0);
      double maxx = Val.chkDbl(sMaxX, 180.0);
      double miny = Val.chkDbl(sMinY, -90.0);
      double maxy = Val.chkDbl(sMaxY, 90.0);
      Envelope envelope = new Envelope(minx, miny, maxx, maxy);
      envelope.setWkid(wkid);
      return envelope;
    }
    return null;
  }
}
