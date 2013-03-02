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
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * ArcIMS renderer factory.
 */
/*package*/ class ArcIMSRendererFactory extends MapBasedRendererFactory {

  /** predicate */
  private static final String PREDICATE = "servlet/com.esri.esrimap.Esrimap?ServiceName=";
  /** context path */
  private String contextPath = "";
  /** proxy URL */
  private String proxyUrl = "";

  /**
   * Creates instance of the factory.
   * @param properties properties
   * @param contextPath context path
   * @param proxyUrl proxy url
   */
  public ArcIMSRendererFactory(ILiveDataProperties properties, String contextPath, String proxyUrl) {
    super(properties);
    this.contextPath = contextPath != null ? contextPath.trim() : "";
    this.proxyUrl = proxyUrl != null ? proxyUrl.trim() : "";
  }

  @Override
  public void register(IRegistry reg, final ISetter setter, final String url) {
    int qmark = url.indexOf(PREDICATE);
    if (qmark < 0)
      return;

    String service = url.substring(qmark + PREDICATE.length() + 1);
    if (service == null || service.length() == 0)
      return;

    String strRequest =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<ARCXML version=\"1.1\">"
      + "<REQUEST>"
      + "<GET_SERVICE_INFO fields=\"false\" envelope=\"true\" dataframe=\"#DEFAULT#\" toc=\"false\"/>"
      + "</REQUEST>"
      + "</ARCXML>";

    reg.register(new HttpRequestDefinition(url, strRequest), new IHttpResponseListener() {

      public void onResponse(ResponseInfo info, String strContent, Document docContent) {
        if (docContent != null) {
          try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            if (xPath.evaluate("/ARCXML/RESPONSE/ERROR", docContent, XPathConstants.NODE) == null) {
              Envelope env = null;
              Node ndEnvelope = (Node) xPath.evaluate("/ARCXML/RESPONSE/SERVICEINFO/PROPERTIES/ENVELOPE", docContent, XPathConstants.NODE);
              if (ndEnvelope != null) {
                String minx = Val.chkStr((String) xPath.evaluate("@minx", ndEnvelope, XPathConstants.STRING));
                String maxx = Val.chkStr((String) xPath.evaluate("@maxx", ndEnvelope, XPathConstants.STRING));
                String miny = Val.chkStr((String) xPath.evaluate("@miny", ndEnvelope, XPathConstants.STRING));
                String maxy = Val.chkStr((String) xPath.evaluate("@maxy", ndEnvelope, XPathConstants.STRING));
                String wkid = Val.chkStr((String) xPath.evaluate("/ARCXML/RESPONSE/SERVICEINFO/PROPERTIES/FEATURECOORDSYS/@id", docContent, XPathConstants.STRING), "4326");
                env = makeExtent(minx, miny, maxx, maxy, wkid);
              }
              final Envelope envelope = env;

              final String servUrl = url;

              setter.set(new ArcIMSRenderer() {

                @Override
                protected Envelope getExtent() {
                  return envelope;
                }

                @Override
                protected String getUrl() {
                  return servUrl;
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
