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

import com.esri.gpt.control.webharvest.IterationContext;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 * Implements IEntryProcessor.extractMetadata() to process entry node and
 * extract metadata xml.
 */
public class AGPEntryProcessor implements IEntryProcessor {
  protected final static Logger LOG = Logger.getLogger(AGPEntryProcessor.class.getCanonicalName());

  protected static final Map<String, String> namespaces = new HashMap<String, String>();

  static {
    namespaces.put("xmlns:atom", "http://www.w3.org/2005/Atom");
    namespaces.put("xmlns:dc", "http://purl.org/dc/elements/1.1/");
    namespaces.put("xmlns:geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
    namespaces.put("xmlns:georss", "http://www.georss.org/georss");
  }

  protected final IterationContext context;

  public AGPEntryProcessor(IterationContext context) {
    this.context = context;
  }
  /**
   * Injects namespaces into the XML document.
   * @param mdDoc XML document
   */
  protected void injectNamespaces(Document mdDoc) {
    Element element = mdDoc.getDocumentElement();
    for (Map.Entry<String, String> entry : namespaces.entrySet()) {
      element.setAttribute(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Extracts xml of entry node and returns xml string.
   *
   * @param info the BaseAtomInfo object
   * @param entry the entry node
   * @return the metadata xml string
   */
  @Override
  public String extractMetadata(BaseAtomInfo info, Node entry) {
    String mdText = "";
    try {
      Document mdDoc = DomUtil.newDocument();

      mdDoc.appendChild(mdDoc.importNode(entry, true));
      injectNamespaces(mdDoc);

      mdText = XmlIoUtil.domToString(mdDoc);
      String id = "";
      try {
        id = parseId(mdDoc);
      } catch (XPathExpressionException e) {
        LOG.log(Level.FINE, "Error parsing metadata id", e);
      }

      String georssBox = readBbox(info, id);
      mdText = mdText.replace("</entry>", georssBox + "</entry>");
    } catch (Exception e) {
      LOG.log(Level.FINE, "Error extracting metadata", e);
    }
    return mdText;
  }

  /**
   * Parses Id from atom entry
   *
   * @param doc the feed
   * @return the id
   * @throws XPathExpressionException
   */
  protected String parseId(Document doc) throws XPathExpressionException {
    XPath xPath = AtomNamespaceUtil.makeXPath(true);
    String id = (String) xPath.evaluate("/atom:entry/atom:id/text()", doc, XPathConstants.STRING);
    if (id != null) {
      return id;
    }
    return null;
  }

  /**
   * Makes http request to json endpoint and returns georss box string.
   *
   * @param info the base atom info
   * @param id the item id
   * @return the georss box string.
   * @throws IOException if the http request fails due to i/o exception
   * @throws JSONException if json parsing fails
   */
  protected String readBbox(BaseAtomInfo info, String id) throws IOException, JSONException {
    String url = info.getUrl();
    String georssBox = "";
    if (url.length() > 0) {
      url = url.substring(0, url.indexOf("?"));
      String params = "?q=id:" + id + "&f=json";
      url = url + params;
      context.assertAccess(url);
      HttpClientRequest cr = context.newHttpClientRequest();
      cr.setUrl(url);
      String response = Val.chkStr(cr.readResponseAsCharacters());
      if (response.length() > 0) {
        JSONObject jso = new JSONObject(response);
        if (jso.has("results")) {
          JSONArray results = jso.getJSONArray("results");
          for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            String idJso = result.getString("id");
            if (idJso == null ? id != null : !idJso.equals(id)) {
              continue;
            }
            if (!result.has("extent")) {
              continue;
            }
            JSONArray extentArray = result.getJSONArray("extent");
            if (extentArray != null && extentArray.length() == 2) {
              String[] lowerCorner = Val.chkStr(extentArray.getJSONArray(0).toString()).replaceAll("^\\[|\\]$", "").split(",");
              String[] upperCorner = Val.chkStr(extentArray.getJSONArray(1).toString()).replaceAll("^\\[|\\]$", "").split(",");
              double minx = -180, miny = -90, maxx = 180, maxy = 90;
              if (lowerCorner != null && lowerCorner.length == 2) {
                minx = Val.chkDbl(lowerCorner[0], minx);
                miny = Val.chkDbl(lowerCorner[1], miny);
              }
              if (upperCorner != null && upperCorner.length == 2) {
                maxx = Val.chkDbl(upperCorner[0], maxx);
                maxy = Val.chkDbl(upperCorner[1], maxy);
              }
              georssBox = "<georss:box>" + miny + " " + minx + " " + maxy + " " + maxx + "</georss:box>";
            }

          }
        }
      }
    }
    return georssBox;
  }
}
